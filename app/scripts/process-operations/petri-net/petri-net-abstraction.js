'use strict';

function petriNetAbstraction(net, isFair, prune){
	// prune the net if specified
	if(prune){
		prunePetriNet(net);
	}

	const observableTransitionMap = {};
	const walker = new PetriNetWalker(net);
	const hiddenTransitions = net.transitions.filter(t => t.label === TAU);

	for(let i = 0; i < hiddenTransitions.length; i++){
		constructObservableTransitions(walker, hiddenTransitions[i]);
	}

	// remove the hidden transitions from the net
	for(let i = 0; i < hiddenTransitions.length; i++){
		net.removeTransition(hiddenTransitions[i].id);
	}

	const original = net.clone;
	net.metaData.original = original;

	// add the observable transitions to the petri net
	for(let key in observableTransitionMap){
		const transition = observableTransitionMap[key];
		const id = net.nextTransitionId;
		const from = transition.from.map(id => net.getPlace(id));
		const to = transition.to.map(id => net.getPlace(id));
		const t = net.addTransition(id, transition.label, from, to, transition.metaData);
		t.locations = transition.locations;
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
			if(incomingSet[incoming[i]] === undefined){
				incomingSet[incoming[i]] = 0;
			}

			incomingSet[incoming[i]]++;
		}

		// construct a set of the outgoing places
		const outgoingSet = {};
		for(let i = 0; i < outgoing.length; i++){
			if(outgoingSet[outgoing[i]] === undefined){
				outgoingSet[outgoing[i]] = 0;
			}

			outgoingSet[outgoing[i]]++;
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
					from[outgoingPlaces[k]]--;
				
					if(from[outgoingPlaces[k]] === 0){
						delete from[outgoingPlaces[k]];
					}
				}

				// add the incoming places from the current transition
				const incomingPlaces = observable[j].incomingPlaces;
				for(let k = 0; k < incomingPlaces.length; k++){
					if(from[incomingPlaces[k]] === undefined){
						from[incomingPlaces[k]] = 0;
					}

					from[incomingPlaces[k]]++;
				}

				const outgoingMarkings = getOutgoingMarkings(walker, hiddenTransition);

				// construct a new observable transition
				for(let k = 0; k < outgoingMarkings.length; k++){
					constructObservableTransition(Object.keys(from), Object.keys(outgoingMarkings[k]), observable[j].label, observable[j].metaDataSet, observable[j].locations);
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
					to[incomingPlaces[k]]--;

					if(to[incomingPlaces[k]] === 0){
						delete to[incomingPlaces[k]];
					}
				}

				// add the outgoing places from the current transition
				const outgoingPlaces = observable[j].outgoingPlaces;
				for(let k = 0; k < outgoingPlaces.length; k++){
					if(to[outgoingPlaces[k]] === undefined){
						to[outgoingPlaces[k]] = 0;
					}

					to[outgoingPlaces[k]]++;
				}

				const incomingMarkings = getIncomingMarkings(walker, hiddenTransition);

				// construct a new observable transition
				for(let k = 0; k < incomingMarkings.length; k++){
					constructObservableTransition(Object.keys(incomingMarkings[k]), Object.keys(to), observable[j].label, observable[j].metaDataSet, observable[j].locations);
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

	function constructObservableTransition(from, to, label, metaData, locations){
		const key = constructTransitionKey(from, to, label);
		if(observableTransitionMap[key] === undefined){
			observableTransitionMap[key] = new ObservableTransition(from, to, label, metaData, locations);
		}
	}

	function constructTransitionKey(from, to, label){
		const incoming = JSON.stringify(from.sort());
		const outgoing = JSON.stringify(to.sort());
		return incoming + ' -|' + label + '|- ' + outgoing; 
	}

	function ObservableTransition(from, to, label, metaData, locations){
		this.from = from;
		this.to = to;
		this.label = label;
		this.metadata = metaData;
		this.locations = locations;
	}
}