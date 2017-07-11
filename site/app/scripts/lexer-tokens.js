module.exports = {
    actionLabel: '[a-z][A-Za-z0-9_]*',
    identifier: '[A-Z][A-Za-z0-9_\\*]*',
    integer: '[0-9][0-9]*',
    processTypes: {'automata':true, 'petrinet':true, 'operation':true, 'equation':true },
    functions: {'abs': true, 'simp':true, 'safe':true, 'nfa2dfa':true },
    terminals: { 'STOP':true, 'ERROR': true },
    keywords: { 'const':true, 'range':true, 'set':true, 'if':true, 'then':true, 'else':true, 'when':true, 'forall':true },
    symbols: '(\\.\\.|\\.|,|:|\\[|\\]|\\(|\\)|\\{|\\}|->|~>|\\\\|@|\\$|\\?)',
    operators: '(\\|\\||\\||&&|&|\\^|==|=|!=|<<|<=|<|>>|>=|>|\\+|-|\\*|/|%|!|\\?)',
    operations: '~|#',
    singleLineCommentStart: '//',
    multiLineCommentStart: '/\\*'
};
