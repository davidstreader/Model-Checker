'use strict';

/**
 * Unit tests that cover the correct and incorrect usages of the lexer.
 */
describe('Lexer Tests', function(){

  const chai = require("chai");
	// used by unit tests
	const expect = chai.expect;
	const assert = chai.assert;

	/**
	 * Defines a set of values that the lexer should be able to
	 * tokenise correctly.
	 */
	const actionLabels = { type:'action', values:['a', 'bTest', 'cTest123', 'd123Test', 'e1T2e3s4t'] };
	const identifiers = { type:'identifier', values:['A', 'BTest', 'CTEST', 'Dtest123', 'ET1E2S4T'] };
	const integers = { type:'integer', values:['1', '2', '3', '4', '5', '10', '145', '156756', '9007199254740991'] };
	const processTypes = { type:'process-type', values:['automata', 'petrinet'] };
	const functions = { type:'function', values:['abs', 'simp', 'safe'] };
	const terminals = { type:'terminal', values:['STOP', 'ERROR'] };
	const keywords = { type:'keyword', values:['const', 'range', 'set', 'if', 'then', 'else', 'when', 'forall'] };
	const symbols = { type:'symbol', values:['..', '.', ',', ':', '[', ']', '(', ')', '{', '}', '->', '~>', '\\', '@'] };
	const operators = { type:'operator', values:['||', '|', '&&', '&', '^', '==', '!=', '<', '<=', '>', '>=', '<<', '>>', '=', '+', '-', '*', '/', '%', '!'] };
	const toTokenise = [actionLabels, identifiers, integers, processTypes, functions, terminals, keywords, symbols, operators];

	/**
	 * Construct test objects used by the test runner to test the
	 * tokenisable values defined above.
	 */
	const validTests = [];
	toTokenise.forEach(function(set){
		set.values.forEach(function(value){
			validTests.push({ type:set.type, value:value });
		});
	});

	/**
	 * Run tests to ensure that all the defined tokenisable values can
	 * be tokenised correctly by the lexer.
	 */
	validTests.forEach(function(test){
		it('Should tokenise ' + test.type + ' \'' + test.value + '\' correctly', function(){
			const tokens = Lexer.tokenise(test.value);
			assert.lengthOf(tokens, 2);
			// the expected token
			expect(tokens[0]).to.have.property('type', test.type);
			expect(tokens[0]).to.have.property('value', test.value);
			// the end of file token
			expect(tokens[1]).to.have.property('type', 'EOF');
			expect(tokens[1]).to.have.property('value', 'end of file');
		});
	});

	/**
	 * Defines an array of invalid characters and values that should not
	 * be tokenisable by the lexer.
	 */
	const invalid = ['#', '\'', '"','0', '01', '0200'];

	/**
	 * Runs tests to ensure that the defined invalid characters and values
	 * are not tokenisable by the lexer
	 */
	invalid.forEach(function(value){
		it('Should not be able to tokenise \'' + value + '\'', function(){
			try{
				const tokens = Lexer.tokenise(value);
				// should not reach this point
				assert.fail('Sucessfully tokenised \'' + value + '\'', 'Should have thrown a LexerException');
			}catch(error){
				// should catch error
			}
		});
	});

});
