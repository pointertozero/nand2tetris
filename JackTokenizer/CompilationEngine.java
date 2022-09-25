import java.io.File;
// import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


// can try to make compiler more expressive
// put everything in try/catch to show syntax errors
public class CompilationEngine {
	private JackTokenizer tokenizer;
	private FileWriter output;
	private Error syntaxError = new Error("syntax error");
	private String margin = "";
	
	public CompilationEngine(File inputFile, File outputFile) throws IOException {
		this.tokenizer = new JackTokenizer(inputFile);
		this.output = new FileWriter(outputFile);
	}
	
	private void printLine(String s) throws IOException {
		output.write(margin + s + "\n");
	}
	
	private void outputTerminal(String value, String label) throws IOException {
		printLine("<" + label + "> " + value + " </" + label + ">");
	}
	
	private void outputStartLabel(String label) throws IOException {
		printLine("<" + label + ">");
	}
	
	private void outputEndLabel(String label) throws IOException {
		printLine("</" + label + ">");
	}
	
	private void increaseMargin() {
		margin += "  "; // two spaces
	}
	
	private void decreaseMargin() {
		margin = margin.substring(0, margin.length() - 2); // two spaces
	}
	
	private void mustAdvance() {
		
		if (tokenizer.hasMoreTokens()) {
			tokenizer.advance();
		} else {
			throw syntaxError;
		}
	}
	
	public void compileClass() throws IOException {
		// useless?
		if (!tokenizer.hasMoreTokens()) {
			return;
			// throw new Error("empty file");
		}
		
		tokenizer.advance();
		
		outputStartLabel("class");
		increaseMargin();
		
		// class
		compileKeyword("class");
		
		// mustAdvance();
		
		// class name
		compileIdentifier();
		
		
		// mustAdvance();
		
		// {
		compileSymbol("{");
		
		// mustAdvance();
		
		compileClassVarDecs();
		
		// mustAdvance();
		
		compileSubroutines();
		
		// }
		// compileSymbol("}"); <------- written manually, end of input
		// not advancing over
		
		if (isSymbol()
			&& isSymbol("}"))
		{
			outputTerminal("}", "symbol");
		} else {
			throw syntaxError;
		}
		
		// maybe advance here?
		
		decreaseMargin();
		outputEndLabel("class");
		
	}
	
	private void compileSubroutines() throws IOException {
		
		while (isKeyword()
				&& (isKeyword("constructor")
					|| isKeyword("function")
					|| isKeyword("method")))
		{
			compileSubroutine();
		}
	}
	
	private void compileSubroutine() throws IOException {
		
		outputStartLabel("subroutineDec");
		increaseMargin();
		
		// constructor | function | method
		if (isKeyword()
			&& (isKeyword("constructor")
				|| isKeyword("function")
				|| isKeyword("method")))
		{
			outputTerminal(tokenizer.keyword(), "keyword");
		} else {
			throw syntaxError;
		}
		
		mustAdvance();
		
		// void | type
		if (isKeyword()
			&& isKeyword("void"))
		{
			outputTerminal("void", "keyword");
			mustAdvance();
		} else {
			compileType();
		}
		
		// subroutineName
		compileIdentifier();
		
		// (
		compileSymbol("(");
		
		// parameterList
		compileParameterList();
		
		// )
		compileSymbol(")");
		
		// subroutineBody
		compileSubroutineBody();
		
		decreaseMargin();
		outputEndLabel("subroutineDec");
	}
	
	private void compileSubroutineBody() throws IOException {
		
		outputStartLabel("subroutineBody");
		increaseMargin();
		
		// {
		compileSymbol("{");
		
		// varDec*
		compileVarDecs();
		// statements
		compileStatements();
		
		// }
		compileSymbol("}");
		
		decreaseMargin();
		outputEndLabel("subroutineBody");
	}
	
	private void compileVarDecs() throws IOException {
		
		while (isKeyword()
				&& isKeyword("var"))
		{
			compileVarDec();
		}
		
	}
	
