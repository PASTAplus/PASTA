@ECHO OFF

SET SERVICE_HOST=http://localhost:8888

ECHO List User Data Packages
curl -i -G "%SERVICE_HOST%/package/user/uid=LNO,o=LTER,dc=ecoinformatics,dc=org
