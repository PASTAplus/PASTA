@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

SET SERVICE_HOST=http://localhost:8888
SET SCOPE=knb-lter-nwk
SET IDENTIFIER=99
SET REVISION=1
SET ENTITY_ID=a9201a0755fc45ae514abb12469c03a0

curl -i -G %SERVICE_HOST%/package/data/eml/%SCOPE%/%IDENTIFIER%/%REVISION%/%ENTITY_ID%
