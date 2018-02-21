@ECHO OFF

SET PASSWORD=%1

SET SERVICE_HOST=http://localhost:8888
SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1
SET ENTITY_ID=67e99349d1666e6f4955e9dda42c3cc2

curl -i --user uid=gmn-pasta,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -G %SERVICE_HOST%/package/data/acl/eml/%SCOPE%/%IDENTIFIER%/%REVISION%/%ENTITY_ID%
