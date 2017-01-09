function hideVariables(processes) {
  const processMap = {};
  for (let process in processes) {
    processes[process] = hideInProcess(processes[process],processMap);
  }
  return processes;
}
function hideInProcess(process,processMap, variableSet, ranges) {
  if (!variableSet && process.variables) {
    variableSet = process.variables.set;
  }
  ranges = ranges || process.ranges;
  if (ranges) {
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
      process.local[local] = hideInProcess(process.local[local],processMap, variableSet, ranges);
    }
  }
  process.process = hideNode(process.process,processMap, variableSet);
  return process;
}
function hideNode(node, processMap, variableSet) {
  if (node.ident) {
    if (processMap[node.ident] instanceof String) {
      node.ident = processMap[node.ident];
      processMap[node.ident] = node;
    }
  }
  // if (node.guardMetadata) {
  //   for (let it in node.guardMetadata.next) {
  //     let cur = node.guardMetadata.next[it];
  //     for (let v in variableSet) {
  //       if (cur.startsWith(variableSet[v])) {
  //         //delete node.guardMetadata.next[it];
  //       }
  //     }
  //   }
  //   for (let it in node.guardMetadata.variables) {
  //     let cur = node.guardMetadata.variables[it];
  //     for (let v in variableSet) {
  //       if (cur.startsWith(variableSet[v])) {
  //         //delete node.guardMetadata.variables[it];
  //       }
  //     }
  //   }
  // }
  if (node.to) {
    node.to = hideNode(node.to, processMap);
    console.log(processMap[node.to.ident] instanceof Object);
  }
  if (node.process1)
    node.process1 = hideNode(node.process1,processMap);
  if (node.process2)
    node.process2 = hideNode(node.process2,processMap);
  return node;
}
