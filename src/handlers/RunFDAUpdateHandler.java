package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server_events.FDADataUpdate;

/**
 * This is a temporary handler to allow the FDA update to run at prompt from the client
 *
 * @author Joel Seidel
 */

public class RunFDAUpdateHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange){
        new FDADataUpdate();
    }
}
