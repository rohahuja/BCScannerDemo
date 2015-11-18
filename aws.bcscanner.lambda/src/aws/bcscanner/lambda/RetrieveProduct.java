package aws.bcscanner.lambda;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class RetrieveProduct implements RequestHandler<String, String> {

	@Override
	public String handleRequest(String input, Context context) {
		Logger logger = new Logger(context);
		
	    logger.write("Input JSON: " + input);

		// TODO HACK - to deal with Json parsing issues. Possibly use another Json
		// library (org.json)
		input = input.replace("'", "\"");
		logger.write("Normalised JSON: " + input);

		JsonReader reader = Json.createReader(new StringReader(input));
		JsonObject paramsObj = reader.readObject();
		reader.close();

		String barcode = paramsObj.getString("barcode");
		String rawIdType = paramsObj.getString("format");
		logger.write("Barcode / Format: " + barcode + " / " + rawIdType);

		SignedRequestsHelper helper;
		try {
			helper = SignedRequestsHelper.getInstance(Constants.ENDPOINT, Constants.AWS_ACCESS_KEY_ID,
					Constants.AWS_SECRET_KEY, Constants.AWS_ASSOCIATE_TAG);
		} catch (Exception e) {
			logger.write("ERROR: " + e.getMessage());
			JsonObject myObj = Json.createObjectBuilder()
					.add("success", "false")
					.add("error", "An error has occurred. Please see CloudWatch logs for details.")
					.build();
			return myObj.toString();
		}

		String requestUrl = null;
		JsonObject myObj = null;
		String idType = getAmazonIdType(rawIdType);

		// TODO - Break up and move the query string params into a config file
		String queryString = "Service=AWSECommerceService&Operation=ItemLookup&ResponseGroup=Images,ItemAttributes&SearchIndex=All&IdType="
				+ idType + "&ItemId=" + barcode;
		requestUrl = helper.sign(queryString);

		Node item = fetchItem(requestUrl);
		if (item != null) {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			try {
				String detailPageUrl = xpath.evaluate("//DetailPageURL/text()", item);
				String medImageUrl = xpath.evaluate("//MediumImage/URL/text()", item);
				String medImageH = xpath.evaluate("//MediumImage/Height/text()", item);
				String medImageW = xpath.evaluate("//MediumImage/Width/text()", item);
				String title = xpath.evaluate("//ItemAttributes/Title/text()", item);
				String price = xpath.evaluate("//ItemAttributes/ListPrice/FormattedPrice/text()", item);
				String currency = xpath.evaluate("//ItemAttributes/ListPrice/CurrencyCode/text()", item);
				String asin = xpath.evaluate("//ASIN/text()", item);

				myObj = Json.createObjectBuilder().add("success", "true")
						.add("itemInfo", Json.createObjectBuilder()
								.add("detailPageUrl", detailPageUrl)
								.add("title", title)
								.add("price", price)
								.add("currency", currency)
								.add("asin", asin)
								.add("medImage", Json.createObjectBuilder()
										.add("url", medImageUrl)
										.add("height", medImageH)
										.add("width", medImageW)))
						.build();
			} catch (XPathExpressionException xe) {
				logger.write("ERROR: Problem experienced parsing item XML. Message: " + xe.getMessage());
				myObj = Json.createObjectBuilder()
						.add("success", "false")
						.add("error", "An error has occurred. Please see CloudWatch logs for details.")
						.build();
			}
		} else {
			myObj = Json.createObjectBuilder()
					.add("success", "false")
					.add("error", "No items found for the barcode: " + barcode + ". Please try another.")
					.build();
		}

		return myObj.toString();
	}

	/*
	 * Utility function to fetch the response from the service and extract the
	 * title from the XML.
	 */
	private Node fetchItem(String requestUrl) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(requestUrl);

			if ((doc != null) && doc.getElementsByTagName("Item").getLength() > 0) {
				// Return just the first item
				return doc.getElementsByTagName("Item").item(0);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private String getAmazonIdType(String paramIdType) {
		if (paramIdType.toLowerCase().contains("upc"))
			return "UPC";
		else
			return "EAN";
	}
}
