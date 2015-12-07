@echo off
cls

set arg1=%1
set web_pckg_dir="C:\Users\Administrator\git\aws.bcscanner.s3web\WebContent"
set s3_bucket="bcscanner-web"
set retrieve_prd_dir="C:\Users\Administrator\git\BCScannerDemo\aws.bcscanner.lambda.product\target"
set retrieve_similar_prd_dir="C:\Users\Administrator\git\BCScannerDemo\aws.bcscanner.lambda.similar\target"
set jar_filename_prd="aws.bcscanner.lambda.product-0.0.1-SNAPSHOT.jar"
set jar_filename_sim_prd="aws.bcscanner.lambda.similar-0.0.1-SNAPSHOT.jar"

::IF arg1=="1" GOTO 

:: Change to directory containing lambda package
pushd %retrieve_prd_dir%
echo directory changed to %retrieve_prd_dir%

:: Update lambda functions
echo starting lambda function RetrieveProduct update process
aws lambda update-function-code --function-name RetrieveProduct --zip-file fileb://%jar_filename_prd% --publish
echo.
echo finished lambda function RetrieveProduct update process

popd
pushd %retrieve_similar_prd_dir%

echo starting lambda function RetrieveSimilarProducts update process
aws lambda update-function-code --function-name RetrieveSimilarProducts --zip-file fileb://%jar_filename_sim_prd% --publish
echo.
echo finished lambda function RetrieveSimilarProducts update process

:: Change to directory containing web (s3) package
popd

:S3ObjectsOnly
pushd %web_pckg_dir%
echo directory changed to %web_pckg_dir%

echo Upload web pages
aws s3 cp home.html s3://%s3_bucket% --exclude "*" --include "*.html"

echo Upload Images
aws s3 cp Images s3://%s3_bucket%/Images --recursive

echo Upload Scripts
aws s3 cp Scripts s3://%s3_bucket%/Scripts --recursive

echo Upload Stylesheets
aws s3 cp Styles s3://%s3_bucket%/Styles --recursive

echo changing directory back to original
popd