package mc.webserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.z3.Z3Exception;
import lombok.Getter;
import mc.compiler.CompilationObject;
import mc.compiler.Compiler;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.util.expr.Expression;
import mc.webserver.webobjects.*;
import mc.webserver.webobjects.ProcessReturn.SkipObject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.fusesource.jansi.Ansi.ansi;
@WebSocket
public class WebSocketServer {
    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    private ObjectMapper mapper = new ObjectMapper();
    public WebSocketServer() {
        SendObject keepAlive = new SendObject("tick","keepalive");
        new Thread(()->{
            while(true) {
                runners.values().stream().map(s -> s.logThread.queue).forEach(queue->queue.add(keepAlive));
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(20));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"Socket KeepAlive").start();
    }
    @Getter
    private class LogThread extends Thread {
        BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Session user;
        public LogThread(Session user) {
            super("Log thread");
            this.user = user;
        }
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted() && user.isOpen()) {
                    Object log = queue.take();
                    if (log instanceof LogMessage) {
                        if (((LogMessage) log).hasExpired()) continue;
                        ((LogMessage) log).render();
                        log = new SendObject(log,"log");
                    }
                    if (log instanceof SendObject) {
                        user.getRemote().sendString(new ObjectMapper().writeValueAsString(log));
                    }
                    if (log instanceof String) {
                        user.getRemote().sendString((String) log);
                    }
                }
                queue.clear();
            } catch (InterruptedException | IOException | WebSocketException ignored) {

            }
        }
    }
    private class CompileThread extends Thread {
        volatile CompileRequest req;
        final Session user;
        final LogThread logThread;

        public CompileThread(Session user, LogThread thread) {
            super("Compiler");
            this.user = user;
            logThread = thread;
        }
        @Override
        public synchronized void run() {
            while (user.isOpen()) {
                System.gc();
                Object ret;
                Expression.closeContext(this);
                try {
                    while (req == null) {
                        wait();
                    }
                    CompileRequest request = req;
                    req = null;
                    //Clear interrupted flag
                    Thread.interrupted();
                    ret = compile(request, logThread.queue);
                } catch (InterruptedException ex) {
                    continue;
                } catch (Exception ex) {
                    if (ex.getCause() instanceof InterruptedException || ex.getCause() instanceof ExecutionException) {
                        continue;
                    }
                    ret = getErrorMessage(ex);
                }
                if (Thread.interrupted()) {
                    continue;
                }
                try {
                    logThread.queue.add(new ObjectMapper().writeValueAsString(new SendObject(ret, "compileReturn")));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            Expression.closeContext(this);
            System.gc();
        }
    }

    private ErrorMessage getErrorMessage(Exception ex) {
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
            return new ErrorMessage(ex.getMessage().replace("mc.exceptions.", ""), ((CompilationException) ex).getLocation());
        } else {
            logger.error(ansi().render("@|red An error occurred while compiling.|@") + "");
            logger.error(ex + "\n" + lines);
            return new ErrorMessage(ex + "", lines, null);
        }
    }

    @OnWebSocketMessage
    public synchronized void onMessage(Session user, String message) {
        logger.info(ansi().render("Received compile command from @|yellow " + user.getRemoteAddress().getHostString() + "|@") + "");
        try {
            runners.get(user).req = mapper.readValue(message, CompileRequest.class);
            runners.get(user).interrupt();
            notifyAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketConnect
    public void onOpen(Session user) {
        LogThread logThread = new LogThread(user);
        CompileThread thread = new CompileThread(user,logThread);
        runners.put(user,thread);
        thread.start();
        logThread.start();
    }
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        if (runners.containsKey(user)) {
            runners.remove(user).interrupt();
        }
    }
    private ProcessReturn compile(CompileRequest data, BlockingQueue<Object> messageQueue) throws CompilationException, InterruptedException {
        CompilationObject ret = new Compiler().compile(data.getCode(),data.getContext(),messageQueue);
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
        public void addMetaData(String key, Object value){}

        public void removeMetaData(String key){}
        public boolean hasMetaData(String key){ return false; }
    }

    private static HashMap<Session,CompileThread> runners = new HashMap<>();
}
