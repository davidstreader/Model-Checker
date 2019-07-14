package mc.client;


import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multiset;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Bounds;
import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.client.graph.*;
import mc.client.ui.CanvasMouseMotionListener;
import mc.client.ui.DoubleClickHandler;
import mc.client.ui.SettingsController;
import mc.compiler.CompilationObject;
import mc.compiler.CompilationObservable;
//import mc.compiler.ImpliesResult;
import mc.compiler.OperationResult;
import mc.processmodels.MappingNdMarking;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.awt.BasicStroke;

/**
 * Created by bealjaco on 29/11/17.
 * Currently called by UseInterfaceController  FMX App
 * and uses  one Graph for all automata and Petri Nets
 *
 * Both will need to be ditched
 */
public class ModelView implements Observer {

  private Graph<GraphNode, DirectedEdge> graph;  //Used by graph layout algorithm
  private Layout<GraphNode, DirectedEdge> layout;
  private SeededRandomizedLayout layoutInitalizer;
  private VisualizationViewer<GraphNode, DirectedEdge> vv;

  private Bounds windowSize;

  private DoubleClickHandler massSelect;
  private CanvasMouseMotionListener cml;
  private Set<String> processModelsToDisplay;
  private SortedSet<String> visibleModels; // Processes that are in the modelsList combox
  private Multimap<String, GraphNode> processModels; //in the list
  // Play places token on current Marking
  private Map<String, Multiset<PetriNetPlace>> currentMarking = new TreeMap<>();
  // from PetriNetPlace find Graph visualisation
  private Map<String,GraphNode> placeId2GraphNode = new TreeMap<>();

  private CompilationObject compiledResult;
  private List<String> processesChanged = new ArrayList<>();

  private Map<String, MappingNdMarking> mappings = new HashMap<>();


  private static final Font sourceCodePro;

  @Setter
  private SettingsController settings; // Contains linkage length and max nodes

//Consumer is a Function with void Return type set by the UserInterfaceController
  @Setter
  private Consumer<Collection<String>> listOfAutomataUpdater;
  @Setter
  private BiConsumer<List<OperationResult>, List<OperationResult>> updateLog;
  //@Setter
  //private BiConsumer<List<ImpliesResult>, List<ImpliesResult>> updateImpLog;

