(function() {
  'use strict';
  Polymer({
    is: 'process-modify',
    properties: {
      added: {
        type: Array,
        value: [],
        notify: true
      },
      addLabel: {
        type: String,
        value: "Add Process"
      },
      PROCESS_TYPES: {
        type: Array,
        value: PROCESS_TYPES
      },
      processes: {
        type: Array
      },
      processName: {
        type: String
      },
      _hasSelection: {
        type: Boolean,
        value: false
      },
      _hasProcesses: {
        type: Boolean,
        computed: '_greaterThan(processes.length, 0)'
      },
      _initialSelection: {
        type: String,
        computed: 'initialSelection()'
      },
      compiledResult: {
        type: String
      },
      editorLabel: {
        type: String,
        value: "Add to Editor"
      },
      isExisting: {
        type: Boolean,
        value: false
      },
      hasCompiled: {
        type: Boolean,
        value: false
      }
    },
    //Call compile if added or processName are modified
    observers: ['compile(added.*,processName)'],

  });
})();
