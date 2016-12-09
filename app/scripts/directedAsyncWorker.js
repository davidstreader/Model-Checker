
importScripts("../../bower_components/lodash/lodash.js");
var window = {_:_};
importScripts("../../bower_components/graphlib/dist/graphlib.core.js");
window.graphlib = graphlib;
importScripts("../../bower_components/dagre/dist/dagre.core.js");
var dagre = window.dagre;
onmessage = e => {

  var opt = e.data.opt;
  var graph = e.data.graph;

  var glGraphType = {directed: true, compound: true, multigraph: true};
  var glGraph = new graphlib.Graph(glGraphType);
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
  graph = [];
  var glLabel = {};
  var marginX = opt.marginX || 0;
  var marginY = opt.marginY || 0;

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
  glGraph.nodes().forEach(function(v) {
    var glNode = glGraph.node(v);
    graph.push({
      type: 'node',
      id: v,
      x: glNode.x - glNode.width / 2,
      y: glNode.y - glNode.height / 2
    });
  });

  // Import all edges.
  glGraph.edges().forEach(function(edgeObj) {
    var glEdge = glGraph.edge(edgeObj);
    var points = glEdge.points || [];
    graph.push({type:'link',id:edgeObj.name,vertices:points.slice(1, points.length - 1)});
  });
  postMessage(graph);
  close();
}
