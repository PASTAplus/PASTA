
REM SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://package-d.lternet.edu:8080
REM SET SERVICE_HOST=http://package-s.lternet.edu:8080
SET SERVICE_HOST=http://pasta.lternet.edu
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk

REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=fish&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=*&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=singledate:*&q=begindate:*&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=id,pubdate,begindate,enddate,singledate&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"
REM curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=%22soil%20temperature%22&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=packageid&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=1000"
curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=soil&q=temperature&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=packageid&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"
