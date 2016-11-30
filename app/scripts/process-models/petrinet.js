'use strict';

const PETRI_NET = {

	get type(){
		return this.type;
	},

	get id(){
		return this.id;
	},

	get roots(){
		const roots = [];
		for(let id in this.rootIds){
			roots.push(this.placeMap[id]);
		}

		return roots;
	},

	addRoot: function(id){
		this.rootIds[id] = true;
	},

	removeRoot: function(id){
		delete this.rootIds[id];
	},

	get places(){
		const places = [];
		for(let id in this.placeMap){
			places.push(this.placeMap[id]);
		}

		return places;
	},

	getPlace: function(id){
		return this.placeMap[id];
	},

	addPlace: function(id, metaData){
		id = (id === undefined) ? this.nextPlaceId : id;
		metaData = (metaData === undefined) ? {} : metaData;

		const locationSet = {};
		locationSet[this.id] = true;

		const place = new PetriNetPlace(id, {}, {}, locationSet, metaData);
		this.placeMap[id] = place;
		this.placeCount++;
		return place;
	},

	removePlace: function(id){
		if(this.placeMap[id] === undefined){
			return;
		}

		delete this.placeMap[id];

		for(let i in this.transitionMap){
			const transition = this.transitionMap[i];
			delete transition.incomingPlaceSet[id];
			delete transition.outgoingPlaceSet[id];
		}

		if(this.rootIds[id] === true){
			delete this.rootIds[id];
		}

		this.placeCount--;
	},

	combinePlaces: function(place1, place2){
		const place = this.addPlace();

		const incoming = place1.incomingTransitions.concat(place2.incomingTransitions);
		for(let i = 0; i < incoming.length; i++){
			const id = incoming[i];
			place.addIncomingTransition(id);
			this.getTransition(id).addOutgoingPlace(place.id);
		}

		const outgoing = place1.outgoingTransitions.concat(place2.outgoingTransitions);
		for(let i = 0; i < outgoing.length; i++){
			const id = outgoing[i];
			place.addOutgoingTransition(id);
			this.getTransition(id).addIncomingPlace(place.id);
		}

		const tokens1 = (place1.metaData.startPlace !== undefined) ? place1.metaData.startPlace : 0;
		const tokens2 = (place2.metaData.startPlace !== undefined) ? place2.metaData.startPlace : 0;
		if((tokens1 + tokens2) !== 0){
			place.metaData.startPlace = 1;
			this.addRoot(place.id);
		}

		if(place1.metaData.references !== undefined || place2.metaData.references !== undefined){
			const references1 = (place1.metaData.references !== undefined) ? place1.metaData.references : {};
			const references2 = (place2.metaData.references !== undefined) ? place2.metaData.references : {};
			for(let id in references2){
				references1[id] = true;
			}

			place.metaData.references = references1;
		}

		return place;
	},

	get transitions(){
		const transitions = [];
		for(let id in this.transitionMap){
			transitions.push(this.transitionMap[id]);
		}

		return transitions;
	},

	getTransition: function(id){
		return this.transitionMap[id];
	},

	addTransition: function(id, label, incomingPlaces, outgoingPlaces){
		const locationSet = {};
		locationSet[this.id] = true;

		const transition = new PetriNetTransition(id, label, {}, {}, locationSet);
		for(let i = 0; i < incomingPlaces.length; i++){
			const place = incomingPlaces[i];
			transition.addIncomingPlace(place.id);
			place.addOutgoingTransition(id);
		}

		for(let i = 0; i < outgoingPlaces.length; i++){
			const place = outgoingPlaces[i];
			transition.addOutgoingPlace(place.id);
			place.addIncomingTransition(id);
		}

		if(this.labelSets[label] === undefined){
			this.labelSets[label] = [];
		}

		this.labelSets[label].push(transition);

		this.transitionMap[id] = transition;
		return transition;
	},

	removeTransition: function(id){
		if(this.transitionMap[id] === undefined){
			return;
		}

		delete this.transitionMap[id];

		for(let i in this.placeMap){
			const place = this.placeMap[i];
			delete place.incomingTransitions[id];
			delete place.outgoingTransitions[id];
		}

		this.transitionCount--;

		for(let label in this.labelSets){
			let transitions = this.labelSets[label];
			for(let i = 0; i < transitions.length; i++){
				if(transitions[i].id === id){
					transitions = transitions.splice(i, 1);
					this.labelSets[label] = transitions;
					if(transitions.length === 0){
						delete this.labelSets[label];
					}

					return;
				}
			}
		}
	},

	relabelTransition: function(oldLabel, newLabel){
		const transitions = this.labelSets[oldLabel];
		for(let i = 0; i < transitions.length; i++){
			transitions[i].label = newLabel;
		}

		this.labelSets[newLabel] = transitions;
		delete this.labelSets[oldLabel];
	},

	getLabelSets: function(){
		const labelSets = {};
		for(let label in this.labelSets){
			labelSets[label] = this.labelSets[label];
		}

		return labelSets;
	},

	get alphabet(){
		const alphabet = {};
		for(let label in this.labelSets){
			alphabet[label] = true;
		}

		return alphabet;
	},

	constructConnection: function(transition, places){
		for(let i = 0; i < places.length; i++){
			const place = places[i];
			transition.addOutgoingPlace(place.id);
			place.addIncomingTransition(transition.id);
		}
	},

	addPetriNet: function(net){
		const places = net.places;
		for(let i = 0; i < places.length; i++){
			const place = places[i];
			this.placeMap[place.id] = place;
			this.placeCount++;
		}

		const transitions = net.transitions;
		for(let i = 0; i < transitions.length; i++){
			const transition = transitions[i];
			this.transitionMap[transition.id] = transition;

			if(this.labelSets[transition.label] === undefined){
				this.labelSets[transition.label] = [];
			}
			this.labelSets[transition.label].push(transition);

			this.transitionCount++;
		}
	},

	get clone(){
		if(this.metaData.cloneCount === undefined){
			this.metaData.cloneCount = 0;
		}
		const cloneId = this.metaData.cloneCount++;

		const net = new PetriNet(this.id + '.' + cloneId);

		// clone places from this net and add them to the clone
		const places = this.places;
		for(let i = 0; i < places.length; i++){
			const id = places[i].id + '.' + cloneId;;
			const incoming = relabelSet(JSON.parse(JSON.stringify(places[i].incomingTransitionSet)));
			const outgoing = relabelSet(JSON.parse(JSON.stringify(places[i].outgoingTransitionSet)));
			const locations = JSON.parse(JSON.stringify(places[i].locations));
			const metaData = JSON.parse(JSON.stringify(places[i].metaData));
			const place = new PetriNetPlace(id, incoming, outgoing, locations, metaData);
			net.placeMap[id] = place;
			net.placeCount++;
		}

		// clone transitions from this net and add them to the clone
		const transitions = this.transitions;
		for(let i = 0; i < transitions.length; i++){
			const id = transitions[i].id + '.' + cloneId;
			const label = transitions[i].label;
			const incoming = relabelSet(JSON.parse(JSON.stringify(transitions[i].incomingPlaceSet)));
			const outgoing = relabelSet(JSON.parse(JSON.stringify(transitions[i].outgoingPlaceSet)));
			const locations = JSON.parse(JSON.stringify(transitions[i].locations));
			const metaData = JSON.parse(JSON.stringify(transitions[i].metaData));
			const transition = new PetriNetTransition(id, label, incoming, outgoing, locations, metaData);
			net.transitionMap[id] = transition;

			if(net.labelSets[label] === undefined){
				net.labelSets[label] = [];
			}
			net.labelSets[label].push(transition);

			net.transitionCount++;
		}

		net.rootIds = relabelSet(JSON.parse(JSON.stringify(this.rootIds)));

		return net;

		function relabelSet(set){
			const newSet = {};
			for(let label in set){
				newSet[label + '.' + cloneId] = true;
			}

			return newSet;
		}
	},

	get nextPlaceId(){
		return this.id + '.p' + this.placeId++;
	},

	get nextTransitionId(){
		return this.id + '.t' + this.transitionId++;
	},

	// Global Functions

	convert: function(net){
		// check that the object has the correct properties
		const properties = ['type', 'id', 'rootIds', 'placeMap', 'transitionMap', 'metaData'];
		let match = true;
		for(let i = 0; i < properties.length; i++){
			if(!net.hasOwnProperty(properties[i])){
				match = false;
				break;
			}
		}

		if(match){
			for(let id in net.placeMap){
				const place = net.placeMap[id];

				const placeProperties = ['id', 'incomingTransitionSet', 'outgoingTransitionSet', 'metaData'];
				let placeMatch = true;
				for(let i = 0; i < placeProperties.length; i++){
					if(!place.hasOwnProperty(placeProperties[i])){
						placeMatch = false;
						break;
					}
				}

				if(!placeMatch){
					// throw error
				}

				Object.setPrototypeOf(place, PETRI_NET_PLACE);
				net.placeMap[id] = place;
			}

			net.labelSets = {};

			for(let id in net.transitionMap){
				const transition = net.transitionMap[id];

				const transitionProperties = ['id', 'label', 'incomingPlaceSet', 'outgoingPlaceSet', 'metaData'];
				let transitionMatch = true;
				for(let i = 0; i < transitionProperties.length; i++){
					if(!transition.hasOwnProperty(transitionProperties[i])){
						transitionMatch = false;
						break;
					}
				}

				if(!transitionMatch){
					// throw error
				}

				Object.setPrototypeOf(transition, PETRI_NET_TRANSITION);
				net.transitionMap[id] = transition;

				if(net.labelSets[transition.label] === undefined){
					net.labelSets[transition.label] = [];
				}

				net.labelSets[transition.label].push(transition);
			}

			Object.setPrototypeOf(net, PETRI_NET);
			return net;
		}
	}
}

