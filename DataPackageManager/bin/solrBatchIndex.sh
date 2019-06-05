# Shell script to re-index PASTA EML documents in Solr.
#
# This script should be executed from the $DATAPACKAGEMANAGER directory. 
# For example:
#
#    $ sh bin/solrBatchIndex.sh
#
# By default, the most current revision of all PASTA EML documents are 
# re-indexed in Solr. Optionally, a comma-separated list of document 
# identifiers may be passed as a command-line argument. For example:
#
#    $ sh bin/solrBatchIndex.sh knb-lter-abc.13,knb-lter-abc.14,knb-lter-abc.15
#
java -cp $DATAPACKAGEMANAGER/WebRoot/WEB-INF/classes:$DATAPACKAGEMANAGER/WebRoot/WEB-INF/lib/*:$NIS/lib/servlet/servlet-api.jar edu.lternet.pasta.datapackagemanager.solr.index.BatchIndex $1
