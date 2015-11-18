/**
 * 
 */

$(document).ready(function () {
    var params = {
        AccountId: "454979696062",
        RoleArn: "arn:aws:iam::454979696062:role/Cognito_BCScannerUnauth_Role",
        IdentityPoolId: "us-east-1:6c84f4c2-783c-40fe-9e15-57923e2924da"
    };

    // set the Amazon Cognito region
    AWS.config.region = 'us-east-1';

    // initialize the Credentials object with our parameters
    AWS.config.credentials = new AWS.CognitoIdentityCredentials(params);
});

function readBarcode(e) {
	// TODO - required for testing purposes
	retrieveProductDetailsRequest("073852400908", "upc_a"); return;
	//retrieveProductDetailsRequest("036000216066", "upc_a"); return;
	//retrieveProductDetailsRequest("740985227909", "upc_a"); return;
    //retrieveSimilarProductsRequest("B000BBYATK"); return;
	
	initCamera();
}

function retrieveProductDetailsRequest(barcode, format) {
    var $outerElement = $('#main-product');
    displayLoading($outerElement);
    
	var dataJson = JSON.stringify("{ 'barcode': '" + barcode + "', 'format': '" + format + "' }");
    
	// TODO
	//alert('lambda invoke started');
	
	var lambda = new AWS.Lambda();
    lambda.invoke({
	        FunctionName: "RetrieveProduct",
	        Payload: dataJson
	    }, 
	    function(err, data){
	    	if (err == null) {
	    		retrieveProductResponse(JSON.parse(JSON.parse(data.Payload)), barcode);
	    	}
	    	else {
	    		console.log("Terminal error: " + textStatus);
	            showError(jqXHR.responseText);
	    	}
	    }
    );
    
    // TODO
    //alert('lambda invoke ended');
}

function retrieveSimilarProductsRequest(asin) {
    if (asin == -1) {
        showError('No ASIN found to query similar products with.');
        return;
    }
        
    var $outerElement = $('#similar-products');
    displayLoading($outerElement);
    
    var asinJson = JSON.stringify("{ 'asin': '" + asin + "' }");
    
    // TODO
	//alert('lambda invoke 2 started');
	
    var lambda = new AWS.Lambda();
    lambda.invoke({
	        FunctionName: "RetrieveSimilarProducts",
	        Payload: asinJson
	    }, 
	    function(err, responseData){
	        var data = JSON.parse(JSON.parse(responseData.Payload));
	        if(err == null) {
	        	retrieveSimilarProductsResponse(data, $outerElement);
	        }
	        else {
	        	console.log("Terminal request error: " + textStatus);
	            showError(jqXHR.responseText);
	        }
	    }
    );
    
    // TODO
    //alert('lambda invoke 2 ended');
}