(function(document) {
  'use strict';

  var app = document.querySelector('#app');

  window.addEventListener('WebComponentsReady', function() {

    /**
     * The data to use.
     */
    app.automata = {values: []};

    app.liveCompiling = true;

    app.helpDialogSelectedTab = 0;

    /**
     * Compile the code in the text editor.
     * Create and display the new automata.
     */
    app.compile = function() {
      app.$.console.clear();
      app.$.console.log('Compiling...');
      var compileStartTime = (new Date()).getTime();
      var compileTime;

      setTimeout(function() {
        var code = app.$.editor.getCode();
        if (code.trim().length === 0) {
          app.$.console.clear();
          app.$.console.log('No input.');
          return;
        }

        var automata = [];
        try {
          automata = app.$.parser.parse(code);
        } catch (e) {
          var buildErrorMessage = function(e) {
            return e.location !== undefined ?
              'on line ' + e.location.start.line + ', col ' + e.location.start.column + ' - ' + e.message :
              e.message;
          };

          var isInterpreterException = e.constructor === app.$.parser.InterpreterException;
          var prefix = isInterpreterException ? 'Error: ' : 'Syntax error ';

          compileTime = Math.max(1, ((new Date()).getTime() - compileStartTime)) / 1000;
          app.$.console.clear(1);
          app.$.console.log('Compulation failed after ' + compileTime.toFixed(3) + ' seconds.');
          app.$.console.error(prefix + buildErrorMessage(e));
          return;
        }

        compileTime = Math.max(1, ((new Date()).getTime() - compileStartTime)) / 1000;
        app.$.console.clear(1);
        app.$.console.log('Compiled successfully in ' + compileTime.toFixed(3) + ' seconds.');
        app.$.console.log('Rendering...');

        var renderStartTime = (new Date()).getTime();
        var renderTime;

        // Can't simply assign app.automata.values to the new array as data bindings will not update.
        // Creating a new automata oject then setting the its values slightly later will work (for some reason).
        app.automata = {};
        setTimeout(function() {
          app.set('automata.values', automata);

          renderTime = Math.max(1, ((new Date()).getTime() - renderStartTime)) / 1000;
          app.$.console.clear(1);
          app.$.console.log('Rendered successfully in ' + renderTime.toFixed(3) + ' seconds.');
          app.$.console.log('Total time: ' + (compileTime + renderTime).toFixed(3) + ' seconds.');
        }, 0);
      }.bind(this), 0);
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
          app.$.editor.setCode(text, app.liveCompiling);
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
      var blob = new Blob(
        [app.$.editor.getCode()],
        {type: 'text/plain;charset=utf-8'});
      saveAs(blob, 'untitled.txt');
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
        app.compile();
      }
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
        visualisations[i].redraw();
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
        visualisations[i].redraw();
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
