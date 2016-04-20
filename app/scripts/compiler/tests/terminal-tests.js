/**
 * Tests to ensure that the parsing of the grammar to abstract syntax tree is correct.
 */

var assert = chai.assert;

/**
 * Correct abstract syntax tree JSON for parsing a singluar STOP process
 */
var STOP_JSON = [{ type:'process', ident:'A', process:{ type:'terminal', terminal:'STOP'}, local:[] }];

/**
 * Correct abstract syntax tree JSON for parsing a singluar ERROR process
 */
var ERROR_JSON = [{ type:'process', ident:'A', process:{ type:'terminal', terminal:'ERROR'}, local:[] }];

/**
 * Test that the parsing of a stop terminal produces the correct abstract syntax tree.
 */
describe('Grammar to AST Stop Terminal Tests', function(){
	
	// code to be used within tests
	var withParen = 'A = (STOP).'
	var withoutParen = 'A = STOP.'

	/**
	 * Tests that parsing a singular STOP terminal produces the corecet abstract
	 * syntax tree.
	 */
	it('Should parse \'' + withParen + '\' successfully', function(){
		var tokens = lexer.parse(withParen);
		var ast = parse(tokens).processes;

		assert.equal(JSON.stringify(ast), JSON.stringify(STOP_JSON));
	});

	/**
	 * Tests that parsing a singular STOP terminal produces the corecet abstract
	 * syntax tree.
	 */
	it('Should parse \'' + withoutParen + '\' successfully', function(){
		var tokens = lexer.parse(withoutParen);
		var ast = parse(tokens).processes;

		assert.equal(JSON.stringify(ast), JSON.stringify(STOP_JSON));
	});

	/**
	 * Tests that parsing a STOP terminal with or without parentheses produces the same
	 * abstract syntax tree
	 */
	it('\'' + withParen + '\' and \'' + withoutParen + '\' should be equivalent', function(){
		var withTokens = lexer.parse(withParen);
		var withAST = parse(withTokens).processes;

		var withoutTokens = lexer.parse(withoutParen);
		var withoutAST = parse(withoutTokens).processes;

		assert.equal(JSON.stringify(withAST), JSON.stringify(withoutAST));
	});
});

/**
 * Test that the parsing of an error terminal produces the correct abstract syntax tree.
 */
describe('Grammar to AST Error Terminal Tests', function(){
	
	// code to be used within tests
	var withParen = 'A = (ERROR).'
	var withoutParen = 'A = ERROR.'

	/**
	 * Tests that parsing a singular ERROR terminal produces the corecet abstract
	 * syntax tree.
	 */
	it('Should parse \'' + withParen + '\' successfully', function(){
		var tokens = lexer.parse(withParen);
		var ast = parse(tokens).processes;

		assert.equal(JSON.stringify(ast), JSON.stringify(ERROR_JSON));
	});

	/**
	 * Tests that parsing a singular ERROR terminal produces the corecet abstract
	 * syntax tree.
	 */
	it('Should parse \'' + withoutParen + '\' successfully', function(){
		var tokens = lexer.parse(withoutParen);
		var ast = parse(tokens).processes;

		assert.equal(JSON.stringify(ast), JSON.stringify(ERROR_JSON));
	});

	/**
	 * Tests that parsing a ERROR terminal with or without parentheses produces the same
	 * abstract syntax tree
	 */
	it('\'' + withParen + '\' and \'' + withoutParen + '\' should be equivalent', function(){
		var withTokens = lexer.parse(withParen);
		var withAST = parse(withTokens).processes;

		var withoutTokens = lexer.parse(withoutParen);
		var withoutAST = parse(withoutTokens).processes;

		assert.equal(JSON.stringify(withAST), JSON.stringify(withoutAST));
	});
});