(function(document) {
  'use strict';
  var app = document.querySelector('#app');

  app.compileNoSocket = ()=> {
    app.loaded = true;
  };
  window.addEventListener('WebComponentsReady', function() {
    /**
     * The data to use.
     */
    app.automata = {values: [],display:[]};
    app.liveCompiling = true;
    app.fairAbstraction = true;
    app.helpDialogSelectedTab = 0;
    app.currentBuild = {};
    app.previousBuild = {};
    app.previousCode = '';
    app.selectedCtx = 0;
    app.isClientSide = true;
    app.willSaveCookie = true;
    app.graphSettings = {autoMaxNode: 100, petriMaxPlace:100, petriMaxTrans: 100};
    app.loaded = app.loaded || false;
    app.saveSettings = {currentFile: '', saveCode: true, saveLayout: true};
    app.decoder = new TextDecoder("UTF-8");
    if (typeof io !== 'undefined') {
      app.socket = io();
      app.socket.on('connect', ()=>{
        app.isClientSide = false;
        app.loaded = true;
        if (app.liveCompiling)
          app.compile();
      });
      app.socket.on('log',data => {
        if (data.clear) app.$.console.clear();
        app.$.console.log(data.message);
      });
      app.socket.on('disconnect', function() {
        app.isClientSide = true;
        app.$.console.log("You have been disconnected from the server.");
        app.$.console.log("As a result, your last compilation may not have completed successfully.");
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
      const skipped = [];
      for(let id in results.processes){
        const graph = results.processes[id];
        if (graph.dontRender) continue;
        if (graph.type === 'automata') {
          if (graph.nodeCount > app.graphSettings.autoMaxNode) {
            skipped.push({id: graph.id, length: graph.nodeCount, type:"nodes",maxLength: app.graphSettings.autoMaxNode})
            continue;
          }
          graphs.push(graph);
        } else if (graph.type === 'petrinet') {
          if (graph.placeCount > app.graphSettings.petriMaxPlace) {
            skipped.push({id: graph.id, length: graph.placeCount, type:"places", maxLength: app.graphSettings.petriMaxPlace})
            continue;
          }
          if (graph.transitionCount > app.graphSettings.petriMaxTrans) {
            skipped.push({id: graph.id, length: graph.transitionCount, type:"transitions",maxLength: app.graphSettings.petriMaxTrans})
            continue;
          }
          graphs.push(graph);
        }

      }
      app.set('automata.values', graphs.reverse());
      app.set('automata.analysis',results.analysis);
      app.$.console.clear();
      app.$.console.log('Successfully Compiled!');
      if(results.operations.length !== 0){
        let passed = 0;
        app.$.console.log('Operations:');
        for(let i = 0; i < results.operations.length; i++){
          const { operation, process1, process2, result } = results.operations[i];
          const op = process1 + ' ' + operation + ' ' + process2 + ' = ' + result;
          if(result){
            app.$.console.log(op);
            passed++;
          }
          else{
            app.$.console.error(op);
          }
        }

        if(passed === results.operations.length){
          app.$.console.log('All operations passed!');
        }
        else{
          app.$.console.log(passed + '/' + results.operations.length + ' operations passed');
        }
      }

      app.$.console.log("Compiled in: "+(((new Date()).getTime()-app.lastCompileStartTime)/1000)+" seconds");
      _.each(skipped, skip=> {
        app.$.console.log("Skipped adding "+skip.id+" to the render list, as it has too many "+skip.type);
      });
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
        isLocal: app.isClientSide,
        pruneAbstraction: true
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
        app.saveSettings.currentFile = input.files[0];
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
        var text = reader.result.split("visualiser_json_layout:");
        var code = text[0];
        var json = text[1];
        if (json.length > 0) {
          app.$.visualiser.loadJSON(json);
        }
        app.$.editor.setCode(code);
        app.$.editor.focus();
      };
      reader.readAsText(app.saveSettings.currentFile);
    }

    /**
     * Save to code the user has written to their computer (as a download).
     */
    app.downloadFile = function() {
      var filename = app.$.save.getFileName();
      // if filename has not been defined set to untitled
      if(filename === ''){
        filename = 'untitled';
      }
      app.saveSettings = {currentFile: '', saveCode: true, saveLayout: true};
      var output = "";
      if (app.saveSettings.saveCode)
        output+= app.$.editor.getCode();
      output+="\nvisualiser_json_layout:"
      if (app.saveSettings.saveLayout)
        output+= JSON.stringify(app.$.visualiser.jgraph.toJSON());
      var blob = new Blob(
        [output],
        {type: 'text/plain;charset=utf-8'});
      saveAs(blob, filename + '.txt');
    };

    /**
     * Opens the help-dialog.
     */
    app.showHelp = function() {
      app.$.help.open();
    };
    app.showSettings = function() {
      app.$.settings.open();
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
     * Simple event listener for when the fair abstraction checkbox is ticked.
     * Compile is called every time the checkbox is ticked or unticked.
     */
    app.$['chbx-fair-abstraction'].addEventListener('iron-change', function() {
      app.compile(true);
      app.$.editor.focus();
      localStorage.setItem("fairAbstraction",app.fairAbstraction);
    });

    $("#settings-dialog")[0].addEventListener('iron-overlay-closed', function() {
      app.saveGraphSettings();
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
      if (app.liveCompiling && app.loaded) {
        app.compile();
      }
    });
    document.addEventListener('console-change', function(e) {
      if (e.target.id==='console') {
        const detail = e.detail;
        if (detail.msg) {
          app.$.visualiserConsole[detail.type](detail.msg);
        } else if (detail.clear) {
          app.$.visualiserConsole.clear(detail.lines);
        }
      }
    });
    app.willSaveCookie = localStorage.getItem("willSave")!=='false';
    app.liveCompiling = localStorage.getItem("liveCompiling")!=='false';
    app.graphSettings = localStorage.getItem("graphSettings") !== null?JSON.parse(localStorage.getItem("graphSettings")) :  app.graphSettings;
    app.saveGraphSettings = ()=>localStorage.setItem("graphSettings",JSON.stringify(app.graphSettings));
    if (app.willSaveCookie && localStorage.getItem('editor') != null) {
      app.$.editor.setCode(decodeURIComponent(localStorage.getItem('editor')));
    }
    /**
     * Listen for key presses.
     * Note: Needs to listen for keydown (not keyup) in order to prevent browser default action
     */
    document.addEventListener('keydown',function(e) {
      if (app.$.help.opened()) {
        // CTRL + S
        if (e.keyCode == 83 && e.ctrlKey) {
          //Even if the help dialog is open, we don't want the default save dialog to show.
          e.preventDefault();
        }
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
            app.$.save.open();
            e.preventDefault();
          }
          break;
        case 112:
          // F1
          app.$.help.open();
          e.preventDefault();
          break;
        default: return;
      }
    });
  });
})(document);
