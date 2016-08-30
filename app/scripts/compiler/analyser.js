'use strict';

/**
 * Determines whether updates have been made to any process since the last
 * compilation. If there have been no updates to a process definition then there
 * is no need to re-interpret that process. This function considers two possible
 * types of updates. The first is if there is any updates to the abstract syntax tree
 * for a given process. The second is if any processes that are referenced by the
 * given abstract syntax tree have been updated.
 *
 * Constructs an analysis object for each defined process. An analysis object is of
 * the form:
 *
 * { process:..., references:..., isUpdated:... }
 *
 * Where process is the ast node for that process, references are the other processes
 * that are referenced by the process and isUpdated is a boolean determining whether the 
 * process has been updated since the last compilation
 *
 * @param {astNode[]} processes - the processes from the abstract syntax tree
 * @param {astNode[]} lastAnalysis - the analysis from the last compilation
 * @return {string -> analysis} - a mapping from identifier to analysis object
 */
function performAnalysis(processes, lastAnalysis, abstractionChanged){
	var analysis = {};

	for(var i = 0; i < processes.length; i++){
		var current = JSON.parse(JSON.stringify(processes[i]));
		var references = analyseProcess(current.process);
		var localAnalysis = analyseLocalProcesses(current.local);
		
		// combine local process references
		for(var identifier in localAnalysis.references){
			references[identifier] = true;
		}

		// create an intersection of the defined local processes and the references
		var intersection = constructIntersection(localAnalysis.identifiers, references);

		// remove any local processes that are not referenced
		processes[i].local = updateLocalProcesses(current.local, intersection);

		//
		analysis[processes[i].ident.ident] = { process:processes[i], references:references };
	}

	// check if any updates have happened since the last compilation
	for(var ident in analysis){
		if(lastAnalysis[ident] !== undefined && !abstractionChanged){
			var current = JSON.stringify(analysis[ident].process);
			var previous = JSON.stringify(lastAnalysis[ident].process);
			analysis[ident].isUpdated = (current === previous) ? false : true;
		}
		else{
			analysis[ident].isUpdated = true;
		}
	}

	// check if any updates to referenced processes require a process to be updated
	for(var ident in analysis){
		// no need to check if process has already been updated
		if(!analysis[ident].isUpdated){
			// check if references have been updated
			for(var reference in analysis[ident].references){
				if(analysis[reference] !== undefined && analysis[reference].isUpdated){
					analysis[ident].isUpdated = true;
					break;
				}
			}
		}
	}

	return analysis;

	/**
	 * Descends through the abstract syntax tree starting from the specified
	 * root and removes any position data from the nodes. Also constructs and
	 * returns a set of the processes that are referenced.
	 *
	 * @param {astNode} root - the ast node to start search from
	 * @return {string{}} - a set of identifiers
	 */
	function analyseProcess(root){
		var fringe = [root];
		var references = {};
		while(fringe.length != 0){
			var currentNode = fringe.pop();
			// check if the current node has position data
			if(currentNode.position !== undefined){
				delete currentNode.position;
			}

			// check if the current node is an identifier
			if(currentNode.type !== undefined && currentNode.type === 'identifier'){
				references[currentNode.ident] = true;
				// cannot descend any further so continue
				continue;
			}

			for(var key in currentNode){
				// if key references an object then push to fringe
				if(typeof(currentNode[key]) === 'object'){
					fringe.push(currentNode[key]);
				}
			}
		}

		return references;
	}

	/**
	 * 
	 *
	 * @param {astNode[]} localProcesses - the local processes
	 * @return {object} - a list of identifiers and a set of references
	 */
	function analyseLocalProcesses(localProcesses){
		var identifiers = [];
		var references = {};
		for(var i = 0; i < localProcesses.length; i++){
			var current = localProcesses[i];
			identifiers.push(current.ident.ident);
			var localReferences = analyseProcess(current.process);
			// add the current local references to the set of references
			for(var reference in localReferences){
				references[reference] = true;
			}
		}

		return { identifiers:identifiers, references:references };
	}

	/**
	 * Constructs an intersection of the identifiers in the specified
	 * identifiers array and the specified references set and returns the
	 * result.
	 * 
	 * @param {string[]} identifiers - an array of identifiers
	 * @param {string{}} references - a set of identifiers
	 * @param {string[]} - the identifiers contained in both parameters
	 */
	function constructIntersection(identifiers, references){
		var intersection = [];

		// construct intersection
		for(var i = 0; i < identifiers.length; i++){
			for(var identifier in references){
				if(identifiers[i] === identifier){
					intersection.push(identifier);
				}
			}
		}

		return intersection;
	}

	/**
	 * Removes any unreferenced local processes from that are not
	 * referenced. Returns the update array of local processes.
	 *
	 * @param {astNode[]} localProcesses - the local processes
	 * @param {string[]} references - the referenced processes
	 * @return {astNode[]} - the updated array of local processes
	 */
	function updateLocalProcesses(localProcesses, references){
		var newLocal = [];
		for(var i = 0; i < localProcesses.length; i++){
			for(var j = 0; j < references.length; j++){
				if(localProcesses[i].ident.ident === references[j]){
					newLocal.push(current.local[i]);
					break;
				}
			}
		}

		return newLocal;
	}
}