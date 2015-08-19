// Generated from AutomataGrammar.g4 by ANTLR 4.5
// jshint ignore: start
var antlr4 = require('antlr4/index');
var AutomataGrammarListener = require('./AutomataGrammarListener').AutomataGrammarListener;
var grammarFileName = "AutomataGrammar.g4";

var serializedATN = ["\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd",
    "\3\16I\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t",
    "\t\4\n\t\n\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\5\3\35\n\3\3\4\3\4\3\4\3",
    "\4\3\4\3\4\3\4\5\4&\n\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6",
    "\3\6\3\6\5\6\65\n\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t",
    "\3\t\3\t\3\t\3\t\3\n\3\n\3\n\2\2\13\2\4\6\b\n\f\16\20\22\2\3\3\2\13",
    "\fF\2\24\3\2\2\2\4\34\3\2\2\2\6%\3\2\2\2\b\'\3\2\2\2\n\64\3\2\2\2\f",
    "\66\3\2\2\2\16:\3\2\2\2\20@\3\2\2\2\22F\3\2\2\2\24\25\5\4\3\2\25\3\3",
    "\2\2\2\26\27\5\6\4\2\27\30\7\2\2\3\30\35\3\2\2\2\31\32\5\6\4\2\32\33",
    "\5\4\3\2\33\35\3\2\2\2\34\26\3\2\2\2\34\31\3\2\2\2\35\5\3\2\2\2\36\37",
    "\5\b\5\2\37 \7\3\2\2 &\3\2\2\2!\"\5\b\5\2\"#\7\4\2\2#$\5\6\4\2$&\3\2",
    "\2\2%\36\3\2\2\2%!\3\2\2\2&\7\3\2\2\2\'(\7\13\2\2()\7\5\2\2)*\5\n\6",
    "\2*\t\3\2\2\2+,\7\6\2\2,-\5\n\6\2-.\7\7\2\2.\65\3\2\2\2/\65\7\r\2\2",
    "\60\65\7\13\2\2\61\65\5\f\7\2\62\65\5\16\b\2\63\65\5\20\t\2\64+\3\2",
    "\2\2\64/\3\2\2\2\64\60\3\2\2\2\64\61\3\2\2\2\64\62\3\2\2\2\64\63\3\2",
    "\2\2\65\13\3\2\2\2\66\67\5\22\n\2\678\7\b\2\289\5\n\6\29\r\3\2\2\2:",
    ";\7\6\2\2;<\5\n\6\2<=\7\t\2\2=>\5\n\6\2>?\7\7\2\2?\17\3\2\2\2@A\7\6",
    "\2\2AB\5\n\6\2BC\7\n\2\2CD\5\n\6\2DE\7\7\2\2E\21\3\2\2\2FG\t\2\2\2G",
    "\23\3\2\2\2\5\34%\64"].join("");


var atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

var decisionsToDFA = atn.decisionToState.map( function(ds, index) { return new antlr4.dfa.DFA(ds, index); });

var sharedContextCache = new antlr4.PredictionContextCache();

var literalNames = [ 'null', "'.'", "','", "'='", "'('", "')'", "'->'", 
                     "'|'", "'||'", 'null', 'null', "'STOP'" ];

var symbolicNames = [ 'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', "Name", "Action", "Stop", "WS" ];

var ruleNames =  [ "file", "globals", "model", "def", "process", "sequence", 
                   "choice", "parallel", "label" ];

function AutomataGrammarParser (input) {
	antlr4.Parser.call(this, input);
    this._interp = new antlr4.atn.ParserATNSimulator(this, atn, decisionsToDFA, sharedContextCache);
    this.ruleNames = ruleNames;
    this.literalNames = literalNames;
    this.symbolicNames = symbolicNames;
    return this;
}

AutomataGrammarParser.prototype = Object.create(antlr4.Parser.prototype);
AutomataGrammarParser.prototype.constructor = AutomataGrammarParser;

Object.defineProperty(AutomataGrammarParser.prototype, "atn", {
	get : function() {
		return atn;
	}
});

AutomataGrammarParser.EOF = antlr4.Token.EOF;
AutomataGrammarParser.T__0 = 1;
AutomataGrammarParser.T__1 = 2;
AutomataGrammarParser.T__2 = 3;
AutomataGrammarParser.T__3 = 4;
AutomataGrammarParser.T__4 = 5;
AutomataGrammarParser.T__5 = 6;
AutomataGrammarParser.T__6 = 7;
AutomataGrammarParser.T__7 = 8;
AutomataGrammarParser.Name = 9;
AutomataGrammarParser.Action = 10;
AutomataGrammarParser.Stop = 11;
AutomataGrammarParser.WS = 12;