function PetriNet(id){
	this.type = 'petrinet';
	this.id = id;
	this.rootIds = {};
	this.placeMap = {};
	this.placeCount = 0;
	this.transitionMap = {};
	this.labelSets = {};
	this.transitionCount = 0;
	this.placeId = 0;
	this.transitionId = 0;
	this.metaData = {};
	Object.setPrototypeOf(this, PETRI_NET);
}

const PETRI_NET_PLACE = {
	get type(){
		return 'place';
	},

	get id(){
		return this.id;
	},

	get incomingTransitions(){
		const incoming = [];
		for(let id in this.incomingTransitionSet){
			incoming.push(id);
		}

		return incoming;
	},

	addIncomingTransition: function(id){
		this.incomingTransitionSet[id] = true;
	},

	removeIncomingTransition: function(id){
		delete this.incomingTransitionSet[id];
	},

	get outgoingTransitions(){
		const outgoing = [];
		for(let id in this.outgoingTransitionSet){
			outgoing.push(id);
		}

		return outgoing;
	},

	addOutgoingTransition: function(id){
		this.outgoingTransitionSet[id] = true;
	},

	removeOutgoingTransition: function(id){
		delete this.outgoingTransitionSet[id];
	},

	get locations(){
		return JSON.parse(JSON.stringify(this.locationSet));
	},

	addLocation: function(id){
		this.locationSet[id] = true;
	},

	removeLocation: function(){
		delete this.locationSet[id];
	},

	isUnreachable: function(){
		return Object.keys(this.incomingTransitions).length === 0;
	},

	isTerminal: function(){
		return Object.keys(this.outgoingTransitions).length === 0;
	},

	addMetaData: function(key, data){
		this.metaData[key] = data;
	},

	getMetaData: function(key){
		return this.metaData[key];
	},

	deleteMetaData: function(key){
		delete this.metaData[key];
	}
}

