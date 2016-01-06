// jscs:enable esnext
// jshint esnext:true
'use strict';

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var _NODE_UID = 0; // used to return unique node id for NodeUid class
var _EDGE_UID = 0; // used to return unique edge id for EdgeUid class
var TAU = '𝜏';
var DELTA = '𝛿';

/**
 * Helper class for Graph which generates unique node identifiers.
 *
 * @static
 */

var NodeUid = (function () {
  function NodeUid() {
    _classCallCheck(this, NodeUid);
  }

  /**
   * Helper class for Graph which generates unique edge identifiers
   *
   * @static
   */

  _createClass(NodeUid, null, [{
    key: 'reset',

    /**
     * Resets the node identifier to zero.
     *
     * @static
     */
    value: function reset() {
      _NODE_UID = 0;
    }
  }, {
    key: 'next',

    /**
     * Returns the next unique node indentifier.
     *
     * @static
     * @returns {!integer} - next node uid
     */
    get: function get() {
      return _NODE_UID++;
    }
  }]);

  return NodeUid;
})();

var EdgeUid = (function () {
  function EdgeUid() {
    _classCallCheck(this, EdgeUid);
  }

  /**
   * A graph data structure (a collections of nodes and edges).
   *
   * @public
   * @class
   * @property {!array} nodes      - The nodes of the graph (readOnly)
   * @property {!array} edges      - The edges of the graph (readOnly)
   * @property {!Graph.Node} root  - The root node of this graph
   * @property {!number} nodeCount - The number of nodes in the graph (readOnly)
   * @property {!number} edgeCount - The number of edges in the graph (readOnly)
   */

  _createClass(EdgeUid, null, [{
    key: 'reset',

    /**
     * Resets the edge identifier to zero.
     *
     * @static
     */
    value: function reset() {
      _EDGE_UID = 0;
    }
  }, {
    key: 'next',

    /**
     * Returns the next unique edge indentifier.
     *
     * @static
     * @returns {!integer} - next edge uid
     */
    get: function get() {
      return _EDGE_UID++;
    }
  }]);

  return EdgeUid;
})();

