/**
 * A copy paste of https://github.com/clientIO/joint/blob/master/plugins/layout/DirectedGraph/joint.layout.DirectedGraph.js
 * But modified to use a worker thread.
 */

let graphlib = window.graphlib;
let dagre = window.dagre;

joint.layout.DirectedGraph = {

  layout: function(graph, opt, callback) {
    graph.callback = callback;
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
  // Create new graphlib graph from existing JointJS graph.
  toGraphLib: function(graph, opt) {
    const graphSend = [];
    opt = opt || {};
    const setNodeLabel = opt.setNodeLabel || _.noop;
    const setEdgeLabel = opt.setEdgeLabel || _.noop;
    const setEdgeName = opt.setEdgeName || _.noop;
    //loop over all cells and convert them to a simple json structure we can then send
    graph.get('cells').each(function(cell) {

      if (cell.isLink()) {

        const source = cell.get('source');
        const target = cell.get('target');

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

    const worker = new Worker("scripts/directedAsyncWorker.js");
    worker.onmessage = e => {
      // Wrap all graph changes into a batch.
      graph.startBatch('layout');
      //Loop over the recieved graph array and convert it back
      e.data.forEach(cell => {
        const element = graph.getCell(cell.id);
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
      graph.stopBatch('layout');
      //Execute the callback
      graph.callback();
      //The graph object no longer needs to store the callback
      delete graph.callback;
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
