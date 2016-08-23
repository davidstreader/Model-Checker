'use strict';

function parallelComposition(id, process1, process2){
	// check that processes are of the same type
	if(process1.type !== process2.type){
		// throw error
	}

	var type = process1.type;
	if(type === 'automata'){
		return automataParallelComposition(id, process1, process2);
	}
	else if(type === 'petrinet'){
		return petriNetParallelComposition(id, process1, process2);
	}
	else{
		// throw error
	}

	/**
	 * The following are helper functions that can be utilised by parallel
	 * composition functions.
	 */

	/**
	 * Helper function for parallel composition functions that creates
	 * and returns a union of the data stored in the two alphabet sets.
	 *
	 * @param {string{}} alphabet1 - the first alphabet set
	 * @oaram {string{}} alphabet2 - the second alphabet set
	 * @return {string{}} - the unioned alphabet set
	 */
	function alphabetUnion(alphabet1, alphabet2){
		var alphabet = {};
		// add actions from first alphabet
		for(var action in alphabet1){
			alphabet[action] = true;
		}

		// add actions from second alphabet
		for(var action in alphabet2){
			alphabet[action] = true;
		}

		return alphabet;
	}

	/**
	 * Helper function for parallel composition functions that creates
	 * and returnds an intersection of the data stored in two meta data objects.
	 *
	 * @param {object} metaData1 - first meta data set
	 * @param {object} metaData2 - second meta data set
	 * @return {object} - the intersected meta data
	 */
	function metaDataIntersection(metaData1, metaData2){
		var metaData = {};
		// check if there are any matching keys between the two meta data sets
		for(var key in metaData1){
			// check if there is a match
			if(metaData2[key] !== undefined){
				// check that the value stored is the same for both sets
				if(metaData1[key] === metaData2[key]){
					metaData[key] = metaData1[key];
				}
			}
		}

		return metaData;
	}
}