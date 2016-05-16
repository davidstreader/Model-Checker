class Graph{

  constructor(id){
    this._id = id;
    this._rootId = undefined;
    this._nodeMap = {};
    this._nodeCount = 0;
    this._edgeMap = {};
    this._edgeCount = 0;
    this._nextNodeId = 0;
    this._nextEdgeId = 0;
  }

  /**
   * Returns the unique identifier for this graph.
   *
   * @return {int} - graph id
   */
  get id(){
    return this._id;
  }

  /**
   * Returns the root node for this graph.
   *
   * @return {node} - the root
   */
  get root(){
    return this._nodeMap[this._rootId];
  }
  
  set root(node){
    if(node === this.root){
      return node;
    }
    
    for(let i in this._nodeMap){
      if(node === this._nodeMap[i]){
        this._rootId = node.id;
        return node;
      }
    }
    
    // node not in graph, throw error
  }
  
  /**
   * Returns the unique identifier for the root node of this graph.
   *
   * @return {int} - root id
   */
  get rootId(){
    return this._rootId;
  }
  
  /**
   * Returns an array of the nodes associated with this graph.
   * The root node is guarenteed to be the first node in the
   * the array.
   *
   * @return {node[]} - an array of nodes
   */
  get nodes(){
    var nodes = [this._nodeMap[this._rootId]];
    for(var i in this._nodeMap){
      i = parseInt(i, 10);
      if(i !== this._rootId){
        nodes.push(this._nodeMap[i]);
      }
    }

    return nodes;
  }

  /**
   * Constructs and adds a new node to this graph. Returns the
   * constructed node.
   *
   * @return {node} - the new node
   */
  addNode(){
    let id = this._nextNodeId++;
    let node = new Graph.Node(id);
    this._nodeMap[id] = node;
    this._nodeCount++;
    return node;
  }

  /**
   * Returns the number of nodes currently in this graph.
   *
   * @return {int} - node count
   */
  get nodeCount(){
    return this._nodeCount;
  }

  /**
   * Returns an array of the edges in this graph.
   *
   * @return {edge[]} - an array of edges
   */
  get edges(){
    let edges = [];
    for(let i in this._edgeMap){
      edges.push(this._edgeMap[i]);
    }

    return edges;
  }

  /**
   * Constructs and adds a new edge to this graph with the specified
   * label and the node ids that the new edge transitions to and from.
   * Returns the new edge.
   *
   * @param {string} label - the action label the new edge represents
   * @param {int} from - the id of the node the new edge transitions from
   * @param {int} to - the id of the node the new edge transitions to
   * @return {edge} - the new edge
   */
  addEdge(label, from, to){
    // check that ids recieved are valid
    if(this._nodeMap[from] === undefined){
      // throw error: from not defined
    }
    if(this._nodeMap[to] === undefined){
      // throw error: to not defined
    }

    let id = this._nextEdgeId++;
    let edge = new Graph.Edge(id, label, from, to);
    this._nodeMap[from].addEdgeFromMe(edge);
    this._nodeMap[to].addEdgeToMe(edge);
    this._edgeMap[id] = edge;
    this._edgeCount++;
    return edge;
  }

  /**
   * Returns the number of edges currently in this graph.
   *
   * @return {int} - edge count
   */
  get edgeCount(){
    return this._edgeCount;
  }
  
  /**
   * Merges the nodes in the specified array into a single node.
   * The first element of the array is the plce which the remaining
   * elements will be merged with.
   *
   * @param {node[]} - an array of nodes
   */
  mergeNodes(nodes){
    let node = nodes[0];
    
    // merge remaining nodes to this to node
    for(let i = 1; i < nodes.length; i++){
      let current = nodes[i];
      
      let edges = current.edgesFromMe;
      for(let j = 0; j < edges.length; j++){
        edges[j].from = node.id;
        node.addEdgeFromMe(edges[j]);
      }
      
      edges = current.edgesToMe;
      for(let j = 0; j < edges.length; j++){
        edges[j].to = node.id;
        node.addEdgeToMe(edges[j]);
      }
      
      delete this._nodeMap[current.id];
      this._nodeCount--;
    }
  }
  
  addGraph(graph, node){
    // add nodes to this graph
    let nodes = graph.nodes;
    for(let i = 0; i < nodes.length; i++){
      let id = this._nextNodeId++;
      nodes[i].id = id;
      
      let edges = nodes[i].edgesFromMe;
      for(let j = 0; j < edges.length; j++){
        edges[i].from = id;
      }
      
      edges = nodes[i].edgesToMe;
      for(let j = 0; j < edges.length; j++){
        edges[j].to = id;
      }
      
      this._nodeMap[id] = nodes[i];
      this._nodeCount++;
    }
    
    // add edges to this graph
    let edges = graph.edges;
    for(let i = 0; i < edges.length; i++){
      let id = this._nextEdgeId++;
      edges[i].id = id;
      this._edgeMap[id] = edges[i];
      this._edgeCount++;
    }
    
    // merge on specified node
    this.mergeNodes([node, graph.root]);
  }

  /**
   * Returns a clone of this graph.
   *
   * @return {graph} - clone of graph
   */
  get clone(){
    let clone = new Graph();

    // add edges to clone
    var edges = this.edges;
    for(let i = 0; i < edges.length; i++){
      var edge = new Graph.Edge(edges[i].id, edges[i].label, edges[i].from, edges[i].to);
      clone._edgeMap[edge.id] = edge;
      clone._edgeCount++;
    }
  
    // add nodes to clone
    var nodes = this.nodes;
    for(var i = 0; i < nodes.length; i++){
      var node = new Graph.Node(nodes[i].id);
      node._metaData = nodes[i].cloneMetaData
      clone._nodeMap[node.id] = node;
      clone._nodeCount++;
    
    // add edges to and from node
    edges = 
    for(let j = 0; j < edges.length; j++){
      
    }
    }

    return clone;
  }
}

