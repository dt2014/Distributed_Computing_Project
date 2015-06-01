REM Invoke ELKI clustering process

java -cp elki-bundle-0.6.5-customized.jar de.lmu.ifi.dbs.elki.application.KDDCLIApplication -algorithm clustering.DBSCAN -algorithm.distancefunction MyDistanceFunction -dbscan.epsilon 1000.0 -dbscan.minpts 4 -dbc.in brisbane_in.txt > brisbane_clusters.txt

java -cp elki-bundle-0.6.5-customized.jar de.lmu.ifi.dbs.elki.application.KDDCLIApplication -algorithm clustering.DBSCAN -algorithm.distancefunction MyDistanceFunction -dbscan.epsilon 1000.0 -dbscan.minpts 4 -dbc.in melbourne_in.txt > melbourne_clusters.txt

java -cp elki-bundle-0.6.5-customized.jar de.lmu.ifi.dbs.elki.application.KDDCLIApplication -algorithm clustering.DBSCAN -algorithm.distancefunction MyDistanceFunction -dbscan.epsilon 1000.0 -dbscan.minpts 4 -dbc.in sydney_in.txt > sydney_clusters.txt