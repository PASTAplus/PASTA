# This is the shell environment for the 'pasta' and 'tcat' user accounts
# and is used on both Turing and Babbage servers, respectively.
# In both cases it is sourced from ~/.bashrc in the shared account.
# The script sets a limited number of environment variables and aliases
# for managing, navigating, and supporting PASTA, NIS, Java, Ant, Perl,
# Tomcat, Metacat, and other relevant applications.
#
# Note that any settings that must differ locally from these settings
# would be declared in each system's '.bashrc' in the shared account on
# that server. This script defines settings that can be applied across 
# all servers.

# System-wide variables
export APPDIR=$HOME/local

# Ant variables
export ANT_HOME=$APPDIR/apache-ant
export ANT_BINDIR=$ANT_HOME/bin

# Java variables
export JAVA_HOME=$APPDIR/java
export JAVA_BINDIR=$JAVA_HOME/bin
# Uncomment for pasta and PASTA
export JAVA_OPTS="-Xms256M -Xmx1536M -Djava.awt.headless=true -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"
# Uncomment for tcat and Metacat
#export JAVA_OPTS="-Xms256M -Xmx8192M -XX:MaxPermSize=512M -Djava.awt.headless=true"
export JAVA_ROOT=$JAVA_HOME
export JDK_HOME=$JAVA_HOME
export JRE_HOME=$JAVA_HOME/jre
export SDK_HOME=$JAVA_HOME

# LTER Subversion repositories and key project directories, PASTA (Turing)
export SVN=$HOME/svn
export NIS_SHELL=$SVN/shell
export AUDITMANAGER=$SVN/AuditManager/AuditManager
export DATAPACKAGEMANAGER=$SVN/DataPackageManager/DataPackageManager
export EVENTMANAGER=$SVN/EventManager/EventManager
export GATEKEEPER=$SVN/Gatekeeper/Gatekeeper

# Temporary set of variables (ending in "_M") for working in the 'master' directory
# while migrating from Subversion to Git
export MASTERDIR=$SVN/NIS/master
export AUDITMANAGER_M=$MASTERDIR/AuditManager
export COMMON_M=$MASTERDIR/common
export DATAPACKAGEMANAGER_M=$MASTERDIR/DataPackageManager
export DATAPORTAL_M=$MASTERDIR/DataPortal
export EVENTMANAGER_M=$MASTERDIR/EventManager
export GATEKEEPER_M=$MASTERDIR/Gatekeeper

# LTER Subversion repositories and key project directories, PASTA (contrib)
export DATAPORTAL=$SVN/DataPortal/DataPortal
export LTERHIVE=$SVN/NIS/contrib/lter-hive
export LTERHIVEPROTOTYPES=$SVN/NIS/contrib/lter-hive-prototypes
export UNITREGISTRY=$SVN/NIS/contrib/unitRegistry

# LTER Subversion repositories and key project directories, Legacy (Babbage)
export LNO=$SVN/LNO
export LNODATAPORTAL=$LNO/projects/dataPortal

# Metacat variables
export METACAT=$APPDIR/metacat-src/metacat
export METACAT_DATA=$APPDIR/metacat_data

# PATH management
export PATH=$ANT_BINDIR:$JAVA_BINDIR:$PATH

# Perl variables
export PERLPATH=$LNO/perl/lib

# Tomcat and Jetty variables and aliases
export CATALINA_HOME=$APPDIR/apache-tomcat
export JETTY=$APPDIR/jetty
export TOMCAT=$CATALINA_HOME
export WEBAPPS=$TOMCAT/webapps
alias jetty_ps='ps auwwx | grep -i jetty' # show Jetty processes
alias nis_checkout="perl $NIS_SHELL/nis_checkout.pl"
alias tomcat_stale_webapp_cleaner="pushd ${TOMCAT}/bin && ./shutdown.sh && echo Giving Tomcat a Moment to Shutdown && sleep 10 && pushd ${TOMCAT}/webapps && for i in *.war; do explode=`echo ${i} | sed -e 's:.war::'` && rm -rf ${explode}; done && popd && ./startup.sh && popd"
alias tomcat_startup="pushd .;cd $TOMCAT/bin;./startup.sh;popd;tail -f $TOMCAT/logs/catalina.out"
alias tomcat_startup_notail="pushd .;cd $TOMCAT/bin;./startup.sh;popd"
alias tomcat_shutdown="$TOMCAT/bin/shutdown.sh; tail -50 $TOMCAT/logs/catalina.out"
alias tomcat_version="$TOMCAT/bin/version.sh"
alias tomcat_ps='ps auwwx | grep catalina.startup.Bootstrap' # show Tomcat processes
