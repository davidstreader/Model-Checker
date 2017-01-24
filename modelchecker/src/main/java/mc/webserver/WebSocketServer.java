package mc.webserver;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import mc.compiler.Interpreter;
import mc.compiler.JSONToASTConverter;
import mc.compiler.ReferenceReplacer;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.process_models.ProcessModel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.Arrays;
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
        AbstractSyntaxTree ast = new JSONToASTConverter().convert(new JSONObject(data).getJSONObject("ast"));
        ast = new ReferenceReplacer().replaceReferences(ast);
         Map<String,ProcessModel> map = new Interpreter().interpret(ast);
        System.out.println(map);
      } catch (Exception ex) {
        System.out.println(ansi().render("@|red An error occurred while compiling.|@"));
        new LogMessage("The following error is unrelated to your script. Please report it to the developers").send();
        //Get a stack trace then split it into lines
        String[] lines = ExceptionUtils.getStackTrace(ex).split("\n");
        for (int i = 0; i < lines.length; i++) {
          //if the line contains com.conrun... then we have gotten up to the socketio portion of the stack trace
          //And we can ignore this line and the rest.
          if (lines[i].contains("com.corundumstudio.socketio")) {
            lines = Arrays.copyOfRange(lines,0,i);
            break;
          }
        }
        System.out.println(String.join("\n",lines));
        new LogMessage(String.join("\n",lines),false,true).send();
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
