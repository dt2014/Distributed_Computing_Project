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
		final HttpClient client = vertx.createHttpClient()
				.setPort(5984)
				.setHost("144.6.227.137");
		final EventBus eventBus = vertx.eventBus();
		
		eventBus.registerHandler("mel.query", new Handler<Message<String>>() {
			@Override
			public void handle(final Message<String> msg) {
				String[] message = msg.body().split(":");
				final String textHandlerID = message[0];
				String view = getView(message[1]);
				final String para = getPara(view, message[2]);
				log.info("view: " + view + "; para: " + para); 
				String query = "/mmrh/_design/map/_view/" + view;
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
					            		if (key.equals(para)) {
					            			JsonObject geo = row.getObject("value");
					            			if (geo != null) {
						            			JsonObject spot = new JsonObject();
						    		        	spot.putNumber("lat", geo.getNumber("lat"));
						    		        	spot.putNumber("lng", geo.getNumber("lon"));
						    		        	eventBus.send(textHandlerID, spot.toString());
						            		} else {
							            		log.info("geo is null!");
						            		}
					            		}
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
		if (str.equals("time") || str.equals("day") || str.equals("road")) {
			view = str;
		} else {
			view = "time"; // the default setting for any error request
		}
		return view;
	}
	
	private static String getPara(String view, String str) {
		String para = "";
		switch (view) {
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
			if (str.equals("beaconsfieldparade")) {
				para = "BEACONSFIELD PARADE";
			} else if (str.equals("burnleytunnel")) {
				para = "BURNLEY TUNNEL";
			} else if (str.equals("burwoodhighway")) {
				para = "BURWOOD HIGHWAY";
			} else if (str.equals("cityroad")) {
				para = "CITY ROAD";
			} else if (str.equals("citylink")) {
				para = "CITYLINK";
			} else if (str.equals("dandenongroad")) {
				para = "DANDENONG ROAD";
			} else if (str.equals("drummondstreet")) {
				para = "DRUMMOND STREET";
			} else if (str.equals("elginstreet")) {
				para = "ELGIN STREET";
			} else if (str.equals("exhibitionstreet")) {
				para = "EXHIBITION STREET";
			} else if (str.equals("ferntreegullyroad")) {
				para = "FERNTREE GULLY ROAD";
			} else if (str.equals("franklinstreet")) {
				para = "FRANKLIN STREET";
			} else if (str.equals("jackaboulevard")) {
				para = "JACKA BOULEVARD";
			} else if (str.equals("kingstreet")) {
				para = "KING STREET";
			} else if (str.equals("kingsway")) {
				para = "KINGS WAY";
			} else if (str.equals("lonsdalestreet")) {
				para = "LONSDALE STREET";
			} else if (str.equals("lygonstreet")) {
				para = "LYGON STREET";
			} else if (str.equals("napierstreet")) {
				para = "NAPIER STREET";
			} else if (str.equals("nicholsonstreet")) {
				para = "NICHOLSON STREET";
			} else if (str.equals("powerstreet")) {
				para = "POWER STREET";
			} else if (str.equals("princeshighway")) {
				para = "PRINCES HIGHWAY";
			} else if (str.equals("puntroad")) {
				para = "PUNT ROAD";
			} else if (str.equals("queenstreet")) {
				para = "QUEEN STREET";
			} else if (str.equals("queensroad")) {
				para = "QUEENS ROAD";
			} else if (str.equals("rathdownestreet")) {
				para = "RATHDOWNE STREET";
			} else if (str.equals("russellstreet")) {
				para = "RUSSELL STREET";
			} else if (str.equals("stkildaroad")) {
				para = "ST KILDA ROAD";
			} else if (str.equals("victoriaparade")) {
				para = "VICTORIA PARADE";
			} else if (str.equals("whitemanstreet")) {
				para = "WHITEMAN STREET";
			} else if (str.equals("wurundjeriway")) {
				para = "WURUNDJERI WAY";
			}
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
