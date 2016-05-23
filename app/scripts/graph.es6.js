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
   * Sets the root's id for this graph to the specified id.
   *
   * @param {int} id - the new root id
   * @param {int} - the new root id
   */
  set rootId(id){
    if(this._nodeMap[id] !== undefined){
      this._rootId = id;
      return this._rootId;
    }

    // throw error: id is not a valid node id
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
   * Returns the node specified by the given unique identifier.
   *
   * @param {int} id - the node id
   * @return {node} - node with the given id
   */
  getNode(id){
    if(this._nodeMap[id] !== undefined){
      return this._nodeMap[id];
    }

    // throw error: not valid id
  }

  /**
   * Constructs and adds a new node to this graph. Returns the
   * constructed node.
   *
   * @param {int} id - unqiue identifier for the new node
   * @param {string} label - the label for the new node
   * @param {map} metaData - meta data for new node
   * @return {node} - the new node
   */
  addNode(id, label, metaData){
    // determine if paramaters have been defined
    id = (id === undefined) ? this.nextNodeId : id;
    label = (label === undefined) ? '' : label;
    metaData = (metaData === undefined) ? {} : metaData;
    let node = new Graph.Node(id, label, metaData);
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
   * Returns the next unique identifier for a node.
   *
   * @return {int} - next node id
   */
  get nextNodeId(){
    return this._nextNodeId++;
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
   * @param {int} id - the id for the new edge
   * @param {string} label - the action label the new edge represents
   * @param {int} from - the id of the node the new edge transitions from
   * @param {int} to - the id of the node the new edge transitions to
   * @return {edge} - the new edge
   */
  addEdge(id, label, from, to){
    // check that ids recieved are valid
    if(this._nodeMap[from] === undefined){
      // throw error: from not defined
    }
    if(this._nodeMap[to] === undefined){
      // throw error: to not defined
    }

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
   * Returns the next unique identifier for an edge.
   *
   * @return {int} - next edge id
   */
  get nextEdgeId(){
    return this._nextEdgeId++;
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
  
  /**
   * Adds the specified graph to this graph. Merges the root node of the specified
   * graph with the given node from this graph.
   *
   * @param {graph} graph - the graph to add
   * @param {node} node - the node to merge on
   */
  addGraph(graph, node){
    let rootId;
    // add nodes to this graph
    let nodes = graph.nodes;
    for(let i = 0; i < nodes.length; i++){
      let n = this.addNode(this.nextNodeId, nodes[i].label, nodes[i].cloneMetaData);
      
      // update root on first pass through the loop
      if(i === 0){
        rootId = n.id;
      }

      // updated references for the current node
      let edges = nodes[i].edgesFromMe;
      for(let j = 0; j < edges.length; j++){
        edges[j].from = n.id;
      }
      
      edges = nodes[i].edgesToMe;
      for(let j = 0; j < edges.length; j++){
        edges[j].to = n.id;
      }
    }
    
    // add edges to this graph
    let edges = graph.edges;
    for(let i = 0; i < edges.length; i++){
      this.addEdge(this.nextEdgeId, edges[i].label, edges[i].from, edges[i].to);
    }
    
    // merge on specified node
    this.mergeNodes([node, this._nodeMap[rootId]]);
  }

  /**
   * Returns a clone of this graph.
   *
   * @return {graph} - clone of graph
   */
  get clone(){
    let clone = new Graph();

    // add nodes to clone
    let nodes = this.nodes;
    for(let i = 0; i < nodes.length; i++){
      clone.addNode(nodes[i].id, nodes[i].label, nodes[i].cloneMetaData);
    }

    // add edges to clone
    let edges = this.edges;
    for(let i = 0; i < edges.length; i++){
      clone.addEdge(edges[i].id, edges[i].label, edges[i].from, edges[i].to);

      // add edges to the nodes they transition between
      this.getNode(edges[i].from).addEdgeFromMe(edges[i]);
      this.getNode(edges[i].to).addEdgeToMe(edges[i]);
    }

    clone.rootId = this._rootId;
    clone._nextNodeId = clone.nodeCount;
    clone._nextEdgeId = clone.edgeCount;

    return clone;
  }
}

Graph.Node = class{

  constructor(id, label, metaData){
    this._id = id;
    this._label = label;
    this._edgesFromMe = {};
    this._edgesToMe = {};
    this._metaData = metaData;
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