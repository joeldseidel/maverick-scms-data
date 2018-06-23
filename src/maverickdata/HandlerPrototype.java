package maverickdata;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public abstract class HandlerPrototype {
    protected String GetQuery(HttpExchange httpExchange) throws IOException {
        BufferedReader parameterReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), "utf-8"));
        return parameterReader.readLine();
    }
    protected List<Parameter> ParseQuery(String query){
        if(query.equals("")){
            return null;
        }
        return null;
    }
}
