@ECHO OFF

REM SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=https://pasta-d.lternet.edu
REM SET SERVICE_HOST=https://pasta-s.lternet.edu
SET SERVICE_HOST=https://pasta.lternet.edu

SET SCOPE=knb-lter-jrn
SET BOGUS_SCOPE=xxx
SET IDENTIFIER=210001001
SET REVISION=41

curl -X GET "%SERVICE_HOST%/package/eml"
curl -X GET "%SERVICE_HOST%/package/eml/%SCOPE%"
curl -X GET "%SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%"
curl -X GET "%SERVICE_HOST%/package/data/eml/%SCOPE%/%IDENTIFIER%/%REVISION%
curl -X GET "%SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%"
curl -X GET "%SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%?filter=oldest"
curl -X GET "%SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%?filter=newest"
