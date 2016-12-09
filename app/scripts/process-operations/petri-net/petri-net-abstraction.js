'use strict';

function petriNetAbstraction(net, isFair){
	const observableTransitionMap = {};
	const walker = new PetriNetWalker(net);
	const hiddenTransitions = net.transitions.filter(t => t.label === TAU);

	for(let i = 0; i < hiddenTransitions.length; i++){
		constructObservableTransitions(walker, hiddenTransitions[i]);
	}

	// add the observable transitions to the petri net
	for(let key in observableTransitionMap){
		const transition = observableTransitionMap[key];
		const id = net.nextTransitionId;
		const from = transition.from.map(id => net.getPlace(id));
		const to = transition.to.map(id => net.getPlace(id));
		const t = net.addTransition(id, transition.label, from, to);
		t.locations = transition.locations;
	}

	// remove the hidden transitions from the net
	for(let i = 0; i < hiddenTransitions.length; i++){
		net.removeTransition(hiddenTransitions[i].id);
	}

	// remove unreachable places from the petri net
	let places = net.places.filter(p => p.incomingTransitions.length === 0 && p.metaData.startPlace === undefined);
	while(places.length !== 0){
		for(let i = 0; i < places.length; i++){
			const place = places[i];
			const outgoing = place.outgoingTransitions.map(id => net.getTransition(id));
			net.removePlace(place.id);

			// remove any transitions that have become uncreachable from the current place
			for(let j = 0; j < outgoing.length; j++){
				const transition = outgoing[j];
				if(transition.incomingPlaces.length === 0){
					net.removeTransition(transition.id);
				}
			}
		}

		places = net.places.filter(p => p.incomingTransitions.length === 0 && p.metaData.startPlace === undefined);
	}

	net.constructTerminals();
	return net;

	function constructObservableTransitions(walker, hiddenTransition){
		const incoming = hiddenTransition.incomingPlaces;
		const outgoing = hiddenTransition.outgoingPlaces;

		// construct a set of the incoming places
		const incomingSet = {};
		for(let i = 0; i < incoming.length; i++){
			incomingSet[incoming[i]] = true;
		}

		// construct a set of the outgoing places
		const outgoingSet = {};
		for(let i = 0; i < outgoing.length; i++){
			outgoingSet[outgoing[i]] = true;
		}

		// find the observable transitions coming from each place
		for(let i = 0; i < incoming.length; i++){
			const observable = net.getPlace(incoming[i]).incomingTransitions
					.map(id => net.getTransition(id)) // get the transitions
					.filter(t => t.label !== TAU); // remove any hidden transitions

			// construct new transitions for all the observable transitions
			for(let j = 0; j < observable.length; j++){
				const outgoingPlaces = observable[j].outgoingPlaces;

				// remove the outgoing places from the current transition
				const from = JSON.parse(JSON.stringify(incomingSet));
				for(let k = 0; k < outgoingPlaces.length; k++){
					delete from[outgoingPlaces[k]];
				}

				// add the incoming places from the current transition
				const incomingPlaces = observable[j].incomingPlaces;
				for(let k = 0; k < incomingPlaces.length; k++){
					from[incomingPlaces[k]] = true;
				}

				const outgoingMarkings = getOutgoingMarkings(walker, hiddenTransition);

				// construct a new observable transition
				for(let k = 0; k < outgoingMarkings.length; k++){
					constructObservableTransition(Object.keys(from), Object.keys(outgoingMarkings[k]), observable[j].label, observable[j].locations);
				}
			}
		}

		// find the observable transitions leaving each place
		for(let i = 0; i < outgoing.length; i++){
			const observable = net.getPlace(outgoing[i]).outgoingTransitions
					.map(id => net.getTransition(id)) // get the transitions
					.filter(t => t.label !== TAU); // remove any hidden transitions

			// construct new transitions from all the observable transitions
			for(let j = 0; j < observable.length; j++){
				const incomingPlaces = observable[j].incomingPlaces;

				// remove incoming places from the current transition
				const to = JSON.parse(JSON.stringify(outgoingSet));
				for(let k = 0; k < incomingPlaces.length; k++){
					delete to[incomingPlaces[k]];
				}

				// add the outgoing places from the current transition
				const outgoingPlaces = observable[j].outgoingPlaces;
				for(let k = 0; k < outgoingPlaces.length; k++){
					to[outgoingPlaces[k]] = true;
				}

				const incomingMarkings = getIncomingMarkings(walker, hiddenTransition);

				// construct a new observable transition
				for(let k = 0; k < incomingMarkings.length; k++){
					constructObservableTransition(Object.keys(incomingMarkings[k]), Object.keys(to), observable[j].label, observable[j].locations);
				}
			}
		}
	}

	function getIncomingMarkings(walker, hiddenTransition){
		const markings = [];
		const visited = {};
		const fringe = [hiddenTransition];
		while(fringe.length !== 0){
			const current = fringe.pop();
			const marking = walker.getIncomingMarking(current);
			markings.push(marking);

			const incoming = walker.getIncomingTransitions(marking).filter(t => t.label === TAU);

			for(let i = 0; i < incoming.length; i++){
				const transition = incoming[i];
				if(!visited[transition.id]){
					fringe.push(transition);
				}
			}

			visited[current.id] = true;
		}

		return markings;
	}

	function getOutgoingMarkings(walker, hiddenTransition){
		const markings = [];
		const visited = {};
		const fringe = [hiddenTransition];
		while(fringe.length !== 0){
			const current = fringe.pop();
			const marking = walker.getOutgoingMarking(current);
			markings.push(marking);

			const outgoing = walker.getOutgoingTransitions(marking).filter(t => t.label === TAU);
			
			for(let i = 0; i < outgoing.length; i++){
				const transition = outgoing[i];
				if(!visited[transition.id]){
					fringe.push(outgoing[i]);
				}
			}

			visited[current.id] = true;
		}

		return markings;
	}

	function constructObservableTransition(from, to, label, locations){
		const key = constructTransitionKey(from, to, label);
		if(observableTransitionMap[key] === undefined){
			observableTransitionMap[key] = new ObservableTransition(from, to, label, locations);
		}
	}

	function constructTransitionKey(from, to, label){
		const incoming = JSON.stringify(from.sort());
		const outgoing = JSON.stringify(to.sort());
		return incoming + ' -|' + label + '|- ' + outgoing; 
	}

	function ObservableTransition(from, to, label, locations){
		this.from = from;
		this.to = to;
		this.label = label;
		this.locations = locations;
	}
}