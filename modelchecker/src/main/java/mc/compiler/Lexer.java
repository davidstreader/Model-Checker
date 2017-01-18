package mc.compiler;

import java.util.ArrayList;
import java.util.List;

import mc.compiler.token.*;
import mc.exceptions.LexerException;
import mc.util.Location;

public class Lexer {
	
	private int index;
	
	// used for constructing locations of tokens
	private int line;
	private int column;
	
	public List<Token> tokenise(String code) throws LexerException{
		reset();
		List<Token> tokens = new ArrayList<Token>();
		
		char[] characters = code.toCharArray();
		
		while(index < characters.length){
			gobbleWhitespace(characters);
			gobbleComments(characters);
			
			if(index >= characters.length){
				break;
			}
			
			if(Character.isLetter(characters[index])){
				String value = parseString(characters);
				Token token = constructStringToken(value);
				tokens.add(token);
				column += value.length();
			}
			else if(Character.isDigit(characters[index])){
				String integer = parseInteger(characters);
				Location location = new Location(line, line, column, column + integer.length());
				Token token = new IntegerToken(Integer.parseInt(integer), location);
				tokens.add(token);
				column += integer.length();
			}
			else{
				Token token = constructSymbolToken(characters);
				tokens.add(token);
			}
		}
		
		return tokens;
	}
	
	private String parseString(char[] characters){
		StringBuilder builder = new StringBuilder();
		while(index < characters.length){
			char letter = characters[index];
			if(Character.isLetter(letter) || Character.isDigit(letter) || letter == '_'){
				builder.append(letter);
			}
			else{
				break;
			}
			
			index++;
		}
		
		return builder.toString();
	}
	
	private String parseInteger(char[] characters){
		StringBuilder builder = new StringBuilder();
		while(index < characters.length){
			if(Character.isDigit(characters[index])){
				builder.append(characters[index++]);
			}
			else{
				break;
			}
		}
		
		return builder.toString();
	}
	
	private Token constructStringToken(String string){
		Location location = new Location(line, line, column, column + string.length());
		if(string.equals("automata") || string.equals("petrinet")){
			return new ProcessTypeToken(string, location);
		}
		else if(string.equals("abs") || string.equals("simp") || string.equals("safe")){
			return new FunctionToken(string, location);
		}
		else if(string.equals("STOP")){
			return new StopToken(location);
		}
		else if(string.equals("ERROR")){
			return new ErrorToken(location);
		}
		else if(string.equals("const")){
			return new ConstToken(location);
		}
		else if(string.equals("range")){
			return new RangeToken(location);
		}
		else if(string.equals("set")){
			return new SetToken(location);
		}
		else if(string.equals("if")){
			return new IfToken(location);
		}
		else if(string.equals("then")){
			return new ThenToken(location);
		}
		else if(string.equals("else")){
			return new ElseToken(location);
		}
		else if(string.equals("when")){
			return new WhenToken(location);
		}
		else if(string.equals("forall")){
			return new ForAllToken(location);
		}
		else if(Character.isUpperCase(string.charAt(0))){
			return new IdentifierToken(string, location);
		}
		
		return new ActionToken(string, location);
	}

