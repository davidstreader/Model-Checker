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
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Bounds;
import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.client.graph.*;
import mc.client.ui.*;
import mc.compiler.CompilationObject;
import mc.compiler.CompilationObservable;
import mc.compiler.OperationResult;
import mc.processmodels.MappingNdMarking;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;

import org.graphstream.graph.*;

/**
 * Created by bealjaco on 29/11/17.
 * Currently called by UseInterfaceController  FMX App
 * dstr  needs to refactored.
 * hateful jung connected here no documentation
 * MUST remove jung! After 2 years still can not find if I can (and how to) change
 * some of the bacis graph features
 * <p>
 * Refractoring work commenced by Lachlan on 1/04/20 for his 489 supervised by David
 * Main intension therin is to replace Jung with new GraphStream framework plus implement new UX features
 */
public class ModelView implements Observer, FontListener {

    private Graph<GraphNode, DirectedEdge> graph;  //Used by graph layout algorithm
    private Layout<GraphNode, DirectedEdge> layout;
    private SeededRandomizedLayout layoutInitalizer;
    private VisualizationViewer<GraphNode, DirectedEdge> vv;
    private Bounds windowSize;
    private CanvasMouseListener canvasML;
    private Keylisten keyl;
    private Set<String> processModelsToDisplay;
    private SortedSet<String> modelsInList; // Processes that are in the modelsList combox
    private Multimap<String, GraphNode> processModelsOnScreen; //process on the screen
    private List<String> processesChanged = new ArrayList<>();
    // Play places token on current Marking
    // from PetriNetPlace find Graph visualisation
    private Map<String, GraphNode> placeId2GraphNode = new TreeMap<>();
    private CompilationObject compiledResult;

    //map from Id to TokenMapping
    private Map<String, MappingNdMarking> mappings = new HashMap<>(); //wont be needed
    private MultiGraph workingCanvasArea; //For GraphStream
    private Viewer workingCanvasAreaViewer;
    private View workingCanvasAreaView;
    private boolean addingAutoNodeStart;
    private boolean addingAutoNodeNeutral;
    private boolean addingAutoNodeEnd;
    private String newProcessNameValue;
    private Node latestNode;
    private Node firstNodeClicked;
    private Node seccondNodeClicked;
    private org.graphstream.ui.layout.Layout workingLayout; //Do proper import when removing jung
    private int nodeCount = 0;
    private ProcessMouseManager PMM;
    private boolean nodeRecentlyPlaced;

    private UserInterfaceController uic;
    private ArrayList<Node> createdNodes = new ArrayList<>();
    private JPanel workingCanvasAreaContainer;
    private ArrayList<Edge> createdEdges = new ArrayList<>();


    public void cleanData() {
        if (!(mappings == null)) mappings.clear();
        if (!(placeId2GraphNode == null)) placeId2GraphNode.clear();
        if (!(processModelsOnScreen == null)) processModelsOnScreen.clear();
        if (!(modelsInList == null)) modelsInList.clear();
        if (!(processModelsToDisplay == null)) processModelsToDisplay.clear();

    }

    private VisualizationServer.Paintable boarder;
    private static Font sourceCodePro;
    private boolean fontListening = false;
    @Setter
    private SettingsController settings; // Contains linkage length and max nodes

    //Consumer is a Function with void Return type set by the UserInterfaceController
    @Setter
    private Consumer<Collection<String>> listOfAutomataUpdater;
    @Setter
    private BiConsumer<List<OperationResult>, List<OperationResult>> updateLog;
    //@Setter
    //private BiConsumer<List<ImpliesResult>, List<ImpliesResult>> updateImpLog;


    @Override
    public void changeFontSize() {
        float fs = settings.getFont();
        ModelView.sourceCodePro = ModelView.sourceCodePro.deriveFont(fs);

    }

    public ProcessModel getProcess(String id) {
        return compiledResult.getProcessMap().get(id);
    }

    public void removeProcess(String id) {
        compiledResult.getProcessMap().remove(id);
    }

