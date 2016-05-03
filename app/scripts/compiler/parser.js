'use strict';

var index;

var constantsMap;

var processes;

var variableMap;

var variableCount;

var actionRanges;

function parse(tokens){

	reset();
	while(index < tokens.length){
		var token = tokens[index];
		if(token.value === 'const'){
			parseConstDefinition(tokens);
		}
		else if(token.value === 'range'){
			parseRangeDefinition(tokens);
		}
		else if(token.value === 'set'){
			parseSetDefinition(tokens);
		}
		else if(token.type === 'identifier'){
			parseProcessDefinition(tokens);
		}
		else{
			throw new ParserException('Expecting to parse a either a constant or process definition, received the ' + token.type + '\' ' + token.value + '\'');
		}
	}

	return { processes:processes, constantsMap:constantsMap, variableMap:variableMap };

	function parseIdentifier(tokens){
		if(tokens[index].type === 'identifier'){
			return parseValue(tokens[index]);
		}
		var token = tokens[index];
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

		index++;
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
	function parseActionLabel(tokens){
		var action = '';

		while(index < tokens.length){
			if(tokens[index].type === 'action'){
				action += parseValue(tokens[index]);
			}
			else if(tokens[index].value === '['){
				// can either parse an expression or an action range at this point
				gobble(tokens[index], '[');
				var functions = [parseActionRange, parseExpression];
				action += '[' + parseMultiple(tokens, functions) + ']';
				gobble(tokens[index], ']');
			}
			else{
				var token = tokens[index];
				throw new ParserException('Received unexpected ' + token.type + '\' ' + token.value + '\' while attempting to parse an action label');
			}

			if(tokens[index].value === '.'){
				action += parseValue(tokens[index]);
			}
			else if(tokens[index].value != '[' && tokens[index].type !== 'action'){
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
	function parseActionRange(tokens){
		var variable = '';
		if(tokens[index].type === 'action'){
			variable = '$' + parseValue(tokens[index]);
			gobble(tokens[index], ':');
		}
		else{
			variable = generateVariableName();
		}

		// attempt to parse identifier, range or set
		var range;
		// check that identifier is not part of a range definition
		if(tokens[index].type === 'identifier' && tokens[index + 1].value !== '..'){
			var ident = parseIdentifier(tokens);
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
		else if(tokens[index].value !== '{'){
			range = parseRange(tokens);
		}
		else{
			range = parseSet(tokens);
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
	function parseRange(tokens){
		var start = parseExpression(tokens);
		gobble(tokens[index], '..');
		var end = parseExpression(tokens);

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
	function parseSet(tokens){
		gobble(tokens[index], '{');
		var currentRanges = actionRanges.length;
		var set = [];
		
		// parse all the elements within the set
		while(index < tokens.length){
			var action = parseActionLabel(tokens).action;
			
			// check if a range has been parsed
			if(currentRanges < actionRanges.length){
				processIndexedElement(currentRanges, {});
				actionRanges = actionRanges.slice(0, currentRanges);
			}
			else{
				set.push(action);
			}

			// check if there are anymore elements to parse
			if(tokens[index].value !== ','){
				break;
			}

			gobble(tokens[index], ',');
		}

		gobble(tokens[index], '}');

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
	function parseAssignment(tokens, type){
		// ensure that the correct type is parsed
	 	if(type !== undefined){
	 		gobble(tokens[index], type);
		}

		var ident = parseIdentifier(tokens);
		gobble(tokens[index], '=');

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
	function parseConstDefinition(tokens){
		var ident = parseAssignment(tokens, 'const');
		var value = parseSimpleExpression(tokens).expr;
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
	function parseRangeDefinition(tokens){
		var ident = parseAssignment(tokens, 'range');
		checkValidIdentifier(ident);

		// check if this range is referencing another range
		var start;
		var end;
		if(tokens[index].type === 'identifier' && constantsMap[tokens[index].value].type === 'range'){
			var reference = parseValue(tokens[index]);
			var constant = constantsMap[reference];
			start = constant.start;
			end = constant.end;
		}
		else{
			start = parseSimpleExpression(tokens).expr;
			gobble(tokens[index], '..');
			end = parseSimpleExpression(tokens).expr;
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
	function parseSetDefinition(tokens){
		var ident = parseAssignment(tokens, 'set');
		checkValidIdentifier(ident);
		
		// check if this set is referencing another set
		var set;
		if(tokens[index].type === 'identifier'){
			var reference = parseValue(tokens[index]);
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
			set = parseSet(tokens);			
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
	 * PROCESS_DEFINITION := IDENTIFIER '=' LOCAL_PROCESS (',' LOCAL_PROCESS_DEFINITION)* [RELABEL] [HIDING] '.'
	 *
	 * @param {token[]} tokens - the array of tokens to parse
	 */
	function parseProcessDefinition(tokens){
		var ident = parseAssignment(tokens);
		var process = parseComposite(tokens);

		// check if any local processes have been defined
		var localProcesses = [];
		while(tokens[index].value === ','){
			var localIdent = parseIdentifier(tokens[index]);

			// check if any ranges have been defined
			var ranges;
			if(tokens[index].value === '['){
				ranges = parseRanges(tokens);
			}

			var process = parseComposite(tokens);
			var localDefinition = { type:'process', ident:localIdent, ranges:ranges, process:process };
			localProcesses.push(localDefinition);
		}

		// check if a relabelling set has been defined
		var relabel;
		if(tokens[index].value === '/'){
			relabel = parseRelabel(tokens);
		}

		// check if a hidden set has been defined
		var hiding;
		if(tokens[index].value === '\\' | tokens[index] === '@'){
			hiding = parseHiding(tokens);
		}

		gobble(tokens[index], '.');

		// construct index nodes for any ranges defined
		while(actionRanges.length !== 0){
			var range = actionRanges.pop();
			range.process = process;
			process = range;
		}

		var definition = { type:'process', ident:ident, process:process, local:localProcesses };
		processes.push(definition);
	}

	function parseComposite(tokens){
		// check if a label has been defined
		var label = parseMultiple(tokens, [parseLabel]);

		var process = parseLocalProcess(tokens);

		// check if a relabelling set has been defined
		var relabel;
		if(tokens[index].value === '/'){
			relabel = parseRelabel(tokens);
		}

		// add label and relabel to process if necessary
		if(label !== undefined && label.type === 'action-label'){
			process.label = label;
		}
		if(relabel !== undefined){
			process.relabel = relabel;
		}

		// check if a composition can be parsed
		if(tokens[index].value === '||'){
			gobble(tokens[index], '||');
			process =  { type:'composite', process1:process, process2:parseComposite(tokens) };
		}

		return process;
	}

	/**
	 * LOCAL_PROCESS := '(' LOCAL_PROCESS ')' | BASE_LOCAL_PROCESS | IF_STATEMENT | FUNCTION | COMPOSITE | CHOICE
	 */
	function parseLocalProcess(tokens){
		var process;
		if(tokens[index].value === '('){
			gobble(tokens[index], '(');
			process = parseLocalProcess(tokens);
			gobble(tokens[index], ')');
		}
		else{
			var functions = [parseBaseLocalProcess, parseChoice];
			process = parseMultiple(tokens, functions);
		}

		if(tokens[index].value === '|'){
			gobble(tokens[index], '|');
			return { type:'choice', process1:process, process2:parseLocalProcess(tokens) };
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
	function parseBaseLocalProcess(tokens){
		if(tokens[index].type === 'terminal'){
				var terminal = parseValue(tokens[index]);
				return { type:'terminal', terminal:terminal };
		}
		else if(tokens[index].type === 'identifier'){
			var ident = parseIdentifier(tokens);

			// check if any indices have been declared
			if(tokens[index].value === '['){
				var indices = parseIndices(tokens);
				ident.indices = indices;
				return ident;
			}

			return ident;
		}
		else if(tokens[index].value === 'if'){
			return parseIfStatement(tokens);
		}
		else if(tokens[index].value === 'abs' || tokens[index].value === 'simp'){
			return parseFunction(tokens);
		}
		else if(tokens[index].value === '('){
			gobble(tokens[index], '(');
			var process = parseLocalProcess(tokens);
			gobble(tokens[index], ')');

			return process;
		}
		else{
			throw new ParserException('Expecting to parse a base local process, received the ' + tokens[index].type + '\'' + tokens[index].value + '\'');
		}
	}

	/**
	 * Attempts to parse and return a terminal from the specified array
	 * of tokens starting at the current index position. A terminal is of
	 * the form:
	 *
	 * TERMINAL := 'STOP' | 'ERROR'
	 */
	function parseTerminal(tokens){
		// check that a terminal node is next
		if(tokens[index].type !== 'terminal'){
			var token = tokens[index];
			throw new ParserException('Expecting to parse a terminal, received the ' + token.type + '\'' + token.value + '\'');
		}

		return parseValue(tokens[index]);
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
		gobble(tokens[index], 'if');
		var guard = parseExpression(tokens);
		gobble(tokens[index], 'then');
		var trueBranch = parseLocalProcess(tokens);

		// check if a false branch has been specified
		if(tokens[index].value === 'else'){
			gobble(tokens[index], 'else');
			var falseBranch = parseLocalProcess(tokens);
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
	function parseFunction(tokens){
		var func = parseFunctionType(tokens);
		gobble(tokens[index], '(');
		var process = parseLocalProcess(tokens);
		gobble(tokens[index], ')');

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
	function parseFunctionType(tokens){
		// check that function token is next
		if(tokens[index].type !== 'function'){
			var token = tokens[index];
			throw new ParserException('Expecting to parse a function type, received the ' + token.type + '\'' + token.value + '\'');
		}

		return parseValue(tokens[index]);
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
	function parseChoice(tokens){
		var process1 = parseSequence(tokens);

		// check if there is a choice available
		if(tokens[index].value === '|'){
			gobble(tokens[index], '|');
			var process2 = parseSequence(tokens);

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
	function parseSequence(tokens){
		var functions = [parseActionLabel, parseBaseLocalProcess];
		var from = parseMultiple(tokens, functions);
		// finish now if the parsed process is a base local process
		if(from.type != 'action-label'){
			return from;
		}

		gobble(tokens[index], '->');
		var to = parseSequence(tokens);

		return { type:'sequence', from:from, to:to };
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
	function parseIndices(tokens){
		var indices = [];
		do{
			gobble(tokens[index], '[');
			var expr = parseExpression(tokens);
			gobble(tokens[index], ']');

			indices.push(expr);
		}while(tokens[index] === '[');

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
	function parseRanges(tokens){
		var ranges = [];
		do{
			gobble(tokens[index], '[');
			var range = parseActionRange(tokens);
			gobble(tokens[index], ']');

			range.push(range);
		}while(tokens[index].value === '[');

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
	function parseLabel(tokens){
		var label = parseActionLabel(tokens);
		// check that a colon is next
		if(tokens[index].value !== ':'){
			throw new ParserException('Expecting to parse \':\', recieved ' + tokens[index].value);
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
	function parseRelabel(tokens){
		gobble(tokens[index], '/');
		gobble(tokens[index], '{');

		var relabels = []

		while(index < tokens.length){
			var relabel = parseRelabelElement(tokens);

			// check if relabelling set is completed
			if(tokens[index] !== ','){
				break;
			}

			gobble(tokens[index], ',');
		}

		gobble(tokens[index], '}');

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
	function parseRelabelElement(tokens){
		var newLabel = parseActionLabel(tokens);
		gobble(tokens[index], '/');
		var oldLabel = parseActionLabel(tokens);

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
	function parseHiding(tokens){
		var type;
		if(tokens[index].value === '\\'){
			type = 'incudes';
			gobble(tokens[index], '\\');
		}
		else if(tokens[index].value === '@'){
			type = 'excludes';
			gobble(tokens[index], '@');
		}
		else{
			var token = tokens[index];
			throw new ParserException('Received unexpected ' + token.type + '\'' + token.value + '\' while attempting to parse a hiding set');
		}

		var set = parseSet(tokens[index]).set;

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
	function parseExpression(tokens){
		var expr;
		// check if this is an unary expression
		if(tokens[index].type === 'operator'){
			var operator = tokens[index++];
			expr = processUnaryOperator(operator, parseBaseExpression(tokens));
		}
		else{
			expr = parseBaseExpression(tokens);
		}

		if(tokens[index].type === 'operator'){
			expr += parseOperator(tokens);
			expr += parseExpression(tokens);
			
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
	function parseBaseExpression(tokens){
			if(tokens[index].type === 'integer'){
				return parseValue(tokens[index]);
			}
			else if(tokens[index].type === 'action'){
				return '$' + parseValue(tokens[index]);
			}
			else if(tokens[index].type === 'identifier'){
				var ident = parseIdentifier(tokens);

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
			else if(tokens[index].value = '('){
				gobble(tokens[index], '(');
				var expr = parseExpression(tokens);
				gobble(tokens[index], ')');

				return expr.expr;
			}
			else{
				var token = tokens[index];
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
	function parseSimpleExpression(tokens){
		var expr;
		// check if this is an unary expression
		if(tokens[index].type === 'operator'){
			var operator = tokens[index++];
			expr = processUnaryOperator(operator, parseBaseSimpleExpression(tokens));
		}
		else{
			expr = parseBaseSimpleExpression(tokens);
		}

		if(index < tokens.length && tokens[index].type === 'operator'){
			expr += parseSimpleOperator(tokens);
			expr += parseSimpleExpression(tokens);
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
	function parseBaseSimpleExpression(tokens){
		if(tokens[index].type === 'integer'){
			return parseValue(tokens[index]);
		}
		else if(tokens[index].type === 'identifier'){
			var ident = parseIdentifier(tokens);

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
		else if(tokens[index].value = '('){
			gobble(tokens[index], '(');
			var expr = parseSimpleExpression(tokens);
			gobble(tokens[index], ')');

			return expr.expr;
		}
		else{
			var token = tokens[index];
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
	function parseOperator(tokens){
		// check that current token is an operator
		if(tokens[index].type !== 'operator'){
			throw new ParserException('Expecting to parse an operator, received the ' + token.type + '\' ' + token.value + '\'');
		}

		switch(tokens[index].value){
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
				return parseValue(tokens[index]);
			default:
				return parseSimpleOperator(tokens);
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
		if(tokens[index].type !== 'operator'){
			var token = tokens[index];
			throw new ParserException('Expecting to parse an operator, received the ' + token.type + '\' ' + token.value + '\'');
		}

		switch(tokens[index].value){
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				return parseValue(tokens[index]);
			default:
				var token = tokens[index];
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
	 * Parses and returns the value from the speicfied token. Increments
	 * the current position in the tokens array.
	 *
	 * @param {token} token - the token to parse a value from
	 */
	function parseValue(token){
		index++;
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
	function parseMultiple(tokens, functions){
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
	 			return functions[i](tokens);
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