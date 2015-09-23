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
        var timeTaken = timeAfter - timeBefore;
        var suffix = timeTaken === 1 ? 'second' : 'seconds';
        app.$.console.log('Compiled successfully - ' + timeTaken.toString() +
          ' ' + suffix + '.');
      } catch (e) {
        var buildErrorMessage = function(e) {
          return e.location !== undefined ?
          'Line ' + e.location.start.line +
          ', column ' + e.location.start.column +
          ': ' + e.message
          : e.message;
        };
        app.$.console.error(buildErrorMessage(e));
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
      opener.onchange = function() {
        var input = event.target;

        var reader = new FileReader();
        reader.onload = function() {
          var text = reader.result;
          app.$.editor.setCode(text);
        };
        reader.readAsText(input.files[0]);
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

  });
})(document);
