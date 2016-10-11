'use strict'

class PetriNet {

	constructor(id, placeMap, placeCount, transitionMap, labelSets, transitionCount, rootIds, terminalIds, alphabet, nextPlaceId, nextTransitionId){
		// check that id has been defined
		if(id === undefined){
			// throw error
		}

		this._id = id;
		this._placeMap = (placeMap !== undefined) ? placeMap : {};
		this._placeCount = (placeCount !== undefined) ? placeCount : 0;
		this._transitionMap = (transitionMap !== undefined) ? transitionMap : {};
		this._labelSets = (labelSets !== undefined) ? labelSets : {};
		this._transitionCount = (transitionCount !== undefined) ? transitionCount : 0;
		this._rootIds = (rootIds !== undefined) ? rootIds : {};
		this._terminalIds = (terminalIds !== undefined) ? terminalIds : [];
		this._alphabet = (alphabet !== undefined) ? alphabet : {};
		this._nextPlaceId = (nextPlaceId !== undefined) ? nextPlaceId : 0;
		this._nextTransitionId = (nextTransitionId !== undefined) ? nextTransitionId : 0;
	}

	/**
	 * Returns the type of this process. Always going to be 'petrinet'.
	 *
	 * @return {string} - type
	 */
	get type(){
		return 'petrinet';
	}

	/**
	 * Returns the unqiue identifier for this petri net.
	 *
	 * @ return {int} - id
	 */
	get id(){
		return this._id;
	}

	/**
	 * Returns an array of the root places for this petri net
	 *
	 * @return {place[]} - an array of places
	 */
	get roots(){
		var roots = [];
		for(var i in this._rootIds){
			roots.push(this._placeMap[i]);
		}

		return roots;
	}

	/**
	 * Adds the specified place id to the array of roots for this
	 * petri net. Only adds the id if it not already located in the
	 * array of root ids.
	 *
	 * @param {string} id - the place id to add
	 * @return {boolean} - true if id added, otherwise false
	 */
	addRoot(id){
		// check if root is already in roots array
		if(this._rootIds[id] !== undefined){
			return false;
		}

		this._rootIds[id] = true;
		this._placeMap[id].addMetaData('startPlace', 1);
		return true;
	}

	removeRoot(id){
		delete this._rootIds[id];
		//this._placeMap[id].deleteMetaData('startPlace');
	}

	/**
	 * Returns an array of the terminal places for this petri net
	 *
	 * @return {place[]} - an array of places
	 */
	get terminals(){
		var terminals = [];
		for(var i = 0; i < this._terminalIds.length; i++){
			terminals.push(this._terminalIds[i]);
		}

		return terminals;
	}

	/**
	 * Adds the specified place id to the array of terminals for this
	 * Petri net. Only adds the id if it is not already located in the
	 * array of terminal ids.
	 *
	 * @param {string} id - the place id to add
	 * @return {boolean} - true if id added, otherwise false
	 */
	addTerminal(id){
		// check if terminal is already in roots array
		for(var i = 0; i < this._terminalIds; i++){
			if(id === this._terminalIds[i]){
				return false;
			}
		}

		// add id to terminals array
		this._terminalIds.push(id);
		return true;
	}

	/**
	 * Returns an array of the places associated with this petri net.
	 *
	 * @param {place[]} - an array of places
	 */
	get places(){
		var places = [];
		for(var id in this._placeMap){
			places.push(this._placeMap[id]);
		}

		return places;
	}

	/**
	 * Returns the place in this Petri net with the specified id.
	 *
	 * @param {string} id - the place id
	 * @return {place} - place
	 */
	getPlace(id){
		if(this._placeMap[id] !== undefined){
			return this._placeMap[id];
		}

		// throw error
	}

	/**
	 * Constructs an adds a new place to this petri net. The place is guaranteed
	 * to have a unique identifier. Returns the constructed place.
	 *
	 * @param {string} id - the id for the constructed place
	 * @return {place} - the constructed place
	 */
	addPlace(id, metaData){
		var id = (id !== undefined) ? id : this.nextPlaceId;
		var metaData = (metaData !== undefined) ? metaData : {};
		var place = new PetriNet.Place(id, metaData);
		this._placeMap[id] = place;
		this._placeCount++;
		return place;
	}

