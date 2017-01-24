package mc.webserver;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import mc.compiler.JSONToASTConverter;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

public class WebSocketServer {
  SocketIOServer server;
  public WebSocketServer() {
    Configuration config = new Configuration();
    config.setHostname("localhost");
    config.setPort(5001);
    config.getSocketConfig().setReuseAddress(true);


    server = new SocketIOServer(config);
    server.startAsync();
    server.addEventListener("compile",Map.class, (client, data, ackSender) -> {
      this.client.set(client);
      System.out.println(ansi().render("Received compile command from @|yellow "+getSocketHostname()+"|@"));
      JSONObject ast = new JSONObject(data);
      System.out.println(new JSONToASTConverter().convert(ast));
    });
  }
  private ThreadLocal<SocketIOClient> client = new ThreadLocal<>();
  public void stop() {
    server.stop();
    System.out.println("Stopped Socket.IO Server.");
  }
  public <T> void send(String event, T obj) {
    client.get().sendEvent(event,obj);
  }
  public <T> void send(String event, T obj, AckCallback<T> callback) {
    client.get().sendEvent(event,callback,obj);
  }
  public String getSocketHostname() {
    return ((InetSocketAddress)client.get().getRemoteAddress()).getHostString();
  }
}
