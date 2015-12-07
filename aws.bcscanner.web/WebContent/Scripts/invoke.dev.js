/**
 * 
 */

function readBarcode(e) {
	// TODO - required for testing purposes
	//retrieveProductDetailsRequest("073852400908", "upc_a"); return;
	//retrieveProductDetailsRequest("036000216066", "upc_a"); return;
	retrieveProductDetailsRequest("740985227909", "upc_a"); return;
    //retrieveSimilarProductsRequest("B000BBYATK"); return;
    
	initCamera();
}

function retrieveProductDetailsRequest(barcode, format) {
    var $outerElement = $('#main-product');
    displayLoading($outerElement);
    
	// TODO - Comment for Release
    //dataString = barcode + "::" + format;
    //var datastring = "barcode=" + barcode;
    //datastring += "&format=" + format;
    
    var dataJson = "json=" + JSON.stringify("{ 'barcode': '" + barcode + "', 'format': '" + format + "' }");
    
    //make the AJAX request, dataType is set to json
    //meaning we are expecting JSON data in response from the server
    // TODO - Change url, data properties for Release
    $.ajax({
        type: "POST",
        url: "RetrieveProductWrapper",
        data: dataJson, //JSON.stringify(datastring),
        dataType: "json",

        //if received a response from the server
        success: function (data, textStatus, jqXHR) {
    		//alert(data);
        	retrieveProductResponse(data);
        },

        //If there was no response from the server
        error: function(jqXHR, textStatus, errorThrown){
            console.log("Terminal error: " + textStatus);
            showError(jqXHR.responseText);
        },

        //capture the request before it was sent to server
        beforeSend: function(jqXHR, settings){
        },

        //this is called after the response or error functions are finsihed
        //so that we can take some action
        complete: function(jqXHR, textStatus){
        }
    });   
}

function retrieveSimilarProductsRequest(asin) {
    if (asin == -1) {
        showError('No ASIN found to query similar products with.');
        return;
    }
        
    // clear similar products div
    var $outerElement = $('#similar-products');
    displayLoading($outerElement);
    
    var dataString = 'json=' + JSON.stringify("{ 'asin': '" + asin + "' }");
    
    //make the AJAX request, dataType is set to json
    //meaning we are expecting JSON data in response from the server
    // -- Change url upon deployment
    $.ajax({
        type: "POST",
        url: "RetrieveSimilarProductsWrapper",
        data: dataString,
        dataType: "json",

        //if received a response from the server
        success: function (data, textStatus, jqXHR) {
        	retrieveSimilarProductsResponse(data, $outerElement);
        },

        //If there was no response from the server
        error: function(jqXHR, textStatus, errorThrown){
            console.log("Terminal request error: " + textStatus);
            showError(jqXHR.responseText);
        },

        //capture the request before it was sent to server
        beforeSend: function(jqXHR, settings){
        },

        //this is called after the response or error functions are finsihed
        //so that we can take some action
        complete: function(jqXHR, textStatus){
        }
    });        
}