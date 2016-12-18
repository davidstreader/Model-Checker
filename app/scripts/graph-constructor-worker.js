//Load all imports required for dagre.
//Note: since we dont have a real window variable, we can just create one and assign things to it.
const window = {};
importScripts("../../bower_components/lodash/lodash.js");
//Add lodash to window
window._ = _;
importScripts("../../bower_components/graphlib/dist/graphlib.core.js");
//add graphlib to window
window.graphlib = graphlib;
importScripts("../../bower_components/dagre/dist/dagre.core.js");

importScripts("constants.js");
importScripts("process-models/automaton.js");
importScripts("process-models/petrinet.js");
//dagre is added to the window normally. Pull it out so we can use it.
const dagre = window.dagre;
/**
 * @param e
 */
onmessage = e => {
  //Load data sent from the main thread
  let graph = e.data.graph;
  const id = e.data.id;
  const hidden = e.data.hidden;
  //Dagre settings
  const glGraphType = {directed: true, compound: true, multigraph: true};
  //Construct a dagre graph
  let glGraph = new graphlib.Graph(glGraphType);
  glGraph.interrupts = [];
  //At this point, we can import that into dagre
  if (graph.type == 'automata') {
    graph = AUTOMATON.convert(graph);
    visualizeAutomata(graph,id,glGraph, hidden);
  }
  if (graph.type == 'petrinet') {
    graph = PETRI_NET.convert(graph);
    visualizePetriNet(graph,id,glGraph, hidden);
  }
  //We do not want to rescale the graph if it has already been rescaled.
  if (graph.type == 'interrupt') return;
  layoutGraph(glGraph);
  _.each(glGraph.interrupts,interrupt=>{
    const parentNode = glGraph.node(interrupt);
    //Add a label to each interrupt
    glGraph.setNode(interrupt+"label",{type:'interruptLabel',id:generateUuid(),
      position: {x:(parentNode.x-parentNode.x/2)-100,y:(parentNode.y-parentNode.y/2)-45},
      size: {width:100,height:50},
      z:2,
      attrs: {
        'text': {text: interrupt.replace("box","").replace(".hidden","").replace("Node","")}
      }});
    glGraph.setParent(interrupt+"label",interrupt);
  });
  const alphabet = graph.alphabet;
  //Clear the imported object so we can reuse it for export
  graph = [];
  //Convert dagres graph to an array of edges and nodes
  _.each(glGraph._nodes,function(glNode,v) {
    if (glNode==undefined) return;
    if (!glNode.position)
      glNode.position = {x:glNode.x - glNode.width / 2,y:glNode.y - glNode.height / 2};
    if (!glNode.size)
      glNode.size = {width:glNode.width ,height:glNode.height};
    delete glNode.x;
    delete glNode.y;
    delete glNode.width;
    delete glNode.height;
    if (glNode.boxId) {
      const parentNode = glGraph.node(glNode.boxId);
      glNode.position = parentNode.position;
      glNode.size = parentNode.size;
    }
    if (glGraph.parent(v)) {
      glNode.parent = glGraph.node(glGraph.parent(v)).id;
    }
    const children = glGraph.children(v);
    if (children) {
      glNode.embeds = [];
      _.each(children, child=>{
        if (glGraph.node(child))
          glNode.embeds.push(glGraph.node(child).id);
        else
          glNode.embeds.push(child);
      });
    }
    graph.push(glNode);
  });
  const parentNode = glGraph.node(id);
  // Import all edges.
  glGraph.edges().forEach(function(edgeObj) {
    //["type", "id", "z", "labels", "attrs", "smooth", "tooltip", "source", "target", "deleteMe", "opts", "points"]
    const glEdge = glGraph.edge(edgeObj);
    //Dont bother importing edges that have been flagged for deletion
    if (glEdge.deleteMe) return;
    //Allow skipping verts
    if (!glEdge.opts.clearVerts)
    // Remove the first and last point from points array.
    // Those are source/target element connection points
    // ie. they lies on the edge of connected elements.
      glEdge.vertices = glEdge.points.slice(1, glEdge.points.length - 1);
    glEdge.source = {id:glGraph.node(glEdge.source).id};
    glEdge.target = {id:glGraph.node(glEdge.target).id};
    if (!glEdge.parentNode) {
      parentNode.embeds = parentNode.embeds.concat(glEdge.id);
    } else {
      //This edge was set via an the _box function from interrupts
      //this means that the parent variable references a node by name
      //So we need to change that to id.
      const parentNode = glGraph.node(glEdge.parentNode);
      parentNode.embeds = parentNode.embeds.concat(glEdge.id);
    }
    graph.push(glEdge);
  });
  //push the converstion back to the main thread
  postMessage({cells:graph,alphabet:alphabet});
  //Since each dagre instance fires up a thread, its best to kill it now.
  close();
}
function layoutGraph(glGraph) {
  // Executes the layout.

  glGraph.setGraph({
    rankdir: 'LR',
    marginx: 0,
    marginy: 0
  });
  dagre.layout(glGraph);
  return glGraph;
}
function visualizeAutomata(process, graphID, glGraph, hidden) {
  // add nodes in automaton to the graph
  const nodes = process.nodes;
  glGraph.setNode(graphID,{type:'parent',id:generateUuid(),
    width: 0,
    height: 0,
    z:0});
  let interruptId = 1;
  for(let i = 0; i < nodes.length; i++){
    let attrs = {text : {text: nodes[i].metaData.label}};
    const nid = 'n' + nodes[i].id;
    let type = "fsa.State";
    let fill = Colours.grey;
    // check if current node is the root node
    if(nodes[i].getMetaData('startNode')){
      fill=Colours.blue;
    }
    if(nodes[i].getMetaData('isTerminal') !== undefined) {
      type = "fsa.EndState";
      fill = Colours.green;
      if (nodes[i].getMetaData('isTerminal') === 'error') {
        fill = Colours.red;
      }
      attrs['.outer'] = {'fill':fill};
    } else {
      attrs['circle'] = {'fill':fill};
    }
    glGraph.setNode(nid,{
      width: 50,
      height: 50,
      z:2,
      type:type, id:generateUuid(),
      attrs: attrs,
      tooltip: nodes[i].getMetaData('variables')
    });
    glGraph.setParent(nid,graphID);
  }
  let toEmbed = [];
  // add the edges between the nodes in the automaton to the graph
  const edges = process.edges;
  for(let i = 0; i < edges.length; i++){
    let label = edges[i].label;
    let tooltip = "";
    const from = 'n' + edges[i].from;
    const to = 'n' + edges[i].to;
    let guard = edges[i].getMetaData('guard');
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

      const box = _box(glGraph, toEmbed, graphID+"."+(interruptId++),graphID);
      _link(from,"embedNode"+box, "deleteMe","",glGraph,graphID);
      const link =_link("embedNode"+box,to, label,tooltip,glGraph,graphID,{clearVerts:true});
      //Now that all the children are inside box, toEmbed should only contain the box, plus the next node
      toEmbed = ["boxNode"+box,to, link];
      continue;
    }
    toEmbed.push(_link(from,to, label,tooltip,glGraph,graphID));
    toEmbed.push(from);
    toEmbed.push(to);
  }
}