	removePlace(id){
		if(this._placeMap[id] !== undefined){
			var incoming = this._placeMap[id].incomingTransitions.map(id => this.getTransition(id));
			for(var i = 0; i < incoming.length; i++){
				incoming[i].deleteOutgoingPlace(this._placeMap[id]);
			}
			var outgoing = this._placeMap[id].outgoingTransitions.map(id => this.getTransition(id));
			for(var i = 0; i < outgoing.length; i++){
				outgoing[i].deleteIncomingPlace(this._placeMap[id]);
			}

			this._placeCount--;
			delete this._placeMap[id];
		}
	}

	combinePlaces(place1, place2){
		var place = this.addPlace();

		// get the incoming and outgoing transitions from the places
		var transitions1 = place1.outgoingTransitions.map(x => this.getTransition(x));
		var transitions2 = place2.outgoingTransitions.map(x => this.getTransition(x));
		for(var i = 0; i < transitions1.length; i++){
			place.addOutgoingTransition(transitions1[i].id);
			transitions1[i].addIncomingPlace(place);
		}
		for(var i = 0; i < transitions2.length; i++){
			place.addOutgoingTransition(transitions2[i].id);
			transitions2[i].addIncomingPlace(place);
		}
		transitions1 = place1.incomingTransitions.map(x => this.getTransition(x));
		transitions2 = place2.incomingTransitions.map(x => this.getTransition(x));
		for(var i = 0; i < transitions1.length; i++){
			place.addIncomingTransition(transitions1[i].id);
			transitions1[i].addOutgoingPlace(place);
		}
		for(var i = 0; i < transitions2.length; i++){
			place.addIncomingTransition(transitions2[i].id);
			transitions2[i].addOutgoingPlace(place);
		}

		// check if either of the two places are start place
		if(place1.getMetaData('startPlace') !== undefined){
			this.removeRoot(place1.id);
		}
		if(place2.getMetaData('startPlace') !== undefined){
			this.removeRoot(place2.id);
		}

		// merge meta data of places
		for(var key in place1.metaData){
			place.addMetaData(key, place1.getMetaData(key));
			if(key === 'startPlace'){
				this.addRoot(place.id);
			}
		}
		for(var key in place2.metaData){
			if(key === 'startPlace' && place.getMetaData(key) !== undefined){
				var tokens = place.getMetaData(key) + place2.getMetaData(key);
				place.addMetaData(key, tokens);
			}
			else{
				place.addMetaData(key, place2.getMetaData(key));
				if(key === 'startPlace'){
					this.addRoot(place.id);
				}
			}
		}
		return place;
	}

	/**
	 * Returns an array of the transitions associated with this petri net.
	 *
	 * @return {transition[]} - an array of transitions
	 */
	get transitions(){
		var transitions = [];
		for(var id in this._transitionMap){
			transitions.push(this._transitionMap[id]);
		}

		return transitions;
	}

	/**
	 * Returns the transition in this Petri net with the specified id.
	 *
	 * @param {string} id - the transition id
	 * @return {transition} - transition
	 */
	getTransition(id){
		if(this._transitionMap[id] !== undefined){
			return this._transitionMap[id];
		}

		// throw error
	}

	/**
	 * Returns the label sets stored in this Petri net. A label set is a mapping from
	 * a label to a set of transitions which contain that label.
	 *
	 * @return{string -> transition{}} - the label sets
	 */
	get labelSets(){
		var labelSets = {};
		for(var label in this._labelSets){
			labelSets[label] = this._labelSets[label];
		}

		return labelSets;
	}
 	
 	/**
 	 * Constructs and adds a new transition to this petri net, transitioning from
 	 * the specified place to a newly constructed place proceeding the new transition.
 	 * The transition is guaranteed to have a unique identifier. Returns the newly 
 	 * constructed place that the new transition transitions to.
 	 *
 	 * @param {string} label - the label the transition represents
 	 * @param {place} from - the place the transition transitions from
 	 * @return {place} - the place the transition transitions to
 	 */
	addTransition(id, label, incoming, outgoing, metaData){
		var metaData = (metaData !== undefined) ? metaData : {};

		var transition = new PetriNet.Transition(id, [this._id], label);
		
		// add outgoing and incoming places to the transition
		for(var i = 0; i < incoming.length; i++){
			transition.addIncomingPlace(incoming[i]);
			incoming[i].addOutgoingTransition(id);
		}

		if(outgoing !== undefined){
			for(var i = 0; i < outgoing.length; i++){
				transition.addOutgoingPlace(outgoing[i]);
				outgoing[i].addIncomingTransition(id);
			}
		}

		// add transition to transition map and label sets
		this._transitionMap[id] = transition;

		if(this._labelSets[label] !== undefined){
			this._labelSets[label].push(transition);
			this._alphabet[label]++;
		}
		else{
			this._labelSets[label] = [transition];
			this._alphabet[label] = 1;
		}

		this._transitionCount++;
		return transition;
	}

