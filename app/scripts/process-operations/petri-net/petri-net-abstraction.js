'use strict';

function petriNetAbstraction(process, isFairAbstraction){
	var hiddenTransitions = process.transitions.filter(t => t.label === TAU);
	while(hiddenTransitions.length !== 0){
		var inPath = {};
		var current = hiddenTransitions.pop();
		inPath[current.id] = true;

		// find the heads of the hidden tau path
		var heads = [];
		var incoming = current.incomingPlaces;
		var transitions = [];
		for(var i = 0; i < incoming.length; i++){
			transitions = transitions.concat(incoming[i].incomingTransitions.map(id => process.getTransition(id)).filter(t => t.label === TAU));
		}

		// if no inconing hidden transitions then the current is the head
		if(transitions.length !== 0){
			while(transitions.length !== 0){
				var transition = transitions.pop();
				incoming = transition.incomingPlaces;
				for(var i = 0; i < incoming.length; i++){
					var incomingTransitions = incoming[i].incomingTransitions.map(id => process.getTransition(id)).filter(t => t.label === TAU);
					if(incomingTransitions.length === 0){
						heads.push(transition);
					}
					else{
						for(var j = 0; j < incomingTransitions.length; j++){
							if(inPath[incomingTransitions[j].id] === undefined){
								transitions.push(incomingTransitions[j]);
							}
						}
					}
				}

				inPath[transition.id] = true;
			}
		}
		else{
			heads.push(current);
		}

		// find the tails of the hidden tau path
		var tails = [];
		var outgoing = current.outgoingPlaces;
		var transitions = [];
		for(var i = 0; i < outgoing.length; i++){
			transitions = transitions.concat(outgoing[i].outgoingTransitions.map(id => process.getTransition(id)).filter(t => t.label === TAU));
		}

		// if no outgoing hidden transitions then the current is the tail
		if(transitions.length !== 0){
			while(transitions.length !== 0){
				var transition = transitions.pop();
				outgoing = transition.outgoingPlaces;
				for(var i = 0; i < incoming.length; i++){
					var outgoingTransitions = incoming[i].outgoingTransitions.map(id => process.getTransition(id)).filter(t => t.label === TAU);
					if(outgoingTransitions.length === 0){
						tails.push(transition);
					}
					else{
						for(var j = 0; j < outgoingTransitions.length; j++){
							if(inPath[outgoingTransitions[j].id] === undefined){
								transitions.push(outgoingTransitions[j]);
							}
						}
					}
				}

				inPath[transition.id] = true;
			}
		}
		else{
			tails.push(current);
		}

		// make guards
		for(var i = 0; i < heads.length; i++){
			var incoming = heads[i].incomingPlaces;
			for(var j = 0; j < tails.length; j++){
				var outgoing = tails[j].outgoingPlaces;
				process.addTransition(process.nextTransitionId, GAMMA, incoming, outgoing);
			}
		}
		for(var i = 0; i < tails.length; i++){
		}

		var nextTransitions = [];
		for(var i = 0; i < hiddenTransitions.length; i++){
			var id = hiddenTransitions[i].id;
			if(inPath[id] === undefined){
				nextTransitions.push(hiddenTransitions[i]);
			}
		}

		// remove the visited hidden transitions
		for(var id in inPath){
			process.removeTransition(id);
		}

		var places = process.places.filter(p => p.incomingTransitions.length === 0 & p.getMetaData('startPlace') === undefined);
		for(var i = 0; i < places.length; i++){
			process.removePlace(places[i].id);
		}

		hiddenTransitions = nextTransitions;
	}

	return process;
}