'use strict';

function automatonToPetriNet(automaton){
	const placeMap = {};
	const executed = {};
	const visitedNodes = {};

	const walker = new AutomatonWalker(automaton);
	const root = walker.root;

	// construct the start places for the net from the root of the automaton
	const net = new PetriNet(automaton.id);
	for(let id in root.locations){
		const place = net.addPlace();
		place.metaData.startPlace = 1;
		net.addRoot(place.id);
		placeMap[id] = place;
	}

	// traverse through the automaton to contstruct the petri net
	const fringe = [root];
	while(fringe.length !== 0){
		const current = fringe.pop();

		// mark the current node as visited
		visitedNodes[current.id] = true;

		const outgoing = walker.getOutgoingNodes(current);
		for(let i = 0; i < outgoing.length; i++){
			const {edge, node} = outgoing[i];

			// push node to fringe if it hasn't been visited
			if(!visitedNodes[node.id]){
				fringe.push(node);
			}

			const edgeId = (edge.metaData.originId === undefined) ? edge.id : edge.metaData.originId;

			// check if the current edge has already been executed
			if(executed[edgeId]){
				continue;
			}

			// find and construct the places proceeding and preceding the transition
			const places = [];
			const nextPlaces = [];
			for(let id in edge.locations){
				// preceding place
				places.push(placeMap[id]);
				// proceeding place
				const place = net.addPlace();
				placeMap[id] = place;
				nextPlaces.push(place);
			}

			net.addTransition(net.nextTransitionId, edge.label, places, nextPlaces);

			// mark this edge as executed
			executed[edgeId] = true;
		}
	}

	return net;
}