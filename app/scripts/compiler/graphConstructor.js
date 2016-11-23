'use strict';

/**
 * Converts the processes in the processes map into graphs from the graphlib library
 * bundled in dagreD3. These graphs can be used to visualise the processes.
 *
 * @param {string -> process} processesMap - mapping from process name to process
 * @return {graph[]} - an array of graphs
 */
function constructGraphs(processesMap, analysis, lastGraphs){
	var processes = [];
	for(var ident in processesMap){
		var graph;

		if(processesMap[ident].type === 'automata' && processesMap[ident].nodeCount > 100){
			continue;
		}

		// check if the current process has been updated since the last compilation
		if(analysis[ident].isUpdated){
			var process = processesMap[ident];
			if(process.type === 'automata'){
        graph = process;
			}
			else if(process.type === 'petrinet'){
				graph = process;
			}
			else{
				// throw error
			}
		}
		else{
			// find the previous graph constructed for this process
			for(var i = 0; i < lastGraphs.length; i++){
				if(lastGraphs[i].name === ident){
					graph = lastGraphs[i].graph;
					break;
				}
			}
		}

		processes.push({ name:ident, graph:graph });
	}

	return processes;

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
