	COMP90019 Distributed Computing Project, Semester 1 2015
	Fengmin Deng (Student ID: 659332)


# Tweets Harvester
----------

##Overview
This sub-project represents a tweets harvester which is able to run for different cities with different accounts. See the Parameters part for the detailed settings.

##Source Code
The souce code are all in the folder: `Distributed_Computing_Project/tweetsHarvester/src/main/java/dcp`.

The following files in the root folder of this sub-project ara not considered as source code but rather they are configuration files used to parameterize the Harvester program for retrieving tweets from 3 cities. The account information and city street data are all in these files. They are not pushed to GitHub but they would be included in code submission.

- `config_BrisbaneMainRoad.json`
- `config_MelbourneMainRoad.json`
- `config_SydneyMainRoad.json`

##Enviroment Requirements
For installing and launching the system, these tools/environments are needed:

- JDK 1.7 or above
- Vert.x 2.1.5 or above
- Maven
- CouchDB

##Running the System
- Maven Install
- Change the 'config.json' file with parameters desired
- Launch via command: `$ vertx runzip tweetsHarvester-0.1-mod.zip -conf config.json`

##Parameters of Application Configuration 
###1. `"appConfig"`: Configuration for the application starter

- `"logVerticleInstances"`: Number of instances of LogVerticle to be deployed 
- `"dbVerticleInstances"`: Number of instances of DBVerticleInstances to be deployed
- `"parseVerticleInstances"`: Number of instances of ParseVerticleInstances to be deployed
- `"queryVerticleInstances"`: Number of instances of ParseVerticleInstances to be deployed

###2. `"logConfig"`: Configuration for the log verticle, currently empty

###3. `"dbConfig"`: Configuration for the database verticle
- `"dbHost"`: The CouchDB host for saving the harvested tweets
- `"dbPort"`: The CouchDB post for saving the harvested tweets
- `"dbName"`": The CouchDB name for saving the harvested tweets
- `"dbConflictCount"`: The count of conflicts cccured during tweet insertion (a duplicated tweet would not be stored but rather would trigger the increment of the counter) This is a stopping criterion - by reaching such amount of conflicts, the system should exit. 

###4. `"queryConfig"`: Configuration for the query verticle
- `"twHost"`: Twitter resource host
- `"twPort"`: Twitter resource post
- `"token"`: The bearer token obtained by issuing a request to 'POST oauth2 / token' for application-only requests, see https://dev.twitter.com/oauth/application-only for the way to retrieve a bearer token
- `"count"`: The number of tweets to return per page
- `"urlType"`: Either "`refresh_url`" for harvesting newer tweets or "`next_results`" for harvesting older tweets in the next round of search, here 'new' and 'old' are in terms of time line when tweets are created in twitter

###5.`"parseConfig"`: Configuration for the parse-block verticle
- `"rawData"`: The processed PSMA street data in JSON format
- `"tag"`: A tag to indicate the harvester, e.g. from which city the tweets are harvested, this would be appended as a field in the tweet documents in CouchDB (Because all the tweets go into a centralized database at the moment, this tag would be used to distinguish data from different harvesters during MapReduce process)
- `"radius"`: The search radius of the query latitude/longitude in kilometers
- `"timeout"`: The timeout in milliseconds for replies from query verticle
- `"interval"`: The maximum interval in milliseconds to query twitter without exceeding the rate limit (450 Requests / 15-min window for Application-only authentication)
- `"skipIdx"`: The index of a block in a processed PSMA street data, from which the system will start the harvesting in case that the program is stopped unexpectedly
- `"urlType"`: Same as the one in "queryConfig"