var Graph = (function () {
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
   *
   * @protected
   * @class
   * @property {!Graph} graph     - The Graph this node is apart of (readOnly)
   * @property {!number} id       - The node's id
   * @property {!string} label    - The node's label
   * @property {!object} metaData - Any meta data that should be stored (readOnly)
   * @property {!array} neighbors - The neighboring nodes of this node (readOnly)
   */

  /**
   * Get an array of the nodes in this graph where the root node is be the first element.
   *
   * @returns {!array} An array of the nodes in this graph
   */

  _createClass(Graph, [{
    key: 'addNode',

    /**
     * Add a node to this graph.
     * If this graph doesn't already have a root node, this node will be set as the root node.
     *
     * @param {!number} uid           - The node's id (must be unquie)
     * @param {!string} [label='']    - The node's label
     * @param {!object} [metaData={}] - Any meta data about this node that should be stored
     * @throws {Graph.Exception} uid must be unquie
     * @returns {!Graph.Node} The node added to the graph
     */
    value: function addNode(uid) {
      var label = arguments.length <= 1 || arguments[1] === undefined ? '' : arguments[1];
      var metaData = arguments.length <= 2 || arguments[2] === undefined ? {} : arguments[2];

      if (this._nodeMap[uid] !== undefined) {
        throw new Graph.Exception('This graph already contains a node with the id "' + uid + '".');
      }

      var node = new Graph.Node(this, uid, label, Graph._deepCloneObject(metaData));

      this._nodeMap[uid] = node;
      this._nodeCount += 1;

      if (this.root === undefined) {
        this.root = node;
      }

      return node;
    }

    /**
     * Add an edge to the graph.
     *
     * @param {!number} uid        - The edge's id (must be unquie)
     * @param {!Graph.Node} from   - The node this edges comes from
     * @param {!Graph.Node} to     - The node this edges goes to
     * @param {!string} [label=''] - The edge's label
     * @throws {Graph.Exception} uid must be unquie
     * @returns {!Graph.Edge} The edge added to the graph
     */
  }, {
    key: 'addEdge',
    value: function addEdge(uid, from, to) {
      var label = arguments.length <= 3 || arguments[3] === undefined ? '' : arguments[3];
      var isHidden = arguments.length <= 4 || arguments[4] === undefined ? false : arguments[4];
      var isDeadlock = arguments.length <= 5 || arguments[5] === undefined ? false : arguments[5];

      if (this._edgeMap[uid] !== undefined) {
        throw new Graph.Exception('This graph already contains a edge with id "' + uid + '".');
      }

      var edge = new Graph.Edge(this, uid, from, to, label, isHidden, isDeadlock);
      this._edgeMap[uid] = edge;
      this._edgeCount += 1;

      from._addEdgeFromMe(edge);
      to._addEdgeToMe(edge);

      return edge;
    }

    /**
     * Get a node in the graph.
     *
     * @param {!number} id - The id of the node to get
     * @returns {Graph.Node} The node
     */
  }, {
    key: 'getNode',
    value: function getNode(id) {
      return this._nodeMap[id];
    }

    /**
     * Returns a set of the reachable nodes in this graph from the root node.
     *
     * @returns {!Set} Set of node ids
     */
  }, {
    key: 'getEdge',

    /**
     * Get an edge in the graph.
     *
     * @param {!number} id - The id of the edge to get
     * @returns {Graph.Edge} The edge
     */
    value: function getEdge(id) {
      return this._edgeMap[id];
    }

    /**
     * Returns true if there is an edge between the specified nodes with the
     * specified label. Otherwise returns false.
     *
     * @param {!Node} from - The start node
     * @param {!Node} to - The end node
     * @param {!string} label - the type of action
     * @returns {boolean} Whether that edge exists or not
     */
  }, {
    key: 'containsEdge',
    value: function containsEdge(from, to, label) {
      for (var i in this._edgeMap) {
        var edge = this._edgeMap[i];
        if (edge.from === from && edge.to === to && edge.label === label) {
          return true;
        }
      }

      return false;
    }

    /**
     * Returns true if the specified edge is contained in this graph's alphabet,
     * otherwise returns false.
     *
     * @returns {boolean} Whether the edge is contained in the graph's alphabet or not
     */
  }, {
    key: 'containsEdgeInAlphabet',
    value: function containsEdgeInAlphabet(edge) {
      var result = this.constructAlphabet()[edge];
      if (result === true) {
        return true;
      }
      return false;
    }

    /**
     * Returns true if the specified edge in this graph is hidden, otherwise
     * returns false.
     *
     * @param {!number} edge - The id of the edge to check
     * @returns {!boolean} Whether the specified edge is hidden or not
     */
  }, {
    key: 'isHiddenEdge',
    value: function isHiddenEdge(edge) {
      for (var i in this._edgeMap) {
        if (this._edgeMap[i].label === edge && this._edgeMap[i].isHidden) {
          return true;
        }
      }

      return false;
    }

    /**
     * Remove a node from the graph.
     * Any edges connected to this node will also be deleted.
     *
     * @param {Graph.Node} node - The node to remove
     */
  }, {
    key: 'removeNode',
    value: function removeNode(node) {
      if (!node || node.graph !== this || this._nodeMap[node.id] !== node) {
        return;
      }

      for (var i in node._edgesToMe) {
        this.removeEdge(node._edgesToMe[i]);
      }

      for (var i in node._edgesFromMe) {
        this.removeEdge(node._edgesFromMe[i]);
      }

      delete this._nodeMap[node.id];
      this._nodeCount -= 1;

      if (this._rootId === node.id) {
        this.root = undefined;
      }
    }

    /**
     * Remove an edge from the graph.
     *
     * @param {Graph.Edge} edge - The edge to remove
     */
  }, {
    key: 'removeEdge',
    value: function removeEdge(edge) {
      if (!edge || edge.graph !== this || this._edgeMap[edge.id] !== edge) {
        return;
      }

      delete this._edgeMap[edge.id].to._edgesToMe[edge.id];
      delete this._edgeMap[edge.id].from._edgesFromMe[edge.id];
      delete this._edgeMap[edge.id];
      this._edgeCount -= 1;
    }

    /**
     * Removes duplicate edges from this graph. An edge is determined to be a duplicate
     * if a node has two or more edges from it that transition to the same node with the
     * same label.
     */
  }, {
    key: 'removeDuplicateEdges',
    value: function removeDuplicateEdges() {
      // search all nodes for duplicate edges
      var nodes = this._nodeMap;
      for (var i in nodes) {
        var node = nodes[i];

        // compare each edge from this node with all other edges from this node
        var edges = node.edgesFromMe;
        for (var j in edges) {
          var edge1 = edges[j];

          for (var k in edges) {
            var edge2 = edges[k];

            // remove edge if it is deemed to be a duplicate
            if (j != k && edge1.to.id === edge2.to.id && edge1.label === edge2.label) {
              this.removeEdge(edge2);
              delete edges[k];
            }
          }
        }
      }
    }

    /**
     * Retrieves the hidden edges from this graph
     *
     * @returns {!Array} - the hidden edges in this graph
     */
  }, {
    key: 'removeHiddenEdges',

    /**
     * Removes the hidden edges from this graph.
     */
    value: function removeHiddenEdges() {
      for (var i in this._edgeMap) {
        var edge = this._edgeMap[i];
        // remove edge if it is hidden
        if (edge.isHidden) {
          this.removeEdge(edge);
        }
      }

      for (var i in this._nodeMap) {
        var node = this._nodeMap[i];
        if (node.edgesToMe.length === 0) {
          node.addMetaData('startNode', true);
        }
      }
    }

    /**
     * Constructs and returns a set containing the alphabet for this graph.
     * The alphabet is a collection of the actions that transition nodes from
     * one state to another.
     */
  }, {
    key: 'constructAlphabet',
    value: function constructAlphabet() {
      var alphabet = {};
      for (var i in this._edgeMap) {
        var label = this._edgeMap[i].isDeadlock ? DELTA : this._edgeMap[i].label;
        alphabet[label] = true;
      }

      return alphabet;
    }

    /**
     *  Constructs and returns a set of the the union of the two alphabets for the specified graphs.
     *
     *  @param {!Graph} graph1 - First graph to get alphabet of
     *  @param {!Graph} graph2 - Second graph to get alphabet of
     */
  }, {
    key: 'alphabetUnion',
    value: function alphabetUnion(otherGraph) {
      var alphabet = this.constructAlphabet();
      var temp = otherGraph.constructAlphabet();

      for (var a in temp) {
        alphabet[a] = true;
      }

      return alphabet;
    }

    /**
     * Set the root node of this graph by specifying its id.
     *
     * @private
     * @param {!number} id - The id of the node to set as the root
     * @returns {Graph.Node} The new root node
     */
  }, {
    key: '_setRootNodeById',
    value: function _setRootNodeById(id) {
      this._rootId = id;
      return this.root;
    }

    /**
     * Create a deep clone of this graph.
     * The clone's new nodes and edges will have the same ids as this graph.
     *
     * @returns {!Graph} The clone
     */
  }, {
    key: 'deepClone',
    value: function deepClone() {
      var clone = new Graph();

      // copy all the nodes
      for (var key in this._nodeMap) {
        clone.addNode(this._nodeMap[key].id, this._nodeMap[key].label, this._nodeMap[key].metaData);
      }

      // copy all the edges
      for (var key in this._edgeMap) {
        clone.addEdge(this._edgeMap[key].id, clone.getNode(this._edgeMap[key].from.id), // make sure to use the copied node, not the original
        clone.getNode(this._edgeMap[key].to.id), this._edgeMap[key].label, this._edgeMap[key].isHidden, this._edgeMap[key].isDeadlock);
      }

      // set the root
      clone._setRootNodeById(this._rootId);

      return clone;
    }

    /**
     * Combine this graph with one or more other graphs.
     * If the given graph(s) contains nodes/edges with the same id as this graph,
     * they will will not be added.
     *
     * @param {...!Graph} graphs - The graphs to be combined with this one
     * @returns {!Graph} this
     */
  }, {
    key: 'combineWith',
    value: function combineWith() {
      for (var _len = arguments.length, graphs = Array(_len), _key = 0; _key < _len; _key++) {
        graphs[_key] = arguments[_key];
      }

      // for each graph given
      for (var i = 0; i < graphs.length; i++) {
        // for each node in that graph
        for (var key in graphs[i]._nodeMap) {
          // if this graph doesn't already have a node with this id
          if (this._nodeMap[graphs[i]._nodeMap[key].id] === undefined) {
            // add the node
            this.addNode(graphs[i]._nodeMap[key].id, graphs[i]._nodeMap[key].label, graphs[i]._nodeMap[key].metaData);
          }
        }

        // for each edge in that graph
        for (var key in graphs[i]._edgeMap) {
          // if this graph doesn't already have an edge with this id
          if (this._edgeMap[graphs[i]._edgeMap[key].id] === undefined) {
            // add the edge
            this.addEdge(graphs[i]._edgeMap[key].id, this.getNode(graphs[i]._edgeMap[key].from.id), // use the node in this graph, not the one in the other graph
            this.getNode(graphs[i]._edgeMap[key].to.id), graphs[i]._edgeMap[key].label, graphs[i]._edgeMap[key].isHidden, graphs[i]._edgeMap[key].isDeadlock);
          }
        }
      }

      return this;
    }

    /**
     * Merge the specified nodes in this graph into a single node.
     * At least two nodes must be specified in order to merge them.
     * The merged node's id will be nodeIds[0].
     *
     * @param {!array} nodeIds - An array of nodes to merge (specified by their IDs)
     * @returns {Node} The merged node
     */
  }, {
    key: 'mergeNodes',
    value: function mergeNodes(nodeIds) {
      var mergedNode = undefined;
      var mergedMetaData = {};
      var isRoot = false;
      // for each node id specified to merge
      for (var i = 0; i < nodeIds.length; i++) {
        var node = this.getNode(nodeIds[i]); // get the node
        // save all the meta data in this node
        var meta = node.metaData;
        for (var key in meta) {
          mergedMetaData[key] = meta[key];
        }

        // check if this node is the root of the graph
        if (node.id === this.rootId) {
          isRoot = true;
        }

        // if this is the first node we are dealing with (i === 0)
        if (mergedNode === undefined) {
          mergedNode = node; // save it
          continue; // and move on to the next node
        }

        // update the edges from this node to be from the merged node
        for (var key in node._edgesFromMe) {
          var edge = node._edgesFromMe[key];
          edge._from = mergedNode;
          mergedNode._addEdgeFromMe(edge);
        }
        node._edgesFromMe = {};

        // update the edges to this node to be to the merged node
        for (var key in node._edgesToMe) {
          var edge = node._edgesToMe[key];
          edge._to = mergedNode;
          mergedNode._addEdgeToMe(edge);
        }
        node._edgesToMe = {};

        // remove the node from the graph
        this.removeNode(node);
      }

      mergedNode._meta = mergedMetaData; // set the merged node's meta data

      if (isRoot) {
        this.root = mergedNode;
      }

      return mergedNode;
    }

    /**
     * Trims any nodes from the graph that are not reachable from the root node.
     */
  }, {
    key: 'trim',
    value: function trim() {
      // get the reachable nodes from the root
      var reachable = this.reachableNodes;
      var visited = [];
      // remove any nodes that are not reachable from the root
      for (var node in this._nodeMap) {
        if (reachable[node] !== true) {
          this.removeNode(this._nodeMap[node]);
        }
      }
    }

    /**
     * Create a deep clone of an object or array.
     *
     * @protected
     * @param {!object|!array} obj - An object or array to clone
     * @returns {!object|!array} The cloned object/array
     */
  }, {
    key: 'nodes',
    get: function get() {
      var nodes = [this.root];
      for (var id in this._nodeMap) {
        if (Number.parseInt(id, 10) !== this._rootId) {
          nodes.push(this._nodeMap[id]);
        }
      }
      return nodes;
    }

    /**
     * Get an array of the edges in this graph.
     *
     * @returns {!array} An array of the edges in this graph
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
     * Get the root node of this graph.
     *
     * @returns {!Graph.Node} The root.
     */
  }, {
    key: 'root',
    get: function get() {
      return this._nodeMap[this._rootId];
    },

    /**
     * Set the node that should be used as the graphs root.
     *
     * @param {!Graph.Node} node - The node to use as the root node
     * @returns {Graph.Node} The new root node
     */
    set: function set(node) {
      if (node === this.root) {
        return node;
      }
      if (node) {
        if (node.graph === this) {
          return this._setRootNodeById(node.id);
        }
        throw new Graph.Exception('cannot set the root of this graph to a node that is not in it.');
      }
      this._rootId = undefined;
      return undefined;
    }

    /**
     * The number of nodes in this graph.
     *
     * @returns {!number} The number of nodes in this graph
     */
  }, {
    key: 'rootId',
    get: function get() {
      return this._rootId;
    }
  }, {
    key: 'nodeCount',
    get: function get() {
      return this._nodeCount;
    }

    /**
     * The number of edges in this graph.
     *
     * @returns {!number} The number of edges in this graph
     */
  }, {
    key: 'edgeCount',
    get: function get() {
      return this._edgeCount;
    }
  }, {
    key: 'reachableNodes',
    get: function get() {
      var nodes = [];
      var stack = [this.rootId];
      var visited = [];
      // perfrom depth first search of graph
      while (stack.length !== 0) {
        var id = stack.pop();
        if (!_.contains(visited, id)) {
          visited.push(id);
          // add current node id to the set
          nodes[id] = true;
          var node = this.getNode(id);

          // add neighbours of current node to stack
          var neighbors = node.neighbors;
          for (var i = 0; i < neighbors.length; i++) {
            stack.push(neighbors[i].id);
          }
        }
      }

      return nodes;
    }
  }, {
    key: 'hiddenEdges',
    get: function get() {
      var hiddenEdges = [];
      for (var i in this._edgeMap) {
        var edge = this._edgeMap[i];
        if (edge.isHidden) {
          hiddenEdges.push(edge);
        }
      }
      return hiddenEdges;
    }

    /**
     * Retrieves the deadlock edges from this graph
     *
     * @returns {!Array} - the deadlock edges in this graph
     */
  }, {
    key: 'deadlockEdges',
    get: function get() {
      var deadlockEdges = [];
      for (var i in this._edgeMap) {
        var edge = this._edgeMap[i];
        if (edge.isDeadlock) {
          deadlockEdges.push(edge);
        }
      }
      return deadlockEdges;
    }
  }], [{
    key: '_deepCloneObject',
    value: function _deepCloneObject(obj) {
      return JSON.parse(JSON.stringify(obj));
    }
  }]);

  return Graph;
})();

