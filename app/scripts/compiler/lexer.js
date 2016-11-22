'use strict';

const Lexer = {
	actionLabel: '[a-z][A-Za-z0-9_]*',
	identifier: '[A-Z][A-Za-z0-9_]*',
	integer: '[1-9][0-9]*',
	processTypes: {'automata':true, 'petrinet':true },
	functions: {'abs': true, 'simp':true, 'tokenRule':true },
	terminals: { 'STOP':true, 'ERROR': true },
	keywords: { 'const':true, 'range':true, 'set':true, 'if':true, 'then':true, 'else':true, 'when':true, 'forall':true },
	symbols: '(\\.\\.|\\.|,|:|\\[|\\]|\\(|\\)|\\{|\\}|->|\\\\|@)',
	operators: '(\\|\\||\\||&&|&|\\^|==|=|!=|<<|<=|<|>>|>=|>|\\+|-|\\*|/|%|!)',

	/**
	 * Passes over the specified code block and converts it into
	 * a series of tokens.
	 *
	 * @param{string} code - the code to tokenise
	 * @param{Token[]} - an array of tokens
	 */
	tokenise: function(code){
		const tokens = [];
    let line = 1;
		let column = 0;

		// loop through the code and construct tokens
		while(code.length !== 0){
			gobbleWhitespace();

      // construct the start position for the current token
      const start = new Position(line, column);

			let value;

			// attempt to match an action label, process type, function or keyword
			value = matchValue(this.actionLabel);
			if(value !== undefined){
        // construct the end point for the current token
        const end = new Position(line, column);

        // construct the location in the code of this token
        const location = new Location(start, end);

        // determine if the action tokenised was actally a process type
				if(this.processTypes[value] !== undefined){
					tokens.push(new Token('process-type', value, location));
				}
        // determine if the action tokenised was actually a function
				else if(this.functions[value] !== undefined){
					tokens.push(new Token('function', value, location));
				}
        // determine if the action tokenised was actually a keyword
				else if(this.keywords[value] !== undefined){
					tokens.push(new Token('keyword', value, location));
				}
				else{
					tokens.push(new Token('action', value, location))
				}

				continue;
			}

			// attempt to match an identifer or terminal
			value = matchValue(this.identifier);
			if(value !== undefined){
        // construct the end point for the current token
        const end = new Position(line, column);

        // construct the location in the code of this token
        const location = new Location(start, end);

				if(this.terminals[value] !== undefined){
					tokens.push(new Token('terminal', value, location));
				}
				else{
					tokens.push(new Token('identifier', value, location));
				}

				continue;
			}

			// attempt to match an integer
			value = matchValue(this.integer);
			if(value !== undefined){
        // construct the end point for the current token
        const end = new Position(line, column);

        // construct the location in the code of this token
        const location = new Location(start, end);

				tokens.push(new Token('integer', value, location));
				continue;
			}

			// attempt to match a symbol
			value = matchValue(this.symbols);
			if(value !== undefined){
        // construct the end point for the current token
        const end = new Position(line, column);

        // construct the location in the code of this token
        const location = new Location(start, end);

				tokens.push(new Token('symbol', value, location));
				continue;
			}

			// attempt to match an operator
			value = matchValue(this.operators);
			if(value !== undefined){
        // construct the end point for the current token
        const end = new Position(line, column);

        // construct the location in the code of this token
        const location = new Location(start, end);

				tokens.push(new Token('operator', value, location));
				continue;
			}

			// no match found, current character not defined by process grammar
			const character = code.charAt(0);
			throw new LexerException(character);

			break;
		}

		tokens.push(new Token('EOF', 'end of file'));
		return tokens;

		// HELPER FUNCTION

		/**
		 * Attempts to match the specified regular expression at the
		 * current position in the code. If the match is successful, the
		 * value that was matched is returned. Otherwise returns undefined.
		 *
		 * @param{string} regex - the regular expression to match
		 * @return{string} value - the value that was matched, or undefined if no match
		 */
		function matchValue(regex){
			const match = code.match(regex);
			if(match !== null && match.index === 0){
				const value = match[0];
				column += value.length;
				code = code.slice(value.length, code.length);
				return value;
			}
		}

		/**
		 * Removes any whitespace from the code and adjusts the current
		 * position accordingly.
		 */
		function gobbleWhitespace(){
			let index = 0
			while(code.length !== 0){
				const next = code.charAt(index);
				if(next === ' ' || next === '\t'){
					// increase the column position
					column++;
				}
				else if(next === '\n' || next === '\r'){
					// move to a new line
					line++;
					column = 0;
				}
				else{
					break;
				}

				index++;
			}

			// remove the whitespace from the code
			code = code.slice(index, code.length);
		}
	}
};

// CONSTRUCTORS

/**
 * Constructs and returns a new Token with the specified type
 * and value.
 *
 * @param{string} type - the type of token
 * @param{string} value - the value the token represents
 * @return{Token} - the constructed token
 */
function Token(type, value){
	this.type = type;
	this.value = value;
}

function Position(line, column){
  this.line = line;
  this.col = column;
}

function Location(start, end){
  this.start = start;
  this.end = end;
}

// EXCEPTIONS

/**
 * Constructs and returns a LexerException, which is used to identify
 * when a character that is not supported by the process grammar is found
 * in a code block.
 *
 * @param{string} character - the unexpected character
 * @param{Location} location - the location of the character
 * @return{LexerException} - the lexer exception
 */
function LexerException(character, location){
	this.message = 'Unexpected character \'' + character + '\' found';
	this.location = location;
	this.toString = function(){
		return 'LexerException: ' + this.message;
	}
}