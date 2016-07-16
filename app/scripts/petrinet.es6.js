'use strict'

class PetriNet {

	constructor(id, placeMap, placeCount, transitionMap, labelSets, transitionCount, rootIds, nextPlaceId, nextTransitionId){
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
		this._rootIds = (rootIds !== undefined) ? rootIds : [];
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
	 * @ return {place[]} - array of places
	 */
	get roots(){
		var roots = [];
		for(var i = 0; i < this._rootIds.length; i++){
			roots.push(this._placeMap[this._rootIds[i]]);
		}

		return roots;
	}

	/**
	 * Adds the specified place id to the array of roots for this
	 * petri net. Only adds the id if it not already located in the
	 * array of root ids.
	 *
	 * @param {int} id - the place id to add
	 * @return {boolean} - true if id added, otherwise false
	 */
	addRoot(id){
		// check if root is already in roots array
		for(var i = 0; i < this._rootIds; i++){
			if(id === this._rootIds[i]){
				return false;
			}
		}

		// add id to roots array
		this._rootIds.push(id);
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
	 * Constructs an adds a new place to this petri net. The place is guaranteed
	 * to have a unique identifier. Returns the constructed place.
	 *
	 * @return {place} - the constructed place
	 */
	addPlace(){
		var id = this._id + '.' + this._nextPlaceId++;
		var place = new PetriNet.Place(id);
		this._placeMap[id] = place;
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
	 * Returns a list of label set objects that are associated with this
	 * petri net.
	 *
	 * @return {labelset[]} - an array of label sets
	 */
	get labelSets(){
		var labelSets = [];
		for(var label in this._labelSets){
			labelSets.push({ label:label, transitions:this._labelSets[label] });
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
	addTransition(label, from){
		var id = this._id + '.' + this._nextTransitionId++;
		var transition = new PetriNet.Transition(id, [this._id], label);
		transition.addIncomingPlace(from);
		from.addOutgoingTransition(id);
		var to = this.addPlace();
		transition.addOutgoingPlace(to);
		to.addIncomingTransition(id);
		this._addTransition(label, transition);
		return to;
	}

	/**
	 * Private helper function for functions that add a transition to this
	 * petri net. Handles adding the specified transition to the transition map
	 * as well as adding it to the correct labels set. Should not be called
	 * from outside this class.
	 *
	 * @param {string} label - the label the transition represents
	 * @param {transition} transition - the transition to add
	 */
	_addTransition(label, transition){
		// add transition to transition map
		this._transitionMap[transition.id] = transition;

		// check if label already exists in label sets
		if(this._labelSets[label] !== undefined){
			this._labelSets[label].push(transition);
		}
		else{
			this._labelSets[label] = [transition];
		}		
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
			// check if the new label is already defined in this petri net
			if(this._labelSets[newLabel] !== undefined){
				this._labelSets[newLabel].concat(this._labelSets[oldLabel]);
			}
			else{
				this._labelSets[newLabel] = this._labelSets[oldLabel];
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
		for(var i = 0; i < places.length; i++){
			this._placeMap[places[i].id] = places[i];
			this._placeCount++;
		}

		// add transitions from net into this petri net
		var labelSets = net.labelSets;
		for(var i = 0; i < labelSets.length; i++){
			var label = labelSets[i].label;
			var transitions = labelSets[i].transitions;
			for(var j = 0; j < transitions.length; j++){
				this._addTransition(label, transitions[j]);
				this._transitionCount++;
			}
		}

		// merge added petri net to the specified place if necessary
		if(mergeTo !== undefined){
			this.mergePlaces(mergeTo, net.roots);
		}
	}

	get clone(){
		// clone the places in this petri net
		var placeMap = {};
		for(var id in this._placeMap){
			var place = this._placeMap[id];
			var clone = new PetriNet.Place(
				place.id,
				place.outgoingTransitions,
				place.incomingTransitions,
				place.metaData
			);
			placeMap[place.id] = place;
		}

		// clone the transitions in this petrinet
		var labelSets = {};
		var transitionMap = {};
		for(var label in this._labelSets){
			var transitions = this._labelSets[label];
			for(var i = 0; i < transitions.length; i++){
				var transition = transitions[i];
				var outgoingPlaces = {};
				var outgoing = transition.outgoingPlaces;
				for(var j = 0; j < outgoing.length; j++){
					var id = outgoing[j].id;
					outgoingPlaces[id] = placeMap[id];
				}

				var incomingPlaces = {};
				var incoming = transition.incomingPlaces;
				for(var j = 0; j < incoming.length; j++){
					var id = incoming[j].id;
					incomingPlaces[id] = placeMap[id];
				}

				var clone = new PetriNet.Transition(
					transition.id, 
					transition.processIds,
					transition.label,
					outgoingPlaces,
					incomingPlaces,
					transition.metaData
				);

				transitionMap[clone.id] = clone;
				if(labelSets[label] !== undefined){
					labelSets[label].push(clone);
				}
				else{
					labelSets[label] = [clone];
				}
			}
		}

		return new PetriNet(
			this._id,
			placeMap,
			this._placeCount,
			transitionMap,
			labelSets,
			this._transitionCount,
			this._rootIds,
			this._nextPlaceId,
			this._nextTransitionId
		);
	}
}

PetriNet.Place = class {

	/**
	 * Constructs a new instance of a place within a PetriNet.
	 */
	constructor(id, outgoingTransitions, incomingTransitions, metaData){
		// check that id has been defined
		if(id === undefined){
			// throw error
		}

		this._id = id;
		this._outgoingTransitions = (outgoingTransitions !== undefined) ? outgoingTransitions : [];
		this._incomingTransitions = (incomingTransitions !== undefined) ? incomingTransitions : [];
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
		return this._outgoingTransitions;
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
				this._outgoingTransitions = this._outgoingTransitions.splice(i, 1);
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
		return this._incomingTransitions;
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
		for(var i = 0; i < this._incomngTransitions.length; i++){
			if(id === this._incomingTransitions[i]){
				this._incomingTransitions = this._incomingTransitions.splice(i, 1);
				return true;
			}
		}

		// id not found in array
		return false;
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

	constructor(id, processIds, label, outgoingPlaces, incomingPlaces, metaData){
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
		this._outgoingPlaces = (outgoingPlaces !== undefined) ? outgoingPlaces : {};
		this._incomingPlaces = (incomingPlaces !== undefined) ? incomingPlaces : {};
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