importScripts("includes.js");

onmessage = function(e){
  const code = e.data.code;
  const context = e.data.context;
  try{
    const tokens = Lexer.tokenise(code);
    const ast = Parser.parse(tokens);
    // check if this is to be compiled client side or server side
    if(context.isClientSide || context.isLocal){
      const compiled = Compiler.localCompile(ast, context);
      postMessage({result: compiled});
    } else {
      postMessage({ast:ast, remoteCompile:true});
    }

  }catch(error){
    postMessage({result: {type:'error',message: error.toString(), stack: error.stack, location: error.location}});
  }
}

