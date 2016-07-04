'use strict';

var processesMap;
var variableMap;
var nextProcessId;

function interpret(processes, variables, analysis, lastProcessesMap){
	reset();
	variableMap = variables;

	for(var i = 0; i < processes.length; i++){
		// check if the current process has been updated since last compilation
		var ident = processes[i].ident.ident;
		if(analysis[ident].isUpdated){
			// interpret the process
			var type = processes[i].processType;
			if(type === 'automata'){
				interpretAutomaton(processes[i], processesMap, variableMap, nextProcessId++);
			}
			else if(type === 'petrinet'){
				interpretPetriNet(processes[i], processesMap, variableMap, nextProcessId++);
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