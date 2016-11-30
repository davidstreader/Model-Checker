'use strict';

/**
 * Performs parallel composition of the two specified Petri nets. Constructs
 * a new Petri net with the specified id representing the composition. Returns
 * the petri net constructed by the composition.
 *
 * @param {string} id - the id of the composition
 * @param {PetriNet} net1 - the first petri net
 * @param {PetriNet} net2 - the second petri net
 * @return {PetriNet} - the petri net formed by the composition
 */
function petriNetParallelComposition(id, net1, net2){
	const net = new PetriNet(id);

	// construct a disjoint union of the places in the two petri nets
	const places = net1.places.concat(net2.places);
	for(let i = 0; i < places.length; i++){
		const place = net.addPlace(places[i].id, places[i].metaData);
		place.locations = places[i].locations;

		// check if this is a start place
		if(places[i].metaData.startPlace !== undefined){
			net.addRoot(places[i].id);
		}
	}

	const alphabet1 = net1.alphabet;
	const alphabet2 = net2.alphabet;

	// construct sets of synchronised and non-synchronised actions
	const synced = {};
	const nonSynced = {};
	const actions = Object.keys(alphabet1).concat(Object.keys(alphabet2));
	for(let i = 0; i < actions.length; i++){
		const action = actions[i];
		// check if the action is either hidden or a deadlock
		if(action === TAU || action === DELTA){
			nonSynced[action] = true;
		}
		// check if both proecsses perfrom the action
		else if(alphabet1[action] !== undefined && alphabet2[action] !== undefined){
			synced[action] = true;
		}
		else{
			nonSynced[action] = true;
		}
	}

	const labelSets1 = net1.labelSets;
	const labelSets2 = net2.labelSets;

	// add the non synchronised transitions
	for(let action in nonSynced){
		let transitions = [];
		// attempts to add from both in case the action is hidden or deadlocked 
		if(labelSets1[action] !== undefined){
			transitions = transitions.concat(labelSets1[action]);
		}
		if(labelSets2[action] !== undefined){
			transitions = transitions.concat(labelSets2[action]);
		}

		// add the non synchronised transitions to the petri net
		for(let i = 0; i < transitions.length; i++){
			// construct and add the transition
			const id = transitions[i].id;
			const incoming = transitions[i].incomingPlaces.map(id => net.getPlace(id));
			const outgoing = transitions[i].outgoingPlaces.map(id => net.getPlace(id));
			const transition = net.addTransition(id, action, incoming, outgoing);
			transition.locations = transitions[i].locations;
		}
	}

	// add the synchronised transitions to the petri net
	for(let action in synced){
		const transitions1 = labelSets1[action];
		const transitions2 = labelSets2[action];

		// sync all transitions in the first net with all the transitions
		// in the second net that represent the same action
		for(let i = 0; i < transitions1.length; i++){
			const transition1 = transitions1[i];
			
			for(let j = 0; j < transitions2.length; j++){
				const transition2 = transitions2[j];

				// construct and add the synced transition
				const id = transition1.id + '||' + transition2.id;
				const incoming = transition1.incomingPlaces.concat(transition2.incomingPlaces).map(id => net.getPlace(id));
				const outgoing = transition1.outgoingPlaces.concat(transition2.outgoingPlaces).map(id => net.getPlace(id));
				const transition = net.addTransition(id, action, incoming, outgoing);

				// update the location information for the constructed transition
				const locations = {};
				for(let id in transition1.locations){
					locations[id] = true;
				}
				for(let id in transition2.locations){
					locations[id] = true;
				}
				transition.locations = locations;
			}
		}
	}

	return net;
}