Graph.Node = (function () {

  /**
   * Graph Node object should only be contructed by the Graph class.
   *
   * @protected
   * @param {!Graph} graph     - The Graph this node is apart of
   * @param {!number} uid      - The node's id (must be unquie)
   * @param {!string} label    - The node's label
   * @param {!object} metaData - Any meta data that should be stored
   */

  function _class(graph, uid, label, metaData) {
    _classCallCheck(this, _class);

    this._graph = graph;
    this._id = uid;
    this._label = label;
    this._meta = metaData;
    this._edgesFromMe = {};
    this._edgesToMe = {};
  }

  /**
   * Get the graph this node is apart of.
   *
   * @returns {Graph} The graph
   */

  _createClass(_class, [{
    key: '_addEdgeFromMe',

    /**
     * Remember that the given edge comes from this node.
     * Assumes edge.from === this
     *
     * @param {!Graph.Edge} edge - An edge that comes from this node
     */
    value: function _addEdgeFromMe(edge) {
      this._edgesFromMe[edge.id] = edge;
    }

    /**
     * Remember that the given edge goes to this node.
     * Assumes edge.to === this
     *
     * @param {!Graph.Edge} edge - An edge that goes to this node
     */
  }, {
    key: '_addEdgeToMe',
    value: function _addEdgeToMe(edge) {
      this._edgesToMe[edge.id] = edge;
    }

    /**
     * Determines if the specified edge transitions this node to a valid state.
     * Returns an array of the nodes this edge can transition to. Returns an
     * an array containing undefined if there are no valid transitions.
     *
     * @param {!Edge} edge - The edge to check if there is a valid transition
     * @returns {!Array} array of nodes this edge transitions to
     */
  }, {
    key: 'coaccessible',
    value: function coaccessible(edge) {
      var temp = [];
      var edges = this.edgesFromMe;
      for (var e in edges) {
        if (edges[e].label === edge) {
          temp.push(edges[e].to);
        }
      }

      if (temp.length === 0) {
        temp.push(undefined);
      }

      return temp;
    }

    /**
     * Returns true if this node is accessible within a graph,
     * otherwise returns false.
     */
  }, {
    key: 'isAccessible',
    value: function isAccessible() {
      return this.edgesToMe.length === 0;
    }

    /**
     * Add some meta data to this node.
     *
     * @param {!string} key - The key to save the data under
     * @param {*} value - The data to save
     */
  }, {
    key: 'addMetaData',
    value: function addMetaData(key, value) {
      this._meta[key] = value;
    }

    /**
     * Add the metaData from the specified array to this node
     *
     * @param {!Array} metaDataArray - an array of meta data
     */
  }, {
    key: 'combineMetaData',
    value: function combineMetaData(metaDataArray) {
      for (var key in metaDataArray) {
        this.addMetaData(key, metaDataArray[key]);
      }
    }

    /**
     * Get a copy of a bit of meta data in this node.
     *
     * @param {!string} key - The key to get the data from
     * @returns {*} The data
     */
  }, {
    key: 'getMetaData',
    value: function getMetaData(key) {
      if (typeof obj === 'object') {
        return Graph._deepCloneObject(this._meta[key]);
      }
      return this._meta[key];
    }

    /**
     * Delete some meta data in this node.
     *
     * @param {!string} key - The key the data is saved under
     */
  }, {
    key: 'deleteMetaData',
    value: function deleteMetaData(key) {
      delete this._meta[key];
    }
  }, {
    key: 'graph',
    get: function get() {
      return this._graph;
    }

    /**
     * Get this node's id.
     *
     * @returns {!number} This node's id
     */
  }, {
    key: 'id',
    get: function get() {
      return this._id;
    },

    /**
     * Set this node's id.
     *
     * @param {!number} newId - What to change this node's id to
     * @returns {!number} The new id of this node
     */
    set: function set(newId) {
      var oldId = this._id;
      // dirty check
      if (newId === oldId) {
        return newId;
      }

      // check that the new id isn't already being used
      if (this._graph._nodeMap[newId] !== undefined) {
        throw new Graph.Exception('Cannot set this node\'s id to an id of another node in the graph.');
      }

      // move the node to it's new index
      this._graph._nodeMap[newId] = this._graph._nodeMap[oldId];
      delete this._graph._nodeMap[oldId];
      this._id = newId;

      // if we are changing the root's id
      if (this.graph._rootId === oldId) {
        this.graph._rootId = newId; // make sure the graph gets the update
      }

      // update all the edges that refer to this node
      for (var edgeId in this._graph._edgeMap) {
        // update edges from this node
        if (this._graph._edgeMap[edgeId].from === oldId) {
          this._graph._edgeMap[edgeId].from = newId;
        }
        // update edges to this node
        if (this._graph._edgeMap[edgeId].to === oldId) {
          this._graph._edgeMap[edgeId].to = newId;
        }
      }

      return newId;
    }

    /**
     * Get this node's label.
     *
     * @returns {!string} This node's label
     */
  }, {
    key: 'label',
    get: function get() {
      return this._label;
    },

    /**
     * Set this node's label.
     *
     * @param {string} lbl - The new label for this node
     * @returns {!string} The new label for this node
     */
    set: function set(lbl) {
      this._label = lbl + ''; // convert lbl to a string then set the label
      return this._label;
    }

    /**
     * Get a copy of this node's meta data.
     *
     * @returns {!object} The meta data
     */
  }, {
    key: 'metaData',
    get: function get() {
      return Graph._deepCloneObject(this._meta);
    }

    /**
     * Get an array of all the neighboring nodes of this node.
     *
     * @returns {!array} An array of neighboring nodes
     */
  }, {
    key: 'neighbors',
    get: function get() {
      var nodes = [];
      for (var i in this._edgesFromMe) {
        nodes.push(this._edgesFromMe[i].to);
      }
      return nodes;
    }

    /**
     * Get an array of all the edges from this node.
     *
     * @returns {!array} An array of edges
     */
  }, {
    key: 'edgesFromMe',
    get: function get() {
      var edge = [];
      for (var i in this._edgesFromMe) {
        edge.push(this._edgesFromMe[i]);
      }
      return edge;
    }

    /**
     * Get an array of all the edges to this node.
     *
     * @returns {!array} An array of edges
     */
  }, {
    key: 'edgesToMe',
    get: function get() {
      var edge = [];
      for (var i in this._edgesToMe) {
        edge.push(this._edgesToMe[i]);
      }
      return edge;
    }
  }]);

  return _class;
})();

