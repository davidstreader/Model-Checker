package mc.webserver;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import mc.compiler.JSONToASTConverter;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
      try {
        JSONObject ast = new JSONObject(data);
        System.out.println(new JSONToASTConverter().convert(ast.getJSONObject("ast")));
      } catch (Exception ex) {
        new LogMessage("The following error is unrelated to your script. Please report it to the developers").send();
        new LogMessage(ExceptionUtils.getStackTrace(ex),false,true).send();
      }
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
