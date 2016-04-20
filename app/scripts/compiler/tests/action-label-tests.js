var assert = chai.assert;

function runActionLabelTests(){

/**
 * Helper function for tests which generates and returns the process code 
 * to parse containing the specified action label.
 *
 * @param {string} action - the action label to generate a process for
 * @return {string} - the process code
 */
function generateProcessCode(action){
	return 'A = ' + action + ' -> STOP.';
}

/**
 * Helper function for tests which generates the correct JSON for an 
 action label node for an ast.
 *
 * @param {string} action - the action to generate JSON for
 * @return {object} - action label node for ast
 */
function generateActionLabelJSON(action){
	return [{
		type:'process',
		ident:'A',
		process:{
			type:'sequence',
			from:{
				type:'action-label',
				action:action
			},
			to:{
				type:'terminal',
				terminal:'STOP'
			}
		},
		local:[]
	}];
}

function generateActionRangeJSON(range, variable, action){
	return [{
		type:'process',
		ident:'A',
		process:{
			type:'index',
			variable:variable,
			range:range,
			process:{
				type:'sequence',
				from:{
					type:'action-label',
					action:action
				},
				to:{
					type:'terminal',
					terminal:'STOP'
				}
			}
		},
		local:[]
	}];
}

/**
 * Helper function for tests which constructs and returns an ast
 * from the specified code.
 *
 * @param {string} code - the code to generate ast from
 * @param {node[]} - the ast generated
 */
function constructAST(code){
	var tokens = lexer.parse(code);
	return parse(tokens).processes;
}

describe('Grammar to AST Action Label Tests', function(){

	it('Should parse \'' + generateProcessCode('a') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('a'));
		var expected = generateActionLabelJSON('a');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('abc') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('abc'));
		var expected = generateActionLabelJSON('abc');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('aBc') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('aBc'));
		var expected = generateActionLabelJSON('aBc');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})		

	it('Should parse \'' + generateProcessCode('aB_C') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('aB_C'));
		var expected = generateActionLabelJSON('aB_C');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('ab_C12_3') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('ab_C12_3'));
		var expected = generateActionLabelJSON('ab_C12_3');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('a.b_C12_3') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('a.b_C12_3'));
		var expected = generateActionLabelJSON('a.b_C12_3');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})				

	it('Should parse \'' + generateProcessCode('[1]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('[1]'));
		var expected = generateActionLabelJSON('[1]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('[1][2]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('[1][2]'));
		var expected = generateActionLabelJSON('[1][2]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})	

	it('Should parse \'' + generateProcessCode('[1][2][4 - 1]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('[1][2][4 - 1]'));
		var expected = generateActionLabelJSON('[1][2][$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})	

	it('Should parse \'' + generateProcessCode('[1].[2]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('[1].[2]'));
		var expected = generateActionLabelJSON('[1].[2]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})	

	it('Should parse \'' + generateProcessCode('[1].[2].[4 - 1]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('[1].[2].[4 - 1]'));
		var expected = generateActionLabelJSON('[1].[2].[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})	

it('Should parse \'' + generateProcessCode('a[1]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('a'));
		var expected = generateActionLabelJSON('a');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('a[1]b[2]c[3]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('abc'));
		var expected = generateActionLabelJSON('abc');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('aB[1][3-2]c[4]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('aB[1][3-2]c[4]'));
		var expected = generateActionLabelJSON('aB[1][$<v0>]c[4]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})		

	it('Should parse \'' + generateProcessCode('aB_[1][5-2]cC[8]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('aB_[1][5-2]cC[8]'));
		var expected = generateActionLabelJSON('aB_[1][$<v0>]cC[8]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('a[1]b_C[2][3-2]c12_[5][3]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('a[1]b_C[2][3-2]c12_[5][3]'));
		var expected = generateActionLabelJSON('a[1]b_C[2][$<v0>]c12_[5][3]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

it('Should parse \'' + generateProcessCode('a.[1]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('a.[1]'));
		var expected = generateActionLabelJSON('a.[1]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('a.[1].b.[2].c.[3]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('a.[1].b.[2].c.[3]'));
		var expected = generateActionLabelJSON('a.[1].b.[2].c.[3]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('aB.[1].[3-2].c.[4]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('aB.[1].[3-2].c.[4]'));
		var expected = generateActionLabelJSON('aB.[1].[$<v0>].c.[4]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})		

	it('Should parse \'' + generateProcessCode('aB_.[1].[5-2].cC.[8]') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('aB_.[1].[5-2].cC.[8]'));
		var expected = generateActionLabelJSON('aB_.[1].[$<v0>].cC.[8]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

	it('Should parse \'' + generateProcessCode('a.[1].b_C[2].[3-2].a12_[5].c3') + '\' correctly', function() {
		var ast = constructAST(generateProcessCode('a.[1].b_C[2].[3-2].a12_[5].c3'));
		var expected = generateActionLabelJSON('a.[1].b_C[2].[$<v0>].a12_[5].c3');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	})

});

describe('Grammar to AST Action Range Tests', function(){

	it('Should parse \'' + generateProcessCode('[0..2]') + '\' correctly', function(){
		var ast = constructAST(generateProcessCode('[0..2]'));
		var expected = generateActionRangeJSON({ type:'range', start:0, end:2 }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'' + generateProcessCode('[a:0..2]') + '\' correctly', function(){
		var ast = constructAST(generateProcessCode('[a:0..2]'));
		var expected = generateActionRangeJSON({ type:'range', start:0, end:2 }, '$a', '[$a]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'' + generateProcessCode('[-10..-5]') + '\' correctly', function(){
		var ast = constructAST(generateProcessCode('[-10..-5]'));
		var expected = generateActionRangeJSON({ type:'range', start:-10, end:-5 }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'' + generateProcessCode('[a:-10..-5]') + '\' correctly', function(){
		var ast = constructAST(generateProcessCode('[a:-10..-5]'));
		var expected = generateActionRangeJSON({ type:'range', start:-10, end:-5 }, '$a', '[$a]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'const N = 0 A = [N..2] -> STOP.\' correctly', function(){
		var ast = constructAST('const N = 0 A = [N..2] -> STOP.');
		var expected = generateActionRangeJSON({ type:'range', start:0, end:2 }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'const N = 2 A = [0..N] -> STOP.\' correctly', function(){
		var ast = constructAST('const N = 2 A = [0..N] -> STOP.');
		var expected = generateActionRangeJSON({ type:'range', start:0, end:2 }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'const X = 0 const Y = 2 A = [X..Y] -> STOP.\' correctly', function(){
		var ast = constructAST('const X = 0 const Y = 2 A = [X..Y] -> STOP.');
		var expected = generateActionRangeJSON({ type:'range', start:0, end:2 }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});	

	it('Should parse \'range N = -5..15 A = [N] -> STOP.\' correctly', function(){
		var ast = constructAST('range N = -5..15 A = [N] -> STOP.');
		var expected = generateActionRangeJSON({ type:'range', start:-5, end:15 }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});	

	it('Should parse \'range N = -5..15 A = [test:N] -> STOP.\' correctly', function(){
		var ast = constructAST('range N = -5..15 A = [test:N] -> STOP.');
		var expected = generateActionRangeJSON({ type:'range', start:-5, end:15 }, '$test', '[$test]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});	

	it('Should parse \'' + generateProcessCode('[{a, b, c}]') + '\' correctly', function(){
		var ast = constructAST(generateProcessCode('[{a, b, c}]'));
		var expected = generateActionRangeJSON({ type:'set', set:['a', 'b', 'c'] }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'' + generateProcessCode('[{[0..2]}]') + '\' correctly', function(){
		var ast = constructAST(generateProcessCode('[{[0..2]}]'));
		var expected = generateActionRangeJSON({ type:'set', set:['[0]', '[1]', '[2]'] }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'' + generateProcessCode('[{a[0..2]}]') + '\' correctly', function(){
		var ast = constructAST(generateProcessCode('[{a[0..2]}]'));
		var expected = generateActionRangeJSON({ type:'set', set:['a[0]', 'a[1]', 'a[2]'] }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'set N = {a, b, c} A = [N] -> STOP.\' correctly', function(){
		var ast = constructAST('set N = {a, b, c} A = [N] -> STOP.');
		var expected = generateActionRangeJSON({ type:'set', set:['a', 'b', 'c'] }, '$<v0>', '[$<v0>]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

	it('Should parse \'set N = {a, b, c, d} A = [test:N] -> STOP.\' correctly', function(){
		var ast = constructAST('set N = {a, b, c, d} A = [test:N] -> STOP.');
		var expected = generateActionRangeJSON({ type:'set', set:['a', 'b', 'c', 'd'] }, '$test', '[$test]');

		assert.equal(JSON.stringify(ast), JSON.stringify(expected));
	});

});

}

runActionLabelTests();