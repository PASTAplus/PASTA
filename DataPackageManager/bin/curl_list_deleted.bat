@ECHO OFF

REM SET SERVICE_HOST=http://localhost:8888
SET SERVICE_HOST=http://pasta-d.lternet.edu
REM SET SERVICE_HOST=http://pasta-s.lternet.edu
REM SET SERVICE_HOST=http://pasta.lternet.edu

curl -X GET "%SERVICE_HOST%/package/eml/deleted
