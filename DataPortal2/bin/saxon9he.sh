##
## Sample call:
##
## sh saxon9he.sh sampleEML.xml /home/pasta/git/NIS/DataPortal2/WebRoot/WEB-INF/xsl/eml-2.xsl sampleEML.html
##
java -cp /home/pasta/git/NIS/lib/saxon9/saxon9he.jar net.sf.saxon.Transform -s:$1 -xsl:$2 -o:$3