    public void setReferenceToUIC(UserInterfaceController userInterfaceController) {
        uic = userInterfaceController;


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


        //Extracts set of ProcessModel maps and converts into set of Multiprocess maps called to expand
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

//  printing (automata) and (petrinet) names and iterates through toExpand to extract node and edge data into mapping and compiledresult ?
        mappings.clear();
        for (Map.Entry<String, MultiProcessModel> mpm : toExpand) {
            /*System.out.println("mpmKey "+ mpm.getKey());
            System.out.println("mpmValue "+ mpm.getValue());*/
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


        //Removes the original process map leaving the expanded node and edge data from before
        toExpand.stream().map(Map.Entry::getKey).forEach(compiledResult.getProcessMap()::remove);


        String dispType = settings.getDisplayType();

        //Stores process models in modelsInList depending on wether current display setting wants it

        if (dispType.equals("All")) {
            modelsInList = getProcessMap().entrySet().stream()
                .filter(e -> e.getValue().getProcessType() != ProcessType.AUTOMATA ||
                    ((Automaton) e.getValue()).getNodes().size() <= settings.getMaxNodes())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
        } else if (dispType.equals(Constant.AUTOMATA)) {
            modelsInList = getProcessMap().entrySet().stream()
                .filter(e -> e.getValue().getProcessType() == ProcessType.AUTOMATA &&
                    ((Automaton) e.getValue()).getNodes().size() <= settings.getMaxNodes())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
        } else {
            modelsInList = getProcessMap().entrySet().stream()
                .filter(e -> e.getValue().getProcessType() != ProcessType.AUTOMATA)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
        }
        //remove processes marked at skipped and too large models to display
        listOfAutomataUpdater.accept(modelsInList);


    }

    /* removes all backgrounds */
    public VisualizationViewer<GraphNode, DirectedEdge> removeBorder(SwingNode s) {
        if (boarder != null) { // must remove old background
            vv.removePreRenderPaintable(boarder);

        }
        return vv;
    }


    public JPanel updateGraphNew(SwingNode modelDisplayNew) {
        System.out.println("updategraph");

        if (compiledResult == null) {
            return workingCanvasAreaContainer;
        }

        //workingCanvasArea.clear();
        workingCanvasArea.addAttribute("ui.stylesheet", getStyleSheet());
        workingCanvasArea.addAttribute("ui.quality");
        workingCanvasArea.addAttribute("ui.antialias");

        //workingCanvasAreaViewer.enableAutoLayout(workingLayout);


        //Do Drawing Work On Canvas
        compiledResult.getProcessMap().keySet().stream()
            .filter(processModelsToDisplay::contains)
            .filter(processesChanged::contains)
            .map(compiledResult.getProcessMap()::get)
            .filter(Objects::nonNull)
            .forEach(this::addProcessNew);

        drawCreatedProcesses();

        processesChanged.clear();

        //Return the Now updated canvas to UIC
        return workingCanvasAreaContainer;
    }

    private void drawCreatedProcesses() {
        for (Node cn : createdNodes) {
            //System.out.println(cn.getId());

            if (workingCanvasArea.getNode(cn.getId()) != null) {
                continue;
            }

            Node n = workingCanvasArea.addNode(cn.getId());
            String cnAttributeAutoType = cn.getAttribute("ui.class");
            n.addAttribute("ui.class", cnAttributeAutoType); // dk ytf cant do this directly

            String cnAttributeLabel = cn.getAttribute("ui.label");

            if (cnAttributeLabel != null) { //Is start so label it
                n.addAttribute("ui.label", cnAttributeLabel); // dk ytf cant do this directly
            }

            //System.out.println("edgecount: " + cn.getEdgeSet().size());

        }

        for (Edge ce : createdEdges) {

            if (workingCanvasArea.getEdge(ce.getId()) != null) {
                continue;
            }

            Edge e = workingCanvasArea.addEdge("test" + Math.random(), (Node) ce.getNode0(), (Node) ce.getNode1(), true);
            String cnEAttributeLabel = ce.getAttribute("ui.label");
            e.addAttribute("ui.label", cnEAttributeLabel);
        }
    }

    private String getStyleSheet() {
        return "node {" +
            "text-size: 20;" +
            "size: 30px; " +
            "fill-color: green;" +
            "}" +
            "node.AutoStart {" +
            "fill-color: green;" +
            "}" +
            "node.AutoNeutral {" +
            "fill-color: gray;" +
            "}" +
            "node.AutoEnd {" +
            "fill-color: red;" +
            "}" +
            "edge {" +
            " " +
            "text-size: 20;" +
            "arrow-shape: arrow;" +
            "}" +
            "graph {" +
            "fill-color: white;" +
            "}" +
            "node.PetriPlace {" +
            "fill-color: gray;" +
            "}" +
            "node.PetriPlaceStart {" +
            "fill-color: green;" +
            "}" +
            "node.PetriPlaceEnd {" +
            "fill-color: red;" +
            "}" +
            "node.PetriTransition {" +
            "shape: box; " +
            "fill-color: gray;" +
            "}"


            ;

    }

    private void addProcessNew(ProcessModel p) {
        switch (p.getProcessType()) {
            case AUTOMATA:
                addAutomataNew((Automaton) p);
                break;
            case PETRINET:
                addPetrinetNew((Petrinet) p);
                break;
        }
    }


    private void addAutomataNew(Automaton automaton) {

        //System.out.println(automaton.getId());

        if (workingCanvasArea.getNode(automaton.getId()) != null) {
            return;
        }


        Map<String, GraphNode> nodeMap = new HashMap<>();

        //System.out.println(automaton);

        //Adds grapth node to display
        automaton.getNodes().forEach(n -> {
            NodeStates nodeTermination = NodeStates.NOMINAL;

            if (n.isStartNode()) {
                GraphNode node = new GraphNode(automaton.getId(), automaton.getId(), nodeTermination, nodeTermination,
                    NodeType.AUTOMATA_NODE, "" + n.getLabelNumber(), n);
                nodeMap.put(n.getId(), node);
            } else {
                GraphNode node = new GraphNode(automaton.getId(), n.getId(), nodeTermination, nodeTermination,
                    NodeType.AUTOMATA_NODE, "" + n.getLabelNumber(), n);
                nodeMap.put(n.getId(), node);
            }

            Node cn;

            if (n.isStartNode()) {
                cn = workingCanvasArea.addNode(automaton.getId());
            } else {
                cn = workingCanvasArea.addNode(n.getId());
            }

            if (n.isStartNode()) {
                cn.addAttribute("ui.label", automaton.getId());
                cn.addAttribute("ui.class", "AutoStart");
            } else if (!n.isStartNode() && !n.isSTOP()) {
                cn.addAttribute("ui.class", "AutoNeutral");
            } else {
                cn.addAttribute("ui.class", "AutoEnd");
            }


        });

        //Connects the node via edges on screen
        automaton.getEdges().forEach(e -> {
            GraphNode to = nodeMap.get(e.getTo().getId());

            GraphNode from = nodeMap.get(e.getFrom().getId());
            String label = e.getLabel();
            String bool;
            String ass;
            if (e.getGuard() != null) {
                bool = e.getGuard().getGuardStr();
                ass = e.getGuard().getAssStr();
            } else {
                bool = "";
                ass = "";
            }
            if (settings.isShowOwners()) {
                label += e.getEdgeOwners();
            }


            Edge edge = workingCanvasArea.addEdge("test" + Math.random(), from.getNodeId(), to.getNodeId(), true);
            edge.addAttribute("ui.label", label);

        });

        //DK if need this yet:
        this.processModelsOnScreen.replaceValues(automaton.getId(), nodeMap.values());


    }

    public void setVisualAutomataNode(String nodeType) {
        if (nodeType.equals("AutoStart")) {
            addingAutoNodeStart = true;
        } else if (nodeType.equals("AutoNeutral")) {
            addingAutoNodeNeutral = true;
        } else {
            addingAutoNodeEnd = true;
        }

        //Not proud of this hack to force graph mouse listener to respond to mouse release from shape mouse listener:
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }


    public void dropNode(int xOnScreen, int yOnScreen) {

        if (!addingAutoNodeStart && !addingAutoNodeNeutral && !addingAutoNodeEnd) {
            return;
        }


        Point3 gu = workingCanvasAreaView.getCamera().transformPxToGu(xOnScreen, yOnScreen);
        //workingCanvasAreaViewer.disableAutoLayout();
        latestNode = workingCanvasArea.addNode(String.valueOf(Math.random()));
        latestNode.setAttribute("xyz", gu.x, gu.y, 0);
        //workingLayout.freezeNode(latestNode.getId(), true);


        if (addingAutoNodeStart) {
            latestNode.addAttribute("ui.class", "AutoStart");
            addingAutoNodeStart = false;
        } else if (addingAutoNodeNeutral) {
            latestNode.addAttribute("ui.class", "AutoNeutral");
            addingAutoNodeNeutral = false;
        } else if (addingAutoNodeEnd) {
            latestNode.addAttribute("ui.class", "AutoEnd");
            addingAutoNodeEnd = false;
        } else {
            System.out.println("doing nothing");
        }

        System.out.println("node placed");

        createdNodes.add(latestNode);

        nodeRecentlyPlaced = true;


    }

    public void setLatestNodeName(String newProcessNameValue) {
        latestNode.addAttribute("ui.label", newProcessNameValue);
    }


    public void determineIfNodeClicked(int x, int y) {

        //To handle the extra redundant "click" from the bot prevents unwanted node linking kinda shit implementation though
        if (nodeRecentlyPlaced) {
            nodeRecentlyPlaced = false;
            return;
        }

        GraphicElement ge = workingCanvasAreaView.findNodeOrSpriteAt(x, y);

        if (ge != null) {
            if (firstNodeClicked == null) {
                firstNodeClicked = (Node) ge;
                System.out.println("Selecting First Node: " + firstNodeClicked.getId());
            } else {
                seccondNodeClicked = (Node) ge;
                System.out.println("Selecting Seccond Node: " + seccondNodeClicked.getId());
            }

        } else {
            System.out.println("Node not Clicked");
        }

        if (firstNodeClicked != null && seccondNodeClicked != null) {
            doDrawEdge();
            firstNodeClicked = null;
            seccondNodeClicked = null;

        }

    }

    private void doDrawEdge() {
        Edge edge = workingCanvasArea.addEdge("test" + Math.random(), firstNodeClicked.getId(), seccondNodeClicked.getId(), true);
        //String labelValue;
        Platform.runLater(() -> {
            String labelValue = uic.nameEdge();
            edge.addAttribute("ui.label", labelValue);
        });

        createdEdges.add(edge);
    }

    private void addPetrinetNew(Petrinet petri) {

        if (workingCanvasArea.getNode(petri.getId()) != null) {
            return;
        }

        Set<PetriNetPlace> petriStarts = petri.getAllRoots();
        int petriStartsSize = petriStarts.size();
        AtomicInteger petriStartSizeTracker = new AtomicInteger();
        petriStartSizeTracker.getAndIncrement();
        Map<PetriNetPlace, Integer> startToIntValue = new HashMap<>();


        Map<String, GraphNode> nodeMap = new HashMap<>();

        Multiset<PetriNetPlace> rts = HashMultiset.create(); // .create(rts);
        petri.getPlaces().values().forEach(place -> {
            NodeStates nodeTermination = NodeStates.NOMINAL;
            if (place.isTerminal()) {
                nodeTermination = NodeStates.valueOf(place.getTerminal().toUpperCase());
            }

            String lab = "";
            if (settings.isShowIds()) lab = place.getId();
            // changing the label on the nodes forces the Petri Net to be relayed out.
            GraphNode node = new GraphNode(petri.getId(), place.getId(),
                nodeTermination, nodeTermination, NodeType.PETRINET_PLACE, lab, place);
            placeId2GraphNode.put(place.getId(), node);

            Node n;

            if(place.isStart()) {
                n = workingCanvasArea.addNode(petri.getId() + (petriStartsSize + 1 - petriStartSizeTracker.get()));
                startToIntValue.put(place, (petriStartsSize + 1 - petriStartSizeTracker.get()));
                petriStartSizeTracker.getAndIncrement();
            } else {
                n = workingCanvasArea.addNode(place.getId());
            }

            if(place.isStart()){
                n.addAttribute("ui.label", petri.getId() + startToIntValue.get(place));
                n.addAttribute("ui.class", "PetriPlaceStart");
            } else if(!place.isStart() && !place.isSTOP()){
                n.addAttribute("ui.class", "PetriPlace");
            } else {
                n.addAttribute("ui.class", "PetriPlaceEnd");
            }


            nodeMap.put(place.getId(), node);
        });


        petri.getTransitions().values().stream().filter(x -> !x.isBlocked())
            .forEach(transition -> {
                String lab = "";
                if (settings.isShowIds()) lab += transition.getId() + "-";
                lab += transition.getLabel() + "";

                GraphNode node = new GraphNode(petri.getId(), transition.getId(),
                    NodeStates.NOMINAL, NodeStates.NOMINAL, NodeType.PETRINET_TRANSITION, lab, transition);
                nodeMap.put(transition.getId(), node);
                Node n = workingCanvasArea.addNode(transition.getId());
                n.addAttribute("ui.class", "PetriTransition");
                n.addAttribute("ui.label", lab);
            });

        for (PetriNetEdge edge : petri.getEdgesNotBlocked().values()) {

            String lab = "";
            if (settings.isShowIds()) lab += edge.getId() + "-";
            if (edge.getOptional()) {

                lab = "Opt";
                int i = edge.getOptionNum();
                if (i > 0) {
                    lab = lab + i;
                }
            }
            if (settings.isShowOwners()) {
                PetriNetPlace place;
                if (edge.getTo() instanceof PetriNetPlace) {
                    place = (PetriNetPlace) edge.getTo();
                } else {
                    place = (PetriNetPlace) edge.getFrom();
                }
                for (String o : (place).getOwners()) {
                    lab += ("." + o);
                }
            }

            String b;
            String a;
            if (edge.getGuard() != null) {
                b = edge.getGuard().getGuardStr();
                a = edge.getGuard().getAssStr();
            } else {
                b = "";
                a = "";
            }

            DirectedEdge nodeEdge = new DirectedEdge(b, lab, a, UUID.randomUUID().toString());

            if(edge.getFrom().getType().equals("PetriNetPlace")){
                PetriNetPlace pnp = (PetriNetPlace) edge.getFrom();
                if(pnp.isStart()){
                    int startValue = startToIntValue.get(pnp);
                    Edge e = workingCanvasArea.addEdge("test" + Math.random(), petri.getId() + startValue, edge.getTo().getId(), true);
                } else {
                    Edge e = workingCanvasArea.addEdge("test" + Math.random(), edge.getFrom().getId(), edge.getTo().getId(), true);
                }
            } else {
                PetriNetPlace pnp = (PetriNetPlace) edge.getTo();
                if(pnp.isStart()){
                    int startValue = startToIntValue.get(pnp);
                    Edge e = workingCanvasArea.addEdge("test" + Math.random(), edge.getFrom().getId(), petri.getId() + startValue, true);
                } else {
                    Edge e = workingCanvasArea.addEdge("test" + Math.random(), edge.getFrom().getId(), edge.getTo().getId(), true);
                }
            }

        }

        this.processModelsOnScreen.replaceValues(petri.getId(), nodeMap.values());
    }


    /**
     * A method to update the graph that is displayed
     *
     * @return the graph component that is displayed
     */

    public VisualizationViewer<GraphNode, DirectedEdge> updateGraph(SwingNode s) {
        //Called from UIC: initialise, addselected, addall, cleargraph
        //Nothing to display
        System.out.println("updategraphold");
        if (compiledResult == null) {
            return new VisualizationViewer<>(new DAGLayout<>(new DirectedSparseGraph<>()));
        }

        //Not needed:
        layoutInitalizer.setDimensions(new Dimension((int) s.getBoundsInParent().getWidth(),
            (int) s.getBoundsInParent().getHeight()));

        //From compiled result filter for processModelstoDisplay and processeschanged
        //Then maps these keys for their values and for values that arnt null are sent to addProcess method
        //Copied into new working fine

        System.out.println("Old");
        System.out.println("Start pmd ");
        for (String q : processModelsToDisplay) {
            System.out.println(q);
        }
        System.out.println("end pmd ");
        System.out.println("Start pc ");

        for (String q : processesChanged) {
            System.out.println(q);
        }
        System.out.println("end pc ");

        compiledResult.getProcessMap().keySet().stream()
            .filter(processModelsToDisplay::contains)
            .filter(processesChanged::contains)
            .map(compiledResult.getProcessMap()::get)
            .filter(Objects::nonNull)
            .forEach(this::addProcess);

        //Not needed yet
        canvasML.updateProcessModelList(processModelsOnScreen);

        //Not Needed
        if (windowSize == null || !windowSize.equals(s.getBoundsInParent())) {
            windowSize = s.getBoundsInParent();
            layout.setSize(new Dimension((int) windowSize.getWidth(), (int) windowSize.getHeight()));
        }

        //Def not needed
        // if the font was imported successfully, set the font
        // (the standard font does not display greek symbols)
        // (i.e. tau and delta events)
        if (sourceCodePro != null) {
            vv.getRenderContext().setEdgeFontTransformer(e -> sourceCodePro);
            vv.getRenderContext().setVertexFontTransformer(e -> sourceCodePro);

        }

        //set the colour of the nodes
        vv.getRenderContext().setVertexFillPaintTransformer(n -> n.getNodeColor().getColorNodes());

        //Not needed:
        //autoscale the graph to fit in the display port
        vv.setPreferredSize(new Dimension((int) windowSize.getWidth(), (int) windowSize.getHeight()));

        //Not needed:
        if (boarder != null) { // must remove old background
            vv.removePreRenderPaintable(boarder);
        }
        /* looks like one paintable added for all graphs But vv has a list of preRender paintables
         *  So could add a one for each automata and then delete one at a time
         * not needed:
         *  */
        boarder = new AutomataBorderPaintable(vv, this.processModelsOnScreen, compiledResult);
        //This draws the boxes around the automata in the compiledResult
        vv.addPreRenderPaintable(boarder);
        vv.addPostRenderPaintable(new PetriMarkingPaintable(vv, this.processModelsOnScreen));
        processesChanged.clear();
        if (!fontListening) {
            settings.addFontListener(this);
            fontListening = true;
        }
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



        /*System.out.println("ModelView addAutomata");
        System.out.println(automaton.toString());*/
        //make a new "parent" object for the children to be parents of
        if (processModelsOnScreen.containsKey(automaton.getId())) {
            // If the automaton is already displayed, but modified.
            // Remove all vertexes that are part of it
            for (GraphNode n : processModelsOnScreen.get(automaton.getId())) {
                graph.removeVertex(n);
            }
            processModelsOnScreen.removeAll(automaton.getId());
        }

        Map<String, GraphNode> nodeMap = new HashMap<>();

        //add all the nodes to the graph


        /*Creates graph nodes objects from automaton object with evaulated termination type and stores them in nodeMap
        Unsure how rootList works, although rootlist size is determined by automaton being instantiated with "construct-
        root bool*/

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


            GraphNode node = new GraphNode(automaton.getId(), n.getId(), nodeTermination, nodeTermination,
                NodeType.AUTOMATA_NODE, "" + n.getLabelNumber(), n);
            nodeMap.put(n.getId(), node);

            //Adds grapth node to display
            graph.addVertex(node);
        });

