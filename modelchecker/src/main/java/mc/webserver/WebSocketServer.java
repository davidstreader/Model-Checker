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
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

public class WebSocketServer {
  Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
  private SocketIOServer server;
  public WebSocketServer() {
    Configuration config = new Configuration();
    config.setHostname("0.0.0.0");
    config.setPort(5001);
    config.getSocketConfig().setReuseAddress(true);


    server = new SocketIOServer(config);
    server.startAsync();
    server.addEventListener("compile",Map.class, (client, data, ackSender) -> {
      this.client.set(client);
      logger.info(ansi().render("Received compile command from @|yellow "+getSocketHostname()+"|@")+"");
      try {
        AbstractSyntaxTree ast = new JSONToASTConverter().convert(new JSONObject(data).getJSONObject("ast"));
        ast = new ReferenceReplacer().replaceReferences(ast);
         Map<String,ProcessModel> map = new Interpreter().interpret(ast);
         Map<String,Map<String,?>> sendMap = new HashMap<>();
         for (String key: map.keySet()) {
           if (map.get(key) instanceof Automaton) {
             Automaton a = (Automaton) map.get(key);
             Map<String,Object> model = new HashMap<>();
             model.put("rootId",a.getRoot().getId());
             model.put("type","automata");
             model.put("id",a.getId());
             model.put("metaData",a.getMetaData());
             Map<String,Map<String,Object>> edgeMap = new HashMap<>();
             a.getEdges().stream().map(this::convertEdge).forEach(e -> edgeMap.put((String)e.get("id"),e));
             model.put("edgeMap",edgeMap);
             Map<String,Map<String,Object>> nodeMap = new HashMap<>();
             for (AutomatonNode e: a.getNodes()) {
               Map<String,Object> node = new HashMap<>();
               node.put("id",e.getId());
               node.put("locationSet",null);
               node.put("label",e.getLabel());
               node.put("incomingEdgeSet",convertEdges(e.getIncomingEdges()));
               node.put("metaData",e.getMetaData());
               nodeMap.put(e.getId(), node);
             }
             model.put("nodeMap",nodeMap);
             sendMap.put(key,model);
           }
         }
        ProcessReturn ret= new ProcessReturn(sendMap, Collections.emptyMap(),null,(Map)data.get("context"),Collections.emptyList());
        ackSender.sendAckData(ret);
      } catch (Exception ex) {
        logger.error(ansi().render("@|red An error occurred while compiling.|@")+"");
        new LogMessage("The following error is unrelated to your script. Please report it to the developers").send();
        //Get a stack trace then split it into lines
        String[] lines = ExceptionUtils.getStackTrace(ex).split("\n");
        for (int i = 0; i < lines.length; i++) {
          //if the line contains com.conrun... then we have gotten up to the socket.io portion of the stack trace
          //And we can ignore this line and the rest.
          if (lines[i].contains("com.corundumstudio.socketio")) {
            lines = Arrays.copyOfRange(lines,0,i);
            break;
          }
        }
        logger.error(String.join("\n",lines));
        new LogMessage(String.join("\n",lines),false,true).send();
      }
    });
  }
  private Map<String,Object> convertEdge(AutomatonEdge e) {
    Map<String,Object> edge = new HashMap<>();
    edge.put("id",e.getId());
    edge.put("to",e.getTo().getId());
    edge.put("from",e.getFrom().getId());
    edge.put("locationSet",null);
    edge.put("label",e.getLabel());
    edge.put("metaData",e.getMetaData());
    return edge;
  }
  private Set<Map<String,Object>> convertEdges(List<AutomatonEdge> incomingEdges) {
    return incomingEdges.stream().map(this::convertEdge).collect(Collectors.toSet());
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
