'use strict';

/**
 * This script contains universal helper functions that can be utilised by
 * any script in the progam.
 */

/**
 * Constructs and returns a union of the two specified alphabet sets
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
 * Constructs and returns an intersection of the data stored in the two
 * specified meta data objects.
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