package mc.processmodels;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lombok.Getter;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by smithjord3 on 2/02/18.
 * For mapping the nodes of an automaton to petrinet and visa versa, when they have been converted from one type to another
 * We now only use MultiProcessModel that contains both a petriNet and an automata
 * long with this mapping.
 * Because of cloning need to use String id  equality!
 *   TODO some data should refactored away
 */

public class MappingNdMarking {
    @Getter
    private Map<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();

    @Getter
    private Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();

    @Getter
    private Map<Multiset<String>, String> idMarkingToNode = new HashMap<>();
    @Getter
    private Map<String, Multiset<String>> idNodeToMarking = new HashMap<>();

    private Map<String, AutomatonNode> idToAutomatonNode = new HashMap<>();
    private Map<String, PetriNetPlace> idToPetriNetPlace = new HashMap<>();
    private Map<String, Multiset<PetriNetPlace>> idToMarking = new HashMap<>();
    @Getter
    private Map<String,String> PidToAid = new HashMap<>();

    public static MappingNdMarking reId(MappingNdMarking map, String tag, String autId) {
        Map<AutomatonNode, Multiset<PetriNetPlace>> node2Marking = new HashMap<>();
        Map<Multiset<PetriNetPlace>, AutomatonNode> marking2Node = new HashMap<>();
        for (AutomatonNode anode : map.nodeToMarking.keySet()) {
            Multiset<PetriNetPlace> mset = HashMultiset.create();
            Iterator<PetriNetPlace> it = map.nodeToMarking.get(anode).iterator();
            while (it.hasNext()) {
                PetriNetPlace pl = it.next();
                pl.setId(pl.getId() + tag);
                mset.add(pl);
            }
            node2Marking.put(anode, mset);
            marking2Node.put(mset, anode);

        }
        MappingNdMarking out = new MappingNdMarking(node2Marking, marking2Node, tag, autId);

        return out;
    }

    public MappingNdMarking(Map<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking_, Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode_ , String tag, String autId) {
        nodeToMarking = nodeToMarking_;
        markingToNode = markingToNode_;
        buildIdMarkingToNode();
        PidToAid.put(tag,autId);
    }
    private void buildIdMarkingToNode(){
        for (Multiset<PetriNetPlace> mark : markingToNode.keySet()) {
            String nodeId = markingToNode.get(mark).getId();
            Multiset<String> idMark = HashMultiset.create();

            for (Multiset.Entry<PetriNetPlace> placEnt : mark.entrySet()) {
                String pid = placEnt.getElement().getId();
                idMark.add(pid, placEnt.getCount());
                idToPetriNetPlace.put(pid, placEnt.getElement());
            }
            idMarkingToNode.put(idMark,nodeId );
            idNodeToMarking.put(nodeId, idMark );
            idToAutomatonNode.put(nodeId, markingToNode.get(mark));
        }
    }

    /**
     * contains uses the ids as the Places, Nodes might have been cloned
     * @param mark
     * @return
     */
    public boolean contains(Multiset<PetriNetPlace> mark) {
        boolean found = false;
        Multiset<String> idMark = HashMultiset.create();
        for (Multiset.Entry<PetriNetPlace> placEnt : mark.entrySet()) {
            idMark.add(placEnt.getElement().getId(), placEnt.getCount());
        }
        if (idMarkingToNode.containsKey(idMark)) found = true;

        return found;
    }
    public AutomatonNode get(Multiset<PetriNetPlace> mark) {
        PetriNetPlace pl = null;
        Multiset<String> idMark = HashMultiset.create();
        for (Multiset.Entry<PetriNetPlace> placEnt : mark.entrySet()) {
            idMark.add(placEnt.getElement().getId(), placEnt.getCount());
        }
        if (idMarkingToNode.containsKey(idMark)) ;
        String id = idMarkingToNode.get(idMark);
        return idToAutomatonNode.get(id);
    }

    public Multiset<PetriNetPlace> get(AutomatonNode nd) {
        return idToMarking.get(nd.getId());
    }
    /**
     * contains uses the ids as the Places, Nodes might have been cloned
     * @param nd
     * @return
     */
    public boolean contains(AutomatonNode nd) {
        if (idNodeToMarking.containsKey(nd.getId())) return true;
        else return false;
    }

    public String toString() {
        String out =
            nodeToMarking.keySet().stream().map(x -> x.getId() + "-> {'+" +
                nodeToMarking.get(x).stream().map(y -> y.getId()).
                    collect(Collectors.joining(", ")) + "}").
                collect(Collectors.joining("\n   "));
        return out;
    }
    public String  n2m2String() {
        StringBuilder sb = new StringBuilder();
            for (String id :idNodeToMarking.keySet()) {
                sb.append(id+"->"+idNodeToMarking.get(id).toString()+", ");
            }
            sb.append("  pid2aid ");
            for(String s : getPidToAid().keySet()) {
                sb.append(s+"->"+PidToAid.get(s)+", ");
            }
        return sb.toString();
    }
    public static String n2m2String(Map<AutomatonNode, Multiset<PetriNetPlace>> nToMark) {
        String out =
            nToMark.keySet().stream().map(x -> x.getId() + " -> {'+" +
                nToMark.get(x).stream().map(y -> y.getId()).
                    collect(Collectors.joining(", ")) + "}").
                collect(Collectors.joining("\n   "));
        return out;
    }

    public static String m2n2String(Map<Multiset<PetriNetPlace>, AutomatonNode> markToN) {
        String out =
            markToN.keySet().stream().map(x -> "{" + x.stream().map(y -> y.getId()).
                collect(Collectors.joining(", ")) + "} -> " + markToN.get(x).getId() + "\n").
                collect(Collectors.joining(" "));
        return out;
    }

}
