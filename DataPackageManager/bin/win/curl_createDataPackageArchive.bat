@ECHO OFF

SET SERVICE_HOST=http://localhost:8888
SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1

ECHO Create Data Package Archive
curl -i -H "Expect:" -X POST %SERVICE_HOST%/package/archive/eml/%SCOPE%/%IDENTIFIER%/%REVISION%
