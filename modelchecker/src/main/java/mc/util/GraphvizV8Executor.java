package mc.util;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.utils.V8Executor;
import lombok.Getter;

import java.io.IOException;

public class GraphvizV8Executor extends V8Executor {
    private final GraphvizV8ThreadedEngine engine;
    @Getter
    private V8Array messages;
    public GraphvizV8Executor(String script, GraphvizV8ThreadedEngine engine) {
        super(script);
        this.engine = engine;
    }
    @Override
    public void setup(final V8 runtime) {
        try {
            engine.init(runtime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
