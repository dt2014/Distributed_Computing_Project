	COMP90019 Distributed Computing Project, Semester 1 2015
	Fengmin Deng (Student ID: 659332)


# Clustering Processes to Indentify Congestion
----------

##Overview
ELKI is the frame work used for clustering tweet records. ELKI has strongly advised to run the application via GUI or command line, instead of using ELKI as a Java library. See [http://elki.dbs.ifi.lmu.de/wiki/HowTo/InvokingELKIFromJava](http://elki.dbs.ifi.lmu.de/wiki/HowTo/InvokingELKIFromJava "Invoking ELKI from Java") Therefore the job is divided into three parts:

1. Before running ELKI, create input file from data in CouchDB databases;
2. Run ELKI separately via command line for clustering process;
3. Use the output from 2 to mark the tweet records and update it.

There is a customized distance function defined for the purpose of spatio-temporal clustering in this project. The source code is defined in file [MyDistanceFunction.java](MyDistanceFunction.java), it was already pre-compiled and embeded in the ELKI jar [elki-bundle-0.6.5-customized.jar](elki-bundle-0.6.5-customized.jar).

##Souce Code
The source code for the mentioned 1 & 3 parts are in folder: `Distributed_Computing_Project/congestionTag/src/main/java/dcp/congestionTag/`.

The scripts for part 2 and the Java code of customized distance function are saved under root folder of this sub-project: `Distributed_Computing_Project/congestionTag/`. 

These files in the root folder are generated files from part 1 & 2, they are just kept for the record:

- `brisbane_in.txt`
- `melbourne_in.txt`
- `sydney_in.txt`
- `brisbane_clusters.txt`
- `melbourne_clusters.txt`
- `sydney_clusters.txt`

Finally the jar file `elki-bundle-0.6.5-customized.jar` is the main application for part 2. It embededed the compiled distance function already. Without it the scripts could not execute. It is not pushed to GitHub but it would be included in the code submission.

##Enviroment Requirements
For installing and launching the applications, these tools/environments are needed:

- JDK 1.7 or above
- Maven
- CouchDB 
- ELKI 0.6.5

##Running the processes
- Maven Install
- Change the 'paras.json' file with parameters desired
- Launch part 1 via command: `$ java -jar congestionTag-ElkiInputProcessor.jar paras.json`
- Launch part 2 via script for ELKI command line execution: `$ ./run_elki.sh` (or for Windows: `run_elki.bat`)
- Launch part 3 via command: `$ java -jar congestionTag-ClusterLabelMaker.jar paras.json`

##Parameters 
- `"url_elki_view"`: The url for the CouchDB View that maps the temporal and spatial values of the records.
- `"file_elki_in"`: The file to store the extracted spatio-temporal data for ELKI as input.
- `"file_elki_out"`: The file to store clustering information as ouput from ELKI.
- `"url_db"`: The CouchDB url to update the tweet records with clustering information.