  public ProcessModel getProcess(String id) {
    return compiledResult.getProcessMap().get(id);
  }
  /**
   * This method is called whenever the observed object is changed. An
   * application calls an <tt>Observable</tt> object's
   * <code>notifyObservers</code> method to have all the object's
   * observers notified of the change.
   *
   * @param o   the observable object.
   * @param arg an argument passed to the <code>notifyObservers</code>
   */
  @Override
  public void update(Observable o, Object arg) {

    if (!(arg instanceof CompilationObject)) {
      throw new IllegalArgumentException("arg object was not of type compilationObject");
    }

    processesChanged.clear();
    compiledResult = (CompilationObject) arg;


    // UG Map.Entry  collection of Key,Value pairs
    Set<Map.Entry<String, MultiProcessModel>> toExpand = compiledResult.getProcessMap().entrySet()
        .stream()
        .filter(e -> e.getValue() instanceof MultiProcessModel)
        .map(e -> new Map.Entry<String, MultiProcessModel>() {
          @Override
          public String getKey() {
            return e.getKey();
          }

          @Override
          public MultiProcessModel getValue() {
            return (MultiProcessModel) e.getValue();
          }

          @Override
          public MultiProcessModel setValue(MultiProcessModel value) {
            return null;
          }
        })
        .collect(Collectors.toSet());

//  printing (automata) and (petrinet)
    mappings.clear();
    for (Map.Entry<String, MultiProcessModel> mpm : toExpand) {
      //System.out.println("mpmKey "+ mpm.getKey());
      if (!mpm.getKey().endsWith(":*")) continue; //Only off processes in Domain * - prevents duplicates
      for (ProcessType pt : ProcessType.values()) {
        if (mpm.getValue().hasProcess(pt)) {
          String name = mpm.getKey() + " (" + pt.name().toLowerCase() + ")";
          mpm.getValue().getProcess(pt).setId(name);
          mappings.put(name, mpm.getValue().getProcessNodesMapping());
          compiledResult.getProcessMap().put(name, mpm.getValue().getProcess(pt));
        }
      }
    }

    toExpand.stream().map(Map.Entry::getKey).forEach(compiledResult.getProcessMap()::remove);

    /*System.out.print("\nKeys ");
    getProcessMap().entrySet().stream().forEach(x->{
      //System.out.print(x.getKey()+" ");
    });*/
    String dispType = settings.getDisplayType();
    //System.out.println("\n >>>>>"+dispType+"<<<<<\n");
    if (dispType.equals("All")) {
      visibleModels = getProcessMap().entrySet().stream()
        .filter(e -> e.getValue().getProcessType() != ProcessType.AUTOMATA ||
          ((Automaton) e.getValue()).getNodes().size() <= settings.getMaxNodes())
        .map(Map.Entry::getKey)
        .collect(Collectors.toCollection(TreeSet::new));
    } else if (dispType.equals(Constant.AUTOMATA)) {
      visibleModels = getProcessMap().entrySet().stream()
        .filter(e -> e.getValue().getProcessType() == ProcessType.AUTOMATA &&
          ((Automaton) e.getValue()).getNodes().size() <= settings.getMaxNodes())
        .map(Map.Entry::getKey)
        .collect(Collectors.toCollection(TreeSet::new));
    } else {
      visibleModels = getProcessMap().entrySet().stream()
        .filter(e -> e.getValue().getProcessType() != ProcessType.AUTOMATA)
        .map(Map.Entry::getKey)
        .collect(Collectors.toCollection(TreeSet::new));
    }
    //remove processes marked at skipped and too large models to display
    listOfAutomataUpdater.accept(visibleModels);

    //Calls the function(Consumer) attached to the updateLog
    //updateLog.accept(compiledResult.getOperationResults(), compiledResult.getEquationResults());
  //  updateImpLog.accept(compiledResult.getImpliesResults(), compiledResult.getImpliesResults());
  }

  /**
   * A method to update the graph that is displayed
   *
   * @return the graph component that is displayed
   */

  public VisualizationViewer<GraphNode, DirectedEdge> updateGraph(SwingNode s) {
    if (compiledResult == null) {
      return new VisualizationViewer<>(new DAGLayout<>(new DirectedSparseGraph<>()));
    }

    layoutInitalizer.setDimensions(new Dimension((int) s.getBoundsInParent().getWidth(),
        (int) s.getBoundsInParent().getHeight()));


    compiledResult.getProcessMap().keySet().stream()
        .filter(processModelsToDisplay::contains)
        .filter(processesChanged::contains)
        .map(compiledResult.getProcessMap()::get)
        .filter(Objects::nonNull)
        .forEach(this::addProcess);

    massSelect.updateProcessModelList(processModels);

    if (windowSize == null || !windowSize.equals(s.getBoundsInParent())) {
      windowSize = s.getBoundsInParent();
      layout.setSize(new Dimension((int) windowSize.getWidth(), (int) windowSize.getHeight()));
    }


    // if the font was imported successfully, set the font
    // (the standard font does not display greek symbols)
    // (i.e. tau and delta events)
    if (sourceCodePro != null) {
      vv.getRenderContext().setEdgeFontTransformer(e -> sourceCodePro);
      vv.getRenderContext().setVertexFontTransformer(e -> sourceCodePro);

    }

    //set the colour of the nodes
    vv.getRenderContext().setVertexFillPaintTransformer(n -> n.getNodeColor().getColorNodes());


    //autoscale the graph to fit in the display port
    vv.setPreferredSize(new Dimension((int) windowSize.getWidth(), (int) windowSize.getHeight()));

    //This draws the boxes around the automata
    vv.addPreRenderPaintable(new AutomataBorderPaintable(vv, this.processModels));
    vv.addPostRenderPaintable(new PetriMarkingPaintable(vv,this.processModels, this.currentMarking));
    processesChanged.clear();
    return vv;
  }

