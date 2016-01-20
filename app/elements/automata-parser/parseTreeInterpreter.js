'use strict';

var definitionsMap;
var compositeStack;

function resetInterpreter(){
	definitionsMap = {};
	compositeStack = [];
	NodeUid.reset;
	EdgeUid.reset;
}

function interpretParseTree(parseTree){
	resetInterpreter();
	for(var i = 0; i < parseTree.length; i++){
		var model = parseTree[i];
		if(model.type == 'model'){
			interpretDefinition(model);
		}
		else if(model.type == 'operation'){
			interpretOperation(model);
		}
		else if(model.subtype == 'constant'){
			interpretConstant(model);
		}
		else if(model.subtype == 'range'){
			interpretRange(model);
		}
		else if(model.subtype == 'set'){
			interpretRange(model);
		}
		else if(model.type != 'comment'){
			console.error('Trying to interpret invalid model type \'' + model.type + '\'.');
		}
	}

	return definitionsMap;
}

function interpretDefinition(model){
	var globalName = model.definitions[0].name.name;
	initialiseDefinitions(model.definitions, globalName);
	for(var i = 0; i < model.definitions.length; i++){
		var definition = model.definitions[i];
		var name = (i == 0) ? definition.name.name : globalName + '.' + definition.name.name.name;
		
		definitionsMap[name].graph = new Graph();
		definitionsMap[name].relabel = definition.relabel;
		definitionsMap[name].hidden = definition.hidden;
		definitionsMap[name].isVisible = definition.isVisible;

		var rootId = NodeUid.next;
		var root = definitionsMap[name].graph.addNode(rootId);
		root.addMetaData('startNode', true);
		definitionsMap[name].referenceMap[name] = root;

		interpretProcess(definition.process, root, name, globalName);
	}

	processDefinition(globalName);
}

function initialiseDefinitions(definitions, globalName){
	// check that there is not already a definition with the specified name
	if(definitionsMap[globalName] != undefined){
		throw  new InterpreterException('Process \'' + globalName + '\' is defined more than once.');
	}

	for(var i = 1; i < definitions.length; i++){
		var name = globalName + '.' + definitions[i].name.name.name; // needs to be fixed in parser
		definitionsMap[name] = new Definition;
	}

	definitionsMap[globalName] = new Definition;
}

function _addDefinition(name, definition){
	// check that name is valid
	if(definitionsMap[name] != undefined){
		// throw interpreter exception
	}

	// add definition to definition map
	definitionsMap[name] = definition;
}

/**
 * Interprets a process node from the parse tree.
 *
 * @param {object} ptNode - the stop node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretProcess(process, currentNode, defName, globalName){
	var type = process.type;
	if(type == 'definition'){
		interpretLocalDefinition(process, currentNode,defName, globalName);
	}
	else if(type == 'sequence'){
		interpretSequence(process, currentNode,defName, globalName);
	}
	else if(type == 'choice'){
		interpretChoice(process, currentNode,defName, globalName);
	}
	else if(type == 'if-statement'){
		interpretIfStatement(process, currentNode,defName, globalName);
	}
	else if(type == 'parallel'){
		interpretParallel(process, currentNode,defName, globalName);
	}
	else if(type == 'composite'){
		interpretComposite(process, currentNode,defName, globalName);
	}
	else if(type == 'name'){
		interpretName(process, currentNode,defName, globalName);
	}
	else if(type == 'abstraction'){
		interpretAbstraction(process, currentNode,defName, globalName);
	}
	else if(type == 'simplification'){
		interpretSimplification(process, currentNode,defName, globalName);
	}
	else if(type == 'stop'){
		interpretStop(process, currentNode,defName, globalName);
	}
	else if(type == 'error'){
		interpretError(process, currentNode,defName, globalName);
	}
	else{
		console.error('Trying to interpret invalid process type \'' + type + '\'.');
	}
}

/**
 * Interprets a locally defined definition node from the parse tree.
 *
 * @param {object} ptNode - the stop node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretLocalDefinition(ptNode, currentNode, defName, globalName){
	interpretProcess(definition.process, currentNode, defName, globalName);
}

/**
 * Interprets a sequence node from the parse tree.
 *
 * @param {object} ptNode - the sequence node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretSequence(ptNode, currentNode, defName, globalName){
	// check if this sequence is guarded
	var guard = ptNode.guard;
	if(guard != undefined){
		var result = processExpression(guard, definitionsMap[defName].variableMap);
		// do not progress with sequence if the guard is not met
		if(!result){
			return;
		}
	}

	var action = processActionLabel(ptNode.from, definitionsMap[defName].variableMap);

	if(typeof(action) == 'object'){
		interpretIndexedSequence(ptNode, currentNode, defName, globalName, action);
	}
	else{
		var graph = definitionsMap[defName].graph;
		var node = graph.addNode(NodeUid.next);
		graph.addEdge(EdgeUid.next, currentNode, node, action);
		interpretProcess(ptNode.to, node, defName, globalName);
	}
}

/**
 * Helper function for 'interpretSequence' which interprets an indexed sequence node from
 * the parse tree
 *
 * @param {object} ptNode - the stop node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 * @param {string|undefined} action - the action label preceeding the current one
 */
