import java.io.File;
import java.io.IOException;


// TODO: can try to make compiler more expressive
// put everything in try/catch to show syntax errors
public class CompilationEngine {
	private JackTokenizer tokenizer;
	private VMWriter output;
	private SymbolTable symbolTable;
	private Error syntaxError = new Error("syntax error");
	private String className;
	private int labelCnt = 0;
	
	public CompilationEngine(File inputFile, File outputFile) throws IOException {
		this.tokenizer = new JackTokenizer(inputFile);
		this.output = new VMWriter(outputFile);
		this.className = inputFile.getName().split("\\.")[0];
		this.symbolTable = new SymbolTable();
	}
	private String createUniqueLabel() {
		return "L" + (labelCnt++);
	}
	private void mustAdvance() {
		
		if (tokenizer.hasMoreTokens()) {
			tokenizer.advance();
		} else {
			throw syntaxError;
		}
	}
	
	private void validate(String expected, String actual) {
		if (!expected.equals(actual)) {
			throw new Error("syntax error\n"
							+ "expected: '" + expected + "'\n"
							+ "actual: '" + actual + "'");
		}
	}
	
	public void compileClass() throws IOException {

		if (!tokenizer.hasMoreTokens()) {
			return;
			// throw new Error("empty file");
		}
		
		tokenizer.advance();
		
		// compileKeyword("class");
		validate("class", tokenizer.keyword());
		mustAdvance();
		
		
		// class name
		// compileIdentifier();
		validate(className, tokenizer.identifier());
		mustAdvance();
		
		// {
		// compileSymbol("{");
		validate("{", tokenizer.symbol());
		mustAdvance();
		
		
		compileClassVarDecs();
		
		
		compileSubroutines();
		
		
		// compileSymbol("}"); <------- written manually, end of input
		// not advancing over
		// }
		validate("}", tokenizer.symbol());
		
		
		
		// close output
		output.close();
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
		
		symbolTable.startSubroutine();
		
		// constructor | function | method
		if (isKeyword()) {
			
			if (isKeyword("constructor")) {
				compileConstructor();
			} else if (isKeyword("function")) {
				compileFunction();
			} else if (isKeyword("method")) {
				compileMethod();
			} else {
				throw syntaxError;
			}
		}
		
	}
	
	private void compileFunction() throws IOException {
		
		// function
		mustAdvance();
		
		// void | type
		mustAdvance();
		
		// subroutineName
		String subroutineName = className + "." + tokenizer.identifier();
		mustAdvance();
		
		// (
		validate("(", tokenizer.symbol());
		mustAdvance();
		
		compileParameterList();
		
		// )
		validate(")", tokenizer.symbol());
		mustAdvance();
		
		
		// subroutineBody
		// {
		validate("{", tokenizer.symbol());
		mustAdvance();
		
		// varDec*
		compileVarDecs();
		// function subroutineName localVarCount
		output.writeFunction(subroutineName, symbolTable.varCount("VAR"));
		// statements
		compileStatements();
		
		// }
		validate("}", tokenizer.symbol());
		mustAdvance();
		
		
		
	}
	
	private void compileMethod() throws IOException {
		
		// method
		mustAdvance();
		// void | type
		mustAdvance();
		
		// subroutineName
		String subroutineName = className + "." + tokenizer.identifier();
		mustAdvance();
		
		// (
		validate("(", tokenizer.symbol());
		mustAdvance();
		
		// save this as first argument
		symbolTable.define("this", className, "ARG");
		// get explicit parameters
		compileParameterList();
		
		// )
		validate(")", tokenizer.symbol());
		mustAdvance();
		
		
		// subroutineBody
		// {
		validate("{", tokenizer.symbol());
		mustAdvance();
		
		// varDec*
		compileVarDecs();
		// function subroutineName localVarCount
		output.writeFunction(subroutineName, symbolTable.varCount("VAR"));
		
		// set this
		output.writePush("argument", 0);
		output.writePop("pointer", 0);
		
		// statements
		compileStatements();
		
		// }
		validate("}", tokenizer.symbol());
		mustAdvance();
	}
	
