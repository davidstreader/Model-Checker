'use strict';

const PETRI_NET_WALKER = {
	get initialMarking(){
		const marking = {};

		const roots = this.net.roots;
		for(let i = 0; i < roots.length; i++){
			const id = roots[i].id;
			if(marking[id] === undefined){
				marking[id] = 0;
			}

			marking[id]++;
		}

		return marking;
	},

	getIncomingTransitions: function(marking){
		// check that the a valid marking has been received
		const transitionSet = {};
		for(let id in marking){
			const place = this.net.getPlace(id);
			// check that the id was valid
			if(place === undefined){
				const message = 'Place id \'' + id + '\' not found in process \'' + this.net.id + '\'';
				throw new PetriNetWalkerException(message);
			}

			const incoming = place.incomingTransitions;
			for(let i = 0; i < incoming.length; i++){
				transitionSet[incoming[i]] = true;
			}
		}

		const transitions = Object.keys(transitionSet).map(id => this.net.getTransition(id));
		return transitions;
	},

	getOutgoingTransitions: function(marking){
		// check that the a valid marking has been received
		const transitionSet = {};
		for(let id in marking){
			const place = this.net.getPlace(id);
			// check that the id was valid
			if(place === undefined){
				const message = 'Place id \'' + id + '\' not found in process \'' + this.net.id + '\'';
				throw new PetriNetWalkerException(message);
			}

			const outgoing = place.outgoingTransitions;
			for(let i = 0; i < outgoing.length; i++){
				transitionSet[outgoing[i]] = true;
			}
		}

		const transitions = Object.keys(transitionSet).map(id => this.net.getTransition(id));
		return transitions;
	},

	getIncomingMarking: function(transition){
		const incoming = transition.incomingPlaces;
		
		// construct a set of the incoming places
		const incomingSet = {};
		for(let i = 0; i < incoming.length; i++){
			incomingSet[incoming[i]] = true;
		}

		return incomingSet;
	},

	getOutgoingMarking: function(transition){
		const outgoing = transition.outgoingPlaces;

		// construct a set of the outgoing places
		const outgoingSet = {};
		for(let i = 0; i < outgoing.length; i++){
			outgoingSet[outgoing[i]] = true;
		}

		return outgoingSet;
	},

	compareMarkings: function(marking1, marking2){
		for(let id in marking1){
			if(marking2[id] === undefined){
				return false;
			}
		}

		for(let id in marking2){
			if(marking1[id] === undefined){
				return false;
			}
		}

		return true;
	},

	markingKey: function(marking){
		const places = [];
		for(let id in marking){
			for(let i = 0; i < marking[id]; i++){
				places.push(id);
			}
		}

		return JSON.stringify(places.sort());
	},

	transitionKey: function(transitions){
		const key = [];
		for(let i = 0; i < transitions.length; i++){
			key.push(transitions[i].label);
		}

		return keys.sort();
	},

	executeTransition: function(transition, marking){
		const nextMarking = JSON.parse(JSON.stringify(marking));
		const incoming = transition.incomingPlaces;

		for(let i = 0; i < incoming.length; i++){
			const id = incoming[i];
			if(marking[id] === undefined){
				return undefined;
			}

			nextMarking[id]--;
			if(nextMarking[id] === 0){
				delete nextMarking[id];
			}
		}

		const outgoing = transition.outgoingPlaces;
		for(let i = 0; i < outgoing.length; i++){
			const id = outgoing[i];
			if(nextMarking[id] === undefined){
				nextMarking[id] = 0;
			}

			nextMarking[id]++;
		}

		return nextMarking;
	},

	findMarkings: function(){
		const markings = [];
		const visited = {};
		const fringe = [this.initialMarking];
		while(fringe.length !== 0){
			const current = fringe.pop();
			
			const key = this.markingKey(current);
			if(visited[key]){
				continue;
			}

			markings.push(current);

			const transitions = this.getOutgoingTransitions(current);
			for(let i = 0; i < transitions.length; i++){
				const transition = transitions[i];

				const nextMarking = this.executeTransition(transition, current);
				if(nextMarking === undefined){
					continue;
				}

				fringe.push(nextMarking);
			}

			visited[key] = true;
		}

		return markings;
	}
}

function PetriNetWalker(net){
	// check that a petri net has been received
	if(net.type !== 'petrinet'){
		const message = 'Expecting a Petri net but received a ' + net.type;
		throw new PetriNetWalkerException(message);
	}

	this.net = net;
	Object.setPrototypeOf(this, PETRI_NET_WALKER);
}

function PetriNetWalkerException(message){
	this.message = message;
	this.toString = function(){
		return 'PetriNetWalkerException: ' + this.message;
	}
}