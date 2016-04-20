var assert = chai.assert;

function runConstantTests(){

function generateProcessCode(type, ident, value){
	return type + ' ' + ident + ' = ' + value;
}

function constructConstantsMap(code){
	var tokens = lexer.parse(code);
	return parse(tokens).constantsMap;
}

describe('Grammar to AST Const Tests', function(){

	it('Should parse \'const N = 1\' correctly', function(){
		var constantsMap = constructConstantsMap('const N = 1');
		assert.equal(constantsMap['N'].value, 1);
	});

	it('Should parse \'const N = 100\' correctly', function(){
		var constantsMap = constructConstantsMap('const N = 100');
		assert.equal(constantsMap['N'].value, 100);
	});

	it('Should parse \'const N = -10\' correctly', function(){
		var constantsMap = constructConstantsMap('const N = -10');
		assert.equal(constantsMap['N'].value, -10);
	});

	it('Should parse \'const M = 1 const N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 1 const N = M');
		assert.equal(constantsMap['N'].value, 1);
	});

	it('Should parse \'const M = -1 const N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = -1 const N = M');
		assert.equal(constantsMap['N'].value, -1);
	});

});

describe('Grammar to AST Range Tests', function(){

	it('Should parse \' range N = 0..2\' correctly', function(){
		var constantsMap = constructConstantsMap('range N = 0..2');
		var range = constantsMap['N'];
		assert.equal(range.start, 0);
		assert.equal(range.end, 2);
	});

	it('Should parse \' range N = -5..500\' correctly', function(){
		var constantsMap = constructConstantsMap('range N = -5..500');
		var range = constantsMap['N'];
		assert.equal(range.start, -5);
		assert.equal(range.end, 500);
	});

	it('Should parse \' const M = 0 range N = M..2\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 0 range N = M..2');
		var range = constantsMap['N'];
		assert.equal(range.start, 0);
		assert.equal(range.end, 2);
	});

	it('Should parse \' const M = 2 range N = 0..M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 2 range N = 0..M');
		var range = constantsMap['N'];
		assert.equal(range.start, 0);
		assert.equal(range.end, 2);
	});

	it('Should parse \' const X = 0 const Y = 2 range N = X..Y\' correctly', function(){
		var constantsMap = constructConstantsMap('const X = 0 const Y = 2 range N = X..Y');
		var range = constantsMap['N'];
		assert.equal(range.start, 0);
		assert.equal(range.end, 2);
	});

	it('Should parse \' range M = 0..2 range N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('range M = 0..2 range N = M');
		var range = constantsMap['N'];
		assert.equal(range.start, 0);
		assert.equal(range.end, 2);
	});

	it('Should parse \' const X = 0 const Y = 2 range M = X..Y range N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const X = 0 const Y = 2 range M = X..Y range N = M');
		var range = constantsMap['N'];
		assert.equal(range.start, 0);
		assert.equal(range.end, 2);
	});

});

describe('Grammar to AST Set Tests', function(){

	it('Should parse \'set N = {a}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a}');
		var set = constantsMap['N'].set;
		var expected = ['a'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i < set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	})

	it('Should parse \'set N = {a, b, c}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a, b, c}');
		var set = constantsMap['N'].set;
		var expected = ['a', 'b', 'c'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i < set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	})

	it('Should parse \'set N = {[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {[0..2]}');
		var set = constantsMap['N'].set;
		var expected = ['[0]', '[1]', '[2]'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set N = {a[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2]}');
		var set = constantsMap['N'].set;
		var expected = ['a[0]', 'a[1]', 'a[2]'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set N = {a.[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2]}');
		var set = constantsMap['N'].set;
		var expected = ['a.[0]', 'a.[1]', 'a.[2]'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set N = {a.[0..2]b}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2]b}');
		var set = constantsMap['N'].set;
		var expected = ['a.[0]b', 'a.[1]b', 'a.[2]b'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set N = {a.[0..2].b}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2].b}');
		var set = constantsMap['N'].set;
		var expected = ['a.[0].b', 'a.[1].b', 'a.[2].b'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set N = {[0..2][0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {[0..2][0..2]}');
		var set = constantsMap['N'].set;
		var expected = ['[0][0]', '[0][1]', '[0][2]', '[1][0]', '[1][1]', '[1][2]', '[2][0]', '[2][1]', '[2][2]'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set N = {a[0..2][0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2][0..2]}');
		var set = constantsMap['N'].set;
		var expected = ['a[0][0]', 'a[0][1]', 'a[0][2]', 'a[1][0]', 'a[1][1]', 'a[1][2]', 'a[2][0]', 'a[2][1]', 'a[2][2]'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set N = {a[0..2]b[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2]b[0..2]}');
		var set = constantsMap['N'].set;
		var expected = ['a[0]b[0]', 'a[0]b[1]', 'a[0]b[2]', 'a[1]b[0]', 'a[1]b[1]', 'a[1]b[2]', 'a[2]b[0]', 'a[2]b[1]', 'a[2]b[2]'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set N = {a[0..2].b[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2].b[0..2]}');
		var set = constantsMap['N'].set;
		var expected = ['a[0].b[0]', 'a[0].b[1]', 'a[0].b[2]', 'a[1].b[0]', 'a[1].b[1]', 'a[1].b[2]', 'a[2].b[0]', 'a[2].b[1]', 'a[2].b[2]'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i  <set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set M = {a, b, c} set N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('set M = {a, b, c} set N = M');
		var set = constantsMap['N'].set;
		var expected = ['a', 'b', 'c'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i < set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

	it('Should parse \'set M = {[0..2][0..2]} set N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('set M = {[0..2][0..2]} set N = M');
		var set = constantsMap['N'].set;
		var expected = ['[0][0]', '[0][1]', '[0][2]', '[1][0]', '[1][1]', '[1][2]', '[2][0]', '[2][1]', '[2][2]'];

		// set should be the same size
		assert.equal(set.length, expected.length);

		// check that sets are identical
		for(var i = 0; i < set.length; i++){
			assert.equal(set[i], expected[i]);
		}
	});

});

}

runConstantTests();