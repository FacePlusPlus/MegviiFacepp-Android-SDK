#!/usr/bin/env bash

#delete tag
if [ -f "release/tag" ]; then
    rm release/tag
fi

#delete apk
list=$(find ./release -name "MGFaceppDemo*")
echo $list
if [ -n "$list" ]; then
	rm ./release/MGFaceppDemo*
fi

# build the project
./gradlew clean
./gradlew buildAAR
./gradlew build

#git date
git rev-parse HEAD > release/tag
_tag=`git rev-parse --short HEAD`
_date=`date '+%Y%m%d'`


#copy apk
cp faceppdemo/build/outputs/apk/faceppdemo-debug.apk release/"MGFaceppDemo_0.5.2_${_date}_${_tag}.apk"