	private Token constructSymbolToken(char[] characters) throws LexerException{
		if(characters[index] == '.'){
			if(index < characters.length - 1 && characters[index + 1] == '.'){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new RangeSeparatorToken(location);
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new DotToken(location);
			}
		}
		else if(characters[index] == ','){
			Location location = new Location(line, line, column, column++);
			index++;
			return new CommaToken(location);
		}
		else if(characters[index] == ':'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new ColonToken(location);
		}
		else if(characters[index] == '['){
			Location location = new Location(line, line, column, column++);
			index++;
			return new OpenBracketToken(location);
		}
		else if(characters[index] == ']'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new CloseBracketToken(location);
		}
		else if(characters[index] == '('){
			Location location = new Location(line, line, column, column++);
			index++;
			return new OpenParenToken(location);
		}
		else if(characters[index] == ')'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new CloseParenToken(location);
		}
		else if(characters[index] == '{'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new OpenBraceToken(location);
		}
		else if(characters[index] == '}'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new CloseBraceToken(location);
		}
		else if(characters[index] == '-'){
			if(index < characters.length - 1 && characters[index + 1] == '>'){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new SequenceToken(location);				
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new SubtractionToken(location);
			}
		}
		else if(characters[index] == '~'){
			if(index < characters.length - 1 && characters[index + 1] == '>'){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new InterruptToken(location);				
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new BisimulationToken(location);
			}
		}
		else if(characters[index] == '\\'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new HideToken(location);
		}		
		else if(characters[index] == '@'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new AtToken(location);
		}
		else if(characters[index] == '$'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new DollarToken(location);
		}
		else if(characters[index] == '?'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new QuestionMarkToken(location);
		}
		else if(characters[index] == '|'){
			if(index < characters.length - 1 && characters[index + 1] == '|'){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new OrToken(location);				
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new BitOrToken(location);
			}
		}
		else if(characters[index] == '&'){
			if(index < characters.length - 1 && characters[index + 1] == '&'){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new AndToken(location);				
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new BitAndToken(location);
			}
		}
		else if(characters[index] == '^'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new ExclOrToken(location);
		}
		else if(characters[index] == '='){
			if(index < characters.length - 1 && characters[index + 1] == '='){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new EqualityToken(location);				
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new AssignToken(location);
			}
		}
		else if(characters[index] == '!'){
			if(index < characters.length - 1 && characters[index] == '='){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new NotEqualToken(location);				
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new NegateToken(location);
			}
		}
		else if(characters[index] == '<'){
			if(index < characters.length - 1 && characters[index] == '='){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index++;
				return new LessThanEqToken(location);				
			}
			if(index < characters.length - 1 && characters[index] == '<'){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new LeftShiftToken(location);				
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new LessThanToken(location);
			}
		}
		else if(characters[index] == '>'){
			if(index < characters.length - 1 && characters[index] == '='){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new GreaterThanEqToken(location);				
			}
			if(index < characters.length - 1 && characters[index] == '>'){
				Location location = new Location(line, line, column, column + 2);
				column += 2;
				index += 2;
				return new RightShiftToken(location);				
			}
			else{
				Location location = new Location(line, line, column, column++);
				index++;
				return new GreaterThanToken(location);
			}
		}
		else if(characters[index] == '+'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new AdditionToken(location);
		}
		else if(characters[index] == '*'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new MultiplicationToken(location);
		}
		else if(characters[index] == '/'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new DivisionToken(location);
		}
		else if(characters[index] == '%'){
			Location location = new Location(line, line, column, column++);
			index++;
			return new ModuloToken(location);
		}
		
		throw new LexerException("Found invalid character '" + characters[index] + "' (" + line + ":" + column + ")");
	}
	
	private void gobbleComments(char[] characters){
		while(index < characters.length - 1){
			if(characters[index] == '/' && characters[index + 1] == '/'){
				gobbleSingleLineComment(characters);
				gobbleWhitespace(characters);
				continue;
			}

			if(characters[index] == '/' && characters[index + 1] == '*'){
				gobbleMultiLineComment(characters);
				gobbleWhitespace(characters);
				continue;
			}
			
			break;
		}
	}
	
	private void gobbleSingleLineComment(char[] characters){
		while(index < characters.length){
			if(characters[index] == '\n'){
				break;
			}
			
			index++;
		}
		
		index++;
		line++;
		column = 0;
	}
	
	private void gobbleMultiLineComment(char[] characters){
		while(index < characters.length - 1){
			if(characters[index] == '*' && characters[index + 1] == '/'){
				break;
			}
			else if(characters[index] == '\n' || characters[index] == '\r'){
				line++;
				column = 0;
			}
			else{
				column++;
			}
			
			index++;
		}
		
		index += 2;
	}
	
	private void gobbleWhitespace(char[] characters){
		while(index < characters.length){
			if(characters[index] == ' ' || characters[index] == '\t'){
				column++;
			}
			else if(characters[index] == '\n' || characters[index] ==  '\r'){
				line++;
				column = 0;
			}
			else{
				break;
			}
			
			index++;
		}
	}
	
	private void reset(){
		index = 0;
		line = 1;
		column = 0;
	}
}
