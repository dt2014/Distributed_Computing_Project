package dcp;
/*
 * @author Fengmin Deng
 */

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;


/*
This is the application starter
 */
public class AppStarter extends Verticle {
	
	public void start() {
		final Logger log = container.logger();
		
		JsonObject config = container.config();
		final JsonObject webServerConfig = config.getObject("webServerConfig");
		final JsonObject webSocketConfig = config.getObject("webSocketConfig");
		final JsonObject queryConfig = config.getObject("queryConfig");
		final JsonObject realTimeClientsConfig = config.getObject("realTimeClientsConfig");
		
		container.deployVerticle(RealTimeClients.class.getCanonicalName(), realTimeClientsConfig, 1, new AsyncResultHandler<String>() {
		    public void handle(AsyncResult<String> asyncResult) {
		        if (asyncResult.succeeded()) {
		        	container.deployVerticle(WebSocketOnServer.class.getCanonicalName(), webSocketConfig, 1, new AsyncResultHandler<String>() {
		    		    public void handle(AsyncResult<String> asyncResult) {
		    		        if (asyncResult.succeeded()) {
		    		        	container.deployVerticle(WebServer.class.getCanonicalName(), webServerConfig, 1, new AsyncResultHandler<String>() {
		    		    		    public void handle(AsyncResult<String> asyncResult) {
		    		    		        if (asyncResult.succeeded()) {
		    		    		        	container.deployVerticle(QueryVerticle.class.getCanonicalName(), queryConfig, 1, new AsyncResultHandler<String>() {
		    		    		    		    public void handle(AsyncResult<String> asyncResult) {
		    		    		    		        if (asyncResult.succeeded()) {
		    		    		    		        	log.info("All verticles deployed.");
		    		    		    		        } else {
		    		    		    		        	asyncResult.cause().printStackTrace();
		    		    		    		            container.exit();
		    		    		    		        }
		    		    		    		    }
		    		    		        	});
		    		    		        } else {
		    		    		            asyncResult.cause().printStackTrace();
		    		    		            container.exit();
		    		    		        }
		    		    		    }
		    		    		});
		    		        } else {
		    		            asyncResult.cause().printStackTrace();
		    		            container.exit();
		    		        }
		    		    }
		    		});
		        } else {
		            asyncResult.cause().printStackTrace();
		            container.exit();
		        }
		    }
		});
	}
}
