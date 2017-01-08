'use strict';

const Parser = {
	index: 0,
	constantsMap: {},
	processes: [],
	operations: [],
	variableMap: {},
	variableCount: 0,
	actionRanges: [],

	parse: function(tokens){
		this.reset();

		while(this.index < tokens.length){
			const token = tokens[this.index];
			switch(token.value){
				case 'automata':
				case 'petrinet':
					this.parseProcessDefinition(tokens);
					break;
				case 'const':
					this.parseConstDefinition(tokens);
					break;
				case 'range':
					this.parseRangeDefinition(tokens);
					break;
				case 'set':
					this.parseSetDefinition(tokens);
					break;
				case 'operation':
					this.parseOperation(tokens);
					break;
				case 'end of file':
					// ignore this token
					this.index++;
					break;
				default:
					const message = 'Expecting to parse a process definition, constant or an operation but received the ' + token.type + ' \'' + token.value + '\'';
					throw new ParserException(message, token.location);
			}
		}

		return new AbstractSyntaxTree(this.processes, this.operations, this.variableMap);
	},

	parseIdentifier: function(tokens){
		const token = tokens[this.index];

		// ensure that the token is an identifier
		if(token.type !== 'identifier'){
			const message = 'Expecting to parse an identifier but received the ' + token.type + '\' ' + token.value + '\'';
			throw new ParserException(message, token.location);
		}

		// return the identifier
		return new IdentifierNode(this.parseValue(token), token.location);
	},

	parseActionLabel: function(tokens){
		let action = '';
		const start = tokens[this.index].location.start;

		while(this.index < tokens.length){
			let token = tokens[this.index];

			if(token.type === 'action'){
				action += this.parseValue(token);
			}
			else if(token.value === '['){
				// can either parse an expression or an action range at this point
				this.gobble(token, '[');
				action += '[' + this.parseMultiple(tokens, [this.parseActionRange, this.parseExpression]) + ']';
				this.gobble(tokens[this.index], ']');
			}
			else{
				// incorrect token found
				const message = 'Received unexpected ' + token.type + ' \'' + token.value + '\' while attempting to parse an action label';
				throw new ParserException(message, token.location);
			}

			// check if more action labels can be parsed
			token = tokens[this.index];

			if(token.value === '.'){
				action += this.parseValue(token);
			}
			else if(token.value !== '[' && token.type !== 'action'){
				// cannot parse anymore action labels
				break;
			}
		}

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		const actionLabel = new ActionLabelNode(action, location);

		// check if this action has been labelled as either a broadcaster or receiver
		if(tokens[this.index].value === '!'){
			this.parseValue(tokens[this.index]);
			actionLabel.broadcaster = true;
		}
		else if(tokens[this.index].value === '?'){
			this.parseValue(tokens[this.index]);
			actionLabel.receiver = true;
		}

		return actionLabel;
	},

	parseActionRange: function(tokens){
		const start = tokens[this.index].location.start;

		let variable = '';
		let token = tokens[this.index];
		if(token.type === 'action'){
			variable = '$' + this.parseValue(token);
			this.gobble(tokens[this.index], ':');
		}
		else{
			variable = this.generateVariableName();
		}

		// attempt to parse identifier, range or set
		let range;
		token = tokens[this.index];
		// check that the identifier is not part of a range definition
		if(token.type === 'identifier' && tokens[this.index + 1].value !== '..'){
			const ident = this.parseIdentifier(tokens);
			range = this.constantsMap[ident.ident];

			// check if the constant has been defined
			if(range === undefined){
				const message = 'The constant \'' + ident.ident + '\' has not been defined';
				throw new ParserException(message, ident.location);
			}

			// check that the constant is either a range or a set
			if(range.type === 'const'){
				const message = 'Expecting to parse a range or set identifier, received a const identifier';
				throw new ParserException(message, ident.location);
			}
		}
		// check if an inline range has been defined
		else if(token.value !== '{'){
			range = this.parseRange(tokens);
		}
		// otherwise an inline set should have been defined
		else{
			range = this.parseSet(tokens);
		}

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		this.actionRanges.push(new IndexNode(variable, range, location));
		return variable;
	},

	parseRange: function(tokens){
		const start = tokens[this.index].location.start;

		const rangeStart = this.parseExpression(tokens);
		this.gobble(tokens[this.index], '..');
		const rangeEnd = this.parseExpression(tokens);

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new RangeNode(rangeStart, rangeEnd, location);
	},

	parseSet: function(tokens){
		const start = tokens[this.index].location.start;

		if(tokens[this.index].type === 'identifier'){
			const ident = this.parseIdentifier(tokens);
			const constant = this.constantsMap[ident.ident];

			// check if constant has been defined
			if(constant === undefined){
				const message = 'The identifier \'' + ident.ident + '\' has not been defined';
				throw new ParserException(message, tokens[this.index - 1].location);
			}

			// check that the constant is a set
			if(constant.type !== 'set'){
				const message = 'Expecting a set but received the costant ' + constant.type;
				throw new ParserException(message, tokens[this.index - 1].location);
			}

			return constant
		}

		const currentRanges = this.actionRanges.length;
		let set = [];

		this.gobble(tokens[this.index], '{');
		while(this.index < tokens.length){
			const element = this.parseActionLabel(tokens).action;

			// check if a range has been parsed
			if(currentRanges < this.actionRanges.length){
				const variableMap = {};
				// have to bind function as it is defined within this function block
				const processedSet = processIndexedElement.bind(this)(currentRanges, element, variableMap);
				set = set.concat(processedSet);
			}
			else{
				set.push(element);
			}

			// check if there are anymore elements to parse
			if(tokens[this.index].value !== ','){
				break;
			}

			this.gobble(tokens[this.index], ',');
		}

		this.gobble(tokens[this.index], '}');

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new SetNode(set);

		function processIndexedElement(start, element, variableMap){
			let set = [];
			if(start === this.actionRanges.length){
				// have to bind function as it is defined within this function block
				const result = processVariables.bind(this)(element, variableMap);
				set.push(result);
			}
			else{
				const range = new IndexIterator(this.actionRanges[start].range);
				while(range.hasNext){
					variableMap[this.actionRanges[start].variable] = range.next;
					// have to bind function as it is defined within this function block
					const processedSet = processIndexedElement.bind(this)(start + 1, element, variableMap);
					set = set.concat(processedSet);
				}
			}

			return set;
		}

		function processVariables(element, variableMap){
			const regex = '[\$][v]*[a-zA-Z0-9]*';
			let result = element;
			let match = result.match(regex);

			while(match !== null){
				result = result.replace(match[0], variableMap[match[0]]);
				match = result.match(regex);
			}

			return result;
		}
	},

	parseConstDefinition: function(tokens){
		this.gobble(tokens[this.index], 'const');
		const ident = this.parseIdentifier(tokens);

		// ensure the the identifier has not already been defined
		if(this.isValidIdentifier(ident)){
			const message = 'The identifier \'' + ident + '\' has already been defined';
			const location = tokens[this.index - 1].location; // last token gobbled was identifier
			throw new ParserException(message, location);
		}

		this.gobble(tokens[this.index], '=');
		const value = this.parseSimpleExpression(tokens);
		this.constantsMap[ident.ident] = { type:'const', value:value };
	},

	parseRangeDefinition: function(tokens){
		this.gobble(tokens[this.index], 'range');
		const ident = this.parseIdentifier(tokens);

		// ensure the the identifier has not already been defined
		if(this.isValidIdentifier(ident)){
			const message = 'The identifier \'' + ident + '\' has already been defined';
			const location = tokens[this.index - 1].location; // last token gobbled was identifier
			throw new ParserException(message, location);
		}

		this.gobble(tokens[this.index], '=');
		const range = this.parseRange(tokens);
		this.constantsMap[ident.ident] = range;
	},

	parseSetDefinition: function(tokens){
		this.gobble(tokens[this.index], 'set');
		const ident = this.parseIdentifier(tokens);

		// ensure the the identifier has not already been defined
		if(this.isValidIdentifier(ident)){
			const message = 'The identifier \'' + ident + '\' has already been defined';
			const location = tokens[this.index - 1].location; // last token gobbled was identifier
			throw new ParserException(message, location);
		}

		this.gobble(tokens[this.index], '=');
		const set = this.parseSet(tokens);
		this.constantsMap[ident.ident] = set;
	},

	parseProcessDefinition: function(tokens){
		const processType = this.parseProcessType(tokens);
		const token = tokens[this.index];
		if(token.type === 'identifier'){
			this.parseSingleProcessDefinition(tokens, processType);
		}
		else if(token.value === '{'){
			this.parseProcessDefinitionBlock(tokens, processType);
		}
		else{
			const message = 'Expecting to parse a process definition or a process definition block but received the ' + token.type + ' \'' + token.value + '\'';
			throw new ParserException(message, token.location);
		}
	},

	parseProcessType: function(tokens){
		const token = tokens[this.index];

		// ensure that the current token is a process type
		if(token.type !== 'process-type'){
			const message = 'Expecting to parse a process type but received the ' + token.type + ' \'' + token.value + '\'';
			throw new ParserException(message, token.location);
		}

		return this.parseValue(token);
	},

	parseSingleProcessDefinition: function(tokens, processType){
		const start = tokens[this.index].location.start;

		const ident = this.parseIdentifier(tokens);
		this.gobble(tokens[this.index], '=');
		const process = this.parseComposite(tokens);

		// check if any local definitions have been defined
		const localProcesses = [];
		while(tokens[this.index].value === ','){
			this.gobble(tokens[this.index], ',');
			const localProcess = this.parseLocalProcessDefinition(tokens);
			localProcesses.push(localProcess);
		}

		// check if a hidden set has been defined
		let hiding;
		if(tokens[this.index].value === '\\' || tokens[this.index].value === '@'){
			hiding = this.parseHiding(tokens);
		}

		// check if a variables set has been defined
		let variables;
		if(tokens[this.index].value === '$'){
			variables = this.parseVariables(tokens);
		}

		// check if an interrupt has been defined
		let interrupt;
		if(tokens[this.index].value === '~>'){
			interrupt = this.parseInterrupt(tokens);
		}

		this.gobble(tokens[this.index], '.');

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		const definition = new ProcessNode(processType, ident, process, localProcesses, location);

		// add hiding set to definition if one was defined
		if(hiding !== undefined){
			definition.hiding = hiding;
		}

		// add variable set to definition if one was defined
		if(variables !== undefined){
			definition.variables = variables;
		}

		// add interrupt to definition if one was defined
		if(interrupt !== undefined){
			definition.interrupt = interrupt;
		}

		this.processes.push(definition);
	},

	parseProcessDefinitionBlock: function(tokens, processType){
		this.gobble(tokens[this.index], '{');

		// check that an empty block has not been specified
		if(tokens[this.index].value === '}'){
			const message = 'Cannot define an empty process definition block';
			const start = tokens[this.index - 2].location.start; // refers to the process type
			const end = tokens[this.index].location.end;
			const location = new Location(start, end);
			throw new ParserException(message, location);
		}

		// parse process definitions
		while(tokens[this.index].value !== '}'){
			this.parseSingleProcessDefinition(tokens, processType);
		}

		this.gobble(tokens[this.index], '}');
	},

	parseLocalProcessDefinition: function(tokens){
		const start = tokens[this.index].location.start;

		const ident = this.parseIdentifier(tokens);

		// check if any ranges have been defined
		let ranges;
		if(tokens[this.index].value === '['){
			ranges = this.parseRanges(tokens);
		}

		// add ranges to the identifier if necessary
		if(ranges !== undefined){
			ident.ranges = ranges;
		}

		this.gobble(tokens[this.index], '=');

		const process = this.parseComposite(tokens);

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new LocalProcessNode(ident, ranges, process, location);
	},

	parseComposite: function(tokens){
		const start = tokens[this.index].location.start;

		const label = this.parseMultiple(tokens, [this.parseLabel]);

		// parse an initial process
		let process = this.parseChoice(tokens);

		// check if a relabelling has been defined
		let relabel;
		if(tokens[this.index].value === '/'){
			relabel = this.parseRelabel(tokens);
		}

		// add label and relabel to process if necessary
		if(label !== undefined && label.type === 'action-label'){
			process.label = label;
		}
		if(relabel !== undefined){
			process.relabel = relabel;
		}

		// check if a composition can be parsed
		if(tokens[this.index].value === '||'){
			this.gobble(tokens[this.index], '||');
			const nextProcess = this.parseComposite(tokens);

			const end = tokens[this.index - 1].location.end;
			const location = new Location(start, end);

			process = new CompositeNode(process, nextProcess, location);
		}

		return process;
	},

	parseChoice: function(tokens){
		const start = tokens[this.index].location.start;

		let process = this.parseLocalProcess(tokens);

		// check if a choice can be parsed
		if(tokens[this.index].value === '|'){
			this.gobble(tokens[this.index], '|');
			const nextProcess = this.parseChoice(tokens);

			const end = tokens[this.index].location.end;
			const location = new Location(start, end);

			process = new ChoiceNode(process, nextProcess, location);
		}

		return process;
	},

	parseLocalProcess: function(tokens){
		// check if a parenthesised process has been defined
		const token = tokens[this.index];
		if(token.value === '('){
			this.gobble(token, '(');
			const process = this.parseComposite(tokens);
			this.gobble(tokens[this.index], ')');

			return process;
		}
		else if(this.isBaseLocalProcess(token)){
			return this.parseBaseLocalProcess(tokens);
		}
		else{
			return this.parseSequence(tokens);
		}
	},

	parseSequence: function(tokens){
		const start = tokens[this.index].location.start;

		const rangeStart = this.actionRanges.length;
		const from = this.parseActionLabel(tokens);
		const rangeEnd = this.actionRanges.length;

		// find the ranges that we defined when parsing the last action label
		const ranges = this.actionRanges.splice(rangeStart, rangeEnd - rangeStart);

		this.gobble(tokens[this.index], '->');

		const to = this.parseLocalProcess(tokens);

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		let process = new SequenceNode(from, to, location);

		// add the process to any ranges that were potentially parsed
		while(ranges.length !== 0){
			const next = ranges.pop();
			next.process = process;
			process = next;
		}

		return process;
	},

	parseBaseLocalProcess: function(tokens){
		const token = tokens[this.index];

		// check if a terminal can be parsed
		if(token.type === 'terminal'){
			return this.parseTerminal(tokens);
		}
		// check if an identifier can be parsed
		else if(token.type === 'identifier'){
			const ident = this.parseIdentifier(tokens);

			// check if any indices have been defined
			if(tokens[this.index].value === '['){
				const indices = this.parseIndices(tokens);
				ident.ident += indices;
			}

			return ident;
		}
		// check if an if statement can be parsed
		else if(token.value === 'if'){
			return this.parseIfStatement(tokens);
		}
		// check if a when statement can be parsed
		else if(token.value === 'when'){
			return this.parseWhenStatement(tokens);
		}
		// check if a function call can be parsed
		else if(token.type === 'function'){
			return this.parseFunction(tokens);
		}
		// check if a casting can be parsed
		else if(token.type === 'process-type'){
			return this.parseCasting(tokens);
		}
		// check if a forall statemnent can be parsed
		else if(token.value === 'forall'){
			return this.parseForAllStatement(tokens);
		}
		// check if a parenthesised local process can be parsed
		else if(token.value === '('){
			this.gobble(token, '(');
			const process = this.parseLocalProcess(tokens);
			this.gobble(tokens[this.index], ')');

			return process;
		}

		// was not able to parse a base local process
		const message = 'Expecting to parse a base local process but received the ' + token.type + ' \'' + token.value + '\'';
		throw new ParserException(message, token.location);

	},

	parseTerminal: function(tokens){
		const token = tokens[this.index];

		// check that the token is a terminal
		if(token.type !== 'terminal'){
			const message = 'Expecting to parse a terminal but received the ' + token.type + ' \'' + token.value;
			throw new ParserException(message, token.location);
		}

		// determine what terminal was parsed
		const terminal = this.parseValue(token);
		const node = new TerminalNode(terminal, token.location);
		switch(terminal){
			case 'STOP':
				return node;
			case 'ERROR':
				const from = new ActionLabelNode(delta);
				return new SequenceNode(from, terminal);
			default:
				const message = 'Invalid terminal \'' + terminal + '\' was parsed';
				throw new ParserException(message, token.location);
		}
	},

	parseIfStatement: function(tokens){
		const start = tokens[this.index].location.start;

		this.gobble(tokens[this.index], 'if');
		const condition = this.parseExpression(tokens);
    this.gobble(tokens[this.index], 'then');

		const trueBranch = this.parseLocalProcess(tokens);

		// check if a false branch has been defined
		if(tokens[this.index].value === 'else'){
			this.gobble(tokens[this.index], 'else');
			const falseBranch = this.parseLocalProcess(tokens);

			const end = tokens[this.index - 1].location.end;
			const location = new Location(start, end);

			return new IfStatementNode(condition, trueBranch, falseBranch, location);
		}

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new IfStatementNode(condition, trueBranch, undefined, location);
	},

	parseWhenStatement: function(tokens){
		const start = tokens[this.index].location.start;

		this.gobble(tokens[this.index], 'when');
		const condition = this.parseExpression(tokens);
		const trueBranch = this.parseLocalProcess(tokens);

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		// when statements are represented as if statements in the abstract syntax tree
		return new IfStatementNode(condition, trueBranch, undefined, location);
	},

	parseFunction: function(tokens){
		const start = tokens[this.index].location.start;

		const type = this.parseFunctionType(tokens);
		this.gobble(tokens[this.index], '(');
		const process = this.parseLocalProcess(tokens);
		this.gobble(tokens[this.index], ')');

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new FunctionNode(type, process, location);
	},

	parseCasting: function(tokens){
		const start = tokens[this.index].location.start;

		const processType = this.parseProcessType(tokens);
		this.gobble(tokens[this.index], '(');
		const process = this.parseLocalProcess(tokens);
		this.gobble(tokens[this.index], ')');

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new FunctionNode(processType, process, location);
	},

	parseFunctionType: function(tokens){
		const token = tokens[this.index];

		// check that a function has been defined
		if(token.type !== 'function'){
			const message = 'Expecting to parse a function but received the ' + token.type + ' \'' + token.value + '\'';
			throw new ParserException(message, token.location);
		}

		return this.parseValue(token);
	},

	parseForAllStatement: function(tokens){
		const start = tokens[this.index].location.start;

		this.gobble(tokens[this.index], 'forall');
		const ranges = this.parseRanges(tokens);
		const process = this.parseComposite(tokens);

		return new ForAllStatementNode(ranges, process);
	},

	parseIndices: function(tokens){
		let indices = '';

		do{
			this.gobble(tokens[this.index], '[');
			const expr = this.parseExpression(tokens);
			indices += '[' + expr + ']';
			this.gobble(tokens[this.index], ']');
		}while(tokens[this.index].value === '[');

		return indices;
	},

	parseRanges: function(tokens){
		const start = tokens[this.index].location.start;

		const rangeStart = this.actionRanges.length;

		do{
			this.gobble(tokens[this.index], '[');
			this.parseActionRange(tokens);
			this.gobble(tokens[this.index], ']');
		}while(tokens[this.index].value === '[');

		// get the parsed  ranges from action ranges
		const ranges = [];
		for(let i = rangeStart; i < this.actionRanges.length; i++){
			ranges.push(this.actionRanges[i]);
		}

		// remove the parsed action ranges
		this.actionRanges = this.actionRanges.slice(0, start);

		const end = tokens[this.index + 1].location.end;
		const location = new Location(start, end);

		return new RangesNode(ranges, location);
	},

	parseLabel: function(tokens){
		const label = this.parseActionLabel(tokens);
		this.gobble(tokens[this.index], ':');
		return label;
	},

	parseRelabel:function(tokens){
		const start = tokens[this.index].location.start;

		this.gobble(tokens[this.index], '/');
		this.gobble(tokens[this.index], '{');

		const relabels = [];

		while(this.index < tokens.length){
			const element = this.parseRelabelElement(tokens);
			relabels.push(element);

			// check if there are no more relabel elements to parse
			if(tokens[this.index].value !== ','){
				break;
			}

			this.gobble(tokens[this.index], ',');
		}

		this.gobble(tokens[this.index], '}');

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end)

		return new RelabelNode(relabels, location);
	},

	parseRelabelElement: function(tokens){
		const start = tokens[this.index].location.start;

		const newLabel = this.parseActionLabel(tokens);
		this.gobble(tokens[this.index], '/');
		const oldLabel = this.parseActionLabel(tokens);

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new RelabelElementNode(newLabel, oldLabel, location);
	},

	parseHiding: function(tokens){
		const token = tokens[this.index];
		const start = token.location.start;

		// determine what type of hiding set was defined
		let type;
		switch(token.value){
			case '\\':
				type = 'includes';
				this.gobble(token, '\\');
				break;
			case '@':
				type = 'excludes';
				this.gobble(token, '@');
				break;
			default:
				const message = 'Received unexpected ' + token.type + ' \'' + token.value + '\' while attempting to parse a hiding set';
				throw new ParserException(message, token.location);
		}

		const set = this.parseSet(tokens).set;

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new HidingNode(type, set, location);
	},

	parseVariables: function(tokens){
		this.gobble(tokens[this.index], '$');
		const set = this.parseSet(tokens);
		return set;
	},

	parseInterrupt: function(tokens){
		const start = tokens[this.index].location.start;

		this.gobble(tokens[this.index], '~>');
		const interrupt = this.parseActionLabel(tokens);
		this.gobble(tokens[this.index], '~>');
		const process = this.parseLocalProcess(tokens);

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		return new InterruptNode(interrupt, process, location);
	},

	parseOperation: function(tokens){
		this.gobble(tokens[this.index], 'operation');

		const token = tokens[this.index];
		if(token.value !== '{'){
			this.parseSingleOperation(tokens);
		}
		else if(token.value === '{'){
			this.parseOperationBlock(tokens);
		}
		else{
			const message = 'Expecting to parse an operation or an operation block but received the ' + token.type + ' \'' + token.value + '\'';
			throw new ParserException(message, token.location);
		}
	},

	parseSingleOperation: function(tokens){
		const start = tokens[this.index].location.start;

		const process1 = this.parseComposite(tokens);

		// check if the operation has been negated
		let isNegated = false;
		if(tokens[this.index].value === '!'){
			this.gobble(tokens[this.index], '!');
			isNegated = true;
		}

		const operator = this.parseOperationType(tokens);
		const process2 = this.parseComposite(tokens);
		this.gobble(tokens[this.index], '.');

		const end = tokens[this.index - 1].location.end;
		const location = new Location(start, end);

		const operation = new OperationNode(operator, isNegated, process1, process2, location);
		this.operations.push(operation);
	},

	parseOperationBlock: function(tokens){
		this.gobble(tokens[this.index], '{');

		// check that an empty block has not been specified
		if(tokens[this.index].value === '}'){
			const message = 'Cannot define an empty operation block';
			const start = tokens[this.index - 2].location.start; // refers to the operation token
			const end = tokens[this.index].location.end;
			const location = new Location(start, end);
			throw new ParserException(message, location);
		}

		// parse operations
		while(tokens[this.index].value !== '}'){
			this.parseSingleOperation(tokens);
		}

		this.gobble(tokens[this.index], '}');
	},

	parseOperationType: function(tokens){
		const token = tokens[this.index];

		// check that the current token is an operation operator
		if(token.type !== 'operation'){
			const message = 'Expecting to parse an operation operator but received the ' + token.type + ' \'' + token.value + '\'';
			throw new ParserException(message, token.location);
		}

		switch(token.value){
			case '~':
				this.gobble(token, '~');
				return 'bisimulation';
			default:
				const message = 'Recieved invalid operation operator \'' + token.value + '\'';
				throw new ParserException(message, token.location);
		}
	},

	parseExpression: function(tokens){
		let expr;

		// check if this is an unary expression
		let token = tokens[this.index];
		if(token.type === 'operator'){
			this.index++;
			expr = this.processUnaryOperator(token, this.parseBaseExpression(tokens));
		}
		else{
			expr = this.parseBaseExpression(tokens);
		}

		// check if an operator has been defined
		token = tokens[this.index];
		if(token.type === 'operator'){
			expr += this.parseOperator(tokens);
			expr += this.parseExpression(tokens);

			const variable = this.generateVariableName();
			this.variableMap[variable] = expr;
			return variable;
		}

		return expr;
	},

	parseBaseExpression: function(tokens){
		const token = tokens[this.index];
		if(token.type === 'integer'){
			return parseInt(this.parseValue(token));
		}
		else if(token.type === 'action'){
			return '$' + this.parseValue(token);
		}
		else if(token.type === 'identifier'){
			const ident = this.parseIdentifier(tokens);

			// check that the constant has been defined
			const constant = this.constantsMap[ident.ident];
			if(constant == undefined){
				const message = 'The constant \'' + ident.ident + '\' has not been defined';
				throw new ParserException(message, ident.location);
			}

			// check that the constant is an integer
			if(constant.type !== 'const'){
				const message = 'Expecting a constant of type const, received a constant of type ' + constant.type;
				throw new ParserException(message, ident.location);
			}

			return constant.value;
		}
		else if(token.value === '('){
			this.gobble(token, '(');
			const expr = this.parseExpression(tokens);
			this.gobble(tokens[this.index], ')');

			return expr;
		}
		else{
			const message = 'Unexpected ' + token.type + ' \'' + token.value + '\' found while attempting to parse an expression';
			throw new ParserException(message, token.location);
		}
	},

	parseSimpleExpression: function(tokens){
		let expr;

		// check if this is an unary expression
		let token = tokens[this.index];
		if(token.type === 'operator'){
			this.index++;
			expr = this.processUnaryOperator(token, this.parseBaseExpression(tokens));
		}
		else{
			expr = this.parseSimpleBaseExpression(tokens);
		}

		// check if an operator has been defined
		token = tokens[this.index];
		if(token.type === 'operator'){
			expr += this.parseOperator(tokens);
			expr += this.parseExpression(tokens);

			const variable = this.generateVariableName();
			this.variableMap[varaible] = expr;
			return variable;
		}

		return expr;
	},

	parseSimpleBaseExpression: function(tokens){
		const token = tokens[this.index];
		if(token.type === 'integer'){
			return parseInt(this.parseValue(token));
		}
		else if(token.type === 'action'){
			return '$' + this.parseValue(token);
		}
		else if(token.type === 'identifier'){
			const ident = this.parseIdentifier(tokens);

			// check that the constant has been defined
			const constant = this.constantsMap[ident.ident];
			if(constant == undefined){
				const message = 'The constant \'' + ident.ident + '\' has not been defined';
				throw new ParserException(message, ident.location);
			}

			// check that the constant is an integer
			if(constant.type !== 'const'){
				const message = 'Expecting a constant of type const, received a constant of type ' + constant.type;
				throw new ParserException(message, ident.location);
			}

			return constant.value;
		}
		else if(token.value === '('){
			this.gobble(token, '(');
			const expr = this.parseSimpleExpression(tokens);
			this.gobble(tokens[this.index], ')');

			return expr;
		}
		else{
			const message = 'Unexpected ' + token.type + ' \'' + token.value + '\' found while attempting to parse an expression';
			throw new ParserException(message, token.location);
		}
	},

	parseOperator: function(tokens){
		const token = tokens[this.index];
		// check that the current token is an operator
		if(token.type !== 'operator'){
			const message = 'Expecting to parse an operator but received the ' + token.type + ' \'' + token.value + '\'';
			throw new ParserException(message, token.location);
		}

		switch(token.value){
			case '||':
			case '&&':
			case '|':
			case '&':
			case '^':
			case '==':
			case '!=':
			case '<<':
			case '>>':
			case '<':
			case '<=':
			case '>':
			case '>=':
				return this.parseValue(token);
			default:
				return this.parseSimpleOperator(tokens);
		}
	},

	parseSimpleOperator: function(tokens){
		const token = tokens[this.index];
		// check that the current token is an operator
		if(token.type !== 'operator'){
			const message = 'Expecting to parse an operator but received the ' + token.type + ' \'' + token.value + '\'';
			throw new ParserException(message, token.location);
		}

		switch(token.value){
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				return this.parseValue(token);
			default:
				const message = 'Received an invalid operator \'' + token.value + '\'';
				throw new ParserException(message, token.location);
		}
	},

	processUnaryOperator: function(operator, value){
		switch(operator.value){
			case '+':
				return value;
			case '-':
				return 0 - value;
			case '!':
				return (value === 0) ? 1 : 0;
			default:
				const message = 'Expecting to parse an unary operator but received the operator \'' + operator.value + '\'';
				throw new ParserException(message, operator.location);
		}
	},

	getNextToken: function(tokens){
		if(this.index != tokens.length - 1){
			return tokens[this.index];
		}

		const message = 'End of file reached';
		const location = tokens[tokens.length - 1];
		throw new ParserException(message, location);
	},

	gobble: function(token, value){
		// ensure that the token value matches the expected value
		if(token.value !== value){
			const message = 'Expecting to parse \'' + value + '\' but received the ' + token.type + ' \'' + token.value + '\'';
			throw new ParserException(message, token.location);
		}

		// increment the index to point to the next token
		this.index++;
	},

	parseValue: function(token){
		// increment the index to point to the next token
		this.index++;
		// return the value
		return token.value;
	},

	parseMultiple: function(tokens, functions){
		const errors = [];
		let furtherestIndex = -1;
		let errorIndex = 0;
		let index = this.index;
		let variableCount = this.variableCount;
		const ranges = [];

		for(let i = 0; i < this.actionRanges.length; i++){
			ranges[i] = this.actionRanges[i];
		}

		// attempt to parse the next tokens using the specified functions
		for(let i = 0; i < functions.length; i++){
			try{
				// attempt to run the function
				const f = functions[i].bind(this);
				return f(tokens);
			}catch(error){
				// save the error
				errors.push(error);

				// check if the current function has been the most successful
				if(this.index > furtherestIndex){
					furtherestIndex = this.index;
					errorIndex = i;
				}

				// reseet variables
				this.index = index;
				this.variableCount = variableCount;
				this.actionRanges = ranges;
			}
		}

		// none of the functions managed to parse successfully
		// return the error for the function that was the most successful
		return errors[errorIndex];
	},

	isValidIdentifier: function(ident){
		return this.constantsMap[ident] !== undefined;
	},

	generateVariableName: function(){
		return '$v' + this.variableCount++;
	},

	isBaseLocalProcess: function(token){
		switch(token.value){
			case 'if':
			case 'when':
			case 'forall':
			case '(':
				return true;
		}

		switch(token.type){
			case 'identifier':
			case 'terminal':
			case 'function':
			case 'process-type':
				return true;
			default:
				return false;
		}
	},

	reset: function(){
		this.index = 0;
		this.constantsMap = {};
		this.processes = [];
		this.operations = [];
		this.variableMap = {};
		this.variableCount = 0;
		this.actionRanges = [];
	}
}

