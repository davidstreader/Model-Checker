var assert = chai.assert;
var expect = chai.expect;
var to = chai.to;
var have = chai.have;

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
		expect(constantsMap).to.have.property('N');
		var constant = constantsMap['N'];
		expect(constant).to.have.property('type', 'const');
		expect(constant).to.have.property('value', 1);

	});

	it('Should parse \'const N = 100\' correctly', function(){
		var constantsMap = constructConstantsMap('const N = 100');
		expect(constantsMap).to.have.property('N');
		var constant = constantsMap['N'];
		expect(constant).to.have.property('type', 'const');
		expect(constant).to.have.property('value', 100);
	});

	it('Should parse \'const N = -10\' correctly', function(){
		var constantsMap = constructConstantsMap('const N = -10');
		expect(constantsMap).to.have.property('N');
		var constant = constantsMap['N'];
		expect(constant).to.have.property('type', 'const');
		expect(constant).to.have.property('value', -10);
	});

	it('Should parse \'const M = 1 const N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 1 const N = M');
		expect(constantsMap).to.have.property('M');
		expect(constantsMap['M']).to.have.property('type', 'const');
		expect(constantsMap['M']).to.have.property('value', 1);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'const');
		expect(constantsMap['N']).to.have.property('value', constantsMap['M'].value);
	});

	it('Should parse \'const M = -1 const N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = -1 const N = M');
		expect(constantsMap).to.have.property('M');
		expect(constantsMap['M']).to.have.property('type', 'const');
		expect(constantsMap['M']).to.have.property('value', -1);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'const');
		expect(constantsMap['N']).to.have.property('value', constantsMap['M'].value);
	});

});

describe('Grammar to AST Range Tests', function(){

	it('Should parse \' range N = 0..2\' correctly', function(){
		var constantsMap = constructConstantsMap('range N = 0..2');
		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'range');
		expect(constantsMap['N']).to.have.property('start', 0);
		expect(constantsMap['N']).to.have.property('end', 2);
	});

	it('Should parse \' range N = -5..500\' correctly', function(){
		var constantsMap = constructConstantsMap('range N = -5..500');
		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'range');
		expect(constantsMap['N']).to.have.property('start', -5);
		expect(constantsMap['N']).to.have.property('end', 500);
	});

	it('Should parse \' const M = 0 range N = M..2\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 0 range N = M..2');
		expect(constantsMap).to.have.property('M');
		expect(constantsMap['M']).to.have.property('type', 'const');
		expect(constantsMap['M']).to.have.property('value', 0);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'range');
		expect(constantsMap['N']).to.have.property('start', 0);
		expect(constantsMap['N']).to.have.property('end', 2);
	});

	it('Should parse \' const M = 2 range N = 0..M\' correctly', function(){
		var constantsMap = constructConstantsMap('const M = 2 range N = 0..M');
		expect(constantsMap).to.have.property('M');
		expect(constantsMap['M']).to.have.property('type', 'const');
		expect(constantsMap['M']).to.have.property('value', 2);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'range');
		expect(constantsMap['N']).to.have.property('start', 0);
		expect(constantsMap['N']).to.have.property('end', 2);
	});

	it('Should parse \' const X = 0 const Y = 2 range N = X..Y\' correctly', function(){
		var constantsMap = constructConstantsMap('const X = 0 const Y = 2 range N = X..Y');
		expect(constantsMap).to.have.property('X');
		expect(constantsMap['X']).to.have.property('type', 'const');
		expect(constantsMap['X']).to.have.property('value', 0);

		expect(constantsMap).to.have.property('Y');
		expect(constantsMap['Y']).to.have.property('type', 'const');
		expect(constantsMap['Y']).to.have.property('value', 2);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'range');
		expect(constantsMap['N']).to.have.property('start', 0);
		expect(constantsMap['N']).to.have.property('end', 2);
	});

	it('Should parse \' range M = 0..2 range N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('range M = 0..2 range N = M');
		expect(constantsMap).to.have.property('M');
		expect(constantsMap['M']).to.have.property('type', 'range');
		expect(constantsMap['M']).to.have.property('start', 0);
		expect(constantsMap['M']).to.have.property('end', 2);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'range');
		expect(constantsMap['N']).to.eql(constantsMap['M']);
	});

	it('Should parse \' const X = 0 const Y = 2 range M = X..Y range N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('const X = 0 const Y = 2 range M = X..Y range N = M');
		expect(constantsMap).to.have.property('X');
		expect(constantsMap['X']).to.have.property('type', 'const');
		expect(constantsMap['X']).to.have.property('value', 0);

		expect(constantsMap).to.have.property('Y');
		expect(constantsMap['Y']).to.have.property('type', 'const');
		expect(constantsMap['Y']).to.have.property('value', 2);

		expect(constantsMap).to.have.property('M');
		expect(constantsMap['M']).to.have.property('type', 'range');
		expect(constantsMap['M']).to.have.property('start', 0);
		expect(constantsMap['M']).to.have.property('end', 2);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'range');
		expect(constantsMap['N']).to.eql(constantsMap['M']);
	});

});

