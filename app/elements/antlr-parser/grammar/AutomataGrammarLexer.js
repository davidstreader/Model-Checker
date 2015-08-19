// Generated from AutomataGrammar.g4 by ANTLR 4.5
// jshint ignore: start
//var antlr4 = require('antlr4/index');


var serializedATN = ["\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd",
    "\2\16G\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4",
    "\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\3\2\3\2\3\3\3\3\3\4\3\4\3\5",
    "\3\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\n\3\n\7\n\60\n\n\f\n",
    "\16\n\63\13\n\3\13\3\13\7\13\67\n\13\f\13\16\13:\13\13\3\f\3\f\3\f\3",
    "\f\3\f\3\r\6\rB\n\r\r\r\16\rC\3\r\3\r\2\2\16\3\3\5\4\7\5\t\6\13\7\r",
    "\b\17\t\21\n\23\13\25\f\27\r\31\16\3\2\6\5\2\60\60CC\\\\\t\2\60\60\62",
    "\62;;CC\\\\cc||\5\2\60\60cc||\5\2\13\f\17\17\"\"I\2\3\3\2\2\2\2\5\3",
    "\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2",
    "\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\3",
    "\33\3\2\2\2\5\35\3\2\2\2\7\37\3\2\2\2\t!\3\2\2\2\13#\3\2\2\2\r%\3\2",
    "\2\2\17(\3\2\2\2\21*\3\2\2\2\23-\3\2\2\2\25\64\3\2\2\2\27;\3\2\2\2\31",
    "A\3\2\2\2\33\34\7\60\2\2\34\4\3\2\2\2\35\36\7.\2\2\36\6\3\2\2\2\37 ",
    "\7?\2\2 \b\3\2\2\2!\"\7*\2\2\"\n\3\2\2\2#$\7+\2\2$\f\3\2\2\2%&\7/\2",
    "\2&\'\7@\2\2\'\16\3\2\2\2()\7~\2\2)\20\3\2\2\2*+\7~\2\2+,\7~\2\2,\22",
    "\3\2\2\2-\61\t\2\2\2.\60\t\3\2\2/.\3\2\2\2\60\63\3\2\2\2\61/\3\2\2\2",
    "\61\62\3\2\2\2\62\24\3\2\2\2\63\61\3\2\2\2\648\t\4\2\2\65\67\t\3\2\2",
    "\66\65\3\2\2\2\67:\3\2\2\28\66\3\2\2\289\3\2\2\29\26\3\2\2\2:8\3\2\2",
    "\2;<\7U\2\2<=\7V\2\2=>\7Q\2\2>?\7R\2\2?\30\3\2\2\2@B\t\5\2\2A@\3\2\2",
    "\2BC\3\2\2\2CA\3\2\2\2CD\3\2\2\2DE\3\2\2\2EF\b\r\2\2F\32\3\2\2\2\6\2",
    "\618C\3\b\2\2"].join("");


var atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

var decisionsToDFA = atn.decisionToState.map( function(ds, index) { return new antlr4.dfa.DFA(ds, index); });

function AutomataGrammarLexer(input) {
	antlr4.Lexer.call(this, input);
    this._interp = new antlr4.atn.LexerATNSimulator(this, atn, decisionsToDFA, new antlr4.PredictionContextCache());
    return this;
}

AutomataGrammarLexer.prototype = Object.create(antlr4.Lexer.prototype);
AutomataGrammarLexer.prototype.constructor = AutomataGrammarLexer;

AutomataGrammarLexer.EOF = antlr4.Token.EOF;
AutomataGrammarLexer.T__0 = 1;
AutomataGrammarLexer.T__1 = 2;
AutomataGrammarLexer.T__2 = 3;
AutomataGrammarLexer.T__3 = 4;
AutomataGrammarLexer.T__4 = 5;
AutomataGrammarLexer.T__5 = 6;
AutomataGrammarLexer.T__6 = 7;
AutomataGrammarLexer.T__7 = 8;
AutomataGrammarLexer.Name = 9;
AutomataGrammarLexer.Action = 10;
AutomataGrammarLexer.Stop = 11;
AutomataGrammarLexer.WS = 12;


AutomataGrammarLexer.modeNames = [ "DEFAULT_MODE" ];

AutomataGrammarLexer.literalNames = [ 'null', "'.'", "','", "'='", "'('",
                                      "')'", "'->'", "'|'", "'||'", 'null',
                                      'null', "'STOP'" ];

AutomataGrammarLexer.symbolicNames = [ 'null', 'null', 'null', 'null', 'null',
                                       'null', 'null', 'null', 'null', "Name",
                                       "Action", "Stop", "WS" ];

AutomataGrammarLexer.ruleNames = [ "T__0", "T__1", "T__2", "T__3", "T__4",
                                   "T__5", "T__6", "T__7", "Name", "Action",
                                   "Stop", "WS" ];

AutomataGrammarLexer.grammarFileName = "AutomataGrammar.g4";



//exports.AutomataGrammarLexer = AutomataGrammarLexer;
