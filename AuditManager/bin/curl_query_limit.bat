@ECHO OFF

SET PASSWORD=%1

REM SET SERVICE_HOST=http://localhost:8888
SET SERVICE_HOST=http://pasta-s.lternet.edu

curl -i -u "uid=LNO,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD%" -X GET "%SERVICE_HOST%/audit/report?category=info&serviceMethod=readMetadata&fromTime=2015-09-22&limit=10"
