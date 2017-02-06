package mc.webserver;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.Getter;
import mc.compiler.CompilationObject;
import mc.compiler.Compiler;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.util.Location;
import mc.webserver.ProcessReturn.SkipObject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;

public class WebSocketServer {
    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
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
                ackSender.sendAckData(compile(data));
            } catch (Exception ex) {
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
                if (ex instanceof CompilationException) {
                    new LogMessage(ex.getMessage().replace("mc.exceptions.",""),false,true,((CompilationException) ex).getLocation()).send();
                    LoggerFactory.getLogger(((CompilationException) ex).getClazz()).error(String.join("\n",lines));
                } else {
                    logger.error(ansi().render("@|red An error occurred while compiling.|@") + "");
                    new LogMessage("The following error is unrelated to your script. Please report it to the developers").send();
                    logger.error(String.join("\n",lines));
                    new LogMessage(String.join("\n",lines),false,true).send();
                }
            }
        });
    }

    private ProcessReturn compile(Map data) throws CompilationException {
        Context context = Context.fromJSON(data.get("context"));
        CompilationObject ret = new Compiler().compile(new JSONObject(data).getJSONObject("ast"));
        Map<String,ProcessModel> processModelMap = ret.getProcessMap();
        List<SkipObject> skipped = processSkipped(processModelMap,context);
        return new ProcessReturn(processModelMap, ret.getOperationResults(),null,context,skipped);
    }

    private List<SkipObject> processSkipped(Map<String, ProcessModel> processMap, Context context) {
        List<SkipObject> skipped = new ArrayList<>();
        for (ProcessModel process: processMap.values()) {
            if (process instanceof Automaton) {
                Automaton automaton = (Automaton) process;
                if (automaton.getMetaData("skipped") != null) {
                    skipped.add(new SkipObject(automaton.getId(),"user",0,0));
                    processMap.put(automaton.getId(),new EmptyProcessModel(automaton));
                }
                if (automaton.getNodes().size() > context.getGraphSettings().getAutoMaxNode()) {
                    skipped.add(new SkipObject(automaton.getId(),
                        "nodes",
                        automaton.getNodes().size(),
                        context.getGraphSettings().getAutoMaxNode()));
                    processMap.put(automaton.getId(),new EmptyProcessModel(automaton));
                }

            }
        }
        return skipped;
    }

    /**
     * A process model with most information stripped out as it will not be rendered.
     */
    @Getter
    private class EmptyProcessModel implements ProcessModel {
        private Set<String> alphabet;
        private Map<String,Object> metaData;
        private String id;
        EmptyProcessModel(Automaton automaton) {
            this.alphabet = automaton.getAlphabet();
            this.metaData = automaton.getMetaData();
            this.id = automaton.getId();
        }
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
