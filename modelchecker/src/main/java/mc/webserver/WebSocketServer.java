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
    config.setHostname("0.0.0.0");
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
        System.out.println(ansi().render("@|red An error occurred while compiling.|@"));
        ex.printStackTrace();
        new LogMessage("The following error is unrelated to your script. Please report it to the developers").send();
        new LogMessage(ExceptionUtils.getStackTrace(ex),false,true).send();
      }
    });
  }

  /**
   * Each client is given its own thread to compile in. Storing the client in a ThreadLocal means
   * that we get access to the client from anywhere during the compilation.
   */
  private ThreadLocal<SocketIOClient> client = new ThreadLocal<>();
  public void stop() {
    server.stop();
    System.out.println("Stopped Socket.IO Server.");
  }

  /**
   * Send a message to the client while compiling.
   * @param event the event
   * @param obj the message
   * @param <T> The message type
   */
  public <T> void send(String event, T obj) {
    client.get().sendEvent(event,obj);
  }
  /**
   * Send a message to the client while compiling.
   * @param event the event
   * @param obj the message
   * @param callback a callback to run with the response from the client
   * @param <T> The message type
   */
  public <T> void send(String event, T obj, AckCallback<T> callback) {
    client.get().sendEvent(event,callback,obj);
  }

  /**
   * Get the hostname of the current client
   * @return the hostname of the current client
   */
  public String getSocketHostname() {
    return ((InetSocketAddress)client.get().getRemoteAddress()).getHostString();
  }
}
