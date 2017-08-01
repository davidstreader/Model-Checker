package mc.compiler;

import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.Expr;
import lombok.SneakyThrows;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import mc.util.expr.ExpressionSimplifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class NodeUtils {
    public static Stream<List<AutomatonEdge>> findLoopsAndPathToRoot(AutomatonNode node) {
        return node.getIncomingEdges().stream().flatMap(edge ->{
            List<AutomatonEdge> visited = new ArrayList<>();
            visited.add(edge);
            return findLoops(node,edge,new ArrayList<>(),visited);
        });
    }
    private static Stream<List<AutomatonEdge>> findLoops(AutomatonNode toFind, AutomatonEdge edge, List<AutomatonEdge> path, List<AutomatonEdge> visited) {
        if (visited.contains(edge)) return Stream.empty();
        visited.add(edge);
        if (edge.getFrom() == toFind || edge.getFrom().hasMetaData("startNode")) {
            path.add(edge);
            return Stream.of(path);
        }
        return edge.getFrom().getIncomingEdges().stream().flatMap(e->findLoops(toFind, e, path, visited));
    }

    /**
     * Create an expression resulting from combining all guards between node and start inclusive.
     * @return
     */
    @SneakyThrows
    public static Map<String,Expr> collectVariables(List<AutomatonEdge> edges) {
        HashMap<String,Expr> exp = new HashMap<>();
        for (AutomatonEdge edge:edges) {
            if (edge.hasMetaData("guard")) {
                Guard guard = (Guard) edge.getMetaData("guard");
                for (String next: guard.getNext()) {
                    next = next.replaceAll(":","");
                    next = next.replaceAll("\\$","");
                    next = next.replaceAll("[a-z]+","\\$$0");
                    String variable = next.substring(0,next.indexOf("="));
                    String expression = next.substring(next.indexOf("=")+1);
                    Expr newExp = ExpressionSimplifier.constructExpression(expression);
                    //If we overwrite a variable, then there is nothing to substitute.
                    if (exp.containsKey(variable) && !(newExp instanceof BitVecNum)) {
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