	/**
	 * Removes the transition with the specified id from this Petri net.
	 *
	 * @param {string} id - the id of the transition to be removed
	 */
	removeTransition(id){
		if(this._transitionMap[id] !== undefined){
			// remove from labelSets
			var transitions = this._labelSets[this._transitionMap[id].label];
			for(var i = 0; i < transitions.length; i++){
				if(transitions[i].id === id){
					this._labelSets[transitions[i].label].splice(i, 1);
				}
			}

			var incoming = this._transitionMap[id].incomingPlaces;
			for(var i = 0; i < incoming.length; i++){
				incoming[i].deleteOutgoingTransitions(id);
				if(incoming[i].outgoingTransitions.length === 0){
					incoming[i].addMetaData('isTerminal', 'stop');
				}
			}
			var outgoing = this._transitionMap[id].outgoingPlaces;
			for(var i = 0; i < outgoing.length; i++){
				outgoing[i].deleteIncomingTransitions(id);
			}

			// remove from transition map
			delete this._transitionMap[id];
			this._transitionCount--;
		}
	}

	get alphabet(){
		return JSON.parse(JSON.stringify(this._alphabet));
	}

	/** 
	 * Relabels the specified transition label with a new label.
	 *
	 * @param {string} oldLabel - the old transition label
	 * @param {string} newLabel - the new transition label
	 */
	relabelTransition(oldLabel, newLabel){
		// check any transitions have the specified label
		if(this._labelSets[oldLabel] !== undefined){
			// relabel the individual transitions
			var transitions = this._labelSets[oldLabel];
			for(var i = 0; i < transitions.length; i++){
				transitions[i].label = newLabel;
			}

			// check if the new label is already defined in this petri net
			if(this._labelSets[newLabel] !== undefined){
				this._labelSets[newLabel].concat(this._labelSets[oldLabel]);
			}
			else{
				this._labelSets[newLabel] = this._labelSets[oldLabel];
			}

			// update transition labels
			for(var i = 0; i < this._labelSets[oldLabel].length; i++){
				this._labelSets[oldLabel][i].label = newLabel;
			}

			// remove the old label from the petri net
			delete this._labelSets[oldLabel];
		}
	}

	/**
	 * Merges the specified array of places into each place specified in
	 * the mergeTo array.
	 *
	 * @param {place[]} mergeTo - array of places to merge to
	 * @param {place[]} places - array of places to merge
	 */
	mergePlaces(mergeTo, places){
		for(var i = 0; i < mergeTo.length; i++){
			var place = mergeTo[i];
			for(var j = 0; j < places.length; j++){
				var current = places[j];

				// check that current place is in this petri net
				if(this._placeMap[current.id] === undefined){
					// throw error
				}
				// update references to outgoing transitions
				var outgoing = current.outgoingTransitions;
				for(var k = 0; k < outgoing.length; k++){
					var transition = this._transitionMap[outgoing[k]];
					transition.deleteIncomingPlace(current);
					transition.addIncomingPlace(place);
					place.addOutgoingTransition(transition.id);
				}

				// update references to incoming transitions
				var incoming = current.incomingTransitions;
				for(var k = 0; k < incoming.length; k++){
					var transition = this._transitionMap[incoming[k]];
					transition.deleteOutgoingPlace(current);
					transition.addOutgoingPlace(place);
					place.addIncomingTransition(transition.id);
				}
			}
		}

		// delete merged place from petri net
		for(var i = 0; i < places.length; i++){
			delete this._placeMap[places[i].id];
			this._placeCount--;
		}
	}

