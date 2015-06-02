package dcp;

/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * 
 * @author Fengmin Deng (Student ID: 659332)
 */

import java.io.File;
import java.net.URISyntaxException;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

/**
 * This class represents the Web Server that answers requests of the web 
 *   content (png, html, css and js files).
 *
 */
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
                public void handle(final HttpServerRequest req) {
                    String uri = req.uri();
                    log.info("uri: " + uri);
                    
                    String query = req.query();
                    if (query != null && query.equals("num_tweets")) {
                        log.info("Server query: " + query);
                        eventBus.send(Constants.QUEUE_NUM_TWEETS, "", new Handler<Message<JsonObject>>(){
                            @Override
                            public void handle(Message<JsonObject> reply) {
                                JsonObject numTwees = reply.body();
                                log.info("numTwees.toString(): " + numTwees.toString());
                                req.response().putHeader("Access-Control-Allow-Origin", "*").end(numTwees.toString());
                            }
                        });
                    }
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
