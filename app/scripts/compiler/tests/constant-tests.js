'use strict';

/**
 * Unit tests that test the definition of global constants, ranges and sets
 * for processes to reference.
 */

describe('Grammar to AST Const Tests', function(){

	it('Should parse \'const N = 1\' correctly', function(){
		var constantsMap = constructConstantsMap('const N = 1');
		expect(constantsMap).to.have.property('N');
		testConstNode(constantsMap['N'], 1);

	});

	it('Should parse \'const N = 100\' correctly', function(){
		var constantsMap = constructConstantsMap('const N = 100');
		expect(constantsMap).to.have.property('N');
		testConstNode(constantsMap['N'], 100);
	});

	it('Should parse \'const N = -10\' correctly', function(){
		var constantsMap = constructConstantsMap('const N = -10');
		expect(constantsMap).to.have.property('N');
		var constant = constantsMap['N'];
		testConstNode(constantsMap['N'], -10);
	});

	it('Should parse \'const M = 1 const N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 1 const N = M');
		expect(constantsMap).to.have.property('M');
		testConstNode(constantsMap['M'], 1);
		expect(constantsMap).to.have.property('N');
		testConstNode(constantsMap['N'], 1);
	});

	it('Should parse \'const M = -1 const N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = -1 const N = M');
		expect(constantsMap).to.have.property('M');
		testConstNode(constantsMap['M'], -1);
		expect(constantsMap).to.have.property('N');
		testConstNode(constantsMap['N'], -1);
	});

});

describe('Grammar to AST Range Tests', function(){

	it('Should parse \' range N = 0..2\' correctly', function(){
		var constantsMap = constructConstantsMap('range N = 0..2');
		expect(constantsMap).to.have.property('N');
		testRangeNode(constantsMap['N'], 0, 2);
	});

	it('Should parse \' range N = -5..500\' correctly', function(){
		var constantsMap = constructConstantsMap('range N = -5..500');
		expect(constantsMap).to.have.property('N');
		testRangeNode(constantsMap['N'], -5, 500);
	});

	it('Should parse \' const M = 0 range N = M..2\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 0 range N = M..2');
		expect(constantsMap).to.have.property('M');
		testConstNode(constantsMap['M'], 0);
		expect(constantsMap).to.have.property('N');
		testRangeNode(constantsMap['N'], 0, 2);
	});

	it('Should parse \' const M = 2 range N = 0..M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 2 range N = 0..M');
		expect(constantsMap).to.have.property('M');
		testConstNode(constantsMap['M'], 2);
		expect(constantsMap).to.have.property('N');
		testRangeNode(constantsMap['N'], 0, 2);
	});

	it('Should parse \' const X = 0 const Y = 2 range N = X..Y\' correctly', function(){
		var constantsMap = constructConstantsMap('const X = 0 const Y = 2 range N = X..Y');
		expect(constantsMap).to.have.property('X');
		testConstNode(constantsMap['X'], 0);
		expect(constantsMap).to.have.property('Y');
		testConstNode(constantsMap['Y'], 2);
		expect(constantsMap).to.have.property('N');
		testRangeNode(constantsMap['N'], 0, 2);
	});

	it('Should parse \' range M = 0..2 range N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('range M = 0..2 range N = M');
		expect(constantsMap).to.have.property('M');
		testRangeNode(constantsMap['M'], 0, 2);
		expect(constantsMap).to.have.property('N');
		testRangeNode(constantsMap['N'], 0, 2);
	});

	it('Should parse \' const X = 0 const Y = 2 range M = X..Y range N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const X = 0 const Y = 2 range M = X..Y range N = M');
		expect(constantsMap).to.have.property('X');
		testConstNode(constantsMap['X'], 0);
		expect(constantsMap).to.have.property('Y');
		testConstNode(constantsMap['Y'], 2);
		expect(constantsMap).to.have.property('M');
		testRangeNode(constantsMap['M'], 0, 2);
		expect(constantsMap).to.have.property('N');
		testRangeNode(constantsMap['N'], 0, 2);
	});

});

describe('Grammar to AST Set Tests', function(){

	it('Should parse \'set N = {a}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a}');
		var expected = ['a'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	})

	it('Should parse \'set N = {a, b, c}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a, b, c}');
		var expected = ['a', 'b', 'c'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	})

	it('Should parse \'set N = {[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {[0..2]}');
		var expected = ['[0]', '[1]', '[2]'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set N = {a[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2]}');
		var expected = ['a[0]', 'a[1]', 'a[2]'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set N = {a.[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2]}');
		expect(constantsMap).to.have.property('N');
		var expected = ['a.[0]', 'a.[1]', 'a.[2]'];
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set N = {a.[0..2]b}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2]b}');
		var expected = ['a.[0]b', 'a.[1]b', 'a.[2]b'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set N = {a.[0..2].b}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2].b}');
		var expected = ['a.[0].b', 'a.[1].b', 'a.[2].b'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set N = {[0..2][0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {[0..2][0..2]}');
		var expected = ['[0][0]', '[0][1]', '[0][2]', '[1][0]', '[1][1]', '[1][2]', '[2][0]', '[2][1]', '[2][2]'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set N = {a[0..2][0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2][0..2]}');
		var expected = ['a[0][0]', 'a[0][1]', 'a[0][2]', 'a[1][0]', 'a[1][1]', 'a[1][2]', 'a[2][0]', 'a[2][1]', 'a[2][2]'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set N = {a[0..2]b[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2]b[0..2]}');
		var expected = ['a[0]b[0]', 'a[0]b[1]', 'a[0]b[2]', 'a[1]b[0]', 'a[1]b[1]', 'a[1]b[2]', 'a[2]b[0]', 'a[2]b[1]', 'a[2]b[2]'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set N = {a[0..2].b[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2].b[0..2]}');
		var expected = ['a[0].b[0]', 'a[0].b[1]', 'a[0].b[2]', 'a[1].b[0]', 'a[1].b[1]', 'a[1].b[2]', 'a[2].b[0]', 'a[2].b[1]', 'a[2].b[2]'];
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set M = {a, b, c} set N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('set M = {a, b, c} set N = M');
		var expected = ['a', 'b', 'c'];
		expect(constantsMap).to.have.property('M');
		testSetNode(constantsMap['M'], expected);
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

	it('Should parse \'set M = {[0..2][0..2]} set N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('set M = {[0..2][0..2]} set N = M');
		var expected = ['[0][0]', '[0][1]', '[0][2]', '[1][0]', '[1][1]', '[1][2]', '[2][0]', '[2][1]', '[2][2]'];
		expect(constantsMap).to.have.property('M');
		testSetNode(constantsMap['M'], expected);
		expect(constantsMap).to.have.property('N');
		testSetNode(constantsMap['N'], expected);
	});

});