(function(document) {
  'use strict';

  var app = document.querySelector('#app');

  window.addEventListener('WebComponentsReady', function() {

    app.data = { automatas: [] };

    app.compile = function(){
      var automatas = app.$.parser.parse(app.$['text-editor'].getCode());

      app.data = { automatas: [] };

      setTimeout(function(){
        app.data.automatas = automatas;
        app.notifyPath('data.automatas', app.data.automatas);
      }, 0);
    };

    app.open = function(){
      var opener = app.$['open-file'];
      opener.click();
      opener.onchange = function(e){
        var input = event.target;

        var reader = new FileReader();
        reader.onload = function(){
          var text = reader.result;
          app.$['text-editor'].setCode(text);
        };
        reader.readAsText(input.files[0]);
      };
    };

    app.save = function(){
      var blob = new Blob([app.$['text-editor'].getCode()], {type: "text/plain;charset=utf-8"});
      saveAs(blob, "untitled.txt");
    };

  });
})(document);
