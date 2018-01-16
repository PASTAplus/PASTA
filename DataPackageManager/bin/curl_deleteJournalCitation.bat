@ECHO OFF

SET PASSWORD=%1
SET JOURNAL_CITATION_ID=%2
SET SERVICE_HOST=http://localhost:8888

curl -i -u uid=LNO,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -X DELETE %SERVICE_HOST%/package/citation/eml/%JOURNAL_CITATION_ID%
