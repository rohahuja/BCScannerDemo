@echo off
cls

set arg1=%1
set web_pckg_dir="C:\Projects\aws.bcscanner.web\WebContent"
set s3_bucket="<INSERT>"
set lambda_dir="C:\Projects\aws.bcscanner.lambda\target"
set jar_filename="aws.bcscanner.lambda-0.0.1-SNAPSHOT.jar"

::IF arg1=="1" GOTO 

:: Change to directory containing lambda package
pushd %lambda_dir%
echo directory changed to %lambda_dir%

:: Update lambda functions
echo starting lambda function RetrieveProduct update process
::aws lambda update-function-code --function-name RetrieveProduct --zip-file fileb://%jar_filename% --publish
echo.
echo finished lambda function RetrieveProduct update process

echo starting lambda function RetrieveSimilarProducts update process
::aws lambda update-function-code --function-name RetrieveSimilarProducts --zip-file fileb://%jar_filename% --publish
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
::aws s3 cp Images s3://%s3_bucket%/Images --recursive

echo Upload Scripts
aws s3 cp Scripts s3://%s3_bucket%/Scripts --recursive

echo Upload Stylesheets
aws s3 cp Styles s3://%s3_bucket%/Styles --recursive

echo changing directory back to original
popd