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
 * @param{Process[]} processes - the processes from the abstract syntax tree
 * @param{Identifier -> Analysis} lastAnalysis - a mapping from identifier
 *		to analysis object from the last compilation
 * @return{Identifier -> Analysis} - a mapping from identifier to analysis object
 */
function performAnalysis(processes, lastAnalysis, abstractionChanged){
	const analysis = {};

	// loop through each process and find references to other processes
	for(let i = 0; i < processes.length; i++){
    if (typeof app !== 'undefined' && app.debug) console.log("Analysing: "+processes[i].ident.ident);
    else processes.socket.emit("analyse",{ident:processes[i].ident.ident,i:i});
		// analyse the main process to find references to other processses
		const process = processes[i];
		const current = JSON.parse(JSON.stringify(process));
		const references = {};
		analyseNode(current.process, references);

		// analyse local processes to find references to other processes
		const localReferences = {};
		for(let j = 0; j < current.local.length; j++){
			analyseNode(current.local[j].process, localReferences);
		}

		// combine references from the main process and local processes
		for(let ident in localReferences){
			references[ident] = true;
		}

		// remove any local processes that are not referenced
		const localProcesses = [];
		for(let j = 0; j < process.local.length; j++){
			if(references[process.local[j].ident.ident]){
				localProcesses.push(process.local[j]);
			}
		}

		// update the local processes
		process.local = localProcesses;
		current.local = localProcesses;

		// construct an analysis object for this process
		analysis[process.ident.ident] = {
			process:current,
			references:references
		};
	}

	// determine whether each process has been updated since the last compilation
	for(let ident in analysis){
		// check if a process with this identifier had been defined last time
		if(lastAnalysis[ident] !== undefined && !abstractionChanged){
			const current = JSON.stringify(analysis[ident].process);
			const previous = JSON.stringify(lastAnalysis[ident].process);
			// compare asts from compilation and last compilation to see if they match
			analysis[ident].isUpdated = (current === previous) ? false : true;
		}
		else{
			analysis[ident].isUpdated = true;
		}
	}

	// update a process if any processes it references were updated
	for(let ident in analysis){
		// no need to check if process was already updated
		if(!analysis[ident].isUpdated){
			for(let reference in analysis[ident].references){
				// check if the reference has been updated
				if(analysis[reference] !== undefined && analysis[reference].isUpdated){
					analysis[ident].isUpdated = true;
					break;
				}
			}
		}
	}

	return analysis;

	// HELPER FUNCTIONS

	/**
	 * Analyses the specified abstract syntax tree node to determine if it contains
	 * any references to other processes. Each node recursively calls this function
	 * on its children to determine if they contain references. Any references that
	 * are found are stored in the specified references set.
	 *
	 * @param{ASTNode} astNode - the ast node to analyse
	 * @param{identifier{}} - a set of referenced identifiers
	 */
	function analyseNode(astNode, references){
		switch(astNode.type){
			case 'sequence':
				analyseNode(astNode.to, references);
				break;
			case 'choice':
			case 'composite':
				analyseNode(astNode.process1, references);
				analyseNode(astNode.process2, references);
				break;
			case 'function':
				analyseNode(astNode.process, references);
				break;
			case 'identifier':
				references[astNode.ident] = true;
				break;
			case 'terminal':
			default:
				break;
		}
	}
}
