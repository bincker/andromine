#!/bin/bash

ANDROID_JARS_PATH="/android/jars/"
export JAVA_CLASSPATH="\
/android/SOOT/soot-github/lib/soot-develop.jar:\
/android/SOOT/soot-github/libs/AXMLPrinter2.jar:\
/android/workspace/Soot/lib/mysql-connector-java-5.1.25-bin.jar:\
/android/andromine/andromine.jar:\
"
APK_FILE=$1
SOOT_OUT_DIR=$2

PROCESS_THIS=" -process-dir $APK_FILE" 
SOOT_CLASSPATH="\
"${APK_FILE}":\
"
SOOT_CMD="andromine.soot.Main --soot-classpath $SOOT_CLASSPATH \
 -d $SOOT_OUT_DIR \
 -android-jars $ANDROID_JARS_PATH \
 -allow-phantom-refs \
 -src-prec apk \
 -ire \
 -pp \
 -f J\
 -w \
 -full-resolver \
 $PROCESS_THIS
"

java \
 -Xss50m \
 -Xmx1500m \
 -classpath  ${JAVA_CLASSPATH} \
 ${SOOT_CMD}\
