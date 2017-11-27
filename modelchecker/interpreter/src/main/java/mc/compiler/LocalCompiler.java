package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.ProcessNode;
import mc.exceptions.CompilationException;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Created by bealjaco on 23/11/17.
 */
@AllArgsConstructor
public class LocalCompiler {
    @Getter
    private HashMap<String,ProcessNode> processNodeMap;
    private Expander expander;
    private ReferenceReplacer replacer;
    private BlockingQueue<Object> messageQueue;

    public ProcessNode compile(ProcessNode node, com.microsoft.z3.Context context) throws CompilationException, InterruptedException {
        node = expander.expand(node,messageQueue,context);
        node = replacer.replaceReferences(node,messageQueue);
        return node;
    }
}