	private boolean isOperator() {
		return isSymbol()
				&& (isSymbol("+")
					|| isSymbol("-")
					|| isSymbol("*")
					|| isSymbol("/")
					|| isSymbol("&")
					|| isSymbol("|")
					|| isSymbol("<")
					|| isSymbol(">")
					|| isSymbol("="));
	}
	
	private boolean isUnaryOperator() {
		return isSymbol()
				&& (isSymbol("-")
					|| isSymbol("~"));
	}
	
	private void compileTerm() throws IOException {
		outputStartLabel("term");
		increaseMargin();
		
		// outputTerminal(tokenizer.identifier(), tokenizer.tokenType().toLowerCase());
		// mustAdvance();
		
		if (isIntegerConstant()) {
			// int const
			outputTerminal(tokenizer.intVal(), "integerConstant");
			mustAdvance();
		} else if (isStringConstant()) {
			// string
			outputTerminal(tokenizer.stringVal(), "stringConstant");
			mustAdvance();
		} else if (isKeywordConstant()) {
			// true/false/null/this
			outputTerminal(tokenizer.keyword(), "keyword");
			mustAdvance();
		} else if (isUnaryOperator()) {
			// unary operator
			outputTerminal(tokenizer.symbol(), "symbol");
			mustAdvance();
			// term
			compileTerm();
		} else if (isSymbol("(")) {
			// ( expression )
			compileSymbol("(");
			compileExpression();
			compileSymbol(")");
		} else if (isIdentifier()) {
			// varName | varName [ expression ] | subroutineCall
			
			compileIdentifier();
			
			if (isSymbol("[")) {
				// varName [ expression ]
				outputTerminal("[", "symbol");
				mustAdvance();
				
				compileExpression();
				compileSymbol("]");
			} else if (isSymbol("(")) {
				// subroutineCall
				
				outputTerminal("(", "symbol");
				mustAdvance();
					
				compileExpressionList();
				compileSymbol(")");
			} else if (isSymbol(".")) {
				outputTerminal(".", "symbol");
				mustAdvance();
					
				// subroutineName ( expressionList ) left
				// as expected from compileSubroutineCall()
				compileSubroutineCall(); // that would be problematic if subroutineCall had label
			}
			
		} else {
			throw syntaxError;
		}
		
		decreaseMargin();
		outputEndLabel("term");
	}
	
	private void compileExpressionList() throws IOException {
		outputStartLabel("expressionList");
		increaseMargin();
		
		// non-empty expression list
		if (isExpression()) {
			// expression
			compileExpression();
		
		
			// (, expression)*
			while (isSymbol()
				&& isSymbol(","))
			{
				// ,
				outputTerminal(",", "symbol");
				mustAdvance();
				// expression
				compileExpression();
			}
		}
		
		decreaseMargin();
		outputEndLabel("expressionList");
	}
	
	private void compileExpression() throws IOException {
		outputStartLabel("expression");
		increaseMargin();
		
		// term
		compileTerm();
		
		// (op term)*
		while (isOperator()) {
			// op
			outputTerminal(tokenizer.symbol(), "symbol");
			mustAdvance();
			// term
			compileTerm();
		}
		
		
		
		decreaseMargin();
		outputEndLabel("expression");
	}
	
	private boolean isStatement() {
		return isKeyword()
				&& (isKeyword("let")
					|| isKeyword("if")
					|| isKeyword("while")
					|| isKeyword("do")
					|| isKeyword("return"));
	}
	
	private void compileStatements() throws IOException {
		outputStartLabel("statements");
		increaseMargin();
		
		while (isStatement()) {
			compileStatement();
		}
		
		decreaseMargin();
		outputEndLabel("statements");
	}
	
	private void compileStatement() throws IOException {
		// called after isStatement returns true
		
		if (isKeyword("let")) {
			compileLet();
		} else if (isKeyword("if")) {
			compileIf();
		} else if (isKeyword("while")) {
			compileWhile();
		} else if (isKeyword("do")) {
			compileDo();
		} else if (isKeyword("return")) {
			compileReturn();
		}
		
		
	}
	
