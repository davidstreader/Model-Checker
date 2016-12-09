'use strict';

const AUTOMATON = {
	get type(){
		return this.type;
	},

	get id(){
		return this.id;
	},

	get root(){
		return this.nodeMap[this.rootId];
	},

	set root(id){
		this.rootId = id;
	},

	get nodes(){
		const nodes = [];
		for(let id in this.nodeMap){
			nodes.push(this.nodeMap[id]);
		}

		return nodes;
	},

	getNode: function(id){
		return this.nodeMap[id];
	},

	addNode: function(id, metaData){
		id = (id === undefined) ? this.nextNodeId : id;
		metaData = (metaData === undefined) ? {} : metaData;
		const locationSet = {};
		locationSet[this.id] = true;
		const node = new AutomatonNode(id, {}, {}, locationSet, metaData);
		this.nodeMap[id] = node;
		this.nodeCount++;
		return node;
	},

	removeNode: function(id){
		if(this.nodeMap[id] === undefined){
			return;
		}

		delete this.nodeMap[id];

		for(let i in this.edgeMap){
			const edge = this.edgeMap[i];
			if(edge.incomingNode === id){
				this.removeEdge(i);
			}

			if(edge.outgoingNode === id){
				this.removeEdge(i);
			}
		}

		if(this.rootId === id){
			this.rootId === undefined;
		}

		this.nodeCount--;
	},

	combineNodes: function(node1, node2){
		const incoming = node2.incomingEdges;
		for(let i = 0; i < incoming.length; i++){
			const id = incoming[i];
			node1.addIncomingEdge(id);
			this.getEdge(id).to = node1.id;
		}

		const outgoing = node2.outgoingEdges;
		for(let i = 0; i < outgoing.length; i++){
			const id = outgoing[i];
			node1.addOutgoingEdge(id);
			this.getEdge(id).from = node1.id;
		}

		node1.locations = node2.locations;

		for(let key in node2.metaData){
			node1.metaData[key] = node2.metaData[key];
		}

		delete this.nodeMap[node2.id];
		this.nodeCount--;
	},

	coaccessible: function(node, label){
		const edges = node.outgoingEdges.map(id => this.getEdge(id)).filter(e => e.label === label);
		for(let i = 0; i < edges.length; i++){
			const edge = edges[i];
			const next = this.getNode(edge.to);
			edges[i] = { node:next, edge:edge };
		}

		return edges;
	},

	isCoaccessible: function(node, label){
		const edges = node.outgoingEdges.map(id => this.getEdge(id));
		for(let i = 0; i < edges.length; i++){
			if(edges[i].label === label){
				return true;
			}
		}

		return false;
	},

	get edges(){
		const edges = [];
		for(let id in this.edgeMap){
			edges.push(this.edgeMap[id]);
		}

		return edges;
	},

	getEdge: function(id){
		return this.edgeMap[id];
	},

	addEdge: function(id, label, from, to, metaData){
		const locationSet = {};
		locationSet[this.id] = true;

		if(metaData === undefined){
			metaData = {};
			const edges = this.edges.filter(e => e.label === label);
			metaData.action = edges.length;
		}

		const edge = new AutomatonEdge(id, label, from.id, to.id, locationSet, metaData);
		from.addOutgoingEdge(id);
		to.addIncomingEdge(id);
		this.edgeMap[id] = edge;
		this.edgeCount++;
		return edge;
	},

	removeEdge: function(id){
		if(this.edgeMap[id] === undefined){
			return;
		}

		delete this.edgeMap[id];
		this.edgeCount--;

		const nodes = this.nodes;
		for(let i = 0; i < nodes.length; i++){
			const node = nodes[i];
			delete node.incomingEdgeSet[id];
			delete node.outgoingEdgeSet[id];
		}
	},

	removeDuplicateEdges: function(){
		const toDelete = {};
		const edges = this.edges;
		for(let i = 0; i < edges.length; i++){
			const edge1 = edges[i];
			for(let j = i + 1; j < edges.length; j++){
				const edge2 = edges[j];
				if(toDelete[edge2.id]){
					continue;
				}

				if(edge1.from === edge2.from && edge1.to === edge2.to && edge1.label === edge2.label){
					toDelete[edge2.id] = true;
				}
			}
		}

		for(let id in toDelete){
			this.removeEdge(id);
		}
	},

	get alphabet(){
		const labels = this.edges.map(e => e.label);
		const alphabet = {};
		for(let i = 0; i < labels.length; i++){
			alphabet[labels[i]] = true;
		}


		return alphabet;
	},

	relabelEdges: function(oldLabel, newLabel){
		const edges = this.edges.filter(e => e.label === oldLabel);
		for(let i = 0; i < edges.length; i++){
			edges[i].label = newLabel;
		}
	},

	addAutomaton: function(automaton){
		const nodes = automaton.nodes;
		for(let i = 0; i < nodes.length; i++){
			const node = nodes[i];
			this.nodeMap[node.id] = node;
			this.nodeCount++;
		}

		const edges = automaton.edges;
		for(let i = 0; i < edges.length; i++){
			const edge = edges[i];
			this.edgeMap[edge.id] = edge;
			this.edgeCount++;
		}
	},

	get clone(){
		if(this.metaData.cloneCount === undefined){
			this.metaData.cloneCount = 0;
		}

		const cloneId = this.metaData.cloneCount++;

		const automaton = new Automaton(this.id + '.' + cloneId);

		// clone nodes from this automaton and add them to the clone
		const nodes = this.nodes;
		for(let i = 0; i < nodes.length; i++){
			const id = nodes[i].id + '.' + cloneId;
			const incoming = relabelSet(JSON.parse(JSON.stringify(nodes[i].incomingEdgeSet)));
			const outgoing = relabelSet(JSON.parse(JSON.stringify(nodes[i].outgoingEdgeSet)));
			const locations = JSON.parse(JSON.stringify(nodes[i].locationSet));
			const metaData = JSON.parse(JSON.stringify(nodes[i].metaData));
			const node = new AutomatonNode(id, incoming, outgoing, locations, metaData);
			automaton.nodeMap[id] = node;
			automaton.nodeCount++;

			if(nodes[i].id === this.rootId){
				automaton.root = id;
				node.metaData.startNode = true;
			}
		}

		// clone edges from this automaton and add them to the clone
		const edges = this.edges;
		for(let i = 0; i < edges.length; i++){
			const id = edges[i].id + '.' + cloneId;
			const label = edges[i].label;
			const from = edges[i].from + '.' + cloneId;
			const to = edges[i].to + '.' + cloneId;
			const locations = JSON.parse(JSON.stringify(edges[i].locationSet));
			const metaData = JSON.parse(JSON.stringify(edges[i].metaData));
			const edge = new AutomatonEdge(id, label, from, to, locations, metaData);
			automaton.edgeMap[id] = edge;
			automaton.edgeCount++;
		}

		return automaton;

		function relabelSet(set){
			const newSet = {};
			for(let label in set){
				newSet[label + '.' + cloneId] = true;
			}

			return newSet;
		}
	},

	trim: function(){
		const visited = {};
		const fringe = [this.root];
		while(fringe.length !== 0){
			const current = fringe.pop();
			if(visited[current.id]){
				continue;
			}

			visited[current.id] = true;

			const neighbours = current.outgoingEdges.map(id => this.getNode(this.getEdge(id).to));
			for(let i = 0; i < neighbours.length; i++){
				const neighbour = neighbours[i];
				if(!visited[neighbour.id]){
					fringe.push(neighbour);
				}
			}

			if(neighbours.length === 0 && current.metaData.isTerminal === undefined){
			  current.metaData.isTerminal = 'stop';
      }
		}

		for(let id in this.nodeMap){
			if(!visited[id]){
				this.removeNode(id);
			}
		}
	},

	get nextNodeId(){
		return this.id + '.n' + this.nodeId++;
	},

	get nextEdgeId(){
		return this.id + '.e' + this.edgeId++;
	},

	// Global Functions

	convert: function(automaton){
		// check that the object has the correct properties
		const properties = ['type', 'id', 'rootId', 'nodeMap', 'edgeMap', 'metaData'];
		let match = true;
		for(let i = 0; i < properties.length; i++){
			if(!automaton.hasOwnProperty(properties[i])){
				match = false;
				break;
			}
		}

		if(match){
			for(let id in automaton.nodeMap){
				const node = automaton.nodeMap[id];
				const nodeProperties = ['id', 'incomingEdgeSet', 'outgoingEdgeSet', 'locationSet', 'metaData'];
				let nodeMatch = true;
				for(let i = 0; i < nodeProperties.length; i++){
					if(!node.hasOwnProperty(nodeProperties[i])){
						nodeMatch = false;
						break;
					}
				}

				if(!nodeMatch){
					// throw error
				}

				Object.setPrototypeOf(node, AUTOMATON_NODE);
				automaton.nodeMap[id] = node;
			}

			for(let id in automaton.edgeMap){
				const edge = automaton.edgeMap[id];
				const edgeProperties = ['id', 'label', 'from', 'to', 'locationSet', 'metaData'];
				let edgeMatch = true;
				for(let i = 0; i < edgeProperties.length; i++){
					if(!edge.hasOwnProperty(edgeProperties[i])){
						edgeMatch = false;
						break;
					}
				}

				if(!edgeMatch){
					// throw error
				}

				Object.setPrototypeOf(edge, AUTOMATON_EDGE);
				automaton.edgeMap[id] = edge;
			}

			Object.setPrototypeOf(automaton, AUTOMATON);
			return automaton;
		}
	}
};

