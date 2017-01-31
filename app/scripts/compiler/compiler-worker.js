importScripts("includes.js");

onmessage = function(e){
    const code = e.data.code;
    const tokens = Lexer.tokenise(code);
    const ast = Parser.parse(tokens);
    postMessage({ast:ast, remoteCompile:true});
}

