package mc.compiler;

import com.microsoft.z3.Context;
import mc.compiler.ast.*;
import mc.util.expr.Expression;

import java.util.Collections;
import java.util.List;

public abstract class TestBase {

    protected final Context context = Expression.mkCtx();

    protected TestBase() throws InterruptedException {
    }

    public SequenceNode constructSequenceNode(String[] actions, ASTNode node){
        for(int i = actions.length - 1; i >= 0; i--){
            ActionLabelNode action = new ActionLabelNode(actions[i], null);
            node = new SequenceNode(action, node, null);
        }

        return (SequenceNode)node;
    }

    public ChoiceNode constructChoiceNode(String[] sequence1, String[] sequence2){
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode node1 = constructSequenceNode(sequence1, terminal);
        SequenceNode node2 = constructSequenceNode(sequence2, terminal);
        return new ChoiceNode(node1, node2, null);
    }

    public ChoiceNode constructChoiceNode(ASTNode process1, ASTNode process2){
    	return new ChoiceNode(process1, process2, null);
    }

    public CompositeNode constructCompositeNode(String operation, String[] sequence1, String[] sequence2){
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode node1 = constructSequenceNode(sequence1, terminal);
        SequenceNode node2 = constructSequenceNode(sequence2, terminal);
        return new CompositeNode(operation, node1, node2, null);
    }

    public CompositeNode constructCompositeNode(String operation, ASTNode process1, ASTNode process2){
    	return new CompositeNode(operation,process1, process2, null);
    }


    public FunctionNode constructFunctionNode(String function, ASTNode process){
        return new FunctionNode(function, Collections.singletonList(process), null);
    }

    public IndexExpNode constructIndexNode(String variable, int start, int end, ASTNode process){
        RangeNode range = new RangeNode(start, end, null);
        return new IndexExpNode(variable, range, process, null);
    }

    public IndexExpNode constructIndexNode(String variable, List<String> set, ASTNode process){
        SetNode node = new SetNode(set, null);
        return new IndexExpNode(variable, node, process, null);
    }
}
