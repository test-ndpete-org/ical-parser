#!/bin/bash
APPSTAGE="$1"
PROJSHA="$2"
PROJBRANCH="${3//\//_}"
PROJVER="`cat ../project-version.txt`"

echo "Version: $PROJVER"
echo "Commit: $PROJSHA"
echo "Branch: $PROJBRANCH"

if [ "$APPSTAGE" = "^" ]; then
  if [ "$PROJBRANCH" = "master" ]; then
    APPSTAGE="prod"
  elif [ "$PROJBRANCH" = "dev" ]; then
    APPSTAGE="stage"
  else
    APPSTAGE="dev"
  fi
fi

docker build -f cicd/Dockerfile \
  --build-arg APPSTAGE=$APPSTAGE \
  -t apptomcat:local .

docker tag apptomcat:local quay.io/byuoit/ical2json:$PROJVER

docker push quay.io/byuoit/ical2json:$PROJVER