function interpretIndexedSequence(ptNode, currentNode, defName, globalName, action){
	var graph = definitionsMap[defName].graph;
	var prefix = (action.action != undefined) ? action.action : '';
	var variable;

	// define variable if this is an indexed action
	if(action.type == 'index'){
		variable = action.variable;
	}

	// iterate over the range or set
	var iterator = new IndexIterator(action.index);
	while(iterator.hasNext){
		var element = iterator.next;

		// update variable reference if variable is defined
		if(variable != undefined){
			definitionsMap[defName].variableMap[variable] = element;
		}

		// format index depending on whether iterating over a range or set
		var index = (iterator.type == 'range') ? '[' + element + ']' : '.' + element;

		var node = graph.addNode(NodeUid.next);
		graph.addEdge(EdgeUid.next, currentNode, node, prefix + index);
		interpretPrococess(ptNode.to, node, defName, globalName)
	}
}

function processActionLabel(ptNode, variableMap){
	if(ptNode.type == 'expression'){
		var expression = ptNode.expression;
		expression = (expression.expression == undefined) ? expression.expression : expression;
		return interpretExpression(expression, variableMap);
	}
	else if(ptNode.type == 'set'){
		return processSet(ptNode.set);
	}
	else if(ptNode.type == 'range'){
		return processRange(ptNode.range);
	}
	else if(ptNode.type == 'name'){
		// to complete
	}
	else if(ptNode.type == 'index'){
		return processIndexActionLabel(ptNode, variableMap);
	}
	else if(ptNode.subtype == undefined){
		return ptNode.action;
	}
	else if(ptNode.subtype == 'expression'){
		return processExpressionActionLabel(ptNode, variableMap);
	}
	else if(ptNode.subtype == 'joined'){
		return processJoinedActionLabel(ptNode, variableMap);
	}
	else if(ptNode.subtype == 'index'){
		return processIndexActionLabel(ptNode, variableMap);
	}
}

function processExpressionActionLabel(ptNode, variableMap){
  var action = undefined;
  if(ptNode.action != undefined){
    action = (typeof(ptNode.action) == 'string') ? ptNode.action : processActionLabel(ptNode.action, variableMap);
  }
  action = (action == undefined) ? '' : action;
  var expressions = ptNode.expressions;
  
  for(var i = 0; i < expressions.length; i++){
    var temp = processActionLabel(expressions[i], variableMap);
    if(temp.type == 'index'){
      temp.action = action;
      action = temp;
    }
    else{
      action += '[' + temp + ']';
    }
  }
   return action;
}

function processJoinedActionLabel(ptNode, variableMap){
  var actions = ptNode.actions;
  var action = '';
     
  for(var i = 0; i < actions.length; i++){
    var temp = processActionLabel(actions[i], variableMap);
    var result = (i == 0) ? '' : '.';
    action += (result + temp);
  }
   return action; 
}

function processIndexActionLabel(ptNode, variableMap){
  var index = processActionLabel(ptNode.index, variableMap);
  return { type: 'index', variable: ptNode.variable, action: ptNode.action, index: index }
}

