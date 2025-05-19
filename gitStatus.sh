#!/bin/bash

rootDir=${PWD}

echo "STATUS: {"
echo "${subDir}"
git status;
echo "}"
echo ""

for subDir in $(find ${rootDir}/ -mindepth 1 -maxdepth 1 -type d | sort);do
	if [ -d "${subDir}/.git" ];then
		cd ${subDir}/
		
		echo "STATUS: {"
		echo "${subDir}"
		git status;
		echo "}"
		echo ""
		
		cd ${rootDir}/
	fi
done
