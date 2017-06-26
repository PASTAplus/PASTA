@ECHO OFF

REM SET SERVICE_HOST=http://localhost:8888
SET SERVICE_HOST=http://pasta-d.lternet.edu
REM SET SERVICE_HOST=http://pasta-s.lternet.edu:8080
REM SET SERVICE_HOST=http://pasta.lternet.edu

SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1

curl -X GET %SERVICE_HOST%/package/metadata/dc/%SCOPE%/%IDENTIFIER%/%REVISION%
