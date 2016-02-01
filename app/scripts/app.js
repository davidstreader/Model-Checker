(function(document) {
  'use strict';

  var app = document.querySelector('#app');

  window.addEventListener('WebComponentsReady', function() {

    /**
     * The data to use.
     */
    app.automata = {values: []};
    app.liveCompiling = true;
    app.liveBuilding = true;
    app.fairAbstraction = true;
    app.helpDialogSelectedTab = 0;
    app.currentBuild = {};
    app.previousBuild = {};
    app.previousCode = '';

    app.compile = function(overrideBuild) {
      var code = app.getCode();

      if(!overrideBuild){
        // if there is nothing to parse then do not continue
        if(code.length === 0){
          app.$.console.clear();
          app.automata = {};
          app.previousCode = '';
          return;
        }

        // if the code has not changed then do not continue
        if(code === app.previousCode){
          return;
        }
      }

      app.build();
    };

    app.build = function() {
      app.$.console.clear();
      // parse the code
      setTimeout(function() {
        var compileStartTime = (new Date()).getTime();
        var results = app.parse();
        // check if parsing failed
        if(results == undefined){
          return;
        }

        // interpret the results of parse
        try{
          results = app.interpret(results);
        }catch(e){
          app.$.console.clear(1);
          app.$.console.log(e);
          return;
        }

        // render the automata
        app.render(results.automata);

        // print the operations
        app.operations(results.operations);
      }.bind(this), 0); 
    }

    /**
     * Compile the code in the text editor.
     * Create and display the new automata.
     */
    app.parse = function(overrideBuild) {
      app.$.console.log('Parsing...');
      var compileStartTime = (new Date()).getTime();
      var code = app.$.editor.getCode();

      var result;
      try {
        result = app.$.parser.initialParse(code);
      } catch (e) {
        var buildErrorMessage = function(e) {
          return e.location !== undefined ?
            'on line ' + e.location.start.line + ', col ' + e.location.start.column + ' - ' + e.message :
            e.message;
        };

        var isInterpreterException = e.constructor === app.$.parser.InterpreterException;
        var prefix = isInterpreterException ? 'Error: ' : 'Syntax error ';

        var compileTime = Math.max(1, ((new Date()).getTime() - compileStartTime)) / 1000;
        app.$.console.clear(1);
        app.$.console.log('Compulation failed after ' + compileTime.toFixed(3) + ' seconds.');
        app.$.console.error(prefix + buildErrorMessage(e));
        return;
      }

      var compileTime = Math.max(1, ((new Date()).getTime() - compileStartTime)) / 1000;
      app.$.console.clear(1);
      app.$.console.log('Parsed successfully in ' + compileTime.toFixed(3) + ' seconds.');
      
      return result;
    };

    app.interpret = function(results) {
      app.$.console.log('Interpreting...')
      var interpretStartTime = (new Date()).getTime();

      app.previousBuild = app.currentBuild;
      app.currentBuild = {};

      // interpret definitions
      var definitionMap = {};
      for(var key in results.processes){
        var code = results.processes[key].replace(/ /g, ' ');
        if(app.previousBuild[key] != undefined){
          if(app.previousBuild[key].code != code){
            definitionMap = app.$.parser.parseDefinition(code, definitionMap, app.liveBuilding, app.fairAbstraction);
            app.currentBuild[key] = { code: code, definition: definitionMap[key] };
            //console.log('constructing new automaton "' + key + '"');
          }
          else{
            app.currentBuild[key] = app.previousBuild[key];
            definitionMap[key] = app.previousBuild[key].definition;
            //console.log('no changes made to automaton "' + key + '"');
          }
        }
        else{
          definitionMap = app.$.parser.parseDefinition(code, definitionMap, app.liveBuilding, app.fairAbstraction);
          app.currentBuild[key] = { code: code, definition: definitionMap[key] };
          //console.log('constructing new automaton "' + key + '"');
        }
      }

      var automata = [];
      for(var key in app.currentBuild){
        // make sure no graphs over specified amount are rendered
        if(app.currentBuild[key].definition.graph.nodeCount < 100){
          automata.unshift(new Automaton(key, app.currentBuild[key].definition.graph));
        }
      }

      // interpret operations if there are any
      var operations = '';
      for(var key in results.operations){
        operations += results.operations[key] + ' ';
      }
      operations = app.$.parser.parseOperations(operations, definitionMap, app.fairAbstraction);

      var pass = 0;
      var fail = 0;
      var operationsArray = [];
      for(var i in operations){
        var input = operations[i].input;
        var result = operations[i].result;
        operationsArray.push(input + ' = ' + result);
          
        // increment the tally of results
        if(result){
          pass++;
        }
        else{
          fail++;
        }
      }

      // if there are no operations do not print the total numbe of operations
      if(operationsArray.length > 0){
        var results = 'Total Operations: ' + (pass + fail) +' (Pass: ' + pass + ', Fail: ' + fail + ')';
        operationsArray.unshift(results);
      }

      var interpretTime = Math.max(1, ((new Date()).getTime() - interpretStartTime)) / 1000;
      app.$.console.clear(1);
      app.$.console.log('Interpreted successfully after ' + interpretTime.toFixed(3) + ' seconds.');

      return { automata: automata, operations: operationsArray };
    }

    app.render = function(automata) {
      app.$.console.log('Rendering...');
      var renderStartTime = (new Date()).getTime();
      // Can't simply assign app.automata.values to the new array as data bindings will not update.
      // Creating a new automata object then setting the its values slightly later will work (for some reason).
      app.automata = {};
      setTimeout(function() {
        app.set('automata.values', automata);
        
        // listen for each rendered event.
        // once all automata have been rendered, log the results and stop listening.
        var automataRendered = 0;
        var renderComplete = function() {
          automataRendered++;
          if (automataRendered === app.automata.values.length) {
            var renderTime = Math.max(1, ((new Date()).getTime() - renderStartTime)) / 1000;
            app.$.console.clear(1);
            app.$.console.log('Rendered successfully after ' + renderTime.toFixed(3) + ' seconds.');

            document.removeEventListener('automata-visualisation-rendered', renderComplete);
          }
        };

        document.addEventListener('automata-visualisation-rendered', renderComplete);
      }.bind(this), 0)
    };

    app.operations = function(operations) {
      setTimeout(function (){
        // only print out operations results if the were any operations performed
        if(operations.length !== 0){
          app.$.console.log(' ');
          app.$.console.log('Operations:');
          var annotations = [];
          for(var i = 0; i < operations.length; i++){
            app.$.console.log(operations[i]);
          }
        }
      }.bind(this), 0);
    };

    /**
     * Compiles and builds what has currenty been entered into the text-area.
     * Ignores whether or not live compile and build are currently set.
     */
    app.compileAndBuild = function() {
      app.compile(true);
    };

    /**
     * Gets and returns the code from the editor. Strips the code of all whitespace
     * and unnecessary line breaks.
     */
    app.getCode = function() {
      var code = '';
      var temp = app.$.editor.getCode();
      
      // remove white space and line breaks
      temp = temp.replace(/ /g, '');
      
      // remove unnecessary whitespace
      var split = temp.split('\n');
      for(var i = 0; i < split.length; i++){
        if(split[i] !== ''){
          code += split[i] + '\n';
        }
      }

      return code;
    };

    /**
     * Open a text file from the user's computer and set the text-area to
     * the text parsed from the file.
     */
    app.openFile = function() {
      var opener = app.$['open-file'];
      opener.click();
      opener.onchange = function(e) {
        if (opener.value === '') {
          return;
        }
        var input = e.target;
        var reader = new FileReader();
        reader.onload = function() {
          var text = reader.result;
          app.$.editor.setCode(text);
          app.$.editor.focus();
        };
        reader.readAsText(input.files[0]);
        opener.value = '';
      };
    };

    /**
     * Save to code the user has written to their computer (as a download).
     */
    app.downloadFile = function() {
      var filename = app.$['filename'].inputElement.bindValue;
      // if filename has not been defined set to untitled
      if(filename === ''){
        filename = 'untitled';
      }

      var blob = new Blob(
        [app.$.editor.getCode()],
        {type: 'text/plain;charset=utf-8'});
      saveAs(blob, filename + '.txt');
    };

    /**
     * Opens the help-dialog.
     */
    app.showHelp = function() {
      var help = app.$['help-dialog'];
      help.open();
    };

    /**
     * Simple event listener for when the checkbox in ticked.
     * Compile is called if it is.
     */
    app.$['chbx-live-compiling'].addEventListener('iron-change', function() {
      if (app.liveCompiling) {
        app.compile(false);
      }
      app.$.editor.focus();
    });

    /**
     * Simple event listener for when the live building checkbox is ticked.
     * Compile is called if live compiling is active.
     */
    app.$['chbx-live-building'].addEventListener('iron-change', function() {
      if(app.liveCompiling){
        app.compile(false);
      }
      app.$.editor.focus();
    });

    /**
     * Simple event listener for when the fair abstraction checkbox is ticked.
     * Compile is called every time the checkbox is ticked or unticked.
     */
    app.$['chbx-fair-abstraction'].addEventListener('iron-change', function() {
      app.compile(true);
      app.$.editor.focus();
    });

    /**
     * This is the event which triggers when the user selects an automata from the
     * list to walk down. It sets the root node of this automata, and all automata
     * with this automata as a sub-graph, blue.
     */
    document.addEventListener('automata-walker-start', function(e) {
      var visualisations = Polymer.dom(this).querySelectorAll('automata-visualisation');
      for (var i in visualisations) {
        visualisations[i].setHighlightNodeId(e.detail.node.id);
      }
    });
    /**
     * This is the event which triggers when the user presses the walk
     * button on the walker element. The walker has already checked for the valid
     * edge and thrown any errors. The edge to walk is given in the event argument
     * 'e.detail.edge'.
     */
    document.addEventListener('automata-walker-walk', function(e) {
      var visualisations = Polymer.dom(this).querySelectorAll('automata-visualisation');
      for (var i in visualisations) {
        visualisations[i].setHighlightNodeId(e.detail.edge.to.id);
      }
    });

    /**
     * This is the event which triggers when the text in the text area is changed.
     * Only care about this if the live-compiling check-box is ticked.
     */
    document.addEventListener('text-editor-change', function() {
      if (app.liveCompiling) {
        app.compile();
      }
    });

    /**
     * Listen for key presses.
     * Note: Needs to listen for keydown (not keyup) in order to prevent browser default action
     */
    document.addEventListener('keydown',function(e) {
      if (app.$['help-dialog'].opened) {
        return;
      }

      switch (e.keyCode) {
        case 13:
          // CTRL + ENTER
          if (e.ctrlKey) {
            app.compile();
            e.preventDefault();
          }
          break;
        case 79:
          // CTRL + O
          if (e.ctrlKey) {
            app.openFile();
            e.preventDefault();
          }
          break;
        case 83:
          // CTRL + S
          if (e.ctrlKey) {
            app.downloadFile();
            e.preventDefault();
          }
          break;
        case 112:
          // F1
          app.$['help-dialog'].open();
          e.preventDefault();
          break;
        default: return;
      }
    });

  });
})(document);
