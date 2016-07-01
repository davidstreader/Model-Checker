'use strict';

/**
 * Converts the processes in the processes map into graphs from the graphlib library
 * bundled in dagreD3. These graphs can be used to visualise the processes.
 *
 * @param {string -> process} processesMap - mapping from process name to process
 * @return {graph[]} - an array of graphs
 */
function constructGraphs(processesMap){
	var processes = [];
	for(var ident in processesMap){
		var process = processesMap[ident];
		var graph;
		if(process.type === 'automata'){
			graph = automataConstructor(process);
		}
		else if(process.type === 'petrinet'){
			graph = petriNetConstructor(process);		
		}
		else{
			// throw error
		}

		processes.push({ name:ident, graph:graph });
	}

	return processes;

	function automataConstructor(process){
		// construct a graph to represent automata
		var graph = initialiseGraph('LR');

		// set the default to assigning a new object as a label for each new edge
		graph.setDefaultEdgeLabel( function(){
			return {};
		});

		// add nodes in automaton to the graph
		var startNode;
		var nodes = process.nodes;
		for(var i = 0; i < nodes.length; i++){
			var styleClasses = 'n' + nodes[i].id;

			// check if current node is the root node
			if(nodes[i].getMetaData('startNode')){
				startNode = nodes[i];
			}

			// check if this node is a terminal
			var terminal = nodes[i].getMetaData('isTerminal');
			if(terminal !== undefined){
				styleClasses += ' terminal ' + terminal;
			}

			// add node to graph
			graph.setNode('n' + nodes[i].id, { label:'', shape:'automataNode', class:styleClasses.trim() });
		}

		// setup pointer to the start node
		graph.setNode('0', { label:'', shape:'circle', class:'start' });
		graph.setEdge('0', 'n' + startNode.id, {label:'', lineInterpolate:'basis' }, 'startEdge');

		// add the edges between the nodes in the automaton to the graph
		var edges = process.edges;
		for(var i = 0; i < edges.length; i++){
			var label = edges[i].label;
			var from = 'n' + edges[i].from;
			var to = 'n' + edges[i].to;
			graph.setEdge(from, to, { label:label, lineInterpolate:'basis' }, edges[i].id);
		}

		// return contructed graph
		return graph;
	}

	/**
	 * Constructs and returns a graphlib graph representing the specified
	 * process. This graph can be used to visualise the process through dagreD3.
	 *
	 * @param {object} process - the process to construct graph for
	 * @return {graph} - the constructed graph
	 */
	function petriNetConstructor(process){
		// construct a graph to represet petri net
		var graph = initialiseGraph('TD');

		// add places in petri net to the graph
		var startPlaces = [];
		var places = process.places;
		for(var i = 0; i < places.length; i++){
			var styleClasses = 'p' + places[i].id;

			// add to array of start places if necessary
			if(places[i].getMetaData('startPlace')){
				startPlaces.push(places[i]);
			}

			// check if this place is a terminal
			var terminal = places[i].getMetaData('isTerminal');
			if(terminal !== undefined){
				styleClasses += ' terminal ' + terminal;
			} 

			// add place to graph
			graph.setNode('p' + places[i].id, { label:'', shape:'placeNode', class:styleClasses.trim() });

		}

		// setup pointers to the start places
		for(var i = 0; i < startPlaces.length; i++){
			graph.setNode(i, { label:'', shape:'circle', class:'start' });
			graph.setEdge(i, 'p' + startPlaces[i].id, { label:'', lineInterpolate:'basis' }, 'startEdge');
		}

		// add transitions to the graph
		var edgeId = 0;
		var labelSets = process.labelSets;
		for(var i = 0; i < labelSets.length; i++){
			var label = labelSets[i].label;
			var transitions = labelSets[i].transitions;
			for(var j = 0; j < transitions.length; j++){
				var styleClasses = 't' + transitions[j].id;
				graph.setNode('t' + transitions[j].id, { label:label, shape:'transitionNode', class:styleClasses.trim() });

				//
				var outgoing = transitions[j].outgoingPlaces;
				for(var k = 0; k < outgoing.length; k++){
					var from = 't' + transitions[j].id;
					var to = 'p' + outgoing[k].id;
					graph.setEdge(from, to, { label:'', lineInterpolate:'basis' }, edgeId++);
				}

				var incoming = transitions[j].incomingPlaces;
				for(var k = 0; k < incoming.length; k++){
					var from = 'p' + incoming[k].id;
					var to = 't' + transitions[j].id;
					graph.setEdge(from, to, { label:'', lineInterpolate:'basis' }, edgeId++);
				}
			}
		}

		// return contructed graph
		return graph;
	}

	/**
	 * Helper function which initialises and returns a blank
	 * graph to build from.
	 *
	 * @return {graph} - blank graph
	 */
	function initialiseGraph(rankdir){
		var graph = new dagreD3.graphlib.Graph({ multigraph: true });
		graph.setGraph({
			rankdir: rankdir,
			marginx: 0,
			marginy: 0
		});

		// set the default to assigning a new object as a label for each new edge
		graph.setDefaultEdgeLabel( function(){
			return {};
		});

		return graph;
	}
}