function Automaton(id){
	this.type = 'automata';
	this.id = id;
	this.nodeMap = {};
	this.nodeCount = 0;
	this.edgeMap = {};
	this.edgeCount = 0;
	this.nodeId = 0;
	this.edgeId = 0;
	this.metaData = {};
	Object.setPrototypeOf(this, AUTOMATON);
}

const AUTOMATON_NODE = {
	get type(){
		return 'node';
	},

	get id(){
		return id;
	},

	get incomingEdges(){
		const incoming = [];
		for(let id in this.incomingEdgeSet){
			incoming.push(id);
		}

		return incoming;
	},

	addIncomingEdge: function(id){
		this.incomingEdgeSet[id] = true;
	},

	removeIncomingEdge: function(id){
		delete this.incomingEdgeSet[id];
	},

	get outgoingEdges(){
		const outgoing = [];
		for(let id in this.outgoingEdgeSet){
			outgoing.push(id);
		}

		return outgoing;
	},

	addOutgoingEdge: function(id){
		this.outgoingEdgeSet[id] = true;
	},

	removeOutgoingEdge: function(id){
		delete this.outgoingEdgeSet[id];
	},

	get locations(){
		return JSON.parse(JSON.stringify(this.locationSet));
	},

	set locations(locations){
		this.locationSet = {};
		for(let id in locations){
			this.locationSet[id] = true;
		}
	},

	addLocation: function(location){
		this.locationSet[location] = true;
	},

	removeLocation: function(location){
		delete this.locationSet[location];
	},

	isTerminal: function(){
		for(let id in this.outgoingEdgeSet){
			return false;
		}

		return true;
	},

	isUnreachable: function(){
		for(let id in this.incomingEdgeSet){
			return false;
		}

		return true;
	},

	addMetaData: function(key, data){
		this.metaData[key] = data;
	},

	getMetaData: function(key){
		return this.metaData[key];
	},

	deleteMetaData: function(key){
		delete this.metaData[key];
	}
};

