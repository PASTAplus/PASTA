@ECHO OFF

SET SERVICE_HOST=http://localhost:8888
REM SERVICE_HOST=https://pasta.lternet.edu

SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1

curl -G %SERVICE_HOST%/package/report/eml/%SCOPE%/%IDENTIFIER%/%REVISION%