	private void compileSubroutineCall() throws IOException {
		
		// subroutineName
		// | className
		// | varName
		compileIdentifier();
		
		// only if className or varName
		if (isSymbol()
			&& isSymbol("."))
		{
			outputTerminal(".", "symbol");
			mustAdvance();
			
			// subroutineName
			compileIdentifier();
		}
		
		// (
		compileSymbol("(");
		// expressionList
		compileExpressionList();
		// )
		compileSymbol(")");
	}
	
	private void compileDo() throws IOException {
		outputStartLabel("doStatement");
		increaseMargin();
		
		// do
		compileKeyword("do");
		// subroutineCall
		compileSubroutineCall();
		
		// ;
		compileSymbol(";");
		
		decreaseMargin();
		outputEndLabel("doStatement");
	}
	
	private void compileLet() throws IOException {
		outputStartLabel("letStatement");
		increaseMargin();
		
		// let
		compileKeyword("let");
		// varName
		compileVarName();
		// ( [ expression ] )?
		if (isSymbol()
			&& isSymbol("["))
		{
			// [
			compileSymbol("[");
			// expression
			compileExpression();
			// ]
			compileSymbol("]");
		}
		
		// =
		compileSymbol("=");
		// expression
		compileExpression();
		// ;
		compileSymbol(";");
		
		decreaseMargin();
		outputEndLabel("letStatement");
	}
	
	private void compileIf() throws IOException {
		outputStartLabel("ifStatement");
		increaseMargin();
		
		// if
		compileKeyword("if");
		// (
		compileSymbol("(");
		// expression
		compileExpression();
		// )
		compileSymbol(")");
		
		// {
		compileSymbol("{");
		// statements
		compileStatements();
		// }
		compileSymbol("}");
		
		// optional else clause
		if (isKeyword()
			&& isKeyword("else"))
		{
			// else
			outputTerminal("else", "keyword");
			mustAdvance();
			
			// {
			compileSymbol("{");
			// statements
			compileStatements();
			// }
			compileSymbol("}");
		}
		
		decreaseMargin();
		outputEndLabel("ifStatement");
		
	}

	private void compileWhile() throws IOException {
		outputStartLabel("whileStatement");
		increaseMargin();
		
		// while
		compileKeyword("while");
		// (
		compileSymbol("(");
		// expression
		compileExpression();
		// )
		compileSymbol(")");
		
		// {
		compileSymbol("{");
		// statements
		compileStatements();
		// }
		compileSymbol("}");
		
		
		decreaseMargin();
		outputEndLabel("whileStatement");
	}
	
	private boolean isTerm() {
		// TODO DONE?
		return isIntegerConstant()
				|| isStringConstant()
				|| isKeywordConstant()
				|| isIdentifier()
				|| isSymbol("(") // ( expression )
				|| isUnaryOperator();
	}
	
	private boolean isExpression() {
		return isTerm();
	}
	
	
	private void compileReturn() throws IOException {
		outputStartLabel("returnStatement");
		increaseMargin();
		
		// return
		compileKeyword("return");
		
		// expression?
		if (isExpression()) {
			compileExpression();
		}
		
		// ;
		compileSymbol(";");
		
		decreaseMargin();
		outputEndLabel("returnStatement");
	}
	
	private void compileVarDec() throws IOException {
		
		outputStartLabel("varDec");
		increaseMargin();
		
		
		// var
		compileKeyword("var");
		// type
		compileType();
		// varName
		compileVarName();
		
		while (isSymbol()
				&& isSymbol(","))
		{
			// ,
			outputTerminal(",", "symbol");
			mustAdvance();
			// varName
			compileVarName();
		}
		
		// ;
		compileSymbol(";");
		
		
		decreaseMargin();
		outputEndLabel("varDec");
	}
	
