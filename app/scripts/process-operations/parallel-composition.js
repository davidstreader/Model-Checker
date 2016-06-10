'use strict';

function parallelComposition(id, process1, process2){
	// check that processes are of the same type
	if(process1.type !== process2.type){
		// throw error
	}

	var type = process1.type;
	if(type === 'automata'){
		return automataParallelComposition(id, process1, process2);
	}
	else if(type === 'petrinet'){
		return petriNetParallelComposition(id, process1, process2);
	}
	else{
		// throw error
	}

	function automataParallelComposition(id, automaton1, automaton2){
		// TODO
	}

	/**
	 * Performs parallel composition of the two specified petri nets. Constructs
	 * a new petri net with the specified id representing the composition. Returns
	 * the petri net constructed by the composition.
	 *
	 * @param {int} id - the id of the composition
	 * @param {petrinet} net1 - the first petri net
	 * @param {petrinet} net2 - the second petri net
	 * @return {petrinet} - the petri net formed by the composition
	 */
	function petriNetParallelComposition(id, net1, net2){
		// construct place map for the composed net
		var placeMap = {};
		var placeCount = 0;
		var places = net1.places.concat(net2.places);
		for(var i = 0; i < places.length; i++){
			placeMap[places[i].id] = places[i];
			placeCount++;
		}

		// construct transition map and label sets for the composed net
		var transitionMap = {};
		var labelSets = {};
		var transitionCount = 0;

		// add the transitions from the first petri net
		var labelSets1 = net1.labelSets;
		for(var i = 0; i < labelSets1.length; i++){
			var label = labelSets1[i].label;
			var transitions = labelSets1[i].transitions;
			for(var j = 0; j < transitions.length; j++){
				transitionMap[transitions[j].id] = transitions[j];
				transitionCount++;
			}
			labelSets[label] = transitions;
		}

		// add transitions from the second petri net and check for duplicate labels
		var labelSets2 = net2.labelSets;
		for(var i = 0; i < labelSets2.length; i++){
			var label = labelSets2[i].label;
			var transitions = labelSets2[i].transitions;

			// check if this is a duplicate label
			if(labelSets[label] !== undefined){
				// sync actions that occur in both petri nets
				var set = labelSets[label];
				for(j = 0; j < set.length; j++){
					for(var k = 0; k < transitions.length; k++){
						transitionMap[transitions[k].id] = transitions[k];
						transitionCount++;
						syncTransitions(set[j], transitions[k]);
					}
				}

				labelSets[label] = labelSets[label].concat(transitions);
			}
			else{
				// proceed as normal
				for(var j = 0; j < transitions.length; j++){
					transitionMap[transitions[j].id] = transitions[j];
					transitionCount++;
				}

				labelSets[label] = transitions;
			}
		}

		// return composed net
		return new PetriNet(id, placeMap, placeCount, transitionMap, labelSets, transitionCount);

		/**
		 * Helper function for processing the parallel composition of petri nets
		 * which syncs the incoming and outgoing places from the two specified
		 * transitions
		 */
		function syncTransitions(transition1, transition2){
			var places = transition1.outgoingPlaces;
			for(var i = 0; i < places.length; i++){
				transition2.addOutgoingPlace(places[i]);
			}
			places = transition2.outgoingPlaces;
			for(var i = 0; i < places.length; i++){
				transition1.addOutgoingPlace(places[i]);
			}

			places = transition1.incomingPlaces;
			for(var i = 0; i < places.length; i++){
				transition2.addIncomingPlace(places[i]);
			}
			places = transition2.incomingPlaces;
			for(var i = 0; i < places.length; i++){
				transition1.addIncomingPlace(places[i]);
			}
		}
	}
}