'use strict';

var index;

var constantsMap;

var processes;

var variableMap;

var variableCount;

var actionRanges;

function parse(tokens){

	reset();
	var tokensIterator = constructTokensIterator();

	while(tokensIterator.hasNext()){
		var token = tokensIterator.peek();

		if(token.value === 'automata'){
			parseProcessDefinition(tokensIterator);
		}
		else if(token.value === 'petrinet'){
			parseProcessDefinition(tokensIterator);
		}
		else if(token.value === 'const'){
			parseConstDefinition(tokensIterator);
		}
		else if(token.value === 'range'){
			parseRangeDefinition(tokensIterator);
		}
		else if(token.value === 'set'){
			parseSetDefinition(tokensIterator);
		}
		else if(token.value === 'EOF'){
			// special case, do nothing
		}
		else{
			throw new ParserException('Expecting to parse a either a process definition or a constant, received the ' + token.type + '\' ' + token.value + '\'');
		}
	}

	return { processes:processes, constantsMap:constantsMap, variableMap:variableMap };

	function parseIdentifier(tokensIterator){
		var token = tokensIterator.next();
		if(token.type === 'identifier'){
			return parseValue(token);
		}

		throw new ParserException('Expecting to parse an identifier, received the ' + token.type + '\' ' + token.value + '\'');
	}

	/**
	 * Generates a variable name that can be used internally in places
	 * where the user has not defined a variable.
	 *
	 * @returns {string} - name of variable
	 */
	 function generateVariableName(){
	 	return '$<v' + variableCount++ +'>';
	 }

	/**
	 * Attempts to parse the specified value. Throws an error if the
	 * specified token's value does not match the specified value.
	 * Increments the index by one.
	 *
	 * @param {object} token - token to parse
	 * @param {string} value - value to parse from token
	 *
	 * @throws {ParserException} - if specifed value cannot be parsed
	 */
	function gobble(token, value){
		if(token.value !== value){
			throw new ParserException(
				'Expecting to parse \'' + value + '\' but received the ' + token.type + ' \'' + token.value + '\'.'
			);
		}
	}

	/**
	 * === ACTION LABELS ===
	 *
	 * Functions for parsing action labels and returning a node representing the
	 * parsed data for the abstract syntax tree. An action label is a label which
	 * represents the transition from one state to another. These labels can
	 * represent a single transition. Through the use of ranges and sets multiple
	 * transitions from a single node can be represented.
	 */

	/**
	 * Attempts to parse and return an action label from the specified array
	 * of tokens starting at the current index position. An action label is
	 * of the form:
	 *
	 * ACTION_LABEL := (LABEL | '[' (EXPR | ACTION_RANGE) ']') [['.'] ACTION_LABEL]
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - an action label node for the ast
	 */
	function parseActionLabel(tokensIterator){
		var action = '';

		while(tokensIterator.hasNext()){
			var token = tokensIterator.next();
			if(token.type === 'action'){
				action += parseValue(token);
			}
			else if(token.value === '['){
				// can either parse an expression or an action range at this point
				gobble(token, '[');
				var functions = [parseActionRange, parseExpression];
				action += '[' + parseMultiple(tokensIterator, functions) + ']';
				gobble(tokensIterator.next(), ']');
			}
			else{
				throw new ParserException(
					'Received unexpected ' + token.type + '\' ' + token.value + '\' while attempting to parse an action label',
					token.position
				);
			}

			token = tokensIterator.peek();
			if(token.value === '.'){
				action += parseValue(tokensIterator.next());
			}
			else if(token.value != '[' && token.type !== 'action'){
				// cannot parse anymore action labels
				break;
			}
		}

		return { type:'action-label', action: action };
	}

	/**
	 * Attempts to parse and return an action range from the specified array
	 * of tokens starting at the current index position. An action range is of
	 * the form:
	 *
	 * ACTION_RANGE := [VARIABLE ':'] (IDENTIFIER | RANGE | SET)
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - an action range node for the ast
	 */
	function parseActionRange(tokensIterator){
		var variable = '';
		if(tokensIterator.peek().type === 'action'){
			variable = '$' + parseValue(tokensIterator.next());
			gobble(tokensIterator.next(), ':');
		}
		else{
			variable = generateVariableName();
		}

		// attempt to parse identifier, range or set
		var token = tokensIterator.peek();
		var range;
		// check that identifier is not part of a range definition
		if(token.type === 'identifier' && tokens[index + 1].value !== '..'){
			var ident = parseIdentifier(tokensIterator);
			range = constantsMap[ident];
			
			// check if constant has been defined
			if(range === undefined){
				throw new ParserException('The constant \'' + ident + '\' has not been defined');
			}

			// check that the constant is either a range or set
			if(range.type === 'const'){
				throw new ParserException('Expecting to parse a range or set identifier, received a const identifier');
			}

		}
		else if(token.value !== '{'){
			range = parseRange(tokensIterator);
		}
		else{
			range = parseSet(tokensIterator);
		}

		actionRanges.push({ type:'index', variable:variable, range:range });
		
		return variable;
	}

	/**
	 * Attempts to parse and return a range from the specified array of
	 * tokens starting at the current index position. A range is of the form:
	 *
	 * RANGE := EXPR '..' EXPR
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - a range node for the ast
	 */
	function parseRange(tokensIterator){
		var start = parseExpression(tokensIterator);
		gobble(tokensIterator.next(), '..');
		var end = parseExpression(tokensIterator);

		return { type:'range', start:start, end: end};
	}

	/**
	 * Attempts to parse and return a set from the speocified array of tokens
	 * starting at the current index position. A set is of the form:
	 *
	 * SET := '{' ACTION_LABEL (',' ACTION_LABEL)* '}'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - a set node for the ast
	 */
	function parseSet(tokensIterator){
		gobble(tokensIterator.next(), '{');
		var currentRanges = actionRanges.length;
		var set = [];
		
		// parse all the elements within the set
		while(tokensIterator.hasNext()){
			var action = parseActionLabel(tokensIterator).action;
			
			// check if a range has been parsed
			if(currentRanges < actionRanges.length){
				processIndexedElement(currentRanges, {});
				actionRanges = actionRanges.slice(0, currentRanges);
			}
			else{
				set.push(action);
			}

			// check if there are anymore elements to parse
			if(tokensIterator.peek().value !== ','){
				break;
			}

			gobble(tokensIterator.next(), ',');
		}

		gobble(tokensIterator.next(), '}');

		return { type:'set', set:set };

		/**
		 * Helper function for parsing sets which handles action ranges declared within
		 * a set. Generates an element for each iteration of the action range and adds
		 * it to the set.
		 *
		 * @param {int} start - position in action ranges array where relevant ranges start
		 * @param {string -> int} variableMap - a mapping of variable names to values
		 */
		function processIndexedElement(start, variableMap){
			if(start === actionRanges.length){
				processVariables(variableMap);
			}
			else{
				var range = new IndexIterator(actionRanges[start]);
				while(range.hasNext()){
					variableMap[range.variable] = range.next();
					processIndexedElement(start + 1, variableMap);
				}
			}

		}

		/**
		 * Helper function for parsing action ranges within sets which replaces the variable 
		 * references within an action label with their value from the specified variable map.
		 *
		 * @param {string -> int} variableMap - a mapping of variable names to values
		 */
		function processVariables(variableMap){
			var regex = '[\$][v<]*[a-zA-Z0-9]*[>]*';
			var result = action;
			var match = result.match(regex);
			
			while(match !== null){
				result = result.replace(match[0], variableMap[match[0]]);
				match = result.match(regex);
			}

			set.push(result);
		}
	}

	/**
	 * === CONST, RANGE AND SET DEFINITIONS ===
	 *
	 * These are globally defined constants that can be referenced within process definitions.
	 * Consts can be referenced within other globally defined range and set definitions. These
	 * constants can only be referenced after they have been declared otherwise an error will be
	 * thrown. Successfully parsed constants are stored in the constants map.
	 */

	 /**
	  * Helper function for parsing definitions which attempts to parse
	  * the type (optional), identifier and assignment token. This is of the form:
	  *
	  * [TYPE] IDENTIFER '='
	  *
	  * @param {token[]} tokens - the array of tokens to parse
	  * @param {string|undefined} type - the type to be parsed (can be undefined)
	  * @return {string} - the parsed identifier
	  */
	function parseAssignment(tokensIterator, type){
		// ensure that the correct type is parsed
	 	if(type !== undefined){
	 		gobble(tokensIterator.next(), type);
		}

		var ident = parseIdentifier(tokensIterator);
		gobble(tokensIterator.next(), '=');

		return ident;
	}

	function checkValidIdentifier(identifier){
		if(constantsMap[identifier] !== undefined){
			throw new ParserException('The identififer \'' + identifier + '\' has already been defined');
		}
	}

	/**
	 * Attempts to parse a const definition from the specified array of tokens
	 * starting at the current index position. A const definition is of the form:
	 *
	 * CONST IDENTIFIER '=' SIMPLE_EXPR
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 */
	function parseConstDefinition(tokensIterator){
		var ident = parseAssignment(tokensIterator, 'const');
		var value = parseSimpleExpression(tokensIterator).expr;
		checkValidIdentifier(ident);

		constantsMap[ident] = { type:'const', value:value };
	}

	/**
	 * Attempts to parse a range definition from the specified array of tokens
	 * starting at the current index position. A range definition is of the form:
	 *
	 * RANGE_DEFINITION := 'range' IDENTIFIER '=' SIMPLE_EXPR '..' SIMPLE_EXPR
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 */
	function parseRangeDefinition(tokensIterator){
		var ident = parseAssignment(tokensIterator, 'range');
		checkValidIdentifier(ident);

		// check if this range is referencing another range
		var token = tokensIterator.peek();
		var start;
		var end;
		if(token.type === 'identifier' && constantsMap[token.value].type === 'range'){
			var reference = parseValue(tokensIterator.next());
			var constant = constantsMap[reference];
			start = constant.start;
			end = constant.end;
		}
		else{
			start = parseSimpleExpression(tokensIterator).expr;
			gobble(tokensIterator.next(), '..');
			end = parseSimpleExpression(tokensIterator).expr;
		}

		constantsMap[ident] = { type:'range', start:start, end:end };
	}

	/**
	 * Attempts to parse a set definition from the specified array of tokens
	 * starting at the current index position. A set definition is of the form:
	 *
	 * SET_DEFINITION := 'set' IDENTIFIER '=' SET
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 */
	function parseSetDefinition(tokensIterator){
		var ident = parseAssignment(tokensIterator, 'set');
		checkValidIdentifier(ident);
		
		// check if this set is referencing another set
		var set;
		if(tokensIterator.peek().type === 'identifier'){
			var reference = parseValue(tokensIterator.next());
			var constant = constantsMap[reference];

			//check if constant is defined
			if(constant === undefined){
				throw new ParserException('The constant \'' + reference + '\' has not been defined');
			}

			// check that constant is a set
			if(constant.type !== 'set'){
				throw new ParserException('Expecting to parse a set identifier, recieved a ' + constant.type + ' identifier');
			}

			set = constant;

		}
		else{
			set = parseSet(tokensIterator);			
		}

		constantsMap[ident] = set;
	}

	/**
	 * === PROCESS DEFINITION ===
	 */

	/**
	 * Attempts to parse a process definition from the specified array of tokens
	 * starting at the current index position. A process definition is of the
	 * form:
	 *
	 * PROCESS_DEFINITION := PROCESS_TYPE IDENTIFIER '=' LOCAL_PROCESS (',' LOCAL_PROCESS_DEFINITION)* [RELABEL] [HIDING] '.'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 */
	function parseProcessDefinition(tokensIterator){
		var processType = parseProcessType(tokensIterator);
		var ident = parseIdentifier(tokensIterator);
		gobble(tokensIterator.next(), '=');
		var process = parseComposite(tokensIterator);

		// check if any local processes have been defined
		var localProcesses = [];
		while(tokens[index].value === ','){
			var localIdent = parseIdentifier(tokensIterator.next());

			// check if any ranges have been defined
			var ranges;
			if(tokens[index].value === '['){
				ranges = parseRanges(tokensIterator);
			}

			var process = parseComposite(tokensIterator);
			var localDefinition = { type:'process', ident:localIdent, ranges:ranges, process:process };
			localProcesses.push(localDefinition);
		}

		// check if a relabelling set has been defined
		var relabel;
		if(tokensIterator.peek().value === '/'){
			relabel = parseRelabel(tokensIterator);
		}

		// check if a hidden set has been defined
		var hiding;
		var token = tokensIterator.peek();
		if(token.value === '\\' | token.value === '@'){
			hiding = parseHiding(tokensIterator);
		}

		gobble(tokensIterator.next(), '.');

		var definition = { type:'process', processType:processType, ident:ident, process:process, local:localProcesses };
		processes.push(definition);
	}

	/**
	 * Attempts to parse and return a process type from the specified array of tokens
	 * starting at the index position. A process type is of the form:
	 *
	 * PROCESS_TYPE := 'automata' | 'petrinet'
	 */
	function parseProcessType(tokensIterator){
		var token = tokensIterator.peek();
		if(token.value === 'automata'){
			return parseValue(tokensIterator.next());
		}
		else if(token.value === 'petrinet'){
			return parseValue(tokensIterator.next());
		}
		else{
			var type = token.type;
			var value = token.value;
			throw new ParserException('Expecting to parse a process type, received the ' + type + ' \'' + value + '\'');
		}
	}

	function parseComposite(tokensIterator){
		// check if a label has been defined
		var label = parseMultiple(tokensIterator, [parseLabel]);

		var process = parseLocalProcess(tokensIterator);

		// check if a relabelling set has been defined
		var relabel;
		if(tokensIterator.peek().value === '/'){
			relabel = parseRelabel(tokensIterator);
		}

		// add label and relabel to process if necessary
		if(label !== undefined && label.type === 'action-label'){
			process.label = label;
		}
		if(relabel !== undefined){
			process.relabel = relabel;
		}

		// check if a composition can be parsed
		if(tokensIterator.peek().value === '||'){
			gobble(tokensIterator.next(), '||');
			process =  { type:'composite', process1:process, process2:parseComposite(tokensIterator) };
		}

		return process;
	}

	/**
	 * LOCAL_PROCESS := '(' LOCAL_PROCESS ')' | BASE_LOCAL_PROCESS | IF_STATEMENT | FUNCTION | COMPOSITE | CHOICE
	 */
	function parseLocalProcess(tokensIterator){
		var process;
		if(tokensIterator.peek().value === '('){
			gobble(tokensIterator.next(), '(');
			process = parseComposite(tokensIterator);
			gobble(tokensIterator.next(), ')');
		}
		else{
			var functions = [parseBaseLocalProcess, parseChoice];
			process = parseMultiple(tokensIterator, functions);
		}

		if(tokensIterator.peek().value === '|'){
			gobble(tokensIterator.next(), '|');
			return { type:'choice', process1:process, process2:parseLocalProcess(tokensIterator) };
		}

		return process;
	}

	/**
	 * Attempts to parse and return a base local process from the specified array
	 * of tokens at the current index position. A base local process is of the
	 * form:
	 *
	 * BASE_LOCAL_PROCESS := TERMINAL | IDENTIFIER [INDICES]
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - either a terminal or identifier node for the ast
	 */
	function parseBaseLocalProcess(tokensIterator){
		var token = tokensIterator.peek();
		if(token.type === 'terminal'){
			var terminal = parseValue(tokensIterator.next());
			return { type:'terminal', terminal:terminal };
		}
		else if(token.type === 'identifier'){
			var ident = { type:'identifier', ident:parseIdentifier(tokensIterator) };

			// check if any indices have been declared
			if(token.value === '['){
				var indices = parseIndices(tokensIterator);
				ident.indices = indices;
				return ident;
			}

			return ident;
		}
		else if(token.value === 'if'){
			return parseIfStatement(tokensIterator);
		}
		else if(token.type === 'function'){
			return parseFunction(tokensIterator);
		}
		else if(token.value === '('){
			gobble(tokensIterator.next(), '(');
			var process = parseLocalProcess(tokensIterator);
			gobble(tokensIterator.next(), ')');

			return process;
		}
		else{
			throw new ParserException('Expecting to parse a base local process, received the ' + token.type + '\'' + token.value + '\'');
		}
	}

	/**
	 * Attempts to parse and return a terminal from the specified array
	 * of tokens starting at the current index position. A terminal is of
	 * the form:
	 *
	 * TERMINAL := 'STOP' | 'ERROR'
	 */
	function parseTerminal(tokensIterator){
		// check that a terminal node is next
		var token = tokensIterator.next();
		if(token.type !== 'terminal'){
			throw new ParserException('Expecting to parse a terminal, received the ' + token.type + '\'' + token.value + '\'');
		}

		return parseValue(token);
	}

	/**
	 * Attempts to parse and return an if statement process from the specified
	 * array of tokens starting at the current index position. An if statement
	 * process is of the form:
	 *
	 * IF_STATEMENT := 'if' EXPR 'then' LOCAL_PROCESS ['else' LOCAL_PROCESS]
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - an if statement node for the ast
	 */
	function parseIfStatement(tokens){
		gobble(tokensIterator.next(), 'if');
		var guard = parseExpression(tokensIterator);
		gobble(tokensIterator.next(), 'then');
		var trueBranch = parseLocalProcess(tokensIterator);

		// check if a false branch has been specified
		if(tokensIterator.peek().value === 'else'){
			gobble(tokensIterator.next(), 'else');
			var falseBranch = parseLocalProcess(tokensIterator);
			return { type:'if-statement', guard:guard, trueBranch:trueBranch, falseBranch:falseBranch };
		}

		return { type:'if-statment', guard:guard, trueBranch:trueBranch };
	}

	/**
	 * Attempts to parse and return a function process from the specified
	 * array of tokens starting at the current index position. A function
	 * process is of the form:
	 *
	 * FUNCTION := FUNCTION_TYPE '(' LOCAL_PROCESS ')'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} a function node for the ast
	 */
	function parseFunction(tokensIterator){
		var func = parseFunctionType(tokensIterator);
		gobble(tokensIterator.next(), '(');
		var process = parseLocalProcess(tokensIterator);
		gobble(tokensIterator.next(), ')');

		return { type:'function', func:func, process:process };
	}

	/**
	 * Attempts to parse and return a function type from the specified array
	 * of tokens starting at the current index position. A function type is of
	 * the form:
	 *
	 * FUNCTION_TYPE := 'abs' | 'simp'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {string} - the function type
	 */
	function parseFunctionType(tokensIterator){
		// check that function token is next
		var token = tokensIterator.next();
		if(token.type !== 'function'){
			throw new ParserException('Expecting to parse a function type, received the ' + token.type + '\'' + token.value + '\'');
		}

		return parseValue(token);
	}

	/**
	 * Attempts to parse and return a choice process from the specified
	 * array of tokens starting at the current index position. A choice process
	 * is of the form:
	 *
	 * CHOICE := SEQUENCE ('|' SEQUENCE)*
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - a choice node for the ast
	 */
	function parseChoice(tokensIterator){
		var process1 = parseSequence(tokensIterator);

		// check if there is a choice available
		if(tokensIterator.peek().value === '|'){
			gobble(tokensIterator.next(), '|');
			var process2 = parseSequence(tokensIterator);

			return { type:'choice', process1:process1, process2:process2 };
		}

		return process1;
	}

	/**
	 * Attempts to parse and return a sequnce from the specified array
	 * of tokens starting at the current index position. A sequence is of
	 * the form:
	 *
	 * SEQUENCE := (ACTION_LABEL | LOCAL_PROCESS) ('->' ACTION_LABEL _ LOCAL_PROCESS)* '->' BASE_LOCAL_PROCESS
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - a sequence node for the ast
	 */
	function parseSequence(tokensIterator){
		var functions = [parseActionLabel, parseBaseLocalProcess];
		var fromIndex = actionRanges.length;
		var from = parseMultiple(tokensIterator, functions);
		// finish now if the parsed process is a base local process
		if(from.type != 'action-label'){
			return processActionRanges(from, fromIndex);
		}

		gobble(tokensIterator.next(), '->');
		var toIndex = actionRanges.length;
		var to = parseSequence(tokensIterator);
		to = processActionRanges(to, toIndex);


		var sequence = { type:'sequence', from:from, to:to };
		return processActionRanges(sequence, fromIndex);
	}

	/**
	 * Attmepts to parse and return a sequence of indices from the specified
	 * array of tokens starting from the current index position. A sequence of
	 * indices is of the form:
	 *
	 * INDICES := ('[' EXPR ']')+
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - an indices node for the ast
	 */
	function parseIndices(tokensIterator){
		var indices = [];
		do{
			gobble(tokensIterator.next(), '[');
			var expr = parseExpression(tokensIterator);
			gobble(tokensIterator.next(), ']');

			indices.push(expr);
		}while(tokensIterator.peek().value === '[');

		return { type:'indices', indices:indices };
	}

	/**
	 * Attempts to parse and return a sequence of ranges from the specified array
	 * of tokens starting at the current index position. A sequence of ranges
	 * is of the form:
	 *
	 * RANGES := ('[' ACTION_RANGE ']')+
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - a ranges node for the ast
	 */
	function parseRanges(tokensIterator){
		var ranges = [];
		do{
			gobble(tokensIterator.next(), '[');
			var range = parseActionRange(tokensIterator);
			gobble(tokensIterator.next(), ']');

			range.push(range);
		}while(tokensIterator.peek().value === '[');

		return { type:'ranges', ranges:ranges };
	}

	/**
	 * Attempts to parse and return a label for a local process from the specified
	 * array of tokens starting at the current index position. A label is of the form:
	 *
	 * LABEL = ACTION_LABEL ':'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - an action label ast node
	 */
	function parseLabel(tokensIterator){
		var label = parseActionLabel(tokensIterator);
		// check that a colon is next
		var token = tokensIterator.next();
		if(token.value !== ':'){
			throw new ParserException('Expecting to parse \':\', recieved ' + token.value);
		}

		return label;
	}

	/**
	 * === RELABELLING AND HIDING ===
	 */

	/**
	 * Attempts to parse and return a set of action relabels from the specified
	 * array of tokens starting at the current index position. A set of action
	 * relabels is of the form:
	 *
	 * RELABEL := '/' '{' RELABEL_ELEMENT (',' RELABEL_ELEMENT)* '}'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - a relabel set node for the ast
	 */
	function parseRelabel(tokensIterator){
		gobble(tokensIterator.next(), '/');
		gobble(tokensIterator.next(), '{');

		var relabels = []

		while(tokensIterator.hasNext()){
			relabels.push(parseRelabelElement(tokensIterator));

			// check if relabelling set is completed
			if(tokensIterator.peek() !== ','){
				break;
			}

			gobble(tokensIterator.next(), ',');
		}

		gobble(tokensIterator.next(), '}');

		return { type:'relabel', set:relabels };
	}

	/**
	 * Attempts to parse and return an element of a relabel set from the
	 * specified array of tokens starting at the current index position. A
	 * relabel set element is of the form:
	 *
	 * RELABEL_ELEMENT := ACTION_LABEL '/' ACTION_LABEL
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - a relabel element node for the ast
	 */
	function parseRelabelElement(tokensIterator){
		var newLabel = parseActionLabel(tokensIterator);
		gobble(tokensIterator.next(), '/');
		var oldLabel = parseActionLabel(tokensIterator);

		return { newLabel:newLabel, oldLabel:oldLabel };
	}

	/**
	 * Attempts to parse and return a set of hidden action labels from the
	 * specified array of tokens starting at the current index position. A
	 * set of hidden action labels is of the form:
	 *
	 * HIDING := ('\' | '@') SET
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - a hidden set node for the ast
	 */
	function parseHiding(tokensIterator){
		var type;
		var token = tokensIterator.peek();
		if(token.value === '\\'){
			type = 'incudes';
			gobble(tokensIterator.next(), '\\');
		}
		else if(token.value === '@'){
			type = 'excludes';
			gobble(tokensIterator.next(), '@');
		}
		else{
			throw new ParserException('Received unexpected ' + token.type + '\'' + token.value + '\' while attempting to parse a hiding set');
		}

		var set = parseSet(tokensIterator).set;

		return { type:type, set:set };
	}

	/**
	 * === EXPRESSIONS ===
	 */

	/**
	 * Attempts to parse and return an expression from the specified array of
	 * tokens starting at the current index position. An expression is of the
	 * form:
	 *
	 * EXPR := BASE_EXPR (OPERATOR BASE_EXPR)*
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - an expression node for the ast
	 */
	function parseExpression(tokensIterator){
		var expr;
		// check if this is an unary expression
		if(tokensIterator.peek().type === 'operator'){
			var operator = tokensIterator.next();
			expr = processUnaryOperator(operator, parseBaseExpression(tokensIterator));
		}
		else{
			expr = parseBaseExpression(tokensIterator);
		}

		if(tokensIterator.peek().type === 'operator'){
			expr += parseOperator(tokensIterator);
			expr += parseExpression(tokensIterator);
			
			var variable = generateVariableName();
			variableMap[variable] = expr;
			return variable;
		}


		return expr;
	}

	/**
	 * Attempts to parse and return a base expression from the specified array of
	 * tokens starting at the current index position. A base expression is of the
	 * form:
	 *
	 * BASE_EXPR := INTEGER | VARIABLE | IDENTIFIER | '(' EXPR ')'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {string} - the base expression
	 */
	function parseBaseExpression(tokensIterator){
		var token = tokensIterator.peek();
		if(token.type === 'integer'){
			return parseValue(tokensIterator.next());
		}
		else if(token.type === 'action'){
			return '$' + parseValue(tokensIterator.next());
		}
		else if(token.type === 'identifier'){
			var ident = parseIdentifier(tokensIterator);

			// check if constant has been defined
			var constant = constantsMap[ident];
			if(constant === undefined){
				throw new ParserException('The constant \'' + ident + '\' has not been defined');
			}

			// check that constant is an integer
			if(constant.type !== 'const'){
				throw new ParserException('Expecting a constant of type const, received a constant of type ' + constant.type);
			}

			return constant.value;
		}
		else if(token.value = '('){
			gobble(tokensIterator.next(), '(');
			var expr = parseExpression(tokensIterator);
			gobble(tokensIterator.next(), ')');

			return expr.expr;
		}
		else{
			throw new ParserException('Invalid ' + token.type + '\'' + token.value + '\' found while attempting to parse a base expression');
		}
	}

	/**
	 * Attempts to parse and return a simple expression from the specified array of
	 * tokens starting at the current index position. A simple expression is of
	 * the form:
	 *
	 *
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {node} - an expression node for the ast
	 */
	function parseSimpleExpression(tokensIterator){
		var expr;
		// check if this is an unary expression
		if(tokensIterator.peek().type === 'operator'){
			var operator = tokensIterator.next();
			expr = processUnaryOperator(operator, parseBaseSimpleExpression(tokensIterator));
		}
		else{
			expr = parseBaseSimpleExpression(tokensIterator);
		}

		if(tokensIterator.peek().type === 'operator'){
			expr += parseSimpleOperator(tokensIterator);
			expr += parseSimpleExpression(tokensIterator);
		}

		return { type:'expression', expr:expr };
	}

	/**
	 * Attempts to parse and return a base simple expression from the specified array of
	 * tokens starting at the current index position. A base simple expression is of the
	 * form:
	 *
	 * BASE_SIMPLE_EXPR := INTEGER | IDENTIFIER | '(' SIMPLE_EXPR ')'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {string} - the base expression
	 */
	function parseBaseSimpleExpression(tokensIterator){
		var token = tokensIterator.peek();
		if(token.type === 'integer'){
			return parseValue(tokensIterator.next());
		}
		else if(token.type === 'identifier'){
			var ident = parseIdentifier(tokensIterator);

			// check if constant has been defined
			var constant = constantsMap[ident];
			if(constant === undefined){
				throw new ParserException('The constant \'' + ident + '\' has not been defined');
			}

			// check that constant is an integer
			if(constant.type !== 'const'){
				throw new ParserException('Expecting a constant of type const, received a constant of type ' + constant.type);
			}

			return constant.value;
		}
		else if(tokensIterator.peek().value = '('){
			gobble(tokensIterator.next(), '(');
			var expr = parseSimpleExpression(tokensIterator);
			gobble(tokensIterator.next(), ')');

			return expr.expr;
		}
		else{
			throw new ParserException('Invalid ' + token.type + '\'' + token.value + '\' found while attempting to parse a base expression');
		}
	}

	/**
	 * Attempts to parse and return an operator from the specified
	 * array of tokens starting at the current index position. An
	 * operator is of the form:
	 *
	 * OPERATOR := '||' | '&&' | '|' | '^' | '&' | '==' | '!=' | '<<' | '>>' | '<=' | '<' | '>=' | '>' | SIMPLE_OPERATOR
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @return {string} - the parsed operator
	 */
	function parseOperator(tokensIterator){
		// check that current token is an operator
		var token = tokensIterator.peek();
		if(token.type !== 'operator'){
			throw new ParserException('Expecting to parse an operator, received the ' + token.type + '\' ' + token.value + '\'');
		}

		switch(token.value){
			case '||':
			case '&&':
			case '|':
			case '^':
			case '|':
			case '==':
			case '!=':
			case '<<':
			case '>>':
			case '<=':
			case '<':
			case '>=':
			case '>':
				return parseValue(tokensIterator.next());
			default:
				return parseSimpleOperator(tokensIterator);
		}
	}

	/**
	 * Attempts to parse and return a simple operator from the specified
	 * array of tokens starting at the current index position. A simple
	 * operator is of the form:
	 *
	 * SIMPLE_OPERATOR := '+' | '-' | '*' | '/' | '%'
	 *
	 * @param {token[]} tokens - array of tokens to parse
	 * @return {string} - the parsed simple operator
	 */
	function parseSimpleOperator(tokens){
		// check that current token is an operator
		var token = tokensIterator.peek();
		if(token.type !== 'operator'){
			throw new ParserException('Expecting to parse an operator, received the ' + token.type + '\' ' + token.value + '\'');
		}

		switch(token.value){
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				return parseValue(tokensIterator.next());
			default:
				throw new ParseException('Received an invalid operator \'' + token.value + '\'');
		}

	}

	/**
	 * Processes and returns the specified value as an unary operation.
	 *
	 * @param {token} operator - the unary operator
	 * @param {int} value - the value to treat as an unary operation
	 * @return {int} - the unary operation
	 */
	function processUnaryOperator(operator, value){
		if(operator.value === '+'){
			return value;
		}
		else if(operator.value === '-'){
			return 0 - value;
		}
		else if(operator.value === '!'){
			return value === 0;
		}
	}

	/**
	 * === HELPER FUNCTIONS ===
	 */

	/**
	 * Resets the parser's fields to their initial state.
	 */
	function reset(){
		 index = 0;
		 constantsMap = {};
		 processes = [];
		 variableMap = {};
		 actionRanges = [];
		 variableCount = 0;
	}

	/**
	 * Parses and returns the value from the speicfied token.
	 *
	 * @param {token} token - the token to parse a value from
	 */
	function parseValue(token){
		return token.value;
	}

	/**
	 * Attempts to successfully parse one of the parsing options specified
	 * in the parsing functions array. Returns the ast node for the first
	 * function that is successfully parsed. If no function is successfully
	 * parsed then the ParserException from the function that was most successful
	 * is thrown.
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 * @param {function[]} functions - the array of parsing functions to attempt
	 */
	function parseMultiple(tokensIterator, functions){
		var errors = [];
		var start = index;
		var varCount = variableCount;
		var ranges = [];
		for(var i = 0; i < actionRanges.length; i++){
			ranges[i] = actionRanges[i];
		}
	 	
	 	// attempt to parse the specified functions
	 	for(var i = 0; i < functions.length; i++){
	 		try{
	 			// attempt function
	 			return functions[i](tokensIterator);
	 		}catch(error){
	 			// save error
	 			errors.push(error);
	 			// reset variables
	 			index = start;
	 			variableCount = varCount;
	 			actionRanges = ranges;
	 		}
	 	}

		// find which function got the furtherest
		var error = errors[0];
	 	for(var i = 1; i < errors.length; i++){
	 		if(errors[i].start.line > error.start.line){
	 			error = errors[i];
	 		}
	 		else if(errors[i].start.line == error.start.line && errors[i].start.column > error.start.column){
	 			error = errors[i];
	 		}
	 	}

	 	return error;
	}

	function processActionRanges(astNode, start){
		while(start < actionRanges.length){
			var range = actionRanges.pop();
			range.process = astNode;
			astNode = range;
		}

		return astNode;
	}

	/**
	 * Constructs and returns an iterator object for iterating over the tokens
	 * array. Skips over unnecessary tokens (comments) and returns a special
	 * EOF token when the end of file has been reached.
	 */
	function constructTokensIterator(){
		return {
			/**
			 * Returns true if there are still tokens to iterate over,
			 * otherwise returns false.
			 *
			 * @return {boolean} - whether there are any tokens left or not
			 */
			hasNext: function(){
				return index < tokens.length;
			},

			/**
			 * Returns the next token from the tokens array and increments the
			 * index to point to the next token. Throws a parser exception if the
			 * end of the file has been reached unexpectedly.
			 *
			 * @param {boolean} ignoreEOF - whether or not to ignore the eof
			 * @return {token} - the next token
			 */
			next: function(ignoreEOF){
				while(index < tokens.length){
					if(tokens[index].type !== 'comment'){
						return tokens[index++];
					}

					index++;
				}

				// throw an error if not expecting the eof
				if(ignoreEOF === false || ignoreEOF === undefined){
					throw new ParserException('EOF Reached')
				}

				// otherwise return an eof token
				return { type:'symbol', value:'EOF' };
			},

			/**
			 * Returns the next token from the tokens array but does not increment the
			 * index to point to the next token.
			 *
			 * @return {token} - the next token
			 */
			peek: function(){
				while(index < tokens.length){
					if(tokens[index].type !== 'comment'){
						return tokens[index];
					}

					index++;
				}

				// otherwise return an eof token
				return { type:'symbol', value:'EOF' };
			}
		};
	}

	/**
	 * Constructs and returns a 'ParserException' based off of the
	 * specified message. Also contains the location in the code being parsed
	 * where the error occured.
	 *
	 * @param {string} message - the cause of the exception
	 * @param {object} location - the location where the exception occured
	 */
	function ParserException(message, location){
		this.message = message;
		this.location = location;
		this.toString = function(){
			return 'ParserException: ' + message;
		};	
	}
}