  private void addProcess(ProcessModel p) {
    switch (p.getProcessType()) {
      case AUTOMATA:
        addAutomata((Automaton) p);
        break;
      case PETRINET:
        addPetrinet((Petrinet) p);
        break;
    }
  }

  /**
   * Add an individual automata to the graph
   *
   * @param automaton the automata object
   */
  private void addAutomata(Automaton automaton) {
   //System.out.println("ModelView addAutomata");
    //make a new "parent" object for the children to be parents of
    if (processModels.containsKey(automaton.getId())) {
      // If the automaton is already displayed, but modified.
      // Remove all vertexes that are part of it
      for (GraphNode n : processModels.get(automaton.getId())) {
        graph.removeVertex(n);
      }
      processModels.removeAll(automaton.getId());
    }

    Map<String, GraphNode> nodeMap = new HashMap<>();

    //add all the nodes to the graph
    automaton.getNodes().forEach(n -> {

      NodeStates nodeTermination = NodeStates.NOMINAL;

      if (n.isStartNode()) {
        if (!n.isSTOP()) {
          nodeTermination = NodeStates.START;
          if (automaton.getRootList().size() > 1 && n.getId().equals(automaton.getRootList().get(1).getId()))
            nodeTermination = NodeStates.START1;
          else if (automaton.getRootList().size() > 2 && n.getId().equals(automaton.getRootList().get(2).getId()))
            nodeTermination = NodeStates.START2;
          else if (automaton.getRootList().size() > 3 && n.getId().equals(automaton.getRootList().get(3).getId()))
            nodeTermination = NodeStates.START3;
          else if (automaton.getRootList().size() > 4 && n.getId().equals(automaton.getRootList().get(4).getId()))
            nodeTermination = NodeStates.START4;
          else if (automaton.getRootList().size() > 5 && n.getId().equals(automaton.getRootList().get(5).getId()))
            nodeTermination = NodeStates.START5;
        } else {
          nodeTermination = NodeStates.STOPSTART;
          if (automaton.getRootList().size() > 1 && n.getId().equals(automaton.getRootList().get(1).getId()))
            nodeTermination = NodeStates.STOPSTART1;
          else if (automaton.getRootList().size() > 2 && n.getId().equals(automaton.getRootList().get(2).getId()))
            nodeTermination = NodeStates.STOPSTART2;
          else if (automaton.getRootList().size() > 3 && n.getId().equals(automaton.getRootList().get(3).getId()))
            nodeTermination = NodeStates.STOPSTART3;
          else if (automaton.getRootList().size() > 4 && n.getId().equals(automaton.getRootList().get(4).getId()))
            nodeTermination = NodeStates.STOPSTART4;
          else if (automaton.getRootList().size() > 5 && n.getId().equals(automaton.getRootList().get(5).getId()))
            nodeTermination = NodeStates.STOPSTART5;
        }
      } else {
        if (n.isERROR()) {
          nodeTermination = NodeStates.ERROR;
        } else {
          if (n.isSTOP()) {
            nodeTermination = NodeStates.STOP;
            if (automaton.getEndList().size() > 1 && n.getId().equals(automaton.getEndList().get(1)))
              nodeTermination = NodeStates.STOP1;
            else if (automaton.getEndList().size() > 2 && n.getId().equals(automaton.getEndList().get(2)))
              nodeTermination = NodeStates.STOP2;
            else if (automaton.getEndList().size() > 3 && n.getId().equals(automaton.getEndList().get(3)))
              nodeTermination = NodeStates.STOP3;
            else if (automaton.getEndList().size() > 4 && n.getId().equals(automaton.getEndList().get(4)))
              nodeTermination = NodeStates.STOP4;
            else if (automaton.getEndList().size() > 5 && n.getId().equals(automaton.getEndList().get(5)))
              nodeTermination = NodeStates.STOP5;
          }
        }
      }


      GraphNode node = new GraphNode(automaton.getId(), n.getId(), nodeTermination,
          NodeType.AUTOMATA_NODE, "" + n.getLabelNumber(), n);
      nodeMap.put(n.getId(), node);

      graph.addVertex(node);
    });

    automaton.getEdges().forEach(e -> {
      GraphNode to = nodeMap.get(e.getTo().getId());
      GraphNode from = nodeMap.get(e.getFrom().getId());
      String label = e.getLabel();
      String bool; String ass;
      if (e.getGuard()!=null) {
        bool = e.getGuard().getGuardStr();
        ass = e.getGuard().getAssStr();
      } else {
        bool=""; ass="";
      }


      graph.addEdge(new DirectedEdge(bool,label + "" ,ass, UUID.randomUUID().toString()), from, to);
    });

    this.processModels.replaceValues(automaton.getId(), nodeMap.values());

//System.out.println("ModelView \n "+ automaton.myString());
  }


