'use strict';

/**
 * Constructs and returns a parallel composition of the two specified graphs.
 *
 * @class
 * @param {!Object} graph1 - the first graph
 * @param {!Object} graph2 - the second graph
 * @returns - parallel composition of the two graphs
 */
function parallelComposition(graph1, graph2) {
  var nodes1 = graph1.nodes;
  var nodes2 = graph2.nodes;
  var graph = _combineStates(nodes1, nodes2);
  var alphabet = graph1.alphabetUnion(graph2);

  // add edges
  for(var i = 0; i < nodes1.length; i++){
    var node1 = nodes1[i];
          
    for(var j = 0; j < nodes2.length; j++){
      var node2 = nodes2[j];
      var fromId = _getId(graph, node1, node2);

      for(let action in alphabet){

        var c1 = node1.coaccessible(action);
        var c2 = node2.coaccessible(action);

        for(let x in c1){
          var coaccessible1 = c1[x];
          for(let y in c2){
            var coaccessible2 = c2[y];

            // check if an edge is needed from the current combined states

            // check if the current action is performed by both the current nodes
            if(coaccessible1 !== undefined && coaccessible2 !== undefined) {
              // calculate the id of the node the new edge is transitioning to
              var toId = _getId(graph, coaccessible1, coaccessible2);
              var isHidden = graph1.isHiddenEdge(action);
              graph.addEdge(EdgeUid.next, graph.getNode(fromId), graph.getNode(toId), action);
            }

            // check if the current action is done by the outer node and is never performed in the second graph
            else if(coaccessible1 !== undefined && !graph2.containsEdgeInAlphabet(action)) {
              // calculate the id of the node the new edge is transitioning to
              var toId = _getId(graph, coaccessible1, node2);
              var isHidden = graph1.isHiddenEdge(action);
              graph.addEdge(EdgeUid.next, graph.getNode(fromId), graph.getNode(toId), action);
            }

            // check if the current action is done by the inner node and is never performed in the first graph
            else if(coaccessible2 !== undefined && !graph1.containsEdgeInAlphabet(action)) {
              // calculate the id of the node the new edge is transitioning to
              var toId = _getId(graph, node1, coaccessible2);
              var isHidden = graph2.isHiddenEdge(action);
              graph.addEdge(EdgeUid.next, graph.getNode(fromId), graph.getNode(toId), action);
            }
          }
        }
      }
    }
  }

  graph.trim();
  return graph;

  /**
   * Helper function for the parallel composition function which combines both sets of specified
   * nodes into a single graph.
   *
   * @param {!Object} nodes1 - the first set of nodes
   * @param {!Object} nodes2 - the second set of nodes
   * @returns {!Object} - a graph containing the combined states of the two specified node sets
   */
  function _combineStates(nodes1, nodes2) {
    var graph = new Graph();
    
    // combine states
    for(let i in nodes1){
      var node1 = nodes1[i];
      // determine if current node is a final node in the first graph
      var startState1 = node1._meta['startNode'] === true;
      var terminalState1 = node1._meta['isTerminal'] === 'stop';
      var label1 = (node1.label !== '') ? node1.label : node1.id;   
      
      for(let j in nodes2){
        var node2 = nodes2[j];
        // determine if the current node is a final node in the second graph
        var startState2 = node2._meta['startNode'] === true;
        var terminalState2 = node2._meta['isTerminal'] === 'stop';
        var label2 = (node2.label !== '') ? node2.label : node2.id;
        var node = graph.addNode(NodeUid.next, (label1 + "." + label2));

        // if both states are a starting state make new node start state
        if(startState1 && startState2){
          node.addMetaData('startNode', true);
        }

        // if both states are terminal make new node terminal
        if(terminalState1 && terminalState2){
          node.addMetaData('isTerminal', 'stop');
        }
      }
    }

    graph.root.addMetaData('parallel', true);
    return graph;
  }

  /**
   * Helper function for the parallel composition function which returns
   * the node id for the node matching the combined state of the two
   * specified labels. If a state cannot be found based on these labels then
   * undefined is returned.
   *
   * @private
   * @param {!Graph} graph - the graph to search for node in
   * @param {!string} node1 - the first node in the combined state
   * @param {!string} node2 - the second node in the combined state
   * @returns {!integer | undefined} the node id or undefined
   */
  function _getId(graph, node1, node2) {
    var label1 = (node1.label === '') ? node1.id : node1.label;
    var label2 = (node2.label === '') ? node2.id : node2.label;
    var label = label1 + '.' + label2;
    var nodes = graph.nodes;
    for(let i in nodes){
      var node = nodes[i];
      if(node.label === label){
        return node.id;
      }
    }

    return undefined;
  }
}