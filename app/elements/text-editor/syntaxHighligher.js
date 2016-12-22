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
        {token : "comment.block",  regex : '\\/\\*', push: 'block'},
        {token : "paren.lparen", regex : "[\\[({]"},
        {token : "paren.rparen", regex : "[\\])}]"},
        {token : "constant.numeric", regex: "[+-]?\\d+\\b"},
        //Using a negative lookahead, we can say that a process is automata | petrinet not followed by a {
        {token : "meta.function", regex: "("+_.keys(Lexer.processTypes).join("|")+")\\s+(?!{)", push: "process"},
        //Because otherwise if parsed here the { is pulled in and coloured when we don't want it to be
        {token : "meta.function", regex: _.keys(Lexer.processTypes).join("|"), push: "scope"},
        {token : "meta.function", regex: "const", push: "const"},
        {caseInsensitive: true}
      ],
      //Inside either a automata or petrinet scope
      "scope" : [
        {token : "keyword.operator", regex: _.keys(Lexer.processTypes).join("|")},
        {token : "comment.block",  regex : '\\/\\*', push: 'block'},
        {token : "comment.double-slash",  regex : '\\/\\/.*'},
        {token : "paren.lparen", regex : "[\\[({]"},
        {token : "paren.rparen", regex : "[\\])}]", next: "pop"},
        {token : "variable.ident", regex : new RegExp(Lexer.identifier), push: "process"},
        {defaultToken : "text"}
      ],
      "const" : [
        {token : "operator", regex : '=', next  : "pop"},
        {defaultToken : "variable.constant"}
      ],
      "block" : [
        {token : "comment.block", regex : '\\*\\/', next  : "pop"},
        {defaultToken : "comment.block"}
      ],
      "process" : [
        {token : "text", regex : ',', next  : "process"},
        {token : "keyword.operator", regex: _.keys(Lexer.processTypes).join("|")},
        {token : "meta.function", regex : _.keys(Lexer.keywords).join("|"), push: "control"},
        {token : "constant.language", regex : _.keys(Lexer.terminals).join("|")},
        {token : "keyword.operator", regex : _.keys(Lexer.functions).join("|")},
        {token : "comment.double-slash",  regex : '\\/\\/.*'},
        {token : "paren.lparen", regex : "[(]"},
        {token : "paren.rparen", regex : "[\\])]"},
        {token : "constant.numeric", regex: "[+-]?\\d+\\b"},
        {token : "paren.lparen", regex : '\\[', push  : "range"},
        {token : "text", regex : '\\/|\\\\|@|$', push  : "set"},
        {token : "keyword.operator", regex: new RegExp(Lexer.operators+"|"+Lexer.operations)},
        {token : "variable.ident", regex : new RegExp(Lexer.identifier)},
        {token : "variable.action", regex : new RegExp(Lexer.actionLabel)},
        {token : "text", regex : '\\.', next  : "pop"},
        {defaultToken : "variable.constant"}
      ],
      "control": [
        {token : "keyword.operator", regex: new RegExp(Lexer.operators)},
        {token : "variable.constant", regex : new RegExp(Lexer.identifier)},
        {token : "paren.lparen", regex : "[\\[(]"},
        {token : "variable.action", regex : new RegExp(Lexer.actionLabel)},
        {token : "paren.rparen", regex : "[\\])]", next: "pop"},
        {defaultToken : "text"}
      ],
      "range" : [
        {token : "text", regex : ',|=|~', next  : "pop"},
        {token : "text", regex : '\\s|\\]:', next  : "pop"},
        {token : "keyword.operator", regex: new RegExp(Lexer.operators)},
        {token : "paren.rparen", regex : "[\\])]"},
        {token : "keyword.operator", regex : "\\.\\."},
        {token : "constant.numeric", regex: "[+-]?\\d+\\b"},
        {token : "variable.constant", regex : new RegExp(Lexer.identifier)},
        {token : "variable.action", regex : new RegExp(Lexer.actionLabel)},
        {token : "text", regex : '\\.', next  : "pop"},
        {defaultToken : "text"}
      ],
      "set" : [
        {token : "variable.action", regex : new RegExp(Lexer.actionLabel)},
        {token : "paren.lparen", regex : "[\\[{(]"},
        {token : "text", regex : '}', next  : "pop"},
        {defaultToken : "text"}
      ],
    };

    this.normalizeRules();
  }

  oop.inherits(ExampleHighlightRules, TextHighlightRules);

  exports.ExampleHighlightRules = ExampleHighlightRules;
});
