@ECHO OFF
REM Run this from the DataPackageManager top-level directory. It supports two modes;
REM
REM Mode 1; Optimize storage for an individual data package revision
REM Specify the scope, identifier, and revision of the data package whose data entities are to be optimized.
REM For example;
REM
REM     cd %DATAPACKAGEMANAGER%
REM     bin\storageManager.bat knb-lter-nwk 1 2
REM
REM Mode 2; Optimize storage for ALL data package revisions in PASTA
REM This mode executes when no arguments are supplied on the command line.
REM For example;
REM
REM     cd %DATAPACKAGEMANAGER%
REM     bin\storageManager.bat knb-lter-nwk 1 2
REM
java -cp %DATAPACKAGEMANAGER%\WebRoot\WEB-INF\classes;%DATAPACKAGEMANAGER%\WebRoot\WEB-INF\lib\*;%TOMCAT%\lib\servlet-api.jar edu.lternet.pasta.datamanager.StorageManager %1 %2 %3
