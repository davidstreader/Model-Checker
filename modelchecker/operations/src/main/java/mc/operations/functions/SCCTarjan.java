package mc.operations.functions;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

// Amended  From https://sites.google.com/site/indy256/algo/scc_tarjan
// optimized version of http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm
public class SCCTarjan {

  Multimap<AutomatonNode,AutomatonNode> graph = ArrayListMultimap.create();
  Map<AutomatonNode,Boolean> visited = new TreeMap<>();
  //boolean[] visited;
  Stack<AutomatonNode> stack;
  int time;
  Map<AutomatonNode,Integer> lowlink = new TreeMap<>();
  List<List<AutomatonNode>> components;

  /**
   * Used in tau abstraction on graphs
   * @param graph
   * @return a partion of the strongly tau-connected nodes.
   */
  public List<List<String>> scc(Multimap<AutomatonNode,AutomatonNode> graph) {
    this.graph = graph;
    stack = new Stack<>();
    time = 0;
    components = new ArrayList<>();

    for (AutomatonNode u : graph.keySet())
      if (!visited.containsKey(u)|| !visited.get(u))
        dfs(u);
   //printCSS(components);
    List<List<String>> out = new ArrayList<>();
    for(List<AutomatonNode> nds: components){
      out.add(nds.stream().map(x->x.getId()).collect(Collectors.toList()));
    }
    return out;
  }

  void dfs(AutomatonNode u) {
    lowlink.put(u,time++);
    visited.put(u,true);
    stack.add(u);
    boolean isComponentRoot = true;

    for (AutomatonNode v : graph.get(u)) {
      if (!visited.containsKey(v)||!visited.get(v))
        dfs(v);
      if (lowlink.get(u) > lowlink.get(v)) {
        lowlink.put(u, lowlink.get(v));
        isComponentRoot = false;
      }
    }

    if (isComponentRoot) {
      List<AutomatonNode> component = new ArrayList<>();
      while (true) {
        AutomatonNode x = stack.pop();
        component.add(x);
        lowlink.put(x,Integer.MAX_VALUE);
        if (x == u)
          break;
      }
      components.add(component);
    }
  }

  private void printCSS(List<List<AutomatonNode>> comp){
    for (List<AutomatonNode> c:comp){
      System.out.println("CCS "+c.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
    }
  }

}
