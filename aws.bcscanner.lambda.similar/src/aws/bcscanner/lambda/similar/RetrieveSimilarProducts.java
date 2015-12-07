package aws.bcscanner.lambda.similar;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import aws.bcscanner.lambda.common.*;

public class RetrieveSimilarProducts implements RequestHandler<String, String> {
	Logger logger;
	
    @Override
    public String handleRequest(String input, Context context) {
    	logger = new Logger(context);
    	logger.write("Input JSON: " + input);
    	
    	// HACK - to deal with Json parsing issues. Possibly use another Json library (org.json)
    	input = input.replace("'", "\"");
    	logger.write("Normalised JSON: " + input);
    	
    	// Parse input
    	JsonReader reader = Json.createReader(new StringReader(input));
        JsonObject paramsObj = reader.readObject();
        reader.close();
        
        // Extract input
        String asin = paramsObj.getString("asin");
        logger.write("ASIN: " + asin);
        
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
        
        // Create REST parameters
        String queryString = "Service=AWSECommerceService&Operation=SimilarityLookup&ResponseGroup=Images,ItemAttributes";
        queryString += "&ItemId=" + asin;
        requestUrl = helper.sign(queryString);

        // Fetch response
 		String responseJson = fetchResponse(requestUrl, asin);
 		return responseJson;
    }
    
    /*
	 * Utility function to fetch the response from the service.
	 */
	private String fetchResponse(String requestUrl, String asin) {
		JsonObject myObj = null;
		
		NodeList items = fetchItems(requestUrl);
        if (items != null)
        {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            
            // Limit number of items retrieved
            int itemsRetrieved = items.getLength();
            int itemLimit = itemsRetrieved > Constants.NUM_SIMILAR_ITEM_LIMIT ? Constants.NUM_SIMILAR_ITEM_LIMIT : itemsRetrieved;
            
            JsonArrayBuilder jab = Json.createArrayBuilder();
            
            for (int i=0; i<itemLimit; i++)
            {
                Node item = items.item(i);
                try {
                    String detailPageUrl = xpath.evaluate("DetailPageURL/text()", item);
                    String smlImageUrl = xpath.evaluate("SmallImage/URL/text()", item);
                    String smlImageH = xpath.evaluate("SmallImage/Height/text()", item);
                    String smlImageW = xpath.evaluate("SmallImage/Width/text()", item);
                    String medImageUrl = xpath.evaluate("MediumImage/URL/text()", item);
                    String medImageH = xpath.evaluate("MediumImage/Height/text()", item);
                    String medImageW = xpath.evaluate("MediumImage/Width/text()", item);
                    String title = xpath.evaluate("ItemAttributes/Title/text()", item);
                    String price = xpath.evaluate("ItemAttributes/ListPrice/FormattedPrice/text()", item);
                    String currency = xpath.evaluate("ItemAttributes/ListPrice/CurrencyCode/text()", item);
                    String prdAsin = xpath.evaluate("ASIN/text()", item);

                    jab.add(Json.createObjectBuilder()
                        .add("success", "true")
                        .add("itemInfo", Json.createObjectBuilder()
                            .add("detailPageUrl", detailPageUrl)
                            .add("title", title)
                            .add("price", price)
                            .add("currency", currency)
                            .add("asin", prdAsin)
                            .add("smlImage", Json.createObjectBuilder()
                                .add("url", smlImageUrl)
                                .add("height", smlImageH)
                                .add("width", smlImageW))
                            .add("medImage", Json.createObjectBuilder()
                                .add("url", medImageUrl)
                                .add("height", medImageH)
                                .add("width", medImageW))));
                } catch (XPathExpressionException xe) {
    				logger.write("ERROR: Problem experienced parsing item XML. Message: " + xe.getMessage());
    				jab.add(Json.createObjectBuilder()
                            .add("success", "false")
    						.add("error", "An error has occurred. Please see CloudWatch logs for details."));
                }
            }
            
            JsonArray myArray = jab.build();
            return myArray.toString();
        }
        else
        {
            myObj = Json.createObjectBuilder()
                .add("success", "false")
                .add("error", "No similar products found for the ASIN: " + asin + ".")
                .build();
            
            return myObj.toString();
        }
	}
   
	/*
	 * Utility function to extract the items from the XML response.
	 */
	private static NodeList fetchItems(String requestUrl) {
       try {
           DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
           DocumentBuilder db = dbf.newDocumentBuilder();
           Document doc = db.parse(requestUrl);
           
           if ((doc != null) &&
                   doc.getElementsByTagName("Item").getLength() > 0) {
               // Return all items
               return doc.getElementsByTagName("Item");
           }
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
       return null;
   }
}
