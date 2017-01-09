function hideVariables(processes) {
  for (let process in processes) {
    const processMap = {};
    processes[process] = constructMap(processes[process],processMap);
    processes[process] = hideInProcess(processes[process],processMap);
  }

  return processes;

  function constructMap(process,processMap, variableSet, ranges) {
    if (!variableSet && process.variables) {
      variableSet = process.variables.set;
    }
    if (!ranges && process.ranges)
      ranges = process.ranges;
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
        process.local[local] = constructMap(process.local[local],processMap, variableSet, ranges);
      }
    }
    return process;
  }
  function hideInProcess(process,processMap, variableSet) {
    if (!variableSet && process.variables) {
      variableSet = process.variables.set;
    }
    if (process.local) {
      for (let local in process.local) {
        process.local[local] = hideInProcess(process.local[local],processMap, variableSet);
      }
    }
    process.process = hideNode(process.process,processMap, variableSet);
    if (process.local) {
      const toDel = [];
      const localMap = {};
      for (let local in process.local) {
        const l = process.local[local];
        if (localMap[l.ident.ident]) {
          localMap[l.ident.ident].process = mergeProcess(localMap[l.ident.ident].process,l.process);
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
  function hideNode(node, processMap, variableSet) {
    switch(node.type) {
      case "choice":
      case "composite":
        node.process1 = processBranch(node, "process1", processMap);
        node.process2 = processBranch(node, "process2", processMap);
        break;
      case "sequence":
        node.to = processBranch(node, "to", processMap);
        break;
      case "function":
        node.process = processBranch(node, "process", processMap);
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
  function mergeProcess(process1, process2) {
    //If both nodes are identifiers, nothing needs to be done as they are identical
    if (process1.type == "identifier" && process2.type == "identifier") {
      //the identifier itself will be identical.
      return process1;
    } else {
      if (process1.type == process2.type) {
        if (process1.type == "sequence" && process1.to.type == process2.to.type) {
          if (process1.to.type == "identifier" && process1.to.ident == process2.to.ident) {
            return process1.to;
          }
        }
      }
      //At this point, location information is hopefully not needed.
      return new ChoiceNode(process1,process2,null);
    }
  }
  function processBranch(parent, child, processMap) {
    const ident = parent[child].ident;
    parent[child] = hideNode(parent[child], processMap);
    if (ident === undefined) return parent[child];
    if (typeof processMap[ident] === 'object') {
      const {parent:parent2,child:child2} = processMap[ident];
      const process = parent[child];
      const process2 = parent2[child2];
      let newNode = mergeProcess(process,process2);
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
