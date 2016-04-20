@ECHO OFF

SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://pasta-d.lternet.edu

SET SCOPE=knb-lter-lno
SET IDENTIFIER=44444

curl -u uid=LNO,o=LTER,dc=ecoinformatics,dc=org:XXXXXXX -X DELETE %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%