/**
 * Interprets a choice node from the parse tree.
 *
 * @param {object} ptNode - the choice node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretChoice(ptNode, currentNode, defName, globalName){
	interpretProcess(ptNode.option1, currentNode, defName, globalName);
	interpretProcess(ptNode.option2, currentNode, defName, globalName);
}

/**
 * Interprets an if statement node from the parse tree.
 *
 * @param {object} ptNode - the if statement node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretIfStatement(ptNode, currentNode, defName, globalName){
	var guard = processExpression(guard, definitionsMap[defName].variableMap);
	// if the guard is valid then process the then statement
	if(guard){
		interpretProcess(ptNode.thenProcess, currentNode, defName, globalName);
	}
	// otherwise check if an else statement was declared
	else if(ptNode.elseProcess != undefined){
		interpretProcess(ptNode.elseProcess, currentNode, defName, globalName);
	}
}

/**
 * Interprets a parallel node from the parse tree.
 *
 * @param {object} ptNode - the parallel node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretParallel(ptNode, currentNode, defName, globalName){
	var processes = [ptNode.process1, ptNode.process2];

	for(var i = 0; i < processes.length; i++){
		var name = defName + '.' + i;
		compositeStack.push(name);

		definitionsMap[name] = new Definition(processes[i].relabel, processes[i].hidden, false);
		definitionsMap[name].graph = new Graph();
		var root = definitionsMap[name].graph.addNode(NodeUid.next);
		definitionsMap[defName].rootId = root.id;
		
		var process = (processes[i].process == undefined) ? processes[i] : processes[i].process;
		interpretProcess(process, definitionsMap[name].graph.root, name, name);
		processDefinition(name);
	}

	var graph1 = definitionsMap[defName + '.0'].graph;
	var graph2 = definitionsMap[defName + '.1'].graph;
	var graph = Graph.Operations.parallelComposition(graph1, graph2);
	
	definitionsMap[defName].graph = graph;
	definitionsMap[defName].graph.root = graph.root;
	definitionsMap[defName].graph.root.addMetaData('startNode', true); // should be done in operations

	compositeStack.push(defName);
}

/**
 * Interprets a composite node from the parse tree.
 *
 * @param {object} ptNode - the composite node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretComposite(ptNode, currentNode, defName, globalName){
	interpretProcess(ptNode.composite, currentNode, defName, globalName);
	var name = compositeStack.pop();
	// update the label for the definition if necessary
	if(ptNode.label != undefined){
		var label = ptNode.label.action;
		definitionsMap[name].label = label;
	}
}

/**
 * Interprets a name node from the parse tree.
 *
 * @param {object} ptNode - the name node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretName(ptNode, currentNode, defName, globalName){
	var references = ptNode.name;
	while(references.name != undefined){ references = references.name; }
	var suffix = '';
	//check if name is indexed
	if(ptNode.index != undefined){
		var index = processActionLabel(ptNode.index, definitionsMap[defName].variableMap);
		suffix = '[' + index + ']';
	}

	// check if this is a local reference
	var localReference = globalName + '.' + references + suffix;
	if(definitionsMap[localReference] != undefined){
		references = localReference;
	}

	interpretReference(ptNode, currentNode, defName, globalName, references);
}

/**
 * Helper function for 'interpretName' which interpets the node which the current
 * node is referencing.
 *
 * @param {object} ptNode - the stop node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 * @param {string} references - the name of the node being referenced
 */
function interpretReference(ptNode, currentNode, defName, globalName, references){
	// check if there is no record of this reference yet
	var referenceMap = definitionsMap[defName].referenceMap;
	if(referenceMap[references] == undefined){
		var metaData = currentNode.getMetaData('references');
		if(metaData == undefined){
			currentNode.addMetaData('references', [references]);
		}
		else{
			currentNode.addMetaData('references', metaData.concat(references));
		}

		referenceMap[references] = currentNode;
		currentNode.addMetaData('referencesUnprocessed', true);
		definitionsMap[defName].unprocessedNodes.push(currentNode);

		// check if there is a definition for this reference yet
		if(definitionsMap[references] == undefined || definitionsMap[references].graph == undefined){
			definitionsMap[references] = new Definition();
			definitionsMap[references].rootId = currentNode.id;
		}
		else{
			currentNode.id = definitionsMap[references].rootId;
		}
	}
	else{
		definitionsMap[defName].graph.mergeNodes([referenceMap[references].id, currentNode.id]);
	}
}

