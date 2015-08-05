(function(document) {
  'use strict';

  var app = document.querySelector('#app');

  window.addEventListener('WebComponentsReady', function() {

    app.automatas = [
      {
        name: 'cheese',
        automata: {
          nodes: [
            {id: 0, label: 'a'},
            {id: 1, label: 'b'},
            {id: 2, label: 'c'},
            {id: 3, label: 'd'},
            {id: 4, label: 'e'}
          ],
          edges: [
            {from: 0, to: 1, label: 'A'},
            {from: 1, to: 2, label: 'B'},
            {from: 2, to: 3},
            {from: 1, to: 3, label: 'D'},
            {from: 0, to: 3, label: 'E'},
            {from: 0, to: 4, label: 'F'},
            {from: 4, to: 1, label: 'G'}
          ]
        }
      },
      {
        name: 'dog',
        automata: {
          nodes: [
            {id: 0, label: 'a'},
            {id: 1, label: 'b'},
            {id: 2, label: 'c'}
          ],
          edges: [
            {from: 0, to: 1, label: 'A'},
            {from: 1, to: 2, label: 'B'},
            {from: 0, to: 2, label: 'C'}
          ]
        }
      }
    ];

  });
})(document);
