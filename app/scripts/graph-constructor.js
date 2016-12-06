'use strict';
function visualizeAutomata(process, name, graphMap, jgraph) {
  // add nodes in automaton to the graph
  const nodes = process.nodes;
  const nodeMap = {};
  const parentNode = new joint.shapes.parent();
  parentNode.name = name;
  let interruptId = 1;
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
      nodeMap['n' + nodes[i].id].metaData = nodes[i].metaData;
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
    nodeMap['n' + nodes[i].id].metaData = nodes[i].metaData;
    parentNode.embed(nodeMap['n' + nodes[i].id]);
    jgraph.addCell(nodeMap['n' + nodes[i].id]);
  }
  let interruptNames = [];
  let toEmbed = [];
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
      if (vars !== undefined)
        label =vars+"\n"+label;
    }
    if (nodeMap[from].metaData.interrupt) {
      toEmbed.splice(toEmbed.indexOf(nodeMap[from],1));
      const connected = jgraph.getConnectedLinks(nodeMap[from]);
      let lastCell;
      //Loop through all embedded cells and find one that links to nowhere
      connected.forEach(link => {
        var cell = jgraph.getCell(link.get("source"));
        if (cell.attributes.type == 'fsa.EndState') {
          lastCell = cell;
        }
      })
      jgraph.removeCells(connected);
      toEmbed = _.difference(toEmbed,connected);
      const box = _box(jgraph, parentNode, toEmbed, name+"."+(interruptId++),graphMap, name);
      const link = _link(nodeMap[from],nodeMap[to], label,parentNode,jgraph);
      const link2 = _link(box.embedNode,nodeMap[from], nodeMap[from].metaData.interrupt.action.action,parentNode,jgraph);
      box.toDelete =_link(lastCell,box.embedNode, "",parentNode,jgraph);
      //move all elements in front of the link
      box.toFront();
      toEmbed.forEach(cell => {
        cell.toFront();
        cell.getEmbeddedCells().forEach(cell2 => {
          cell2.toFront();
        });
      });

      //Now that all the children are inside box, toEmbed should only contain the box, plus the next node
      toEmbed = [link,box,nodeMap[from],link2];

    }  else {
      toEmbed.push(_link(nodeMap[from],nodeMap[to], label,parentNode,jgraph));
      toEmbed.push(nodeMap[from]);
      toEmbed.push(nodeMap[to]);
    }
  }
}
function _box(jgraph, parent, toEmbed, name, graphMap, key) {
  const boxNode = new joint.shapes.box({
    type:'parentNode'
  });
  const embedNode = new joint.shapes.box({
    type:'embedNode'
  });
  parent.embed(boxNode);
  jgraph.addCell(embedNode);
  boxNode.embed(embedNode);
  jgraph.addCell(boxNode);
  //Remove embedded cells from the parent and add them to the box
  toEmbed.forEach(cell => {parent.unembed(cell);boxNode.embed(cell);});
  if (!graphMap[key].interrupts) graphMap[key].interrupts = [];
  graphMap[key].interrupts.push({name:name,parentNode:boxNode});
  boxNode.embedNode = embedNode;
  return boxNode;
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
      if (vars !== undefined)
        label =vars+"\n"+label;
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
      'rect': {fill: Colours.textBackground}
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
  let interruptHeight = 0;
  if (graphMap[key].interrupts) {
    interruptHeight = graphMap[key].interrupts.length * 30;
  }
  const cell = new joint.shapes.basic.Rect({
    size: {width: 100, height: 30},
    position: {x: lx - 50, y: ly - 50-interruptHeight+10},
    attrs: {
      rect: {fill: 'transparent', stroke: 'none'},
      'text': {text: key, fill: 'red', 'font-size': 20}
    }
  });

  graphMap[key].parentNode.embed(cell);
  graphMap[key].label = cell;
  jgraph.addCell(cell);
  graphMap[key].parentNode.resize(width+100,height+100+interruptHeight);
  //Move the parent node without moving its children, to add a padding around it
  //position needs to subtract intersize to center interrupted components
  graphMap[key].parentNode.position(lx-50,ly-50-interruptHeight);
  //But translate now needs to move everything back so its at its old position, but with a changed padding.
  //Now move the parent node and its components back by the padding
  graphMap[key].parentNode.translate(50, 50+interruptHeight);
  //Move the component back to the origin with a bit of padding
  graphMap[key].parentNode.translate(50, -ly+50);
}
function constructGraphs(graphMap, id) {
  //Find the process by id
  let graph = _.findWhere(app.get("automata.values"), {id: id});
  if (!graph.type || (graphMap[id] && app.get("automata.analysis")[graph.id] &&  !app.get("automata.analysis")[graph.id].isUpdated)) return;
  //Calculate the bottom of the last drawn graph
  let tmpjgraph = new joint.dia.Graph();
  if (graph.type == 'automata') {
    visualizeAutomata(graph,graph.id,graphMap,tmpjgraph);
  }
  if (graph.type == 'petrinet') {
    visualizePetriNet(graph,graph.id,graphMap,tmpjgraph);
  }
  //We do not want to rescale the graph if it has already been rescaled.
  if (graph.type == 'interrupt') return;
  //Pass this through to dagre to get everything laid out
  joint.layout.DirectedGraph.layout(tmpjgraph, {rankDir:'LR',setLinkVertices: true});
  addLabelAndPadding(graphMap,graph.id,tmpjgraph);
  if (graphMap[graph.id].interrupts) {
    _.each(graphMap[graph.id].interrupts,graph => {
      const id = parseInt(graph.name.split(".")[1]);
      const bbox = graph.parentNode.getBBox().origin();
      const cell = new joint.shapes.basic.Rect({
        type: "boxLabel",
        size: {width: 100, height: 30},
        position: {x:bbox.x,y:bbox.y-25*id},
        attrs: {
          rect: {fill: 'transparent', stroke: 'none'},
          'text': {text: graph.name, fill: 'red', 'font-size': 20}
        }
      });
      const bbox2 = graph.parentNode.getBBox();
      graph.parentNode.toDelete.remove();
      graph.parentNode.embed(cell);
      graph.parentNode.resize(bbox2.width,bbox2.height+id*30,{direction:"top"});
      graph.parentNode.resize(bbox2.width-50,bbox2.height+id*30,{direction:"right"});
      graph.parentNode.embedNode.set('position',graph.parentNode.get('position'));
      graph.parentNode.embedNode.set('size',graph.parentNode.get('size'));
      tmpjgraph.addCell(cell);
      graph.label = cell;
      graph.id = graph.name;
      graph.type = 'interrupt';
      graphMap[graph.name] = graph;
    });
  }
}
/**
 * Move a cells vertices when moving the cell
 * @param graph The graph
 * @param cell the cell
 */
function adjustVertices(graph, cell) {

  //TODO: it would be nice if the distance moved was proportional to the distance from the other cell
  // If the cell is a view, find its model.
  cell = cell.model || cell;
  //Ignore all clicks that arent on a cell
  if (!cell.attributes.position) return;
  const {x:nx,y:ny} = cell.get("position");
  const {x:ox,y:oy} = cell.previous("position");
  const diff = {x:nx-ox,y:ny-oy};
  //Lets just nuke the original verticies.
  _.each(graph.getConnectedLinks(cell),link =>{
    let verticies = [];
    _.each(link.get("vertices"),function (vert) {
      verticies.push({x:vert.x+diff.x,y:vert.y+diff.y});
    })
    link.set('vertices', verticies);
  });
}
let subtree = [];
function collectDeepEmbedded(cell) {
  subtree = [];
  _collectDeepEmbedded(cell);
  return subtree;
}
function _collectDeepEmbedded(cell) {
  _.each(cell.getEmbeddedCells(), function(c) {
    subtree.push(c);
    _collectDeepEmbedded(c);
  })
}
