#!/bin/bash

# to exit if an error happens
set -e

if [ $# -eq 0 ]; then
	echo "No arguments supplied"
	exit -1
fi

ENVNAME="JAVA_HOME_$1_X64"
LOCATION="dl_tools/jdk$1"

if [ $1 -eq 8 ]; then 
	DOWNLOAD_URL="https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u232-b09/OpenJDK8U-jdk_x64_linux_hotspot_8u232b09.tar.gz"
elif [ $1 -eq 9 ]; then 
	DOWNLOAD_URL="https://github.com/AdoptOpenJDK/openjdk9-binaries/releases/download/jdk-9.0.4%2B11/OpenJDK9U-jdk_x64_linux_hotspot_9.0.4_11.tar.gz"
elif [ $1 -eq 10 ]; then 
	DOWNLOAD_URL="https://github.com/AdoptOpenJDK/openjdk10-binaries/releases/download/jdk-10.0.2%2B13.1/OpenJDK10U-jdk_x64_linux_hotspot_10.0.2_13.tar.gz"
elif [ $1 -eq 11 ]; then 
	DOWNLOAD_URL="https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.5%2B10/OpenJDK11U-jdk_x64_linux_hotspot_11.0.5_10.tar.gz"
elif [ $1 -eq 12 ]; then 
	DOWNLOAD_URL="https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jdk_x64_linux_hotspot_12.0.2_10.tar.gz"
elif [ $1 -eq 13 ]; then 
	DOWNLOAD_URL="https://github.com/AdoptOpenJDK/openjdk13-binaries/releases/download/jdk-13.0.1%2B9/OpenJDK13U-jdk_x64_linux_hotspot_13.0.1_9.tar.gz"
else
	echo "Unrecognized JDK version $1"
	exit -1
fi

if [[ ! -z "${!ENVNAME}" ]]; then
	echo ${!ENVNAME}
	exit 0
fi

mkdir -p $LOCATION
mkdir -p "${LOCATION}/jdk"

curl -L -f -s $DOWNLOAD_URL -o "${LOCATION}/jdk_download.tar.gz"
tar -xzf "${LOCATION}/jdk_download.tar.gz" -C "$LOCATION/jdk"
echo "$(pwd)/${LOCATION}/$(ls ${LOCATION}/jdk)"
