@ECHO OFF

SET PASSWORD=%1
SET SERVICE_HOST=http://localhost:8888
SET EML_FILE=..\test\data\NoneSuchBugCount.xml
REM SET EML_FILE=..\test\data\NoneSuchBugCountBadData.xml
REM SET EML_FILE=..\test\data\NoneSuchBugCountBadPackageId.xml
REM SET EML_FILE=..\test\data\NoneSuchBugCountBadReference.xml
REM SET EML_FILE=..\test\data\NoneSuchBugCountDuplicateEntity.xml

ECHO DP-1: Create Data Package
curl -i -u uid=cjack,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -X POST -H "Expect:" -H "Content-Type: application/xml" -T %EML_FILE% %SERVICE_HOST%/package/eml
