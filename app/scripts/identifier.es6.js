'use strict';

/**
 * This class represents a unique identifier for a component within
 * a process in the program. An Identifier consists of two different types
 * of identification:
 * 
 * Local - A local identifier is one which provides a unqiue id for within
 * a process. These are unqiue within a process, however there may be components
 * c1 and c2 within process p1 and p2 respectively such that c1.local === c2.local
 *
 * Global - A global identifier is a set of identifiers which the process component
 * can belong to. A component can belong to more than one process in cases where two
 * processes are parallely composed.
 */
class Identifier {
	
	/**
	 * Constructs a new instance of an Identifier.
	 */
	constructor(localId, globalIds){
		// check that parameters have been defined
		if(localId === undefined){
			// throw error
		}
		if(globalIds === undefined){
			// throw error
		}

		this._type = 'identifier';
		this._localId = localId;
		this._globalIds = globalIds;
	}

	/**
	 * Returns the fact that this object is an Identifier
	 *
	 * @return {string} - the type of object
	 */
	 get type(){
	 	return this._type;
	 }

	/**
	 * Returns the local id associated with this Identifier.
	 *
	 * @return {int} - local id
	 */
	get localId(){
		return this._localId;
	}

	/**
	 * Sets the value of the local id associated with this Identifier
	 * to the specified value.
	 *
	 * @oaram {int} id - the new local id
	 * @return {int} - the new local id
	 */
	set localId(id){
		this._localId = id;
		return this._localId;
	}

	/**
	 * Returns the set of global ids associated with this identifier
	 * in numerical order.
	 *
	 * @return {int[]} - global ids
	 */
	get globalIds(){
		var ids = this._globalIds;
		return ids.sort();
	}

	/**
	 * Adds the specified id to the set of global ids and returns a
	 * confirmation that the id was added. A positive comfirmation means
	 * that id was added, a negative conformation means that it was not
	 * added.
	 *
	 * @param {int} id - the global id to add
	 * @return {boolean} - true if id was added, otherwise false
	 */
	addGlobalId(id){
		// check that id is not already a global id
		for(var i = 0; i < this._globalIds.length; i++){
			if(id === this._globalIds[i]){
				return false;
			}
		}

		// add id to global ids
		this._globalIds.push(id);
		return true;
	}

	/**
	 * Deletes the specified id to the set of global ids and returns a
	 * confirmation that the id was deleted. A positive comfirmation means
	 * that id was deleted, a negative confirmation means that id was not part
	 * of the set.

	 * @param {int} id - the global id to delete
	 * @return {boolean} - true if id was deleted, otherwise false
	 */
	deleteGlobalid(id){
		// check if id is in global ids
		for(var i = 0; i < this._globalIds.length; i++){
			if(id === this._globalIds[i]){
				this._globalIds.splice(i, 1);
				return true;
			}
		}

		// id was not in global id set
		return false;
	}

	/**
	 * Returns true if the specified Identifier is equivalent to this Identifier.
	 * Identifiers are considered equivalent if both local ids are identical and
	 * if both sets of global ids are identical.
	 *
	 * @param {identifier} identifer - the identifer to compare with
	 * @return {boolean} - true if identifiers are equivalent, otherwise false
	 */
	equals(identifier){
		// check that identifier is defined
		if(identifier === undefined || identifier === null){
			return false;
		}

		// check that identifier is an Identifier
		if(identifier.type !== 'identifier'){
			return false;
		}

		// check that local ids match
		if(this._localId !== identifier.localId){
			return false;
		}

		// check that global ids match
		var ids1 = this.globalIds;
		var ids2 = identifier.globalIds;
		if(ids1.length !== ids2.length){
			return false;
		}
		for(var i = 0; i < ids1.length; i++){
			// ids are guaranteed to be in numerical order
			if(ids1[i] !== ids2[i]){
				return false;
			}
		}

		return true;
	}
}