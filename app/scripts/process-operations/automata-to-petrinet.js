'use strict';

function automatonToPetriNet(automaton){
	const placeMap = {}; // process -> place
	const executed = {};
	const stateMap = {}; // node id -> {location -> place}
	const visited = {};

	const walker = new AutomatonWalker(automaton);
	const root = walker.root;
	stateMap[root.id] = {};

	// construct the start places for the net from the root of the automaton
	const net = new PetriNet(automaton.id);
	for(let id in root.locations){
		const place = net.addPlace();
		place.metaData.startPlace = 1;
		net.addRoot(place.id);
		placeMap[id] = place;
		stateMap[root.id][id] = place;
	}

	// traverse through the automaton to contstruct the petri net
	const fringe = [root];
	while(fringe.length !== 0){
		const current = fringe.pop();

		const outgoing = walker.getOutgoingNodes(current);
		for(let i = 0; i < outgoing.length; i++){
			const {edge, node} = outgoing[i];


			// check if this node has already been visited
			if(!visited[node.id]){
				fringe.push(node);
			}
			else{
				continue;
			}

			const edgeId = (edge.metaData.originId === undefined) ? edge.id : edge.metaData.originId;

			// check if the current edge has already been executed
			if(executed[edgeId]){
				continue;
			}

			// get the incoming places to the new transition
			const places = [];
			for(let id in edge.locations){
				places.push(placeMap[id]);
			}

			// get the outgoing places to the new transition
			const nextPlaces = [];

			if(stateMap[node.id] === undefined){
				stateMap[node.id] = {};
			}

			for(let id in edge.locations){
				// check if the next node has been visited
				if(stateMap[node.id][id] === undefined){
					const place = net.addPlace();
					placeMap[id] = place;
					stateMap[node.id][id] = place;
				}

				nextPlaces.push(stateMap[node.id][id])
			}

			net.addTransition(net.nextTransitionId, edge.label, places, nextPlaces);

			// mark this edge as executed
			executed[edgeId] = true;
			visited[node.id] = true;
		}
	}

	// mark places as terminals if necessary
	const places = net.places;
	for(let i = 0; i < places.length; i++){
		const place = places[i];

		if(place.outgoingTransitions.length === 0){
			// check if an incoming transition is a deadlock
			const incoming = place.incomingTransitions.map(id => net.getTransition(id)).filter(t => t.label === DELTA);
			if(incoming.length === 1){
				place.metaData.isTerminal = 'error';
			}
			else{
				place.metaData.isTerminal = 'stop';
			}
		}
	}

	return net;
}