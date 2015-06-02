	COMP90019 Distributed Computing Project, Semester 1 2015
	Fengmin Deng (Student ID: 659332)


# Twitter & Transport Congestion Project
----------

##Overview
This project has attempted to explore the relevance of tweeting on the roads to traffic congestion. The system is developed by firstly adopting the 'road block searching' algorithm devised by Yikai Gong (MSc Dissertation, 2014) to an implementation for any city. Secondly three instances in NeCTAR cloud are deployed to harvest main roads tweets from Brisbane, Melbourne and Sydney. Thirdly the transport congestion are tagged after running ELKI (release 0.6), a clustering application with customized distance function. Last a visualization mapping is built to represent the clustering of tweets along the road.

##Main Components
There are four main components in this project:

- A Tweet Harvester
- Scripts for CouchDB Views
- Congestion Flag
- Web Service

There are individual README files in component folders. See there for the details to find source code and invoke the system.
