package mc.util;

import com.eclipsesource.v8.V8;
import guru.nidi.graphviz.engine.AbstractJsGraphvizEngine;
import guru.nidi.graphviz.engine.GraphvizException;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GraphvizV8ThreadedEngine extends AbstractJsGraphvizEngine {
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
        if (v8Engines.containsKey(thread)) {
            for (V8 v8 : v8Engines.get(thread)) {
                if (v8 == null) {
                    return;
                }
                v8.terminateExecution();
                v8.release(true);
            }
        }
        v8Engines.remove(thread);
    }
}
