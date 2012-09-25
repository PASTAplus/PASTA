@ECHO OFF

SET SERVICE_HOST=http://localhost:8080
REM SET SERVICE_HOST=http://package.lternet.edu:8080
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk
SET SCOPE=knb-lter-lno
SET IDENTIFIER=4057
SET ENTITY_ID=NoneSuchBugCount
SET EML_FILE=test\data\NoneSuchBugCount.xml
REM SET EML_FILE=test\data\NoneSuchBugCountBadData.xml
REM SET EML_FILE=test\data\NoneSuchBugCountBadPackageId.xml
REM SET EML_FILE=test\data\NoneSuchBugCountBadReference.xml
REM SET EML_FILE=test\data\NoneSuchBugCountDuplicateEntity.xml
REM SET EML_FILE=test\data\NoneSuchBugCountTooFewCols.xml
REM SET EML_FILE=test\data\NoneSuchBugCountTooManyCols.xml
REM SET EML_FILE=test\data\NoneSuchBugCountTwoURLs.xml
REM SET EML_FILE=test\data\Level-1-EML.xml
REM SET EML_FILE=test\data\knb-lter-gce.1.9.xml
REM SET EML_FILE=test\data\ecotrends.14326.1.xml
REM SET EML_FILE=5709.xml
REM SET EML_FILE=%DOWNLOADS%\ecotrends.8300.1.xml
REM SET EML_FILE=%DOWNLOADS%\knb-lter-vcr.151.8.xml
REM SET EML_FILE=%DOWNLOADS%\obrien_test_sbc40.1.1.xml
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X POST -H "Content-Type: application/xml" -d @test\data\NoneSuchBugCount.xml %SERVICE_HOST%/package/evaluate/eml
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X POST -H "Content-Type: application/xml" -H "Accept: text/html" -d @test\data\NoneSuchBugCount.xml %SERVICE_HOST%/package/evaluate/eml
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X POST -H "Content-Type: application/xml" -d @%EML_FILE% %SERVICE_HOST%/package/evaluate/eml
