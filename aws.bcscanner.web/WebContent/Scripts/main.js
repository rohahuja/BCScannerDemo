var simPrdJsonArr = null;
var detectShake;

$(document).ready(function () {
    detectShake = new Shake({
        threshold: 15, // optional shake strength threshold
        timeout: 1000 // optional, determines the frequency of event generation
    });

    // bind phone shake event to window
    $(window).on('shake', showNextProduct);
    // ensure shake event is not detected until content is loaded later
    detectShake.stop();

    // launch barcode scanner 
    //$('#file-chooser').on('change', readBarcode);
    $('#file-chooser').on('click', readBarcode);
    
    // process detected barcode
    Quagga.onDetected(barcodeDetected);
    
    initialiseDialogBoxes();
    
    //Stops the submit request
    $("#ajax-request-form").submit(function(e){
        e.preventDefault();
    });
    
    // device detection
    detectForMobile();
});

function initialiseDialogBoxes() {
    // initialise dialogs
    $( '.dialog' ).dialog({ autoOpen: false });
    $( '.dialog' ).dialog('option', 'modal', true);
    $( '.dialog' ).dialog('option', 'show', { effect: 'fadeIn', duration: 1000 });
    $( '.dialog' ).dialog('option', 'hide', { effect: 'fadeOut', duration: 1000 });
    
    $( '#detected-dialog' ).dialog('option', 'height', 100);
    
    var $bcInfoDialog = $( '#barcode-info-dialog' );
    $bcInfoDialog.dialog('option', 'height', 280);
    $bcInfoDialog.dialog('option', 'width', 170);
    
    $( '#barcode-info' ).on('click', function() {
    	$bcInfoDialog.dialog('open');
    });
}

function barcodeDetected(data) {
    // turn off the camera
    Quagga.stop();
    
    // hide the camera div
    var $cameraDiv = $('#interactive');
    if (!$cameraDiv.is(':hidden')) {
        $cameraDiv.slideUp();
    }        
    
    // process the barcode
    if (data && data.codeResult != undefined) {
        var $dispBarcode = $( "#detected-dialog" );
        $dispBarcode.dialog('open').dialog('close');
        
        //disable the button until we get the response
        $('#file-chooser').attr('disabled', true);
        showInfo(data.codeResult.code);
        
        // get product details via ajax
        retrieveProductDetailsRequest(data.codeResult.code, data.codeResult.format);
    }
    else {
        showError('Unreadable barcode detected.<br/>Please try again or scan another.');
    }
}

function initCamera() {
    var $cameraDiv = $('#interactive');
    // turn on camera
    if ($cameraDiv.is(':hidden')) {
        $cameraDiv.slideDown();
    }
    else {
        // turn off camera
        $cameraDiv.slideUp();
        Quagga.stop();
        return;
    }
    
    // stop the shake detection until content is loaded later
    detectShake.stop();

    // turn on the camera
    App.init();
    
    // clear all content divs
    $('#main-product').html('');
    $('#similar-products').html('');
    $('#info').html('');
    $('#err').html('');
    
    // TODO
    $('#info').html('in initCamera');
}

function retrieveProductResponse(data, barcode) {
    if(data.success){
        bindMainProduct(data.itemInfo);
        asin = data.itemInfo.asin;
        showInfo("<b>Barcode used:</b> " + barcode);
        retrieveSimilarProductsRequest(asin);
    } 
    //display error message
    else {
        console.log("Error retrieving products: " + obj.error);
        showError("Something bad happened during product retrieval.");
    }
}

function retrieveSimilarProductsResponse(data, $outerElement) {
	if (data.length > 0) {
        $outerElement.html("<div id='sim-prd-text'>Similar products (<span class='shake'>to view</span>):</div>");
        
        for (i=0; i<data.length; i++)
        {
            item = data[i];
            
            if (item.itemInfo.smlImage.url == "") {
            	item.itemInfo.smlImage.width = "75";
            	item.itemInfo.smlImage.height = "75";
            	item.itemInfo.smlImage.url = "./Images/no-img-sml.png";
            }
            
            if (item.success) {
                var tmplData = {
                    imageUrl: item.itemInfo.smlImage.url,
                    imageH: item.itemInfo.smlImage.height,
                    imageW: item.itemInfo.smlImage.width,
                    title: item.itemInfo.title,
                    detailPageUrl: item.itemInfo.detailPageUrl
                };
                
                $('#tmpl-similar-prd').tmpl(tmplData).appendTo($outerElement);
            }
            else {
                console.log("Error displaying item: " + item.itemInfo.title);
            }
        }
        
        productsFound = true;
        // save Json array for use later
        simPrdJsonArr = data;
        showInfo("<b>ASIN used:</b> " + asin);
    } 
    //display error message
    else {
        $outerElement.html("There are no similar products available.");
        console.log("Error retrieving products: " + data.error);
    }
	
	$('#file-chooser').attr('disabled', false);

    if (productsFound) {
        detectShake.start();
    }
    else {
        detectShake.stop();
    }
}

//<Utility Method>
function showNextProduct() {
    var simPrdSpans = $('#similar-products').children('span');
    if (simPrdSpans.length > 0 && simPrdJsonArr.length > 0) {
        // get next similar product and display as main product
        simPrdSpans[0].remove();
        bindMainProduct(simPrdJsonArr.shift().itemInfo);
        
        if (simPrdSpans.length == 1) {
            $('#sim-prd-text').html("No similar products left to view. Try another barcode!");
        }
    }
}

function bindMainProduct(itemInfo) {
    //alert("bindmain start");
	if (itemInfo.medImage.url == "") {
    	itemInfo.medImage.width = "160";
    	itemInfo.medImage.height = "160";
    	itemInfo.medImage.url = "./Images/no-img-med.png";
    }
    
	var tmplData = {
        detailPageUrl: itemInfo.detailPageUrl,
        imageUrl: itemInfo.medImage.url,
        imageH: itemInfo.medImage.height,
        imageW: itemInfo.medImage.width,
        title: itemInfo.title,
        price: itemInfo.price,
        currency: itemInfo.currency
    };

    $outerElement = $('#main-product');
    $outerElement.html('');
    $('#tmpl-main-prd').tmpl(tmplData).appendTo($outerElement);
    //alert("bindmain end");
}

function displayLoading($outerElement) {
	$outerElement.html('');
    $outerElement.html('<div class=\'loading\'></div>');
}

function detectForMobile() {
    // device detection
    if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|ipad|iris|kindle|Android|Silk|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(navigator.userAgent) 
		|| /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(navigator.userAgent.substr(0, 4))) {
        isMobile = true;
        showInfo(navigator.userAgent);
    }
    else {
        showError("Please browse using a mobile device.");
    }
}

function resetFormState() {
    $('#main-product').html('');
    $('#similar-products').html('');
    $('#file-chooser').attr('disabled', false);
}

function showInfo(infoMsg) {
    var infoDiv = $('#info');
    var errDiv = $('#err');
    
    infoDiv.show()
        .html(infoMsg);
    errDiv.hide();
    
    //resetFormState();
}

function showError(errMsg) {
    var infoDiv = $('#info');
    var errDiv = $('#err');

    infoDiv.hide();
    errDiv.show()
        .html(errMsg);

    resetFormState();
}

