@ECHO OFF

SET PASSWORD=%1

SET SERVICE_HOST=http://localhost:8888
SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1

curl -i --user uid=LNO,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -G %SERVICE_HOST%/package/acl/eml/%SCOPE%/%IDENTIFIER%/%REVISION%
