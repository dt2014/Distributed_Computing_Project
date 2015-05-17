package dcp;

/*
 * @author Fengmin Deng
 */


import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class ParseVerticle extends Verticle {
	
	private final Map<Integer, JsonObject> blocks = new HashMap<>();
	private int blockCount = 0;
	private int roundCount = 0;
	
	public void start() {
		final Logger log = container.logger();
		log.info("Parse Verticle started.");
		final JsonObject config = container.config();
		final String tag = config.getString("tag");
		final EventBus eventBus = vertx.eventBus();
		eventBus.setDefaultReplyTimeout(config.getLong("timeout"));
		JsonArray roadInfos = config.getArray("rawData");
		final int rowDataSize = roadInfos.size();
		int skipIdx = config.getInteger("skipIdx");
		final double radius = config.getNumber("radius").doubleValue();

		// Road block algorithm from Yikai Gong
		for (int i = 0; i < rowDataSize; ++i) {
			JsonObject roadInfo = (JsonObject)roadInfos.get(i);
            String streetName = roadInfo.getString("streetName");
            JsonArray coordinates = roadInfo.getArray("coordinates");
            // Find the longest distance from the starting point
            // Compare with the searching radius to create a more efficient query 
            // (reduce the conflicts of repeated tweets from overlapping searching area)
            double latOrigin = Double.parseDouble(((JsonObject)coordinates.get(0)).getString("latitude"));
            double lonOrigin = Double.parseDouble(((JsonObject)coordinates.get(0)).getString("longitude"));
            double latComp = Double.parseDouble(((JsonObject)coordinates.get(1)).getString("latitude"));
            double lonComp = Double.parseDouble(((JsonObject)coordinates.get(1)).getString("longitude"));
            double maxDistance = getDistance(latOrigin, lonOrigin, latComp, lonComp);
            for (int j = 2; j < coordinates.size(); ++j) {
            	JsonObject coordinate = (JsonObject)coordinates.get(j);
            	double latTemp = Double.parseDouble(coordinate.getString("latitude"));
            	double lonTemp = Double.parseDouble(coordinate.getString("longitude"));
            	double distance = getDistance(latOrigin, lonOrigin, latTemp, lonTemp);
            	if (distance > maxDistance) {
            		maxDistance = distance;
            		latComp = latTemp;
            		lonComp = lonTemp;
            	}
            }
            
            double diffLat = latComp - latOrigin;
            double diffLon = lonComp - lonOrigin;
            
            if (maxDistance <= radius * 2000) { // compare with search diameter in meter
            	double queryLat = latOrigin + diffLat / 2;
            	double queryLon = lonOrigin + diffLon / 2;
            	final JsonObject queryInfo = queryInfoBuilder(tag, streetName, queryLat, queryLon, radius);
                blocks.put(blockCount, queryInfo);
                ++blockCount;
            } else {
            	int block = (int) (maxDistance / (radius * 2000)) + 1;
                double dlat = diffLat / block;
                double dlon = diffLon / block;
                //each sample point
                for (int k = 1; k <= block; ++k) {
                	double queryLat = latOrigin + k * dlat - dlat / 2;
                	double queryLon = lonOrigin + k * dlon - dlon / 2;
                    final JsonObject queryInfo = queryInfoBuilder(tag, streetName, queryLat, queryLon, radius);
                    blocks.put(blockCount, queryInfo);
                    ++blockCount;
                }
            }
		}
		final int totalBlocks = blockCount;
		log.info(String.format("Total %d blocks to monitor.", totalBlocks));
		if (blocks.size() > 0) {
			int interval = config.getInteger("interval");
			blockCount = skipIdx;
			vertx.setPeriodic(interval, new Handler<Long>() {
				public void handle(Long timerID) {
					log.info(String.format("CurrentBlock: %d, TotalBlocks: %d", blockCount, totalBlocks));
			        final JsonObject queryInfo = blocks.get(blockCount);
			        eventBus.send(Constants.QUEUE_QUERYINFO, queryInfo, new Handler<Message<String>>() {
	                   	    public void handle(Message<String> message) {
	                   	    	String updatehUrl = message.body();
	                   	    	queryInfo.putString(config.getString("urlType"), updatehUrl);
	                   	    }
	                   	});
	                   	if (++blockCount == totalBlocks) {
	                   		blockCount = 0;
	                   		log.info(String.format("The whole road data is traversed %d time(s).", ++roundCount));
	                   	}
			    }
			});
			log.info("Sending request...");
		} else {
			log.error("No road info! App is exiting...");
			container.exit();
		}
	}
	
	private static JsonObject queryInfoBuilder(String tag, String streetName, double lat, double lon, double radius) {
		final JsonObject queryInfo = new JsonObject();
		queryInfo.putString("tag", tag);
		queryInfo.putString("method", "search");
		queryInfo.putString("catalog", "street");
		queryInfo.putString("name", streetName);
        JsonObject geo = new JsonObject();
        geo.putString("type", "round_area");
        geo.putNumber("latitude", lat);
        geo.putNumber("longitude", lon);
        geo.putNumber("radius", radius);
        queryInfo.putObject("geo", geo);
        return queryInfo;
	}	
	
	private static Double getDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        Double dlat = radian(lat1) - radian(lat2);
        Double dlon = radian(lon1) - radian(lon2);
        Double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(dlat/2),2) +
                Math.cos(radian(lat1))*Math.cos(radian(lat2))*Math.pow(Math.sin(dlon/2),2)));
        s = s * 6378.137 ;  // EARTH_RADIUS
        s = s *1000;
        return s;
    }

    private static Double radian(Double degree){
        return degree * Math.PI / 180.0;
    }
}
