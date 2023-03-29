@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

IF NOT DEFINED SERVICE_HOST SET SERVICE_HOST=http://localhost:8080
IF NOT DEFINED AUTH_TOKEN_UCARROLL SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
IF NOT DEFINED AUTH_TOKEN_DCOSTA SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk
IF NOT DEFINED SCOPE SET SCOPE=knb-lter-lno
IF NOT DEFINED IDENTIFIER SET IDENTIFIER=10076
IF NOT DEFINED ENTITY_ID SET ENTITY_ID=LTE_cover_all_years.csv

ECHO DP-1: Create Data Package
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X POST -H "Content-Type: application/xml" -d @test\data\testEML.xml %SERVICE_HOST%/package/eml

ECHO DP-2: Read Data Package
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%/1
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%/newest
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%/oldest

ECHO DP-3: Update Data Package
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X PUT -H "Content-Type: application/xml" -T test\data\testEML2.xml %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%

ECHO DP-5: DP-5 List Data Package Scopes, Identifiers, Revisions, or Data Entities
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%SCOPE%
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/data/eml/%SCOPE%/%IDENTIFIER%/1

ECHO DP-6: Search Data Packages
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X PUT -H "Content-Type: application/xml" -T test\data\pathQuery.xml %SERVICE_HOST%/package/eml/search

ECHO DP-7: Read a Data Package Report
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/report/eml/%SCOPE%/%IDENTIFIER%/1
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -H "Accept: text/html" -G %SERVICE_HOST%/package/report/eml/%SCOPE%/%IDENTIFIER%/1

ECHO DP-8: Evaluate Data Package
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X POST -H "Content-Type: application/xml" -d @test\data\testEML.xml %SERVICE_HOST%/package/evaluate/eml
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -H "Accept: text/html" -X POST -H "Content-Type: application/xml" -d @test\data\testEML.xml %SERVICE_HOST%/package/evaluate/eml

ECHO DP-10: Read a Metadata Document
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X GET %SERVICE_HOST%/package/metadata/eml/%SCOPE%/%IDENTIFIER%/1

ECHO DP-11: Read a Data Entity
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/data/eml/%SCOPE%/%IDENTIFIER%/1/%ENTITY_ID%
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G %SERVICE_HOST%/package/data/eml/%SCOPE%/%IDENTIFIER%/1/%ENTITY_ID%

ECHO DP-4: Delete Data Package
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X DELETE %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%
