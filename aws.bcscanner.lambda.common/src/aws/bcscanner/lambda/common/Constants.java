/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
package aws.bcscanner.lambda.common;

import com.amazonaws.regions.*;

public class Constants {
    /*
     * All strings are handled as UTF-8
     */
	public static final String UTF8_CHARSET = "UTF-8";
    
    /*
     * The HMAC algorithm required by Amazon
     */
    public static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    /*
     * This is the URI for the service, don't change unless you really know
     * what you're doing.
     */
    public static final String REQUEST_URI = "/onca/xml";
    
    /*
     * The sample uses HTTP GET to fetch the response. If you changed the sample
     * to use HTTP POST instead, change the value below to POST. 
     */
    public static final String REQUEST_METHOD = "GET";
    
    /*
     * Use one of the following end-points, according to the region you are
     * interested in:
     * 
     *      US: ecs.amazonaws.com 
     *      CA: ecs.amazonaws.ca 
     *      UK: ecs.amazonaws.co.uk 
     *      DE: ecs.amazonaws.de 
     *      FR: ecs.amazonaws.fr 
     *      JP: ecs.amazonaws.jp
     * 
     */
    public static final String ENDPOINT = "ecs.amazonaws.com";
    
    /*
     * The limit applied to the number of similar items retrieved
     */
    public static final int NUM_SIMILAR_ITEM_LIMIT = 5;
    
    /*
     * The region the S3 bucket containing Product Advertising API credentials
     * is stored in
     */
    public static final Regions REGION = Regions.US_EAST_1;
    
    /*
     * The key of the S3 bucket item that contains the credentials
     */
    public static final String API_CREDENTIALS_KEY = "prd_adv_credentials.json";
    
    /*
     * The name of the S3 bucket that contains the credentials
     */
    public static final String API_CREDENTIALS_BUCKET = "bcscanner-kms";
}