/**
 * A Graph Edge.
 *
 * @protected
 * @class
 * @property {!Graph} graph  - The Graph this node is apart of (readOnly)
 * @property {!number} id    - The node's id (readOnly)
 * @property {!object} from  - The id of the node this edges comes from (readOnly)
 * @property {!object} to    - The id of the node this edges goes to (readOnly)
 * @property {!string} label - The node's label
 */
Graph.Edge = (function () {

  /**
   * Graph Edge object should only be contructed by the Graph class.
   *
   * @protected
   * @param {!Graph} graph  - The Graph this edges is apart of
   * @param {!number} uid   - The edge's id (must be unquie)
   * @param {!number} from  - The id of the node this edges comes from
   * @param {!number} to    - The id of the node this edges goes to
   * @param {!string} label - The edge's label
   */

  function _class2(graph, uid, from, to, label) {
    var isHidden = arguments.length <= 5 || arguments[5] === undefined ? false : arguments[5];
    var isDeadlock = arguments.length <= 6 || arguments[6] === undefined ? false : arguments[6];

    _classCallCheck(this, _class2);

    this._graph = graph;
    this._id = uid;
    this._from = from;
    this._to = to;
    this._label = label;
    this._isHidden = isHidden;
    this._isDeadlock = isDeadlock;
  }

  /**
   * Get the graph this edges is apart of.
   *
   * @returns {!Graph} The graph
   */

  _createClass(_class2, [{
    key: 'graph',
    get: function get() {
      return this._graph;
    }

    /**
     * Get this edge's id.
     *
     * @returns {!number} This edge's id
     */
  }, {
    key: 'id',
    get: function get() {
      return this._id;
    }

    /**
     * Get the node this edge connects from.
     *
     * @returns {!Graph.Node} The node
     */
  }, {
    key: 'from',
    get: function get() {
      return this._graph._nodeMap[this._from.id];
    }

    /**
     * Get the node this edge connects to.
     *
     * @returns {!Graph.Node} The node
     */
  }, {
    key: 'to',
    get: function get() {
      return this._graph._nodeMap[this._to.id];
    }

    /**
     * Get this edge's label.
     *
     * @returns {!string} The label
     */
  }, {
    key: 'label',
    get: function get() {
      return this._label;
    },

    /**
     * Set this edge's label.
     *
     * @returns {!string} The new label
     */
    set: function set(lbl) {
      this._label = lbl + ''; // convert lbl to a string then set the label
      return this._label;
    }

    /**
     * Get a boolean determining whether this edge is hidden or not.
     */
  }, {
    key: 'isHidden',
    get: function get() {
      return this._isHidden;
    },

    /**
     * Sets the value of isHidden to the specified boolean.
     *
     * @param {!boolean} isHidden - whether or not this edge is hidden
     * @param {!boolean} - the new value of isHidden
     */

    /**
     * Sets isHidden to the specified boolean.
     *
     * @returns {!boolean} The new isHidden value
     */
    set: function set(isHidden) {
      // make sure that parameter is a boolean
      if (isHidden !== true && isHidden !== false) {
        throw new Graph.Exception("Expecting a boolean value but received " + isHidden + "\n");
      }
      this._isHidden = isHidden;
      return this._isHidden;
    }
  }, {
    key: 'isDeadlock',

    /**
     * Get a boolean determining whether this edge leads to a deadlock or not.
     */
    get: function get() {
      return this._isDeadlock;
    },

    /**
     * Sets the value of isDeadlock to the specified boolean.
     *
     * @param {!boolean} isDeadlock - whether or not this edge is a deadlock
     * @returns {!boolean} - the new value of isDeadlock
     */
    set: function set(isDeadlock) {
      this._isDeadlock = isDeadlock;
      return this._isDeadlock;
    }
  }]);

  return _class2;
})();