function interpretAbstraction(ptNode, currentNode, defName, globalName){
	var references = interpretFunction(ptNode, currentNode, defName, globalName);

	// throw an error if the definition to be abstracted is not defined
	if(definitionsMap[references] == undefined){
		throw new InterpreterException('\'' + reference + '\' has not been defined.');
	}

	var graph = definitionsMap[references].graph;
	definitionsMap[defName].graph = Graph.Operations.abstraction(graph);
	currentNode.id = definitionsMap[defName].graph.root.id;
}

function interpretSimplification(ptNode, currentNode, defName, globalName){
	var references = interpretFunction(ptNode, currentNode, defName, globalName);

	// throw an error if the definition to be abstracted is not defined
	if(definitionsMap[references] == undefined){
		throw new InterpreterException('\'' + reference + '\' has not been defined.');
	}

	var graph = definitionsMap[references].graph;
	graph.root.addMetaData('startNode', true);
	definitionsMap[defName].graph = Graph.Operations.simplification(graph);
	currentNode.id = definitionsMap[defName].graph.root.id;
}

function interpretFunction(ptNode, currentNode, defName, globalName){
	var name;
	var references;
	var process = ptNode.process;

	if(process.type == 'definition'){
		name = defName + '(internal-definition)';
		references = name;
		
		definitionsMap[name] = new Definition(process.relabel, process.hidden, false);
		definitionsMap[name].graph = new Graph();
		var root = definitionsMap[name].graph.addNode(NodeUid.next);
		definitionsMap[defName].rootId = root.id;
		root.addMetaData('startNode', true);

		interpretProcess(process.process, definitionsMap[name].graph.root, name, globalName);
		processDefinition(name);
	}
	else{
		name = process.name.name;
		references = process;
		while(typeof(references) != 'string'){
			references = references.name;
		}
	}

	// check if there are any nested functions within this process
	if(typeof(references) == 'object'){
		interpretProcess(ptNode.name, currentNode, defName, globalName);
		references = defName;
	}

	return references;
}

/**
 * Interprets an error node from the parse tree.
 *
 * @param {object} ptNode - the error node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretError(ptNode, currentNode, defName, globalName){
	// add a deadlock edge from the current node
	var graph = definitionsMap[defName].graph;
	var node = graph.addNode(NodeUid.next);
	graph.addEdge(EdgeUid.next, currentNode, node, '', false, true);
	node.addMetaData('isTerminal', 'error');
}

/**
 * Interprets a stop node from the parse tree.
 *
 * @param {object} ptNode - the stop node
 * @param {object} currentNode - the current node being added to
 * @param {string} defName - the name of the current definition being processed
 * @param {string} globalName - the current globally defined process
 */
function interpretStop(ptNode, currentNode, defName, globalName){
	currentNode.addMetaData('isTerminal', 'stop');
}

function processDefinition(defName){
	processReferences(defName);
	processRelabelling(defName);
	processHiding(defName);
	relabelNodes(defName, true, false);
}

function processReferences(defName){
	var prevReferences = defName;
	// while there are unprocessed nodes
	while(definitionsMap[defName].unprocessedNodes.length > 0){
		var definition = definitionsMap[defName];
		var size = definitionsMap[defName].unprocessedNodes.length;
		var references;

		// loop through all the unprocessed nodes (only the ones that were marked as unprocessed
		// when the while loop started, not any new unprocessed nodes that were added)
		for(var i = 0; i < size; i++){
			var node = definition.unprocessedNodes[i];
			var refs = node.getMetaData('references');

			for(var j = 0; j < refs.length; j++){
				references = refs[j];
				var localReference = defName + '.' + references;
				// check whether or not the automaton that this node is referencing actually exists
				if(definitionsMap[localReference] == undefined || definitionsMap[localReference].graph == undefined){
					if(definitionsMap[references] == undefined || definitionsMap[references].graph == undefined){
						throw new InterpreterException('Unknown automaton \'' + references + '\' referenced in \'' + prevReferences + '\'.');
					}
				}
				else{
					references = localReference;
				}

				var refGraph = definitionsMap[references].graph;
				var refNodes = refGraph.nodes;
				// set the current nodes id to the root of the reference graph
				node.id = refGraph.root.id;
				// add the nodes and edges from the previous graph into this one
				definition.graph.combineWith(refGraph);
				
				// final all the unprocessed nodes that we are adding to this graph
				// and add them to the array of unprocessed nodes
				for(var k = 0; k < refNodes.length; k++){
					var gRefNode = definition.graph.getNode(refNodes[k].id);
					if(gRefNode.getMetaData('referenceUnprocessed')){
						definition.unprocessedNodes.push(gRefNode);
					}
				}

				// this node has now been processed
				node.deleteMetaData('referenceUnprocessed');
			}
		}

		// remove all the nodes we just processed from the unprocessed nodes array
		definition.unprocessedNodes.splice(0, size);
		prevReferences = references;
	}
}

