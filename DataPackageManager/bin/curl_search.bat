@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

REM SET SERVICE_HOST=http://localhost:8080
REM SET SERVICE_HOST=http://package-d.lternet.edu:8080
SET SERVICE_HOST=http://package-s.lternet.edu:8080
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk

ECHO DP-6: Search Data Packages
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=fish&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=*&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=lter&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=title,author,pubdate,packageid,doi,funding&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"
