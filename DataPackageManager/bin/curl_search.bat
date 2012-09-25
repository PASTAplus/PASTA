@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

IF NOT DEFINED SERVICE_HOST SET SERVICE_HOST=http://localhost:8080
IF NOT DEFINED AUTH_TOKEN_UCARROLL SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
IF NOT DEFINED AUTH_TOKEN_DCOSTA SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk

ECHO DP-6: Search Data Packages
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X PUT -H "Content-Type: application/xml" -T test\data\pathQuery.xml %SERVICE_HOST%/package/eml/search
