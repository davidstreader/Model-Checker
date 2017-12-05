package mc.compiler;

import mc.compiler.token.*;
import mc.exceptions.LexerException;
import mc.util.Location;

import java.util.*;

public class Lexer {

    private Set<String> displayTypes;
    private Set<String> functions;

	// used for constructing locations of tokens
	private int index;
	private int line;
	private int column;

    public Lexer(){

        displayTypes = new HashSet<>(Arrays.asList("automata", "petrinet"));
        functions = new HashSet<>(Arrays.asList("abs", "simp", "safe", "prune", "nfa2dfa"));
    }

	public List<Token> tokenise(String code) throws LexerException{
		reset();
		List<Token> tokens = new ArrayList<>();

		char[] characters = code.toCharArray();

		while(index < characters.length && !Thread.currentThread().isInterrupted()){
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
				Location location = new Location(line, column, line, column + integer.length(),index,index+integer.length());
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

	private Token constructStringToken(String stringToToken){
		Location location = new Location(line, column, line, column + stringToToken.length(),index,index+stringToToken.length());

        if(stringToToken.equals("processes")) {
            return new ProcessesDefintionToken(location);
        } else if(displayTypes.contains(stringToToken)){
			return new DisplayTypeToken(stringToToken, location); // Sets the type of display we want the process to undergo
		}
		else if(functions.contains(stringToToken)){
			return new FunctionToken(stringToToken, location);
		}

        switch(stringToToken) {
            case "operation":
                return new OperationToken(location);

            case "equation":
                return new EquationToken(location);

            case "STOP":
                return new StopToken(location);

            case "ERROR":
                return new ErrorToken(location);

            case "const":
                return new ConstToken(location);

            case "range":
                return new RangeToken(location);

            case "set":
                return new SetToken(location);

            case "if":
                return new IfToken(location);

            case "then":
                return new ThenToken(location);

            case "else":
                return new ElseToken(location);

            case "when":
                return new WhenToken(location);

            case "forall":
                return new ForAllToken(location);
        }

        if(Character.isUpperCase(stringToToken.charAt(0))){
            return new IdentifierToken(stringToToken, location);
        }

		return new ActionToken(stringToToken, location);
	}

	private Token constructSymbolToken(char[] characters) throws LexerException{

        //The more complex operators consisting of 1 or more symbols
        if(characters[index] == '.'){
			if(index < characters.length - 1 && characters[index + 1] == '.'){
				Location location = new Location(line, column, line, column + 2,index,index+2);
				column += 2;
				index += 2;
				return new RangeSeparatorToken(location);
			}
			else{
				Location location = new Location(line, column, line, column++,index,index+1);
				index++;
				return new DotToken(location);
			}
		}else if(characters[index] == '-'){
            if(index < characters.length - 1 && characters[index + 1] == '>'){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new SequenceToken(location);
            }
            else{
                Location location = new Location(line, column, line, column++,index,index+1);
                index++;
                return new SubtractionToken(location);
            }
        } else if(characters[index] == '~'){
            if(index < characters.length - 1 && characters[index + 1] == '>'){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new InterruptToken(location);
            }
            else{
                Location location = new Location(line, column, line, column++,index,index+1);
                index++;
                return new BisimulationTypeToken(location);
            }
        }else if(characters[index] == '|'){
            if(index < characters.length - 1 && characters[index + 1] == '|'){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new OrToken(location);
            }
            else{
                Location location = new Location(line, column, line, column++,index,index+1);
                index++;
                return new BitOrToken(location);
            }
        }
        else if(characters[index] == '&'){
            if(index < characters.length - 1 && characters[index + 1] == '&'){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new AndToken(location);
            }
            else{
                Location location = new Location(line, column, line, column++,index,index+1);
                index++;
                return new BitAndToken(location);
            }
        }		else if(characters[index] == '='){
            if(index < characters.length - 1 && characters[index + 1] == '='){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new EqualityToken(location);
            }
            else{
                Location location = new Location(line, column, line, column++,index,index+1);
                index++;
                return new AssignToken(location);
            }
        }
        else if(characters[index] == '!'){
            if(index < characters.length - 1 && characters[index + 1] == '='){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new NotEqualToken(location);
            }
            else{
                Location location = new Location(line, column, line, column++,index,index+1);
                index++;
                return new NegateToken(location);
            }
        }
        else if(characters[index] == '<'){
            if(index < characters.length - 1 && characters[index + 1] == '='){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new LessThanEqToken(location);
            }
            if(index < characters.length - 1 && characters[index + 1] == '<'){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new LeftShiftToken(location);
            }
            else{
                Location location = new Location(line, column, line, column++,index,index+1);
                index++;
                return new LessThanToken(location);
            }
        }
        else if(characters[index] == '>'){

            if(index < characters.length - 1 && characters[index + 1] == '='){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new GreaterThanEqToken(location);
            }
            if(index < characters.length - 1 && characters[index + 1] == '>'){
                Location location = new Location(line, column, line, column + 2,index,index+2);
                column += 2;
                index += 2;
                return new RightShiftToken(location);
            }
            else{
                Location location = new Location(line, column, line, column++,index,index+1);
                index++;
                return new GreaterThanToken(location);
            }
        }

        //Less complex symbols that are made of only one character
        Location location = new Location(line, column, line, column++,index,index+1);
        switch(characters[index] ) {

            case ',': {
                index++;
                return new CommaToken(location);
            }

            case ':': {
                index++;
                return new ColonToken(location);
            }

            case '[': {
                index++;
                return new OpenBracketToken(location);
            }

            case ']': {
                index++;
                return new CloseBracketToken(location);
            }

            case '(': {
                index++;
                return new OpenParenToken(location);
            }

            case ')': {
                index++;
                return new CloseParenToken(location);
            }

            case '{': {
                index++;
                return new OpenBraceToken(location);
            }

            case '}': {
                index++;
                return new CloseBraceToken(location);
            }

            case '#': {
                index++;
                return new TraceEquivalentTypeToken(location);
            }

            case '\\': {
                index++;
                return new HideToken(location);
            }

            case '@': {
                index++;
                return new AtToken(location);
            }

            case '$': {
                index++;
                return new DollarToken(location);
            }

            case '?': {
                index++;
                return new QuestionMarkToken(location);
            }

            case '^': {
                index++;
                return new ExclOrToken(location);
            }

            case '+': {
                index++;
                return new AdditionToken(location);
            }

            case '*': {
                index++;
                return new MultiplicationToken(location);
            }

            case '/': {
                index++;
                return new DivisionToken(location);
            }

            case '%': {
                index++;
                return new ModuloToken(location);
            }

            default: {
                throw new LexerException("Found invalid character '" + characters[index] + "' (" + line + ":" + column + ")");
            }


        }
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
			else if(characters[index] == '\n'){
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
			if(characters[index] == ' ' || characters[index] == '\t'|| characters[index] == '\r'){
				column++;
			}
			else if(characters[index] == '\n'){
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
