## Run this from the DataPackageManager top-level directory. It supports two modes:
##
## Mode 1: Optimize storage for an individual data package revision
## Specify the scope, identifier, and revision of the data package whose data entities are to be optimized.
## For example:
##
##     cd $DATAPACKAGEMANAGER
##     sh bin/storageManager.sh knb-lter-nwk 1 2
##
## Mode 2: Optimize storage for ALL data package revisions in PASTA
## This mode executes when no arguments are supplied on the command line.
## For example:
##
##     cd $DATAPACKAGEMANAGER
##     sh bin/storageManager.sh
##
java -cp $DATAPACKAGEMANAGER/WebRoot/WEB-INF/classes:$DATAPACKAGEMANAGER/WebRoot/WEB-INF/lib/*:$TOMCAT/lib/servlet-api.jar edu.lternet.pasta.datamanager.StorageManager $1 $2 $3
