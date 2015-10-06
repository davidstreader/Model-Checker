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

var _createClass = (function() { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function(Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var Graph = (function() {
  function Graph() {
    _classCallCheck(this, Graph);

    this._nodeCount = 0;
    this._edgeCount = 0;
    this._nodeMap = {};
    this._edgeMap = {};
    this._rootId = undefined;
  }

  /**
   * A Graph Node.
   * This class should not be contructed from outside, but instancies can be passed outside.
   *
   * @protected
   * @class
   * @property {Graph} _graph   - The Graph this node is apart of
   * @property {number} _id     - The node's id
   * @property {string} _label  - The node's label
   * @property {object} _extras - Any extra data that should be stored
   */

  /**
   * The nodes in this graph.
   */

  _createClass(Graph, [{
    key: 'addNode',

    /**
     * Add a node to the graph.
     * If the graph then only contains one node, it will be set as the root node.
     *
     * @param {number} id          - The node's id (must be unquie)
     * @param {string} label       - The node's label
     * @param {object} [extras={}] - Any extra data that should be stored
     */
    value: function addNode(id, label) {
      var extras = arguments.length <= 2 || arguments[2] === undefined ? {} : arguments[2];

      if (this._nodeMap[id] !== undefined) {
        throw 'This graph already contains a node with the id "' + id + '"';
      }

      var safeExtras = Graph._deepCloneObject(extras);
      this._nodeMap[id] = new Graph.Node(this, id, label, safeExtras);

      this._nodeCount += 1;

      if (this._nodeCount === 1) {
        this.setRootNodeById(id);
      }

      return this._nodeMap[id];
    }

    /**
     * Add an edge to the graph.
     *
     * @param {number} id    - The edge's id (should be unquie)
     * @param {number} from  - The id of the node this edges comes from
     * @param {number} to    - The id of the node this edges goes to
     * @param {string} label - The edge's label
     */
  }, {
    key: 'addEdge',
    value: function addEdge(id, from, to, label) {
      if (this._edgeMap[id] !== undefined) {
        throw 'Graph already contains a edge with id "' + id + '"';
      }

      this._edgeCount += 1;
      this._edgeMap[id] = new Graph.Edge(this, id, from, to, label);
      return this._edgeMap[id];
    }

    /**
     * Get a node in the graph.
     *
     * @param {number} id - The id of the node to get
     */
  }, {
    key: 'getNode',
    value: function getNode(id) {
      return this._nodeMap[id];
    }

    /**
     * Get an edge in the graph.
     *
     * @param {number} id - The id of the edge to get
     */
  }, {
    key: 'getEdge',
    value: function getEdge(id) {
      return this._edgeMap[id];
    }

    /**
     * Remove a node from the graph.
     *
     * @param {number} id - The id of the node to remove
     */
  }, {
    key: 'removeNode',
    value: function removeNode(id) {
      delete this._nodeMap[id];

      if (this._rootId === id) {
        this.setRootNode(undefined);
      }
    }

    /**
     * Remove an edge from the graph.
     *
     * @param {number} id - The id of the edge to remove
     */
  }, {
    key: 'removeEdge',
    value: function removeEdge(id) {
      delete this._edgeMap[id];
    }

    /**
     * Set the node that should be used as the graphs root.
     *
     * @param {Graph.Node} node - The node to use as the root node
     */
  }, {
    key: 'setRootNode',
    value: function setRootNode(node) {
      if (node) {
        return this.setRootNode(node.id);
      }
      this._rootId = undefined;
    }

    /**
     * Set by id the node that should be used as the graphs root.
     *
     * @param {number} id - The id of the node to use as the root node
     */
  }, {
    key: 'setRootNodeById',
    value: function setRootNodeById(id) {
      this._rootId = id;
    }

    /**
     * Create a deep clone of this graph.
     * The clone's new nodes and edges will have the same ids as this graph.
     *
     * @returns {Graph} The clone
     */
  }, {
    key: 'deepClone',
    value: function deepClone() {
      var clone = new Graph();

      // copy all the nodes
      for (var key in this._nodeMap) {
        clone.addNode(this._nodeMap[key].id, this._nodeMap[key].label, this._nodeMap[key].extras);
      }

      // copy all the edges
      for (var key in this._edgeMap) {
        clone.addEdge(this._edgeMap[key].id, this._edgeMap[key].from, this._edgeMap[key].to, this._edgeMap[key].label);
      }

      // set the root
      clone.setRootNodeById(this._rootId);

      return clone;
    }

    /**
     * Combine this graph with one or more other graphs.
     * If the given graph(s) contains nodes/edges with the same id as this graph,
     * they will simpley be ignored.
     *
     * @param {Graph.Node} root - The node to use as the root of the combined graph
     * @param {...Graph} g      - A graph to be combined with this one
     * @returns {Graph} The combined graph
     */
  }, {
    key: 'combineWith',
    value: function combineWith(root, g) {
      var combined = this.deepClone();

      // for each graph given
      for (var i = 1; i < arguments.length; i++) {
        // for each node
        for (var key in arguments[i]._nodeMap) {
          // if this graph doesn't already have a node with this id
          if (this._nodeMap[arguments[i]._nodeMap[key].id] === undefined) {
            combined.addNode(arguments[i]._nodeMap[key].id, arguments[i]._nodeMap[key].label, arguments[i]._nodeMap[key].extras);
          }
        }

        // for each edge
        for (var key in arguments[i]._edgeMap) {
          // if this graph doesn't already have an edge with this id
          if (this._edgeMap[arguments[i]._edgeMap[key].id] === undefined) {
            combined.addEdge(arguments[i]._edgeMap[key].id, arguments[i]._edgeMap[key].from, arguments[i]._edgeMap[key].to, arguments[i]._edgeMap[key].label);
          }
        }
      }

      // set the root
      combined.setRootNodeById(root);

      return combined;
    }

    /**
     * Merge the given nodes in this graph into a single node.
     *
     * @param {Array} nodeIds     - An array of nodes to merge (specified by their IDs)
     * @param {number} [mergedId=nodeIds[0]] - The id to use for the merge node
     */
  }, {
    key: 'mergeNodes',
    value: function mergeNodes(nodeIds) {
      var mergedId = arguments.length <= 1 || arguments[1] === undefined ? nodeIds[0] : arguments[1];
      return (function() {
        if (nodeIds.length < 2) {
          console.error('Not enough nodes given to merge');
        }

        var extras = {};
        var mergedNode = undefined;

        for (var id in this._nodeMap) {
          id = Number.parseInt(id, 10);
          for (var i = 0; i < nodeIds.length; i++) {
            if (id === nodeIds[i]) {
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

        // change all the edges that refer to a delete node to refer to the merge one
        for (var id in this._edgeMap) {
          for (var i = 1; i < nodeIds.length; i++) {
            if (this._edgeMap[id].from === nodeIds[i]) {
              this._edgeMap[id].from = mergedId;
            }
            if (this._edgeMap[id].to === nodeIds[i]) {
              this._edgeMap[id].to = mergedId;
            }
          }
        }
      }).apply(this, arguments);
    }

    /**
     * Create a deep clone of an object.
     *
     * @protected
     * @param {object} obj - An object to clone
     * @returns {object} The clone
     */
  }, {
    key: 'nodes',
    get: function get() {
      var nodes = [];
      for (var key in this._nodeMap) {
        nodes.push(this._nodeMap[key]);
      }
      return nodes;
    }

    /**
     * The edges in this graph.
     */
  }, {
    key: 'edges',
    get: function get() {
      var edges = [];
      for (var key in this._edgeMap) {
        edges.push(this._edgeMap[key]);
      }
      return edges;
    }

    /**
     * The root node of this graph.
     */
  }, {
    key: 'rootNode',
    get: function get() {
      return this._nodeMap[this._rootId];
    }

    /**
     * The number of nodes in this graph.
     */
  }, {
    key: 'nodeCount',
    get: function get() {
      return this._nodeCount;
    }

    /**
     * The number of edges in this graph.
     */
  }, {
    key: 'edgeCount',
    get: function get() {
      return this._edgeCount;
    }
  }], [{
    key: '_deepCloneObject',
    value: function _deepCloneObject(obj) {
      return JSON.parse(JSON.stringify(obj));
    }
  }]);

  return Graph;
})();

Graph.Node = (function() {

  /**
   * @param {Graph} graph        - The Graph this node is apart of
   * @param {number} id          - The node's id
   * @param {string} label       - The node's label
   * @param {object} [extras={}] - Any extra data that should be stored
   */

  function _class(graph, id, label) {
    var extras = arguments.length <= 3 || arguments[3] === undefined ? {} : arguments[3];

    _classCallCheck(this, _class);

    this._graph = graph;
    this._id = id;
    this._label = label;
    this._extras = extras;
  }

  /**
   * The graph this node is apart of.
   */

  _createClass(_class, [{
    key: 'addExtraData',

    /**
     * Add some extra data to this node.
     *
     * @param {string} key - The key to save the data under
     * @param {*} value - The data to save
     */
    value: function addExtraData(key, value) {
      this._extras[key] = value;
    }

    /**
     * Get a copy of a bit of extra data in this node.
     *
     * @param {string} key - The key to get the data from
     */
  }, {
    key: 'getExtraData',
    value: function getExtraData(key) {
      if (typeof obj === 'object') {
        return Graph._deepCloneObject(this._extras[key]);
      }
      return this._extras[key];
    }
  }, {
    key: 'graph',
    get: function get() {
      return this._graph;
    }

    /**
     * This node's id.
     */
  }, {
    key: 'id',
    get: function get() {
      return this._id;
    },

    /**
     * A setter method to change this node's id.
     *
     * @param {number} newId - What to change this node's id to
     */
    set: function set(newId) {
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

    /**
     * This node's label.
     */
  }, {
    key: 'label',
    get: function get() {
      return this._label;
    }

    /**
     * A copy of this node's extra data.
     */
  }, {
    key: 'extras',
    get: function get() {
      return Graph._deepCloneObject(this._extras);
    }
  }]);

  return _class;
})();

/**
 * A Graph Edge.
 * This class should not be contructed from outside, but instancies can be passed outside.
 *
 * @protected
 * @class
 * @property {Graph} _graph  - The Graph this node is apart of
 * @property {number} _id    - The node's id
 * @property {object} _from  - The id of the node this edges comes from
 * @property {object} _to    - The id of the node this edges goes to
 * @property {string} _label - The node's label
 */
Graph.Edge = (function() {

  /**
   * @param {Graph} graph  - The Graph this edges is apart of
   * @param {number} id    - The edge's id (should be unquie)
   * @param {number} from  - The id of the node this edges comes from
   * @param {number} to    - The id of the node this edges goes to
   * @param {string} label - The edge's label
   */

  function _class2(graph, id, from, to, label) {
    _classCallCheck(this, _class2);

    this._graph = graph;
    this._id = id;
    this._from = from;
    this._to = to;
    this._label = label;
  }

  /**
   * The graph this edges is apart of.
   */

  _createClass(_class2, [{
    key: 'graph',
    get: function get() {
      return this._graph;
    }

    /**
     * This edge's id.
     */
  }, {
    key: 'id',
    get: function get() {
      return this._id;
    }

    /**
     * The node this edge connects from.
     */
  }, {
    key: 'from',
    get: function get() {
      return this._graph._nodeMap[this._from];
    }

    /**
     * The node this edge connects to.
     */
  }, {
    key: 'to',
    get: function get() {
      return this._graph._nodeMap[this._to];
    }

    /**
     * This edge's label.
     */
  }, {
    key: 'label',
    get: function get() {
      return this._label;
    }
  }]);

  return _class2;
})();
