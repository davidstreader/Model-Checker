var assert = chai.assert;

describe('Boolean Tests', function(){

	it('The expression \'3 < 1\' should equal 0', function(){
		var result = evaluate("3 < 1");
		assert.equal(0, result);
	});

	it('The expression \'3 <= 1\' should equal 0', function(){
		var result = evaluate("3 <= 1");
		assert.equal(0, result);
	});

	it('The expression \'5 < 5\' should equal 0', function(){
		var result = evaluate("5 < 5");
		assert.equal(0, result);
	});

	it('The expression \'5 <= 5\' should equal 1', function(){
		var result = evaluate("5 <= 5");
		assert.equal(1, result);
	});

	it('The expression \'6 > 9\' should equal 0', function(){
		var result = evaluate("6 > 9");
		assert.equal(0, result);
	});

	it('The expression \'6 >= 9\' should equal 0', function(){
		var result = evaluate("6 >= 9");
		assert.equal(0, result);
	});

	it('The expression \'7 > 7\' should equal 0', function(){
		var result = evaluate("7 > 7");
		assert.equal(0, result);
	});

	it('The expression \'7 >= 7\' should equal 1', function(){
		var result = evaluate("7 >= 7");
		assert.equal(1, result);
	});

	it('The expression \'1 != 1\' should equal 0', function(){
		var result = evaluate("1 != 1");
		assert.equal(0, result);
	});

	it('The expression \'2 == 2\' should equal 1', function(){
		var result = evaluate("2 == 2");
		assert.equal(1, result);
	});

	it('The expression \'10 != 10 || 10 == 10\' should equal 1', function(){
		var result = evaluate("10 != 10 || 10 == 10");
		assert.equal(1, result);
	});

	it('The expression \'10 != 10 && 10 == 10\' should equal 0', function(){
		var result = evaluate("10 != 10 && 10 == 10");
		assert.equal(0, result);
	});

});
