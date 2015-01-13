##
## Sample call:
##
## sh testSaxon.sh
##
for FN in $(find ~/local/metadata | grep Level-1-EML.xml); do echo ${FN}; sh saxon9he.sh ${FN} /home/pasta/git/NIS/DataPortal2/WebRoot/WEB-INF/xsl/eml-2.xsl test.html; done;

 