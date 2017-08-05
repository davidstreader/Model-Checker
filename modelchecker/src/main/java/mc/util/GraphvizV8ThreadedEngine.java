package mc.util;

import com.eclipsesource.v8.V8;
import guru.nidi.graphviz.engine.AbstractJsGraphvizEngine;
import guru.nidi.graphviz.engine.GraphvizException;
import lombok.Getter;
import mc.webserver.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GraphvizV8ThreadedEngine extends AbstractJsGraphvizEngine {
    private static Logger logger = LoggerFactory.getLogger(GraphvizV8ThreadedEngine.class);
    public GraphvizV8ThreadedEngine() {
        super(true);
    }

    @Override
    public void release() {
    }
    public void init(V8 runtime) throws IOException {
        runtime.executeVoidScript(jsInitEnv());
        runtime.executeVoidScript(jsVizCode("1.8.0"));
    }
    @Override
    protected void doInit() throws Exception {
    }
    @Override
    protected String jsExecute(String call) {
        V8 v8 = V8.createV8Runtime();
        if (Thread.currentThread().isInterrupted()) {
            v8.terminateExecution();
            v8.release(true);
            throw new RuntimeException(new InterruptedException("Interrupted!"));
        }
        v8Engines.get(Thread.currentThread()).add(v8);
        try {
            init(v8);
            return v8.executeStringScript(call);
        } catch (IOException e) {
            e.printStackTrace();
            throw new GraphvizException(e.getMessage());
        } finally {
            v8Engines.get(Thread.currentThread()).remove(v8);
            v8.release(true);
        }
    }
    @Getter
    private static ConcurrentHashMap<Thread,List<V8>> v8Engines= new ConcurrentHashMap<>();

    public static void terminateAllOnThread(Thread thread) {
        List<V8> v8s = v8Engines.remove(thread);
        if (v8s != null) {
            for (V8 v8 : v8s) {
                v8.terminateExecution();
            }
        }
    }
}
