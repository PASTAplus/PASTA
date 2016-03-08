@ECHO OFF

SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://pasta-d.lternet.edu
REM SET SERVICE_HOST=http://pasta-s.lternet.edu:8080
REM SET SERVICE_HOST=http://pasta.lternet.edu

SET SCOPE=knb-lter-nwk
SET IDENTIFIER=1408
SET REVISION=1

curl -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%/%REVISION%
