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
		var result = evaluate("3 + (2 + 1)");
		assert.equal(6, result);
	});

	it('The expression \'(3 + 2 + 1)\' should equal 6', function(){
		var result = evaluate("(3 + 2 + 1)");
		assert.equal(6, result);
	});

});
