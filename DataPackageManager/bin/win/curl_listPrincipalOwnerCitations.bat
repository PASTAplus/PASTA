@ECHO OFF
SET SERVICE_HOST=http://localhost:8888
SET PRINCIPAL_OWNER=uid=LNO,o=LTER,dc=ecoinformatics,dc=org

curl -i -G %SERVICE_HOST%/package/citations/eml/%PRINCIPAL_OWNER%
