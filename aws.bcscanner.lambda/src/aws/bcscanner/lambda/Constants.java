/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */

package aws.bcscanner.lambda;

public class Constants {
    /*
     * Your AWS Access Key ID, as taken from the AWS Your Account page.
     */
    public static final String AWS_ASSOCIATE_TAG = "<INSERT>";     
    /*
     * Your AWS Access Key ID, as taken from the AWS Your Account page.
     */
    public static final String AWS_ACCESS_KEY_ID = "<INSERT>";

    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */
    public static final String AWS_SECRET_KEY = "<INSERT>";

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
    
    public static final int NUM_SIMILAR_ITEM_LIMIT = 5;
}
