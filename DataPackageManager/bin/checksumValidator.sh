#!/bin/sh

# checksumValidator.sh - runs the PASTA resource checksum validation
# application.
# 
# Note: this file should be made executable for crontab.

JAVA_BINDIR=/home/pasta/local/java/bin
JAVA_HOME=/home/pasta/local/java
JAVA_ROOT=/home/pasta/local/java
TOMCAT=/home/pasta/local/apache-tomcat
DATAPACKAGEMANAGER=/home/pasta/git/NIS/DataPackageManager
CHECKSUM_DIR=/home/pasta/local/checksum
DATE=$(date +"%Y-%m-%d_%H:%M:%S")

cd $DATAPACKAGEMANAGER

mkdir -p $CHECKSUM_DIR
echo Directing output to $CHECKSUM_DIR/$DATE
$JAVA_BINDIR/java -cp $DATAPACKAGEMANAGER/WebRoot/WEB-INF/classes:$DATAPACKAGEMANAGER/WebRoot/WEB-INF/lib/*:$TOMCAT/lib/servlet-api.jar edu.lternet.pasta.datapackagemanager.checksum.ChecksumValidator > $CHECKSUM_DIR/$DATE 2>&1
