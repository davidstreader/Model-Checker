package mc.processmodels;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lombok.Getter;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by smithjord3 on 2/02/18.
 * For mapping the nodes of an automaton to petrinet and visa versa, when they have been converted from one type to another
 */

public class MappingNdMarking {
    @Getter
    private Map<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();

    @Getter
    private Map<Multiset<PetriNetPlace>, AutomatonNode > markingToNode = new HashMap<>();

    public static MappingNdMarking reId(MappingNdMarking map, String tag) {
        Map<AutomatonNode, Multiset<PetriNetPlace>> node2Marking = new HashMap<>();
        Map<Multiset<PetriNetPlace>, AutomatonNode > marking2Node = new HashMap<>();
        for (AutomatonNode anode: map.nodeToMarking.keySet()){
            Multiset<PetriNetPlace> mset = HashMultiset.create();
            Iterator<PetriNetPlace> it = map.nodeToMarking.get(anode).iterator();
            while (it.hasNext()) {
                PetriNetPlace pl = it.next();
                pl.setId(pl.getId()+tag);
                mset.add(pl);
            }
            node2Marking.put(anode,mset);
            marking2Node.put(mset,anode);
        }
        MappingNdMarking out = new MappingNdMarking(node2Marking,marking2Node);

        return out;
    }
    public MappingNdMarking(Map<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking_, Map<Multiset<PetriNetPlace>, AutomatonNode > markingToNode_) {
        nodeToMarking = nodeToMarking_;
        markingToNode = markingToNode_;
    }
}