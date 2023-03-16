@ECHO OFF
REM Run this from the DataPackageManager top-level directory.
REM
java -cp %DATAPACKAGEMANAGER%\WebRoot\WEB-INF\classes;%DATAPACKAGEMANAGER%\WebRoot\WEB-INF\lib\*;%TOMCAT%\lib\servlet-api.jar edu.lternet.pasta.datamanager.HardLinker %1 %2
