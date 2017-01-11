'use strict';

function prunePetriNet(net){
	const transitionMap = {};
	const hiddenTransitions = net.transitions.filter(t => t.label === TAU);
	const toDelete = [];

	// check if there are any hidden transitions
	if(hiddenTransitions.length === 0){
		return net;
	}

	const walker = new PetriNetWalker(net);

	// find transitions to prune
	for(let i = 0; i < hiddenTransitions.length; i++){
		const hidden = hiddenTransitions[i];

		const marking = walker.getIncomingMarking(hidden);

		// make sure that the only transitions that are executable from
		// the curent marking are hidden transitions
		const transitions = walker.getOutgoingTransitions(marking);
		
		let allHidden = true;
		for(let j = 0; j < transitions.length; j++){
			if(transitions[j].label !== TAU){
				allHidden = false;
				break;
			}
		}

		// do not continue if all outgoing transitions are not hidden
		if(!allHidden){
			continue;
		}

		toDelete.push(hidden);

		// get the outgoing transitions from the marking once the
		// hidden transition is executed
		const nextMarking = walker.getOutgoingMarking(hidden);
		const outgoingTransitions = walker.getOutgoingTransitions(nextMarking);

		for(let j = 0; j < outgoingTransitions.length; j++){
			const transition = outgoingTransitions[j];
			const incoming = walker.getIncomingMarking(transition);
			const outgoing = walker.getOutgoingMarking(transition);

			const to = walker.getOutgoingMarking(hidden);
			for(let id in incoming){
				to[id]--;

				if(to[id] === 0){
					delete to[id];
				}
			}
			for(let id in outgoing){
				if(to[id] === undefined){
					to[id] = 0;
				}

				to[id]++;
			}

			constructTransition(transition.label, marking, to);

		}
	}

	// add the constructed transitions
	for(let key in transitionMap){
		const transition = transitionMap[key];
		const from = [];
		for(let id in transition.from){
			from.push(net.getPlace(id));
		}
		const to = [];
		for(let id in transition.to){
			to.push(net.getPlace(id));
		}

		net.addTransition(net.nextTransitionId, transition.label, from, to);
	}

	// delete the hidden transitions that were pruned
	for(let i = 0; i < toDelete.length; i++){
		net.removeTransition(toDelete[i].id);
	}

	net.trim();
	return net;

	function constructTransition(label, from, to){
		const key = constructTransitionKey(label, from, to);
		transitionMap[key] = {label:label, from:from, to:to };
	}

	function constructTransitionKey(label, from, to){
		const fromKey = JSON.stringify(Object.keys(from).sort());
		const toKey = JSON.stringify(Object.keys(to).sort());
		return fromKey + '-|' + label + '|- ' + toKey;
	}
}