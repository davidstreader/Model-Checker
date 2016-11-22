'use strict';

var processesMap;
var variableMap;
var nextProcessId;

function interpret(processes, analysis, lastProcessesMap, context){
	reset();

	for(var i = 0; i < processes.length; i++){
		// check if the current process has been updated since last compilation
		var ident = processes[i].ident.ident;
		if(analysis[ident] !== undefined && analysis[ident].isUpdated){
			// interpret the process
			var type = processes[i].processType;
			if(type === 'automata'){
				interpretAutomaton(processes[i], processesMap, context);
			}
			else if(type === 'petrinet'){
				interpretPetriNet(processes[i], processesMap, context);
			}
			else{
				// throw error
			}
		}
		else{
			// use the last interpretation
			processesMap[ident] = lastProcessesMap[ident];
		}
	}

	return processesMap;

	function reset(){
		processesMap = {};
		variableMap = {};
		nextProcessId = 0;
	}
}

function interpretOneOff(ident, process, processType, processes, variables){
	reset();

	var process = new ProcessNode(ident, process, processType);
	process = expand({ processes:[process], variableMap:variables });

	if(processType === 'automata'){
		interpretAutomaton(process.processes[0], processes, variableMap, ident, true);
	}
	else if(processType === 'petrinet'){
		interpretPetriNet(process.processes[0], processes, variableMap, ident, true);
	}
	else{
		// throw error
	}

	return processes[ident];

	function ProcessNode(ident, process, type){
		var node = {
			type:'process',
			processType:type,
			ident:{
				type:'identifier',
				ident:ident
			},
			process:process,
			local:[]
		};

		return node;
	}

	function reset(){
		processesMap = {};
		variableMap = {};
		nextProcessId = 0;
	}
}