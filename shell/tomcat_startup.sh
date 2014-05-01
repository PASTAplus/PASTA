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
export JAVA_OPTS="-Xms256M -Xmx1536M -XX:MaxPermSize=512M -Djava.awt.headless=true -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"
export JAVA_ROOT=$JAVA_HOME
export JDK_HOME=$JAVA_HOME
export JRE_HOME=$JAVA_HOME/jre
export SDK_HOME=$JAVA_HOME

# LTER Git repository
export NIS=$HOME/git/NIS

# PATH management
export PATH=$ANT_BINDIR:$JAVA_BINDIR:$PATH


# Tomcat variables and aliases
export CATALINA_HOME=$APPDIR/apache-tomcat
export TOMCAT=$CATALINA_HOME
export WEBAPPS=$TOMCAT/webapps

# Startup Tomcat using the default '$TOMCAT/bin/startup.sh'
/home/pasta/local/apache-tomcat/bin/startup.sh
