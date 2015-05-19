package dcp;

/*
 * @author Fengmin Deng
 */

import java.util.Locale;

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
		final String host = config.getString("dbhost");
		final int port = config.getInteger("dbport");
		final HttpClient client = vertx.createHttpClient()
				.setPort(port)
				.setHost(host);
		final EventBus eventBus = vertx.eventBus();
		
		eventBus.registerHandler(Constants.QUEUE_DB_VIEW, new Handler<Message<String>>() {
			@Override
			public void handle(final Message<String> msg) {
				String[] message = msg.body().split(":");
				final String textHandlerID = message[0];
				String cityDB = message[1];
				String view = getView(message[2]);
				final String para = getPara(view, message[3]);
				log.info("cityDB: " + cityDB + "view: " + view + "; para: " + para); 
				String query = "/" + cityDB + "/_design/map/_view/" + view + "?key=" + "\"" + para.replaceAll(" ", "%20") + "\"";
				log.info("query: " + query); 
				HttpClientRequest request = client.request("GET", query, new Handler<HttpClientResponse>() {
					public void handle(HttpClientResponse resp) {
						int status = resp.statusCode();
						if (status == 200) {
							resp.bodyHandler(new Handler<Buffer>() {
					            public void handle(Buffer body) {
					            	JsonObject returnedData = new JsonObject(body.toString("utf-8"));
					            	JsonArray rows = returnedData.getArray("rows");
					            	log.info("number of rows returned: " + rows.size());
					            	for(int i = 0; i < rows.size(); ++i) {
					            		JsonObject row = (JsonObject)rows.get(i);
					            		String key = row.getString("key");
//					            		if (key.equals(para)) {
					            			JsonObject geo = row.getObject("value");
					            			if (geo != null) {
						            			JsonObject spot = new JsonObject();
						    		        	spot.putNumber("lat", geo.getNumber("lat"));
						    		        	spot.putNumber("lng", geo.getNumber("lon"));
						    		        	eventBus.send(textHandlerID, spot.toString());
						            		} else {
							            		log.info("geo is null!");
						            		}
//					            		}
					            	}
					            }
					        });
						} else {
							log.error(resp);
						}
					}
				}).exceptionHandler(new Handler<Throwable>() {
					@Override
					public void handle(Throwable t) {
						log.error(t);
					}
				});
				request.end();
				log.info("query end!"); 
			}
		});
	}
	
	private static String getView(String str) {
		String view;
		if (str.equals("time") || str.equals("day") || str.equals("road") || str.equals("date")) {
			view = str;
		} else {
			view = "time"; // the default setting for any error request
		}
		return view;
	}
	
	private static String getPara(String view, String str) {
		String para = "";
		switch (view) {
		case "date":
			para = str;
			break;
		case "day":
			if (str.equals("mon")) {
				para = "Monday";
			} else if (str.equals("tue")) {
				para = "Tuesday";
			} else if (str.equals("wed")) {
				para = "Wednesday";
			} else if (str.equals("thu")) {
				para = "Thursday";
			} else if (str.equals("fri")) {
				para = "Friday";
			} else if (str.equals("sat")) {
				para = "Saturday";
			} else {
				para = "Sunday";
			}
			break;
		case "road":
			para = str.replace("_", " ").toUpperCase(Locale.ENGLISH);
			break;
		default: //"time"
			if (str.equals("mr")) {
				para = "morning rush";
			} else if (str.equals("er")) {
				para = "evening rush";
			} else {
				para = "off peak";
			}
			break;
		}		
		return para;
	}
}
