package mc.processmodels.conversion;


import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by smithjord3 on 23/01/18.
 * Method algorithm for converting an Petrinet to automaton
 */
public class tokenRule {
    static public Automaton tokenRule(Petrinet convertFrom) throws CompilationException{
        Automaton outputAutomaton = new Automaton(convertFrom.getId()+" automata", false);

        Map<Set<PetriNetPlace>, AutomatonNode> nodeMap = new HashMap<>();
        AutomatonNode root = outputAutomaton.addNode();
        root.setStartNode(true);
        outputAutomaton.addRoot(root);

        nodeMap.put(convertFrom.getRoots(), root);

        Stack<Set<PetriNetPlace>> toDo = new Stack<>();
        toDo.push(convertFrom.getRoots());

        Set<PetriNetTransition> previouslyVisitiedTransitions = new HashSet<>();

        while(!toDo.isEmpty()) {
            Set<PetriNetPlace> currentMarking = toDo.pop();

            Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking);
            if(satisfiedPostTransitions.size() == 0)
               nodeMap.get(currentMarking).setTerminal("STOP");


            for(PetriNetTransition transition : satisfiedPostTransitions) {
                if(previouslyVisitiedTransitions.contains(transition))
                    continue;

                Set<PetriNetPlace> newMarking = new HashSet<>(currentMarking);
                newMarking.removeAll(pre(transition)); // Clear out the places in the current marking which are moving token

                newMarking.addAll(transition.getOutgoing().stream()
                          .map(outEdge -> (PetriNetPlace) outEdge.getTo()).collect(Collectors.toList()));


                if(!nodeMap.containsKey(newMarking))
                        toDo.add(newMarking);



                AutomatonNode newNode = outputAutomaton.addNode();
                outputAutomaton.addEdge(transition.getLabel(), nodeMap.get(currentMarking), newNode, null);

                nodeMap.put(newMarking, newNode);

               previouslyVisitiedTransitions.add(transition);
            }

        }



        return outputAutomaton;
    }

    static private Set<PetriNetTransition> satisfiedTransitions(Set<PetriNetPlace> currentPlace) {
        Set<PetriNetTransition> potentialTransitions = post(currentPlace);

        return potentialTransitions.stream().filter(transition -> currentPlace.containsAll(pre(transition))).collect(Collectors.toSet());
    }


    static private Set<PetriNetTransition> post(Set<PetriNetPlace> currentMarking) {
        Set<PetriNetTransition> output = new HashSet<>();


        for(PetriNetPlace place : currentMarking) {
            output.addAll(place.getOutgoing().stream().map(outgoingEdge -> (PetriNetTransition) outgoingEdge.getTo()).collect(Collectors.toList()));
        }

        return output;
    }

    static private Set<PetriNetPlace> pre(PetriNetTransition transtionToGetPrePlaces) {
        return  transtionToGetPrePlaces.getIncoming().stream().map(incomingEdge -> (PetriNetPlace) incomingEdge.getFrom()).collect(Collectors.toSet());
    }


}
/*
while(!toDo.isEmpty()) {
            Set<PetriNetPlace> currentMarking = toDo.pop();

            for(PetriNetPlace firedPlace : currentMarking) {

                for(PetriNetEdge edgeOut : firedPlace.getOutgoing() ) {
                    PetriNetTransition transition = (PetriNetTransition) edgeOut.getTo();

                    boolean createNewMarking = true;
                    for(PetriNetEdge preReq : transition.getIncoming()) { // Check if this transition has all preplaces fired
                        PetriNetPlace prePlace = (PetriNetPlace)preReq.getFrom();

                        if(!currentMarking.contains(prePlace)) {
                            createNewMarking = false;
                            break;
                        }
                    }

                    if(createNewMarking) {
                        Set<PetriNetPlace> newMarking = transition.getOutgoing().stream()
                                                        .map(postPlace -> (PetriNetPlace) postPlace.getTo())
                                                        .collect(Collectors.toSet());

                        Set<PetriNetPlace> unfiredPlaces = new HashSet<>(currentMarking);
                        for(PetriNetEdge prePlace : transition.getIncoming())  {
                            unfiredPlaces.remove((PetriNetPlace) prePlace.getFrom());
                        }

                        unfiredPlaces.remove(firedPlace); // Remove the place we have come from

                        newMarking.addAll(unfiredPlaces);

                        AutomatonNode newNode = outputAutomaton.addNode();
                        nodeMap.put(newMarking, newNode);

                        outputAutomaton.addEdge(transition.getLabel(),nodeMap.get(currentMarking), newNode, null);


                        toDo.push(newMarking);
                    }


                }


            }

        }
 */
