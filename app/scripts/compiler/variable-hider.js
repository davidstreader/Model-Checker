function hideVariables(processes) {
  for (let process in processes) {
    const processMap = {};
    const actionMap = [];
    constructMap(processes[process],processMap);
    processes[process] = hideInProcess(processes[process],processMap,undefined,actionMap);
  }

  return processes;

  function constructMap(process,processMap, variableSet) {
    if (!variableSet && process.variables) {
      variableSet = process.variables.set;
    }
    const ranges = process.ranges;
    //At this point we can finally remove the unused ranges.
    delete process.ranges;
    delete process.ident.ranges;

    if (ranges && variableSet) {
      for (let range in ranges.ranges) {
        //Get the variable without a leading $
        const v = ranges.ranges[range].variable.substring(1);
        //This variable exists in the hide set
        if (variableSet.indexOf(v) > -1) {
          //Get the original identifier, and split out the number at range index.
          //We can then join everything back up after replacing that with the variable name.
          const oldIdent = process.ident.ident;
          let tmp = oldIdent.split("[");
          const rangeIdx = parseInt(range)+1;
          tmp[rangeIdx] = tmp[rangeIdx].split("]");
          tmp[rangeIdx][0] = v;
          tmp[rangeIdx] = tmp[rangeIdx].join("]");
          process.ident.ident = tmp.join("[");
          //Store a mapping from the original name to the new name
          processMap[oldIdent] = {
            ident: process.ident.ident,
            matched: false
          };
        }
      }
    }
    //Construct maps for local processes also.
    if (process.local) {
      for (let local in process.local) {
        constructMap(process.local[local],processMap, variableSet);
      }
    }
  }
  function hideInProcess(process,processMap, variableSet, actionMap) {
    if (!variableSet && process.variables) {
      variableSet = process.variables.set;
    }
    //Hide local processes first, as the main process references the local processes.
    if (process.local) {
      for (let local in process.local) {
        process.local[local] = hideInProcess(process.local[local],processMap, variableSet, actionMap);
      }
    }
    //replace the process with a version with all hidden variables hidden
    process.process = hideNode(process.process,processMap, variableSet, actionMap);
    if (process.local) {
      const toDel = [];
      const localMap = {};
      //Look through all local processes, and then merge together processes with identical identifiers
      for (let local in process.local) {
        const l = process.local[local];
        if (localMap[l.ident.ident]) {
          localMap[l.ident.ident].process = mergeProcess(localMap[l.ident.ident].process,l.process, actionMap);
          toDel.push(process.local[local]);
        } else {
          localMap[l.ident.ident] = l;
        }
      }
      for (let i in toDel) {
        process.local.splice(process.local.indexOf(toDel[i]),1);
      }
    }
    return process;
  }
  //Process a node for hiding
  function hideNode(node, processMap, variableSet, actionMap) {
    switch(node.type) {
      case "choice":
      case "composite":
        let p1 = processBranch(node, "process1", processMap, actionMap, variableSet);
        let p2 = processBranch(node, "process2", processMap, actionMap, variableSet);
        //When we want to delete something, it will be set to null.
        //In that case, we can just return the other node
        if (p1 == null) node = p2;
        if (p2 == null) node = p1;
        //It is possible that both processes are null.
        //In this case, the parent will get to this exact point and return the other process.
        if (node == null) return null;
        break;
      case "sequence":
        if (node.to.type == "identifier") {
          if (processMap[node.to.ident]) {
            const l = node.from.location;
            //We can use the location as a unique identifier of the action
            //As label is not enough - somebody could label two actions with
            //identical labels
            const key = l.start.line + "," +
              l.start.col + l.end.line + "," +
              l.end.col + "->" + processMap[node.to.ident].ident;
            if (actionMap[key]) {
              return null;
            }
            actionMap[key] = true;
          }
        }
        processBranch(node, "to", processMap, actionMap, variableSet);
        break;
      case "function":
        processBranch(node, "process", processMap, actionMap, variableSet);
        break;
      case "identifier":
        if (processMap[node.ident]) {
          node.ident = processMap[node.ident].ident;
        }
        break;
    }
    //Clear out hidden variables from showing their current value in guards as it is not defined anymore.
    if (node.guardMetadata) {
      for (let it in node.guardMetadata.variables) {
        let cur = node.guardMetadata.variables[it];
        for (let v in variableSet) {
          if (cur.startsWith(variableSet[v])) {
            node.guardMetadata.variables.splice(it,1);
          }
        }
      }
    }
    return node;
  }
  function mergeProcess(process1, process2) {
    if (process1 == null) return process2;
    if (process2 == null) return process1;
    //If both nodes are identifiers, nothing needs to be done as they are identical
    if (process1.type == "identifier" && process2.type == "identifier") {
      //the identifier itself will be identical.
      return process1;
    } else {
      //At this point, location information is hopefully not needed.
      //the easiest way to merge two unrelated nodes is to create a choice from them.
      return new ChoiceNode(process1,process2,null);
    }
  }
  function processBranch(parent, child, processMap, actionMap, variableSet) {
    const ident = parent[child].ident;
    parent[child] = hideNode(parent[child], processMap, variableSet, actionMap);
    if (ident === undefined) return parent[child];
    if (processMap[ident]) {
      if (processMap[ident].matched) {
        //There is already a node with this identifier. Instead of duplicating it, we merge it
        //With the new node
        const {parent:parent2, child:child2} = processMap[ident];
        const process = parent[child];
        const process2 = parent2[child2];
        let newNode = mergeProcess(process, process2, actionMap);
        parent[child] = parent2[child2] = newNode;
      }
      const newIdent = processMap[ident].ident;
      processMap[ident] = {
        child: child,
        parent: parent,
        ident: newIdent,
        matched: true
      };
    }
    return parent[child];
  }

}
