'use strict';

function petriNetAbstraction(process, isFair){
	var observableTransitionMap = {};

	// get all places that have at least one hidden tau event that it transitions to
	var tauTransitions = process.transitions.filter(transition => transition.label === TAU);
	for(var id = 0; id < tauTransitions.length; id++){
		var places = tauTransitions[id].incomingPlaces;
		for(var p = 0; p < places.length; p++){
			constructObservableTransitions(places[p]);
		}
	}

	// add the observable transitions to the process
	for(var key in observableTransitionMap){
		var transition = observableTransitionMap[key];
		var from = process.getPlace(transition.from);
		var to = process.getPlace(transition.to);
		process.addTransition(process.nextPlaceId, transition.label, [from], [to]);
	}

	// delete the hidden tau events from the process
	for(var i = 0; i < tauTransitions.length; i++){
		var incoming = tauTransitions[i].incomingPlaces;
		for(var j = 0; j < incoming.length; j++){
			incoming[i].deleteOutgoingTransitions(tauTransitions[i].id);
		}
		var outgoing = tauTransitions[i].outgoingPlaces;
		for(var j = 0; j < outgoing.length; j++){
			outgoing[j].deleteIncomingTransitions(tauTransitions[i].id);
		}
		process.removeTransition(tauTransitions[i].id);
	}

	// remove any places that are not transitionable to
	var places = process.places.filter(place => place.incomingTransitions.length === 0);
	for(var i = 0; i < places.length; i++){
		var transitions = places[i].outgoingTransitions.map(id => process.getTransition(id));
		process.removePlace(places[i].id);
		for(var j = 0; j < transitions.length; j++){
			var outgoing = transitions[j].outgoingPlaces;
			if(outgoing.length === 1){
				places.push(outgoing[0]);
			}

			process.removeTransition(transitions[j].id);
		}
	}

	return process;

	function constructObservableTransitions(place){
		// get observable events (transitions) that transition to the specified place
		var incomingObservableTransitions = place.incomingTransitions
			.map(id => process.getTransition(id))
			.filter(transition => transition.label !== TAU);

		var visited = {};
		var fringe = [place];
		while(fringe.length !== 0){
			var current = fringe.pop();
			// get neighbouring places that are transitionable to via a hidden tau event
			var unobservableTransitions = current.outgoingTransitions
				.map(id => process.getTransition(id))
				.filter(transition => transition.label === TAU);

			var neighbours = [];
			for(var i = 0; i < unobservableTransitions.length; i++){
				var outgoingPlaces = unobservableTransitions[i].outgoingPlaces;
				for(var j = 0; j < outgoingPlaces.length; j++){
					neighbours.push(outgoingPlaces[j]);
				}
			}

			for(var i = 0; i < neighbours.length; i++){
				var neighbour = neighbours[i];

				// check if there is a loop in the tau path
				if(neighbour.id === place.id){
					// add a deadlocked state if the abstraction is defined as unfair
					if(!isFair){
						// TODO
					}

					continue;
				}

				// check if the current place has been visited
				if(visited[neighbour.id] !== undefined){
					continue;
				}

				// push the neighbour to the fringe
				fringe.push(neighbour);

				var outgoingObservableTransitions = neighbour.outgoingTransitions
					.map(id => process.getTransition(id))
					.filter(transition => transition.label !== TAU);

				for(var j = 0; j < incomingObservableTransitions.length; j++){
					var transition = incomingObservableTransitions[j];
					var places = transition.incomingPlaces;
					if(places !== undefined){
						for(var k = 0; k < places.length; k++){
							constructObservableTransition(places[k].id, neighbour.id, transition.label);	
						}
					}
				}

				for(var j = 0; j < outgoingObservableTransitions.length; j++){
					var transition = outgoingObservableTransitions[j];
					var places = transition.outgoingPlaces;
					if(places !== undefined){
						for(var k = 0; k < places.length; k++){
							constructObservableTransition(place.id, places[k].id, transition.label);
						}
					}
				}
			}

			// mark the current place as visited
			visited[current.id] = true;
		}
	}

	/**
	 * Constructs an observable transition object and adds it to the
	 * observable transition map.
	 *
	 * @param {string} from - the place id the transition transitions from
	 * @param {string} to - the place id the transition transitions to
	 * @param {string} label - the action the transition represents
	 */
	function constructObservableTransition(from, to, label){
		var key = constructTransitionKey(from, to, label);
		if (observableTransitionMap[key] === undefined){
			observableTransitionMap[key] = new ObservableTransition(from, to, label);
		}
	}

	/**
	 * Constructs and returns a key that refers to an observable transition.
	 *
	 * @param {string} from - the place id the transition transitions from
	 * @param {string} to - the place id the transition transitions to
	 * @param {string} label - the action the transition represents
	 * @return {string} - the key for the transition
	 */
	function constructTransitionKey(from, to, label){
		return from + ' -|' + label + '|- ' + to; 
	}

	/**
	 * Constructs and returns an observable transition object.
	 *
	 * @param {string} from - the place id the transition transitions from
	 * @param {string} to - the place id the transition transitions to
	 * @param {string} label - the action the transition represents
	 * @return {object} - object representing an ovservable transition
	 */
	function ObservableTransition(from, to, label){
		return {
			from : from,
			to : to,
			label : label
		};
	}
}