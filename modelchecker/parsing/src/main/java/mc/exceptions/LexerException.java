package mc.exceptions;

import mc.compiler.Lexer;

public class LexerException extends CompilationException {

	private static final long serialVersionUID = 1L;

	public LexerException(String message){
		super(Lexer.class, "LexerException: " + message, null);
	}
	
}
