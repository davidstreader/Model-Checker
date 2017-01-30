function automataNFA2DFA(automaton) {
  const table = {};
  let alpha = automaton.alphabet;
  for (let i in automaton.nodes) {
    const cur = automaton.nodes[i];
    const curLbl = cur.metaData.label;
    const outgoing = Object.keys(cur.outgoingEdgeSet).map(e=>automaton.getEdge(e));
    for (let out in outgoing) {
      const rout = outgoing[out];
      const to = automaton.getNode(rout.to);
      table[curLbl] = table[curLbl] || {};
      table[curLbl][rout.label] = table[curLbl][rout.label] || [];
      table[curLbl][rout.label].push(label(to));
    }
    table[curLbl][TAU] = table[curLbl][TAU] || [];
    table[curLbl][TAU].push(curLbl);

  }
  const table2 = {};
  let stack = [];
  stack.push(clousure(automaton.root));
  delete alpha[TAU];
  while (stack.length > 0) {
    const curList = stack.pop();
    const lbl = curList.toString();
    for (let a in alpha) {
      let subTable = new Set();
      for (let cur in curList) {
        const ta = table[curList[cur]];
        for (const s in ta[a]) {
          table[ta[a][s]][TAU].forEach(s => subTable.add(s));
        }
      }
      if (subTable.size == 0) continue;
      table2[lbl] = table2[lbl] || [];
      if (table2[lbl][a]) continue;
      table2[lbl][a] = subTable;
      stack.push([...subTable]);
    }
  }
  const nodeTable = [];
  const newAutomaton = new Automaton(automaton.id);
  for (let node in table2) {
    nodeTable[node] = newAutomaton.addNode(undefined,{label: node});
    if (node == label(automaton.root)) {
      newAutomaton.rootId = nodeTable[node].id;
    }
  }
  for (let node in table2) {
    for (let edge in table2[node]) {
      newAutomaton.addEdge(newAutomaton.nextEdgeId,edge,nodeTable[node],nodeTable[[...table2[node][edge]].toString()]);
    }
  }
  return newAutomaton;
  function label(n) {
    if (typeof n == "string") n = automaton.getNode(n);
    return n.metaData.label;
  }
  function clousure(node) {
    let ret = [label(node)];
    const outgoing = Object.keys(node.outgoingEdgeSet).map(e=>automaton.getEdge(e)).filter(e=>e.label==TAU).map(e => label(e.to));
    ret = ret.concat(outgoing);
    return ret;
  }
}
