@ECHO OFF

REM SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://pasta-d.lternet.edu
REM SET SERVICE_HOST=http://pasta-s.lternet.edu:8080
SET SERVICE_HOST=http://pasta.lternet.edu

REM curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=%22soil%20temperature%22&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=packageid&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=1000"
curl -X GET "%SERVICE_HOST%/package/search/eml?defType=edismax&q=soil&q=temperature&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=packageid&sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"