function processRelabelling(defName){
	var relabel = definitionsMap[defName].relabel;
	if(relabel != undefined){
		relabel = processRelabellingSet(relabel);

		var edges = definitionsMap[defName].graph.edges;
		// process all edges in graph
		for(var i = 0; i < edges.length; i++){
			var edge = edges[i];

			// check if edge needs to be relabelled
			for(var j = 0; j < relabel.length; j++){
				var oldLabel = relabel[j]['old'];
				if(oldLabel == edge.label){
					edge.label = relabel[j]['new'];
				}
			}
		}
	}
}

function processRelabellingSet(set){
	var result = [];
	for(var i = 0; i < set.length; i++){
		var newLabel = processActionLabel(set[i]['new']);
		var oldLabel = processActionLabel(set[i]['old']);
		result.push({ new: newLabel, old: oldLabel });
	}

	return result
}

function processHiding(defName){
	var hidden = definitionsMap[defName].hidden;
	if(hidden != undefined){
		var type = hidden.type;
		var set = processSet(hidden.set);

		var edges = definitionsMap[defName].graph.edges;
		for(var i = 0; i < edges.length; i++){
			var edge = edges[i];

			for(var j = 0; j < set.length; j++){
				var label = set[j];
				
				// check the edge label is in the set
				var contains = edge.label == label;
				if((type == 'includes' && contains) || (type == 'excludes' && !contains)){
					edge.isHidden = true;
				}
			}
		}
	}
}

function relabelNodes(defName, showReferences, useIds){
	var definition = definitionsMap[defName];
	var label = 0;
	var visitedNodes = {};
	var fringe = [];
	fringe.push(definition.graph.root);
	
	// process all the nodes in the graph
	while(fringe.length > 0){
		var node = fringe.shift();
		
		if(!visitedNodes[node.id]){
			// mark current node as visited
			visitedNodes[node.id] = true;
			// label the node
			var reference = node.getMetaData('references');
			if(showReferences && reference && reference != defName){
				reference = reference[0];
				reference = reference.split('.');
				reference = reference[reference.length - 1];
				console.log(reference);
				if(reference.length > 3){
					reference = reference.substring(0, 2) + '...';
				}
				node.label = reference;
			}
			else if(node.label == ''){
				node.label = (useIds) ? node.id : label++;
			}

			// add the neighbours of this node to the fringe
			var neighbors = node.neighbors;
			for(var i = 0; i < neighbors.length; i++){
				// only add neighour if it has not been visited
				if(!visitedNodes[neighbors[i].id]){
					fringe.push(neighbors[i]);
				}
			}
		}
	}
}

function interpretOperation(model){
	// to complete
}

function interpretConstant(model){
	var constant = { type: 'constant', value: model.value };
	_addDefinition(model.name, constant);
}

function interpretRange(model){
	var range = this.processRange(model.name, model.range);
	_addDefinition(model.name, range);
}

function interpretSet(model){
	var set = processSet(model.name, model.set);
	_addDefinition(model.name, set);
}

function processSet(set){
	// check if set is an array
	if(typeof(set) == 'object' && set.length == undefined){
		return [processActionLabel(set)];
	}

	var result = [];
	for(var i = 0; i < set.length; i++){
		result.push(processActionLabel(set[i]));
	}

	return result;
}

/**
 * Constructs and returns an 'InterpreterException' message based
 * off of the specified message.
 *
 * @param {string} message - the message to be constructed 
 */
function InterpreterException(message){
	this.message = message;
	this.toString = function(){
		return ('InterpreterException: ' + message);
	};
}