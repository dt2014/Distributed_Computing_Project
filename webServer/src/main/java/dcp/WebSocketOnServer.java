package dcp;

/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * 
 * @author Fengmin Deng (Student ID: 659332)
 */

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.http.WebSocketFrame;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

/**
 * This verticle represents a WebSocket that serves the client with tweets data
 * specified by the client, e.g. tweets from a certain date in a city.
 *
 */
public class WebSocketOnServer extends Verticle {
    
    public void start() {
        final Logger log = container.logger();
        log.info("WebSocketOnServer started.");
        final JsonObject config = container.config();
        final String host = config.getString("host");
        final int port = config.getInteger("port");
        final EventBus eventBus = vertx.eventBus();
        HttpServer server = vertx.createHttpServer();

        /**
         * Opens a web socket for a request, receives queries and let QueryVerticle
         *   to handle the querying task, and keeps RealTimeClients updated with
         *   the ID of the connected clients.
         */
        server.websocketHandler(new Handler<ServerWebSocket>() {
            public void handle(final ServerWebSocket webSocket) {
//              log.info("ServerWebSocket - path(): " + webSocket.path());
                if (!webSocket.path().equals("/ws") && !webSocket.path().equals("/ws/")) {
                    webSocket.reject();
                } else {
                    final String webSocketQuery = webSocket.query();
                    if (webSocketQuery != null && webSocketQuery.equals("realtime")) {
                        eventBus.send(Constants.QUEUE_WSH, webSocket.textHandlerID());
                    }
                    
                    webSocket.frameHandler(new Handler<WebSocketFrame>() {
                        @Override
                        public void handle(WebSocketFrame webSocketFrame) {
                            //Handle the client 
                            if (webSocketFrame.isText()) {
                                log.info("webSocketFrame.textData(): " + webSocketFrame.textData());
                                String[] request = webSocketFrame.textData().split("-");
                                if (request.length == 3) { //check if number of parameters is correct
                                    eventBus.send(Constants.QUEUE_DB_VIEW, webSocket.textHandlerID() + 
                                            ":" + request[0] + ":" + request[1] + ":" + request[2]);
                                }
                            }
                        }
                    });
                    
                    webSocket.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void arg0) {
                            /* QUOTE from API:
                             * java.lang.String textHandlerID()
                             * When a Websocket is created it automatically registers an event handler with the eventbus, 
                             * the ID of that handler is given by textHandlerID. Given this ID, a different event loop 
                             * can send a text frame to that event handler using the event bus and that buffer will be 
                             * received by this instance in its own event loop and written to the underlying connection. 
                             * This allows you to write data to other websockets which are owned by different event loops(threads).
                             */
                            if (webSocketQuery != null && webSocketQuery.equals("realtime")) {
                                eventBus.send(Constants.QUEUE_WSH, webSocket.textHandlerID());
                            }
                        }
                    });
                }
            }
        }).listen(port, host);
    }
}
