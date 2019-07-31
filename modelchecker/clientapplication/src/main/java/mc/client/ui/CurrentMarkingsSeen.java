package mc.client.ui;

import com.google.common.collect.Multiset;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CurrentMarkingsSeen {
  public static Map<String, Multiset<PetriNetPlace>> currentMarkingsSeen = new TreeMap<>();
  private static Map<String, Multiset<PetriNetPlace>> rootMarkings = new TreeMap<>();
  public static void addRootMarking(String pid,Multiset<PetriNetPlace> root) {
      rootMarkings.put(pid,root);
  }
  public static void setCurrentMarkingsSeen(Map<String, Multiset<PetriNetPlace>> markToSee){
      currentMarkingsSeen = new TreeMap<>();
      currentMarkingsSeen.putAll(markToSee);
      //System.out.println("sett cMS "+currentMarkingsSeen.keySet());
  }
  public static Map<String, Multiset<PetriNetPlace>> getRootMarkings() {
      return rootMarkings;
  }
  public static String myString() {
    StringBuilder sb = new StringBuilder();
    return currentMarkingsSeen.keySet().stream()
      .map(x->x+"->"+currentMarkingToString(x)+"\n")
      .collect(Collectors.joining());
  }
  public static String currentMarkingToString(String pid) {
    return markingToString(currentMarkingsSeen.get(pid));

  }

  public static String  markingToString(Multiset<PetriNetPlace> m) {
    StringBuilder sb = new StringBuilder();
    m.stream().forEach(x -> sb.append(x.getId() + ", "));
    return sb.toString();
  }
  public static Set<String> getIds(String pid){
    return currentMarkingsSeen.get(pid).stream().map(x -> x.getId()).collect(Collectors.toSet());

  }
}
