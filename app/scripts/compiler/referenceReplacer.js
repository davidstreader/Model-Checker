'use strict';

/**
 * Handles the replacement of processes referenced in other processes with the abstract syntax
 * tree for that process. For example with the following processes:
 *
 *   B = (b -> STOP).
 *   A = (a -> B).
 *
 * The A process references the B process. This function replaces the reference to B so that A becomes:
 *
 *   A = (a -> b -> STOP).
 *
 * It also handles self references, where processes reference themselves which constructs a loop back to
 * the root of the process. References are added to the root of a process and a new ASTNode called
 * ReferenceNode is introduced to identify that the procss has to transition to that reference. It is up
 * to the process model interpreters to handle how the reference pointers are stored, as well as looping
 * back to those references when necessary.
 *
 * @param{Process[]} - an array of processes
 * @return{Process[]} - an array of the updated processes
 */
function replaceReferences(processes){

	// fields
	const referenceMap = {}; // a mapping from process identifier to abstract syntax tree of that process
	let localReferenceMap; // a mapping from process identifier to abstract syntax tree of that process
	let referenceId = 0; // the next reference id

	// MAIN PROCESS

	// replace references in each process
	for(let i = 0; i < processes.length; i++){
    if (typeof postMessage !== 'undefined') {
      postMessage({clear:true,message:("Replacing References: "+processes[i].ident.ident+" ("+(i+1)+"/"+processes.length)+")"});
    }
    else processes.socket.emit("replacer",{ident:processes[i].ident.ident,i:i});
		// reset reference map for local processes
		localReferenceMap =  new LocalReferenceMap(processes[i].local);

		const ident = processes[i].ident.ident;
		// construct a mapping from process identifiers to reference ids
		const idMap = {};
		processes[i].process = replaceProcess(processes[i].process, ident, idMap);
		delete processes[i].local;
		// put current process in the reference map
		referenceMap[ident] = processes[i].process;
	}

	return processes;

	// HELPER FUNCTIONS

	/**
	 * Replaces any references in the specified process with the abstract syntax tree
	 * for that reference. Returns the updated abstract syntax tree.
	 *
	 * @param{ASTNode} process - the root ast node for the process
	 * @param{string} ident - the identifier for the specified process
	 * @param{string -> int} idMap - a mapping from identifier to reference id
	 * @return{ASTNode} - the updated process root
	 */
	function replaceProcess(process, ident, idMap){
		idMap[ident] = referenceId++;
		// add a reference to the root of the process ast
		process.reference = idMap[ident];

		// replace any references in the ast for the current process
		return replaceNode(process, ident, idMap);
	}

	/**
	 * Handles the traversal through the abstract syntax tree to find IdentifierNodes and replace
	 * them with the abstract syntax tree of the process they represent. Returns the updated ASTNode.
	 *
	 * @param{ASTNode} astNode - the current ast node
	 * @param{string} ident - the identifier of the process the ast node belongs to
	 * @param{string -> int} idMap - a mapping from identifier to reference id
	 * @return{ASTNode} - the updated ast node
	 */
	function replaceNode(astNode, ident, idMap){
		const type = astNode.type;
		switch(type){
			case 'sequence':
				astNode.to = replaceNode(astNode.to, ident, idMap);
				return astNode;
			case 'choice':
				// choice and composite nodes are structured the same,
				// so choice falls through into the composite case
			case 'composite':
				astNode.process1 = replaceNode(astNode.process1, ident, idMap);
				astNode.process2 = replaceNode(astNode.process2, ident, idMap);
				return astNode;
			case 'function':
				astNode.process = replaceNode(astNode.process, ident, idMap);
				return astNode;
			case 'identifier':
				return replaceIdentifier(astNode, ident, idMap);
			case 'terminal':
				// ignore terminal nodes
				return astNode;
			default:
				console.error("incorrect ast node type");
		}
	}

	/**
	 * Handles the replacement of an identifer ast node with the ast node that reference
	 * represents. Determines whether the reference is a self reference or either a local or
	 * global reference. Returns the root to the abstract syntax tree of the referenced process.
	 *
	 * @param{ASTNode} astNode - the current ast node
	 * @param{string} ident - the identifier of the process the ast node belongs to
	 * @param{string -> int} idMap - a mapping from identifier to reference id
	 * @return{ASTNode} - the updated ast node
	 */
	function replaceIdentifier(astNode, ident, idMap){
		const reference = astNode.ident;
		// check if the reference already exists
		if(idMap[reference] !== undefined){
			return new ReferenceNode(idMap[reference]);
		}
		let process;
		// check if the process is referencing a local or global process
		if(localReferenceMap[reference] !== undefined){
			process = JSON.parse(JSON.stringify(localReferenceMap[reference]));
		}
		else if(referenceMap[reference] !== undefined){
			return astNode;
		}
		// check that the reference was valid
		if(process === undefined){
      throw new ReferenceReplacerException(reference);
		}

		// check whether a reference id has already been defined for the current process
		if(idMap[reference] !== undefined){
			return new ReferenceNode(idMap[reference]);
		}

		// otherwise replace the current ast node with the referenced process
		return replaceProcess(process, reference, idMap);
	}

	// OBJECT CONSTRUCTORS

	/**
	 * Constructs and returns a local reference map, which contains a mapping
	 * from local process identifiers to the abstract syntax tree of the process
	 * represented by that identifier.
	 *
	 * @param{LocalProcess[]} - an array of local processes
	 * @return{LocalReferenceMap} - a local reference map
	 */
	function LocalReferenceMap(localProcesses){
		let map = {};
		for(let i = 0; i < localProcesses.length; i++){
			map[localProcesses[i].ident.ident] = localProcesses[i].process;
		}

		return map;
	}

	/**
	 * Constructs and returns a reference ast node with the specified
	 * reference.
	 *
	 * @param{int} refrence - the reference
	 * @return{ReferenceNode} - the constructed reference node
	 */
	function ReferenceNode(reference){
		this.type = 'reference';
		this.reference = reference;
	}
  function ReferenceReplacerException(process, location){
    this.message = 'Process \'' + process + '\' has not been defined';
    this.location = location;
    this.toString = function(){
      return 'ReferenceReplacerException: ' + this.message;
    }
  }
}
