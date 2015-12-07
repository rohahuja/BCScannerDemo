/**
 * 
 */
package aws.bcscanner.lambda.common;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Utility class to bundle common utility functions in
 */
public class Utility {
	
	/*
	 * Retrieves the AWS Managed Key encrypted file from S3 that contains the
	 * credentials to access Amazon's Product Advertising API
	 */
	public static JsonObject getProductAdvertisingAPICredentials(Context context) {
		Logger logger = new Logger(context);
	    logger.write("Getting credentials from S3");
	    
		System.setProperty(SDKGlobalConfiguration.ENFORCE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        AmazonS3 s3 = new AmazonS3Client();
        Region region = Region.getRegion(Constants.REGION);
        s3.setRegion(region);
        
        String bucketName = Constants.API_CREDENTIALS_BUCKET;
        String key = Constants.API_CREDENTIALS_KEY;
        
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
        JsonReader reader = Json.createReader(object.getObjectContent());
        return reader.readObject();
	}
}
