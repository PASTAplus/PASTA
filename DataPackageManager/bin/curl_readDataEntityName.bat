@ECHO OFF

SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://package-d.lternet.edu:8080
REM SET SERVICE_HOST=http://package-s.lternet.edu:8080
REM SET SERVICE_HOST=http://pasta.lternet.edu

SET SCOPE=knb-lter-mcm
SET IDENTIFIER=82
SET REVISION=1
SET ENTITY_ID=d94e195d55256c68cafb762399346f6a

curl -i -X GET "%SERVICE_HOST%/package/name/eml/%SCOPE%/%IDENTIFIER%/%REVISION%/%ENTITY_ID%"
