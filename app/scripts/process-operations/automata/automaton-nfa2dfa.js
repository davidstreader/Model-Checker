function automataNFA2DFA(automaton) {
  const newAutomaton = new Automaton(automaton.id);
  const stack = [];
  stack.push({from:null,set:[automaton.root]});
  while(stack.length > 0) {
    const cur = stack.pop();
    const node = newAutomaton.addNode(undefined,{label:"["+cur.set.map(e=>e.metaData.label)+"]"});
    const edges = {};
    cur.set.map(c3=>Object.keys(c3.outgoingEdgeSet).forEach(c=>{
      const edge = automaton.getEdge(c);
      if (edge.v) return;
      edge.v = true;
      const set = edges[edge.label] && edges[edge.label].set || [];
      set.push(automaton.getNode(edge.to));
      edges[edge.label] = {from:{label:edge.label,node:node},set:set};
    //newAutomaton.addEdge(newAutomaton.nextEdgeId,edge.label,node,)
    }));
    Object.values(edges).forEach(e => stack.push(e));
    console.log(cur);
    //console.log(cur);
  }
  //console.log(JSON.stringify(newAutomaton,null,2));
  return automaton;
}
