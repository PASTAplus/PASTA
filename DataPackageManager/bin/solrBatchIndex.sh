# Shell script to re-index PASTA EML documents in Solr.
# This script should be executed from the $DATAPACKAGEMANAGER directory.
# By default, the most current revision of all PASTA EML documents are re-indexed in Solr.
# Optionally, a comma-separated list of document identifiers may be passed as a command-line argument. For example:
#
#  bin/solrBatchIndex.sh knb-lter-arc.1386,knb-lter-arc.1392,knb-lter-arc.1594,knb-lter-arc.1595,knb-lter-arc.1644,knb-lter-arc.10045,knb-lter-arc.10279,knb-lter-arc.10334,knb-lter-arc.10588,knb-lter-arc.10590
#
java -cp $DATAPACKAGEMANAGER/WebRoot/WEB-INF/classes:$DATAPACKAGEMANAGER/WebRoot/WEB-INF/lib/*:$TOMCAT/lib/servlet-api.jar edu.lternet.pasta.datapackagemanager.solr.index.BatchIndex $1