/**
 * A wrapper class for Graph.Node which also holds a color. Used for performing
 * bisimulation.
 *
 * @class
 * @property {!Object} node - the node this class represents
 * @property {string} color - the color of the node property
 */
Graph.ColoredNode = (function () {

  /**
   * Constructs an instance of ColoredNode. If node has no deadlock transitions to it
   * then it is colored as 0, otherwise it is colored -1.
   *
   * @protected
   * @param {!Object} node - the node to be colored
   * @param {string} color - color of the node
   */

  function _class3(node) {
    var color = arguments.length <= 1 || arguments[1] === undefined ? '0' : arguments[1];

    _classCallCheck(this, _class3);

    this._node = node;
    this._color = color;;

    // check for deadlocks
    var edges = node.edgesToMe;
    for (var e in edges) {
      var edge = edges[e];
      if (edge.isDeadlock) {
        this._color = '-1';
        break;
      }
    }
  }

  /**
   * Returns the node associated with this ColoredNode.
   *
   * @public
   * @returns {!Object} - Node
   */

  _createClass(_class3, [{
    key: 'constructNodeColoring',

    /**
     * Constructs a node coloring for this node.
     *
     * @param {!Array} coloredNodes - Array of colored nodes
     * @returns {!Array} The coloring for the specified colored node
     */
    value: function constructNodeColoring(coloredNodes) {
      var colors = new Graph.NodeColoring();

      // construct coloring for the specified node
      var edges = this._node.edgesFromMe;
      for (var e in edges) {
        var edge = edges[e];
        var from = this._color;
        var to = edge.isDeadlock ? '-1' : coloredNodes[edge.to.id].color;
        var label = edge.isDeadlock ? DELTA : edge.label;
        var color = Graph.NodeColoring.constructColor(from, to, label);

        // only add color if it is not a duplicate
        if (!colors.contains(color)) {
          colors.add(color);
        }
      }

      // check if current node has any deadlock transitions to it
      edges = this._node.edgesToMe;
      for (var e in edges) {
        var edge = edges[e];
        if (edge.isDeadlock) {
          colors.add(Graph.NodeColoring.constructColor('-1', undefined, undefined));
        }
      }

      // if current node is a stop node then give it the empty coloring
      if (colors.length === 0) {
        colors.add(Graph.NodeColoring.constructColor('0', undefined, undefined));
      }

      return colors;
    }
  }, {
    key: 'node',
    get: function get() {
      return this._node;
    }

    /**
     * Returns the color associated with this ColoredNode.
     *
     * @public
     * @returns {string} - Color
     */
  }, {
    key: 'color',
    get: function get() {
      return this._color;
    },

    /**
     * Sets the color associated with this ColoredNode to the specified color.
     *
     * @public
     * @param color - the color to be set
     */
    set: function set(color) {
      this._color = color;
      return this._color;
    }
  }]);

  return _class3;
})();

/**
 * Represents the coloring of a node in a graph. A coloring is an array containing
 * individual colors. A color consists of:
 *   from  - the id of the node an edge is transitioning from
 *   to    - the id of the node an edge is transitioning to
 *   label - the label of the edge
 *
 * @class
 * @property {!Object[]} - array of colors
 */
