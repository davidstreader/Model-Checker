define('ace/mode/example', function(require, exports, module) {

  var oop = require("ace/lib/oop");
  var TextMode = require("ace/mode/text").Mode;
  var ExampleHighlightRules = require("ace/mode/example_highlight_rules").ExampleHighlightRules;

  var Mode = function() {
    this.HighlightRules = ExampleHighlightRules;
  };
  oop.inherits(Mode, TextMode);

  (function() {
    // Extra logic goes here. (see below)
  }).call(Mode.prototype);

  exports.Mode = Mode;
});

define('ace/mode/example_highlight_rules', function(require, exports, module) {

  var oop = require("ace/lib/oop");
  var TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;

  var ExampleHighlightRules = function() {
    this.$rules = {
      "start" : [
        {token : "comment.double-slash",  regex : '\\/\\/.*'},
        {token : "comment.block",  regex : '\\/\\*', next: 'block'},
        {token : "paren.lparen", regex : "[\\[({]"},
        {token : "paren.rparen", regex : "[\\])}]"},
        {token : "constant.numeric", regex: "[+-]?\\d+\\b"},
        {token : "meta.function", regex: "automata | petrinet", next: "scope"},
        {token : "meta.function", regex: "const", next: "const"},
        {caseInsensitive: true}
      ],
      "scope" : [
        {token : "comment.block",  regex : '\\/\\*', next: 'blockscope'},
        {token : "paren.lparen", regex : "[\\[({]"},
        {token : "paren.rparen", regex : "[\\])}]", next: "start"},
        {token : "variable.ident", regex : new RegExp(Lexer.identifier), next: "process"},
        {defaultToken : "text"}
      ],
      "const" : [
        {token : "operator", regex : '=', next  : "start"},
        {defaultToken : "variable.constant"}
      ],
      "block" : [
        {token : "comment.block", regex : '\\*\\/', next  : "start"},
        {defaultToken : "comment.block"}
      ],
      "blockscope" : [
        {token : "comment.block", regex : '\\*\\/', next  : "scope"},
        {defaultToken : "comment.block"}
      ],
      "process" : [
        {token : "meta.function", regex : "if|then|else|when|forall", next: "control"},
        {token : "constant.language", regex : "STOP|ERROR"},
        {token : "keyword.operator", regex : "abs|simp|safe"},
        {token : "comment.double-slash",  regex : '\\/\\/.*'},
        {token : "paren.lparen", regex : "[(]"},
        {token : "paren.rparen", regex : "[\\])]"},
        {token : "constant.numeric", regex: "[+-]?\\d+\\b"},
        {token : "paren.lparen", regex : '\\[', next  : "range"},
        {token : "string", regex : '\\/', next  : "rename"},
        {token : "string", regex : '\\.', next  : "scope"},
        {token : "string", regex : '\\,', next  : "scope"},
        {token : "keyword.operator", regex: new RegExp(Lexer.operators)},
        {token : "variable.ident", regex : new RegExp(Lexer.identifier)},
        {token : "variable.action", regex : new RegExp(Lexer.actionLabel)},
        {defaultToken : "text"}
      ],
      "control": [
        {token : "keyword.operator", regex: new RegExp(Lexer.operators)},
        {token : "variable.constant", regex : new RegExp(Lexer.identifier)},
        {token : "paren.lparen", regex : "[\\[(]"},
        {token : "variable.action", regex : new RegExp(Lexer.actionLabel)},
        {token : "paren.rparen", regex : "[\\])]", next: "process"},
        {defaultToken : "text"}
      ],
      "range" : [
        {token : "text", regex : '\\s|\\]:', next  : "process"},
        {token : "keyword.operator", regex: new RegExp(Lexer.operators)},
        {token : "paren.rparen", regex : "[\\])]"},
        {token : "keyword.operator", regex : "\\.\\."},
        {token : "constant.numeric", regex: "[+-]?\\d+\\b"},
        {token : "variable.constant", regex : new RegExp(Lexer.identifier)},
        {token : "variable.action", regex : new RegExp(Lexer.actionLabel)},
        {defaultToken : "text"}
      ],
      "rename" : [
        {token : "variable.action", regex : new RegExp(Lexer.actionLabel)},
        {token : "paren.lparen", regex : "[\\[{(]"},
        {token : "text", regex : '}', next  : "process"},
        {defaultToken : "text"}
      ],
    };

  }

  oop.inherits(ExampleHighlightRules, TextHighlightRules);

  exports.ExampleHighlightRules = ExampleHighlightRules;
});