  /**
   * Adding a PetriNet to the observed Graph
   * @param petri
   */
  private void addPetrinet(Petrinet petri) {

    //make a new "parent" object for the children to be parents of
    if (processModels.containsKey(petri.getId())) {
      // If the automaton is already displayed, but modified.
      // Remove all vertexes that are part of it
      for (GraphNode n : processModels.get(petri.getId())) {
        placeId2GraphNode.remove(n.getNodeId());
        graph.removeVertex(n);
      }

      processModels.removeAll(petri.getId());
    }

    Map<String, GraphNode> nodeMap = new HashMap<>();

    Multiset<PetriNetPlace> rts = HashMultiset.create(); // .create(rts);
    petri.getPlaces().values().forEach(place -> {
      NodeStates nodeTermination = NodeStates.NOMINAL;
      if (place.isTerminal()) {
        nodeTermination = NodeStates.valueOf(place.getTerminal().toUpperCase());
      }
      if (place.isStart()) {
        //System.out.println("Root "+ place.getId());
        if (!place.isSTOP()) {
          rts.add(place);
          nodeTermination = NodeStates.START;
        if (place.getMaxStartNo() == 2)
          nodeTermination = NodeStates.START1;
        else if (place.getMaxStartNo() == 3)
          nodeTermination = NodeStates.START2;
        else if (place.getMaxStartNo() == 4)
          nodeTermination = NodeStates.START3;
        else if (place.getMaxStartNo() == 5)
          nodeTermination = NodeStates.START4;
        else if (place.getMaxStartNo() == 6)
          nodeTermination = NodeStates.START5;
      } else {
          nodeTermination = NodeStates.STOPSTART;
          if (place.getMaxStartNo() == 2)
            nodeTermination = NodeStates.STOPSTART1;
          else if (place.getMaxStartNo() == 3)
            nodeTermination = NodeStates.STOPSTART2;
          else if (place.getMaxStartNo() == 4)
            nodeTermination = NodeStates.STOPSTART3;
          else if (place.getMaxStartNo() == 5)
            nodeTermination = NodeStates.STOPSTART4;
          else if (place.getMaxStartNo() == 6)
            nodeTermination = NodeStates.STOPSTART5;
        }
    } else if (place.isSTOP()) {
        nodeTermination = NodeStates.STOP;
        if (place.getMaxEndNo()==2)
          nodeTermination = NodeStates.STOP1;
        else if (place.getMaxEndNo()==3)
          nodeTermination = NodeStates.STOP2;
        else if (place.getMaxEndNo()==4)
          nodeTermination = NodeStates.STOP3;
        else if (place.getMaxEndNo()==5)
          nodeTermination = NodeStates.STOP4;
        else if (place.getMaxEndNo()==6)
          nodeTermination = NodeStates.STOP5;
      } else if (place.isERROR()) {
        nodeTermination = NodeStates.ERROR;
      }

      //System.out.println("Owners setting "+ settings.isShowOwners());
      String lab="." ;
      if (settings.isShowOwners()) {
        //System.out.println("Owners added");
        for(String o:place.getOwners()){lab+=o;}
      } else {
        lab = "";
      }

      GraphNode node = new GraphNode(petri.getId(), place.getId(),
          nodeTermination, NodeType.PETRINET_PLACE, lab, place);
      placeId2GraphNode.put(place.getId(), node);
      graph.addVertex(node);
      nodeMap.put(place.getId(), node);
    });
    currentMarking.put(petri.getId(),rts);

    petri.getTransitions().values().forEach(transition -> {
      String lab=transition.getLabel()+".";
      if (settings.isShowOwners()) {
        for(String o:transition.getOwners()){lab+=o;}
      } else {
        lab = transition.getLabel();
      }
      GraphNode node = new GraphNode(petri.getId(), transition.getId(),
          NodeStates.NOMINAL, NodeType.PETRINET_TRANSITION, lab, transition);
      nodeMap.put(transition.getId(), node);

    });
    float dash[] = { 10.0f };
     for (PetriNetEdge edge: petri.getEdges().values()) {
       //System.out.println(edge.myString());
      // dstr commented out below 13/7/19
      //   vv.getRenderContext().setEdgeStrokeTransformer(e -> new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
      //           BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
      // EdgeType et = EdgeType.DIRECTED;
       String lab = "";
       if (edge.getOptional()) {
        // et = EdgeType.UNDIRECTED;//NICE try but fails
         lab = "Opt";
       }
       String b ; String a;
       if (edge.getGuard()!=null) {
         b = edge.getGuard().getGuardStr();
         a = edge.getGuard().getAssStr();
         //System.out.println("ModelView "+edge.getGuard().myString());
         //System.out.println("ModelView b "+b+" a "+a);
       } else {
         b = "";
         a = "";
       }
       DirectedEdge nodeEdge = new DirectedEdge(b,
                   lab,
                   a,
                   UUID.randomUUID().toString());
       //System.out.println("Nodes in Map "+ nodeMap.keySet());
       //System.out.println("  toId "+edge.getTo().getId());
       //System.out.println("fromId "+edge.getFrom().getId());
       //System.out.println("Before addEdge" +nodeEdge.getAll());
       //System.out.println("from "+nodeMap.get(edge.getFrom().getId()));
       //System.out.println("to   "+nodeMap.get(edge.getTo().getId()));
       graph.addEdge(nodeEdge, nodeMap.get(edge.getFrom().getId()),
               nodeMap.get(edge.getTo().getId()));
     }
   /* petri.getEdges().values().forEach(edge -> {
        vv.getRenderContext().setEdgeStrokeTransformer(e -> new BasicStroke(4.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, dash, 1.0f));

      DirectedEdge nodeEdge = new DirectedEdge("", UUID.randomUUID().toString());
      graph.addEdge(nodeEdge, nodeMap.get(edge.getFrom().getId()),
          nodeMap.get(edge.getTo().getId()));

    }); */

    this.processModels.replaceValues(petri.getId(), nodeMap.values());

  }

