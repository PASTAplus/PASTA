@ECHO OFF

SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://package-d.lternet.edu:8080
REM SET SERVICE_HOST=http://package-s.lternet.edu:8080
REM SET SERVICE_HOST=http://pasta.lternet.edu

SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1
SET ENTITY_ID=67e99349d1666e6f4955e9dda42c3cc2

curl -i -X GET "%SERVICE_HOST%/package/name/eml/%SCOPE%/%IDENTIFIER%/%REVISION%/%ENTITY_ID%"
