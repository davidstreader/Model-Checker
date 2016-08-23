'use strict';

function petriNetAbstraction(process, isFair){
	var paths = [];
	var visited = {};

	var fringe = [];
	var roots = process.roots;
	for(var i = 0; i < roots.length; i++){
		fringe.push({ start:undefined, place:roots[i] });
	}

	while(fringe.length !== 0){
		var current = fringe.pop();

		// check if this place has been visited
		if(visited[current.place.id] !== undefined){
			continue;
		}

		// push the next places to the fringe
		var transitions = getTransitions(process, current.place.outgoingTransitions);
		for(var i = 0; i < transitions.length; i++){
			var places = transitions[i].outgoingPlaces;
			// check if the current transition represents a hidden event
			if(transitions[i].label === TAU){
				for(var j = 0; j < places.length; j ++){
					// either continue an existing path or start a new one
					var start = (current.start !== undefined) ? current.start : current.place;
					fringe.push({ start:start, place:places[i] });
				}
			}
			// check if the current path has completed
			else if(transitions[i].label !== TAU && current.start !== undefined){
				for(var j = 0; j < places.length; j++){
					fringe.push({ start:undefined, place:places[i] });
					paths.push({ start:current.start, end:current.place });
				}
			}
			// push the next places to the fringe
			else{
				for(var j = 0; j < places.length; j++){
					fringe.push({ start:undefined, place:places[j] });
				}
			}
		}
	}

	// add observable actions
	for(var i = 0; i < paths.length; i++){
		// add observable actions from transitions that lead to the start place
		var incoming = getTransitions(process, paths[i].start.incomingTransitions);
		for(var j = 0; j < incoming.length; j++){
			var id = process.nextTransitionId;
			process.addTransition(id, incoming[j].label, incoming[j].incomingPlaces, [paths[i].end]);
		}

		// add observable actions from the start place to outgoing transitions from the end place
		var outgoing = getTransitions(process, paths[i].end.outgoingTransitions);
		for(var j = 0; j < outgoing.length; j++){
			var id = process.nextTransitionId;
			process.addTransition(id, outgoing[j].label, [paths[i].start], outgoing[j].outgoingPlaces);
		}
	}

	// remove hidden transitions
	var transitions = process.transitions;
	var places = [];
	for(var i = 0; i < transitions.length; i++){
		if(transitions[i].label === TAU){
			// remove references to this transition
			var incoming = transitions[i].incomingPlaces;
			for(var j = 0; j < incoming.length; j++){
				incoming[j].deleteOutgoingTransitions(transitions[i].id);
				// delete this place if it is now unreachable
				if(incoming[j].isUnreachable){
					places.push(incoing[j].id);
				}
			}

			var outgoing  = transitions[i].outgoingPlaces;
			for(var j = 0; j < outgoing.length; j++){
				outgoing[j].deleteIncomingTransitions(transitions[i].id);
				// make this place a terminal if there are no transitions from it
				if(outgoing[j].isTerminal){
					outgoing[j].addMetaData('isTerminal', 'stop');
				}
			}

			process.removeTransition(transitions[i].id);
		}
	}

	// remove unreachable places from the petri net
	for(var i = 0; i < places.length; i++){
		process.removePlace(places[i]);
	}

	return process;

	function getTransitions(process, ids){
		var transitions = [];
		for(var i = 0; i < ids.length; i++){
			transitions[i] = process.getTransition(ids[i]);
		}

		return transitions;
	}

}