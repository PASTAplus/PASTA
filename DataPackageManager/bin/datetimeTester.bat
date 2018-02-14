## Run this from the DataPackageManager top-level directory.
## For example:
##
##     cd $DATAPACKAGEMANAGER
##     sh bin/datetimeTester.sh YYYY-MM-DDThh:mm-hh 2002-09-30T16:00-08
##
java -cp %DATAPACKAGEMANAGER%\WebRoot\WEB-INF\classes;%DATAPACKAGEMANAGER%\WebRoot\WEB-INF\lib\* edu.lternet.pasta.dml.database.DatabaseAdapter %1 %2
