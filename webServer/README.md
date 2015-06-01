	COMP90019 Distributed Computing Project, Semester 1 2015
	Fengmin Deng (Student ID: 659332)


# Web Service
----------

##Enviroment Requirements
For installing and launching the applications, these tools/environments are needed:

- JDK 1.7 or above
- Vert.x 2.1.5 or above
- Maven
- CouchDB

##Running the processes
- Maven Install
- Change the 'config.json' file with parameters desired
- Launch via command: `$ vertx runzip webServer-0.1-mod.zip -conf config.json`

##Parameters of Application Configuration 
###1. "realTimeClientsConfig": Configuration for the verticle serving and monitoring real time clients

- **"OAuthConsumerKey"**: Twitter OAuth consumer key
- **"OAuthConsumerSecret"**: Twitter OAuth consumer secret
- **"OAuthAccessToken"**: Twitter OAuth access token
- **"OAuthAccessTokenSecret"**: Twitter OAuth access token secret

###2. "queryConfig": Configuration for the query verticle
- **"dbHost"**: The CouchDB host for the harvested tweets
- **"dbPort"**: The CouchDB post for the harvested tweets

###3. "webServerConfig": Configuration for web server verticle
- **"host"**: The host where the service is deployed
- **"Port"**: The post for servicing HTTP requests

###4. "webSocketConfig": Configuration for web socket verticle
- **"host"**: The host where the service is deployed
- **"Port"**: The post for servicing Web Socket requests