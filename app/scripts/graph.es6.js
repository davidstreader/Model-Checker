// jscs:enable esnext
// jshint esnext:true
'use strict';

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
class Graph {

  constructor() {
    this._nodeCount = 0;
    this._edgeCount = 0;
    this._nodeMap = {};
    this._edgeMap = {};
    this._rootId = undefined;
  }

  /**
   * Get an array of the nodes in this graph where the root node is be the first element.
   *
   * @returns {!array} An array of the nodes in this graph
   */
  get nodes() {
    let nodes = [this.root];
    for (let id in this._nodeMap) {
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
  get edges() {
    let edges = [];
    for (let key in this._edgeMap) {
      edges.push(this._edgeMap[key]);
    }
    return edges;
  }

  /**
   * Get the root node of this graph.
   *
   * @returns {!Graph.Node} The root.
   */
  get root() {
    return this._nodeMap[this._rootId];
  }

  get rootId() {
    return this._rootId;
  }

  /**
   * Set the node that should be used as the graphs root.
   *
   * @param {!Graph.Node} node - The node to use as the root node
   * @returns {Graph.Node} The new root node
   */
  set root(node) {
    if (node === this.root) {
      return node;
    }
    if (node) {
      if (node.graph === this) {
        return this._setRootNodeById(node.id);
      }
      throw new Graph.Exception(
        'cannot set the root of this graph to a node that is not in it.');
    }
    this._rootId = undefined;
    return undefined;
  }

  /**
   * The number of nodes in this graph.
   *
   * @returns {!number} The number of nodes in this graph
   */
  get nodeCount() {
    return this._nodeCount;
  }

  /**
   * The number of edges in this graph.
   *
   * @returns {!number} The number of edges in this graph
   */
  get edgeCount() {
    return this._edgeCount;
  }

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
  addNode(uid, label='', metaData={}) {
    if (this._nodeMap[uid] !== undefined) {
      throw new Graph.Exception(
        'This graph already contains a node with the id "' + uid + '".');
    }

    let node = new Graph.Node(this, uid, label,
      Graph._deepCloneObject(metaData));

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
  addEdge(uid, from, to, label='') {
    if (this._edgeMap[uid] !== undefined) {
      throw new Graph.Exception(
        'This graph already contains a edge with id "' + uid + '".');
    }

    let edge = new Graph.Edge(this, uid, from, to, label);
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
  getNode(id) {
    return this._nodeMap[id];
  }

  /**
   * Returns a set of the reachable nodes in this graph from the root node.
   *
   * @returns {!Set} Set of node ids
   */
  get reachableNodes() {
    var nodes = [];
    var stack = [this.rootId];
    
    // perfrom depth first search of graph
    while(stack.length !== 0){
      var id = stack.pop();
      // add current node id to the set
      nodes[id] = true;
      var node = this.getNode(id);

      // add neighbours of current node to stack
      var neighbors = node.neighbors;
      for(var i = 0; i < neighbors.length; i++){
        stack.push(neighbors[i].id);
      }
    }

    return nodes;
  }

  /**
   * Get an edge in the graph.
   *
   * @param {!number} id - The id of the edge to get
   * @returns {Graph.Edge} The edge
   */
  getEdge(id) {
    return this._edgeMap[id];
  }

  /**
   * Returns true if the specified edge is contained in this graph's alphabet,
   * otherwise returns false.
   *
   * @returns {boolean} Whether the edge is contained in the graph's alphabet or not
   */
  containsEdge(edge) {
    var result = this.constructAlphabet()[edge];
    if(result === true){
      return true;
    }
    return false;
  }

  /**
   * Remove a node from the graph.
   * Any edges connected to this node will also be deleted.
   *
   * @param {Graph.Node} node - The node to remove
   */
  removeNode(node) {
    if (!node || node.graph !== this || this._nodeMap[node.id] !== node) {
      return;
    }

    for (let i in node._edgesToMe) {
      this.removeEdge(node._edgesToMe[i]);
    }

    for (let i in node._edgesFromMe) {
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
  removeEdge(edge) {
    if (!edge || edge.graph !== this || this._edgeMap[edge.id] !== edge) {
      return;
    }

    delete this._edgeMap[edge.id].to._edgesToMe[edge.id];
    delete this._edgeMap[edge.id].from._edgesFromMe[edge.id];
    delete this._edgeMap[edge.id];
    this._edgeCount -= 1;
  }

  /**
   * Constructs and returns a set containing the alphabet for this graph.
   * The alphabet is a collection of the actions that transition nodes from
   * one state to another.
   */
  constructAlphabet() {
    var alphabet = {};
    for(let i in this._edgeMap){
      var label = this._edgeMap[i].label;
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
  alphabetUnion(otherGraph) {
    var alphabet = this.constructAlphabet();
    var temp = otherGraph.constructAlphabet();

    for(let a in temp){
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
  _setRootNodeById(id) {
    this._rootId = id;
    return this.root;
  }

  /**
   * Create a deep clone of this graph.
   * The clone's new nodes and edges will have the same ids as this graph.
   *
   * @returns {!Graph} The clone
   */
  deepClone() {
    let clone = new Graph();

    // copy all the nodes
    for (let key in this._nodeMap) {
      clone.addNode(
        this._nodeMap[key].id,
        this._nodeMap[key].label,
        this._nodeMap[key].metaData);
    }

    // copy all the edges
    for (let key in this._edgeMap) {
      clone.addEdge(
        this._edgeMap[key].id,
        clone.getNode(this._edgeMap[key].from.id),  // make sure to use the copied node, not the original
        clone.getNode(this._edgeMap[key].to.id),
        this._edgeMap[key].label);
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
  combineWith(...graphs) {
    // for each graph given
    for (let i = 0; i < graphs.length; i++) {
      // for each node in that graph
      for (let key in graphs[i]._nodeMap) {
        // if this graph doesn't already have a node with this id
        if (this._nodeMap[graphs[i]._nodeMap[key].id] === undefined) {
          // add the node
          this.addNode(
            graphs[i]._nodeMap[key].id,
            graphs[i]._nodeMap[key].label,
            graphs[i]._nodeMap[key].metaData);
        }
      }

      // for each edge in that graph
      for (let key in graphs[i]._edgeMap) {
        // if this graph doesn't already have an edge with this id
        if (this._edgeMap[graphs[i]._edgeMap[key].id] === undefined) {
          // add the edge
          this.addEdge(
            graphs[i]._edgeMap[key].id,
            this.getNode(graphs[i]._edgeMap[key].from.id), // use the node in this graph, not the one in the other graph
            this.getNode(graphs[i]._edgeMap[key].to.id),
            graphs[i]._edgeMap[key].label);
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
  mergeNodes(nodeIds) {
    let mergedNode;
    let mergedMetaData  = {};

    // for each node id specified to merge
    for (let i = 0; i < nodeIds.length; i++) {
      let node = this.getNode(nodeIds[i]);  // get the node
      // save all the meta data in this node
      let meta = node.metaData;
      for (let key in meta) {
        mergedMetaData[key] = meta[key];
      }

      // if this is the first node we are dealing with (i === 0)
      if (mergedNode === undefined) {
        mergedNode = node;  // save it
        continue;           // and move on to the next node
      }

      // update the edges from this node to be from the merged node
      for (let key in node._edgesFromMe) {
        let edge = node._edgesFromMe[key];
        edge._from = mergedNode;
        mergedNode._addEdgeFromMe(edge);
      }
      node._edgesFromMe = {};

      // update the edges to this node to be to the merged node
      for (let key in node._edgesToMe) {
        let edge = node._edgesToMe[key];
        edge._to = mergedNode;
        mergedNode._addEdgeToMe(edge);
      }
      node._edgesToMe = {};

      // remove the node from the graph
      this.removeNode(node);
    }

    mergedNode._meta = mergedMetaData;    // set the merged node's meta data

    return mergedNode;
  }

  /**
   * Trims any nodes from the graph that are not reachable from the root node.
   */
  trim() {
    // get the reachable nodes from the root
    var reachable = this.reachableNodes;

    // remove any nodes that are not reachable from the root
    for(let node in this._nodeMap){
      if(reachable[node] !== true){
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
  static _deepCloneObject(obj) {
    return JSON.parse(JSON.stringify(obj));
  }
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
Graph.Node = class {

  /**
   * Graph Node object should only be contructed by the Graph class.
   *
   * @protected
   * @param {!Graph} graph     - The Graph this node is apart of
   * @param {!number} uid      - The node's id (must be unquie)
   * @param {!string} label    - The node's label
   * @param {!object} metaData - Any meta data that should be stored
   */
  constructor(graph, uid, label, metaData) {
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
  get graph() {
    return this._graph;
  }

  /**
   * Get this node's id.
   *
   * @returns {!number} This node's id
   */
  get id() {
    return this._id;
  }

  /**
   * Set this node's id.
   *
   * @param {!number} newId - What to change this node's id to
   * @returns {!number} The new id of this node
   */
  set id(newId) {
    let oldId = this._id;
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
      this.graph._rootId = newId;   // make sure the graph gets the update
    }

    // update all the edges that refer to this node
    for (let edgeId in this._graph._edgeMap) {
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
  get label() {
    return this._label;
  }

  /**
   * Set this node's label.
   *
   * @param {string} lbl - The new label for this node
   * @returns {!string} The new label for this node
   */
  set label(lbl) {
    this._label = lbl + ''; // convert lbl to a string then set the label
    return this._label;
  }

  /**
   * Get a copy of this node's meta data.
   *
   * @returns {!object} The meta data
   */
  get metaData() {
    return Graph._deepCloneObject(this._meta);
  }

  /**
   * Get an array of all the neighboring nodes of this node.
   *
   * @returns {!array} An array of neighboring nodes
   */
  get neighbors() {
    let nodes = [];
    for (let i in this._edgesFromMe) {
      nodes.push(this._edgesFromMe[i].to);
    }
    return nodes;
  }

  /**
   * Get an array of all the edges from this node.
   *
   * @returns {!array} An array of edges
   */
  get edgesFromMe() {
    let edge = [];
    for (let i in this._edgesFromMe) {
      edge.push(this._edgesFromMe[i]);
    }
    return edge;
  }

  /**
   * Get an array of all the edges to this node.
   *
   * @returns {!array} An array of edges
   */
  get edgesToMe() {
    let edge = [];
    for (let i in this._edgesToMe) {
      edge.push(this._edgesToMe[i]);
    }
    return edge;
  }

  /**
   * Remember that the given edge comes from this node.
   * Assumes edge.from === this
   *
   * @param {!Graph.Edge} edge - An edge that comes from this node
   */
  _addEdgeFromMe(edge) {
    this._edgesFromMe[edge.id] = edge;
  }

  /**
   * Remember that the given edge goes to this node.
   * Assumes edge.to === this
   *
   * @param {!Graph.Edge} edge - An edge that goes to this node
   */
  _addEdgeToMe(edge) {
    this._edgesToMe[edge.id] = edge;
  }

  /**
   * Determines if the specified edge transitions this node to a valid state.
   * Returns the nodeId of the node if there is a valid transition, otherwise
   * returns -1.
   *
   * @param {!Edge} edge - The edge to check if there is a valid transition
   */
  coaccessible(edge){
    for(let e in this._edgesFromMe) {
      if(this._edgesFromMe[e].label === edge){
        return this._edgesFromMe[e]._to._id;
      }
    }

    return -1;
  }

  /**
   * Returns true if this node is accessible within a graph,
   * otherwise returns false.
   */
  isAccessible(){
    return this.edgesToMe.length === 0;
  }

  /**
   * Add some meta data to this node.
   *
   * @param {!string} key - The key to save the data under
   * @param {*} value - The data to save
   */
  addMetaData(key, value) {
    this._meta[key] = value;
  }

  /**
   * Get a copy of a bit of meta data in this node.
   *
   * @param {!string} key - The key to get the data from
   * @returns {*} The data
   */
  getMetaData(key) {
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
  deleteMetaData(key) {
    delete this._meta[key];
  }

};

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
Graph.Edge = class {

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
  constructor(graph, uid, from, to, label) {
    this._graph = graph;
    this._id = uid;
    this._from = from;
    this._to = to;
    this._label = label;
  }

  /**
   * Get the graph this edges is apart of.
   *
   * @returns {!Graph} The graph
   */
  get graph() {
    return this._graph;
  }

  /**
   * Get this edge's id.
   *
   * @returns {!number} This edge's id
   */
  get id() {
    return this._id;
  }

  /**
   * Get the node this edge connects from.
   *
   * @returns {!Graph.Node} The node
   */
  get from() {
    return this._graph._nodeMap[this._from.id];
  }

  /**
   * Get the node this edge connects to.
   *
   * @returns {!Graph.Node} The node
   */
  get to() {
    return this._graph._nodeMap[this._to.id];
  }

  /**
   * Get this edge's label.
   *
   * @returns {!string} The label
   */
  get label() {
    return this._label;
  }

  /**
   * Set this edge's label.
   *
   * @returns {!string} The new label
   */
  set label(lbl) {
    this._label = lbl + ''; // convert lbl to a string then set the label
    return this._label;
  }
};

/**
 * A Graph Exception.
 *
 * @class
 * @property {!string} message - The message
 */
Graph.Exception = class {

  /**
   * @param {!string} msg - The message
   */
  constructor(msg) {
    this.message = msg;
  }
};