function AbstractSyntaxTree(processes, operations, variableMap){
	this.processes = processes;
	this.operations = operations;
	this.variableMap = variableMap;
}

function IdentifierNode(ident, location){
	this.type = 'identifier';
	this.ident = ident;
	this.location = location;
}

function ActionLabelNode(action, location){
	this.type = 'action-label';
	this.action = action;
	this.location = location;
}

function IndexNode(variable, range, location){
	this.type = 'index';
	this.variable = variable;
	this.range = range;
	this.location = location;
}

function RangeNode(start, end, location){
	this.type = 'range';
	this.start = start;
	this.end = end;
	this.location = location;
}

function SetNode(set){
	this.type = 'set';
	this.set = set;
}

function ProcessNode(processType, ident, process, localProcesses, location){
	this.type = 'process';
	this.processType = processType;
	this.ident = ident;
	this.process = process;
	this.local = localProcesses;
	this.location = location;
}

function LocalProcessNode(ident, ranges, process, location){
	this.type = 'process';
	this.ident = ident;
	this.ranges = ranges;
	this.process = process;
	this.location = location;
}

function CompositeNode(process1, process2, location){
	this.type = 'composite';
	this.process1 = process1;
	this.process2 = process2;
	this.location = location;
}

function ChoiceNode(process1, process2, location){
	this.type = 'choice';
	this.process1 = process1;
	this.process2 = process2;
	this.location = location;
}

