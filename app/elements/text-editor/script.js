// jshint -W106
(function() {
  Polymer({
    is: 'text-editor',

    properties: {
      /**
       * If true the editor will be brought into focus automatically when the elements is added to the dom
       *
       * @default false
       */
      autofocus: {
        type: Boolean,
        value: false
      },

      /**
       * The amount of time before a text-editor-change event is fired after the editor's content changes.
       *
       * @default 1000
       */
      changeEventDebounceTime: {
        type: Number,
        value: 1000,
        notify: true
      },

      /**
       * the text editor
       */
      _editor: {
        type: Object,
        value: function(){
          var langTools = ace.require("ace/ext/language_tools");
          var editor = ace.edit(this.$.editor);
          editor.setTheme('ace/theme/example');
          editor.getSession().setMode('ace/mode/example'); // syntax highlighting
          var qtags = {
            getCompletions: function(editor, session, pos, prefix, callback) {
              var types = {"scope": _.keys(Lexer.processTypes),"keyword": _.keys(Lexer.keywords),"terminal": _.keys(Lexer.terminals),"function":_.keys(Lexer.functions)};
              var mapped = [];
              _.each(types,(words,scope)=>{
                mapped = mapped.concat(words.map(function(word) {
                  return {
                    caption: word,
                    value: word,
                    meta: scope
                  };
                }));
              });
              console.log(mapped);
              callback(null, mapped);
            }
          };

          langTools.setCompleters([qtags]);

          editor.setOptions({
            enableBasicAutocompletion: true,
            enableLiveAutocompletion: true
          });

          return editor;
        },
        readOnly: true
      },

      /**
       * don't fire a 'text-editor-change' event the next time the editor's content changes
       */
      _skipNextCodeChangeEvent: {
        type: Boolean,
        value: false,
        readOnly: true
      }
    },

    /**
     * Get the code the user has provided
     * @return {string} The code
     */
    getCode: function(){
      return this._editor.getValue();
    },

    /**
     * Set the code.
     *
     * @param {!string} code - The code
     */
    setCode: function(code){
      this._set_skipNextCodeChangeEvent(true);
      this._editor.setValue(code, 1);
      this.fire('text-editor-change');
    },
    /**
     * constructs and returns an annotation.
     *
     * @param {!integer} line - the line number to put annotation on
     * @param {!string} text - the text to put in the annotation
     * @oaram {!string} type - the type of annotation to place
     * @returns {!object} - an annotation object
     */
    constructAnnotation: function(line, text, type) {
      var annotation = {
        row: line,
        column: 2,
        text: text,
        type: type
      }

      return annotation;
    },

    /**
     * Sets the annotations of the editor to those specified in the array.
     *
     * @param {!array} annotations - an array of annotations
     */
    setAnnotations: function(annotations) {
      this._editor.session.setAnnotations(annotations);
    },

    /**
     * Bring this element into focus
     */
    focus: function(){
      this._editor.focus();
    },

    ready: function(){
      if(this.autofocus){
        this.focus();
      }

      // fire a 'text-editor-change' event when the text editor's content changes
      this._editor.on('change', function() {
        if (this._skipNextCodeChangeEvent){
          this._set_skipNextCodeChangeEvent(false);
          return;
        }

        // merge multiple events into one (debounce them)
        this.debounce('text-editor-change', function() {
          this.fire('text-editor-change');
        }, this.changeEventDebounceTime);
      }.bind(this));
    }
  });
})();
