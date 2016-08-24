@ECHO OFF

SET PASSWORD=%1
SET SERVICE_HOST=http://localhost:8888
SET SCOPE=knb-lter-nin
SET IDENTIFIER=99999

curl -i -u uid=cjack,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -X DELETE %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%
