#!/bin/bash

if [ $# -ne 3 ]
	then
		echo "PASTA FullPublic usage: growthstats.sh <dbUrl> <dbUser> <dbPassword>"
	else
		javac -d bin -sourcepath src -classpath lib/postgresql-8.4-702.jdbc4.jar src/edu/lternet/pasta/utilities/statistics/FullPublic.java
		java -classpath bin:lib/postgresql-8.4-702.jdbc4.jar edu.lternet.pasta.utilities.statistics.FullPublic $1 $2 $3
fi
