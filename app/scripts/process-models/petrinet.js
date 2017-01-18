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
			transition.removeIncomingPlace(id);
			transition.removeOutgoingPlace(id);
		}

		if(this.rootIds[id] === true){
			delete this.rootIds[id];
		}

		this.placeCount--;
	},

	combinePlaces: function(place1, place2){
		const place = this.addPlace();
    const isPartOfInterrupt = place1.metaData.isPartOfInterrupt || place2.metaData.isPartOfInterrupt;
		place.metaData.isPartOfInterrupt = isPartOfInterrupt;
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
			place.metaData.startPlace = tokens1 + tokens2;
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

		place.locations = place2.locations;

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

	addTransition: function(id, label, incomingPlaces, outgoingPlaces, metadata){
		const locationSet = {};
		locationSet[this.id] = true;

		const transition = new PetriNetTransition(id, label, {}, {}, locationSet, metadata);
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
			delete place.incomingTransitionSet[id];
			delete place.outgoingTransitionSet[id];
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

	combineTransitions: function(transitions){
		let incoming = {};
		for(let i = 0; i < transitions.length; i++){
			const transition = transitions[i];
			for(let id in transition.incomingPlaceSet){
				incoming[id] = transition.incomingPlaceSet[id];
			}
		}
		incoming = Object.keys(incoming).map(id => this.getPlace(id));

		let outgoing = {};
		for(let i = 0; i < transitions.length; i++){
			const transition = transitions[i];
			for(let id in transition.outgoingPlaceSet){
				outgoing[id] = transition.outgoingPlaceSet[id];
			}
		}
		outgoing = Object.keys(outgoing).map(id => this.getPlace(id));
	
		const transition = this.addTransition(this.nextTransitionId, transitions[0].label, incoming, outgoing);
		return transition;
	},

	relabelTransition: function(oldLabel, newLabel){
		const transitions = this.labelSets[oldLabel];

		if(transitions === undefined){
			return;
		}

		for(let i = 0; i < transitions.length; i++){
			transitions[i].label = newLabel;
		}

		this.labelSets[newLabel] = transitions;
		delete this.labelSets[oldLabel];
	},

	labelPetriNet: function(label){
		for(let id in this.placeMap){
			const newId = label + ':' + id;
			const place = this.getPlace(id);
			this.placeMap[newId] = place;
			place.id = newId;

			for(let transition in place.incomingTransitionSet){
				place.incomingTransitionSet[label + ':' + transition] = place.incomingTransitionSet[transition];
				delete place.incomingTransitionSet[transition];
			}

			for(let transition in place.outgoingTransitionSet){
				place.outgoingTransitionSet[label + ':' + transition] = place.outgoingTransitionSet[transition];
				delete place.outgoingTransitionSet[transition];
			}

			place.locationSet[label + ':' + this.id] = true;
			delete place.locationSet[this.id];

			delete this.placeMap[id];
		}

		for(let id in this.transitionMap){
			const newId = label + ':' + id;

			const transition = this.getTransition(id);
			this.transitionMap[newId] = transition;
			transition.id = newId;

			transition.label = label + '.' + transition.label;

			for(let place in transition.incomingPlaceSet){
				transition.incomingPlaceSet[label + ':' + place] = transition.incomingPlaceSet[place];
				delete transition.incomingPlaceSet[place];
			}

			for(let place in transition.outgoingPlaceSet){
				transition.outgoingPlaceSet[label + ':' + place] = transition.outgoingPlaceSet[place];
				delete transition.outgoingPlaceSet[place];
			}

			transition.locationSet[label + ':' + this.id] = true;
			delete transition.locationSet[this.id];

			delete this.transitionMap[id];
		}

		for(let action in this.labelSets){
			const set = this.labelSets[action];
			this.labelSets[label + '.' + action] = set;
			delete this.labelSets[action];
		}
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

			if(place.metaData.startPlace !== undefined){
				this.addRoot(place.id);
			}
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
		// use convert function to clone this petri net
		const json = JSON.parse(JSON.stringify(this));
		return this.convert(json);
	},

	trim: function(){
		let places = this.places.filter(p => p.incomingTransitions.length === 0 && p.metaData.startPlace === undefined);
		while(places.length !== 0){
			for(let i = 0; i < places.length; i++){
				const place = places[i];
				const outgoing = place.outgoingTransitions.map(id => this.getTransition(id));
				this.removePlace(place.id);

				// remove any transitions that have become uncreachable from the current place
				for(let j = 0; j < outgoing.length; j++){
					const transition = outgoing[j];
					if(transition.incomingPlaces.length === 0){
						this.removeTransition(transition.id);
					}
					if(transition.outgoingPlaces.length === 0){
						this.removeTransition(transition.id);
					}
				}
			}

			places = this.places.filter(p => p.incomingTransitions.length === 0 && p.metaData.startPlace === undefined);
		}		
	},

	get nextPlaceId(){
		let id;
		while(true){
			id = this.id + '.p' + this.placeId++;

			// check if this is a unique id
			if(this.placeMap[id] === undefined){
				break;
			}
		}

		return id;
	},

	get nextTransitionId(){
		let id;
		while(true){
			id = this.id + '.t' + this.transitionId++;

			// check if this is a unique id
			if(this.transitionMap[id] === undefined){
				break;
			}
		}

		return id;
	},

	constructTerminals: function(){
		const places = this.places;
		for(let i = 0; i < places.length; i++){
			const place = places[i];

			if(place.outgoingTransitions.length === 0){
				// check if an incoming transition is a deadlock
				const incoming = place.incomingTransitions.map(id => this.getTransition(id)).filter(t => t.label === DELTA);
				if(incoming.length === 1){
					place.metaData.isTerminal = 'error';
				}
				else{
					place.metaData.isTerminal = 'stop';
				}
			}
		}
	},

	// Global Functions

	convert: function(net){
		// set up the object properties that petri nets, places and transitions should have at minimum
		const netProperties = ['id', 'rootIds', 'placeMap', 'placeCount', 'transitionMap', 'labelSets', 'transitionCount', 'metaData'];
		const placeProperties = ['id', 'incomingTransitionSet', 'outgoingTransitionSet', 'locationSet', 'metaData'];
		const transitionProperties = ['id', 'label', 'incomingPlaceSet', 'outgoingPlaceSet', 'locationSet', 'metaData'];

		// check if the parametised net has the correct properties
		for(let i = 0; i < netProperties.length; i++){
			const property = netProperties[i];
			if(!net.hasOwnProperty(property)){
				const message = 'Petri net JSON object should have property \'' + property + '\'';
				throw new PetriNetException(message);
			}
		}

		// check that all the places in the petri net have the correct properties
		for(let id in net.placeMap){
			const p = net.placeMap[id];
			for(let i = 0; i < placeProperties.length; i++){
				const property = placeProperties[i];
				if(!p.hasOwnProperty(property)){
					const message = 'Place JSON object should have property \'' + property + '\'';
					throw new PetriNetException(message);
				}
			}

			// construct a place object
			net.placeMap[id] = new PetriNetPlace(p.id, p.incomingTransitionSet, p.outgoingTransitionSet, p.locationSet, p.metaData);
		}

		const labelSets = {};

		// check that all the transitions in the petri net have to correct properties
		for(let id in net.transitionMap){
			const t = net.transitionMap[id];

			for(let i = 0; i < transitionProperties.length; i++){
				const property = transitionProperties[i];
				if(!t.hasOwnProperty(property)){
					const message = 'Transition JSON object should have property \'' + property + '\'';
					throw new PetriNetException(message);
				}
			}

			// construct a new transition object
			const transition = new PetriNetTransition(t.id, t.label, t.incomingPlaceSet, t.outgoingPlaceSet, t.locationSet, t.metaData);

			// add transition to the label set
			if(labelSets[t.label] === undefined){
				labelSets[t.label] = [];
			}
			labelSets[t.label].push(transition);

			// add transition to the map
			net.transitionMap[id] = transition;
		}

		return new PetriNet(net.id, net.rootIds, net.placeMap, net.placeCount, net.transitionMap, labelSets, net.metaData);
	}
}

function PetriNet(id, rootIds, placeMap, placeCount, transitionMap, labelSets, transitionCount, metaData){
	this.type = 'petrinet';
	this.id = id;
	this.rootIds = (rootIds !== undefined) ? rootIds : {};
	this.placeMap = (placeMap !== undefined) ? placeMap : {};
	this.placeCount = (placeCount !== undefined) ? placeCount : 0;
	this.transitionMap = (transitionMap !== undefined) ? transitionMap : {};
	this.labelSets = (labelSets !== undefined) ? labelSets : {};
	this.transitionCount = (transitionCount !== undefined) ? transitionCount : 0;
	this.placeId = 0;
	this.transitionId = 0;
	this.metaData = (metaData !== undefined) ? metaData : {};
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
		if(this.incomingTransitionSet[id] === undefined){
			this.incomingTransitionSet[id] = 0;
		}

		this.incomingTransitionSet[id]++;
	},

	removeIncomingTransition: function(id){
		if(this.incomingTransitionSet[id] !== undefined){
			this.incomingTransitionSet[id]--;

			if(this.incomingTransitionSet[id] === 0){
				delete this.incomingTransitionSet[id];
			}
		}
	},

	get outgoingTransitions(){
		const outgoing = [];
		for(let id in this.outgoingTransitionSet){
			const amount = this.outgoingTransitionSet[id];
			for(let i = 0; i < amount; i++){
				outgoing.push(id);
			}
		}

		return outgoing;
	},

	addOutgoingTransition: function(id){
		if(this.outgoingTransitionSet[id] === undefined){
			this.outgoingTransitionSet[id] = 0;
		}

		this.outgoingTransitionSet[id]++;
	},

	removeOutgoingTransition: function(id){
		if(this.outgoingTransitionSet[id] !== undefined){
			this.outgoingTransitionSet[id]--;

			if(this.outgoingTransitionSet[id] === 0){
				delete this.outgoingTransitionSet[id];
			}
		}
	},

	get locations(){
		return JSON.parse(JSON.stringify(this.locationSet));
	},

	set locations(locations){
		this.locationSet = locations;
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
			for(let i = 0; i < this.incomingPlaceSet[id]; i++){
				incoming.push(id);
			}
		}

		return incoming;
	},

	addIncomingPlace: function(id){
		if(this.incomingPlaceSet[id] === undefined){
			this.incomingPlaceSet[id] = 0;
		}

		this.incomingPlaceSet[id]++;
	},

	removeIncomingPlace: function(id){
		if(this.incomingPlaceSet[id] !== undefined){
			this.incomingPlaceSet[id]--;

			if(this.incomingPlaceSet[id] === 0){
				delete this.incomingPlaceSet[id];
			}
		}
	},

	get outgoingPlaces(){
		const outgoing = [];
		for(let id in this.outgoingPlaceSet){
			for(let i = 0; i < this.outgoingPlaceSet[id]; i++){
				outgoing.push(id);
			}
		}

		return outgoing;
	},

	addOutgoingPlace: function(id){
		if(this.outgoingPlaceSet[id] === undefined){
			this.outgoingPlaceSet[id] = 0;
		}

		this.outgoingPlaceSet[id]++;
	},

	removeOutgoingPlace: function(id){
		if(this.outgoingPlaceSet[id] !== undefined){
			this.outgoingPlaceSet[id]--;

			if(this.outgoingPlaceSet[id] === 0){
				delete this.outgoingPlaceSet[id];
			}
		}
	},

	isSuperSetOf: function(transition){
		for(let id in transition.incomingPlaceSet){
			if(this.incomingPlaceSet[id] === undefined){
				return false;
			}
			else if(this.incomingPlaceSet[id] < transition.incomingPlaceSet[id]){
				return false;
			}
		}

		for(let id in transition.outgoingPlaceSet){
			if(this.outgoingPlaceSet[id] === undefined){
				return false;
			}
			else if(this.outgoingPlaceSet[id] < transition.outgoingPlaceSet[id]){
				return false;
			}
		}

		return true;
	},

	get locations(){
		return JSON.parse(JSON.stringify(this.locationSet));
	},

	set locations(locations){
		this.locationSet = locations;
	},

	addLocation: function(id){
		this.locationSet[id] = true;
	},

	removeLocation: function(){
		delete this.locationSet[id];
	},

	get metaDataSet(){
		return JSON.parse(JSON.stringify(this.metaData));
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

function PetriNetException(message){
	this.message = message;
	this.toString = function(){
		return 'PetriNetException: ' + this.message;
	}
}
