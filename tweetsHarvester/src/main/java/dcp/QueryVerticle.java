package dcp;

/*
 * @author Fengmin Deng
 */

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class QueryVerticle extends Verticle {

	public void start() {
		final Logger log = container.logger();
		log.info("Query Verticle started.");
		final JsonObject config = container.config();
		final HttpClient client = vertx.createHttpClient()
				.setPort(config.getInteger("twPort"))
				.setHost(config.getString("twHost"))
				.setSSL(true)
				.setTrustAll(true)
				.setTryUseCompression(true);
		//The number of tweets to return per page
		final int count = config.getInteger("count"); 
		final EventBus evenBus = vertx.eventBus();
		evenBus.registerHandler(Constants.QUEUE_QUERYINFO, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(final Message<JsonObject> message) {
				final JsonObject queryInfo = message.body();
				// updateUrl is from either "refresh_url" or "next_results" in "search_metadata"
		       	String updateUrl = queryInfo.getString(config.getString("urlType"));
		    	if (updateUrl == null) { // for the first round of search or empty info from search_search
		    		JsonObject geo = queryInfo.getObject("geo");
		    		updateUrl = String.format("?q=&geocode=%f,%f,%fkm&count=%d", 
		    			geo.getNumber("latitude"), geo.getNumber("longitude"), geo.getNumber("radius"), count);
		    	}
//	    		log.info(updateUrl);
		    	HttpClientRequest request = client.request("GET", "/1.1/search/tweets.json" + updateUrl, new Handler<HttpClientResponse>() {
					public void handle(HttpClientResponse resp) {
						int status = resp.statusCode();
						if (status == Constants.TW_STATUS_OK) {
							resp.bodyHandler(new Handler<Buffer>() {
					            public void handle(Buffer body) {
					            	JsonObject returnedData = new JsonObject(body.toString("utf-8"));
					            	String updateUrl = returnedData.getObject("search_metadata").getString(config.getString("urlType"));
					            	message.reply(updateUrl);
					            	JsonArray tweets = returnedData.getArray("statuses");
					            	for(int i = 0; i < tweets.size(); ++i) {
					            		JsonObject tweet = tweets.get(i);
					            		tweet.putObject("harvester_appends", queryInfo);
					            		evenBus.send(Constants.QUEUE_TWDATA, tweet);
					            	}
					            }
					        });
						} else {
							JsonObject errMsg = new JsonObject();
							errMsg.putObject(Constants.TW_QUERY_FAIL, queryInfo);
							evenBus.send(Constants.QUEUE_LOG, errMsg);
						}
					}
				}).exceptionHandler(new Handler<Throwable>() {
					@Override
					public void handle(Throwable t) {
						JsonObject errMsg = new JsonObject();
						errMsg.putString(Constants.TW_QUERY_FAIL, t.getMessage());
						evenBus.send(Constants.QUEUE_LOG, errMsg);
					}
				});
				request.headers().set("Content-Length", "0")
								 .set("Authorization", config.getString("token"))
								 .set("Accept-Encoding", "gzip")
								 .set("Accept-Charset", "utf-8");
				request.end();
		    }
		});
	}
}