Graph.NodeColoring = (function () {

  /**
   * Constructs an instance of a node coloring.
   *
   * @protected
   * @param coloring - array of colors
   */

  function _class4() {
    var coloring = arguments.length <= 0 || arguments[0] === undefined ? [] : arguments[0];

    _classCallCheck(this, _class4);

    this._coloring = coloring;
  }

  /**
   * Gets the array of colors associated with this node coloring.
   *
   * @public
   * @returns {!Object[]} - array of colors
   */

  _createClass(_class4, [{
    key: 'add',

    /**
     * Adds the specified color to this coloring. Duplicate colors will not
     * be added.
     *
     * @public
     * @param color - the color to be added
     */
    value: function add(color) {
      if (!this.contains(color)) {
        this._coloring.push(color);
      }
    }

    /**
     * Returns true if the specified color is contained in this node coloring,
     * otherwise returns false.
     *
     * @public
     * @param color - the color to be checked
     * @returns {boolean} - true if color present, otherwise false
     */
  }, {
    key: 'contains',
    value: function contains(color) {
      for (var i in this._coloring) {
        var current = this._coloring[i];
        if (_.isEqual(current, color)) {
          return true;
        }
      }

      return false;
    }

    /**
     * Returns true if both this coloring and the specified coloring are considered equal.
     * To be considered equal, all the colors contained in this coloring must be contained
     * in the specified coloring and vice versa.
     */
  }, {
    key: 'equals',
    value: function equals(coloring) {
      // check that coloring is defined
      if (coloring === undefined || coloring === null) {
        return false;
      }

      // check that both colors are the same length
      if (this._coloring.length != coloring.length) {
        return false;
      }

      // check all the colors in this coloring for a match
      for (var i in this._coloring) {
        var col1 = this._coloring[i];

        // check that there is a match for col1 in the secondary coloring
        var match = false;
        for (var j in coloring) {
          var col2 = coloring[j];
          if (_.isEqual(col1, col2)) {
            match = true;
            break;
          }
        }

        // if there was not a match then return false
        if (!match) {
          return false;
        }
      }

      return true;
    }

    /**
     * Constructs a single color based on the specified from, to and label.
     *
     * @public
     * @param {int} from - id of the node that the specified edge label leaves from
     * @param {int} to - id of the node that the specified edge label transitions to
     * @param {string} label - label given to the edge transition between from and to
     */
  }, {
    key: 'coloring',
    get: function get() {
      return this._coloring;
    }

    /**
     * Gets the number of individual colors within this coloring.
     *
     * @public
     * @returns {int} - number of colors present in this coloring
     */
  }, {
    key: 'length',
    get: function get() {
      return this._coloring.length;
    }
  }], [{
    key: 'constructColor',
    value: function constructColor(from, to, label) {
      return { from: from, to: to, label: label };
    }
  }]);

  return _class4;
})();

/**
 * Class containing static functions that can be used to alter graphs.
 */
