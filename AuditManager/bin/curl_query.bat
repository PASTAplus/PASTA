@ECHO OFF

SET PASSWORD=%1

REM SET SERVICE_HOST=http://localhost:8888
SET SERVICE_HOST=http://pasta.lternet.edu

REM curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD%" -X GET "%SERVICE_HOST%/audit/report?category=info&serviceMethod=createDataPackage&fromTime=2015-09-22"
REM curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD%" -X GET "%SERVICE_HOST%/audit/report?category=info&user=uid=SBC,o=LTER,dc=ecoinformatics,dc=org&serviceMethod=createDataPackage&serviceMethod=updateDataPackage&fromTime=2012-09-01"
curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD%" -X GET "%SERVICE_HOST%/audit/report?serviceMethod=deleteDataPackage&status=200&toTime=2013-01-31"
