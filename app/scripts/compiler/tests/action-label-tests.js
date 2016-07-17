var assert = chai.assert;

function runActionLabelTests(){

var processTypes = ['automata', 'petrinet'];

/**
 * Helper function for tests which constructs and returns an ast
 * from the specified code.
 *
 * @param {string} code - the code to generate ast from
 * @param {node[]} - the ast generated
 */
function constructProcesses(code){
	var tokens = lexer.parse(code);
	return parse(tokens).processes;
}

describe('Grammar to AST Action Label Tests', function(){

	for(var i = 0; i < processTypes.length; i++){
		var processType = processTypes[i];
		var processes;

		it('[' + processType + ' test] - Should parse the action label \'a\' successfully', function() {
			processes = constructProcesses(processType + ' A = (a -> STOP).');
			testActionLabelNode(processes[0].process.from, 'a');
		});

		it('[' + processType + ' test] - Should parse the action label \'a\' successfully', function() {
			processes = constructProcesses(processType + ' A = (abc -> STOP).');
			testActionLabelNode(processes[0].process.from, 'abc');
		});

		it('[' + processType + ' test] - Should parse the action label \'aBc\' successfully', function() {
			processes = constructProcesses(processType + ' A = (aBc -> STOP).');
			testActionLabelNode(processes[0].process.from, 'aBc');
		});		

		it('[' + processType + ' test] - Should parse the action label \'aB_C\' successfully', function() {
			processes = constructProcesses(processType + ' A = (aB_C -> STOP).');
			testActionLabelNode(processes[0].process.from, 'aB_C');
		});

		it('[' + processType + ' test] - Should parse the action label \'a\' successfully', function() {
			processes = constructProcesses(processType + ' A = (ab_C12_3 -> STOP).');
			testActionLabelNode(processes[0].process.from, 'ab_C12_3');
		});

		it('[' + processType + ' test] - Should parse the action label \'a.b_C12_3\' successfully', function() {
			processes = constructProcesses(processType + ' A = (a.b_C12_3 -> STOP).');
			testActionLabelNode(processes[0].process.from, 'a.b_C12_3');
		});				

		it('[' + processType + ' test] - Should parse the action label \'[1]\' successfully', function() {
			processes = constructProcesses(processType + ' A = ([1] -> STOP).');
			testActionLabelNode(processes[0].process.from, '[1]');
		});

		it('[' + processType + ' test] - Should parse the action label \'a\' 1][2]uccessfully', function() {
			processes = constructProcesses(processType + ' A = ([1][2] -> STOP).');
			testActionLabelNode(processes[0].process.from, '[1][2]');
		});	

		it('[' + processType + ' test] - Should parse the action label \'[1][2][4 - 1]\' successfully', function() {
			processes = constructProcesses(processType + ' A = ([1][2][4 - 1] -> STOP).');
			testActionLabelNode(processes[0].process.from, '[1][2][$<v0>]');
		});	

		it('[' + processType + ' test] - Should parse the action label \'[1].[2]\' successfully', function() {
			processes = constructProcesses(processType + ' A = ([1].[2] -> STOP).');
			testActionLabelNode(processes[0].process.from, '[1].[2]');
		});	

		it('[' + processType + ' test] - Should parse the action label \'[1].[2].[4 - 1]\' successfully', function() {
			processes = constructProcesses(processType + ' A = ([1].[2].[4 - 1] -> STOP).');
			testActionLabelNode(processes[0].process.from, '[1].[2].[$<v0>]');
		});	

		it('[' + processType + ' test] - Should parse the action label \'a[1]\' successfully', function() {
			processes = constructProcesses(processType + ' A = (a[1] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'a[1]');
		});

		it('[' + processType + ' test] - Should parse the action label \'a[1]b[2]c[3] \' successfully', function() {
			processes = constructProcesses(processType + ' A = (a[1]b[2]c[3] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'a[1]b[2]c[3]');
		});

		it('[' + processType + ' test] - Should parse the action label \'aB[1][3-2]c[4]\' successfully', function() {
			processes = constructProcesses(processType + ' A = (aB[1][3-2]c[4] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'aB[1][$<v0>]c[4]');
		});		

		it('[' + processType + ' test] - Should parse the action label \'aB_[1][5-2]cC[8]\' successfully', function() {
			processes = constructProcesses(processType + ' A = (aB_[1][5-2]cC[8] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'aB_[1][$<v0>]cC[8]');
		});

		it('[' + processType + ' test] - Should parse the action label \'a[1]b_C[2][3-2]c12_[5][3]\' successfully', function() {
			processes = constructProcesses(processType + ' A = (a[1]b_C[2][3-2]c12_[5][3] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'a[1]b_C[2][$<v0>]c12_[5][3]');
		});

		it('[' + processType + ' test] - Should parse the action label \'a.[1]\' successfully', function() {
			processes = constructProcesses(processType + ' A = (a.[1] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'a.[1]');
		});

		it('[' + processType + ' test] - Should parse the action label \'a.[1].b.[2].c.[3]\' successfully', function() {
			processes = constructProcesses(processType + ' A = (a.[1].b.[2].c.[3] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'a.[1].b.[2].c.[3]');
		});

		it('[' + processType + ' test] - Should parse the action label \'aB.[1].[3-2].c.[4]\' successfully', function() {
			processes = constructProcesses(processType + ' A = (aB.[1].[3-2].c.[4] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'aB.[1].[$<v0>].c.[4]');
		});		

		it('[' + processType + ' test] - Should parse the action label \'aB_.[1].[5-2].cC.[8] \' successfully', function() {
			processes = constructProcesses(processType + ' A = (aB_.[1].[5-2].cC.[8] -> STOP).');
			testActionLabelNode(processes[0].process.from, 'aB_.[1].[$<v0>].cC.[8]');
		});

		it('[' + processType + ' test] - Should parse the action label \'a.[1].b_C[2].[3-2].a12_[5].c3\' successfully', function() {
			processes = constructProcesses(processType + ' A = (a.[1].b_C[2].[3-2].a12_[5].c3 -> STOP).');
			testActionLabelNode(processes[0].process.from, 'a.[1].b_C[2].[$<v0>].a12_[5].c3');
		});

		/**
		 * Runs this test after each of the other tests in this block has run to
		 * ensure that processes array has been constructed correctly by the parser.
		 */
		after(function(){
			expect(processes).to.have.lengthOf(1);
			testProcessNode(processes[0], processType, 'A', false);
		});
	}
});

describe('Grammar to AST Action Range Tests', function(){

	for(var i = 0; i < processTypes.length; i++){
		var processType = processTypes[i];
		var processes;

		it('[' + processType + ' test] - Should parse the action range \'[0..2]\' successfully', function(){
			processes = constructProcesses(processType + ' A = ([0..2] -> STOP).');
			var range = { type:'range', start:0, end:2 };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[a:0..2]\' successfully', function(){
			processes = constructProcesses(processType + ' A = ([a:0..2] -> STOP).');
			var range = { type:'range', start:0, end:2 };
			var variable = '$a';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[-10..-5]\' successfully', function(){
			processes = constructProcesses(processType + ' A = ([-10..-5] -> STOP).');
			var range = { type:'range', start:-10, end:-5 };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[a:-10..-5]\' successfully', function(){
			processes = constructProcesses(processType + ' A = ([a:-10..-5] -> STOP).');
			var range = { type:'range', start:-10, end:-5 };
			var variable = '$a';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[N..2]\' successfully', function(){
			processes = constructProcesses('const N = 0 ' + processType + ' A = ([N..2] -> STOP).');
			var range = { type:'range', start:0, end:2 };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[0..N]\' successfully', function(){
			processes = constructProcesses('const N = 2 ' + processType + ' A = ([0..N] -> STOP).');
			var range = { type:'range', start:0, end:2 };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[X..Y]\' successfully', function(){
			processes = constructProcesses('const X = 0 const Y = 2 ' + processType + ' A = ([X..Y] -> STOP).');
			var range = { type:'range', start:0, end:2 };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});	

		it('[' + processType + ' test] - Should parse the action range \'[N]\' successfully', function(){
			processes = constructProcesses('range N = -5..15 ' + processType + ' A = ([N] -> STOP).');
			var range = { type:'range', start:-5, end:15 };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});	

		it('[' + processType + ' test] - Should parse the action range \'[test:N]\' successfully', function(){
			processes = constructProcesses('range N = -5..15 ' + processType + ' A = ([test:N] -> STOP).');
			var range = { type:'range', start:-5, end:15 };
			var variable = '$test';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});	

		it('[' + processType + ' test] - Should parse the action range \'[{a, b, c}]\' successfully', function(){
			processes = constructProcesses(processType + ' A = ([{a, b, c}] -> STOP).');
			var range = { type:'set', set:['a', 'b', 'c'] };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[{[0..2]}]\' successfully', function(){
			processes = constructProcesses(processType + ' A = ([{[0..2]}] -> STOP).');
			var range = { type:'set', set:['[0]', '[1]', '[2]'] };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[{a[0..2]}]\' successfully', function(){
			processes = constructProcesses(processType + ' A = ([{a[0..2]}] -> STOP).');
			var range = { type:'set', set:['a[0]', 'a[1]', 'a[2]'] };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[N]\' successfully', function(){
			processes = constructProcesses('set N = {a, b, c} ' + processType + ' A = ([N] -> STOP).');
			var range = { type:'set', set:['a', 'b', 'c'] };
			var variable = '$<v0>';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		it('[' + processType + ' test] - Should parse the action range \'[test:N]\' successfully', function(){
			processes = constructProcesses('set N = {a, b, c, d} ' + processType + ' A = ([test:N] -> STOP).');
			var range = { type:'set', set:['a', 'b', 'c', 'd'] };
			var variable = '$test';
			hasActionRange(processes[0], variable, range, '[' + variable + ']');
		});

		/**
		 * Runs this test after each of the other tests in this block has run to
		 * ensure that processes array has been constructed correctly by the parser.
		 */
		after(function(){
			expect(processes).to.have.lengthOf(1);
			testProcessNode(processes[0], processType, 'A', false);
		});

		/**
		 * Helper function for tests that eliminates duplicate code. The specified
		 * process is expected to be an index ast node, containing a variable property,
		 * a range property which is either a range or set and a process property.
		 *
		 * @param {astnode} process - the index ast node to process
		 * @param {string} variable - the expected variable name
		 * @param {object} range - the expected range/set object
		 * @param {string} actionLabel - the expected action label 
		 */
		function hasActionRange(process, variable, range, actionLabel){
			expect(process).to.have.property('process');
			expect(process.process).to.have.property('type', 'index');
			expect(process.process).to.have.property('variable', variable);
			expect(process.process).to.have.property('range');
			expect(process.process.range).to.eql(range);
			hasActionLabel(process.process, actionLabel);
		}

		/**
		 * Helper function for tests that eliminates dulpicate code. The specified
		 * process is expected to be a sequence ast node, with the from property of the
		 * sequence node being an action label and the to property being a terminal
		 * node.
		 *
		 * @param {sequence node} process - the sequence ast node to process
		 * @param {string} actionLabel - the expected action label
		 */
		function hasActionLabel(process, actionLabel){
			expect(process).to.have.property('process');
			expect(process.process).to.have.property('type', 'sequence');
			expect(process.process).to.have.property('from');
			expect(process.process.from).to.have.property('type', 'action-label');
			expect(process.process.from).to.have.property('action', actionLabel);
			expect(process.process).to.have.property('to');
			expect(process.process.to).to.have.property('type', 'terminal');
			expect(process.process.to).to.have.property('terminal', 'STOP');
		}
	}
});

}

runActionLabelTests();