'use strict';

/**
 * A graph data structure (a collections of nodes and edges).
 *
 * @public
 * @class
 * @property {number} _nodeCount - The number of nodes in the graph
 * @property {number} _edgeCount - The number of edges in the graph
 * @property {object} _nodeMap   - A map of id to nodes (The nodes of the graph)
 * @property {object} _edgeMap   - A map of id to edges (The edges of the graph)
 * @property {number} _rootId    - The id of the root node
 */
function Graph() {
  this._nodeCount = 0;
  this._edgeCount = 0;
  this._nodeMap = {};
  this._edgeMap = {};
  this._rootId = undefined;
}

/**
 * Define the public properties for the Graph class.
 */
Object.defineProperties(Graph.prototype, {

  /**
   * The nodes in the graph as an array.
   */
  nodes: {
    get: function() {
      var nodes = [];
      for (var key in this._nodeMap) {
        nodes.push(this._nodeMap[key]);
      }
      return nodes;
    }
  },

  /**
   * The edges in the graph as an array.
   */
  edges: {
    get: function() {
      var edges = [];
      for (var key in this._edgeMap) {
        edges.push(this._edgeMap[key]);
      }
      return edges;
    }
  },

  /**
   * The root node of the graph.
   */
  rootNode: {
    get: function() {
      return this._nodeMap[this._rootId];
    }
  },

  /**
   * The number of nodes in the graph.
   */
  nodeCount: {
    get: function() {
      return this._nodeCount;
    }
  },

  /**
   * The number of edges in the graph.
   */
  edgeCount: {
    get: function() {
      return this._edgeCount;
    }
  }
});

/**
 * A Graph Node.
 * This class should not be contructed from outside, but instancies can be passed outside.
 *
 * @protected
 * @class
 * @param {Graph} graph     - The Graph this node is apart of
 * @param {number} id       - The node's id (should be unquie)
 * @param {string} label    - The node's label
 * @param {object} [extras] - Any extra data that should be stored
 */
Graph.prototype.Node = function(graph, id, label, extras) {
  this._graph = graph;
  this._id = id;
  this.label = label;
  this.extras = extras || {};
};

/**
 * Define the public properties for the Graph class.
 */
Object.defineProperties(Graph.prototype.Node.prototype, {
  id: {
    get: function() {
      return this._id;
    },
    set: function(newId) {
      var oldId = this._id;
      if (newId === oldId) {
        return;
      }
      this._graph._nodeMap[newId] = this._graph._nodeMap[oldId];
      delete this._graph._nodeMap[oldId];
      this._id = newId;

      for (var edgeId in this._graph._edgeMap) {
        if (this._graph._edgeMap[edgeId].from === oldId) {
          this._graph._edgeMap[edgeId].from = newId;
        }
        if (this._graph._edgeMap[edgeId].to === oldId) {
          this._graph._edgeMap[edgeId].to = newId;
        }
      }
    }
  }
});

/**
 * A Graph Edge.
 * This class should not be contructed from outside, but instancies can be passed outside.
 *
 * @protected
 * @class
 * @param {Graph} graph  - The Graph this edges is apart of
 * @param {number} id    - The edge's id (should be unquie)
 * @param {number} from  - The id of the node this edges comes from
 * @param {number} to    - The id of the node this edges goes to
 * @param {string} label - The edge's label
 */
Graph.prototype.Edge = function(graph, id, from, to, label) {
  this._graph = graph;
  this.id = id;
  this.from = from;
  this.to = to;
  this.label = label;
};

/**
 * Add a node to the graph.
 * If there is only one node in the graph, it will be set as the root node.
 *
 * @public
 * @param {number} id       - The node's id (should be unquie)
 * @param {string} label    - The node's label
 * @param {object} [extras] - Any extra data that should be stored
 */
Graph.prototype.addNode = function(id, label, extras) {
  if (this._nodeMap[id] !== undefined) {
    throw 'Graph already contains a node with id "' + id + '"';
  }

  this._nodeCount += 1;
  if (this._nodeCount === 1) {
    this.setRootNode(id);
  }

  this._nodeMap[id] =
    new this.Node(this, id, label, this._deepCloneobject(extras));
  return this._nodeMap[id];
};

/**
 * Add an edge to the graph.
 *
 * @public
 * @param {number} id    - The edge's id (should be unquie)
 * @param {number} from  - The id of the node this edges comes from
 * @param {number} to    - The id of the node this edges goes to
 * @param {string} label - The edge's label
 */
Graph.prototype.addEdge = function(id, from, to, label) {
  if (this._edgeMap[id] !== undefined) {
    throw 'Graph already contains a edge with id "' + id + '"';
  }

  this._edgeCount += 1;
  this._edgeMap[id] = new this.Edge(this, id, from, to, label);
  return this._edgeMap[id];
};

/**
 * Get a node in the graph.
 *
 * @public
 * @param {number} id - The id of the node to get
 */