  /**
   * @param modelLabel The name of the model process to be displayed / added to display.
   */
  public void addDisplayedModel(String modelLabel) {
    assert compiledResult.getProcessMap().containsKey(modelLabel);
    assert visibleModels.contains(modelLabel);

    processesChanged.add(modelLabel);
    processModelsToDisplay.add(modelLabel);
  }

  public void clearDisplayed() {
    processModelsToDisplay.clear();
    initalise();
  }

  public void addAllModels() {
    processModelsToDisplay.clear();
    processesChanged.addAll(compiledResult.getProcessMap().keySet());

    if (visibleModels != null) {
      processModelsToDisplay.addAll(visibleModels);
    }
  }

  /**
   * All functions below deal with "freezing" vertexes, to disallow the layout algorithm
   * to act upon the vertexes.
   */
  public void freezeAllCurrentlyDisplayed() {
    if (layout != null) {
      for (String processModeName : processModels.keySet()) {
        for (GraphNode vertexToLock : processModels.get(processModeName)) {
          layout.lock(vertexToLock, true);

        }
      }
    }
  }

  public void unfreezeAllCurrentlyDisplayed() {
    if (layout != null) {
      for (String processModeName : processModels.keySet()) {
        for (GraphNode vertexToLock : processModels.get(processModeName)) {
          layout.lock(vertexToLock, false);

        }
      }
    }
  }

