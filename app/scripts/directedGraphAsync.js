/**
 * A copy paste of https://github.com/clientIO/joint/blob/master/plugins/layout/DirectedGraph/joint.layout.DirectedGraph.js
 * But modified to use a worker thread.
 */
if (typeof exports === 'object') {

  var graphlib = require('graphlib');
  var dagre = require('dagre');
}

// In the browser, these variables are set to undefined because of JavaScript hoisting.
// In that case, should grab them from the window object.
graphlib = graphlib || (typeof window !== 'undefined' && window.graphlib);
dagre = dagre || (typeof window !== 'undefined' && window.dagre);

joint.layout.DirectedGraph = {

  layout: function(graphOrCells, opt, callback) {

    var graph;

    if (graphOrCells instanceof joint.dia.Graph) {
      graph = graphOrCells;
    } else {
      // Reset cells in dry mode so the graph reference is not stored on the cells.
      graph = (new joint.dia.Graph()).resetCells(graphOrCells, { dry: true });
    }
    graph.callback = callback;
    // This is not needed anymore.
    graphOrCells = null;

    // create a graphlib.Graph that represents the joint.dia.Graph
    graph.toGraphLib({
      directed: true,
      // We are about to use edge naming feature.
      multigraph: true,
      // We are able to layout graphs with embeds.
      compound: true,
      setNodeLabel: function(element) {
        return {
          width: element.get('size').width,
          height: element.get('size').height,
          rank: element.get('rank')
        };
      },
      setEdgeLabel: function(link) {
        return {
          minLen: link.get('minLen') || 1
        };
      },
      setEdgeName: function(link) {
        // Graphlib edges have no ids. We use edge name property
        // to store and retrieve ids instead.
        return link.id;
      }
    });
  },

  fromGraphLib: function(glGraph, opt) {

    opt = opt || {};

    var importNode = opt.importNode || _.noop;
    var importEdge = opt.importEdge || _.noop;
    var graph = (this instanceof joint.dia.Graph) ? this : new joint.dia.Graph;

    // Import all nodes.
    glGraph.nodes().forEach(function(node) {
      importNode.call(graph, node, glGraph, graph, opt);
    });

    // Import all edges.
    glGraph.edges().forEach(function(edge) {
      importEdge.call(graph, edge, glGraph, graph, opt);
    });

    return graph;
  },

  // Create new graphlib graph from existing JointJS graph.
  toGraphLib: function(graph, opt) {
    const graphSend = [];
    opt = opt || {};
    var setNodeLabel = opt.setNodeLabel || _.noop;
    var setEdgeLabel = opt.setEdgeLabel || _.noop;
    var setEdgeName = opt.setEdgeName || _.noop;
    //loop over all cells and convert them to a simple json structure we can then send
    graph.get('cells').each(function(cell) {

      if (cell.isLink()) {

        var source = cell.get('source');
        var target = cell.get('target');

        // Links that end at a point are ignored.
        if (!source.id || !target.id) return;

        // Note that if we are creating a multigraph we can name the edges. If
        // we try to name edges on a non-multigraph an exception is thrown.
        graphSend.push({type: 'edge', source:source.id, target:target.id,label: setEdgeLabel(cell), name: setEdgeName(cell)});
      } else {

        graphSend.push({type: 'cell', id:cell.id, label:setNodeLabel(cell)});
        // For the compound graphs we have to take embeds into account.
        if (cell.has('parent')) {
          graphSend.push({type: 'parent', id:cell.id, parent: cell.get('parent')});
        }
      }
    });

    var worker = new Worker("scripts/directedAsyncWorker.js");
    var _graph = graph;
    worker.onmessage = e => {
      // Wrap all graph changes into a batch.
      _graph.startBatch('layout');
      //Loop over the recieved graph array and convert it back
      e.data.forEach(cell => {
        var element = _graph.getCell(cell.id);
        switch(cell.type) {
          case 'node':
            element.set('position', {
              x: cell.x,
              y: cell.y
            });
            break;
          case 'link':
            element.set('vertices', cell.vertices);
            break;
        }
      });
      _graph.stopBatch('layout');
      //Execute the callback
      _graph.callback();
      //The graph object no longer needs to store the callback
      delete _graph.callback;
    };
    //push the graph array to the worker
    worker.postMessage({graph:graphSend,opt:JSON.parse(JSON.stringify(opt))});
  }
};

joint.dia.Graph.prototype.toGraphLib = function(opt) {

  return joint.layout.DirectedGraph.toGraphLib(this, opt);
};

joint.dia.Graph.prototype.fromGraphLib = function(glGraph, opt) {

  return joint.layout.DirectedGraph.fromGraphLib.call(this, glGraph, opt);
};