Graph.prototype.getNode = function(id) {
  return this._nodeMap[id];
};

/**
 * Get an edge in the graph.
 *
 * @public
 * @param {number} id - The id of the edge to get
 */
Graph.prototype.getEdge = function(id) {
  return this._edgeMap[id];
};

/**
 * Remove a node from the graph.
 * If there is only one node in the graph, it will be set as the root node.
 *
 * @public
 * @param {number} id - The id of the node to remove
 */
Graph.prototype.removeNode = function(id) {
  delete this._nodeMap[id];

  if (this._nodeMap.length === 0) {
    this.setRootNode(undefined);
  }
};

/**
 * Remove an edge from the graph.
 *
 * @public
 * @param {number} id - The id of the edge to remove
 */
Graph.prototype.removeEdge = function(id) {
  delete this._edgeMap[id];
};

/**
 * Set the node that should be used as the graphs root.
 *
 * @public
 * @param {number} id - The id of the node to use as the root
 */
Graph.prototype.setRootNode = function(id) {
  this._rootId = id;
};

/**
 * Create a deep clone of this graph.
 * The clone will contain new nodes and edges that have the same ids as the original.
 *
 * @public
 * @returns {Graph} The clone
 */
Graph.prototype.deepClone = function() {
  var clone = new Graph();

  var key;

  for (key in this._nodeMap) {
    clone.addNode(
      this._nodeMap[key].id,
      this._nodeMap[key].label,
      this._nodeMap[key].extras);
  }

  for (key in this._edgeMap) {
    clone.addEdge(
      this._edgeMap[key].id,
      this._edgeMap[key].from,
      this._edgeMap[key].to,
      this._edgeMap[key].label);
  }

  clone.setRootNode(this._rootId);

  return clone;
};

/**
 * Combine this graph with one or more other graphs.
 * This functions assumes the graphs do not share any nodes or edges.
 *
 * @public
 * @param {number} rootId - The ID of the node to use as the root of the combined graph
 * @param {...Graph} g    - A graph to be combined with this one
 * @returns {Graph} The combined graph
 */
Graph.prototype.combineWith = function(rootId, g) {
  var combined = this.deepClone();

  for (var i = 1; i < arguments.length; i++) {
    var key;
    for (key in arguments[i]._nodeMap) {
      if (this._nodeMap[arguments[i]._nodeMap[key].id] !== undefined) {
        continue;
      }
      combined.addNode(
        arguments[i]._nodeMap[key].id,
        arguments[i]._nodeMap[key].label,
        arguments[i]._nodeMap[key].extras);
    }
    for (key in arguments[i]._edgeMap) {
      if (this._edgeMap[arguments[i]._edgeMap[key].id] !== undefined) {
        continue;
      }
      combined.addEdge(
        arguments[i]._edgeMap[key].id,
        arguments[i]._edgeMap[key].from,
        arguments[i]._edgeMap[key].to,
        arguments[i]._edgeMap[key].label);
    }
  }

  combined.setRootNode(rootId);
  return combined;
};

/**
 * Merge the given nodes in this graph into a single node.
 *
 * @private
 * @param {Array} nodeIds     - An array of nodes to merge (specified by their IDs)
 * @param {number} [mergedId] - The id to use for the merge node
 */
Graph.prototype.mergeNodes = function(nodeIds, mergedId) {
  if (nodeIds.length < 2) { console.error('Not enough nodes given to merge');}
  mergedId = mergedId || nodeIds[0];

  var extras = {};
  var mergedNode;
  var id;
  var i;

  for (id in this._nodeMap) {
    for (i = 0; i < nodeIds.length; i++) {
      if (Number.parseInt(id, 10) === nodeIds[i]) {
        for (var key in this._nodeMap[id].extras) {
          extras[key] = this._nodeMap[id].extras[key];
        }

        if (id === this._rootId) {
          this._rootId = mergedId;
        }

        if (mergedNode === undefined) {
          mergedNode = this._nodeMap[id];
          break;
        }

        delete this._nodeMap[id];
      }
    }
  }

  mergedNode.id = mergedId;
  mergedNode.extras = extras;

  // change all the edges the refer to a delete node to refer to the merge one
  for (id in this._edgeMap) {
    for (i = 1; i < nodeIds.length; i++) {
      if (this._edgeMap[id].from === nodeIds[i]) {
        this._edgeMap[id].from = mergedId;
      }
      if (this._edgeMap[id].to === nodeIds[i]) {
        this._edgeMap[id].to = mergedId;
      }
    }
  }
};

/**
 * Create a deep clone of a js object.
 *
 * @private
 * @param {object|*} obj - An object to clone (if this param is not object, it will simple be returned)
 * @returns {object} The clone
 */
Graph.prototype._deepCloneobject = function(obj) {
  if (typeof obj !== 'object') {
    return obj;
  }
  return JSON.parse(JSON.stringify(obj));
};
;
