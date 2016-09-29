'use strict';

/**
 * Takes the sepcified abstract syntax tree and expands out any shorthand syntax in the grammar
 * with its longhand equivalent. For example:
 *
 * automata A = ([1..2] -> STOP). === automata A = ([1] -> STOP | [2] -> STOP).
 *
 * 
 * This saves potentially having to process local references twice later on in the analysis and
 * interpretation stages of the compiler.
 *
 * @param {ast} ast - the abstract syntax tree containing defined processes and the variable map
 * @return {ast} - the ast containing expanded versions of the processes defined
 */
function expand(ast){
	var processes = ast.processes;

	// expand the defined processes
	for(var i = 0; i < processes.length; i++){
		var variableMap = JSON.parse(JSON.stringify(ast.variableMap));
		processes[i].process = expandNode(processes[i].process, variableMap);
		
		// expand local procsses if any are defined
		if(processes[i].local.length !== 0){
			variableMap = JSON.parse(JSON.stringify(ast.variableMap));
			processes[i].local = expandLocalProcessDefinitions(processes[i].local, variableMap);
		}
	}

	// return the result
	return ast;

	/**
	 * Expands and returns the local processes defined within a process.
	 *
	 * @param {astNode[]} localProcesses - an array of locally defined references
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode[]} - the expanded local processes
	 */
	function expandLocalProcessDefinitions(localProcesses, variableMap){
		var newProcesses = [];
		for(var j = 0; j < localProcesses.length; j++){
			if(localProcesses[j].ident.ranges === undefined){
				localProcesses[j].process = expandNode(localProcesses[j].process, variableMap);
				newProcesses.push(localProcesses[j]);
			}
			else{
				var ident = localProcesses[j].ident.ident;
				var ranges = localProcesses[j].ident.ranges.ranges;
				newProcesses = newProcesses.concat(expandIndexedDefinition(localProcesses[j], ident, ranges, variableMap));
			}
		}

		return newProcesses;
	}

	/**
	 * Helper function for 'expandLocalProcessDefinition' that handles expanding
	 * an indexed local process. Recursively processes the defined ranges so that:
	 *
	 * for all i in ranges[0]:
	     ...
	 *   for all j in ranges[n - 1]:
	 *     <process local definition> 
	 *
	 * Where '...' represents the ranges defined inbetween range[0] and ranges[n - 1] (if any).
   * This will create an individual local definition for <ident>[i]...[n - 1], where <ident>
   * is the main identifier for the specified localProcess.
   *
	 * @param {astNode} localProcess - the defined local process
	 * @param {string} ident - current identifier name
	 * @param {rangeNode[]} ranges - the remaining ranges
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode[]} - the expanded local processes
	 */
	function expandIndexedDefinition(localProcess, ident, ranges, variableMap){
		var newProcesses = [];

		// recursive case
		if(ranges.length !== 0){
			var iterator = new IndexIterator(ranges[0].range);
			var variable = ranges[0].variable;
			ranges = (ranges.length > 1) ? ranges.slice(1) : [];
			// setup construction of a local process for each iteration (processed in base case)
			while(iterator.hasNext){
				var element = iterator.next;
				variableMap[variable] = element;
				var newIdent = ident + '[' + element + ']';
				newProcesses = newProcesses.concat(expandIndexedDefinition(localProcess, newIdent, ranges, variableMap));
			}
		}
		// base case
		else{
			// construct a new locally defined process
			var clone = JSON.parse(JSON.stringify(localProcess));
			clone.ident.ident = ident;
			delete clone.ident.ranges;
			delete clone.ranges;
			clone.process = expandNode(clone.process, variableMap);
			newProcesses.push(clone);
		}

		return newProcesses;
	}

	/**
	 * Takes the specified astNode and calls the correct function for processing
	 * it based on its type. Not all types need to get processes, such as terminals.
	 * These are simply returned at the end of the function.
	 *
	 * @param {astNode} astNode - the astNode to expand
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded ast node
	 */
	function expandNode(astNode, variableMap){
		var type = astNode.type;
		var node;
		if(type === 'process'){
			node = expandProcessNode(astNode, variableMap);
		}
		else if(type === 'action-label'){
			node = expandActionLabelNode(astNode, variableMap);
		}
		else if(type === 'index'){
			node = expandIndexNode(astNode, variableMap);
		}
		else if(type === 'sequence'){
			node = expandSequenceNode(astNode, variableMap);
		}
		else if(type === 'choice' || type === 'composite'){
			// choice and composite nodes are structured the same (apart from type)
			node = expandChoiceOrCompositeNode(astNode, variableMap);
		}
		else if(type === 'if-statement'){
			node = expandIfStatementNode(astNode, variableMap);
		}
		else if(type === 'function'){
			node = expandFunctionNode(astNode, variableMap);
		}
		else if(type === 'identifier'){
			node = expandIdentiferNode(astNode, variableMap);
		}
		else if(type === 'forall'){
			node = expandForallNode(astNode, variableMap);
		}

		// check if the ast node did not got processed
		if(node === undefined){
			return astNode;
		}

		// return a terminal if the process produced an empty ast node
		if(node.type === 'empty'){
			return { type:'terminal', terminal:'STOP' };
		}

		return node;
	}

	/**
	 * Processes the specified action label ast node so that any variable references
	 * made by the action are replaced with their actual values.
	 *
	 * @param {astNode} astNode - the action label node to process
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded ast node
	 */
	function expandActionLabelNode(astNode, variableMap){
		astNode.action = processLabel(astNode.action, variableMap);
		return astNode;
	}

	/**
	 * Expands the specified index astNode into a series of choice ast nodes.
	 *
	 * @param {astNode} astNode - the index ast node to expand
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded ast node
	 */
	function expandIndexNode(astNode, variableMap){
		var iterator = new IndexIterator(astNode.range);
		var iterations = [];
		while(iterator.hasNext){
			var element = iterator.next;
			variableMap[astNode.variable] = element;
			var clone = JSON.parse(JSON.stringify(astNode.process));
			iterations.push(expandNode(clone, variableMap));
		}

		// convert the indexed processes into choice ast nodes
		var newNode = iterations.pop();
		while(iterations.length !== 0){
			var nextNode = iterations.pop();
			newNode = { type:'choice', process1:nextNode, process2:newNode };
		}

		return newNode;
	}

	/**
	 * Expands the specified sequence ast node.
	 *
	 * @param {astNode} astNode - the sequence ast node to expand
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded ast node
	 */
	function expandSequenceNode(astNode, variableMap){
		astNode.from = expandNode(astNode.from, variableMap);
		astNode.to = expandNode(astNode.to, variableMap);
		return astNode;
	}

	/**
	 * Expands the specified choice or composite ast node.
	 *
	 * @param {astNode} astNode - the choice or composite ast node to expand
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded ast node
	 */
	function expandChoiceOrCompositeNode(astNode, variableMap){
		astNode.process1 = expandNode(astNode.process1, variableMap);
		astNode.process2 = expandNode(astNode.process2, variableMap);
		
		// no need for choice or composition if one of the processes is empty
		if(astNode.process1.type === 'empty'){
			return astNode.process2;
		}
		if(astNode.process2.type === 'empty'){
			return astNode.process1;
		}

		// if both branches are empty it will be caught by the expandNode function
		return astNode;
	}

	/**
	 * Expands the specified if statement ast node. Only returns the branch that
	 * gets executed based on the current state of the variable map. In some cases
	 * it is possible for no paths to be executed. When this happens a special
	 * empty ast node is returned.
	 *
	 * @param {astNode} astNode - the if statement ast node to expand
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded ast node
	 */
	function expandIfStatementNode(astNode, variableMap){
		var guard = processGuardExpression(astNode.guard, variableMap);
		if(guard){
			return expandNode(astNode.trueBranch, variableMap);
		}
		else if(astNode.falseBranch !== undefined){
			return expandNode(astNode.falseBranch, variableMap)
		}
		
		return { type:'empty' };
	}

	/**
	 * Expands the specified function ast node.
	 *
	 * @param {astNode} astNode - the function ast node to expand
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded ast node
	 */
	function expandFunctionNode(astNode, variableMap){
		astNode.process = expandNode(astNode.process, variableMap);
		return astNode;
	}

	/**
	 * Expands the specified identifier ast node.
	 *
	 * @param {astNode} astNode - the index ast node to expand
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded ast node
	 */
	function expandIdentiferNode(astNode, variableMap){
		astNode.ident = processLabel(astNode.ident, variableMap);
		if(astNode.label !== undefined){
			astNode.label = expandNode(astNode.label, variableMap);
		}
		return astNode;
	}

	/**
	 * Expans the specified forall ast node into a series of composite ast nodes.
	 *
	 * @param {astNode} astNode - the forall ast node to expand
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {astNode} - the expanded astNode
	 */
	function expandForallNode(astNode, variableMap){
		var nodes = _expandForallNode(astNode.process, astNode.ranges.ranges, variableMap);
		astNode = nodes.pop();
		while(nodes.length !== 0){
			var next = nodes.pop();
			astNode = { type:'composite', process1:next, process2:astNode };
		}

		return astNode;

		/**
		 * A helper function for expandForallNode that processes the ranges defined
		 * in the forall ast node.
		 *
		 * @param {astNode} process - the defined process
		 * @param {range[]} ranges - the remaining ranges 
		 * @param {string -> string} variableMap - a mapping from variable name to value
		 * @param {astNode[]} - the processed ast nodes
		 */
		function _expandForallNode(process, ranges, variableMap){
			var newNodes = [];
			// recursive case
			if(ranges.length !== 0){
				var iterator = new IndexIterator(ranges[0].range);
				var variable = ranges[0].variable;
				ranges = (ranges.length > 1) ? ranges.slice(1) : [];

				while(iterator.hasNext){
					variableMap[variable] = iterator.next;
					newNodes = newNodes.concat(_expandForallNode(process, ranges, variableMap));
				}
			}
			// base case
			else{
				var clone = JSON.parse(JSON.stringify(process));
				clone = expandNode(clone, variableMap);
				newNodes.push(clone);
			}

			return newNodes;
		}
	}

	/**
	 * Processes the specified expression by replacing any variable references
	 * with the value that variable represents and evaluating the result. Throws 
	 * an error if a variable is found to be undefined.
	 *
	 * @param {string} expr - the expr to evaluate
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {int} - result of the evaluation
	 */
	function processExpression(expr, variableMap){
		// replace any variables declared in the expression with its value
		var regex = '[\$][a-zA-Z0-9]*';
		var match = expr.match(regex);
		while(match !== null){
			// check if the variable has been defined
			if(variableMap[match[0]] === undefined){
				throw new VariableDeclarationException('the variable \'' + match[0].substring(1) + '\' has not been defined');
			}

			expr = expr.replace(match[0], variableMap[match[0]]);
			match = expr.match(regex);
		}

		return evaluate(expr);		
	}

	/**
	 * Processes the specified label by replacing any variable references
	 * with the value that variable represents. Throws an error if a variable
	 * is found to be undefined.
	 *
	 * @param {string} label - the label to process
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {string} - the processed label
	 */
	function processLabel(label, variableMap){
		// replace any variables declared in the label with its value
		var regex = '[\$][a-zA-Z0-9]*';
		var match = label.match(regex);
		
		// if no variable was found then return
		if(match === null){
			return label;
		}
		
		while(match !== null){
			var expr = processExpression(match[0], variableMap);
			label = label.replace(match[0], expr);
			match = label.match(regex);
		}

		return label;
	}

	/**
	 * Processes the specified guard expression by replacing any variable references
	 * with the value that variable represents and evaluating the result. Throws 
	 * an error if a variable is found to be undefined.
	 *
	 * @param {string} expr - the expr to evaluate
	 * @param {string -> string} variableMap - a mapping from variable name to value
	 * @return {boolean} - result of the evaluation
	 */
	function processGuardExpression(expr, variableMap){
		expr = processExpression(expr, variableMap);
		return (expr === 0) ? false : true;
	}

	/**
	 * Constructs and returns a 'VariableDeclarationException' based off of the
	 * specified message. Also contains the location in the code being parsed
	 * where the error occured.
	 *
	 * @param {string} message - the cause of the exception
	 * @param {object} location - the location where the exception occured
	 */
	function VariableDeclarationException(message, location){
		this.message = message;
		this.location = location;
		this.toString = function(){
			return 'VariableDeclarationException: ' + message;
		};	
	}
}