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

    app.setupDownloadLink = function(){
      alert(app.$['text-editor'].getCode());
    };

  });
})(document);
