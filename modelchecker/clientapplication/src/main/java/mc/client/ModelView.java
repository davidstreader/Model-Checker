package mc.client;


import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Bounds;
import lombok.Getter;
import lombok.Setter;
import mc.client.graph.AutomataBorderPaintable;
import mc.client.graph.DirectedEdge;
import mc.client.graph.EdgeShape;
import mc.client.graph.GraphNode;
import mc.client.graph.NodeStates;
import mc.client.graph.NodeType;
import mc.client.graph.SeededRandomizedLayout;
import mc.client.graph.SpringlayoutBase;
import mc.client.ui.DoubleClickHandler;
import mc.client.ui.SettingsController;
import mc.compiler.CompilationObject;
import mc.compiler.CompilationObservable;
import mc.compiler.OperationResult;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;

/**
 * Created by bealjaco on 29/11/17.
 */
public class ModelView implements Observer {

  private Graph<GraphNode, DirectedEdge> graph;
  private Layout<GraphNode, DirectedEdge> layout;
  private SeededRandomizedLayout layoutInitalizer;
  private VisualizationViewer<GraphNode, DirectedEdge> vv;

  private Bounds windowSize;

  private DoubleClickHandler massSelect;

  private Set<String> processModelsToDisplay;
  private SortedSet<String> visibleModels; // Processes that are in the modelsList combox
  private Multimap<String, GraphNode> processModels;

  private CompilationObject compiledResult;
  private List<String> processesChanged = new ArrayList<>();


  private static final Font sourceCodePro;

  @Setter
  private SettingsController settings; // Contains linkage length and max nodes


  @Setter
  private Consumer<Collection<String>> listOfAutomataUpdater;
  @Setter
  private BiConsumer<List<OperationResult>, List<OperationResult>> updateLog;

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

    for (Map.Entry<String, MultiProcessModel> mpm : toExpand) {
      for (ProcessType pt : ProcessType.values()) {
        if (mpm.getValue().hasProcess(pt)) {
          String name = mpm.getKey() + " (" + pt.name().toLowerCase() + ")";
          mpm.getValue().getProcess(pt).setId(name);
          compiledResult.getProcessMap().put(name, mpm.getValue().getProcess(pt));
        }
      }
    }

    toExpand.stream().map(Map.Entry::getKey).forEach(compiledResult.getProcessMap()::remove);
    visibleModels = getProcessMap().entrySet().stream()
        .filter(e -> e.getValue().getProcessType() != ProcessType.AUTOMATA ||
            ((Automaton) e.getValue()).getNodes().size() <= settings.getMaxNodes())
        .map(Map.Entry::getKey)
        .collect(Collectors.toCollection(TreeSet::new));

    //remove processes marked at skipped and too large models to display
    listOfAutomataUpdater.accept(visibleModels);

    updateLog.accept(compiledResult.getOperationResults(), compiledResult.getEquationResults());
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
    vv.getRenderContext().setVertexFillPaintTransformer(n -> n.getNodeTermination().getColorNodes());


    //autoscale the graph to fit in the display port
    vv.setPreferredSize(new Dimension((int) windowSize.getWidth(), (int) windowSize.getHeight()));

    //This draws the boxes around the automata
    vv.addPreRenderPaintable(new AutomataBorderPaintable(vv, this.processModels));

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
        nodeTermination = NodeStates.START;
      }
      if (n.isTerminal()) {
        nodeTermination = NodeStates.valueOf(n.getTerminal().toUpperCase());
      }

      GraphNode node = new GraphNode(automaton.getId(), n.getId(), nodeTermination,
          NodeType.AUTOMATA_NODE, "" + n.getLabelNumber());
      nodeMap.put(n.getId(), node);

      graph.addVertex(node);
    });


    //add the edges to the graph

      automaton.getEdges().forEach(e -> {
        GraphNode to = nodeMap.get(e.getTo().getId());
        GraphNode from = nodeMap.get(e.getFrom().getId());
        String label = e.getLabel();
        label = label + " " + ((e.getGuard() != null) ?
            e.getGuard().getGuardStr() + " " + e.getGuard().getNextStr(): "");

        label = label + "owners = [" + e.getOwnerLocation() + "]";

        graph.addEdge(new DirectedEdge(label, UUID.randomUUID().toString()), from, to);
      });




    this.processModels.replaceValues(automaton.getId(), nodeMap.values());
  }

  private void addPetrinet(Petrinet petri) {
    //make a new "parent" object for the children to be parents of
    if (processModels.containsKey(petri.getId())) {
      // If the automaton is already displayed, but modified.
      // Remove all vertexes that are part of it
      for (GraphNode n : processModels.get(petri.getId())) {
        graph.removeVertex(n);
      }

      processModels.removeAll(petri.getId());
    }

    Map<String, GraphNode> nodeMap = new HashMap<>();

    petri.getPlaces().values().forEach(place -> {
      NodeStates nodeTermination = NodeStates.NOMINAL;
      if (place.isTerminal()) {
        nodeTermination = NodeStates.valueOf(place.getTerminal().toUpperCase());
      }

      if (place.isStart()) {
        nodeTermination = NodeStates.START;
      }

      GraphNode node = new GraphNode(petri.getId(), place.getId(),
          nodeTermination, NodeType.PETRINET_PLACE, "");
      nodeMap.put(place.getId(), node);
      graph.addVertex(node);
    });

    petri.getTransitions().values().forEach(transition -> {
      GraphNode node = new GraphNode(petri.getId(), transition.getId(),
          NodeStates.NOMINAL, NodeType.PETRINET_TRANSITION, transition.getLabel());
      nodeMap.put(transition.getId(), node);

    });

    petri.getEdges().values().forEach(edge -> {
      DirectedEdge nodeEdge = new DirectedEdge("", UUID.randomUUID().toString());
      graph.addEdge(nodeEdge, nodeMap.get(edge.getFrom().getId()),
          nodeMap.get(edge.getTo().getId()));

    });

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
   * All functions below deal with "freezing" vertexes, this means to disallow the layout algorithm
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

    //apply a layout to the graph

    layout = new SpringlayoutBase<>(graph, e->settings.getLinkageLength());


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

    massSelect = new DoubleClickHandler(processModels, vv);
    vv.addGraphMouseListener(massSelect);


    //label the nodes
    vv.getRenderContext().setVertexLabelTransformer(GraphNode::getLabel);
    vv.getRenderContext().setEdgeLabelTransformer(DirectedEdge::getLabel);

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
