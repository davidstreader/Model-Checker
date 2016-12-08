'use strict';

/**
 * This function is a wrapper for interpreters for each process model. Takes an
 * array of process abstract syntax trees and delegates the interpretation of each
 * ast to the defined process model for that process. Uses the analysis to determine
 * if processes have had changes made to them since the last compilation.
 *
 * @param{Process[]} processes - an array of process abstract syntax trees
 * @param{Identifier -> Analysis} analysis - an analysis of the currently defined processes
 * @param{Identifier -> ProcessModel} lastProcessesMap - processes defined last compilation
 * @param{Object} context - contains information from the main application
 * @return{Identifier -> ProcessModel} - a mapping from identifier to process model
 */
function interpret(processes, analysis, lastProcessesMap, context){
  const processesMap = {};

  // interpret the defined processes
  for(let i = 0; i < processes.length; i++){
    // check if the current process has been updated since last compilation
    const ident = processes[i].ident.ident;
    if(analysis[ident] !== undefined && analysis[ident].isUpdated){
      postMessage({clear:true,message:("Interpreting: "+ident+" ("+(i+1)+"/"+processes.length)+")"});
      // interpret the process
      switch(processes[i].processType){
        case 'automata':
          interpretAutomaton(processes[i], processesMap, context);
          break;
        case 'petrinet':
          interpretPetriNet(processes[i], processesMap, context);
          break;
        default:
          break;
      }
    }
    else{
      // use the last interpretation
      processesMap[ident] = lastProcessesMap[ident];
    }
  }

  return processesMap;
}

function interpretOneOff(ident, process, processType, processes, variables){
  var process = new ProcessNode(ident, process, processType);
  process = expand({ processes:[process], variableMap:variables });

  if(processType === 'automata'){
    interpretAutomaton(process.processes[0], processes, true);
  }
  else if(processType === 'petrinet'){
    interpretPetriNet(process.processes[0], processes, true);
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
}
