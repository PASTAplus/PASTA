@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

SET SERVICE_HOST=http://localhost:8888
SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1
SET ENTITY_ID=67e99349d1666e6f4955e9dda42c3cc2

curl -i -G %SERVICE_HOST%/package/data/eml/%SCOPE%/%IDENTIFIER%/%REVISION%/%ENTITY_ID%
