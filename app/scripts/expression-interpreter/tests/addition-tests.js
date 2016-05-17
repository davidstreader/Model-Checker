var assert = chai.assert;

describe('Addition Tests', function(){

	it('The expression 1 should equal 1', function(){
		var result = evaluate(1);
		assert.equal(1, result);
	});

	it('The expression \'1\' should equal 1', function(){
		var result = evaluate("1");
		assert.equal(1, result);
	});

	it('The expression \'1 + 1\' should equal 2', function(){
		var result = evaluate("1 + 1");
		assert.equal(2, result);
	});

	it('The expression \'3 + 2 + 1\' should equal 6', function(){
		var result = evaluate("3 + 2 + 1");
		assert.equal(6, result);
	});

	it('The expression \'(3 + 2) + 1\' should equal 6', function(){
		var result = evaluate("(3 + 2) + 1");
		assert.equal(6, result);
	});

	it('The expression \'3 + (2 + 1)\' should equal 6', function(){
		var result = evaluate('3 + (2 + 1)');
		assert.equal(6, result);
	});

	it('The expression \'(3 + 2 + 1)\' should equal 6', function(){
		var result = evaluate('(3 + 2 + 1)');
		assert.equal(6, result);
	});

	it('The expression \'3 - 2\' should equal 1', function(){
		var result = evaluate('3 - 2');
		assert.equal(1, result);
	});

	it('The expression \'5 - 2 - 1\' should equal 2', function(){
		var result = evaluate('5 - 2 - 1');
		assert.equal(2, result);
	});	

	it('The expression \'(5 - 2) - 1\' should equal 2', function(){
		var result = evaluate('(5 - 2) - 1');
		assert.equal(2, result);
	});

	it('The expression \'5 - (2 + 1)\' should equal 2', function(){
		var result = evaluate('5 - (2 + 1)');
		assert.equal(2, result);
	});

	it('The expression \'5 - (2 - 1)\' should equal 4', function(){
		var result = evaluate('5 - (2 - 1)');
		assert.equal(4, result);
	});

	it('The expression \'(5 - 2 - 1)\' should equal 2', function(){
		var result = evaluate('(5 - 2 - 1)');
		assert.equal(2, result);
	});

	it('The expression \'((5 - 2 - 1))\' should equal 2', function(){
		var result = evaluate('((5 - 2 - 1))');
		assert.equal(2, result);
	});								

	it('The expression \'10 + 2 - 7\' should equal 5', function(){
		var result = evaluate('10 + 2 - 7');
		assert.equal(5, result);
	});

	it('The expression \'(10 + 2) - 7\' should equal 5', function(){
		var result = evaluate("(10 + 2) - 7");
		assert.equal(5, result);
	});

	it('The expression \'10 + (2 - 7)\' should equal 5', function(){
		var result = evaluate('10 + (2 - 7)');
		assert.equal(5, result);
	});

	it('The expression \'10 - (2 - 7)\' should equal 15', function(){
		var result = evaluate('10 - (2 - 7)');
		assert.equal(15, result);
	});

	it('The expression \'(10 + 5) - (3 + 2)\' should equal 10', function(){
		var result = evaluate('(10 + 5) - (3 + 2)');
		assert.equal(10, result);
	});

	it('The expression \'(10 + (5 - 3) + 2)\' should equal 14', function(){
		var result = evaluate('(10 + (5 - 3) + 2)');
		assert.equal(14, result);
	});

});
