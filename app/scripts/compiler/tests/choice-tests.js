var assert = chai.assert;

function runChoiceTests(){

/**
 * Heleper function for tests which generates the ast for a
 * the specified array of actions. Generates the tree recursively
 * starting from the specified index.
 *
 * @param {string[]} sequence - array of actions to construct sequence for
 * @param {int} index - the index position to start constructing from
 * @return {node} - ast sequence node
 */
function generateSequenceNode(sequence, index){
	if(index === sequence.length - 1){
		return { type:'terminal', terminal:sequence[index] };
	}

	return { type:'sequence', from:{ type:'action-label', action:sequence[index] }, to:generateSequenceNode(sequence, index + 1) };
}

/**
 * Helper function for tests which generates a choice node for the
 * ast from the specified processes.
 *
 * @param {node} process1 - the first process
 * @param {node} process2 - the second process
 * @return 
 */
function generateChoiceNode(process1, process2){
	return [{
		type:'process',
		ident:'A',
		process:{
			type:'choice',
			process1:process1,
			process2:process2
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

describe('Grammar to AST Choice Tests', function(){

	it('Should parse A = STOP | STOP. correctly', function(){
		var expected = generateChoiceNode(generateSequenceNode(['STOP'], 0), generateSequenceNode(['STOP'], 0));
		var received = constructAST('A = STOP | STOP.');
		assert.equal(JSON.stringify(expected), JSON.stringify(received));
	});

	it('Should parse A = a -> STOP | STOP. correctly', function(){
		var expected = generateChoiceNode(generateSequenceNode(['a', 'STOP'], 0), generateSequenceNode(['STOP'], 0));
		var received = constructAST('A = a -> STOP | STOP.');

		assert.equal(JSON.stringify(expected), JSON.stringify(received));
	});

	it('Should parse A = STOP | b -> STOP. correctly', function(){
		var expected = generateChoiceNode(generateSequenceNode(['STOP'], 0), generateSequenceNode(['b', 'STOP'], 0));
		var received = constructAST('A = STOP | b -> STOP.');

		assert.equal(JSON.stringify(expected), JSON.stringify(received));
	});

	it('Should parse A = a -> STOP | b -> STOP. correctly', function(){
		var expected = generateChoiceNode(generateSequenceNode(['a', 'STOP'], 0), generateSequenceNode(['b', 'STOP'], 0));
		var received = constructAST('A = a -> STOP | b -> STOP.');

		assert.equal(JSON.stringify(expected), JSON.stringify(received));
	});

	it('Should parse A = a -> b -> STOP | c -> STOP. correctly', function(){
		var expected = generateChoiceNode(generateSequenceNode(['a', 'b', 'STOP'], 0), generateSequenceNode(['c', 'STOP'], 0));
		var received = constructAST('A = a -> b -> STOP | c -> STOP.');

		assert.equal(JSON.stringify(expected), JSON.stringify(received));
	});

	it('Should parse A = a -> STOP | b -> c -> STOP. correctly', function(){
		var expected = generateChoiceNode(generateSequenceNode(['a', 'STOP'], 0), generateSequenceNode(['b', 'c', 'STOP'], 0));
		var received = constructAST('A = a -> STOP | b -> c -> STOP.');

		assert.equal(JSON.stringify(expected), JSON.stringify(received));
	});

	it('Should parse A = a -> b -> STOP | c -> d -> STOP. correctly', function(){
		var expected = generateChoiceNode(generateSequenceNode(['a', 'b', 'STOP'], 0), generateSequenceNode(['c', 'd', 'STOP'], 0));
		var received = constructAST('A = a -> b -> STOP | c -> d -> STOP.');

		assert.equal(JSON.stringify(expected), JSON.stringify(received));
	});

});

}

runChoiceTests();