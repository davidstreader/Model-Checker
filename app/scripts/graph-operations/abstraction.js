'use strict';

/**
 * Performs the abstraction function on the specified graph, which removes the hidden 
 * tau actions and adds the observable transitions.
 */
function abstraction(graph, isFair) {
  isFair = (isFair === undefined) ? true : isFair;
  var clone = graph.deepClone();
  var edgesToAdd = [];
  var hiddenEdges = clone.hiddenEdges;

  for(let i in hiddenEdges){
    var edge = hiddenEdges[i];

    var from = _getTransitions(edge.from.edgesToMe, false);
    var to = _getTransitions(edge.to.edgesFromMe, true);

      edgesToAdd = edgesToAdd.concat(_addObservableEdges(edge.from, edge.to, from, true));
      edgesToAdd = edgesToAdd.concat(_addObservableEdges(edge.to, edge.from, to, false));
  }

  // add the edges constructed by the abstraction
  for(let i in edgesToAdd){
    var edge = edgesToAdd[i];
    if(!clone.containsEdge(edge.from, edge.to, edge.label)){
      clone.addEdge(edge.uid, edge.from, edge.to, edge.label, edge.isHidden, edge.isDeadlock);
    }
  }

  // if this is a fair abstraction then remove all the hidden edges
  if(isFair){
    clone.removeHiddenEdges();
  }
  // otherwise only remove the hidden edges from before the abstraction
  else{
    for(let i in hiddenEdges){
      clone.removeEdge(hiddenEdges[i]);
    }
    // construct deadlocks
    clone = _constructDeadlocks(clone);
  }
  //clone = hideDeadlocks(clone);
  clone = _constructStopNodes(clone);
  clone.trim();
  return clone;


  function _getTransitions(edges, isFrom){
    var transitions = [];
    for(let i in edges){
      var edge = edges[i];
      if(!edge.isHidden){
        var node = isFrom ? edge.to : edge.from;
        transitions.push({node: node, label: edge.label});
      }
    }

    return transitions;
  }

  function _hideDeadlocks(graph) {
    var clone = graph.deepClone();
    var edgesToAdd = [];
    var deadlockEdges = clone.deadlockEdges;

    for(let i in deadlockEdges){
      var edge = deadlockEdges[i];
      var from = _getTransitions(edge.from.edgesToMe, false);
      var to = _getTransitions(edge.to.edgesFromMe, true);

        edgesToAdd = edgesToAdd.concat(_addObservableEdges(edge.from, edge.to, from, true));
        edgesToAdd = edgesToAdd.concat(_addObservableEdges(edge.to, edge.from, to, false));
    }

    // add the edges constructed by the abstraction
    for(let i in edgesToAdd){
      var edge = edgesToAdd[i];
      if(!clone.containsEdge(edge.from, edge.to, edge.label)){
        clone.addEdge(edge.uid, edge.from, edge.to, edge.label);
      }
    }

    for(let i in deadlockEdges){
      clone.removeEdge(deadlockEdges[i]);
    }

    // add the edges constructed by the abstraction
    for(let i in edgesToAdd){
      var edge = edgesToAdd[i];
      if(!clone.containsEdge(edge.from, edge.to, edge.label)){
        clone.addEdge(edge.uid, edge.from, edge.to, edge.label);
      }
    }

    return clone;
  }

  /**
   * Helper function for the abstraction function which adds the observable transitions through
   * a series of hidden, unobservable transitions.
   *
   * @private
   * @param {!Node} start - the node that the first tau event starts from
   * @param {!Node} first - the first node visited by the first tau event
   * @param {!Array} transitions - array of observable events to add
   * @param {!boolean} isFrom - determines if transitioning forwards or backwards through tau events
   * @returns {!Array} an array of edges to add to the graph
   */
  function _addObservableEdges(start, first, transitions, isFrom){
    var stack = [first];
    var visited = [start];
    var edgesToAdd = [];

    while(stack.length !== 0){
      var current = stack.pop();
      visited.push(current);

      // add edges to/from the current node
      for(let i in transitions){
        var transition = transitions[i];
        if(isFrom){
          edgesToAdd.push(_constructEdge(EdgeUid.next, transition.node, current, transition.label));
        }
        else{
          edgesToAdd.push(_constructEdge(EdgeUid.next, current, transition.node, transition.label));
        }
      }

      var edges = isFrom ? current.edgesFromMe : current.edgesToMe;
      for(let i in edges){
        var edge = edges[i];
        var node = isFrom ? edge.to : edge.from;

        if(edge.isHidden && !_.contains(visited, node)){
          stack.push(node);
        }
        // if next node has already been visited add a tau loop
        else if(edge.isHidden && _.contains(visited, node)){
          edgesToAdd.push(_constructEdge(EdgeUid.next, node, node, TAU));
        }
      }
    }

    return edgesToAdd;
  }

  /**
   * Helper method for the abstraction function which constructs an object containing
   * data to construct an edge.
   *
   * @private
   * @param {!integer} uid - the unique identifier for that edge
   * @param {!Node} from - the node the edge will transition from
   * @param {!Node} to - the node the edge will transition to
   * @param {!string} label - the action of the edge
   * @returns {!Object} object containing data to construct an edge
   */
  function _constructEdge(uid, from, to, label){
    return {uid: uid, from:from, to:to, label:label};
  }

  /**
   * Constructs a deadlock for each hidden edge in the specified graph that transitions
   * back to the node it started from.
   *
   * @private
   * @param {!Object} graph - the graph to construct deadlocks for
   * @returns {!object} - the graph with deadlocks included
   */
  function _constructDeadlocks(graph){
    var clone = graph.deepClone();
    var edges = clone.edges;
    for(let e in edges){
      var edge = edges[e];

      // if hidden edge links back to itself add a dead lock
      if(edge.isHidden && edge.from.id === edge.to.id){
        var temp = clone.addNode(NodeUid.next);
        // add a new deadlock edge and remove the tau loop
        clone.addEdge(EdgeUid.next, edge.from, temp, DELTA);
        clone.removeEdge(edge);
        // set metadata of node to show it is an error node
        temp.addMetaData('isTerminal', 'error');
      }
    }

    return clone;
  }

  /**
   * Helper function for the abstraction function which searches the specified graph
   * for any nodes which have become stop nodes due to hidden actions being removed. These
   * nodes have their meta data updated to show they are stop nodes.
   *
   * @private
   * @param {!graph} graph - the graph to process
   * @returns {!graph} the processed graph
   */
  function _constructStopNodes(graph){
    var clone = graph.deepClone();
    var stack = [clone.root];
    var visited = [];
    while(stack.length !== 0){
      var node = stack.pop();
      visited.push(node);
      
      // add neighbours to stack
      var neighbors = node.neighbors;
      for(let i in neighbors){
        // only add neighbour if it has not been visited
        if(!_.contains(visited, neighbors[i])){
          stack.push(neighbors[i]);
        }
      }

      // if this is a final node set its meta data to reflect this
      if(neighbors.length === 0){
        if(node.getMetaData('isTerminal') === undefined){
          node.addMetaData('isTerminal', 'stop');
        }
      }
    }

    return clone;
  }
}