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
<<<<<<< HEAD
    app.compile = function(){
    //  var automatas = app.$.parser.parse(app.$.editor.getCode());

      // Can't simply assign app.data.automatas to the new array as data bindings will not update.
      // Creating a new data oject then setting the automatas value slighly later will work (for some reason).
    //  app.data = {};
    //  this.async(function(){
    //    app.set('data.automatas', automatas);
   //   });
=======
    app.compile = function() {
      var automatas = app.$.parser.parse(app.$.editor.getCode());

      // Can't simply assign app.data.automatas to the new array as data bindings will not update.
      // Creating a new data oject then setting the automatas value slighly later will work (for some reason).
      app.data = {};
      this.async(function() {
        app.set('data.automatas', automatas);
      });
>>>>>>> 5139cab04aa504ba777eb8d3bc75c462376a4717
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

function createWalker(){
  //Check which visualisation
  //Set walking boolean to true.
  //Set first node to new color.
  //Check for choice

}

function nextNode(){
  alert('nextNode')
  //If walking boolean false do nothing.
  //If no more nodes to step through do nothing.
  //Change color of the next node down the given path.

}

function checkChoice(){
  //For all the edges.
  //If there are more than one edge with the same current from.
  //Set hasChoice boolean to true.
  //Add them to a temporary list.
  //Save the current edge somewhere
  //Change the current edge to the new color.
}

function changeChoice(){
  //If hasChoice is false do nothing.

}