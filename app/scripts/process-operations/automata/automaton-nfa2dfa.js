function automataNFA2DFA(automaton) {
    // const newAutomaton = new Automaton(automaton.id);
    // const stateMap = {};
    // const stack = [];
    // let alpha = automaton.alphabet;
    // let last = newAutomaton.addNode(undefined, {label: "[" + label(automaton.root) + "]"});
    // stateMap["[ " + label(automaton.root) + " ]"] = last;
    // newAutomaton.rootId = last.id;
    // stack.push({last:last,orig:automaton.root});
    // while (stack.length > 0) {
    //   const lastOrig = stack.pop();
    //   const cur = lastOrig.orig;
    //   const outgoing = Object.keys(cur.outgoingEdgeSet).map(e=>automaton.getEdge(e));
    //   for (let a in alpha) {
    //     const reachable = outgoing.filter(function (item) {
    //       return item.label == a;
    //     });
    //     const state = reachable.map(o => label(o.to)).filter(function (item, index, inputArray) {
    //       return inputArray.indexOf(item) == index;
    //     }).toString();
    //     if (state.trim().length > 0) {
    //       if (!stateMap[state]) {
    //         stateMap[state] = newAutomaton.addNode(undefined, {label: state});
    //         for (let out in outgoing) {
    //           stack.push({last: stateMap[state], orig: automaton.getNode(outgoing[out].to)});
    //         }
    //       }
    //       newAutomaton.addEdge(newAutomaton.nextEdgeId,a,lastOrig.last,stateMap[state]);
    //     }
    //   }
    // }
    // //console.log(JSON.stringify(newAutomaton,null,2));
    // // stack.push({from: null, set: [automaton.root]});
    // // while (stack.length > 0) {
    // //   const cur = stack.pop();
    // //   newAutomaton.addNode(undefined, {label: "[" + cur.set.map(e => e.metaData.label) + "]"});
    // //   //console.log(cur);
    // // }
    // //console.log(JSON.stringify(newAutomaton,null,2));
    return automaton;
  function label(n) {
    if (typeof n == "string") n = automaton.getNode(n);
    return n.metaData.label;
  }
}
