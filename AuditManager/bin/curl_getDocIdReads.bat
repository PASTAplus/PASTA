@ECHO OFF

SET PASSWORD=%1

SET SCOPE="edi"
SET IDENTIFIER="0"
SET SERVICE_HOST=http://localhost:8888
REM SET SERVICE_HOST=http://pasta.lternet.edu

curl -i -X GET "%SERVICE_HOST%/audit/reads/%SCOPE%/%IDENTIFIER%"
