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

# The default -Xmx value below is okay on most servers, but it
# should be configured in the env.sh (or env_2.sh file) for 
# the particular Tomcat server that is using it.
#
export JAVA_OPTS="-Xms256M -Xmx1536M -Djava.awt.headless=true -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"

export JAVA_ROOT=$JAVA_HOME
export JDK_HOME=$JAVA_HOME
export JRE_HOME=$JAVA_HOME/jre
export SDK_HOME=$JAVA_HOME

# LTER Git repositories and key project directories, PASTA (Turing)
export GIT=$HOME/git
export NIS=$GIT/NIS
export NIS_SHELL=$NIS/shell
export AUDITMANAGER=$NIS/AuditManager
export COMMON=$NIS/common
export DATAPACKAGEMANAGER=$NIS/DataPackageManager
export DATAPORTAL=$NIS/DataPortal2
export DATASERVER=$NIS/dataserver
export GATEKEEPER=$NIS/Gatekeeper
export IDENTITYMANAGER=$NIS/IdentityManager

export TEAMNIS=$GIT/teamNIS
export EVENTTESTER=$TEAMNIS/eventTester
export PASTATESTER=$TEAMNIS/pastaTester

# Variables (ending in "_M") for working in the Subversion 'master' directory
# while migrating from Subversion to Git
export SVN=$HOME/svn
export MASTERDIR=$SVN/NIS/master
export AUDITMANAGER_M=$MASTERDIR/AuditManager
export COMMON_M=$MASTERDIR/common
export DATAPACKAGEMANAGER_M=$MASTERDIR/DataPackageManager
export DATAPORTAL_M=$MASTERDIR/DataPortal
export EVENTMANAGER_M=$MASTERDIR/EventManager
export GATEKEEPER_M=$MASTERDIR/Gatekeeper

# LTER Subversion repositories and key project directories, PASTA (contrib)
export SVN_SHELL=$SVN/shell
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
alias nis_checkout="perl $SVN_SHELL/nis_checkout.pl"
alias tomcat_stale_webapp_cleaner="pushd ${TOMCAT}/bin && ./shutdown.sh && echo Giving Tomcat a Moment to Shutdown && sleep 10 && pushd ${TOMCAT}/webapps && for i in *.war; do explode=`echo ${i} | sed -e 's:.war::'` && rm -rf ${explode}; done && popd && ./startup.sh && popd"
alias tomcat_startup="sh $NIS_SHELL/tomcat_startup.sh;tail -f $TOMCAT/logs/catalina.out"
alias tomcat_shutdown="sh $NIS_SHELL/tomcat_shutdown.sh; tail -50 $TOMCAT/logs/catalina.out"
alias tomcat_version="$TOMCAT/bin/version.sh"
alias tomcat_ps='ps auwwx | grep catalina.startup.Bootstrap' # show Tomcat processes

# Tomcat2 variables used on Data Package Manager to support a second Tomcat instance for the dataserver
export CATALINA_HOME2=$APPDIR/apache-tomcat-2
export TOMCAT2=$CATALINA_HOME2
export WEBAPPS2=$TOMCAT2/webapps
alias tomcat_startup_2="sh $NIS_SHELL/tomcat_startup_2.sh;tail -f $TOMCAT2/logs/catalina.out"
alias tomcat_shutdown_2="sh $NIS_SHELL/tomcat_shutdown_2.sh; tail -50 $TOMCAT2/logs/catalina.out"
alias tomcat_version_2="$TOMCAT2/bin/version.sh"

# Solr variables and aliases
export SOLR=$APPDIR/solr
export SOLR_PASTA=$SOLR/example/solr-pasta
alias solr_startup="$SOLR/bin/solr start -noprompt -s $SOLR_PASTA"
alias solr_shutdown="$SOLR/bin/solr stop -all"
alias solr_restart="$SOLR/bin/solr restart -noprompt -s $SOLR_PASTA"
