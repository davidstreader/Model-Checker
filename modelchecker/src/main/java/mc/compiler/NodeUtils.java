package mc.compiler;

import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionSimplifier;
import mc.util.expr.IntegerOperand;

import java.util.*;
import java.util.stream.Stream;

public class NodeUtils {
    public static Stream<List<AutomatonEdge>> findLoopsAndPathToRoot(AutomatonNode node) {
        return node.getIncomingEdges().parallelStream().flatMap(edge ->{
            List<AutomatonEdge> visited = new ArrayList<>();
            visited.add(edge);
            return findLoops(node,edge,new ArrayList<>(),visited);
        });
    }
    private static Stream<List<AutomatonEdge>> findLoops(AutomatonNode toFind, AutomatonEdge edge, List<AutomatonEdge> path, List<AutomatonEdge> visited) {
        if (visited.contains(edge)) return Stream.empty();
        visited.add(edge);
        if (edge.getFrom() == toFind) {
            path.add(edge);
            return Stream.of(path);
        }
        if (edge.getFrom().hasMetaData("startNode")) {
            path.add(edge);
            return Stream.of(path);
        }
        return edge.getFrom().getIncomingEdges().parallelStream().flatMap(e->findLoops(toFind, e, path, visited));
    }

    /**
     * Create an expression resulting from combining all guards between node and start inclusive.
     * @return
     * @throws CompilationException
     */
    @SneakyThrows
    public static Map<String,Expression> collectVariables(List<AutomatonEdge> edges) {
        HashMap<String,Expression> exp = new HashMap<>();
        for (AutomatonEdge edge:edges) {
            if (edge.hasMetaData("guard")) {
                Guard guard = (Guard) edge.getMetaData("guard");
                for (String next: guard.getNext()) {
                    next = next.replaceAll(":","");
                    next = next.replaceAll("\\$","");
                    next = next.replaceAll("[a-z]+","\\$$0");
                    String variable = next.substring(0,next.indexOf("="));
                    String expression = next.substring(next.indexOf("=")+1);
                    Expression newExp = Expression.constructExpression(expression);
                    //If we overwrite a variable, then there is nothing to substitute.
                    if (exp.containsKey(variable) && !(newExp instanceof IntegerOperand)) {
                        exp.put(variable, ExpressionSimplifier.substitute(newExp,exp));
                    } else {
                        exp.put(variable,newExp);
                    }
                }
            }
        }
        return exp;
    }


}
