@ECHO OFF

SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=https://pasta-d.lternet.edu
REM SET SERVICE_HOST=https://pasta-s.lternet.edu
REM SET SERVICE_HOST=https://pasta.lternet.edu

REM curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=%22soil%20temperature%22&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=packageid&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=1000"
REM curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=soil&q=temperature&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=packageid&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"
REM curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=water+balance&fl=*&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"
REM curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=*&sort=packageid,asc&debug=false&start=0&rows=100&fl=*"
curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=bugs&start=0&rows=5&fl=packageid,score"
