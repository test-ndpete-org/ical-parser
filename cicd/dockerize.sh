#!/bin/bash
APPSTAGE="$1"
PROJSHA="$2"
PROJBRANCH="${3//\//_}"
PROJVER="`cat ../project-version.txt`"
PROJTAGS="`git show-ref --tags |grep $PROJSHA |cut -d \/  -f 3- |tr / _ |xargs echo`"

echo "Version: $PROJVER"
echo "Commit: $PROJSHA"
echo "Branch: $PROJBRANCH"
echo "Tags:"
for i in ${PROJTAGS[@]}
do
	echo "\t$i"
done

if [ "$PROJBRANCH" = "versioning" ]; then
  echo "On versioning branch - skipping docker build."
  exit 0
fi

if [ "$APPSTAGE" = "^" ]; then
  if [ "$PROJBRANCH" = "master" ]; then
    APPSTAGE="prod"
  elif [ "$PROJBRANCH" = "stage" ]; then
    APPSTAGE="stage"
  else
    APPSTAGE="dev"
  fi
fi

docker build -f cicd/Dockerfile \
  --build-arg APPSTAGE=$APPSTAGE \
  --build-arg SRC_BRANCH_A=$PROJBRANCH \
  --build-arg SRC_VER_A="$PROJVER" \
  --build-arg SRC_COMMIT_A=$PROJSHA \
  --build-arg SRC_TAGS_A="$PROJTAGS" \
  -t apptomcat:local .

docker tag apptomcat:local quay.io/byuoit/ical2json-rest:$PROJBRANCH
[ "$4" != "skip" ] && docker push quay.io/byuoit/ical2json-rest:$PROJBRANCH

docker tag apptomcat:local quay.io/byuoit/ical2json-rest:${PROJSHA:0:16}
[ "$4" != "skip" ] && docker push quay.io/byuoit/ical2json-rest:${PROJSHA:0:16}

if [ "$PROJBRANCH" = "master" ] || [ "$PROJBRANCH" = "stage" ]; then
  docker tag apptomcat:local quay.io/byuoit/ical2json-rest:$PROJVER
  [ "$4" != "skip" ] && docker push quay.io/byuoit/ical2json-rest:$PROJVER
fi

for i in ${PROJTAGS[@]}
do
	docker tag apptomcat:local quay.io/byuoit/ical2json-rest:$i
	[ "$4" != "skip" ] && docker push quay.io/byuoit/ical2json-rest:$i
done