	/**
	 * Adds the places and transitions from the specified petri net into
	 * this petri net. The petri net to add is assumed to be a clone. If
	 * an array of merge places is specified then the roots of the specified
	 * graph will be merged with those places.
	 *
	 * @param {petrinet} net - the petri net to add
	 * @param {place[]} mergeTo - places to merge to (optional)
	 */
	addPetriNet(net, mergeTo){
		// add places from net into this petri net
		var places = net.places;
		var roots = [];
		for(var i = 0; i < places.length; i++){
			var place = places[i];
			this._placeMap[place.id] = place;
			
			// check if this place is a start place
			if(place.getMetaData('startPlace') !== undefined){
				this.addRoot(place.id);
				roots.push(place);
			}

			// check if this place is a terminal
			if(place.getMetaData('isTerminal') !== undefined){
				this.addTerminal(place);
			}

			this._placeCount++;
		}

		// add transitions from net into this petri net
		var transitions = net.transitions;
		for(var i = 0; i < transitions.length; i++){
			var transition = transitions[i];
			this._transitionMap[transition.id] = transition;

			if(this._labelSets[transition.label] !== undefined){
				this._labelSets[transition.label].push(transition);
				this._alphabet[transition.label]++;
			}
			else{
				this._labelSets[transition.label] = [transition];
				this._alphabet[transition.label] = 1;
			}

			this._transitionCount++;
		}

		// merge added petri net to the specified place if necessary
		if(mergeTo !== undefined){
			this.mergePlaces(mergeTo, net.roots);
		}

		return roots;
	}

	clone(label){
		label = (label === undefined) ? '' : label + ':';
		var clone = new PetriNet(label + this._id);
		
		// add places to the clone
		var places = this.places;
		for(var i = 0; i < places.length; i++){
			var place = clone.addPlace(label + places[i].id, places[i].metaData);
			// check if this place is either a start place or terminal
			if(places[i].getMetaData('startPlace') !== undefined){
				clone.addRoot(place.id);
			}
			if(places[i].getMetaData('isTerminal') !== undefined){
				clone.addTerminal(place.id);
			}
		}

		// add transitions to clone
		var transitions = this.transitions;
		for(var i = 0; i < transitions.length; i++){
			// update the references to the incoming and outgoing places
			var incoming = transitions[i].incomingPlaces;
			for(var j = 0; j < incoming.length; j++){
				incoming[j] = clone.getPlace(label + incoming[j].id);
			}
			var outgoing = transitions[i].outgoingPlaces;
			for(var j = 0; j < outgoing.length; j++){
				outgoing[j] = clone.getPlace(label + outgoing[j].id);
			}

			clone.addTransition(label + transitions[i].id, transitions[i].label, incoming, outgoing, transitions[i].metaData);
		}

		return clone;
	}

	/**
	 * Returns the next place id for this Petri net.
	 *
	 * @return {string} - the next place id
	 */
	get nextPlaceId(){
		return this._id + '.p' + this._nextPlaceId++;
	}

	/**
	 * Returns the next transition id for this Petri net.
	 *
	 * @return {string} - the next place id
	 */
	get nextTransitionId(){
		return this._id + '.t' + this._nextTransitionId++;
	}
}

