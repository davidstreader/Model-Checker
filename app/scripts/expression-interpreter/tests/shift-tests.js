var assert = chai.assert;

describe('Shift Tests', function(){

	it('The expression \'2 << 3\' should equal 16', function(){
		var result = evaluate("2 << 3");
		assert.equal(16, result);
	});

	it('The expression \'3 << 2\' should equal 12', function(){
		var result = evaluate("3 << 2");
		assert.equal(12, result);
	});

	it('The expression \'2 << (2 + 1)\' should equal 16', function(){
		var result = evaluate("2 << (2 + 1)");
		assert.equal(16, result);
	});

	it('The expression \'(3 - 1) << (2 + 1)\' should equal 16', function(){
		var result = evaluate("(3 - 1) << (2 + 1)");
		assert.equal(16, result);
	});

	it('The expression \'3 - 1 << 2 + 1\' should equal 16', function(){
		var result = evaluate("3 - 1 << 2 + 1");
		assert.equal(16, result);
	});

	it('The expression \'2 << 3 << 4\' should equal 256', function(){
		var result = evaluate("2 << 3 << 4");
		assert.equal(256, result);
	});

	it('The expression \'512 >> 3\' should equal 64', function(){
		var result = evaluate("512 >> 3");
		assert.equal(64, result);
	});

	it('The expression \'512 >> 2\' should equal 128', function(){
		var result = evaluate("512 >> 2");
		assert.equal(128, result);
	});

	it('The expression \'512 >> (2 + 1)\' should equal 64', function(){
		var result = evaluate("512 >> (2 + 1)");
		assert.equal(64, result);
	});

	it('The expression \'(520 - 8) >> (2 + 1)\' should equal 64', function(){
		var result = evaluate("(520 - 8) >> (2 + 1)");
		assert.equal(64, result);
	});

	it('The expression \'520 - 8 >> 2 + 1\' should equal 64', function(){
		var result = evaluate("520 - 8 >> 2 + 1");
		assert.equal(64, result);
	});

	it('The expression \'1024 >> 3 >> 2\' should equal 32', function(){
		var result = evaluate("1024 >> 3 >> 2");
		assert.equal(32, result);
	});

});