describe('Grammar to AST Set Tests', function(){

	it('Should parse \'set N = {a}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	})

	it('Should parse \'set N = {a, b, c}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a, b, c}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a', 'b', 'c'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	})

	it('Should parse \'set N = {[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {[0..2]}');
		expect(constantsMap).to.have.property('N');

		var expected = ['[0]', '[1]', '[2]'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set N = {a[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2]}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a[0]', 'a[1]', 'a[2]'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set N = {a.[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2]}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a.[0]', 'a.[1]', 'a.[2]'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set N = {a.[0..2]b}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2]b}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a.[0]b', 'a.[1]b', 'a.[2]b'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set N = {a.[0..2].b}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a.[0..2].b}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a.[0].b', 'a.[1].b', 'a.[2].b'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set N = {[0..2][0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {[0..2][0..2]}');
		expect(constantsMap).to.have.property('N');

		var expected = ['[0][0]', '[0][1]', '[0][2]', '[1][0]', '[1][1]', '[1][2]', '[2][0]', '[2][1]', '[2][2]'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set N = {a[0..2][0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2][0..2]}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a[0][0]', 'a[0][1]', 'a[0][2]', 'a[1][0]', 'a[1][1]', 'a[1][2]', 'a[2][0]', 'a[2][1]', 'a[2][2]'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set N = {a[0..2]b[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2]b[0..2]}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a[0]b[0]', 'a[0]b[1]', 'a[0]b[2]', 'a[1]b[0]', 'a[1]b[1]', 'a[1]b[2]', 'a[2]b[0]', 'a[2]b[1]', 'a[2]b[2]'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set N = {a[0..2].b[0..2]}\' correctly', function(){
		var constantsMap = constructConstantsMap('set N = {a[0..2].b[0..2]}');
		expect(constantsMap).to.have.property('N');

		var expected = ['a[0].b[0]', 'a[0].b[1]', 'a[0].b[2]', 'a[1].b[0]', 'a[1].b[1]', 'a[1].b[2]', 'a[2].b[0]', 'a[2].b[1]', 'a[2].b[2]'];
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(expected);
	});

	it('Should parse \'set M = {a, b, c} set N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('set M = {a, b, c} set N = M');
		expect(constantsMap).to.have.property('M');
		
		var expected = ['a', 'b', 'c'];
		expect(constantsMap['M']).to.have.property('type', 'set');
		expect(constantsMap['M']).to.have.property('set');
		expect(constantsMap['M'].set).to.eql(expected);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(constantsMap['M'].set);
	});

	it('Should parse \'set M = {[0..2][0..2]} set N = M\' correctly', function(){
		var constantsMap = constructConstantsMap('set M = {[0..2][0..2]} set N = M');
		expect(constantsMap).to.have.property('M');
		var expected = ['[0][0]', '[0][1]', '[0][2]', '[1][0]', '[1][1]', '[1][2]', '[2][0]', '[2][1]', '[2][2]'];
		expect(constantsMap['M']).to.have.property('type', 'set');
		expect(constantsMap['M']).to.have.property('set');
		expect(constantsMap['M'].set).to.eql(expected);

		expect(constantsMap).to.have.property('N');
		expect(constantsMap['N']).to.have.property('type', 'set');
		expect(constantsMap['N']).to.have.property('set');
		expect(constantsMap['N'].set).to.eql(constantsMap['M'].set);
	});

});

}

runConstantTests();