function AutomatonNode(id, incomingEdges, outgoingEdges, locations, metaData){
	this.id = id;
	this.incomingEdgeSet = (incomingEdges === undefined) ? {} : incomingEdges;
	this.outgoingEdgeSet = (outgoingEdges === undefined) ? {} : outgoingEdges;
	this.locationSet = (locations === undefined) ? {} : locations;
	this.metaData = (metaData === undefined) ? {} : metaData;
	Object.setPrototypeOf(this, AUTOMATON_NODE);
}

const AUTOMATON_EDGE = {
	get type(){
		return 'edge';
	},

	get id(){
		return this.id;
	},

	get label(){
		return this.label;
	},

	set label(label){
		this.label = label;
	},

	get incomingNode(){
		return this.from;
	},

	get outgoingNode(){
		return this.to;
	},

	get locations(){
		return JSON.parse(JSON.stringify(this.locationSet));
	},

	set locations(locations){
		this.locationSet = {};
		for(let id in locations){
			this.locationSet[id] = true;
		}
	},

	addLocation: function(location){
		this.locationSet[location] = true;
	},

	removeLocation: function(location){
		delete this.locationSet[location];
	},

	isHidden: function(){
		return this.label === TAU;
	},

	isDeadlocked: function(){
		return this.label === DELTA;
	},

	addMetaData: function(key, data){
		this.metaData[key] = data;
	},

	getMetaData: function(key){
		return this.metaData[key];
	},

	deleteMetaData: function(key){
		delete this.metaData[key];
	}
};

function AutomatonEdge(id, label, from, to, locations, metaData){
	this.id = id;
	this.label = label;
	this.from = from;
	this.to = to;
	this.locationSet = (locations === undefined) ? {} : locations;
	this.metaData = (metaData === undefined) ? {} : metaData;
	Object.setPrototypeOf(this, AUTOMATON_EDGE);
}