function _link(source, target, label,tooltip, glGraph, graphID, opts) {
  const id = generateUuid();
  glGraph.setEdge(source,target,{
    type:"fsa.Arrow", id:id,
    z:2,
    labels: [{position: 0.5, attrs: {text: {text: label || '', 'font-weight': 'bold'}}}],
    attrs: {
      //Add a slight transparency, so that you can see the links behind
      'rect': {fill: Colours.textBackground}
    },
    smooth: false,
    tooltip: tooltip,
    source: source,
    target: target,
    deleteMe: label === "deleteMe",
    opts: opts ||{}
  },id+"");
  return id+"";
}
function _box(glGraph, toEmbed, name, graphID) {
  glGraph.interrupts.push("boxNode"+name);
  glGraph.setNode("boxNode"+name,{type:'interruptParentNode',id:generateUuid(),
    width: 0,
    height: 0,
    z:1});
  //We dont want the embed to show or have any interactions.
  glGraph.setNode("embedNode"+name,{type:'interruptEmbedNode',id:generateUuid(),
    attrs: {
      rect: {fill: 'transparent', stroke: 'none', visibility: 'hidden'}
    },
    width: 0,
    height: 0,
    boxId: "boxNode"+name,
    z:1});
  glGraph.setParent("boxNode"+name,graphID);
  glGraph.setParent("embedNode"+name,"boxNode"+name);
  //Remove embedded cells from the parent and add them to the box
  toEmbed.forEach(cell => {
    if (glGraph.node(cell))
      glGraph.setParent(cell,"boxNode"+name);
    else glGraph.parentNode = "boxNode"+name;
  });
  return name;
}
function first(data) {
  for(let key in data){
    if(data.hasOwnProperty(key)){
      return key;
    }
  }
}
function visualizePetriNet(process, graphID, glGraph, hidden) {
  glGraph.setNode(graphID,{type:'parent',id:generateUuid(),
    width: 50,
    height: 50,
    z:0});
  let interruptId = 1;
  const places = process.places;
  const places2 = _.map(places,place=>place.id);
  for(let i = 0; i < places.length; i++){
    let type;
    let attrs = {};
    // add to array of start places if necessary
    if(places[i].metaData.startPlace !== undefined){
      type = "pn.StartPlace";
    } else if(places[i].metaData.isTerminal !== undefined){
      const fill = places[i].metaData.isTerminal === 'stop' ? Colours.green : Colours.red;
      type ="pn.TerminalPlace";
      attrs = { '.outer': {'fill':fill} };
    } else{
      type = "pn.Place";
    }
    glGraph.setNode('p'+places[i].id,{type:type,id:generateUuid(),
      width: 50,
      height: 50,
      z:1,
      attrs:attrs,
      tooltip: places[i].getMetaData('variables')});
    glGraph.setParent('p'+places[i].id,graphID);
  }

  let toEmbed = [];
  // add transitions to the graph
  const transitions = process.transitions;
  for(let i = 0; i < transitions.length; i++){
    const incoming = transitions[i].incomingPlaces;
    const outgoing = transitions[i].outgoingPlaces;
    const tid ='t'+transitions[i].id;
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
      toEmbed = toEmbed.concat(inCom.map(node => 'p'+node));
      //At this point, we dont actually want to create any transitions.
      //All we want to do is link from the last incoming to the last outgoing.
      const target = 'p' + outgoing[0];
      const from = 'p' + incoming[0];
      const box = _box(glGraph, toEmbed, graphID+"."+(interruptId++), graphID);
      glGraph.setNode(tid,{
        width: 50,
        height: 50,
        z:2,
        type:"pn.Transition", id:generateUuid(),
        attrs: {text : {text:label}},
        tooltip: tooltip
      });
      glGraph.setParent(tid,graphID);
      _link(from,"embedNode"+box, "deleteMe",'',glGraph,graphID);

      //Now that all the children are inside box, toEmbed should only contain the box, plus the next node and the transition + links
      toEmbed = ["boxNode"+box,target,tid,_link("embedNode"+box,tid, '','',glGraph,graphID,{clearVerts:true}),_link(tid,target, '','',glGraph,graphID,{clearVerts:true})];
      continue;
    }

    let guard = transitions[i].getMetaData('guard');
    if(guard !== undefined){
      let vars = guard.variables;
      if (guard.next !== undefined)
        tooltip =guard.next+"\n"+tooltip;
      tooltip =guard.guard+"\n"+tooltip;
      if (vars !== undefined)
        tooltip =vars+"\n"+tooltip;
    }
    glGraph.setNode(tid,{
      width: 50,
      height: 50,
      z:2,
      type:"pn.Transition", id:generateUuid(),
      attrs: {text : {text:label}},
      tooltip: tooltip
    });
    glGraph.setParent(tid,graphID);
    for(let j = 0; j < outgoing.length; j++){
      const from = tid;
      const to = 'p' + outgoing[j];
      toEmbed.push(_link(from,to, '','',glGraph,graphID));
      toEmbed.push(from);
      toEmbed.push(to);
    }

    for(let j = 0; j < incoming.length; j++){
      const from = 'p' + incoming[j];
      const to = tid;
      toEmbed.push(_link(from,to,'','',glGraph,graphID));
      toEmbed.push(from);
      toEmbed.push(to);
    }

  }
}
//Generate a UUID for joint
//From stackoverflow
const lut = []; for (let i=0; i<256; i++) { lut[i] = (i<16?'0':'')+(i).toString(16); }
function generateUuid()
{
  const d0 = Math.random() * 0xffffffff | 0;
  const d1 = Math.random() * 0xffffffff | 0;
  const d2 = Math.random() * 0xffffffff | 0;
  const d3 = Math.random() * 0xffffffff | 0;
  return lut[d0&0xff]+lut[d0>>8&0xff]+lut[d0>>16&0xff]+lut[d0>>24&0xff]+'-'+
    lut[d1&0xff]+lut[d1>>8&0xff]+'-'+lut[d1>>16&0x0f|0x40]+lut[d1>>24&0xff]+'-'+
    lut[d2&0x3f|0x80]+lut[d2>>8&0xff]+'-'+lut[d2>>16&0xff]+lut[d2>>24&0xff]+
    lut[d3&0xff]+lut[d3>>8&0xff]+lut[d3>>16&0xff]+lut[d3>>24&0xff];
}
