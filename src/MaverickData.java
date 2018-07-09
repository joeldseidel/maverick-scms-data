import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import handlers.AuthenticateUserHandler;
import handlers.AddItemHandler;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
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
            FileInputStream inputStream = new FileInputStream("testkey.jks");
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
            server.createContext("/authenticate_user", new AuthenticateUserHandler());
            server.createContext("/add_item", new AddItemHandler());
            //Create the context of the commands and the handlers in this line
            server.setExecutor(null);
            server.start();

            //Debug 
            System.out.println("Server Running and Listening On " + address);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
}