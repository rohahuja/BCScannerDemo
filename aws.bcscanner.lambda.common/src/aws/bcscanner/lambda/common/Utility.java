/**
 * 
 */
package aws.bcscanner.lambda.common;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * @author Administrator
 *
 */
public class Utility {
	
	public static JsonObject getProductAdvertisingAPICredentials(Context context) {
		Logger logger = new Logger(context);
	    logger.write("Getting Credentials in Utility class");
	    
		System.setProperty(SDKGlobalConfiguration.ENFORCE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        AmazonS3 s3 = new AmazonS3Client();
        Region region = Region.getRegion(Constants.REGION);
        s3.setRegion(region);
        
        String bucketName = Constants.API_CREDENTIALS_BUCKET;
        String key = Constants.API_CREDENTIALS_KEY;
        
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
        JsonReader reader = Json.createReader(object.getObjectContent());
        return reader.readObject();
        
        //JsonObject credsObj = (getFileContents(object.getObjectContent()));
        //return credsObj;
	}

    /**
     * Displays the contents of the specified input stream as text.
     *
     * @param input
     *            The input stream to display as text.
     *
     * @throws IOException
     */
    /*private JsonObject getFileContents(InputStream input) {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    	StringBuilder strBuilder = new StringBuilder();
    	String inputStr;
    	
    	while ((inputStr = reader.readLine()) != null) {
    		strBuilder.append(inputStr);
    	}
    	
    	return strBuilder;
    }*/
}