PetriNet.Place = class {

	/**
	 * Constructs a new instance of a place within a PetriNet.
	 */
	constructor(id, metaData){
		// check that id has been defined
		if(id === undefined){
			// throw error
		}

		this._id = id;
		this._outgoingTransitions = [];
		this._incomingTransitions = [];
		this._metaData = (metaData !== undefined) ? metaData : {};
	}

	/**
	 * Returns the fact that this object is a place.
	 *
	 * @return {string} - type
	 */
	get type(){
		return 'place';
	}

	/**
	 * Returns the unique identifier for this place.
	 *
	 * @return {string} - place id
	 */
	get id(){
		return this._id;
	}

	/**
	 * Returns an array of unique identifiers for the transitions that
	 * this place transitions to.
	 *
	 * @return {int[]} - array of transition ids
	 */
	get outgoingTransitions(){
		return this._outgoingTransitions.slice(0);
	}

	/**
	 * Adds the specified transition id to the array of unique identifiers
	 * for the transitions that this place transitions to. Only successfully
	 * adds the id if the id is not already located in the array.
	 *
	 * @param {int} id - the transition id to add
	 * @return {boolean} - true if id added, otherwise false
	 */
	addOutgoingTransition(id){
		// check if transition id is already located in the array
		for(var i = 0; i < this._outgoingTransitions.length; i++){
			if(id === this._outgoingTransitions[i]){
				return false;
			}
		}

		// add id to array
		this._outgoingTransitions.push(id);
		return true;
	}

	/**
	 * Deletes the specified transition id to the array of unique identifiers
	 * for the transitions that this place transitions to. Only successfully
	 * deletes the id if the id is not already located in the array.
	 *
	 * @param {int} id - the transition id to add
	 * @return {boolean} - true if id added, otherwise false
	 */
	deleteOutgoingTransitions(id){
		// check if transition id is located in the array
		for(var i = 0; i < this._outgoingTransitions.length; i++){
			if(id === this._outgoingTransitions[i]){
				this._outgoingTransitions.splice(i, 1);
				return true;
			}
		}

		// id not found in array
		return false;
	}

	/**
	 * Returns an array of unique identifiers for the transitions that
	 * transition to this place.
	 *
	 * @return {int[]} - array of transition ids
	 */
	get incomingTransitions(){
		return this._incomingTransitions.slice(0);
	}

	/**
	 * Adds the specified transition id to the array of unique identifiers
	 * for the transitions that transition to this place. Only successfully
	 * adds the id if the id is not already located in the array.
	 *
	 * @param {int} id - the transition id to add
	 * @return {boolean} - true if id added, otherwise false
	 */
	addIncomingTransition(id){
		// check if transition id is already located in the array
		for(var i = 0; i < this._incomingTransitions.length; i++){
			if(id === this._incomingTransitions[i]){
				return false;
			}
		}

		// add id to array
		this._incomingTransitions.push(id);
		return true;
	}

	/**
	 * Deletes the specified transition id to the array of unique identifiers
	 * for the transitions that transition to this place. Only successfully
	 * deletes the id if the id is not already located in the array.
	 *
	 * @param {int} id - the transition id to add
	 * @return {boolean} - true if id added, otherwise false
	 */
	deleteIncomingTransitions(id){
		// check if transition id is located in the array
		for(var i = 0; i < this._incomingTransitions.length; i++){
			if(id === this._incomingTransitions[i]){
				this._incomingTransitions.splice(i, 1);
				return true;
			}
		}

		// id not found in array
		return false;
	}

	/**
	 * Returns true if this place is unreachable, otherwise returns false.
	 *
	 * @return {boolean} - whether or not this place is unreachable
	 */
	get isUnreachable(){
		return this._incomingTransitions.length === 0 && !this._metaData['startPlace']
	}

	/**
	 * Returns true if this place has no transitions from it, otherwise returns false. Note
	 * that this is different to the 'isTerminal' value stored in this place's meta data.
	 *
	 * @return {boolean} - whether or not this place is terminal
	 */
	get isTerminal(){
		return this._outgoingTransitions.length === 0;
	}

	get metaData(){
		return JSON.parse(JSON.stringify(this._metaData));
	}

	/**
	 * Returns the value associated with the specified key in the meta data
	 * for this place or undefined if no value is stored with that key.
	 *
	 * @param {string} key - the key
	 * @return {value} - the value to return (potentially undefined)
	 */
	getMetaData(key){
		return this._metaData[key];
	}

	/**
	 * Adds the specified key-value mapping to the meta data for this place.
	 *
	 * @param {string} key - the key to add
	 * @param {value} value - the value to add
	 */
	addMetaData(key, value){
		this._metaData[key] = value;
	}

	/**
	 * Deletes the key-value mapping for the specified key from the meta data
	 * for this place.
	 *
	 * @param {string} key - the key to delete
	 */
	deleteMetaData(key){
		delete this._metaData[key];
	}
}

