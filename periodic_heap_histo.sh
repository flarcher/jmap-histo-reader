#!/bin/bash
#
# This scripts outputs histograms of JVM memory usage on a regular basis
# It helps to identify the origin of a memory leak (or memory over-use)
#
# - The first argument is the Java process ID
# - The second argument is the output directory
# - The third optional argument is the delay between two extracts in seconds

PID=$1
OUT_DIR=$2
JVM_PATH=$JAVA_HOME
PERIOD_SECONDS=$3

# Testing arguments
if [[ -e $PID ]]; then
	echo "No process ID provided"
	exit 1
fi
JPS_NAME=$($JVM_PATH/bin/jps -l | grep $PID | cut -d' ' -f2)
if [[ -e $JPS_NAME ]]; then
	echo "No process $PID found"
	exit 1
else
	echo "Extracting memory histograms of process $PID (${JPS_NAME}) every $PERIOD_SECONDS seconds"
fi

if [[ ! -d $OUT_DIR ]]; then
	echo "Output directory $OUT_DIR does not exists"
	exit 1
fi
if [[ ! -w $OUT_DIR ]]; then
	echo "No write access to output directory $OUT_DIR"
	exit 1
fi

if [[ -e $PERIOD_SECONDS ]]; then
	PERIOD_SECONDS=3
fi

# Until the process disappears, we extract the histograms on a regular basis 
while :; do
	OUT_FILE=${OUT_DIR}/${PID}_$(date -Iseconds | sed 's/:/-/g').histo
	# Extract `jmap -histo`
	$JVM_PATH/bin/jmap -histo "${PID}" 1> "${OUT_FILE}" 2> /dev/null
	# the status gets 1 as soon as the process is unknown
	if [[ $? -ne 0 ]]; then
		rm "$OUT_FILE"
		break
	else
		sleep $PERIOD_SECONDS
	fi
done

echo "End of extraction"

exit 0