        //Connects the node via edges on screen
        automaton.getEdges().forEach(e -> {
            GraphNode to = nodeMap.get(e.getTo().getId());
            GraphNode from = nodeMap.get(e.getFrom().getId());
            String label = e.getLabel();
            String bool;
            String ass;
            if (e.getGuard() != null) {
                bool = e.getGuard().getGuardStr();
                ass = e.getGuard().getAssStr();
            } else {
                bool = "";
                ass = "";
            }
            if (settings.isShowOwners()) {
                label += e.getEdgeOwners();
            }
            if (settings.isShowOptional()) {
                if (e.getMarkedOwners() != null && e.getMarkedOwners().size() > 0 &&
                    !e.getMarkedOwners().equals(e.getEdgeOwners())) bool += (" mk" + e.getMarkedOwners());

            }

            graph.addEdge(new DirectedEdge(bool, label + "", ass, UUID.randomUUID().toString()), from, to);
        });

        this.processModelsOnScreen.replaceValues(automaton.getId(), nodeMap.values());
    }


    /**
     * Adding a PetriNet to the observed Graph
     *
     * @param petri
     */
    private void addPetrinet(Petrinet petri) {

        //Method seems similiar in approach to "addAutomata"

        //make a new "parent" object for the children to be parents of
        if (processModelsOnScreen.containsKey(petri.getId())) {
            // If the automaton is already displayed, but modified.
            // Remove all vertexes that are part of it
            for (GraphNode n : processModelsOnScreen.get(petri.getId())) {
                placeId2GraphNode.remove(n.getNodeId()); //not sure what this dose
                graph.removeVertex(n);
            }

            processModelsOnScreen.removeAll(petri.getId());
        }

        Map<String, GraphNode> nodeMap = new HashMap<>();

        Multiset<PetriNetPlace> rts = HashMultiset.create(); // .create(rts);
        petri.getPlaces().values().forEach(place -> {
            NodeStates nodeTermination = NodeStates.NOMINAL;
            if (place.isTerminal()) {
                nodeTermination = NodeStates.valueOf(place.getTerminal().toUpperCase());
            }
            if (place.isStart()) {
                rts.add(place);
                //System.out.println("Root "+ place.getId());
                if (!place.isSTOP()) {

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
                if (place.getMaxEndNo() == 2)
                    nodeTermination = NodeStates.STOP1;
                else if (place.getMaxEndNo() == 3)
                    nodeTermination = NodeStates.STOP2;
                else if (place.getMaxEndNo() == 4)
                    nodeTermination = NodeStates.STOP3;
                else if (place.getMaxEndNo() == 5)
                    nodeTermination = NodeStates.STOP4;
                else if (place.getMaxEndNo() == 6)
                    nodeTermination = NodeStates.STOP5;
            } else if (place.isERROR()) {
                nodeTermination = NodeStates.ERROR;
            }

            //System.out.println("Owners setting "+ settings.isShowOwners());
            String lab = "";
            if (settings.isShowIds()) lab = place.getId();
            // changing the label on the nodes forces the Petri Net to be relayed out.
            GraphNode node = new GraphNode(petri.getId(), place.getId(),
                nodeTermination, nodeTermination, NodeType.PETRINET_PLACE, lab, place);
            placeId2GraphNode.put(place.getId(), node);
            graph.addVertex(node);
            nodeMap.put(place.getId(), node);
        });
        CurrentMarkingsSeen.
            currentMarkingsSeen.put(petri.getId(), rts);
        CurrentMarkingsSeen.addRootMarking(petri.getId(), rts);

        petri.getTransitions().values().stream().filter(x -> !x.isBlocked())
            .forEach(transition -> {
                String lab = "";
                if (settings.isShowIds()) lab += transition.getId() + "-";
                lab += transition.getLabel() + "";
    /*  if (settings.isShowOwners()) {
        for(String o:transition.getOwners()){lab+=o;}
      } else {
        lab = transition.getLabel();
      }

     */
                GraphNode node = new GraphNode(petri.getId(), transition.getId(),
                    NodeStates.NOMINAL, NodeStates.NOMINAL, NodeType.PETRINET_TRANSITION, lab, transition);
                nodeMap.put(transition.getId(), node);
                graph.addVertex(node);  //dstr seem to be forgotten ?
            });


        float dash[] = {10.0f};
        for (PetriNetEdge edge : petri.getEdgesNotBlocked().values()) {
            //System.out.println(edge.myString());
            // dstr commented out below 13/7/19
            //   vv.getRenderContext().setEdgeStrokeTransformer(e -> new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
            //           BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            // EdgeType et = EdgeType.DIRECTED;
            String lab = "";
            if (settings.isShowIds()) lab += edge.getId() + "-";
            if (edge.getOptional()) {

                // et = EdgeType.UNDIRECTED;//NICE try but fails
                lab = "Opt";
                int i = edge.getOptionNum();
                if (i > 0) {
                    lab = lab + i;
                }
            }
            if (settings.isShowOwners()) {
                PetriNetPlace place;
                if (edge.getTo() instanceof PetriNetPlace) {
                    place = (PetriNetPlace) edge.getTo();
                } else {
                    place = (PetriNetPlace) edge.getFrom();
                }
                //System.out.println("Owners added");
                for (String o : (place).getOwners()) {
                    lab += ("." + o);
                }
            }

            String b;
            String a;
            if (edge.getGuard() != null) {
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

        this.processModelsOnScreen.replaceValues(petri.getId(), nodeMap.values());

    }

    /**
     * @param modelLabel The name of the model process to be displayed / added to display.
     */
    public void addDisplayedModel(String modelLabel) {
        //Called when model added to display, Previous methods implicitly invoked due to observering, and ie update
        //Graph will use processchanged/modelstodisplay in filtering through compilation object
        assert compiledResult.getProcessMap().containsKey(modelLabel);
        assert modelsInList.contains(modelLabel);

        processesChanged.add(modelLabel);

        //if(!processModelsToDisplay.contains(modelLabel)) {
        processModelsToDisplay.add(modelLabel);
        //}
        //  canvasML. refreshtransitionColor();
    }

    /* Guesing  dstr */
    public void removeDisplayedModel(String modelLabel) {
        assert compiledResult.getProcessMap().containsKey(modelLabel);
        assert modelsInList.contains(modelLabel);

        processesChanged.add(modelLabel);
        processModelsToDisplay.remove(modelLabel);
        //  canvasML. refreshtransitionColor();
    }

    public void clearDisplayed() {
        processModelsToDisplay.clear();
        initalise();
    }

    public void addAllModels() {
        processModelsToDisplay.clear();
        processesChanged.addAll(compiledResult.getProcessMap().keySet());

        if (modelsInList != null) {
            processModelsToDisplay.addAll(modelsInList);
        }
        // canvasML. refreshtransitionColor();
    }

    /**
     * All functions below deal with "freezing" vertexes, to disallow the layout algorithm
     * to act upon the vertexes.
     */
    public void freezeAllCurrentlyDisplayed() {
        if (layout != null) {
            for (String processModeName : processModelsOnScreen.keySet()) {
                for (GraphNode vertexToLock : processModelsOnScreen.get(processModeName)) {
                    layout.lock(vertexToLock, true);

                }
            }
        }
    }

    public void unfreezeAllCurrentlyDisplayed() {
        if (layout != null) {
            for (String processModeName : processModelsOnScreen.keySet()) {
                for (GraphNode vertexToLock : processModelsOnScreen.get(processModeName)) {
                    layout.lock(vertexToLock, false);

                }
            }
        }
    }

    public void freezeProcessModel(String automataLabel) {

        if (layout != null && automataLabel != null && processModelsToDisplay.contains(automataLabel)) {

            for (GraphNode vertexToLock : processModelsOnScreen.get(automataLabel)) {
                layout.lock(vertexToLock, true);
            }
        }
    }

    public void removeProcessModel(String automataLabel) {
// fails to remove boarder Not sure wher this is
        if (layout != null && automataLabel != null && processModelsToDisplay.contains(automataLabel)) {
            processModelsToDisplay.remove(automataLabel);
            processesChanged.remove(automataLabel);
            modelsInList.remove(automataLabel);
            for (GraphNode vertex : processModelsOnScreen.get(automataLabel)) {
                graph.removeVertex(vertex);
            }
            processModelsOnScreen.removeAll(automataLabel);  // hope to remove the background
            // boarder.
        }
    }

    public void unfreezeProcessModel(String automataLabel) {
        if (layout != null && automataLabel != null && processModelsToDisplay.contains(automataLabel)) {
            for (GraphNode vertexToLock : processModelsOnScreen.get(automataLabel)) {
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
            x -> settings.isShowColor()

        );

        //  settings.addFontListener(this);
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
        gm.add(new MyPickingGraphMousePlugin<>());

        vv.setGraphMouse(gm);


        Boolean keyX = false;
        keyl = new Keylisten(keyX);
        vv.addKeyListener(keyl);
        canvasML = new CanvasMouseListener(processModelsOnScreen, vv, mappings, keyX);
        // cml = new CanvasMouseMotionListener(vv);
        vv.addMouseListener(canvasML);

        // vv.addMouseMotionListener(cml);
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
        // vv.getRenderContext().getEdgeLabelTransformer().;
        processModelsOnScreen = MultimapBuilder.hashKeys().hashSetValues().build();
        //  settings.addFontListener(this); can NOY be done here ?!?

        //Graphstream:

        //Reinitialise The Working Canvas area
        workingCanvasAreaContainer = new JPanel();
        workingCanvasAreaContainer.setLayout(new BorderLayout());
        workingCanvasArea = new MultiGraph("WorkingCanvasArea"); //field
        workingCanvasArea.addAttribute("ui.stylesheet", getStyleSheet());
        workingCanvasArea.addAttribute("ui.quality");
        workingCanvasArea.addAttribute("ui.antialias");

        workingCanvasAreaViewer = new Viewer(workingCanvasArea, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

        workingLayout = Layouts.newLayoutAlgorithm();
        workingCanvasAreaViewer.enableAutoLayout(workingLayout);
        workingCanvasAreaView = workingCanvasAreaViewer.addDefaultView(false);
        PMM = new ProcessMouseManager();
        workingCanvasAreaView.addMouseListener(PMM);
        workingCanvasAreaView.getCamera().setViewPercent(2);
        workingCanvasAreaView.getCamera().setAutoFitView(true);
        workingCanvasAreaContainer.add((Component) workingCanvasAreaView, BorderLayout.CENTER);


    }

    /*
         This seems to work - methods are not static and the constructor is private
         To call foo() you have to call ModelView.getInstance.foo()
     */
    @Getter
    private static ModelView instance = new ModelView();

    /**
     * Enforcing Singleton.
     */
    private ModelView() {
        CompilationObservable.getInstance().addObserver(this);

        initalise();
        // settings.addFontListener(this);  can NOT be done here
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
        // System.out.println("New Font size "+source.getSize());

        sourceCodePro = source;
    }


}
