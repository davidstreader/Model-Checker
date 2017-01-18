package mc.exceptions;

public class LexerException extends Exception {

	private static final long serialVersionUID = 1L;

	public LexerException(String message){
		super("LexerException: " + message);
	}
	
}
