#!/bin/bash

PROJBRANCH="$1"

SNAP=$(grep SNAPSHOT ../project-version.txt |wc -l)

if [ "$PROJBRANCH" = "versioning" ]; then
	echo "On versioning branch"
	mvn clean install
elif [ $SNAP -eq 1 ] && [ "$PROJBRANCH" = "master" ]; then
	echo "SNAPSHOT version on master branch"
	mvn clean install
elif [ $SNAP -eq 1 ]; then
	echo "SNAPSHOT version on other branch ($PROJBRANCH)"
	mvn clean deploy
elif [ "$PROJBRANCH" = "master" ]; then
	echo "Non-SNAPSHOT version on master branch"
	mvn clean deploy
else
	echo "Non-SNAPSHOT version on other branch ($PROJBRANCH)"
	exit 1
fi