AutomataGrammarParser.RULE_file = 0;
AutomataGrammarParser.RULE_globals = 1;
AutomataGrammarParser.RULE_model = 2;
AutomataGrammarParser.RULE_def = 3;
AutomataGrammarParser.RULE_process = 4;
AutomataGrammarParser.RULE_sequence = 5;
AutomataGrammarParser.RULE_choice = 6;
AutomataGrammarParser.RULE_parallel = 7;
AutomataGrammarParser.RULE_label = 8;

function FileContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_file;
    return this;
}

FileContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
FileContext.prototype.constructor = FileContext;

FileContext.prototype.globals = function() {
    return this.getTypedRuleContext(GlobalsContext,0);
};

FileContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterFile(this);
	}
};

FileContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitFile(this);
	}
};




AutomataGrammarParser.FileContext = FileContext;

AutomataGrammarParser.prototype.file = function() {

    var localctx = new FileContext(this, this._ctx, this.state);
    this.enterRule(localctx, 0, AutomataGrammarParser.RULE_file);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 18;
        this.globals();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function GlobalsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_globals;
    return this;
}

GlobalsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
GlobalsContext.prototype.constructor = GlobalsContext;

GlobalsContext.prototype.model = function() {
    return this.getTypedRuleContext(ModelContext,0);
};

GlobalsContext.prototype.EOF = function() {
    return this.getToken(AutomataGrammarParser.EOF, 0);
};

GlobalsContext.prototype.globals = function() {
    return this.getTypedRuleContext(GlobalsContext,0);
};

GlobalsContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterGlobals(this);
	}
};

GlobalsContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitGlobals(this);
	}
};




AutomataGrammarParser.GlobalsContext = GlobalsContext;

AutomataGrammarParser.prototype.globals = function() {

    var localctx = new GlobalsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 2, AutomataGrammarParser.RULE_globals);
    try {
        this.state = 26;
        var la_ = this._interp.adaptivePredict(this._input,0,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 20;
            this.model();
            this.state = 21;
            this.match(AutomataGrammarParser.EOF);
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 23;
            this.model();
            this.state = 24;
            this.globals();
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ModelContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_model;
    return this;
}

ModelContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ModelContext.prototype.constructor = ModelContext;

ModelContext.prototype.def = function() {
    return this.getTypedRuleContext(DefContext,0);
};

ModelContext.prototype.model = function() {
    return this.getTypedRuleContext(ModelContext,0);
};

ModelContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterModel(this);
	}
};

ModelContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitModel(this);
	}
};




AutomataGrammarParser.ModelContext = ModelContext;

AutomataGrammarParser.prototype.model = function() {

    var localctx = new ModelContext(this, this._ctx, this.state);
    this.enterRule(localctx, 4, AutomataGrammarParser.RULE_model);
    try {
        this.state = 35;
        var la_ = this._interp.adaptivePredict(this._input,1,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 28;
            this.def();
            this.state = 29;
            this.match(AutomataGrammarParser.T__0);
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 31;
            this.def();
            this.state = 32;
            this.match(AutomataGrammarParser.T__1);
            this.state = 33;
            this.model();
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function DefContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_def;
    return this;
}

DefContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
DefContext.prototype.constructor = DefContext;

DefContext.prototype.Name = function() {
    return this.getToken(AutomataGrammarParser.Name, 0);
};

DefContext.prototype.process = function() {
    return this.getTypedRuleContext(ProcessContext,0);
};

DefContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterDef(this);
	}
};

DefContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitDef(this);
	}
};




AutomataGrammarParser.DefContext = DefContext;

AutomataGrammarParser.prototype.def = function() {

    var localctx = new DefContext(this, this._ctx, this.state);
    this.enterRule(localctx, 6, AutomataGrammarParser.RULE_def);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 37;
        this.match(AutomataGrammarParser.Name);
        this.state = 38;
        this.match(AutomataGrammarParser.T__2);
        this.state = 39;
        this.process();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ProcessContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_process;
    return this;
}

ProcessContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ProcessContext.prototype.constructor = ProcessContext;

ProcessContext.prototype.process = function() {
    return this.getTypedRuleContext(ProcessContext,0);
};

ProcessContext.prototype.Stop = function() {
    return this.getToken(AutomataGrammarParser.Stop, 0);
};

ProcessContext.prototype.Name = function() {
    return this.getToken(AutomataGrammarParser.Name, 0);
};

ProcessContext.prototype.sequence = function() {
    return this.getTypedRuleContext(SequenceContext,0);
};

ProcessContext.prototype.choice = function() {
    return this.getTypedRuleContext(ChoiceContext,0);
};

ProcessContext.prototype.parallel = function() {
    return this.getTypedRuleContext(ParallelContext,0);
};

ProcessContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterProcess(this);
	}
};

ProcessContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitProcess(this);
	}
};




AutomataGrammarParser.ProcessContext = ProcessContext;