function PetriNetPlace(id, incomingTransitions, outgoingTransitions, locationSet, metaData){
	this.id = id;
	this.incomingTransitionSet = (incomingTransitions === undefined) ? {} : incomingTransitions;
	this.outgoingTransitionSet = (outgoingTransitions === undefined) ? {} : outgoingTransitions;
	this.locationSet = locationSet;
	this.metaData = (metaData === undefined) ? {} : metaData;
	Object.setPrototypeOf(this, PETRI_NET_PLACE);
}

const PETRI_NET_TRANSITION = {
	get type(){
		return 'transition';
	},

	get id(){
		return this.id;
	},

	get label(){
		return this.label;
	},

	set label(label){
		this.label = label;
	},

	get incomingPlaces(){
		const incoming = [];
		for(let id in this.incomingPlaceSet){
			incoming.push(id);
		}

		return incoming;
	},

	addIncomingPlace: function(id){
		this.incomingPlaceSet[id] = true;
	},

	removeIncomingPlace: function(id){
		delete this.incomingPlaceSet[id];
	},

	get outgoingPlaces(){
		const outgoing = [];
		for(let id in this.outgoingPlaceSet){
			outgoing.push(id);
		}

		return outgoing;
	},

	addOutgoingPlace: function(id){
		this.outgoingPlaceSet[id] = true;
	},

	removeOutgoingPlace: function(id){
		delete this.outgoingPlaceSet[id];
	},

	get locations(){
		return JSON.parse(JSON.stringify(this.locationSet));
	},

	addLocation: function(id){
		this.locationSet[id] = true;
	},

	removeLocation: function(){
		delete this.locationSet[id];
	},

	addMetaData: function(key, data){
		this.metaData[key] = data;
	},

	getMetaData: function(key){
		return this.metaData[key];
	},

	deleteMetaData: function(key){
		delete this.metaData[key];
	}
}

function PetriNetTransition(id, label, incomingPlaces, outgoingPlaces, locationSet, metaData){
	this.id = id;
	this.label = label;
	this.incomingPlaceSet = (incomingPlaces === undefined) ? {} : incomingPlaces;
	this.outgoingPlaceSet = (outgoingPlaces === undefined) ? {} : outgoingPlaces;
	this.locationSet = locationSet;
	this.metaData = (metaData === undefined) ? {} : metaData;
	Object.setPrototypeOf(this, PETRI_NET_TRANSITION);
}