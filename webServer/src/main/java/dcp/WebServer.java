package dcp;

/*
 * @author Fengmin Deng
 */

import java.io.File;
import java.net.URISyntaxException;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class WebServer extends Verticle {

	public void start() {
		final Logger log = container.logger();
		log.info("WebServer started.");
		final JsonObject config = container.config();
		final String host = config.getString("host");
		final int port = config.getInteger("port");
		final EventBus eventBus = vertx.eventBus();
		HttpServer server = vertx.createHttpServer();

		try {
			final String path = new File(this.getClass().getResource("/web").toURI()).getAbsolutePath();
			server.requestHandler(new Handler<HttpServerRequest>() {
				public void handle(HttpServerRequest req) {
					String uri = req.uri();
					log.info(uri);
					if (uri.equals("/")) {
						uri = "/index.html";
					}
					if (new File(path + uri).exists()) {
						req.response().sendFile(path + uri);
						log.info("sending file: " + path + uri);
					}
				}
			}).listen(port, host);
		} catch (URISyntaxException e1) {
			container.exit();
		}
	}
}
