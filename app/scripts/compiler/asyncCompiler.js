importScripts("includes.js");

onmessage = function(e){
  code = e.data.code;
  context = e.data.context;
  try{
    const tokens = Lexer.tokenise(code);
    const ast = parse(tokens);
    // check if this is to be compiled client side or server side
    if(context.isClientSide || context.isLocal){
      postMessage({result: Compiler.localCompile(ast, context)});
    } else {
      postMessage({ast:ast, remoteCompile:true});
    }

  }catch(error){
    postMessage({result: {type:'error',message: error.toString(), stack: error.stack}});
  }
}

