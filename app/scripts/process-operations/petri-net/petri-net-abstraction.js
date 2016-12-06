'use strict';

function petriNetAbstraction(net, isFair){
	const observableTransitionMap = {};
	const hiddenTransitions = net.transitions.filter(t => t.label === TAU);

	for(let i = 0; i < hiddenTransitions.length; i++){
		constructObservableTransitions(hiddenTransitions[i]);
	}

	// add the observable transitions to the petri net
	for(let key in observableTransitionMap){
		const transition = observableTransitionMap[key];
		const id = net.nextTransitionId;
		const from = transition.from.map(id => net.getPlace(id));
		const to = transition.to.map(id => net.getPlace(id));
		net.addTransition(id, transition.label, from, to);
	}

	// remove the hidden transitions from the net
	for(let i = 0; i < hiddenTransitions.length; i++){
		net.removeTransition(hiddenTransitions[i].id);
	}

	net.constructTerminals();
	return net;

	function constructObservableTransitions(hiddenTransition){
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

				// construct a new observable transition
				constructObservableTransition(Object.keys(from), outgoing, observable[j].label);
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

				// construct a new observable transition
				constructObservableTransition(incoming, Object.keys(to), observable[j].label);
			}
		}
	}

	function constructObservableTransition(from, to, label){
		const key = constructTransitionKey(from, to, label);
		if(observableTransitionMap[key] === undefined){
			observableTransitionMap[key] = new ObservableTransition(from, to, label);
		}
	}

	function constructTransitionKey(from, to, label){
		const incoming = JSON.stringify(from.sort());
		const outgoing = JSON.stringify(to.sort());
		return incoming + ' -|' + label + '|- ' + outgoing; 
	}

	function ObservableTransition(from, to, label){
		this.from = from;
		this.to = to;
		this.label = label;
	}
}