  public void freezeProcessModel(String automataLabel) {


    if (layout != null && automataLabel != null && processModelsToDisplay.contains(automataLabel)) {

      for (GraphNode vertexToLock : processModels.get(automataLabel)) {
        layout.lock(vertexToLock, true);
      }
    }
  }

  public void unfreezeProcessModel(String automataLabel) {
    if (layout != null && automataLabel != null && processModelsToDisplay.contains(automataLabel)) {
      for (GraphNode vertexToLock : processModels.get(automataLabel)) {
        layout.lock(vertexToLock, false);
      }
    }
  }

  private Map<String, ProcessModel> getProcessMap() {
    return compiledResult.getProcessMap();
  }

  /**
   * Resets all graph varaibles and re-adds default blank state.
   */
  private void initalise() {


    processModelsToDisplay = new HashSet<>();

    layoutInitalizer = new SeededRandomizedLayout();

    graph = new DirectedSparseMultigraph<>();

    //apply a layout to the graph Note the linkageLength changes the layout in real time

    layout = new SpringlayoutBase<>(graph,
            x -> settings.getMaxNodes(),
            x -> settings.getSpring(),
            x -> settings.getRepulse(),
            x -> settings.getStep(),
            x -> settings.getDelay(),
            x -> settings.isShowOwners(),
            x->  settings.isShowColor()

    );


    ((SpringlayoutBase) layout).setStretch(0.8);
    ((SpringlayoutBase) layout).setRepulsionRange(1000);

    layout.setInitializer(layoutInitalizer);

    vv = new VisualizationViewer<>(layout);

    vv.getRenderingHints().remove( //As this seems to be very expensive in jung
        RenderingHints.KEY_ANTIALIASING);

    //create a custom mouse controller (both movable, scalable and manipulatable)
    PluggableGraphMouse gm = new PluggableGraphMouse();
    gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON2_MASK));
    gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON3_MASK));
    gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1.1f, 0.9f));
    gm.add(new PickingGraphMousePlugin<>());

    vv.setGraphMouse(gm);

    massSelect = new DoubleClickHandler(processModels, vv, mappings,currentMarking);
    cml = new CanvasMouseMotionListener(vv,currentMarking);
    vv.addMouseListener(massSelect);
    vv.addMouseMotionListener(cml);
    //System.out.println();

    //label the nodes
    vv.getRenderContext().setVertexLabelTransformer(GraphNode::getLabel);
    vv.getRenderContext().setEdgeLabelTransformer(DirectedEdge::getAll);
    //vv.getRenderContext().setEdgeArrowStrokeTransformer(edgeStroke);
    //vv.getRenderContext().setEdgeStrokeTransformer(e->edgeStroke);
    //set the shape
    vv.getRenderContext().setVertexShapeTransformer(n -> n.getType().getNodeShape());
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

    // Sets edges as lines
    vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.mixedLineCurve(graph));

    processModels = MultimapBuilder.hashKeys().hashSetValues().build();
  }


  @Getter
  private static ModelView instance = new ModelView();

  /**
   * Enforcing Singleton.
   */
  private ModelView() {
    CompilationObservable.getInstance().addObserver(this);
    initalise();
  }

  //register font
  static {
    Font source;
    try {
      source = Font.createFont(Font.TRUETYPE_FONT,
          ModelView.class.getResourceAsStream("/clientres/SourceCodePro-Bold.ttf"))
          .deriveFont(15f);
    } catch (FontFormatException | IOException e) {
      source = null;
    }
    sourceCodePro = source;
  }


}
