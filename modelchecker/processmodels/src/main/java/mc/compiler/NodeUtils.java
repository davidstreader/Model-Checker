package mc.compiler;

import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.util.expr.Expression;


public class NodeUtils {

  /**
   * The edges reachable from a specified node.
   *
   * @param node the node being checked
   * @return a stream of all the edges
   */
  public static Stream<List<AutomatonEdge>> findLoopsAndPathToRoot(AutomatonNode node) {
    return node.getIncomingEdges().stream().flatMap(edge -> {
      List<AutomatonEdge> visited = new ArrayList<>();
      visited.add(edge);
      return findLoops(node, edge, new ArrayList<>(), visited);
    });
  }

  private static Stream<List<AutomatonEdge>> findLoops(AutomatonNode toFind, AutomatonEdge edge,
                                                       List<AutomatonEdge> path,
                                                       List<AutomatonEdge> visited) {
    if (visited.contains(edge)) {
      return Stream.empty();
    }
    visited.add(edge);
    if (edge.getFrom() == toFind || edge.getFrom().isStartNode()) {
      path.add(edge);
      return Stream.of(path);
    }
    return edge
        .getFrom()
        .getIncomingEdges()
        .stream()
        .flatMap(e -> findLoops(toFind, e, path, visited));
  }

  /**
   * Create an expression resulting from combining all guards between node and start inclusive.
   *
   * @return a map of the variable to expression
   */
  @SneakyThrows(value = {CompilationException.class,InterruptedException.class})
  public static Map<String, Expr> collectVariables(List<AutomatonEdge> edges, Context context) {
    HashMap<String, Expr> exp = new HashMap<>();
    for (AutomatonEdge edge : edges) {
      if (edge.getGuard() != null) {
        Guard guard = edge.getGuard();
        for (String next : guard.getNext()) {
          next = next.replaceAll(":", "");
          next = next.replaceAll("\\$", "");
          next = next.replaceAll("[a-z]+", "\\$$0");
          String variable = next.substring(0, next.indexOf("="));
          String expression = next.substring(next.indexOf("=") + 1);
          Expr newExp = Expression.constructExpression(expression, null, context);
          //If we overwrite a variable, then there is nothing to substitute.
          if (exp.containsKey(variable) && !(newExp instanceof BitVecNum)) {
            exp.put(variable, Expression.substitute(newExp, exp, context));
          } else {
            exp.put(variable, newExp);
          }
        }
      }
    }
    return exp;
  }
}
