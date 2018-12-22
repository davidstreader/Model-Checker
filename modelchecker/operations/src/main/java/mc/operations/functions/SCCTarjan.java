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

  public List<List<AutomatonNode>> scc(Multimap<AutomatonNode,AutomatonNode> graph) {
    int n = graph.size();
    this.graph = graph;
    //visited = new boolean[n];
    stack = new Stack<>();
    time = 0;
   // lowlink = new int[n];
    components = new ArrayList<>();

    for (AutomatonNode u : graph.keySet())
      if (!visited.containsKey(u)|| !visited.get(u))
        dfs(u);
 printCSS(components);
    return components;
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
  /* Usage example
  public static void main(String[] args) {
    List<Integer>[] g = new List[3];
    for (int i = 0; i < g.length; i++)
      g[i] = new ArrayList<>();

    g[2].add(0);
    g[2].add(1);
    g[0].add(1);
    g[1].add(0);

    List<List<Integer>> components = new SCCTarjan().scc(g);
    System.out.println(components);
  }
  */
}