function SequenceNode(from, to, location){
	this.type = 'sequence';
	this.from = from;
	this.to = to;
	this.location = location;
}

function TerminalNode(terminal, location){
	this.type = 'terminal';
	this.terminal = terminal;
	this.location = location;
}

function IfStatementNode(condition, trueBranch, falseBranch, location){
	this.type = 'if-statement';
	this.guard = condition;
	this.trueBranch = trueBranch;

	// check if a false branch was defined
	if(falseBranch !== undefined){
		this.falseBranch = falseBranch;
	}

	this.location = location;
}

function FunctionNode(func, process, location){
	this.type = 'function';
	this.func = func;
	this.process = process;
	this.location = location;
}

function ForAllStatementNode(ranges, process, location){
	this.type = 'forall';
	this.ranges = ranges;
	this.process = process;
	this.location = location;
}

function RangesNode(ranges, location){
	this.type = 'ranges';
	this.ranges = ranges;
	this.location;
}

function RelabelNode(relabelSet, location){
	this.type = 'relabel';
	this.set = relabelSet;
	this.location;
}

function RelabelElementNode(newLabel, oldLabel, location){
	this.newLabel = newLabel;
	this.oldLabel = oldLabel;
	this.location = location;
}

function HidingNode(type, hiding, location){
	this.type = type;
	this.set = hiding;
	this.location = location;
}

function InterruptNode(action, process, location){
	this.type = 'interrupt';
	this.action = action;
	this.process = process;
	this.location = location;
}

function OperationNode(operation, isNegated, process1, process2, location){
	this.type = 'operation';
	this.operation = operation;
	this.isNegated = isNegated;
	this.process1 = process1;
	this.process2 = process2;
	this. location = location;
}

function ParserException(message, location){
	this.message = message;
	this.location = location;
	this.toString = function(){
		return 'ParserException: ' + message + ' (' + location.start.line + ':' + location.start.col + ')';
	};
}
