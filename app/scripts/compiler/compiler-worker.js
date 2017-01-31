importScripts("includes.js");

onmessage = function(e){
    const code = e.data.code;
    try{
        const tokens = Lexer.tokenise(code);
        const ast = Parser.parse(tokens);
        postMessage({ast:ast, remoteCompile:true});

    }catch(error){
        postMessage({result: {type:'error',message: error.toString(), stack: error.stack, location: error.location}});
    }
}

