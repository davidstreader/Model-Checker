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
    delete process.ranges;
    delete process.ident.ranges;

    if (ranges && variableSet) {
      for (let range in ranges.ranges) {
        const v = ranges.ranges[range].variable.substring(1);
        if (variableSet.indexOf(v) > -1) {
          const oldIdent = process.ident.ident;
          let tmp = oldIdent.split("[");
          const rangeIdx = parseInt(range)+1;
          tmp[rangeIdx] = tmp[rangeIdx].split("]");
          tmp[rangeIdx][0] = v;
          tmp[rangeIdx] = tmp[rangeIdx].join("]");
          process.ident.ident = tmp.join("[");
          processMap[oldIdent] = process.ident.ident;
        }
      }
    }
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
    if (process.local) {
      for (let local in process.local) {
        process.local[local] = hideInProcess(process.local[local],processMap, variableSet, actionMap);
      }
    }
    process.process = hideNode(process.process,processMap, variableSet, actionMap);
    if (process.local) {
      const toDel = [];
      const localMap = {};
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
  function hideNode(node, processMap, variableSet, actionMap) {
    switch(node.type) {
      case "choice":
      case "composite":
        node.process1 = processBranch(node, "process1", processMap, actionMap);
        node.process2 = processBranch(node, "process2", processMap, actionMap);
        break;
      case "sequence":
        node.to = processBranch(node, "to", processMap, actionMap);
        break;
      case "function":
        node.process = processBranch(node, "process", processMap, actionMap);
        break;
      case "identifier":
        if (processMap[node.ident]) {
          if (typeof processMap[node.ident] == "string") {
            node.ident = processMap[node.ident];
          } else {
            node.ident = processMap[node.ident].ident;
          }
        }
        break;
    }
    //Clear out hidden variables from showing their current value as it is not defined anymore.
    if (node.guardMetadata) {
      for (let it in node.guardMetadata.variables) {
        let cur = node.guardMetadata.variables[it];
        for (let v in variableSet) {
          if (cur.startsWith(variableSet[v])) {
            delete node.guardMetadata.variables[it];
          }
        }
      }
    }
    return node;
  }
  function mergeProcess(process1, process2, actionMap) {
    //If both nodes are identifiers, nothing needs to be done as they are identical
    if (process1.type == "identifier" && process2.type == "identifier") {
      //the identifier itself will be identical.
      return process1;
    } else {
      if (process1.type == "sequence") {
        const loc = process1.to.location;
        //Locations are unique. So if an action is duplicated, we can easily tell by its location being set.
        let key = loc.start.line+","+loc.start.col+","+loc.end.line+","+loc.end.col;
        if (actionMap[key]) {
          //Since we dont want to include the sequence since it is duplicated, we can just return the other process
          return process2;
        }
        actionMap[key] = true;
      }
      if (process2.type == "sequence") {
        const loc = process2.to.location;
        //Locations are unique. So if an action is duplicated, we can easily tell by its location being set.
        let key = loc.start.line+","+loc.start.col+","+loc.end.line+","+loc.end.col;
        if (actionMap[key]) {
          //Since we dont want to include the sequence since it is duplicated, we can just return the other process
          return process1;
        }
        actionMap[key] = true;
      }
      //At this point, location information is hopefully not needed.
      return new ChoiceNode(process1,process2,null);
    }
  }
  function processBranch(parent, child, processMap, actionMap) {
    const ident = parent[child].ident;
    parent[child] = hideNode(parent[child], processMap);
    if (ident === undefined) return parent[child];
    if (typeof processMap[ident] === 'object') {
      const {parent:parent2,child:child2} = processMap[ident];
      const process = parent[child];
      const process2 = parent2[child2];
      let newNode = mergeProcess(process,process2, actionMap);
      parent[child] = parent2[child2] = newNode;
      const newIdent = processMap[ident].ident;
      processMap[ident] = {
        child: child,
        parent: parent,
        ident: newIdent
      }
    }
    if (typeof processMap[ident] == 'string') {
      const newIdent = processMap[ident];
      processMap[ident] = {
        child: child,
        parent: parent,
        ident: newIdent
      }
    }
    return parent[child];
  }

}
