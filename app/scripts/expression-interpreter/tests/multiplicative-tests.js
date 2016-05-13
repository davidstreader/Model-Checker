var assert = chai.assert;

describe('Multiplicative Tests', function(){

	it('The expression \'1 * 1\' should equal 1', function(){
		var result = evaluate("1 * 1");
		assert.equal(1, result);
	});

	it('The expression \'2 * 3\' should equal 6', function(){
		var result = evaluate("2 * 3");
		assert.equal(6, result);
	});

	it('The expression \'4 * 5 * 6\' should equal 120', function(){
		var result = evaluate("4 * 5 * 6");
		assert.equal(120, result);
	});

	it('The expression \'(4 * 5) * 6\' should equal 120', function(){
		var result = evaluate("(4 * 5) * 6");
		assert.equal(120, result);
	});		

	it('The expression \'4 * (5 * 6)\' should equal 120', function(){
		var result = evaluate("4 * (5 * 6)");
		assert.equal(120, result);
	});	

	it('The expression \'(4 * 5 * 6)\' should equal 120', function(){
		var result = evaluate("(4 * 5 * 6)");
		assert.equal(120, result);
	});

	it('The expression \'((4 * 5 * 6))\' should equal 120', function(){
		var result = evaluate("((4 * 5 * 6))");
		assert.equal(120, result);
	});

	it('The expression \'3 / 2\' should equal 1.5', function(){
		var result = evaluate("3 / 2");
		assert.equal(1.5, result);
	});

	it('The expression \'4 / 2 * 8 / 4\' should equal 4', function(){
		var result = evaluate("4 / 2 * 8 / 4");
		assert.equal(4, result);
	});


	it('The expression \'4 * 2 / 2 * 2\' should equal 8', function(){
		var result = evaluate("4 * 2 / 2 * 2");
		assert.equal(8, result);
	});

	it('The expression \'(4 * 2) / (2 * 2)\' should equal 2', function(){
		var result = evaluate("(4 * 2) / (2 * 2)");
		assert.equal(2, result);
	});

	it('The expression \'4 + 2 * 8 + 6\' should equal 26', function(){
		var result = evaluate("4 + 2 * 8 + 6");
		assert.equal(26, result);
	});

	it('The expression \'(4 + 2) * (8 + 6)\' should equal 84', function(){
		var result = evaluate("(4 + 2) * (8 + 6)");
		assert.equal(84, result);
	});

	it('The expression \'4 - 2 * 8 - 6\' should equal -18', function(){
		var result = evaluate("4 - 2 * 8 - 6");
		assert.equal(-18, result);
	});

	it('The expression \'(4 - 2) * (8 - 6)\' should equal 4', function(){
		var result = evaluate("(4 - 2) * (8 - 6)");
		assert.equal(4, result);
	});

});
