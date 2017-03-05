package mc.compiler;

import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionSimplifier;
import mc.util.expr.IntegerOperand;

import java.util.*;
import java.util.stream.Collectors;

public class NodeUtils {
    public static List<List<AutomatonEdge>> findLoopsAndPathToRoot(AutomatonNode node) {
        List<List<AutomatonEdge>> cycles = new ArrayList<>();
        for (AutomatonEdge edge:node.getIncomingEdges()) {
            List<AutomatonEdge> visited = new ArrayList<>();
            visited.add(edge);
            if (edge.getTo() == node) cycles.add(Collections.singletonList(edge));
            for (AutomatonEdge edge2:edge.getFrom().getIncomingEdges()) {
                findLoops(node,edge2,new ArrayList<>(),visited,cycles);
            }
        }
        return cycles;
    }
    private static boolean findLoops(AutomatonNode toFind, AutomatonEdge edge, List<AutomatonEdge> path, List<AutomatonEdge> visited, List<List<AutomatonEdge>> cycles) {
        if (visited.contains(edge)) return false;
        visited.add(edge);
        if (edge.getFrom() == toFind) {
            path.add(edge);
            cycles.add(new ArrayList<>(path));
            return true;
        }
        if (edge.getFrom().hasMetaData("startNode")) {
            path.add(edge);
            cycles.add(new ArrayList<>(path));
            return true;
        }
        for (AutomatonEdge e : edge.getFrom().getOutgoingEdges()) {
            if (findLoops(toFind, e, path, visited, cycles)) {
                path.add(edge);
                return true;
            }
        }
        for (AutomatonEdge e : edge.getFrom().getIncomingEdges()) {
            if (findLoops(toFind, e, path, visited, cycles)) {
                path.add(edge);
                return true;
            }
        }
        return false;
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
