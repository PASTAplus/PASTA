# Steps to checkout and deploy the Event Manager

ssh basalt.lternet.edu

sudo su -l pasta

cd /home/pasta/svn

svn checkout https://svn.lternet.edu/svn/NIS/trunk/shell
 - or -
cd shell ; svn update ; cd ..

mkdir EventManager
 - or -
rm -rf EventManager/*

cd EventManager

svn checkout https://svn.lternet.edu/svn/NIS/trunk/EventManager
svn checkout https://svn.lternet.edu/svn/NIS/trunk/common
svn checkout https://svn.lternet.edu/svn/NIS/trunk/ant-util
svn checkout https://svn.lternet.edu/svn/NIS/trunk/doc-util
svn checkout https://svn.lternet.edu/svn/NIS/trunk/db-util
svn checkout https://svn.lternet.edu/svn/NIS/trunk/lib
svn checkout https://svn.lternet.edu/svn/NIS/trunk/eventmanagertester

cd eventmanagertester
ant deploy
 ../../shell/tomcat_shutdown.sh
../../shell/tomcat_startup.sh

cd ../EventManager

open src/META-INF/persistence.xml and replace both occurrences of
@PASSWORD@ with the password for the postgres user 'pasta'.

ant test
ant deploy
 ../../shell/tomcat_shutdown.sh
../../shell/tomcat_startup.sh
