package mc.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import mc.compiler.CompilationObject;
import mc.compiler.Compiler;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.webserver.ProcessReturn.SkipObject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.fusesource.jansi.Ansi.ansi;
@WebSocket
public class WebSocketServer {
    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        isStopped.set(false);
        String hostName = user.getRemoteAddress().getHostName();
        if (runners.containsKey(hostName)) runners.get(hostName).interrupt();
        if (loggers.containsKey(hostName)) loggers.get(hostName).interrupt();
        BlockingQueue<LogMessage> queue = new LinkedBlockingQueue<>();
        Thread logThread = new Thread(() -> {
            client.set(user);
            try {
                while (!Thread.interrupted()) {
                    queue.take().send();
                }
            } catch (InterruptedException ignored) {
            }
        });
        loggers.put(hostName,logThread);
        logThread.start();
        Thread runner = new Thread(()-> {
            try {
                client.set(user);
                messageQueue.set(queue);
                ObjectMapper mapper = new ObjectMapper();
                CompileRequest data = mapper.readValue(message, CompileRequest.class);
                logger.info(ansi().render("Received compile command from @|yellow " + getSocketHostname() + "|@") + "");
                HashMap<String, Object> ret2 = new HashMap<>();
                try {
                    ProcessReturn ret = compile(data);
                    ret2.put("data", ret);
                } catch (Exception ex) {
                    //Get a stack trace then split it into lines
                    String[] lineSplit = ExceptionUtils.getStackTrace(ex).split("\n");
                    for (int i = 0; i < lineSplit.length; i++) {
                        //if the line contains com.conrun... then we have gotten up to the socket.io portion of the stack trace
                        //And we can ignore this line and the rest.
                        if (lineSplit[i].contains("mc.webserver")) {
                            lineSplit = Arrays.copyOfRange(lineSplit, 1, i);
                            break;
                        }
                    }
                    String lines = String.join("\n", lineSplit);
                    if (ex instanceof CompilationException) {
                        ret2.put("data", new ErrorMessage(ex.getMessage().replace("mc.exceptions.", ""), ((CompilationException) ex).getLocation()));
                        LoggerFactory.getLogger(((CompilationException) ex).getClazz()).error(ex + "\n" + lines);
                    } else {
                        logger.error(ansi().render("@|red An error occurred while compiling.|@") + "");
                        logger.error(ex + "\n" + lines);
                        ret2.put("data", new ErrorMessage(ex + "", lines, null));
                    }
                }
                logThread.interrupt();
                ret2.put("event", "compileReturn");
                user.getRemote().sendString(mapper.writeValueAsString(ret2));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        runners.put(hostName,runner);
        runner.start();
    }

    private ProcessReturn compile(CompileRequest data) throws CompilationException {
        CompilationObject ret = new Compiler().compile(data.getCode(),data.getContext());
        Map<String,ProcessModel> processModelMap = ret.getProcessMap();
        List<SkipObject> skipped = processSkipped(processModelMap,data.getContext());
        return new ProcessReturn(processModelMap, ret.getOperationResults(),ret.getEquationResults(),data.getContext(),skipped);
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
     * Send a message to the client while compiling.
     * @param event the event
     * @param obj the message
     * @param <T> The message type
     */
    public static <T> void send(String event, T obj) {
        Map<String,Object> map = new HashMap<>();
        map.put("event",event);
        map.put("data",obj);
        try {
            client.get().getRemote().sendString(new ObjectMapper().writeValueAsString(map));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        public void addMetaData(String key, Object value){};
        public void removeMetaData(String key){}
        public boolean hasMetaData(String key){ return false; }
    }

    /**
     * Each client is given its own thread to compile in. Storing the client in a ThreadLocal means
     * that we get access to the client from anywhere during the compilation.
     */
    private static HashMap<String,Thread> runners = new HashMap<>();
    private static HashMap<String,Thread> loggers = new HashMap<>();
    private static ThreadLocal<Session> client = new ThreadLocal<>();
    private static ThreadLocal<Boolean> isStopped = new ThreadLocal<>();
    @Getter
    private static ThreadLocal<BlockingQueue<LogMessage>> messageQueue = new ThreadLocal<>();
    /**
     * Get the hostname of the current client
     * @return the hostname of the current client
     */
    public static String getSocketHostname() {
        return client.get().getRemoteAddress().getHostString();
    }
    public static boolean hasClient() {
        return client.get() != null;
    }
    public static boolean isStopped() {
        return isStopped.get() == null?false:isStopped.get();
    }
}