	private void compileParameterList() throws IOException {
		
		outputStartLabel("parameterList");
		increaseMargin();
		
		// non-empty parameter list
		if (!(isSymbol()
				&& isSymbol(")"))) {
			
			// type
			compileType();
			// varName
			compileVarName();
			
			while (isSymbol()
					&& isSymbol(","))
			{
				outputTerminal(",", "symbol");
				mustAdvance();
				
				// type
				compileType();
				// varName
				compileVarName();
			}
		}
		
		decreaseMargin();
		outputEndLabel("parameterList");
	}
	
	private void compileClassVarDec() throws IOException {
		// called from compileClassVarDecs()
		outputStartLabel("classVarDec");
		
		increaseMargin();
		// already checked, static/field
		outputTerminal(tokenizer.keyword(), "keyword");
		
		mustAdvance();
		
		
		compileType();
		
		
		// mustAdvance();
		
		// varName: identifier
		compileVarName();
		
		// mustAdvance();
		
		while (isSymbol()
				&& isSymbol(","))
		{
			outputTerminal(",", "symbol");
			mustAdvance();
			
			
			compileVarName();
			
			// mustAdvance();
		}
		
		compileSymbol(";");
		
		// mustAdvance();
		
		decreaseMargin();
		
		outputEndLabel("classVarDec");
	}
	
	private void compileVarName() throws IOException {
		
		// varName: identifier
		compileIdentifier();
	}
	
	private void compileSymbol(String symbol) throws IOException {
		
		if (isSymbol()
			&& isSymbol(symbol))
		{
			outputTerminal(symbol, "symbol");
		} else {
			throw syntaxError;
		}
		
		mustAdvance();
	}
	
	private void compileKeyword(String keyword) throws IOException {
		
		if (isKeyword()
			&& isKeyword(keyword))
		{
			outputTerminal(keyword, "keyword");
		} else {
			throw syntaxError;
		}
		
		mustAdvance();
	}
	
	private void compileIdentifier() throws IOException {
		
		if (isIdentifier()) {
			outputTerminal(tokenizer.identifier(), "identifier");
		} else {
			throw syntaxError;
		}
		
		mustAdvance();
	}
	
	private void compileType() throws IOException {
		
		// type: int/char/boolean, keyword
		if (isKeyword()
			&& (isKeyword("int")
				|| isKeyword("char")
				|| isKeyword("boolean")))
		{
			outputTerminal(tokenizer.keyword(), "keyword");
			
		} else if (isIdentifier()) {
			// type: className, identifier
			outputTerminal(tokenizer.identifier(), "identifier");
		} else {
			throw syntaxError;
		}
		
		mustAdvance();
	}
	
	private boolean isKeyword() {
		return tokenizer.tokenType().equals("KEYWORD");
	}
	
	private boolean isKeyword(String keywordName) {
		return tokenizer.keyword().equals(keywordName);
	}
	
	private boolean isSymbol() {
		return tokenizer.tokenType().equals("SYMBOL");
	}
	
	private boolean isSymbol(String symbol) {
		return tokenizer.symbol().equals(symbol);
	}
	
	private boolean isIdentifier() {
		return tokenizer.tokenType().equals("IDENTIFIER");
	}
	
	private boolean isIntegerConstant() {
		return tokenizer.tokenType().equals("INT_CONST");
	}
	
	private boolean isStringConstant() {
		return tokenizer.tokenType().equals("STRING_CONST");
	}
	
	private boolean isKeywordConstant() {
		return isKeyword()
				&& (isKeyword("true")
					|| isKeyword("false")
					|| isKeyword("null")
					|| isKeyword("this"));
	}
	
	public void DEBUGclose() throws IOException {
		output.close();
	}
	
	private void compileClassVarDecs() throws IOException {
		
		while (isKeyword()
				&& (isKeyword("static")
					|| isKeyword("field")))
		{
			compileClassVarDec();
		}
		
	}
}
