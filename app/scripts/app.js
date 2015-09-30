(function(document) {
  'use strict';

  var app = document.querySelector('#app');

  window.addEventListener('WebComponentsReady', function() {

    /**
     * The data to use.
     */
    app.data = {automatas: []};

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
        var suffix = timeTaken === 1000 ? 'second' : 'seconds';
        app.$.console.log('Compiled successfully - ' + timeTaken.toFixed(3) +
          ' ' + suffix + '.');
      } catch (e) {
        var buildErrorMessage = function(e) {
          return e.location !== undefined ?
          'on line ' + e.location.start.line +
          ':' + e.location.start.column +
          ' - ' + e.message
          : e.message;
        };

        var isInterpreterError = e.constructor ===
                                 app.$.parser.InterpreterError;
        var prefix = isInterpreterError ? 'Error: ' : 'Syntax error ';

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

<<<<<<< HEAD

    document.addEventListener('keydown',function(e) {
      if(e.ctrlKey && e.keyCode === 13){
        alert("CTRL + ENTER");
      }
      else if(e.ctrlKey && e.keyCode === 79){
        alert("CTRL + O");
      }
      else if(e.ctrlKey && e.keyCode === 83){
        alert("CTRL + S");
=======
    /**
    *EventListener function that allows the use of keybindings.
    */
    document.addEventListener('keydown',function(e) {
      //CTRL + ENTER
      if (e.ctrlKey && e.keyCode === 13) {
        app.compile();
      //CTRL + O
      } else if (e.ctrlKey && e.keyCode === 79) {
        app.open();
      //CTRL + S
      } else if (e.ctrlKey && e.keyCode === 83) {
        app.save();
>>>>>>> 2255fd0df3cc6208e82ff3cb5e4112d2e6af2e3d
      }
    });

  });
})(document);
