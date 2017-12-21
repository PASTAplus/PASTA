@ECHO OFF

REM SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=https://pasta-d.lternet.edu
REM SET SERVICE_HOST=https://pasta-s.lternet.edu
SET SERVICE_HOST=https://pasta.lternet.edu

SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1

REM Use this entity_id on development where knb-lter-nin.1.1 was added more recently
REM SET ENTITY_ID=67e99349d1666e6f4955e9dda42c3cc2

REM Use this entity_id on production where knb-lter-nin.1.1 was add early in PASTA's history
SET ENTITY_ID=DailyWaterSample-NIN-LTER-1978-1992

curl -X GET "%SERVICE_HOST%/package/rmd/eml/%SCOPE%/%IDENTIFIER%/%REVISION%"
curl -X GET "%SERVICE_HOST%/package/metadata/rmd/eml/%SCOPE%/%IDENTIFIER%/%REVISION%"
curl -X GET "%SERVICE_HOST%/package/report/rmd/eml/%SCOPE%/%IDENTIFIER%/%REVISION%"
curl -X GET "%SERVICE_HOST%/package/data/rmd/eml/%SCOPE%/%IDENTIFIER%/%REVISION%/%ENTITY_ID%"
