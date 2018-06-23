package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import maverickdata.*;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthenticateUserHandler extends HandlerPrototype implements HttpHandler {
    public void handle(HttpExchange httpExchange) throws IOException {
        //String query = GetQuery(httpExchange);
        //System.out.println(query);
        String response = "bing bong bing bing bong you people know a lot about trucks";
        httpExchange.sendResponseHeaders(200, response.length());
        System.out.println(response);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {
        if(query != null){
            String pairs[] = query.split("[&]");
            for(String pair : pairs){
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if(param.length > 0){
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }
            }
        }
    }
}
