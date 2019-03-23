import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import handlers.*;
import maverick_data.DatabaseInteraction;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;

public class MaverickData {
    public static void main(String args[]){
        try{
            //Create socket address
            InetSocketAddress address = new InetSocketAddress(6969);

            //Initialize https server
            HttpsServer server = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            //Initialize the keystore
            char[] password = "password".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream inputStream = DatabaseInteraction.class.getClassLoader().getResourceAsStream("maverick_data/testkey.jks");
            keyStore.load(inputStream, password);

            //Create key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password);

            //Create the trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            //Create the HTTPS context and parameters
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext){
                public void configure(HttpsParameters params){
                    try{
                        //Init SSL context
                        SSLContext context = SSLContext.getDefault();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        //Fetch default params
                        SSLParameters defaultSSLParams = context.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParams);
                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
            //Create server event handler context
            server = createHandlerContexts(server);
            //Start the server instance and hope for the best
            server.setExecutor(null);
            server.start();
            //Debug
            System.out.println("Server Running and Listening On " + address);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Create and assign the event handler contexts to their respective call urls
     * @param server Reference to the HTTPS server object
     */
    private static HttpsServer createHandlerContexts(HttpsServer server){
        server.createContext("/item", new ItemRequestHandler());
        server.createContext("/item/new", new ItemRequestHandler("new"));
        server.createContext("/item/get", new ItemRequestHandler("get"));
        server.createContext("/item/edit", new ItemRequestHandler("edit"));
        server.createContext("/item/move", new ItemRequestHandler("move"));
        server.createContext("/item/delete", new ItemRequestHandler("delete"));

        server.createContext("/pallet", new PalletRequestHandler());
        server.createContext("/pallet/new", new PalletRequestHandler("new"));
        server.createContext("/pallet/add", new PalletRequestHandler("add"));
        server.createContext("/pallet/edit", new PalletRequestHandler("edit"));
        server.createContext("/pallet/delete", new PalletRequestHandler("delete"));
        server.createContext("/pallet/move", new PalletRequestHandler("move"));
        server.createContext("/pallet/get", new PalletRequestHandler("get"));

        server.createContext("/po", new PurchaseOrderRequestHandler());
        server.createContext("/po/new", new PurchaseOrderRequestHandler("new"));
        server.createContext("/po/newline", new PurchaseOrderRequestHandler("newline"));
        server.createContext("/po/edit", new PurchaseOrderRequestHandler("edit"));
        server.createContext("/po/delete", new PurchaseOrderRequestHandler("delete"));
        server.createContext("/po/update", new PurchaseOrderRequestHandler("update"));
        server.createContext("/po/get", new PurchaseOrderRequestHandler("get"));

        server.createContext("/user", new UserRequestHandler());
        server.createContext("/user/new", new UserRequestHandler("new"));
        server.createContext("/user/edit", new UserRequestHandler("edit"));
        server.createContext("/user/delete", new UserRequestHandler("delete"));
        server.createContext("/user/authenticate", new UserRequestHandler("authenticate"));
        server.createContext("/user/get", new UserRequestHandler("get"));

        server.createContext("/device", new DeviceRequestHandler());
        server.createContext("/device/edit", new DeviceRequestHandler("edit"));
        server.createContext("/device/import", new DeviceRequestHandler("import"));
        server.createContext("/device/get", new DeviceRequestHandler("get"));
        return server;
    }
}
