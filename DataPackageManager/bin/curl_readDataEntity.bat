@ECHO OFF

SET PASSWORD=%1

SET SERVICE_HOST=http://localhost:8888
SET SCOPE=knb-lter-nwk
SET IDENTIFIER=3182
SET REVISION=1
SET ENTITY_ID=8d2b8f34c7316b6035b48de2b9ecc1aa
REM http://localhost:8888/package/data/eml/knb-lter-nwk/3182/1/8d2b8f34c7316b6035b48de2b9ecc1aa

curl -G %SERVICE_HOST%/package/data/eml/%SCOPE%/%IDENTIFIER%/%REVISION%/%ENTITY_ID%