	private void compileConstructor() throws IOException {
		// DONE? allocate space
		// push size
		// call Memory.alloc
		// pop pointer 0
		
		// constructor
		mustAdvance();
		
		// className
		validate(className, tokenizer.identifier());
		mustAdvance();
		
		// subroutineName
		String subroutineName = className + "." + tokenizer.identifier();
		mustAdvance();
		
		// (
		validate("(", tokenizer.symbol());
		mustAdvance();
		
		compileParameterList();
		
		// )
		validate(")", tokenizer.symbol());
		mustAdvance();
		
		
		// subroutineBody
		// {
		validate("{", tokenizer.symbol());
		mustAdvance();
		
		// varDec*
		compileVarDecs();
		// function subroutineName localVarCount
		output.writeFunction(subroutineName, symbolTable.varCount("VAR"));
		// allocate space
		output.writePush("constant", symbolTable.varCount("FIELD")); // size
		output.writeCall("Memory.alloc", 1);
		output.writePop("pointer", 0);
		// statements
		compileStatements();
		
		// }
		validate("}", tokenizer.symbol());
		mustAdvance();
		
		
		
	}
	
	private void compileVarDecs() throws IOException {
		// compile varDecs
		
		while (isKeyword()
				&& isKeyword("var"))
		{
			// compile var declarations
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
		
		if (isIntegerConstant()) {
			// int const
			output.writePush("constant", tokenizer.intVal());
			mustAdvance();
		} else if (isStringConstant()) {
			
			final String stringConst = tokenizer.stringVal();
			
			output.writePush("constant", stringConst.length());
			output.writeCall("String.new", 1);
			for (char c : stringConst.toCharArray()) {
				output.writePush("constant", c); // char gets implicitly casted to int
				output.writeCall("String.appendChar", 2);
			}
			mustAdvance();
		} else if (isKeywordConstant()) {
			// true/false/null/this
			
			if (isKeyword("false") || isKeyword("null")) {
				output.writePush("constant", 0);
			} else if (isKeyword("true")) {
				output.writePush("constant", 1);
				output.writeArithmetic("neg");
			} else { // this
				output.writePush("pointer", 0); // push address of current object
			}
			
			mustAdvance();
		} else if (isUnaryOperator()) {
			// unary operator
			String unaryOp = tokenizer.symbol();
			mustAdvance();
			
			// term pushed on the stack
			compileTerm();
			// apply the operator
			applyUnaryOperator(unaryOp);
			
		} else if (isSymbol("(")) {
			// ( expression )
			mustAdvance();
			
			compileExpression();
			
			validate(")", tokenizer.symbol());
			mustAdvance();
		} else if (isIdentifier()) {
			// varName | varName [ expression ] | subroutineCall
			
			String name = tokenizer.identifier();
			final String[] typeKindIndex = symbolTable.findTypeKindIndex(name);
			mustAdvance();
			
			if (isSymbol("[")) {
				// varName [ expression ] -- ARRAY
				
				// [
				mustAdvance();
				
				// pushes variable to the stack, depending on the kind
				pushVariable(typeKindIndex);
					
				
				compileExpression(); // pushed value of expression in the stack
				
				output.writeArithmetic("add"); // varName + expressionResult
				output.writePop("pointer", 1); // set "that" segment to correct place in memory
				output.writePush("that", 0); // push value of varName[expressionResult] to the stack
				
				validate("]", tokenizer.symbol());
				mustAdvance();
			} else if (isSymbol("(")) {
				// subroutineName ( expressionList )
				// so method
				
				// (
				mustAdvance();
				
				name = this.className + "." + name;
				
				// push this
				// we ASSUME that we are inside of a method
				// and "this" is first argument by contract
				output.writePush("pointer", 0);
				// pushes values to the stack
				int nArgs = compileExpressionList();
				
				// call the function
				output.writeCall(name, nArgs + 1); // number of args + this
				
				// )
				validate(")", tokenizer.symbol());
				mustAdvance();
			} else if (isSymbol(".")) {
				// .
				mustAdvance();
				
				
				if (typeKindIndex == null) {
					// className.subroutineName
					
					name += "." + tokenizer.identifier();
					mustAdvance();
					
					// (
					validate("(", tokenizer.symbol());
					mustAdvance();
					
					
					final int nArgs = compileExpressionList();
					
					output.writeCall(name, nArgs);
					
					// )
					validate(")", tokenizer.symbol());
					mustAdvance();
				} else {
					// varName.subroutineName
					
					name = typeKindIndex[0]; // set name as the name of the class of var
					name += "." + tokenizer.identifier();
					mustAdvance();
					
					// (
					validate("(", tokenizer.symbol());
					mustAdvance();
					
					// push this
					pushVariable(typeKindIndex);
					final int nArgs = compileExpressionList();
					
					output.writeCall(name, nArgs + 1); // + this
					
					// )
					validate(")", tokenizer.symbol());
					mustAdvance();
				}
				
				
			} else {
				// simply varName
				
				pushVariable(typeKindIndex);
			}
			
		} else {
			throw syntaxError;
		}
		
		
	}
	
	private void pushVariable(String[] typeKindIndex) throws IOException {
		
		final int index = Integer.parseInt(typeKindIndex[2]);
		
		switch (typeKindIndex[1]) {
		case "STATIC":
			output.writePush("static", index);
			break;
		case "FIELD":
			output.writePush("this", index);
			break;
		case "VAR":
			output.writePush("local", index);
			break;
		case "ARG":
			output.writePush("argument", index);
			break;
		default:
			throw new Error("shouldn't be here");
		}
	}
	
	private int compileExpressionList() throws IOException {
		// in parentheses next to subroutine call
		// pushes expressions to the stack and counts them
		
		int cnt = 0;
		
		// non-empty expression list
		if (isExpression()) {
			// expression
			compileExpression();
			
			++cnt;
			// (, expression)*
			while (isSymbol()
					&& isSymbol(","))
			{
				// ,
				mustAdvance();
			
				// expression
				compileExpression();
				++cnt;
			}
		}
		
		return cnt;
	}
	
	private void compileExpression() throws IOException {
		// term
		compileTerm();
		
		// (op term)*
		while (isOperator()) {
			// op
			String op = tokenizer.symbol();
			mustAdvance();
			// term
			compileTerm();
			applyBinaryOperator(op);
		}
		
	}
	
	private void applyUnaryOperator(String op) throws IOException {
		
		switch (op) {
			case "-":
				output.writeArithmetic("neg");
				break;
			case "~":
				output.writeArithmetic("not");
				break;
			default:
				throw new Error("shouldn't be here\n"
								+ "unary operator: " + op);
		}
	}
	
	private void applyBinaryOperator(String op) throws IOException {
		
		switch (op) {
			case "+":
				output.writeArithmetic("add");
				break;
			case "-":
				output.writeArithmetic("sub");
				break;
			case "*":
				output.writeCall("Math.multiply", 2);
				break;
			case "/":
				output.writeCall("Math.divide", 2);
				break;
			case "&":
				output.writeArithmetic("and");
				break;
			case "|":
				output.writeArithmetic("or");
				break;
			case "<":
				output.writeArithmetic("lt");
				break;
			case ">":
				output.writeArithmetic("gt");
				break;
			case "=":
				output.writeArithmetic("eq");
				break;
			default:
				throw new Error("shouldn't be here, op: " + op);
		}
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
		
		while (isStatement()) {
			compileStatement();
		}
		
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
		String name = tokenizer.identifier();
		final String[] typeKindIndex = symbolTable.findTypeKindIndex(name);
		mustAdvance();
		
		if (typeKindIndex == null) {
			// className | subroutineName
			
			if (isSymbol() && isSymbol(".")) {
				// function
				
				// .
				mustAdvance();
				
				// subroutineName
				name += "." + tokenizer.identifier(); // className.subroutineName
				mustAdvance();
				
				// (
				validate("(", tokenizer.symbol());
				mustAdvance();
				
				// expressionList
				final int nArgs = compileExpressionList();
				
				// )
				validate(")", tokenizer.symbol());
				mustAdvance();
				
				output.writeCall(name, nArgs);
				
			} else {
				// method
				// called in method, implicit this from arg 0
				
				name = this.className + "." + name; // currentClass.subroutineName
				
				// (
				validate("(", tokenizer.symbol());
				mustAdvance();
				
				
				// push this
				output.writePush("pointer", 0);
				// expressionList
				final int nArgs = compileExpressionList();
				
				// )
				validate(")", tokenizer.symbol());
				mustAdvance();
				
				output.writeCall(name, nArgs + 1);
			}
		} else {
			// method
			name = typeKindIndex[0]; // change name to the name of the class
			
			validate(".", tokenizer.symbol());
			mustAdvance();
			
			// subroutineName
			name += "." + tokenizer.identifier();
			mustAdvance();
			
			// (
			validate("(", tokenizer.symbol());
			mustAdvance();
			
			
			// push this
			pushVariable(typeKindIndex);
			// expressionList
			final int nArgs = compileExpressionList();
			
			// )
			validate(")", tokenizer.symbol());
			mustAdvance();
			
			output.writeCall(name, nArgs + 1);
			
		}
	}
	
	private void compileDo() throws IOException {
		
		// do
		validate("do", tokenizer.keyword());
		mustAdvance();
		// subroutineCall
		compileSubroutineCall();
		
		// get rid of dummy return (constant 0)
		output.writePop("temp", 0);
		
		// ;
		// compileSymbol(";");
		validate(";", tokenizer.symbol());
		mustAdvance();
		
	}
	
	private void compileLet() throws IOException {
		// let
		validate("let", tokenizer.keyword());
		mustAdvance();
		// varName
		final String varName = tokenizer.identifier();
		mustAdvance();
		
		// ( [ expression ] )?
		if (isSymbol()
			&& isSymbol("["))
		{
			// array
			
			final String segment = symbolTable.getSegmentName(varName);
			final int index = symbolTable.indexOf(varName);
			
			// push pointer to first element of array
			output.writePush(segment, index);
			
			// [
			mustAdvance();
			// expression
			compileExpression(); // index pushed on the stack
			// ]
			validate("]", tokenizer.symbol());
			mustAdvance();
			
			output.writeArithmetic("add");
			
			// = 
			validate("=", tokenizer.symbol());
			mustAdvance();
			
			compileExpression();
			
			output.writePop("temp", 0); // store the value in temp
			
			output.writePop("pointer", 1); // set that segment
			
			output.writePush("temp", 0);
			output.writePop("that", 0);
			
			
		} else {
			// defining new variable or updating
			
			// = 
			validate("=", tokenizer.symbol());
			mustAdvance();
			
			// expression
			compileExpression();
			output.writePop(symbolTable.getSegmentName(varName), symbolTable.indexOf(varName));
			
		}
		
		
		// ;
		validate(";", tokenizer.symbol());
		mustAdvance();
		
	}
	
	private void compileIf() throws IOException {
		String label1 = createUniqueLabel();
		String label2 = createUniqueLabel();
		
		// if
		validate("if", tokenizer.keyword());
		mustAdvance();
		// (
		validate("(", tokenizer.symbol());
		mustAdvance();
		// expression
		compileExpression();
		output.writeArithmetic("not");
		// )
		validate(")", tokenizer.symbol());
		mustAdvance();
		
		output.writeIf(label1);
		
		// {
		validate("{", tokenizer.symbol());
		mustAdvance();
		// statements
		compileStatements();
		// }
		validate("}", tokenizer.symbol());
		mustAdvance();
		
		output.writeGoto(label2);
		
		output.writeLabel(label1);
		
		// optional else clause
		if (isKeyword()
			&& isKeyword("else"))
		{
			// else
			mustAdvance();
			
			// {
			validate("{", tokenizer.symbol());
			mustAdvance();
			// statements
			compileStatements();
			// }
			validate("}", tokenizer.symbol());
			mustAdvance();
		}
		
		output.writeLabel(label2);
		
	}

	private void compileWhile() throws IOException {
		String label1 = createUniqueLabel();
		String label2 = createUniqueLabel();
		
		output.writeLabel(label1);
		// while
		validate("while", tokenizer.keyword());
		mustAdvance();
		// (
		validate("(", tokenizer.symbol());
		mustAdvance();
		// expression
		compileExpression();
		output.writeArithmetic("not"); // not expression
		// )
		validate(")", tokenizer.symbol());
		mustAdvance();
		
		output.writeIf(label2);
		
		// {
		validate("{", tokenizer.symbol());
		mustAdvance();
		// statements
		compileStatements();
		// }
		validate("}", tokenizer.symbol());
		mustAdvance();
		
		output.writeGoto(label1);
		
		output.writeLabel(label2);
	}
	
	private boolean isTerm() {
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
		// return
		validate("return", tokenizer.keyword());
		mustAdvance();
		
		// expression?
		if (isExpression()) {
			compileExpression();
		} else {
			output.writePush("constant", 0);
		}
		
		
		output.writeReturn();
		
		// ;
		validate(";", tokenizer.symbol());
		mustAdvance();
	}
	
	private void compileVarDec()  {
		
		final String kind = "VAR";
		
		// var
		validate(tokenizer.keyword(), "var");
		mustAdvance();
		// type; builtin types or identifier
		final String type = tokenizer.identifier();
		mustAdvance();
		// varName
		String varName = tokenizer.identifier();
		mustAdvance();
		
		symbolTable.define(varName, type, kind);
		
		
		while (isSymbol()
				&& isSymbol(","))
		{
			// ,
			mustAdvance();
			// varName
			varName = tokenizer.identifier();
			mustAdvance();
			symbolTable.define(varName, type, kind);
			
		}
		
		// ;
		validate(";", tokenizer.symbol());
		mustAdvance();
		
	}
	
	private void compileParameterList() {
		// "this" handled outside
		
		// non-empty parameter list
		if (!(isSymbol()
				&& isSymbol(")"))) {
			
			final String kind = "ARG";
			
			// type
			String type = tokenizer.identifier();
			mustAdvance();
			
			// varName
			String varName = tokenizer.identifier();
			mustAdvance();
			
			symbolTable.define(varName, type, kind);
			
			while (isSymbol()
					&& isSymbol(","))
			{
				// ,
				mustAdvance();
				
				// type
				type = tokenizer.identifier();
				mustAdvance();
				// varName
				varName = tokenizer.identifier();
				mustAdvance();
				
				symbolTable.define(varName, type, kind);
			}
		}
		
		
	}
	
	private void compileClassVarDec() {
		// called from compileClassVarDecs()
		
		// already checked, static/field
		final String kind = tokenizer.keyword().toUpperCase();
		mustAdvance();
		
		
		final String type = tokenizer.identifier();
		mustAdvance();
		
		
		// varName
		final String name = tokenizer.identifier();
		mustAdvance();
		
		symbolTable.define(name, type, kind);
		
		while (isSymbol()
				&& isSymbol(","))
		{
			// ,
			mustAdvance();
			
			// varName
			symbolTable.define(tokenizer.identifier(), type, kind);
			mustAdvance();
		}
		
		// ;
		validate(";", tokenizer.symbol());
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
	
	private void compileClassVarDecs() {
		
		while (isKeyword()
				&& (isKeyword("static")
					|| isKeyword("field")))
		{
			compileClassVarDec();
		}
		
	}
}
