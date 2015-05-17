package dcp;

/*
 * @author Fengmin Deng
 */


import java.util.Set;
import java.util.TreeSet;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;


public class RealTimeClients extends Verticle {
	
	public void start() {
		final Logger log = container.logger();
		log.info("RealTimeClients started.");
		final EventBus eventBus = vertx.eventBus();
		final Set<String> clients = new TreeSet<>();
		
		eventBus.registerHandler(Constants.QUEUE_WSH, new Handler<Message<String>>() {
		    public void handle(Message<String> msg) {
		       	final String content = msg.body();
		       	log.info("RealTimeClients.content:" + content);
		       	if (clients.contains(content)) {
		       		clients.remove(content);
		       		log.info("ws closed: " + content);
		       	} else {
		       		clients.add(content);
		       		log.info("ws is on for: " + content);
		       	}
		    }
		});
		
		final JsonObject config = container.config();
		final String OAuthConsumerKey = config.getString("OAuthConsumerKey");
		final String OAuthConsumerSecret = config.getString("OAuthConsumerSecret");
		final String OAuthAccessToken = config.getString("OAuthAccessToken");
		final String OAuthAccessTokenSecret = config.getString("OAuthAccessTokenSecret");
		final JsonObject bbx = config.getObject("boundingBox");
		//{{longitude1, latitude1}, {longitude2, latitude2}}
		final double[][] boundingBox = {{152.668522848, -27.767440994}, {153.31787024, -26.996844991}, //Brisbane
				{144.593741856, -38.433859306}, {145.512528832, -37.5112737225},// Melbourne
				{150.520928608, -34.1183470085}, {151.343020992, -33.578140996}}; // Sydney
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setJSONStoreEnabled(true);
        cb.setOAuthConsumerKey(OAuthConsumerKey);
        cb.setOAuthConsumerSecret(OAuthConsumerSecret);
        cb.setOAuthAccessToken(OAuthAccessToken);
        cb.setOAuthAccessTokenSecret(OAuthAccessTokenSecret);

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(new StatusListener() {
			@Override
			public void onException(Exception ex) {
				log.error(ex.toString());
			}
			@Override
			public void onStatus(Status status) {
				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				JsonObject tweet = new JsonObject(rawJSON);
//				log.info("tweet: " + tweet);
				JsonObject data = validateTweet(tweet); // discard the tweets with no coordinates
				log.info("data: " + data.toString());
				if (data.getBoolean("valid") && (!clients.isEmpty())) {
					for (String client : clients) {
			        	eventBus.send(client, data.toString());
			        }
				}
			}
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				log.info("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}
			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				log.info("Got track limitation notice:" + numberOfLimitedStatuses);
				
			}
			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				log.info("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}
			@Override
			public void onStallWarning(StallWarning warning) {
				log.info("Got Stall Warning:" + warning.getMessage());
			}
        });
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.locations(boundingBox);
        twitterStream.filter(filterQuery);
	}
	
	private static JsonObject validateTweet(JsonObject tweet) {
		JsonObject message = new JsonObject();
		if (tweet.containsField("place") && tweet.containsField("geo") && tweet.containsField("coordinates")){
			JsonObject place = tweet.getObject("place");
			if (place != null && place.containsField("name")) {
				String city = place.getString("name");
				if (city != null && (city.equals("Brisbane") || city.equals("Melbourne") || city.equals("Sydney"))) {
					message.putString("city", city);
					double lat = 0.0;
					double lon = 0.0;
					if (tweet.getObject("geo") != null) {
						double[] coordinates = convertCoordinates(tweet.getObject("geo").getValue("coordinates").toString());
						lat = coordinates[0];
						lon = coordinates[1];
						message.putNumber("lat", lat);
						message.putNumber("lng", lon);
						message.putBoolean("valid", true);
//						System.out.println("line 130: "+message.getBoolean("valid"));
					} else if (tweet.getObject("coordinates") != null) {
						double[] coordinates = convertCoordinates(tweet.getObject("coordinates").getValue("coordinates").toString());
						lat = coordinates[1];
						lon = coordinates[0];
						message.putNumber("lat", lat);
						message.putNumber("lng", lon);
						message.putBoolean("valid", true);
//						System.out.println("line 138: "+message.getBinary("valid"));
					} else {
						message.putBoolean("valid", false);
					}
				} else {
					message.putBoolean("valid", false);
				}
			} else {
				message.putBoolean("valid", false);
			}
		} else {
			message.putBoolean("valid", false);
		}
		return message;
	}
	
	private static double[] convertCoordinates(String coordinates) {
		double[] result = {0.0, 0.0};
		String[] numbers = coordinates.replace("[", "").replace("]", "").split(",");
		result[0] = Double.valueOf(numbers[0]);
		result[1] = Double.valueOf(numbers[1]);
		return result;
	}
}
