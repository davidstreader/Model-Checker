'use strict';

var results;
var opId;
var idents;

function evaluateOperations(operations, processesMap, variableMap){
	reset();

	for(var i = 0; i < operations.length; i++){
    var op = operations[i].isNegated ? '!~' : '~';
		var {process1, process2} = operations[i];
		const process1Err = !processesMap[getIdent(process1)];
    const process2Err = !processesMap[getIdent(process2)];

    if (process1Err || process2Err) {
      process1.ident = getIdent(process1);
      process1.exists = !process1Err;
      process2.ident = getIdent(process2);
      process2.exists = !process2Err;
      results.push({ operation:op, process1:process1, process2:process2, result:"notfound" });
		  continue;
    }
		var graph1 = interpretOneOff(generateProcessIdent(), process1, 'automata', processesMap, variableMap);
		var graph2 = interpretOneOff(generateProcessIdent(), process2, 'automata', processesMap, variableMap);

		var result = areBisimular([graph1, graph2], 'automata');
		if(operations[i].isNegated){
			result = !result;
		}

		process1 = reconstruct(process1);
		process2 = reconstruct(process2);

		results.push({ operation:op, process1:process1, process2:process2, result:result });
	}

	for(var i = 0; i < idents.length; i++){
		delete processesMap[idents[i]];
	}

	return results;

	function generateProcessIdent(){
		var ident = 'op' + opId++;
		idents.push(ident);
		return ident;
	}

	function reset(){
		results = [];
		opId = 0;
		idents = [];
	}

  function getIdent(process) {
	  if (process.type =="function") return getIdent(process.process);
	  return process.ident;
  }
}
