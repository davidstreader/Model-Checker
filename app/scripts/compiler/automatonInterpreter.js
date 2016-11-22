'use strict';

function interpretAutomaton(process, processesMap, isFairAbstraction){
	const processStack = [];
	const referenceMap = {};

	const ident = process.ident.ident;
	const automaton = new Automaton(ident);
	const root = automaton.addNode();
	automaton.root = root.id;
	root.metaData.startNode = true;

	interpretNode(process.process, automaton, root)

	if(process.hiding !== undefined){
		processHiding(automaton, process.hiding);
	}

	labelAutomaton(automaton);
	processesMap[ident] = automaton;

	function interpretSubAutomaton(subProcess, automaton){
		// setup the sub automaton
		const subAutomaton = new Automaton(automaton.id);
		subAutomaton.nodeId = automaton.nodeId;
		subAutomaton.edgeId = automaton.edgeId;

		// setup the sub root
		const subRoot = subAutomaton.addNode();
		subAutomaton.root = subRoot.id;
		subRoot.metaData.startNode = true;

		// interpret the sub process
		interpretNode(subProcess, subAutomaton, subRoot);

		// update the main automaton
		automaton.nodeId = subAutomaton.nodeId;
		automaton.edgeId = subAutomaton.edgeId;

		processStack.push(subAutomaton);
	}

	function interpretNode(astNode, automaton, currentNode){
		processReferencePointer(astNode, currentNode);
		switch(astNode.type){
			case 'sequence':
				interpretSequence(astNode, automaton, currentNode);
				break;
			case 'choice':
				interpretChoice(astNode, automaton, currentNode);
				break;
			case 'composite':
				interpretComposite(astNode, automaton, currentNode);
				break;
			case 'function':
				interpretFunction(astNode, automaton, currentNode);
				break;
			case 'identifier':
				interpretIdentifier(astNode, automaton, currentNode);
				break;
			case 'terminal':
				currentNode.metaData.isTerminal = astNode.terminal;
				break;
			default:
				break;
		}

		if(astNode.label !== undefined){
			processLabelling(automaton, astNode.label.action);
		}

		if(astNode.relabel !== undefined){
			processRelabelling(automaton, astNode.relabel.set);
		}
	}

	function interpretSequence(astNode, automaton, currentNode){
		const next = (astNode.to.type === 'reference') ? referenceMap[astNode.to.reference] : automaton.addNode();
		const id = automaton.nextEdgeId;
		automaton.addEdge(id, astNode.from.action, currentNode, next);

		if(astNode.to.type !== 'reference'){
			interpretNode(astNode.to, automaton, next);
		}
	}

	function interpretChoice(astNode, automaton, currentNode){
		interpretNode(astNode.process1, automaton, currentNode);
		interpretNode(astNode.process2, automaton, currentNode);
	}

	function interpretComposite(astNode, automaton, currentNode){
		interpretSubAutomaton(astNode.process1, automaton);
		interpretSubAutomaton(astNode.process2, automaton);

		const automaton2 = processStack.pop();
		const automaton1 = processStack.pop();
		const composedAutomaton = parallelComposition(automaton.id + '.comp', automaton1, automaton2);

		const root = composedAutomaton.root;
		composedAutomaton.root = undefined;
		delete root.metaData.startNode;

		automaton.addAutomaton(composedAutomaton);
		automaton.combineNodes(currentNode, root);
	}

	function interpretFunction(astNode, automaton, currentNode){
		interpretSubAutomaton(astNode.process, automaton);

		let processedAutomaton = processStack.pop();
		switch(astNode.func){
			case 'abs':
				processedAutomaton = abstraction(processedAutomaton, isFairAbstraction);
				break;
			case 'simp':
				processedAutomaton = bisimulation(processedAutomaton);
			default:
				break;
		}

		const root = processedAutomaton.root;
		processedAutomaton.root = undefined;
		delete root.metaData.startNode;

		automaton.addAutomaton(processedAutomaton);
		automaton.combineNodes(currentNode, root);
	}

	function interpretIdentifier(astNode, automaton, currentNode){
		const reference = processesMap[astNode.ident].clone;
		const root = reference.root;
		reference.root = undefined;
		delete root.metaData.startNode;
		automaton.addAutomaton(reference);
		automaton.combineNodes(currentNode, root);
	}

	function processReferencePointer(astNode, currentNode){
		if(astNode.reference !== undefined){
			referenceMap[astNode.reference] = currentNode;
		}
	}

	function processHiding(automaton, hidingSet){
		const alphabet = automaton.alphabet;
		const set = {};

		for(let i = 0; i < hidingSet.set.length; i++){
			set[hidingSet.set[i]] = true;
		}

		for(let label in alphabet){
			if(set[label] !== undefined && hidingSet.type === 'includes'){
				automaton.relabelEdges(label, TAU);
			}
			else if(set[label] === undefined && hidingSet.type === 'excludes'){
				automaton.relabelEdges(label, TAU);
			}
		}
	}

	function processLabelling(automaton, label){
		const alphabet = automaton.alphabet;
		for(let action in alphabet){
			automaton.relabelEdges(action, label + '.' + action);
		}
	}

	function processRelabelling(automaton, relabelSet){
		for(let i = 0; i < relabelSet.length; i++){
			const newLabel = relabelSet[i].newLabel.action;
			const oldLabel = relabelSet[i].oldLabel.action;
			automaton.relabelEdges(oldLabel, newLabel);
		}
	}

	function labelAutomaton(automaton){
		let label = 0;
		const visited = {};
		const fringe = [automaton.root];
		let index = 0;
		while(index < fringe.length){
			const current = fringe[index++];
			if(visited[current.id]){
				continue;
			}

			visited[current.id] = true;
			current.metaData.label = label++;

			const neighbours = current.outgoingEdges.map(id => automaton.getEdge(id)).map(e => automaton.getNode(e.to));
			for(let i = 0; i < neighbours.length; i++){
				const neighbour = neighbours[i];
				if(!visited[neighbour.id]){
					fringe.push(neighbour);
				}
			}
		}
	}
}