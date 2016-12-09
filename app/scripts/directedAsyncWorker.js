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
//dagre is added to the window normally. Pull it out so we can use it.
const dagre = window.dagre;
onmessage = e => {
  //Load data sent from the main thread
  const opt = e.data.opt;
  let graph = e.data.graph;
  //Dagre settings
  const glGraphType = {directed: true, compound: true, multigraph: true};
  //Construct a dagre graph
  const glGraph = new graphlib.Graph(glGraphType);
  //The main thread passes through an array representation of all the graphs.
  //At this point, we can import that into dagre
  graph.forEach(cell => {
    switch (cell.type) {
      case 'edge':
        glGraph.setEdge(cell.source, cell.target, cell.label, cell.name);
        break;
      case 'cell':
        glGraph.setNode(cell.id, cell.label);
        break;
      case 'parent':
        glGraph.setParent(cell.id, cell.parent);
        break;
    }
  });
  //Clear the imported object so we can reuse it for export
  graph = [];
  //Some more dagre settings.
  const glLabel = {};
  const marginX = opt.marginX || 0;
  const marginY = opt.marginY || 0;

  // Dagre layout accepts options as lower case.
  // Direction for rank nodes. Can be TB, BT, LR, or RL
  glLabel.rankdir = "LR";
  // Alignment for rank nodes. Can be UL, UR, DL, or DR
  if (opt.align) glLabel.align = opt.align;
  // Number of pixels that separate nodes horizontally in the layout.
  if (opt.nodeSep) glLabel.nodesep = opt.nodeSep;
  // Number of pixels that separate edges horizontally in the layout.
  if (opt.edgeSep) glLabel.edgesep = opt.edgeSep;
  // Number of pixels between each rank in the layout.
  if (opt.rankSep) glLabel.ranksep = opt.rankSep;
  // Number of pixels to use as a margin around the left and right of the graph.
  if (marginX) glLabel.marginx = marginX;
  // Number of pixels to use as a margin around the top and bottom of the graph.
  if (marginY) glLabel.marginy = marginY;

  // Set the option object for the graph label.
  glGraph.setGraph(glLabel);

  // Executes the layout.
  dagre.layout(glGraph, { debugTiming: !!opt.debugTiming });
  //Convert dagres graph to an array of edges and nodes
  glGraph.nodes().forEach(function(v) {
    const glNode = glGraph.node(v);
    graph.push({
      type: 'node',
      id: v,
      x: glNode.x - glNode.width / 2,
      y: glNode.y - glNode.height / 2
    });
  });

  // Import all edges.
  glGraph.edges().forEach(function(edgeObj) {
    const glEdge = glGraph.edge(edgeObj);
    const points = glEdge.points || [];
    graph.push({type:'link',id:edgeObj.name,vertices:points.slice(1, points.length - 1)});
  });
  //push the converstion back to the main thread
  postMessage(graph);
  //Since each dagre instance fires up a thread, its best to kill it now.
  close();
}
