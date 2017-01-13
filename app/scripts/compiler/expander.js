'use strict';

/**
 * Takes the sepcified abstract syntax tree and expands out any shorthand syntax in the grammar
 * with its longhand equivalent. For example:
 *
 * automata A = ([1..2] -> STOP). === automata A = ([1] -> STOP | [2] -> STOP).
 *
 *
 * This saves potentially having to process local references twice later on in the analysis and
 * interpretation stages of the compiler.
 *
 * @param {ast} ast - the abstract syntax tree containing defined processes and the variable map
 * @return {ast} - the ast containing expanded versions of the processes defined
 */
function expand(ast){
  var processes = ast.processes;
  var localProcess;
  // expand the defined processes

  for(var i = 0; i < processes.length; i++){
    if (typeof postMessage === 'function') {
      if (processes[i].ident.ident.startsWith("op")) {
        let tmp = processes[i].process;
        let tmpId = "";
        let br = "";
        while (tmp.type == "function") {
          br += ")";
          tmpId = tmpId+tmp.func+"(";
          tmp = tmp.process;
        }
        tmpId = tmpId+tmp.ident+br;
        postMessage({
          clear: true,
          message: ("Expanding Operation: " + tmpId + " (" + (i + 1) + "/" + processes.length) + ")"
        });

      } else {
        postMessage({
          clear: true,
          message: ("Expanding: " + processes[i].ident.ident + " (" + (i + 1) + "/" + processes.length) + ")"
        });
      }
    }
    if (processes[i].ident.ident.indexOf("*") > -1) {
      processes[i].ident.dontRender = true;
      processes[i].ident.ident = processes[i].ident.ident.replace("*","");
    }
    var variableMap = JSON.parse(JSON.stringify(ast.variableMap));
    processes[i].process = expandNode(processes[i].process, variableMap);
    // expand local procsses if any are defined
    if(processes[i].local.length !== 0){
      variableMap = JSON.parse(JSON.stringify(ast.variableMap));
      processes[i].local = expandLocalProcessDefinitions(processes[i].local, variableMap);
    }
  }
  // return the result
  return ast;
  /**
   * Expands and returns the local processes defined within a process.
   *
   * @param {astNode[]} localProcesses - an array of locally defined references
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode[]} - the expanded local processes
   */
  function expandLocalProcessDefinitions(localProcesses, variableMap){
    var newProcesses = [];
    for(var j = 0; j < localProcesses.length; j++){
      localProcess = localProcesses[j];
      if(localProcesses[j].ident.ranges === undefined){
        localProcesses[j].process = expandNode(localProcesses[j].process, variableMap);
        newProcesses.push(localProcesses[j]);
      }
      else{
        var ident = localProcesses[j].ident.ident;
        var ranges = localProcesses[j].ident.ranges.ranges;
        newProcesses = newProcesses.concat(expandIndexedDefinition(localProcesses[j], ident, ranges, variableMap));
      }
    }
    return newProcesses;
  }

  /**
   * Helper function for 'expandLocalProcessDefinition' that handles expanding
   * an indexed local process. Recursively processes the defined ranges so that:
   *
   * for all i in ranges[0]:
   ...
   *   for all j in ranges[n - 1]:
   *     <process local definition>
   *
   * Where '...' represents the ranges defined inbetween range[0] and ranges[n - 1] (if any).
   * This will create an individual local definition for <ident>[i]...[n - 1], where <ident>
   * is the main identifier for the specified localProcess.
   *
   * @param {astNode} localProcess - the defined local process
   * @param {string} ident - current identifier name
   * @param {rangeNode[]} ranges - the remaining ranges
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode[]} - the expanded local processes
   */
  function expandIndexedDefinition(localProcess, ident, ranges, variableMap){
    var newProcesses = [];

    // recursive case
    if(ranges.length !== 0){
      var iterator = new IndexIterator(ranges[0].range);
      var variable = ranges[0].variable;
      ranges = (ranges.length > 1) ? ranges.slice(1) : [];
      // setup construction of a local process for each iteration (processed in base case)
      while(iterator.hasNext){
        var element = iterator.next;
        variableMap[variable] = element;
        var newIdent = ident + '[' + element + ']';
        newProcesses = newProcesses.concat(expandIndexedDefinition(localProcess, newIdent, ranges, variableMap));
      }
    }
    // base case
    else{
      const vars = parseIndexedLabel(ident,variableMap);
      // construct a new locally defined process
      var clone = JSON.parse(JSON.stringify(localProcess));
      clone.ident.ident = ident;
      clone.process = expandNode(clone.process, variableMap);
      clone.process.vars = vars;
      newProcesses.push(clone);
    }

    return newProcesses;
  }

  /**
   * Takes the specified astNode and calls the correct function for processing
   * it based on its type. Not all types need to get processes, such as terminals.
   * These are simply returned at the end of the function.
   *
   * @param {astNode} astNode - the astNode to expand
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded ast node
   */
  function expandNode(astNode, variableMap){
    var type = astNode.type;
    var node;
    if(type === 'action-label'){
      node = expandActionLabelNode(astNode, variableMap);
    }
    else if(type === 'index'){
      node = expandIndexNode(astNode, variableMap);
    }
    else if(type === 'sequence'){
      node = expandSequenceNode(astNode, variableMap);
    }
    else if(type === 'choice' || type === 'composite'){
      // choice and composite nodes are structured the same (apart from type)
      node = expandChoiceOrCompositeNode(astNode, variableMap);
    }
    else if(type === 'if-statement'){
      node = expandIfStatementNode(astNode, variableMap);
    }
    else if(type === 'function'){
      node = expandFunctionNode(astNode, variableMap);
    }
    else if(type === 'identifier'){
      node = expandIdentiferNode(astNode, variableMap);
    }
    else if(type === 'forall'){
      node = expandForallNode(astNode, variableMap);
    }

    // check if the ast node did not got processed
    if(node === undefined){
      return astNode;
    }

    // return a terminal if the process produced an empty ast node
    if(node.type === 'empty'){
      return { type:'terminal', terminal:'STOP' };
    }
    return node;
  }

  /**
   * Processes the specified action label ast node so that any variable references
   * made by the action are replaced with their actual values.
   *
   * @param {astNode} astNode - the action label node to process
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded ast node
   */
  function expandActionLabelNode(astNode, variableMap){
    var lbl = processLabel(astNode.action, variableMap);
    astNode.action = lbl;
    return astNode;
  }

  /**
   * Expands the specified index astNode into a series of choice ast nodes.
   *
   * @param {astNode} astNode - the index ast node to expand
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded ast node
   */
  function expandIndexNode(astNode, variableMap){
    var iterator = new IndexIterator(astNode.range);
    var iterations = [];
    while(iterator.hasNext){
      var element = iterator.next;
      variableMap[astNode.variable] = element;
      var clone = JSON.parse(JSON.stringify(astNode.process));
      iterations.push(expandNode(clone, variableMap));
    }

    // convert the indexed processes into choice ast nodes
    var newNode = iterations.pop();
    while(iterations.length !== 0){
      var nextNode = iterations.pop();
      newNode = { type:'choice', process1:nextNode, process2:newNode };
    }

    return newNode;
  }

  function processNext(astNode, variableMap) {
    let next;
    if (astNode.to && astNode.to.ident) {
      var expr = astNode.to.ident;
      var regex = '[\$][a-zA-Z0-9]*';
      var match = expr.match(regex);
      next = [];
      while (match != null) {
        //String works perfectly for i+1, but fails for direct node.
        if (typeof variableMap[match[0]] === 'string') {
          //Append an equals sign to operators to make it obvious we are assigning
          next.push(variableMap[match[0]].substring(1).replace(new RegExp(Lexer.operators),s=>s+"="));
        }
        expr = expr.replace(match[0], variableMap[match[0]]);
        match = expr.match(regex);
      }
      if (next.length == 0)
        next = parseIndexedLabel(astNode.to.ident, variableMap, true);
    } else if (astNode.to && astNode.to.range) {
      next = astNode.to.process.from.action;
      const variable = astNode.to.variable;
      const range = astNode.to.range;
      next = [next.replace(variable,variable.substring(1)+":"+range.start+".."+range.end)];
    }
    return next;
  }

  /**
   * Expands the specified sequence ast node.
   *
   * @param {astNode} astNode - the sequence ast node to expand
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded ast node
   */
  function expandSequenceNode(astNode, variableMap){
    astNode.from = expandNode(astNode.from, variableMap);
    astNode.to = expandNode(astNode.to, variableMap);
    return astNode;
  }

  /**
   * Expands the specified choice or composite ast node.
   *
   * @param {astNode} astNode - the choice or composite ast node to expand
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded ast node
   */
  function expandChoiceOrCompositeNode(astNode, variableMap){
    astNode.process1 = expandNode(astNode.process1, variableMap);
    astNode.process2 = expandNode(astNode.process2, variableMap);

    // no need for choice or composition if one of the processes is empty
    if(astNode.process1.type === 'empty' || astNode.process1.type === 'terminal'){
      return astNode.process2;
    }
    if(astNode.process2.type === 'empty' || astNode.process2.type === 'terminal'){
      return astNode.process1;
    }

    // if both branches are empty it will be caught by the expandNode function
    return astNode;
  }

  /**
   * Expands the specified if statement ast node. Only returns the branch that
   * gets executed based on the current state of the variable map. In some cases
   * it is possible for no paths to be executed. When this happens a special
   * empty ast node is returned.
   *
   * @param {astNode} astNode - the if statement ast node to expand
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded ast node
   */
  function expandIfStatementNode(astNode, variableMap){
    var guard = processGuardExpression(astNode.guard, variableMap);
    let node;
    let next;
    if(guard.result){
      next = processNext(astNode.trueBranch,variableMap);
      node = expandNode(astNode.trueBranch, variableMap);
    } else if(astNode.falseBranch !== undefined){
      next = processNext(astNode.falseBranch,variableMap);
      node = expandNode(astNode.falseBranch, variableMap);
    } else return { type:'empty' };
    if (astNode.guard) {
      const guard = processExpression(astNode.guard, variableMap).exprWithVars;
      const variables = processVariables(variableMap[astNode.guard], variableMap);
      node.guardMetadata = {next: next, guard: guard, variables:variables};
    }
    return node;
  }
  /**
   * Pull variable names and values from a guard
   * @param {string} guard the guard
   * @param {string -> string} variableMap
   * @returns {string}
   */
  function processVariables(guard, variableMap) {
    var variables = [];
    // replace any variables declared in the expression with its value
    var regex = '[\$][a-zA-Z0-9]*';
    var match = guard.match(regex);
    while(match !== null){
      if (match[0].indexOf("v") === -1)
        variables.push(match[0].substring(1)+"="+variableMap[match[0]]);
      guard = guard.replace(match[0], variableMap[match[0]]);
      match = guard.match(regex);
    }
    return variables;
  }
  /**
   * Change from the form C[1][2] to i:=1 j:=2
   * @param {string} ident the original identifier
   * @param {string -> string} variableMap
   * @returns {string}
   */
  function parseIndexedLabel(ident, variableMap, addColon) {
    var vars = [];
    if (ident.indexOf("[")===-1) return ([ident]);
    var split = ident.substring(1).replace(/[\[']+/g,'').split("]");
    for (var index in split) {
      var val = split[index];
      //Skip variables that havent been resolved (e.g. C[$i][1])
      if (val === "" || val.indexOf("$") !== -1) continue;
      var variable = localProcess.ranges.ranges[index].variable;
      vars.push(variable.substring(1)+(addColon?":":"")+"="+val);
    }
    return vars;
  }
  /**
   * Expands the specified function ast node.
   *
   * @param {astNode} astNode - the function ast node to expand
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded ast node
   */
  function expandFunctionNode(astNode, variableMap){
    astNode.process = expandNode(astNode.process, variableMap);
    return astNode;
  }

  /**
   * Expands the specified identifier ast node.
   *
   * @param {astNode} astNode - the index ast node to expand
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded ast node
   */
  function expandIdentiferNode(astNode, variableMap){
    astNode.ident = processLabel(astNode.ident, variableMap);
    if (astNode.label) {
      astNode.label.action = processLabel( astNode.label.action, variableMap);
    }
    return astNode;
  }

  /**
   * Expans the specified forall ast node into a series of composite ast nodes.
   *
   * @param {astNode} astNode - the forall ast node to expand
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {astNode} - the expanded astNode
   */
  function expandForallNode(astNode, variableMap){
    var nodes = _expandForallNode(astNode.process, astNode.ranges.ranges, variableMap);
    astNode = nodes.pop();
    while(nodes.length !== 0){
      var next = nodes.pop();
      astNode = { type:'composite', process1:next, process2:astNode };
    }
    return astNode;

    /**
     * A helper function for expandForallNode that processes the ranges defined
     * in the forall ast node.
     *
     * @param {astNode} process - the defined process
     * @param {range[]} ranges - the remaining ranges
     * @param {string -> string} variableMap - a mapping from variable name to value
     * @param {astNode[]} - the processed ast nodes
     */
    function _expandForallNode(process, ranges, variableMap){
      var newNodes = [];
      // recursive case
      if(ranges.length !== 0){
        var iterator = new IndexIterator(ranges[0].range);
        var variable = ranges[0].variable;
        ranges = (ranges.length > 1) ? ranges.slice(1) : [];

        while(iterator.hasNext){
          variableMap[variable] = iterator.next;
          newNodes = newNodes.concat(_expandForallNode(process, ranges, variableMap));
        }
      }
      // base case
      else{
        var clone = JSON.parse(JSON.stringify(process));
        clone = expandNode(clone, variableMap);
        newNodes.push(clone);
      }

      return newNodes;
    }
  }

  /**
   * Processes the specified expression by replacing any variable references
   * with the value that variable represents and evaluating the result. Throws
   * an error if a variable is found to be undefined.
   *
   * @param {string} expr - the expr to evaluate
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {int} - result of the evaluation
   */
  function processExpression(expr, variableMap){
    // replace any variables declared in the expression with its value
    var regex = '[\$][a-zA-Z0-9]*';
    var match = expr.match(regex);
    var exprWithVars = expr;
    while(match !== null){
      // check if the variable has been defined
      if(variableMap[match[0]] === undefined){
        throw new VariableDeclarationException('the variable \'' + match[0].substring(1) + '\' has not been defined');
      }
      if (match[0].indexOf("v")!=-1) {
        exprWithVars=exprWithVars.replace(match[0],"("+variableMap[match[0]]+")");
      }
      expr = expr.replace(match[0], variableMap[match[0]]);
      match = expr.match(regex);
    }
    //Web workers can not use eval and evaluate. This is an alternative.
    return {result:new Function('return '+expr)(),expr:expr,exprWithVars:exprWithVars.split("$").join("")};
  }

  /**
   * Processes the specified label by replacing any variable references
   * with the value that variable represents. Throws an error if a variable
   * is found to be undefined.
   *
   * @param {string} label - the label to process
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {string} - the processed label
   */
  function processLabel(label, variableMap){
    // replace any variables declared in the label with its value
    var regex = '[\$][a-zA-Z0-9]*';
    var match = label.match(regex);

    while(match !== null){
      //Normal variable replacement
      var expr = processExpression(match[0], variableMap);
      label = label.replace(match[0], expr.result);
      match = label.match(regex);
    }
    return label;
  }

  /**
   * Processes the specified guard expression by replacing any variable references
   * with the value that variable represents and evaluating the result. Throws
   * an error if a variable is found to be undefined.
   *
   * @param {string} expr - the expr to evaluate
   * @param {string -> string} variableMap - a mapping from variable name to value
   * @return {boolean} - result of the evaluation
   */
  function processGuardExpression(expr, variableMap){
    expr = processExpression(expr, variableMap);
    return expr;
  }

  /**
   * Constructs and returns a 'VariableDeclarationException' based off of the
   * specified message. Also contains the location in the code being parsed
   * where the error occured.
   *
   * @param {string} message - the cause of the exception
   * @param {object} location - the location where the exception occured
   */
  function VariableDeclarationException(message, location){
    this.message = message;
    this.location = location;
    this.toString = function(){
      return 'VariableDeclarationException: ' + message;
    };
  }
}
