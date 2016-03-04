@ECHO OFF

REM SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://package-d.lternet.edu:8080
REM SET SERVICE_HOST=http://package-s.lternet.edu:8080
SET SERVICE_HOST=http://pasta.lternet.edu

curl -X GET "%SERVICE_HOST%/package/eml/deleted
