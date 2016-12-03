'use strict';
function visualizeAutomata(process, name, graphMap, jgraph) {
  // add nodes in automaton to the graph
  const nodes = process.nodes;
  const nodeMap = {};
  const parentNode = new joint.shapes.parent();
  parentNode.name = name;
  jgraph.addCell(parentNode);
  graphMap[name] = {name:name,height:0,parentNode:parentNode};
  for(let i = 0; i < nodes.length; i++){
    if(nodes[i].getMetaData('isTerminal') !== undefined){
      let fill = Colours.green;
      if (nodes[i].getMetaData('isTerminal') === 'error') {
        fill = Colours.red;
      }
      nodeMap['n' + nodes[i].id] = new joint.shapes.fsa.EndState({
        attrs: { text : { text: nodes[i].metaData.label}, '.outer': {'fill':fill} }
      });
      parentNode.embed(nodeMap['n' + nodes[i].id]);
      jgraph.addCell(nodeMap['n' + nodes[i].id]);
      continue;
    }
    let fill = Colours.grey;
    // check if current node is the root node
    if(nodes[i].getMetaData('startNode')){
      fill=Colours.blue;
    }
    // add node to graph
    nodeMap['n' + nodes[i].id] = new joint.shapes.fsa.State({
      size: { width: 60, height: 60 },
      attrs: { text : { text: nodes[i].metaData.label}, circle: {'fill':fill} }
    });
    parentNode.embed(nodeMap['n' + nodes[i].id]);
    jgraph.addCell(nodeMap['n' + nodes[i].id]);
  }

  // add the edges between the nodes in the automaton to the graph
  const edges = process.edges;
  for(let i = 0; i < edges.length; i++){
    let label = edges[i].label;
    const from = 'n' + edges[i].from;
    const to = 'n' + edges[i].to;
    let vars = edges[i].getMetaData('variables');
    if(edges[i].getMetaData('guard') !== undefined){
      if (edges[i].getMetaData('next') !== undefined)
        label =edges[i].getMetaData('next')+"\n"+label;
      label =edges[i].getMetaData('guard')+"\n"+label;
      if (vars !== undefined) label = "\n"+label;
      _.each(vars,(variable,i) => {
        label =(vars.length==i+1?"":",")+variable+label;
      })
    }
    _link(nodeMap[from],nodeMap[to], label,parentNode,jgraph);
  }
}
function visualizePetriNet(process, name, graphMap, jgraph) {
  const nodeMap = {};
  const parentNode = new joint.shapes.parent();
  parentNode.name = name;
  jgraph.addCell(parentNode);
  graphMap[name] = {name:name,height:0,parentNode:parentNode};
  const places = process.places;
  for(let i = 0; i < places.length; i++){
    let cell;
    // add to array of start places if necessary
    if(places[i].metaData.startPlace !== undefined){
      cell = new joint.shapes.pn.StartPlace();
    }
    else if(places[i].metaData.isTerminal !== undefined){
      const fill = places[i].metaData.isTerminal === 'stop' ? Colours.green : Colours.red;
      cell = new joint.shapes.pn.TerminalPlace({ attrs: { '.outer': {'fill':fill} }});
    }
    else{
      cell = new joint.shapes.pn.Place();
    }
    parentNode.embed(cell);
    jgraph.addCell(cell);
    nodeMap['p' + places[i].id] = cell;
  }

  // add transitions to the graph
  const transitions = process.transitions;
  for(let i = 0; i < transitions.length; i++){
    let label = transitions[i].label;
    let vars = transitions[i].getMetaData('variables');
    if(transitions[i].getMetaData('guard') !== undefined){
      if (transitions[i].getMetaData('next') !== undefined)
        label =transitions[i].getMetaData('next')+"\n"+label;
      label =transitions[i].getMetaData('guard')+"\n"+label;
      if (vars !== undefined) label = "\n"+label;
      _.each(vars,(variable,i) => {
        label =(vars.length==i+1?"":",")+variable+label;
      })
    }
    nodeMap['t' + transitions[i].id] = new joint.shapes.pn.Transition({
      attrs: { text : { text: label }}
    });
    parentNode.embed(nodeMap['t' + transitions[i].id]);
    jgraph.addCell(nodeMap['t' + transitions[i].id]);
    const outgoing = transitions[i].outgoingPlaces;
    for(let j = 0; j < outgoing.length; j++){
      const from = 't' + transitions[i].id;
      const to = 'p' + outgoing[j];
      _link(nodeMap[from],nodeMap[to],'',parentNode,jgraph);
    }

    const incoming = transitions[i].incomingPlaces;
    for(let j = 0; j < incoming.length; j++){
      const from = 'p' + incoming[j];
      const to = 't' + transitions[i].id;
      _link(nodeMap[from],nodeMap[to],'',parentNode,jgraph);
    }
  }
}

function _link(source, target, label, parentNode, jgraph) {
  const cell = new joint.shapes.fsa.Arrow({
    source: {id: source.id},
    target: {id: target.id},
    labels: [{position: 0.5, attrs: {text: {text: label || '', 'font-weight': 'bold'}}}],
    smooth: false,
    attrs: {
      //Add a slight transparency, so that you can see the links behind
      'rect': {fill: 'rgba(255, 255, 255, 0.3)'}
    }
  });
  parentNode.embed(cell);
  jgraph.addCell(cell);
  return cell;
}
function addLabelAndPadding(graphMap, key, jgraph) {
  const bbox = graphMap[key].parentNode.getBBox();
  const lx = bbox.origin().x, ly = bbox.origin().y, ux = bbox.corner().x, uy = bbox.corner().y;
  const width = ux - lx;
  const height = uy - ly;
  const cell = new joint.shapes.basic.Rect({
    size: {width: 100, height: 30},
    position: {x: lx - 50, y: ly - 50},
    attrs: {
      rect: {fill: 'transparent', stroke: 'none'},
      'text': {text: key, fill: 'red', 'font-size': 20}
    }
  });

  graphMap[key].parentNode.embed(cell);
  graphMap[key].label = cell;
  jgraph.addCell(cell);
  graphMap[key].parentNode.resize(width+100,height+100);
  //Move the parent node without moving its children, to add a padding around it
  graphMap[key].parentNode.position(lx-50,ly-50);
  //Now move the parent node and its components back by the padding
  graphMap[key].parentNode.translate(50, 50);
  //Move the component back to the origin with a bit of padding
  graphMap[key].parentNode.translate(50, -ly+50);
}
function constructGraphs(graphMap, id) {
  //Find the process by id
  let graph = _.findWhere(app.get("automata.values"), {id: id});
  if (!graph.type || (graphMap[id] && !app.get("automata.analysis")[graph.id].isUpdated)) return;
  //Calculate the bottom of the last drawn graph
  let tmpjgraph = new joint.dia.Graph();
  if (graph.type == 'automata') {
    visualizeAutomata(graph,graph.id,graphMap,tmpjgraph);
  }
  if (graph.type == 'petrinet') {
    visualizePetriNet(graph,graph.id,graphMap,tmpjgraph);
  }
  //Pass this through to dagre to get everything laid out
  joint.layout.DirectedGraph.layout(tmpjgraph, {rankDir:'LR',setLinkVertices: true});
  addLabelAndPadding(graphMap,graph.id,tmpjgraph);
}
