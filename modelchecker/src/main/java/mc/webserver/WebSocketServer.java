package mc.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import mc.compiler.CompilationObject;
import mc.compiler.Compiler;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.webserver.webobjects.*;
import mc.webserver.webobjects.ProcessReturn.SkipObject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fusesource.jansi.Ansi.ansi;
@WebSocket
public class WebSocketServer {
    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        interruptSession(user);
        isStopped.put(user,new AtomicBoolean(false));
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
        loggers.put(user,logThread);
        logThread.start();
        Thread runner = new Thread(()-> {
            try {
                client.set(user);
                messageQueue.set(queue);
                ObjectMapper mapper = new ObjectMapper();
                CompileRequest data = mapper.readValue(message, CompileRequest.class);
                logger.info(ansi().render("Received compile command from @|yellow " + user.getRemoteAddress().getHostString() + "|@") + "");
                Object ret;
                try {
                    ret = compile(data);
                } catch (Exception ex) {
                    ex.printStackTrace();
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
                        ret = new ErrorMessage(ex.getMessage().replace("mc.exceptions.", ""), ((CompilationException) ex).getLocation());
//                        LoggerFactory.getLogger(((CompilationException) ex).getClazz()).error(ex + "\n" + lines);
                    } else {
                        logger.error(ansi().render("@|red An error occurred while compiling.|@") + "");
                        logger.error(ex + "\n" + lines);
                        ret = new ErrorMessage(ex + "", lines, null);
                    }
                }
                logThread.interrupt();
                send("compileReturn", ret);
                user.getRemote().sendString(mapper.writeValueAsString(ret));
                //Ignore as all exceptions here are InterruptedExceptions which we dont care about.
            } catch (Exception ignored) {}
            interruptSession(user);
        });
        runners.put(user,runner);
        runner.start();
    }
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        interruptSession(user);
    }
    private void interruptSession(Session user) {
        if (isStopped.containsKey(user)) isStopped.get(user).set(true);
        if (runners.containsKey(user)) runners.get(user).interrupt();
        if (loggers.containsKey(user)) loggers.get(user).interrupt();
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
                if (automaton.getNodes().size() > context.getAutoMaxNode()) {
                    skipped.add(new SkipObject(automaton.getId(),
                        "nodes",
                        automaton.getNodes().size(),
                        context.getAutoMaxNode()));
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
    private static HashMap<Session,Thread> runners = new HashMap<>();
    private static HashMap<Session,Thread> loggers = new HashMap<>();
    private static HashMap<Session,AtomicBoolean> isStopped = new HashMap<>();
    private static ThreadLocal<Session> client = new ThreadLocal<>();
    @Getter
    private static ThreadLocal<BlockingQueue<LogMessage>> messageQueue = new ThreadLocal<>();

    public static boolean hasClient() {
        return client.get() != null;
    }
    public static AtomicBoolean isStopped() {
        return hasClient()?isStopped.get(client.get()):new AtomicBoolean(false);
    }
}
