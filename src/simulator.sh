#!/bin/sh

export JAVA_HOME="/usr/lib/jvm/java"
export BIGDEMO_CONF_DIR="/home/ec2-user/Bigdemo/conf/"
export BIGDEMO_DATA_DIR="/home/ec2-user/Bigdemo/data/"
export BIGDEMO_SRC_DIR="/home/ec2-user/Bigdemo/src/"
currentDate=$(date +"%Y%m%d_%H%M%S")

outputfile=$BIGDEMO_DATA_DIR"rec_$currentDate.cdr"

echo "Outputing to file : " $outputfile

cd $BIGDEMO_SRC_DIR && java Initiator > $outputfile


