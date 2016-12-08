(function(document) {
  'use strict';
  var app = document.querySelector('#app');

  window.addEventListener('WebComponentsReady', function() {
    /**
     * The data to use.
     */
    //For settings, defaults are set here.
    app.automata = {values: [],display:[]};
    app.liveCompiling = true;
    app.liveBuilding = true;
    app.fairAbstraction = true;
    app.helpDialogSelectedTab = 0;
    app.currentBuild = {};
    app.previousBuild = {};
    app.previousCode = '';
    app.currentFile = '';
    app.selectedCtx = 0;
    app.isClientSide = true;
    app.willSaveCookie = true;

    if (typeof io !== 'undefined') {
      app.socket = io();
      app.socket.on('connectedToServer', ()=>app.isClientSide = false);
      app.socket.on('log',data => {
        if (data.clear) app.$.console.clear();
        app.$.console.log(data.message);
      });
    }
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

      app.build(overrideBuild);
    };


    /**
     * Runs the compiler converting the code in the editor into visualisable
     * graphs and calls the renderer.
     */
    app.build = function(override) {
      app.$.console.clear();
      setTimeout(function() {
        if(app.liveCompiling === true || override){
          app.lastCompileStartTime = (new Date()).getTime();
          const code = app.$.editor.getCode();
          const settings = app.getSettings();
          app.$.parser.compile(code, settings);
        }
      }.bind(this), 0);
    }
    app.finalizeBuild = function(results) {
      if(results.type === 'error'){
        if (results.stack) {
          app.$.console.error("An exception was thrown that was not related to your script.");
          app.$.console.error(results.stack);
          console.log(results.stack);
        } else {
          app.$.console.error(results.message);
        }
        return;
      }
      //No matter how we get here, the graphs will have been converted to json and will lose all their
      //structure, as we can compile locally and the worker will destroy it, or remotely and the server
      //will destroy it.
      const graphs = [];
      for(let id in results.processes){
        const graph = results.processes[id];
        if (graph.type === 'automata') {
          graphs.push(AUTOMATON.convert(graph));
        } else if (graph.type === 'petrinet') {
          graphs.push(PETRI_NET.convert(graph));
        }

      }
      app.set('automata.values', graphs.reverse());
      app.set('automata.analysis',results.analysis);
      app.$.console.clear();
      app.$.console.log('Successfully Compiled!');
      if(results.operations.length !== 0){
        var passed = 0;
        app.$.console.log('Operations:');
        for(var i = 0; i < results.operations.length; i++){
          var { operation, process1, process2, result } = results.operations[i];
          var op = process1 + ' ' + operation + ' ' + process2 + ' = ' + result;
          if(result){
            passed++;
          }

          app.$.console.log(op);
        }

        if(passed === results.operations.length){
          app.$.console.log('All operations passed!');
        }
        else{
          app.$.console.log(passed + '/' + results.operations.length + ' operations passed');
        }
      }

      app.$.console.log("Compiled in: "+(((new Date()).getTime()-app.lastCompileStartTime)/1000)+" seconds");
    }
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

    app.getSettings = function() {
      return {
        isFairAbstraction: app.fairAbstraction,
        isLocal: app.isClientSide
      };
    }

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

        // Load file into editor
        var input = e.target;
        app.currentFile = input.files[0];
        app.reloadFile();
        opener.value = '';

        // Enable reload button
        var reload = app.$['reload'];
        reload.disabled = false;
      };
    };

    /**
     * Reload the last used file.
     */
    app.reloadFile = function() {
      if (app.currentFile === '') {
        return;
      }

      var reader = new FileReader();
      reader.onload = function() {
        var text = reader.result;
        app.$.editor.setCode(text);
        app.$.editor.focus();
      };
      reader.readAsText(app.currentFile);
    }

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
      localStorage.setItem("liveCompiling",app.liveCompiling);
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
      localStorage.setItem("liveBuilding",app.liveBuilding);
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
      localStorage.setItem("fairAbstraction",app.fairAbstraction);
    });

    app.$['chbx-save-cookie'].addEventListener('iron-change', function() {
      localStorage.setItem("willSave",app.willSaveCookie);
    });
    /**
     * Simple event listener for when the user switches tabs.
     * When we switch to index 1 (Diagram), we need to redraw the canvas,
     * as it needs to be showing currently to render.
     * If we switch to the editor, request focus on it.
     */
    app.$['maintabs'].addEventListener('iron-select', function (e) {
      if (app.$.maintabs.selected === 1) {
        app.$.visualiser.redraw();
      } else if (app.$.maintabs.selected === 0) {
        app.$.editor._editor.focus();
      }
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
      localStorage.setItem("editor",encodeURIComponent(app.$.editor.getCode()));
      if (app.liveCompiling) {
        app.compile();
      }
    });
    app.willSaveCookie = localStorage.getItem("willSave")!=='false';
    app.liveBuilding = localStorage.getItem("liveBuilding")!=='false';
    app.liveCompiling = localStorage.getItem("liveCompiling")!=='false';
    if (app.willSaveCookie && localStorage.getItem('editor') != null) {
      app.$.editor.setCode(decodeURIComponent(localStorage.getItem('editor')));
    }
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
