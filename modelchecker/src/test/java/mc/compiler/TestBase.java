package mc.compiler;

import mc.compiler.ast.*;

import java.util.List;

public abstract class TestBase {

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

    public CompositeNode constructCompositeNode(String[] sequence1, String[] sequence2){
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode node1 = constructSequenceNode(sequence1, terminal);
        SequenceNode node2 = constructSequenceNode(sequence2, terminal);
        return new CompositeNode(node1, node2, null);
    }

    public CompositeNode constructCompositeNode(ASTNode process1, ASTNode process2){
    	return new CompositeNode(process1, process2, null);
    }


    public FunctionNode constructFunctionNode(String function, ASTNode process){
        return new FunctionNode(function, process, null);
    }

    public IndexNode constructIndexNode(String variable, int start, int end, ASTNode process){
        RangeNode range = new RangeNode(start, end, null);
        return new IndexNode(variable, range, process, null);
    }

    public IndexNode constructIndexNode(String variable, List<String> set, ASTNode process){
        SetNode node = new SetNode(set, null);
        return new IndexNode(variable, node, process, null);
    }
}
