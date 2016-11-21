'use strict'

function petriNetAbstraction(process, isFairAbstraction){
	var observableTransitionMap = {};
	var hiddenTransitions = process.transitions.filter(t => t.label === TAU);

	for(var i = 0; i < hiddenTransitions.length; i++){
		constructObservableTransitions(hiddenTransitions[i]);
	}

	// add the observable transitions to the process
	for(var key in observableTransitionMap){
		var transition = observableTransitionMap[key];
		var id = process.nextTransitionId;
		var from = transition.from.map(id => process.getPlace(id));
		var to = transition.to.map(id => process.getPlace(id));
		process.addTransition(id, transition.label, from, to);
	}

	// delete the hidden tau events from the process
	for(var i = 0; i < hiddenTransitions.length; i++){
		process.removeTransition(hiddenTransitions[i].id);
	}

	// remove any places that are not transitionable to
	var places = process.places.filter(p => p.incomingTransitions.length === 0 && p.getMetaData('startPlace') === undefined);
	while(places.length !== 0){
		var place = places.pop();
		var transitions = place.outgoingTransitions.map(id => process.getTransition(id));
		for(var i = 0; i < transitions.length; i++){
			var incoming = transitions[i].incomingPlaces.map(id => process.getPlace(id));
			if(incoming.length === 1){
				var outgoing = transitions[i].outgoingPlaces.map(id => process.getPlace(id)).filter(p => p.incomingTransitions.length === 1);
				for(var j = 0; j < outgoing.length; j++){
					places.push(outgoing[j]);
				}

				process.removeTransition(transitions[i].id);
			}
		}

		process.removePlace(place.id);
	}

	return process;

	function constructObservableTransitions(hiddenTransition){
		var incoming = hiddenTransition.incomingPlaces.map(id => process.getPlace(id));

		var fringe = [hiddenTransition];
		while(fringe.length !== 0){
			hiddenTransition = fringe.pop();
			var outgoing = hiddenTransition.outgoingPlaces.map(id => process.getPlace(id));

			for(var i = 0; i < incoming.length; i++){
				var observableTransitions = incoming[i].incomingTransitions
					.map(id => process.getTransition(id))
					.filter(t => t.label !== TAU);

				for(var j = 0; j < observableTransitions.length; j++){
					var incomingPlaces = incoming.concat(observableTransitions[j].incomingPlaces.map(id => process.getPlace(id)));
					var incomingSet = {};
					for(var k = 0; k < incomingPlaces.length; k++){
						incomingSet[incomingPlaces[k].id] = true;
					}

					var outgoingPlaces = observableTransitions[j].outgoingPlaces.map(id => process.getPlace(id));
					for(var k = 0; k < outgoingPlaces.length; k++){
						delete incomingSet[outgoingPlaces[k].id];
					}

					var from = [];
					for(var id in incomingSet){
						from.push(id);
					}

					constructObservableTransition(from, outgoing.map(p => p.id), observableTransitions[j].label);
				}
			}

			for(var i = 0; i < outgoing.length; i++){
				var transitions = outgoing[i].outgoingTransitions.map(id => process.getTransition(id));
				var observableTransitions = transitions.filter(t => t.label !== TAU);

				for(var j = 0; j < observableTransitions.length; j++){
					var outgoingPlaces = outgoing.concat(observableTransitions[j].outgoingPlaces.map(id => process.getPlace(id)));
					var outgoingSet = {};
					for(var k = 0; k < outgoingPlaces.length; k++){
						outgoingSet[outgoingPlaces[k].id] = true;
					}

					var incomingPlaces = observableTransitions[j].incomingPlaces.map(id => process.getPlace(id));
					for(var k = 0; k < incomingPlaces.length; k++){
						delete outgoingSet[incomingPlaces[k].id];
					}

					var to = [];
					for(var id in outgoingSet){
						to.push(id);

						constructObservableTransition(incoming.map(p => p.id), to, observableTransitions[j].label);
					}
				}
			}

			// push hidden transitions to fringe
			var outgoingPlaces = hiddenTransition.outgoingPlaces.map(id => process.getPlace(id));
			for(var i = 0; i < outgoingPlaces.length; i++){
				var outgoingTransitions = outgoingPlaces[i].outgoingTransitions
					.map(id => process.getTransition(id))
					.filter(t => t.label === TAU);

				fringe = fringe.concat(outgoingTransitions);
			}
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
		var incoming = JSON.stringify(from.sort());
		var outgoing = JSON.stringify(to.sort());
		return incoming + ' -|' + label + '|- ' + outgoing; 
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