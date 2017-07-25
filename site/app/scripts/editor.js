const langTools = ace.require("ace/ext/language_tools");
const editor = ace.edit("ace-editor");
const LexerTokens = require("./lexer-tokens");
editor.setBehavioursEnabled(true);
editor.setTheme('ace/theme/example');
editor.getSession().setMode('ace/mode/example'); // syntax highlighting
const qtags = {
    getCompletions: function(editor, session, pos, prefix, callback) {
        var types = {"scope": _.keys(LexerTokens.processTypes),"keyword": _.keys(LexerTokens.keywords),"terminal": _.keys(LexerTokens.terminals),"function":_.keys(LexerTokens.functions)};
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
        callback(null, mapped);
    }
};

langTools.setCompleters([qtags]);

editor.setOptions({
    enableBasicAutocompletion: true,
    enableLiveAutocompletion: true
});
module.exports = {
    _editor: editor,
    getCode: ()=>editor.getValue(),
    setCode: code => editor.setValue(code,1),
    setAnnotations: annotations => editor.session.setAnnotations(annotations),
    setTheme: theme=>editor.setTheme(theme),
    getCodeClean: getCodeClean
};


/**
 * Gets and returns the code from the editor. Strips the code of all whitespace
 * and unnecessary line breaks.
 */
function getCodeClean() {
    let code = '';
    let temp = editor.getValue();

    // remove white space and line breaks
    temp = temp.replace(/ /g, '');

    // remove unnecessary whitespace
    const split = temp.split('\n');
    for(let i = 0; i < split.length; i++){
        if(split[i] !== ''){
            code += split[i] + '\n';
        }
    }

    return code;
};
