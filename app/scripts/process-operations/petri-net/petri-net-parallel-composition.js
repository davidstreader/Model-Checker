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
		net.addPlace(places[i].id, undefined, undefined, places[i].metaData);
		// check if this place is a start or terminal place
		if(places[i].getMetaData('startPlace') !== undefined){
			net.addRoot(places[i].id);
		}
	}

	var labelSets1 = net1.labelSets;
	var labelSets2 = net2.labelSets;

	for(var action in labelSets1){
		var transitions = labelSets1[action];
		for(var i = 0; i < transitions.length; i++){
			var incoming = transitions[i].incomingPlaces.map(id => net.getPlace(id));
			var outgoing = transitions[i].outgoingPlaces.map(id => net.getPlace(id));
			var locations = transitions[i].locations;

			// check if the actions are synced
			if(labelSets2[action] !== undefined){
				var synced = labelSets2[action];
				for(var j = 0; j < synced.length; j++){
					incoming = incoming.concat(synced[j].incomingPlaces.map(id => net.getPlace(id)));
					outgoing = outgoing.concat(synced[j].outgoingPlaces.map(id => net.getPlace(id)));
					for(var id in synced[j].locations){
						locations[id] = true;
					}
				}


			}

			incoming = processPlaceArray(incoming);
			outgoing = processPlaceArray(outgoing);
			var transition = net.addTransition(transitions[i].id, action, incoming, outgoing);
			transition.locations = locations;
		}
	}

	for(var action in labelSets2){
		if(labelSets1[action] === undefined){
			var transitions = labelSets2[action];
			for(var i = 0; i < transitions.length; i++){
				// update references to incoming and outgoing places
				var incoming = processPlaceArray(transitions[i].incomingPlaces.map(id => net.getPlace(id)));
				var outgoing = processPlaceArray(transitions[i].outgoingPlaces.map(id => net.getPlace(id)));
				var transition = net.addTransition(transitions[i].id, action, incoming, outgoing);
				transition.locations = transitions[i].locations;
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
		var places = transition1.outgoingPlaces.map(id => net.getPlace(id));
		for(var i = 0; i < places.length; i++){
			transition2.addOutgoingPlace(places[i]);
			places[i].addIncomingTransition(transition2.id);
		}
		places = transition2.outgoingPlaces.map(id => net.getPlace(id));
		for(var i = 0; i < places.length; i++){
			transition1.addOutgoingPlace(places[i]);
			places[i].addIncomingTransition(transition1.id);
		}

		places = transition1.incomingPlaces.map(id => net.getPlace(id));
		for(var i = 0; i < places.length; i++){
			transition2.addIncomingPlace(places[i]);
			places[i].addOutgoingTransition(transition2.id);
		}
		places = transition2.incomingPlaces.map(id => net.getPlace(id));
		for(var i = 0; i < places.length; i++){
			transition1.addIncomingPlace(places[i]);
			places[i].addOutgoingTransition(transition1.id);
		}
	}

	function processPlaceArray(places){
		var ids = {};
		for(var i = 0; i < places.length; i++){
			ids[places[i].id] = true;
		}

		var newPlaces = [];
		for(var id in ids){
			newPlaces.push(net.getPlace(id));
		}

		return newPlaces;
	}
}