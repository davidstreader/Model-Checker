'use strict';
function visualizeAutomata(process, name, graphMap, jgraph, hidden) {
  // add nodes in automaton to the graph
  const nodes = process.nodes;
  const nodeMap = {};
  const parentNode = new joint.shapes.parent();
  parentNode.name = name;
  let interruptId = 1;
  const interrupts = [];
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
      if (nodes[i].getMetaData('variables') !== undefined) {
        nodeMap['n' + nodes[i].id].set("tooltip",nodes[i].getMetaData('variables'));
      }
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
    if (nodes[i].getMetaData('variables') !== undefined) {
      nodeMap['n' + nodes[i].id].set("tooltip",nodes[i].getMetaData('variables'));
    }
    parentNode.embed(nodeMap['n' + nodes[i].id]);
    jgraph.addCell(nodeMap['n' + nodes[i].id]);
  }
  let toEmbed = [];
  // add the edges between the nodes in the automaton to the graph
  const edges = process.edges;
  for(let i = 0; i < edges.length; i++){
    let label = edges[i].label;
    let tooltip = "";
    const from = 'n' + edges[i].from;
    const to = 'n' + edges[i].to;
    let vars = edges[i].getMetaData('variables');
    if(edges[i].getMetaData('guard') !== undefined){
      if (edges[i].getMetaData('next') !== undefined)
        tooltip =edges[i].getMetaData('next')+"\n"+tooltip;
      tooltip =edges[i].getMetaData('guard')+"\n"+tooltip;
      if (vars !== undefined)
        tooltip =vars+"\n"+tooltip;
    }
    if (edges[i].metaData.interrupt && hidden) {
      var toNode = process.nodeMap[edges[i].to];
      //Destroy all interrupt edges besides the last one.
      if (toNode.incomingEdges.indexOf(edges[i].id) != toNode.incomingEdges.length-1) {
        toEmbed.push(nodeMap[from]);
        continue;
      }

      const target = nodeMap[to];
      const box = _box(jgraph, parentNode, toEmbed, name+"."+(interruptId++),graphMap, name);
      box.toDelete = _link(nodeMap[from],box.embedNode, "","",parentNode,jgraph);
      const link =_link(box.embedNode,target, label,tooltip,parentNode,jgraph);
      //move all elements in front of the link
      box.toFront();
      toEmbed.forEach(cell => {
        cell.toFront();
        cell.getEmbeddedCells().forEach(cell2 => {
          cell2.toFront();
        });
      });
      //Now that all the children are inside box, toEmbed should only contain the box, plus the next node
      toEmbed = [box,target, link];
      continue;
    }
    toEmbed.push(_link(nodeMap[from],nodeMap[to], label,tooltip,parentNode,jgraph));
    toEmbed.push(nodeMap[from]);
    toEmbed.push(nodeMap[to]);
  }
}
function _box(jgraph, parent, toEmbed, name, graphMap, key) {
  const boxNode = new joint.shapes.box({
    type:'interruptParentNode'
  });
  const embedNode = new joint.shapes.box({
    type:'interruptEmbedNode'
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
function first(data) {
  for(let key in data){
    if(data.hasOwnProperty(key)){
      return key;
    }
  }
}
function visualizePetriNet(process, name, graphMap, jgraph, hidden) {
  const nodeMap = {};
  const parentNode = new joint.shapes.parent();
  let interruptId = 1;
  parentNode.name = name;
  jgraph.addCell(parentNode);
  graphMap[name] = {name:name,height:0,parentNode:parentNode};
  const places = process.places;
  const places2 = _.map(places,place=>place.id);
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

  let toEmbed = [];
  // add transitions to the graph
  const transitions = process.transitions;
  for(let i = 0; i < transitions.length; i++){
    const incoming = transitions[i].incomingPlaces;
    const outgoing = transitions[i].outgoingPlaces;
    let label = transitions[i].label;
    let tooltip = "";
    if (transitions[i].metaData.interrupt && hidden) {
      //inCom is expensive to calculate and needs to be done once per interrupt, so store it.
      //inCom needs to be a list of all places leading to this interrupt.
      //We do this by getting the node this transition leads to, and then getting all transitions from that node.
      //We can then map the transitions to the first element in their incomingPlaceSet (as interrupts will only have one incoming node)
      //and then we finally sort inCom so that its in order of the tree.
      //this way, the node at the end of inCom will be one of the rightMost elements, and this is important as they are the easiest
      //nodes to link from when creating fake nodes for dagre placement.
      const inCom = transitions[i].metaData.inCom || _.map(process.placeMap[outgoing[0]].incomingTransitionSet,(val,transition)=>first(process.transitionMap[transition].incomingPlaceSet)).sort(place => places2.indexOf(place));
      transitions[i].metaData.inCom = inCom;
      if (incoming[0] !== inCom[inCom.length-1]) {
        continue;
      }
      //At this point, we know that inCom contains all nodes from the interrupted process, so add all of them.
      toEmbed = toEmbed.concat(inCom.map(node => nodeMap['p'+node]));
      //At this point, we dont actually want to create any transitions.
      //All we want to do is link from the last incoming to the last outgoing.
      const to = 'p' + outgoing[0];
      const from = 'p' + incoming[0];
      const target = nodeMap[to];
      const box = _box(jgraph, parentNode, toEmbed, name+"."+(interruptId++),graphMap, name);
      let transition = new joint.shapes.pn.Transition({
        attrs: { text : { text: label }}
      });
      parentNode.embed(transition);
      jgraph.addCell(transition);
      box.toDelete = _link(nodeMap[from],box.embedNode, "",'',parentNode,jgraph);
      box.embedLink = [_link(box.embedNode,transition, '','',parentNode,jgraph),transition,_link(transition,target, '','',parentNode,jgraph)];
      //move all elements in front of the link
      box.toFront();
      toEmbed.forEach(cell => {
        cell.toFront();
        cell.getEmbeddedCells().forEach(cell2 => {
          cell2.toFront();
        });
      });
      //Now that all the children are inside box, toEmbed should only contain the box, plus the next node
      toEmbed = [box,target].concat(box.embedLink);
      continue;
    }
    let vars = transitions[i].getMetaData('variables');
    if(transitions[i].getMetaData('guard') !== undefined){
      if (transitions[i].getMetaData('next') !== undefined)
        tooltip =transitions[i].getMetaData('next')+"\n"+tooltip;
      tooltip =transitions[i].getMetaData('guard')+"\n"+tooltip;
      if (vars !== undefined)
        tooltip =vars+"\n"+tooltip;
    }
    nodeMap['t' + transitions[i].id] = new joint.shapes.pn.Transition({
      attrs: {
        text : { text: label }
      }
    });
    nodeMap['t' + transitions[i].id].set("tooltip",tooltip);
    parentNode.embed(nodeMap['t' + transitions[i].id]);
    jgraph.addCell(nodeMap['t' + transitions[i].id]);
    for(let j = 0; j < outgoing.length; j++){
      const from = 't' + transitions[i].id;
      const to = 'p' + outgoing[j];
      toEmbed.push(_link(nodeMap[from],nodeMap[to], '','',parentNode,jgraph));
      toEmbed.push(nodeMap[from]);
      toEmbed.push(nodeMap[to]);
    }

    for(let j = 0; j < incoming.length; j++){
      const from = 'p' + incoming[j];
      const to = 't' + transitions[i].id;
      toEmbed.push(_link(nodeMap[from],nodeMap[to],'','',parentNode,jgraph));
      toEmbed.push(nodeMap[from]);
      toEmbed.push(nodeMap[to]);
    }

  }
}

function _link(source, target, label,tooltip, parentNode, jgraph) {
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
  cell.set("tooltip",tooltip);
  parentNode.embed(cell);
  jgraph.addCell(cell);
  return cell;
}
function addLabelAndPadding(graphMap, key, jgraph) {
  graphMap[key].parentNode.fitEmbeds({padding:50});
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
function constructGraphs(graphMap, id, hidden, callback) {
  //Find the process by id
  let graph = _.findWhere(app.get("automata.values"), {id: id});
  if (hidden)
    id += ".hidden";
  if (!graph.type || (graphMap[id] && app.get("automata.analysis")[id] &&  !app.get("automata.analysis")[id].isUpdated)) {
    callback();
    return;
  }
  //Calculate the bottom of the last drawn graph
  let tmpjgraph = new joint.dia.Graph();
  if (graph.type == 'automata') {
    visualizeAutomata(graph,id,graphMap,tmpjgraph, hidden);
  }
  if (graph.type == 'petrinet') {
    visualizePetriNet(graph,id,graphMap,tmpjgraph, hidden);
  }
  //We do not want to rescale the graph if it has already been rescaled.
  if (graph.type == 'interrupt') return;
  //Pass this through to dagre to get everything laid out
  joint.layout.DirectedGraph.layout(tmpjgraph, {}, ()=>{
    addLabelAndPadding(graphMap,id,tmpjgraph);
    if (graphMap[id].interrupts) {
      _.each(graphMap[id].interrupts,graph => {
        graph.name = graph.name.replace(".hidden","");
        const id = parseInt(graph.name.split(".")[1]);
        const bbox = graph.parentNode.getBBox().origin();
        const cell = new joint.shapes.basic.Rect({
          type: "interruptLabel",
          size: {width: 100, height: 30},
          position: {x:bbox.x,y:bbox.y-25*id},
          attrs: {
            rect: {fill: 'transparent', stroke: 'none'},
            'text': {text: graph.name, fill: 'red', 'font-size': 20}
          }
        });
        const bbox2 = graph.parentNode.getBBox();
        if (graph.parentNode.embedLink) {
          graph.parentNode.embedLink.forEach(link=>link.toFront());
        }
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
    callback();
  });

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
