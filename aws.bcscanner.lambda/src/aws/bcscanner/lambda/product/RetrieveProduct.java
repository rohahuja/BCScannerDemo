package aws.bcscanner.lambda.product;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

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

import com.amazonaws.AmazonClientException;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import aws.bcscanner.lambda.common.*;

public class RetrieveProduct implements RequestHandler<String, String> {
	Logger logger;
	
	@Override
	public String handleRequest(String input, Context context) {
		logger = new Logger(context);
	    logger.write("Input JSON: " + input);

		// TODO HACK - to deal with Json parsing issues. Possibly use another Json
		// library (org.json)
		input = input.replace("'", "\"");
		logger.write("Normalised JSON: " + input);

		// Parse input
		JsonReader reader = Json.createReader(new StringReader(input));
		JsonObject paramsObj = reader.readObject();
		reader.close();
		
		// Extract input
		String barcode = paramsObj.getString("barcode");
		String rawIdType = paramsObj.getString("format");
		logger.write("Barcode / Format: " + barcode + " / " + rawIdType);

		// Extract credentials
		JsonObject apiCredentials = Utility.getProductAdvertisingAPICredentials(context);
        String assocTag = apiCredentials.getString("awsAssocTag");
        String accessKey = apiCredentials.getString("awsAccessKeyId");
        String secretKey = apiCredentials.getString("awsSecretKey");
        logger.write("Credentials retrieved from S3: " + assocTag);
        
        // Create request
		SignedRequestsHelper helper;
		try {
			helper = SignedRequestsHelper.getInstance(Constants.ENDPOINT, 
					accessKey, secretKey, assocTag);
		} catch (Exception e) {
			logger.write("ERROR: " + e.getMessage());
			JsonObject myObj = Json.createObjectBuilder()
					.add("success", "false")
					.add("error", "An error has occurred. Please see CloudWatch logs for details.")
					.build();
			return myObj.toString();
		}

		String requestUrl = null;
		String idType = getAmazonIdType(rawIdType);

		// Create REST parameters
		String queryString = "Service=AWSECommerceService&Operation=ItemLookup&ResponseGroup=Images,ItemAttributes&SearchIndex=All&IdType="
				+ idType + "&ItemId=" + barcode;
		requestUrl = helper.sign(queryString);

		// Fetch response
		String responseJson = fetchResponse(requestUrl);
		return responseJson; 
	}

	/*
	 * Utility function to fetch the response from the service.
	 */
	private String fetchResponse(String requestUrl) {
		JsonObject myObj = null;
		
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
					.add("error", "No items found for this barcode. Please try another.")
					.build();
		}

		return myObj.toString();
	}
	
	/*
	 * Utility function to extract the title from the XML response.
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

	/* 
	 * Return normalised barcode type compatible with REST call
	 */
	private String getAmazonIdType(String paramIdType) {
		if (paramIdType.toLowerCase().contains("ean"))
			return "EAN";
		else
			return "UPC";
	}
}
