package mc.processmodels;

import lombok.Getter;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by smithjord3 on 2/02/18.
 * For mapping the nodes of an automaton to petrinet and visa versa, when they have been converted from one type to another
 */

public class Mapping {
    @Getter
    private Map<AutomatonNode, Set<PetriNetPlace>> nodeToMarking = new HashMap<>();

    @Getter
    private Map<Set<PetriNetPlace>, AutomatonNode > markingToNode = new HashMap<>();

    public Mapping(Map<AutomatonNode, Set<PetriNetPlace>> nodeToMarking_, Map<Set<PetriNetPlace>, AutomatonNode > markingToNode_) {
        nodeToMarking = nodeToMarking_;
        markingToNode = markingToNode_;
    }
}