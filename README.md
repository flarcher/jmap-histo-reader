**Helps at analyzing memory behavior over time**

Usage :

1. Build this project using `mvn clean install` (requires Java 8 )
2. Launch your Java application in background
3. Runs the script `periodic_heap_histo.sh <PID> <dir> <delay>` (that is available at the root of the project). The first argument is the PID of your running Java application. It will stop as soon as the Java application stops. It will create a bunch of files that contain outputs of the `jmap -histo` command in the directory provided as the second argument. The third argument is the delay between two extracts in seconds.
4. Runs the project by providing the directory that contains the outputs of the previous script; `java -jar target/jmap_histo_reader-1.0-SNAPSHOT.jar <dir>`. It will print the growth of instance count / memory usage by class name. The statistics consider all provided extracts in the chronological order.
