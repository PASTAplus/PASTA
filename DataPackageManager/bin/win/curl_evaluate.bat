@ECHO OFF

SET PASSWORD=%1

SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://pasta-d.lternet.edu
SET EML_FILE=NoneSuchBugCount.xml
curl -i --user uid=LNO,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -X POST -H "Expect:" -H "Content-Type: application/xml" -T %EML_FILE% %SERVICE_HOST%/package/evaluate/eml?useChecksum