Graph.Operations = (function () {
  function _class5() {
    _classCallCheck(this, _class5);
  }

  _createClass(_class5, null, [{
    key: 'abstraction',

    /**
     * Performs the abstraction function on the specified graph, which removes the hidden 
     * tau actions and adds the observable transitions.
     */
    value: function abstraction(graph) {
      var isFair = arguments.length <= 1 || arguments[1] === undefined ? true : arguments[1];

      var clone = graph.deepClone();
      var edgesToAdd = [];
      var hiddenEdges = clone.hiddenEdges;

      for (var i in hiddenEdges) {
        var edge = hiddenEdges[i];

        var from = this._getTransitions(edge.from.edgesToMe, false);
        var to = this._getTransitions(edge.to.edgesFromMe, true);

        edgesToAdd = edgesToAdd.concat(this._addObservableEdges(edge.from, edge.to, from, true));
        edgesToAdd = edgesToAdd.concat(this._addObservableEdges(edge.to, edge.from, to, false));
      }

      // add the edges constructed by the abstraction
      for (var i in edgesToAdd) {
        var edge = edgesToAdd[i];
        if (!clone.containsEdge(edge.from, edge.to, edge.label)) {
          clone.addEdge(edge.uid, edge.from, edge.to, edge.label, edge.isHidden, edge.isDeadlock);
        }
      }

      // if this is a fair abstraction then remove all the hidden edges
      if (isFair) {
        clone.removeHiddenEdges();
      }
      // otherwise only remove the hidden edges from before the abstraction
      else {
          for (var i in hiddenEdges) {
            clone.removeEdge(hiddenEdges[i]);
          }
          // construct deadlocks
          clone = this._constructDeadlocks(clone);
        }
      //clone = this.hideDeadlocks(clone);
      clone = this._constructStopNodes(clone);
      clone.trim();
      return clone;
    }
  }, {
    key: '_getTransitions',
    value: function _getTransitions(edges, isFrom) {
      var transitions = [];
      for (var i in edges) {
        var edge = edges[i];
        if (!edge.isHidden) {
          var node = isFrom ? edge.to : edge.from;
          transitions.push({ node: node, label: edge.label, isDeadlock: edge.isDeadlock });
        }
      }

      return transitions;
    }
  }, {
    key: 'hideDeadlocks',
    value: function hideDeadlocks(graph) {
      var clone = graph.deepClone();
      var edgesToAdd = [];
      var deadlockEdges = clone.deadlockEdges;

      for (var i in deadlockEdges) {
        var edge = deadlockEdges[i];
        var from = this._getTransitions(edge.from.edgesToMe, false);
        var to = this._getTransitions(edge.to.edgesFromMe, true);

        edgesToAdd = edgesToAdd.concat(this._addObservableEdges(edge.from, edge.to, from, true));
        edgesToAdd = edgesToAdd.concat(this._addObservableEdges(edge.to, edge.from, to, false));
      }

      // add the edges constructed by the abstraction
      for (var i in edgesToAdd) {
        var edge = edgesToAdd[i];
        if (!clone.containsEdge(edge.from, edge.to, edge.label)) {
          clone.addEdge(edge.uid, edge.from, edge.to, edge.label, edge.isHidden, edge.isDeadlock);
        }
      }

      for (var i in deadlockEdges) {
        clone.removeEdge(deadlockEdges[i]);
      }

      // add the edges constructed by the abstraction
      for (var i in edgesToAdd) {
        var edge = edgesToAdd[i];
        if (!clone.containsEdge(edge.from, edge.to, edge.label)) {
          clone.addEdge(edge.uid, edge.from, edge.to, edge.label, edge.isHidden, true);
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
  }, {
    key: '_addObservableEdges',
    value: function _addObservableEdges(start, first, transitions, isFrom) {
      var stack = [first];
      var visited = [start];
      var edgesToAdd = [];

      while (stack.length !== 0) {
        var current = stack.pop();
        visited.push(current);

        // add edges to/from the current node
        for (var i in transitions) {
          var transition = transitions[i];
          if (isFrom) {
            edgesToAdd.push(this._constructEdge(EdgeUid.next, transition.node, current, transition.label, false, transition.isDeadlock));
          } else {
            edgesToAdd.push(this._constructEdge(EdgeUid.next, current, transition.node, transition.label, false, transition.isDeadlock));
          }
        }

        var edges = isFrom ? current.edgesFromMe : current.edgesToMe;
        for (var i in edges) {
          var edge = edges[i];
          var node = isFrom ? edge.to : edge.from;

          if (edge.isHidden && !_.contains(visited, node)) {
            stack.push(node);
          }
          // if next node has already been visited add a tau loop
          else if (edge.isHidden && _.contains(visited, node)) {
              edgesToAdd.push(this._constructEdge(EdgeUid.next, node, node, '', true));
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
     * @param {!boolean} isHidden - true if the edge is hidden, otherwise false
     * @returns {!Object} object containing data to construct an edge
     */
  }, {
    key: '_constructEdge',
    value: function _constructEdge(uid, from, to, label, isHidden, isDeadlock) {
      return { uid: uid, from: from, to: to, label: label, isHidden: isHidden, isDeadlock: isDeadlock };
    }

    /**
     * Constructs a deadlock for each hidden edge in the specified graph that transitions
     * back to the node it started from.
     *
     * @private
     * @param {!Object} graph - the graph to construct deadlocks for
     * @returns {!object} - the graph with deadlocks included
     */
  }, {
    key: '_constructDeadlocks',
    value: function _constructDeadlocks(graph) {
      var clone = graph.deepClone();
      var edges = clone.edges;
      for (var e in edges) {
        var edge = edges[e];

        // if hidden edge links back to itself add a dead lock
        if (edge.isHidden && edge.from.id === edge.to.id) {
          var temp = clone.addNode(NodeUid.next);
          // add a new deadlock edge and remove the tau loop
          clone.addEdge(EdgeUid.next, edge.from, temp, '', false, true);
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
  }, {
    key: '_constructStopNodes',
    value: function _constructStopNodes(graph) {
      var clone = graph.deepClone();
      var stack = [clone.root];
      var visited = [];
      while (stack.length !== 0) {
        var node = stack.pop();
        visited.push(node);

        // add neighbours to stack
        var neighbors = node.neighbors;
        for (var i in neighbors) {
          // only add neighbour if it has not been visited
          if (!_.contains(visited, neighbors[i])) {
            stack.push(neighbors[i]);
          }
        }

        // if this is a final node set its meta data to reflect this
        if (neighbors.length === 0) {
          if (node.getMetaData('isTerminal') === undefined) {
            node.addMetaData('isTerminal', 'stop');
          }
        }
      }

      return clone;
    }

    /**
     * Constructs and returns a simplifed version of the specified graph by performing a 
     * bisimulation colouring of the graph an merging the nodes that are of the same color.
     *
     * @param {!object} graph - the graph to be simplified
     * @returns {!object} - a simplified version of the graph
     */
  }, {
    key: 'simplification',
    value: function simplification(graph) {
      // perform the bisimulation
      var result = this._bisimulation([graph]);
      return result.graphs[0];
    }

    /**
     * Determines if the specified graphs are equivalent by performing a bisimulation colouring
     * for each graph and comparing the colours of the root of each graph. If the roots from each graph
     * all have the same colour then all the specified graphs are equivalent.
     *
     * @param {!array} graphs - an array of graphs to check for equivalency
     * @returns {!boolean} - true if all graphs are equivalent, otherwise false.
     */
  }, {
    key: 'isEquivalent',
    value: function isEquivalent() {
      for (var _len2 = arguments.length, graphs = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
        graphs[_key2] = arguments[_key2];
      }

      // perform the bisimulataion
      var result = this._bisimulation(graphs);

      // compare the colors of each root node
      var coloredNodes = result.coloredNodes;
      for (var i = 0; i < graphs.length - 1; i++) {
        var rootId1 = graphs[i].rootId;
        var rootId2 = graphs[i + 1].rootId;

        // if root nodes do not match the graphs are not equivalent
        if (coloredNodes[rootId1].color !== coloredNodes[rootId2].color) {
          return false;
        }
      }

      // if this point is reached then all graphs were equivalent
      return true;
    }

    /**
     * Performs a bisimulation coloring on the specified graphs, which gives the nodes in each graph a colouring
     * based on transitions it makes to neighbouring nodes. Once the colouring is completed any nodes
     * within the same graph with the same colour are considered equivalent and are merged together.
     *
     * @private
     * @param {!array} - an array of graphs to perform bisimulation colouring on.
     * @returns {!object} - the coloured nodes and the simplified graphs
     */
  }, {
    key: '_bisimulation',
    value: function _bisimulation(graphs) {
      var clones = [];
      for (var i in graphs) {
        clones.push(graphs[i].deepClone());
      }

      // construct map of nodes and give them all the same color
      var coloredNodes = [];
      for (var i in clones) {
        var clone = clones[i];
        var nodes = clone.nodes;
        for (var n in nodes) {
          var node = nodes[n];
          coloredNodes[node.id] = new Graph.ColoredNode(node);
        }
      }

      // continue process until color map does not increase in size
      var previousLength = -1;
      var colorMap = [];
      while (previousLength < colorMap.length) {
        previousLength = colorMap.length;
        colorMap = this._constructColoring(coloredNodes);
        coloredNodes = this._applyColoring(coloredNodes, colorMap);
      }

      // merge nodes together that have the same colors
      for (var i in clones) {
        var clone = clones[i];
        var nodes = clone.nodes;

        for (var _i in colorMap) {
          var nodeIds = [];

          for (var k in nodes) {
            var node = coloredNodes[nodes[k].id];
            if (node.color === _i) {
              nodeIds.push(node.node.id);
            }
          }
          if (nodeIds.length > 1) {
            clone.mergeNodes(nodeIds);
          }
        }
      }

      // remove duplicate edges
      for (var i in clones) {
        clones[i].removeDuplicateEdges();
      }

      return { coloredNodes: coloredNodes, graphs: clones };
    }

    /**
     * Helper function for the bisimulation function which constructs and returns
     * a color map for the specified colored nodes.
     *
     * @param {!Array} coloredNodes - The nodes to construct a color map for
     * @returns {!Array} A color map to color the specified nodes with
     */
  }, {
    key: '_constructColoring',
    value: function _constructColoring(coloredNodes) {
      var colorMap = [];
      // get coloring for each node in the graph
      for (var n in coloredNodes) {
        var node = coloredNodes[n];
        var coloring = node.constructNodeColoring(coloredNodes);

        // only add coloring if it is not a duplicate
        var equals = false;
        for (var c in colorMap) {
          equals = colorMap[c].equals(coloring.coloring);
          if (equals) {
            break;
          }
        }
        if (!equals) {
          colorMap.push(coloring);
        }
      }

      return colorMap;
    }

    /**
     * Helper function for the bisimulation function which applies a coloring to
     * the specified colored nodes based on the specified color map.
     *
     * @param {!Array} coloredNodes - Array of colored nodes
     * @param {!Array} colorMap - map of colors
     * @returns {!Array} The new coloring of the colored nodes
     */
  }, {
    key: '_applyColoring',
    value: function _applyColoring(coloredNodes, colorMap) {
      var newColors = [];
      // get new color for each node in the graph
      for (var n in coloredNodes) {
        var node = coloredNodes[n];

        // work out new color for the current node
        var coloring = node.constructNodeColoring(coloredNodes);
        for (var c in colorMap) {
          if (colorMap[c].equals(coloring.coloring)) {
            newColors[n] = c;
            break;
          }
        }
      }

      // apply new color to each node
      for (var i in newColors) {
        coloredNodes[i].color = newColors[i];
      }

      return coloredNodes;
    }

    /**
     * Constructs and returns a parallel composition of the two specified graphs.
     *
     * @class
     * @param {!Object} graph1 - the first graph
     * @param {!Object} graph2 - the second graph
     * @returns - parallel composition of the two graphs
     */
  }, {
    key: 'parallelComposition',
    value: function parallelComposition(graph1, graph2) {
      var nodes1 = graph1.nodes;
      var nodes2 = graph2.nodes;
      var graph = this._combineStates(nodes1, nodes2);
      var alphabet = graph1.alphabetUnion(graph2);

      // add edges
      for (var i = 0; i < nodes1.length; i++) {
        var node1 = nodes1[i];

        for (var j = 0; j < nodes2.length; j++) {
          var node2 = nodes2[j];
          var fromId = this._getId(graph, node1, node2);

          for (var action in alphabet) {

            var c1 = node1.coaccessible(action);
            var c2 = node2.coaccessible(action);

            for (var x in c1) {
              var coaccessible1 = c1[x];
              for (var y in c2) {
                var coaccessible2 = c2[y];

                // check if an edge is needed from the current combined states

                // check if the current action is performed by both the current nodes
                if (coaccessible1 !== undefined && coaccessible2 !== undefined) {
                  // calculate the id of the node the new edge is transitioning to
                  var toId = this._getId(graph, coaccessible1, coaccessible2);
                  var isHidden = graph1.isHiddenEdge(action);
                  graph.addEdge(EdgeUid.next, graph.getNode(fromId), graph.getNode(toId), action, isHidden);
                }

                // check if the current action is done by the outer node and is never performed in the second graph
                else if (coaccessible1 !== undefined && !graph2.containsEdgeInAlphabet(action)) {
                    // calculate the id of the node the new edge is transitioning to
                    var toId = this._getId(graph, coaccessible1, node2);
                    var isHidden = graph1.isHiddenEdge(action);
                    graph.addEdge(EdgeUid.next, graph.getNode(fromId), graph.getNode(toId), action, isHidden);
                  }

                  // check if the current action is done by the inner node and is never performed in the first graph
                  else if (coaccessible2 !== undefined && !graph1.containsEdgeInAlphabet(action)) {
                      // calculate the id of the node the new edge is transitioning to
                      var toId = this._getId(graph, node1, coaccessible2);
                      var isHidden = graph2.isHiddenEdge(action);
                      graph.addEdge(EdgeUid.next, graph.getNode(fromId), graph.getNode(toId), action, isHidden);
                    }
              }
            }
          }
        }
      }

      graph.trim();
      return graph;
    }

    /**
     * Helper function for the parallel composition function which combines both sets of specified
     * nodes into a single graph.
     *
     * @param {!Object} nodes1 - the first set of nodes
     * @param {!Object} nodes2 - the second set of nodes
     * @returns {!Object} - a graph containing the combined states of the two specified node sets
     */
  }, {
    key: '_combineStates',
    value: function _combineStates(nodes1, nodes2) {
      var graph = new Graph();

      // combine states
      for (var i in nodes1) {
        var node1 = nodes1[i];
        // determine if current node is a final node in the first graph
        var startState1 = node1._meta['startNode'] === true;
        var terminalState1 = node1._meta['isTerminal'] === 'stop';
        var label1 = node1.label !== '' ? node1.label : node1.id;

        for (var j in nodes2) {
          var node2 = nodes2[j];
          // determine if the current node is a final node in the second graph
          var startState2 = node2._meta['startNode'] === true;
          var terminalState2 = node2._meta['isTerminal'] === 'stop';
          var label2 = node2.label !== '' ? node2.label : node2.id;
          var node = graph.addNode(NodeUid.next, label1 + "." + label2);

          // if both states are a starting state make new node start state
          if (startState1 && startState2) {
            node.addMetaData('startNode', true);
          }

          // if both states are terminal make new node terminal
          if (terminalState1 && terminalState2) {
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
  }, {
    key: '_getId',
    value: function _getId(graph, node1, node2) {
      var label1 = node1.label === '' ? node1.id : node1.label;
      var label2 = node2.label === '' ? node2.id : node2.label;
      var label = label1 + '.' + label2;
      var nodes = graph.nodes;
      for (var i in nodes) {
        var node = nodes[i];
        if (node.label === label) {
          return node.id;
        }
      }

      return undefined;
    }
  }]);

  return _class5;
})();

/**
 * A Graph Exception.
 *
 * @class
 * @property {!string} message - The message
 */
Graph.Exception = (function () {

  /**
   * @param {!string} msg - The message
   */

  function _class6(msg) {
    _classCallCheck(this, _class6);

    this.message = msg;
  }

  return _class6;
})();