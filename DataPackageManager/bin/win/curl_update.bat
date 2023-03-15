@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

SET PASSWORD=%1
SET SERVICE_HOST=http://localhost:8888
SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET ENTITY_ID=NoneSuchBugCount
SET EML_FILE=..\test\data\NoneSuchBugCount.xml

ECHO DP-3: Update Data Package
curl -i -u uid=cjack,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -X PUT -H "Expect:" -H "Content-Type: application/xml" -T %EML_FILE% %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%
