#!/bin/sh

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
export JAVA_OPTS="-Xms256M -Xmx1536M -Djava.awt.headless=true -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"
export JAVA_ROOT=$JAVA_HOME
export JDK_HOME=$JAVA_HOME
export JRE_HOME=$JAVA_HOME/jre
export SDK_HOME=$JAVA_HOME

# LTER Subversion repositories and key project directories
export LNO=$HOME/svn/LNO
export NIS=$HOME/svn/NIS
export DATAPORTAL=$LNO/projects/dataPortal
export LTERHIVE=$NIS/contrib/lter-hive
export METAMAN=$NIS/software/MetadataManagementSuite/trunk

# Metacat variables
export METACAT=$APPDIR/metacat-src/metacat
export METACAT_DATA=$APPDIR/metacat_data

# PATH management
export PATH=$ANT_BINDIR:$JAVA_BINDIR:$PATH

# Perl variables
export PERLPATH=$LNO/perl/lib

# Tomcat variables and aliases
export CATALINA_HOME=$APPDIR/apache-tomcat
export TOMCAT=$CATALINA_HOME
export WEBAPPS=$TOMCAT/webapps
alias tomcat_startup="pushd .;cd $TOMCAT/bin;./startup.sh;popd;tail -f $TOMCAT/logs/catalina.out"
alias tomcat_startup_notail="pushd .;cd $TOMCAT/bin;./startup.sh;popd"
alias tomcat_shutdown="$TOMCAT/bin/shutdown.sh; tail -50 $TOMCAT/logs/catalina.out"
alias tomcat_version="$TOMCAT/bin/version.sh"
alias tomcat_ps='ps auwwx | grep catalina.startup.Bootstrap' # show Tomcat processes

# Shutdown Tomcat using the default '$TOMCAT/bin/shutdown.sh'
/home/pasta/local/apache-tomcat/bin/shutdown.sh