Graph.Node = class{

  constructor(id){
    this._id = id;
    this._label = '';
    this._edgesFromMe = {};
    this._edgesToMe = {};
    this._metaData = {};
  }

  /**
   * Returns the unique identifier for this node.
   *
   * @return {int} - node id
   */
  get id(){
    return this._id;
  }

  /**
   * Sets the unique identifier for this node
   *
   * @param {int} id - the new id
   * @return {int} - the new id
   */
  set id(id){
    this._id = id;
    return id;
  }

  /**
   * Returns the label of this node.
   *
   * @return {string} - label
   */
  get label(){
    return this._label;
  }

  /**
   * Sets the label of this node to the specified label.
   * Returns the new label.
   *
   * @param {string} label - the new label
   * @return {string} - the new label
   */
  set label(label){
    this._label = label;
    return this._label;
  }

  /**
   * Returns an array of edges that transition from this node.
   *
   * @return {edge[]} - array of edges
   */
  get edgesFromMe(){
    var edges = [];
    for(let i in this._edgesFromMe){
      edges.push(this._edgesFromMe[i]);
    }

    return edges;
  }

  /**
   * Adds the specified edge to the map of edges that
   * transitions from this node.
   *
   * @param {edge} edge - the edge to add
   */
  addEdgeFromMe(edge){
    this._edgesFromMe[edge.id] = edge;
  }

  /**
   * Returns an array of edges that transition to this node.
   *
   * @return {edge[]} - array of edges
   */
  get edgesToMe(){
    var edges = [];
    for(let i in this._edgesToMe){
      edges.push(this._edgesToMe[i]);
    }

    return edges;
  }

  /**
   * Adds the specified edge to the map of edges that
   * transition to this node.
   *
   * @param {edge} edge - the edge to add
   */
  addEdgeToMe(edge){
    this._edgesToMe[edge.id] = edge;
  }

  /**
   * Returns an array of neighbouring nodes to this node.
   * A neighbouring node is a node which is transitionable
   * to from this node.
   *
   * @return {node[]} - array of nodes
   */
  get neighbours(){
    let neighbours = []
    for(let i in this._edgesFromMe){
      neighbours.push(this._edgesFromMe[i].to);
    }

    return neighbours;
  }

  /**
   * Returns true if this node is accessible, otherwise returns
   * false. A node is considered accessible when there are edges
   * transitioning to that node.
   *
   * @return {boolean} - whether this node is accessible or not
   */
  get isAccessible(){
    return this._edgesToMe.length === 0;
  }

  /**
   * Adds the specified key-value pair to the meta data
   * associated with this node.
   *
   * @param {string} key - the data key
   * @param {var} value - the value to add
   */
  addMetaData(key, value){
    this._metaData[key] = value;
  }

  /**
   * Returns the value associated with the specified key.
   * returns undefined if the key is not present in the
   * meta data for this node.
   *
   * @param {string} key - the data key
   * @return {var} - the value associated with the key
   */
  getMetaData(key){
    return this._metaData[key];
  }

  /**
   * Deletes the entry in the meta data for the specified key.
   *
   * @param {string} key - the data key
   */
  deleteMetaData(key){
    delete this._metaData[key];
  }

  /**
   * Returns a clone of the meta data associated with this node.
   *
   * @return {object} - cloned meta data
   */
  get cloneMetaData(){
    return JSON.parse(JSON.stringify(this._metaData));
  }
}

Graph.Edge = class{

  constructor(id, label, from, to){
    this._id = id;
    this._label = label;
    this._from = from;
    this._to = to;
  }

  /**
   * Returns the unique identifier for this edge.
   *
   * @return {int} - edge id
   */
  get id(){
    return this._id;
  }
  
  /**
   * Sets the unique identifier for this edge to the specified
   * id.
   *
   * @param {int} id - the new id
   * @return {int} - the new id
   */
  set id(id){
    this._id = id;
    return this._id;
  }
  
  /**
   * Returns the action label associated with this edge.
   *
   * @return {string} - label
   */
  get label(){
    return this._label;
  }

  /**
   * Sets the action label of this edge to the specified
   * label.
   *
   * @param {string} label - the new label
   * @return {string} - the new label
   */
  set label(label){
    this._label = label;
    return this._label;
  }

  /**
   * Returns the unique identifier for the node that this
   * edge transitions from.
   *
   * @return {int} - node id
   */
  get from(){
    return this._from;
  }

  /**
   * Sets the unique identifier for the node that this edge
   * transitions from to the specified id.
   *
   * @param {int} id - the new id
   * @return {int} - the new id
   */
  set from(id){
    this._from = id;
    return this._from;
  }

  /**
   * Returns the unique identifier for the node that this
   * edge transitions to.
   *
   * @return {int} - node id
   */
  get to(){
    return this._to;
  }

  /**
   * Sets the unique identifier for the node that this edge
   * transitions to to the specified id.
   *
   * @param {int} id - the new id
   * @return {int} - the new id
   */
  set to(id){
    this._to = id;
    return this._to;
  }
}