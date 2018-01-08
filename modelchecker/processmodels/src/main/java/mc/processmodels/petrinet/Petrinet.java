package mc.processmodels.petrinet;


import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import mc.compiler.ast.HidingNode;
import mc.compiler.ast.RelabelElementNode;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.ProcessType;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.util.Location;

@Data
public class Petrinet extends ProcessModelObject implements ProcessModel {

  private Map<String, PetriNetPlace> places = new HashMap<>();
  private Map<String, PetriNetTransition> transitions = new HashMap<>();
  private Multimap<String, PetriNetTransition> alphabet = MultimapBuilder.hashKeys()
      .arrayListValues()
      .build();
  private Map<String, PetriNetEdge> edges = new HashMap<>();
  private Set<PetriNetPlace> roots = new HashSet<>();
  private Set<RelabelElementNode> relabels = new HashSet<>();

  private HidingNode hiding;
  private Set<String> hiddenVariables = new HashSet<>();
  private Location    hiddenVariablesLocation;

  private Set<String> variables = new HashSet<>();
  private Location    variablesLocation;

  private Location location;
  private String id;
  private int placeId = 0;
  private int transitionId = 0;
  private int edgeId = 0;

  public Petrinet(String id) {
    this(id, true);
  }

  public Petrinet(String id, boolean constructRoot){
    super(id, "Petrinet");
    this.id = id;
    if(constructRoot){
      PetriNetPlace  origin = addPlace();
      //TODO:
    }
  }

  public void addRoot(PetriNetPlace place) {
    if (places.values().contains(place)) {
      roots.add(place);
    }
  }

  public PetriNetPlace addPlace() {
    String id = this.id + ":" + placeId++;
    PetriNetPlace place = new PetriNetPlace(id);
    places.put(id, place);
    return place;
  }

  public PetriNetTransition addTransition(String label) {
    String id = this.id + ":" + transitionId++;
    PetriNetTransition transition = new PetriNetTransition(id, label);
    transitions.put(id, transition);
    return transition;
  }

  public void addEdge(PetriNetTransition to, PetriNetPlace from) throws CompilationException {
    if (transitions.containsValue(to) && places.containsValue(from)) {
      String id = this.id + ":" + edgeId++;
      PetriNetEdge edge = new PetriNetEdge(id, to, from);
      edges.put(id, edge);
    }
    throw new CompilationException(getClass(), "Cannot add an edge to an object not inside the petrinet");
  }

  public void addEdge(PetriNetPlace to, PetriNetTransition from) throws CompilationException {
    if (transitions.containsValue(to) && places.containsValue(from)) {
      String id = this.id + ":" + edgeId++;
      PetriNetEdge edge = new PetriNetEdge(id, to, from);
      edges.put(id, edge);
    }
    throw new CompilationException(getClass(), "Cannot add an edge to an object not inside the petrinet");
  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.PETRINET;
  }

}