PetriNet.Transition = class {

	constructor(id, processIds, label, metaData){
		// check that global ids array is defined
		if(id === undefined){
			// throw error
		}
		if(processIds === undefined){
			// throw error
		}

		if(label === undefined){
			// throw error
		}

		this._id = id;
		this._processIds = processIds;
		this._label = label;
		this._outgoingPlaces = {};
		this._incomingPlaces = {};
		this._metaData = (metaData !== undefined) ? metaData : {}; 
	}

	/**
	 * Returns the fact that this object is a transition.
	 *
	 * @return {string} - type
	 */
	get type(){
		return 'transition';
	}

	/**
	 * Returns the unique identifier for this transition.
	 *
	 * @return {string} - id
	 */
	get id(){
		return this._id;
	}

	/**
	 * Returns the array of global ids associated with this transition.
	 *
	 * @return {int[]} - array of process ids
	 */
	get processIds(){
		return this._processIds.sort();
	}

	/**
	 * Adds the specified process id to the array of process ids associated
	 * with this transition. Will not add the process id if it is a
	 * duplicate.
	 *
	 * @param {int} id - the process id
	 * @return {boolean} - true if id is added, otherwise false
	 */
	addProcessId(id){
		// check that process is not already present in array
		for(var i = 0; i < this._processIds.length; i++){
			if(id === this._processIds[i]){
				return false;
			}
		}

		// add id to array of process ids
		this._processIds.push(id);
		return true;
	}

	/**
	 * Deletes the specified id from the array of process ids associated with
	 * this transition. The array of process ids remains unchanged if the
	 * specified id is not located in the array.
	 *
	 * @param {int} id - the process id
	 * @return {boolean} - true if id is deleted, otherwise false
	 */
	deleteProcessId(id){
		// chek that global id is present in array
		for(var i = 0; i < this._processIds.length; i++){
			if(id === this._processIds[i]){
				this._processIds = processIds.splice(i, 1);
				return true;
			}
		}

		// id not located
		return false;
	}

	/**
	 * Returns the label associated with this transition.
	 *
	 * @return {string} - transition label
	 */
	get label(){
		return this._label;
	}

	/**
	 * Sets the label associated with this transition to the specified
	 * label.
	 *
	 * @param {string} label - the new label
	 * @return {string} - the new label
	 */
	set label(label){
		this._label = label;
		return this._label;
	}

	/**
	 * Returns an array of the places that this transition transitions to.
	 *
	 * @return {place[]} - array of places
	 */
	get outgoingPlaces(){
		var places = [];
		for(var id in this._outgoingPlaces){
			places.push(this._outgoingPlaces[id]);
		}

		return places;
	}

	/**
	 * Adds the specifed place to the set of places that this transition
	 * transitions to.
	 *
	 * @param {place} place - the place to add
	 */
	addOutgoingPlace(place){
		this._outgoingPlaces[place.id] = place;
	}

	/**
	 * Deletes the specifed place from the set of places that this transition
	 * transitions to.
	 *
	 * @param {place} place - the place to delete
	 */
	deleteOutgoingPlace(place){
		delete this._outgoingPlaces[place.id];
	}

	/**
	 * Returns an array of the places that transition to this transition.
	 *
	 * @return {place[]} - array of places
	 */
	get incomingPlaces(){
		var places = [];
		for(var id in this._incomingPlaces){
			places.push(this._incomingPlaces[id]);
		}

		return places;
	}

	/**
	 * Adds the specified place to the set of places that transition to this
	 * transition.
	 *
	 * @param {place} place - the place to add
	 */
	addIncomingPlace(place){
		this._incomingPlaces[place.id] = place;
	}

	/**
	 * Deletes the specifed place from the set of places that transition to
	 * this transition.
	 *
	 * @param {place} place - the place to delete
	 */
	deleteIncomingPlace(place){
		delete this._incomingPlaces[place.id];
	}

	get metaData(){
		return JSON.parse(JSON.stringify(this._metaData));
	}

	/**
	 * Returns the value associated with the specified key in the meta data
	 * for this place or undefined if no value is stored with that key.
	 *
	 * @param {string} key - the key
	 * @return {value} - the value to return (potentially undefined)
	 */
	getMetaData(key){
		return this._metaData[key];
	}

	/**
	 * Adds the specified key-value mapping to the meta data for this place.
	 *
	 * @param {string} key - the key to add
	 * @param {value} value - the value to add
	 */
	addMetaData(key, value){
		this._metaData[key] = value;
	}

	/**
	 * Deletes the key-value mapping for the specified key from the meta data
	 * for this place.
	 *
	 * @param {string} key - the key to delete
	 */
	deleteMetaData(key){
		delete this._metaData[key];
	}
}