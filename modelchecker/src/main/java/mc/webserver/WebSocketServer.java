package mc.webserver;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import mc.compiler.JSONToASTConverter;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by sanjay on 18/01/2017.
 */
public class WebSocketServer {
  SocketIOServer server;
  public WebSocketServer() {
    Configuration config = new Configuration();
    config.setHostname("localhost");
    config.setPort(5001);


    server = new SocketIOServer(config);
    server.start();
    server.addEventListener("compile",Map.class, (client, data, ackSender) -> {
      JSONObject ast = new JSONObject(data);
      System.out.println(new JSONToASTConverter().convert(ast));
    });
  }

  public void stop() {
    server.stop();
    System.out.println("Stopped Socket.IO Server.");
  }
}
