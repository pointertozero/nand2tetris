import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class JackTokenizer {
	private Scanner reader;
	private String currentLine = ""; // string[] maybe?
	//private List<String> tokensInCurrentLine;
	private String currentToken;
	private int lineIndex = 0;
	
	public JackTokenizer(File file) throws FileNotFoundException {
		this.reader = new Scanner(file);
	}
	
	public String DEBUGgetCurrentToken() {
		return currentToken;
	}
	
	public void advance() {
		// called if hasMoreTokens() returns true (?)
		// lineIndex at the beginning of the next (possible?) token
		
		currentToken = "";
		
		if (isSymbol()) {
			currentToken = Character.toString(currentLine.charAt(lineIndex));
			++lineIndex;
		} else if (isDoubleQuote()) {
			// handle String
			
			getString();
		} else if (isDigit()) {
			// get constant into token
			getConstant();
		} else {
			// identifier or reserved word
			
			// get the word
			getWord();
		}
		
		
		
//		String readLine = reader.nextLine();
//		
//		readLine = readLine.replaceAll("//.*", ""); // remove comments
//		readLine = readLine.trim(); // remove trailing whitespace
//		// replace symbol or double quote with " " + symbol
//		readLine = readLine.replaceAll("(\\{|\\}|\\(|\\)|\\[|\\]|\\.|,|;|\\+|-|\\*|\\/|&|\\||<|>|=|~|\")", " $1"); // problem with string
		
//		String token = "";
//		for (int i = 0; i < readLine.length(); ++i) {
//			
//			if (isSymbolOrDoubleQuote(readLine.charAt(i))) {
//				tokensInCurrentLine.
//			}
//		}
//		
//		// currentLine = readLine;
		
	}
	
	private void getNextLine() {
		lineIndex = 0;
		currentLine = reader.nextLine();
		currentLine = currentLine.strip();
	}
	
	public boolean hasMoreTokens() {
		// sets the index on the beginning of the token
		
		if (lineIndex >= currentLine.length()) {
			
			if (reader.hasNextLine()) {
				getNextLine();
				return hasMoreTokens();
			} else {
				return false;
			}
		} else {
			if (isEndLineComment()) {
				skipEndLineComment();
			} else if (isBlockComment()) {
				skipBlockComment();
			} else if (isWhitespace()) {
				skipWhitespace();
			} else {
				// there is something there, so true
				return true;
			}
			
			return hasMoreTokens();
		}
	}
	
	private boolean isEndLineComment() {
		return currentLine.charAt(lineIndex) == '/'
				&& currentLine.charAt(lineIndex + 1) == '/';
	}
	
	private boolean isBlockComment() {
		return currentLine.charAt(lineIndex) == '/'
				&& currentLine.charAt(lineIndex + 1) == '*';
	}
	
	private boolean isWhitespace() {
		// return currentLine.charAt(lineIndex) == ' ';
		return Character.isWhitespace(currentLine.charAt(lineIndex));
	}
	
	private boolean isSymbol() {
		String temp = currentToken;
		currentToken = Character.toString(currentLine.charAt(lineIndex));
		String type = tokenType();
		currentToken = temp;
		return type.equals("SYMBOL");
	}
	
	private boolean isDoubleQuote() {
		return currentLine.charAt(lineIndex) == '"';
	}
	
	private boolean isDigit() {
		return Character.isDigit(currentLine.charAt(lineIndex));
	}
	
	private void skipEndLineComment() {
		getNextLine();
	}
	
	private void skipBlockComment() {
		// skip /*
		lineIndex = lineIndex + 2;
		// start searching for */ at the index lineIndex forward
		// save the potential position of * in lineIndex
		// if */ not found in the currentLine, get the next line
		// we ASSUME that the comment is eventually closed
		// so we don't check whether there is a next line
		while ((lineIndex = currentLine.indexOf("*/", lineIndex)) == -1) {
			getNextLine();
		}
		// skip */
		lineIndex = lineIndex + 2;
	}
	
	private void skipWhitespace() {
		// we don't check bounds since we stripped whitespace from ends
		// therefore it's impossible to reach the end
		
		// while (currentLine.charAt(lineIndex) == ' ') {
		while (isWhitespace()) {
			++lineIndex;
		}
	}
	
	private void getString() {
		// we have lineIndex at "
		currentToken += '"';
		++lineIndex;
		
		// we ASSUME that string terminates in the same line it started
		while (currentLine.charAt(lineIndex) != '"') {
			currentToken += currentLine.charAt(lineIndex);
			++lineIndex;
		}
		
		currentToken += '"';
		++lineIndex; // skip "
		
	}
	
	private void getConstant() {
		
		while (Character.isDigit(currentLine.charAt(lineIndex))) {
			currentToken += currentLine.charAt(lineIndex);
			++lineIndex;
		}
	}
	
	private void getWord() {
		
		while (Character.isAlphabetic(currentLine.charAt(lineIndex))
				|| Character.isDigit(currentLine.charAt(lineIndex))
				|| currentLine.charAt(lineIndex) == '_')
		{
			currentToken += currentLine.charAt(lineIndex);
			++lineIndex;
		}
		
	}
	
	public String tokenType() {
		
		switch (currentToken) {
			case "class": case "constructor": case "function":
			case "method": case "field": case "static":
			case "var": case "int": case "char":
			case "boolean": case "void": case "true":
			case "false": case "null": case "this":
			case "let": case "do": case "if":
			case "else": case "while": case "return":
				return "KEYWORD";

			case "{": case "}": case "(": case ")":
			case "[": case "]": case ".": case ",":
			case ";": case "+": case "-": case "*":
			case "/": case "&": case "|": case "<":
			case ">": case "=": case "~":
				return "SYMBOL";
				
			default:
				break;
		}
		
		char firstChar = currentToken.charAt(0);
		if (Character.isDigit(firstChar)) {
			return "INT_CONST";
		} else if (firstChar == '"') {
			return "STRING_CONST";
		} else {
			return "IDENTIFIER";
		}
	}
	
	public String keyword() {
		return currentToken;
	}
	
	public String symbol() {
		return currentToken;
	}
	
	public String identifier() {
		return currentToken;
	}
	
	public int intVal() {
		return Integer.parseInt(currentToken);
	}
	
	public String stringVal() {
		// return string without double quotes at the beginning and end
		return currentToken.substring(1, currentToken.length() - 1);
	}
}
