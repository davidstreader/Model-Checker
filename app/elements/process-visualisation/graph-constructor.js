function convertGraph(graph, id, hidden) {
  let glGraph = {};
  if (graph.type == 'automata') {
    visualizeAutomata(graph,id, hidden, glGraph);
  }
  if (graph.type == 'petrinet') {
    visualizePetriNet(graph,id, hidden, glGraph);
  }
  return glGraph;
}
function visualizeAutomata(process, graphID, hidden, glGraph) {
  glGraph.interrupts = [];
  let lastBox = graphID;
  // add nodes in automaton to the graph
  const nodes = Object.values(process.nodeMap);
  glGraph.nodes = [];
  glGraph.edges = [];
  let interruptId = 1;
  for(let i = 0; i < nodes.length; i++){
    const nid = 'n' + nodes[i].id;
    let type = "fsaState";
    // check if current node is the root node
    if(nodes[i].metaData.startNode){
      type = "fsaStartState";
    }
    if(nodes[i].metaData.isTerminal !== undefined) {
      type = "fsaEndState";
      if (nodes[i].metaData.isTerminal === 'error') {
        type = "fsaErrorState";
      }
    }
    console.log(type);
    glGraph.nodes.push({
      group:"nodes",
      data: {id: graphID+nid, label: nodes[i].metaData.label, type: type, tooltip: nodes[i].variables, parent: graphID},
    });
  }
  let toEmbed = [];
  // add the edges between the nodes in the automaton to the graph
  const edges = Object.values(process.edgeMap);
  for(let i = 0; i < edges.length; i++){
    let label = edges[i].label;
    let tooltip = "";
    const from = graphID+'n' + edges[i].from;
    const to = graphID+'n' + edges[i].to;
    if (edges[i].metaData.broadcaster) {
      label += "?";
    } else if (edges[i].metaData.receiver) {
      label += "!";
    }
    let guard = edges[i].metaData.guard;
    if(guard !== undefined){
      let vars = guard.variables;
      if (guard.next !== undefined)
        tooltip =guard.next+"\n"+tooltip;
      tooltip =guard.guard+"\n"+tooltip;
      if (vars !== undefined)
        tooltip =vars+"\n"+tooltip;
    }
    if (edges[i].metaData.interrupt && hidden) {
      const toNode = process.nodeMap[edges[i].to];
      //Destroy all interrupt edges besides the last one.
      if (toNode.incomingEdges.indexOf(edges[i].id) != toNode.incomingEdges.length-1) {
        toEmbed.push(from);
        continue;
      }
      lastBox = _box(glGraph, toEmbed, graphID+"."+(interruptId++),graphID);
      //Now that all the children are inside box, toEmbed should only contain the box, plus the next node
      toEmbed = [ lastBox, _link(lastBox,to, label,tooltip, glGraph, lastBox), to];
      continue;
    }
    toEmbed.push(_link(from,to, label,tooltip,glGraph, lastBox));
    toEmbed.push(from);
    toEmbed.push(to);
  }
}

function _link(source, target, label, tooltip, glGraph, lastBox) {
  glGraph.edges.push({
    group: "edges",
    data: {id:source+"->"+target,label: label, tooltip: tooltip,source: source,target: target, parent: lastBox},
  });
  return source+"->"+target;
}
function _box(glGraph, toEmbed, name, graphID) {
  glGraph.interrupts.push("boxNode"+name);
  //we need to use unshift here, as the parents need to load before the children.
  glGraph.nodes.unshift({
    group:"nodes",
    data: {id: "boxNode"+name, type: 'Interrupt', parent: graphID, label: name},
  });
  //Remove embedded cells from the parent and add them to the box
  toEmbed.forEach(embed => {
    let el = _.findWhere(glGraph.nodes,{data:{id: embed}});
    if (!el) {
      el = _.findWhere(glGraph.edges,{data:{id: embed}});
    }
    el.data.parent = "boxNode"+name;
  });
  return "boxNode"+name;
}
function first(data) {
  for(let key in data){
    if(data.hasOwnProperty(key)){
      return key;
    }
  }
}
function visualizePetriNet(process, graphID, hidden) {

}
function getCytoscapeStyle() {
  return [
    {
      selector: 'node',
      style: {
        'background-color': Colours.grey,
        'label': 'data(label)',
        "text-valign" : "center",
        "text-halign" : "center",
        'font-size': '15',
        'font-weight': 'bold',
        'border-width': '3px',
      }
    },
    {
      selector: 'node[type=\'fsaStartState\']',
      style: {
        'border-style': 'double',
        'background-color': Colours.green,
        'border-width': '10px',
      }
    },
    {
      selector: 'node[type=\'fsaEndState\']',
      style: {
        'border-style': 'double',
        'background-color': Colours.red,
        'border-width': '10px',
      }
    },
    {
      selector: ':parent',
      style: {
        'background-opacity': 0.333,
        "text-valign" : "top",
      }
    },
    {
      selector: 'edge',
      style: {
        'width': 3,
        'line-color': 'black',
        'label': 'data(label)',
        'curve-style': 'bezier',
        'font-size': '18',
        'font-weight': 'bold',
        'target-arrow-color': 'black',
        'target-arrow-shape': 'triangle',
        'text-background-opacity': 1,
        'text-background-color': '#ffffff',
        'text-background-shape': 'rectangle',
        'text-rotation': 'autorotate'
      }
    }
  ]
}
