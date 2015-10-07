(function(document) {
  'use strict';

  var app = document.querySelector('#app');

  window.addEventListener('WebComponentsReady', function() {

    /**
     * The data to use.
     */
    app.data = {automatas: []};
    
    app.liveCompiling = true;

    /**
     * Compile the code in the text editor.
     * Create and display the new automatas.
     */

    app.compile = function() {
      app.$.console.clear();
      if (app.$.editor.getCode().trim().length === 0) {
        app.$.console.log('No input.');
        return;
      }
      var success = false;
      try {
        var timeBefore = (new Date()).getTime();
        var automatas = app.$.parser.parse(app.$.editor.getCode());
        var timeAfter = (new Date()).getTime();
        success = true;
        var timeTaken = Math.max(1, (timeAfter - timeBefore)) / 1000;
        app.$.console.log('Compiled successfully in ' + timeTaken.toFixed(3) + ' seconds.');
      } catch (e) {
        var buildErrorMessage = function(e) {
          return e.location !== undefined ?
            'on line ' + e.location.start.line + ', col ' + e.location.start.column + ' - ' + e.message :
            e.message;
        };

        var isInterpreterException = e.constructor === app.$.parser.InterpreterException;
        var prefix = isInterpreterException ? 'Error: ' : 'Syntax error ';

        app.$.console.error(prefix + buildErrorMessage(e));
      }
      if (success) {
        // Can't simply assign app.data.automatas to the new array as data bindings will not update.
        // Creating a new data oject then setting the automatas value slighly later will work (for some reason).
        app.data = {};
        this.async(function() {
          app.set('data.automatas', automatas);
        });
      }
    };

    /**
     * Open a text file from the user's computer and use its contents as the code
     */
    app.open = function() {
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
        };
        reader.readAsText(input.files[0]);
        opener.value = '';
      };
    };

    /**
     * Save to code the user has written to their computer (as a download).
     */
    app.save = function() {
      var blob = new Blob(
        [app.$.editor.getCode()],
        {type: 'text/plain;charset=utf-8'});
      saveAs(blob, 'untitled.txt');
    };

    app.help = function() {
      var helptext = app.$['help'];
      helptext.open();
    };

    app.closehelp = function() {
      var helptext = app.$['help'];
      helptext.close();
    };

    app.$['live-compiling-check'].addEventListener('iron-change', function(e){
      app.$['editor'].focus();
      if(app.liveCompiling){
        app.compile();
      }
    });

    document.addEventListener('automata-walker-start', function(e) {
      var visualisations =
        Polymer.dom(this).querySelectorAll('automata-visualisation');
      for (var i in visualisations) {
        visualisations[i].setHighlightNodeId(e.detail.node);
        visualisations[i].redraw();
      }
    });

    document.addEventListener('automata-walker-walk', function(e) {
      var visualisations =
        Polymer.dom(this).querySelectorAll('automata-visualisation');
      for (var i in visualisations) {
        visualisations[i].setHighlightNodeId(e.detail.edge.to);
        visualisations[i].redraw();
      }
    });

    document.addEventListener('text-editor-change', function(e){
      if(app.liveCompiling){
        app.compile();
      }
    });

    /**
    *EventListener function that allows the use of keybindings.
    */
    document.addEventListener('keyup',function(e) {
      if (app.$['help-dialog'].opened) {
        return;
      }

      switch (e.keyCode){
        case 13:
          // CTRL + ENTER
          if (e.ctrlKey) {
            app.compile();
          }
          break;
        case 79:
          // CTRL + O
          if (e.ctrlKey) {
            app.open();
          }
          break;
        case 83:
          // CTRL + S
          if (e.ctrlKey) {
            app.save();
          }
          break;
        default: return;
      }
      e.preventDefault();
    });

  });
})(document);