AutomataGrammarParser.prototype.process = function() {

    var localctx = new ProcessContext(this, this._ctx, this.state);
    this.enterRule(localctx, 8, AutomataGrammarParser.RULE_process);
    try {
        this.state = 50;
        var la_ = this._interp.adaptivePredict(this._input,2,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 41;
            this.match(AutomataGrammarParser.T__3);
            this.state = 42;
            this.process();
            this.state = 43;
            this.match(AutomataGrammarParser.T__4);
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 45;
            this.match(AutomataGrammarParser.Stop);
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 46;
            this.match(AutomataGrammarParser.Name);
            break;

        case 4:
            this.enterOuterAlt(localctx, 4);
            this.state = 47;
            this.sequence();
            break;

        case 5:
            this.enterOuterAlt(localctx, 5);
            this.state = 48;
            this.choice();
            break;

        case 6:
            this.enterOuterAlt(localctx, 6);
            this.state = 49;
            this.parallel();
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function SequenceContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_sequence;
    return this;
}

SequenceContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
SequenceContext.prototype.constructor = SequenceContext;

SequenceContext.prototype.label = function() {
    return this.getTypedRuleContext(LabelContext,0);
};

SequenceContext.prototype.process = function() {
    return this.getTypedRuleContext(ProcessContext,0);
};

SequenceContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterSequence(this);
	}
};

SequenceContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitSequence(this);
	}
};




AutomataGrammarParser.SequenceContext = SequenceContext;

AutomataGrammarParser.prototype.sequence = function() {

    var localctx = new SequenceContext(this, this._ctx, this.state);
    this.enterRule(localctx, 10, AutomataGrammarParser.RULE_sequence);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 52;
        this.label();
        this.state = 53;
        this.match(AutomataGrammarParser.T__5);
        this.state = 54;
        this.process();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ChoiceContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_choice;
    return this;
}

ChoiceContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ChoiceContext.prototype.constructor = ChoiceContext;

ChoiceContext.prototype.process = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ProcessContext);
    } else {
        return this.getTypedRuleContext(ProcessContext,i);
    }
};

ChoiceContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterChoice(this);
	}
};

ChoiceContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitChoice(this);
	}
};




AutomataGrammarParser.ChoiceContext = ChoiceContext;

AutomataGrammarParser.prototype.choice = function() {

    var localctx = new ChoiceContext(this, this._ctx, this.state);
    this.enterRule(localctx, 12, AutomataGrammarParser.RULE_choice);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 56;
        this.match(AutomataGrammarParser.T__3);
        this.state = 57;
        this.process();
        this.state = 58;
        this.match(AutomataGrammarParser.T__6);
        this.state = 59;
        this.process();
        this.state = 60;
        this.match(AutomataGrammarParser.T__4);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ParallelContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_parallel;
    return this;
}

ParallelContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ParallelContext.prototype.constructor = ParallelContext;

ParallelContext.prototype.process = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ProcessContext);
    } else {
        return this.getTypedRuleContext(ProcessContext,i);
    }
};

ParallelContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterParallel(this);
	}
};

ParallelContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitParallel(this);
	}
};




AutomataGrammarParser.ParallelContext = ParallelContext;

AutomataGrammarParser.prototype.parallel = function() {

    var localctx = new ParallelContext(this, this._ctx, this.state);
    this.enterRule(localctx, 14, AutomataGrammarParser.RULE_parallel);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 62;
        this.match(AutomataGrammarParser.T__3);
        this.state = 63;
        this.process();
        this.state = 64;
        this.match(AutomataGrammarParser.T__7);
        this.state = 65;
        this.process();
        this.state = 66;
        this.match(AutomataGrammarParser.T__4);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function LabelContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = AutomataGrammarParser.RULE_label;
    return this;
}

LabelContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
LabelContext.prototype.constructor = LabelContext;

LabelContext.prototype.Name = function() {
    return this.getToken(AutomataGrammarParser.Name, 0);
};

LabelContext.prototype.Action = function() {
    return this.getToken(AutomataGrammarParser.Action, 0);
};

LabelContext.prototype.enterRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.enterLabel(this);
	}
};

LabelContext.prototype.exitRule = function(listener) {
    if(listener instanceof AutomataGrammarListener ) {
        listener.exitLabel(this);
	}
};




AutomataGrammarParser.LabelContext = LabelContext;

AutomataGrammarParser.prototype.label = function() {

    var localctx = new LabelContext(this, this._ctx, this.state);
    this.enterRule(localctx, 16, AutomataGrammarParser.RULE_label);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 68;
        _la = this._input.LA(1);
        if(!(_la===AutomataGrammarParser.Name || _la===AutomataGrammarParser.Action)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


exports.AutomataGrammarParser = AutomataGrammarParser;
