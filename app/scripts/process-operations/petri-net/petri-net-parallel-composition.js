'use strict';

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
	var net = new PetriNet(id);

	// add places to the composed net
	var places = net1.places.concat(net2.places);
	for(var i = 0; i < places.length; i++){
		net.addPlace(places[i].id, places[i].metaData);
		// check if this place is a start or terminal place
		if(places[i].getMetaData('startPlace') !== undefined){
			net.addRoot(places[i].id);
		}
		if(places[i].getMetaData('isTerminal') !== undefined){
			net.addTerminal(places[i].id);
		}
	}

	var labelSets1 = net1.labelSets;
	var labelSets2 = net2.labelSets;

	for(var action in labelSets1){
		var transitions = labelSets1[action];
		for(var i = 0; i < transitions.length; i++){
			var incoming = transitions[i].incomingPlaces;
			var outgoing = transitions[i].outgoingPlaces;
			// check if the actions are synced
			if(labelSets2[action] !== undefined){
				var synced = labelSets2[action];
				for(var j = 0; j < synced.length; j++){
					incoming = incoming.concat(synced[j].incomingPlaces);
					outgoing = outgoing.concat(synced[j].outgoingPlaces);
				}
			}

			// update references to incoming and outgoing places
			for(var j = 0; j < incoming.length; j++){
				incoming[j] = net.getPlace(incoming[j].id);
			}
			for(var j = 0; j < outgoing.length; j++){
				outgoing[j] = net.getPlace(outgoing[j].id);
			}

			net.addTransition(transitions[i].id, action, incoming, outgoing);
		}
	}

	for(var action in labelSets2){
		if(labelSets1[action] === undefined){
			var transitions = labelSets2[action];
			for(var i = 0; i < transitions.length; i++){
				// update references to incoming and outgoing places
				var incoming = transitions[i].incomingPlaces;
				for(var j = 0; j < incoming.length; j++){
					incoming[j] = net.getPlace(incoming[j].id);
				}
				var outgoing = transitions[i].outgoingPlaces;
				for(var j = 0; j < outgoing.length; j++){
					outgoing[j] = net.getPlace(outgoing[j].id);
				}

				net.addTransition(transitions[i].id, action, incoming, outgoing);
			}
		}
	}

	return net;

	/**
	 * Helper function for processing the parallel composition of petri nets
	 * which syncs the incoming and outgoing places from the two specified
	 * transitions
	 */
	function syncTransitions(transition1, transition2){
		var places = transition1.outgoingPlaces;
		for(var i = 0; i < places.length; i++){
			transition2.addOutgoingPlace(places[i]);
			places[i].addIncomingTransition(transition2.id);
		}
		places = transition2.outgoingPlaces;
		for(var i = 0; i < places.length; i++){
			transition1.addOutgoingPlace(places[i]);
			places[i].addIncomingTransition(transition1.id);
		}

		places = transition1.incomingPlaces;
		for(var i = 0; i < places.length; i++){
			transition2.addIncomingPlace(places[i]);
			places[i].addOutgoingTransition(transition2.id);
		}
		places = transition2.incomingPlaces;
		for(var i = 0; i < places.length; i++){
			transition1.addIncomingPlace(places[i]);
			places[i].addOutgoingTransition(transition1.id);
		}
	}
}