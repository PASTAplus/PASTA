Steps to checkout, configure, build, and deploy the Data Package 
Manager on a PASTA-configured host:

1. Login to the 'pasta' account on the host for which Data Package 
   Manager is to be installed

2. cd $SVN/shell

   # for tagged version, replace 'DataPackageManager' with 'DataPackageManager-0.1'
3. perl nis_checkout.pl DataPackageManager  

   # for tagged version, use $DATAPACKAGEMANAGER_01 instead
4. cd $DATAPACKAGEMANAGER                   

5. Edit properties file 'WebRoot/WEB-INF/conf/datapackagemanager.properties'
   (typical edits would be to change the service host name, 'pasta' database password value, 
   and database connector value).
   
6. Build and deploy the common.jar library
  a. cd ../common
  b. ant deploy

7. Create the database schemas (if this is a first-time installation)
  a. cd ../db-util
  
   # for tagged version, use $DATAPACKAGEMANAGER_01 instead
  b. sh create_schemas.sh $DATAPACKAGEMANAGER   

8. Deploy to tomcat 
  a. cd ../DataPackageManager
  b. ant deploy

9. Run the JUnit tests
  a. ant test >& test_results.txt
  b. grep -i "Tests run" test_results.txt
  c. check for 0 failures and 0 errors for all tests; fix problems as necessary

10. Start tomcat
  a. tomcat_startup
