import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
	
	private FileWriter output;
	private String filename;
	private int labelCount = 0;
	private String currentFunctionName;
	private int returnCnt = 0;
	
	public CodeWriter(String filename) throws IOException {
		// this.output = new FileWriter(filename);
		// this.filename = filename;
		
		String[] splitName = filename.split("\\.");
		String outName = splitName[0] + ".asm";
		this.output = new FileWriter(outName);
		this.filename = splitName[0];
		
		// writeInit();
	}
	
	public void writeLine(String line) throws IOException {
		output.write(line + "\n");
	}
	
	public void close() throws IOException {
		output.close();
	}
	
	public void setFileName(String filename) throws IOException {
		
		String[] splitName = filename.split("\\.");
		// String outName = splitName[0] + ".asm";
		// this.output = new FileWriter(outName);
		this.filename = splitName[0];
	}
	
	private void setCurrentFunctionName(String functionName) {
		this.currentFunctionName = functionName;
	}
	
	private int getLabelCount() {
		return labelCount++;
	}
	
	private int getReturnCnt() {
		return returnCnt++;
	}
	
	public void writeArithmetic(String command) throws IOException {
		writeLine("// " + command);
		
		writePushPop("C_POP", "temp", 0);
		
		
		// writePushPop assumes @temp0 open, later on too depending on argument!
		if (command.equals("add")) {
			writePushPop("C_POP", "temp", 1);
			writeLine("D=M");
			atTemp(0);
			writeLine("D=D+M");
			writeTemp(2);
			writePushPop("C_PUSH", "temp", 2);
		} else if (command.equals("sub")) {
			writePushPop("C_POP", "temp", 1);
			writeLine("D=M");
			atTemp(0);
			writeLine("D=D-M");
			writeTemp(2);
			writePushPop("C_PUSH", "temp", 2);
		} else if (command.equals("neg")) {
			writeLine("M=-M");
			writePushPop("C_PUSH", "temp", 0);
		} else if (command.equals("eq")) {
			int cnt = getLabelCount();
			
			writePushPop("C_POP", "temp", 1);
			writeLine("D=M");
			atTemp(0);
			writeLine("D=D-M");
			writeLine("@__EQ" + cnt);
			writeLine("D;JEQ");
			
			writePushPop("C_PUSH", "constant", 0); // false
			writeLine("@__END" + cnt);
			writeLine("0;JMP");
			
			writeLine("(__EQ" + cnt + ")");
			writePushPop("C_PUSH", "constant", -1); // true
			
			writeLine("(__END" + cnt + ")");
		} else if (command.equals("gt")) {
			int cnt = getLabelCount();
			
			writePushPop("C_POP", "temp", 1);
			writeLine("D=M");
			atTemp(0);
			writeLine("D=D-M");
			writeLine("@__GT" + cnt);
			writeLine("D;JGT");
			
			writePushPop("C_PUSH", "constant", 0); // false
			writeLine("@__END" + cnt);
			writeLine("0;JMP");
			
			writeLine("(__GT" + cnt + ")");
			writePushPop("C_PUSH", "constant", -1); // true
			
			writeLine("(__END" + cnt + ")");
		} else if (command.equals("lt")) {
			int cnt = getLabelCount();
			
			writePushPop("C_POP", "temp", 1);
			writeLine("D=M");
			atTemp(0);
			writeLine("D=D-M");
			writeLine("@__LT" + cnt);
			writeLine("D;JLT");
			
			writePushPop("C_PUSH", "constant", 0); // false
			writeLine("@__END" + cnt);
			writeLine("0;JMP");
			
			writeLine("(__LT" + cnt + ")");
			writePushPop("C_PUSH", "constant", -1); // true
			
			writeLine("(__END" + cnt + ")");
		} else if (command.equals("and")) {
			writePushPop("C_POP", "temp", 1);
			writeLine("D=M");
			atTemp(0);
			writeLine("D=D&M");
			writeTemp(2);
			writePushPop("C_PUSH", "temp", 2);
		} else if (command.equals("or")) {
			writePushPop("C_POP", "temp", 1);
			writeLine("D=M");
			atTemp(0);
			writeLine("D=D|M");
			writeTemp(2);
			writePushPop("C_PUSH", "temp", 2);
		} else if (command.equals("not")) {
			writeLine("M=!M");
			writePushPop("C_PUSH", "temp", 0);
		}
	}
	
	private void atTemp(int index) throws IOException {
		// 0 <= index <= 8
		writeLine("@" + (index + 5));
	}
	
	private void atPointer(int index) throws IOException {
		// index = 0 or 1
		writeLine("@" + (3 + index));
	}
	
	private void writeTemp(int index) throws IOException {
		// 0 <= index <= 8
		writeLine("@" + (index + 5));
		writeLine("M=D");
	}
	
	private void readTemp(int index) throws IOException {
		// 0 <= index <= 8
		atTemp(index);
		writeLine("D=M");
	}
	
	private void pushToStack() throws IOException {
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
	}
	
	private void incSP() throws IOException {
		// ++SP
		writeLine("@SP");
		writeLine("M=M+1");
	}
	
	private void push() throws IOException {
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		
		// ++SP
		writeLine("@SP");
		writeLine("M=M+1");
	}
	
	private void atSegmentIndex(String segment, int index) throws IOException {
		switch (segment) {
			case "this": 
				writeLine("@THIS");
				break;
			case "that":
				writeLine("@THAT");
				break;
			case "argument":
				writeLine("@ARG");
				break;
			case "local":
				writeLine("@LCL");
				break;
			default:
				throw new IOException("shouldn't be here " + segment + " " + index);
		}
		
		writeLine("A=M");
		writeLine("A=A+D");
	}
	
	private void writePush(String segment, int index) throws IOException {
		
//		if (segment.equals("constant")) {
//			writeLine("@" + index);
//			writeLine("D=A");
//			pushToStack();
//		} else if (segment.equals("static")) {
//			// push static 3 => @filename.3
//			writeLine("@" + filename + "." + index);
//			writeLine("D=M");
//			pushToStack();
//		} else if (segment.equals("temp")) {
//			atTemp(index);
//			
//		} else if (segment.equals("this")) {
//			
//		} else if (segment.equals("that")) {
//			
//		} else if (segment.equals("pointer")) {
//			
//			
//		} else { // arg or local
//			writeLine("@" + index);
//			writeLine("D=A");
//			if (segment.equals("local"))
//			writeLine("@!!!!!SEGMENT"); // only thing changing? !!!
//			writeLine("A=M");
//			writeLine("A=A+D");
//			writeLine("D=M");
//			writeLine("@SP");
//			writeLine("A=M");
//			writeLine("M=D");
//		}
		
		
		
		if (segment.equals("static")) {
			writeLine("@" + filename + "." + index);
			writeLine("D=M");
		} else if (segment.equals("temp")) {
			readTemp(index);
		} else if (segment.equals("pointer")) { // might be wrong
			atPointer(index);
			writeLine("D=M");
		} else if (segment.equals("constant")) {
			if (index == -1) {
				// true from eq,gt,lt in arithmetic
				writeLine("D=-1");
			} else {
				writeLine("@" + index);
				writeLine("D=A");
			}
		} else { // this, that, arg, local
			writeLine("@" + index);
			writeLine("D=A");
			atSegmentIndex(segment, index);
			writeLine("D=M");
		}
		
		
		push();
	}
	
	private void pop() throws IOException {
		// --SP
		writeLine("@SP");
		writeLine("M=M-1");
		// D = *SP
		writeLine("A=M");
		writeLine("D=M");
		
	}
	
	private void writePop(String segment, int index) throws IOException {
		pop(); // think about refactoring
		
		if (segment.equals("static")) {
			writeLine("@" + filename + "." + index);
		} else if (segment.equals("temp")) {
			atTemp(index);
		} else if (segment.equals("pointer")) {
			atPointer(index);
		} else if (segment.equals("constant")) {
			throw new IOException("trying to push into constant register");
		} else { // this, that, arg, local
			
			// store *SP in @15
			writeLine("@15");
			writeLine("M=D");
			
			writeLine("@" + index);
			writeLine("D=A");
			atSegmentIndex(segment, index);
			// save address of wanted segment in @14
			writeLine("D=A");
			writeLine("@14");
			writeLine("M=D");
			
			// finally *SEGMENT = *SP
			writeLine("@15");
			writeLine("D=M");
			writeLine("@14");
			writeLine("A=M");
			// writeLine("M=D");
		}
		
		writeLine("M=D");
	}
	
	public void writePushPop(String command, String segment, int index) throws IOException {
		
		writeLine("// " + command + " " + segment + " " + index);
		
		if (command.equals("C_PUSH")) {
			// push argument 0 ??? example?
			writePush(segment, index);
		} else { // C_POP
			writePop(segment, index);
		}
	}
	
	public void writeInit() throws IOException {
		// set stack pointer
		writeLine("@256");
		writeLine("D=A");
		writeLine("@SP");
		writeLine("M=D");
		
		writeCall("Sys.init", 0);
	}
	
	public void writeLabel(String label) throws IOException {
		writeLine("(" + currentFunctionName + "$" + label + ")");
	}
	
	private void atLabel(String label) throws IOException {
		writeLine("@" + currentFunctionName + "$" + label);
	}
	
	public void writeGoto(String label) throws IOException {
		atLabel(label);
		writeLine("0;JMP");
	}
	
	public void writeIf(String label) throws IOException {
		pop();
		atLabel(label);
		writeLine("D;JNE"); // 
	}
	
	public void writeCall(String functionName, int numArgs) throws IOException {
		String returnAddress = currentFunctionName + "$ret." + getReturnCnt();
		
		writeLine("@" + returnAddress);
		// think about it
		writeLine("D=A");
		push();
		
		writeLine("@LCL");
		writeLine("D=M");
		push();
		
		writeLine("@ARG");
		writeLine("D=M");
		push();
		
		writeLine("@THIS");
		writeLine("D=M");
		push();
		
		writeLine("@THAT");
		writeLine("D=M");
		push();
		
		// ARG = SP - numArgs - 5
		writeLine("@SP");
		writeLine("D=M");
		writeLine("@" + numArgs);
		writeLine("D=D-A");
		writeLine("@5");
		writeLine("D=D-A");
		writeLine("@ARG");
		writeLine("M=D");
		
		// LCL = SP
		writeLine("@SP");
		writeLine("D=M");
		writeLine("@LCL");
		writeLine("M=D");
		
		writeLine("@" + functionName);
		writeLine("0;JMP");
		
		writeLine("(" + returnAddress + ")");
	}
	
	private void getSavedState(int n) {
		// 1 <= n <= 5
		// D = *(FRAME - 5)
		
		
	}
	
	public void writeReturn() throws IOException {
		// D = LCL
		writeLine("@LCL");
		writeLine("D=M");
		// FRAME = LCL
		writeLine("@__FRAME");
		writeLine("M=D");
		// D = FRAME - 5
		writeLine("@5");
		writeLine("D=D-A");
		// D = *D (= *(FRAME - 5))
		writeLine("A=D");
		writeLine("D=M");
		// RET = D (= *(FRAME - 5))
		writeLine("@RET");
		writeLine("M=D");
		
		// *ARG = pop()
		pop(); // pops from stack into D
		writeLine("@ARG");
		writeLine("A=M");
		writeLine("M=D");
		
		// D = ARG + 1
		writeLine("@ARG");
		writeLine("D=M+1");
		// SP = D (= ARG + 1)
		writeLine("@SP");
		writeLine("M=D");
		
		// THAT
		writeLine("@__FRAME");
		// @(__FRAME - 1)
		writeLine("A=M-1");
		// D = *(__FRAME - 1)
		writeLine("D=M");
		// THAT = *(__FRAME - 1)
		writeLine("@THAT");
		writeLine("M=D");
		
		// THIS
		writeLine("@__FRAME");
		writeLine("D=M");
		writeLine("@2");
		// @*(__FRAME - 2)
		writeLine("A=D-A");
		writeLine("D=M");
		
		writeLine("@THIS");
		writeLine("M=D");
		
		
		// ARG
		writeLine("@__FRAME");
		writeLine("D=M");
		writeLine("@3");

		
		writeLine("A=D-A");
		writeLine("D=M");
		
		writeLine("@ARG");
		writeLine("M=D");
		
		
		// LCL
		writeLine("@__FRAME");
		writeLine("D=M");
		writeLine("@4");

		
		writeLine("A=D-A");
		writeLine("D=M");
		
		writeLine("@LCL");
		writeLine("M=D");
		
		
		// writeGoto("RET");
		
		// jump back
		// revise, might be possible to write better
		writeLine("@RET");
		writeLine("A=M"); // so the address is after @
		writeLine("0;JMP");
		
	}
	
	public void writeFunction(String functionName, int numLocals) throws IOException {
		writeLine("(" + functionName + ")");
		setCurrentFunctionName(functionName); // might be wrong
		
		// loop counter for filling local segment
		// stored in D
		writeLine("@" + numLocals);
		writeLine("D=A");
		
		writeLabel("INIT_LCL");
		// jump if filled local segment
		atLabel("__END"); // think about changing name
		writeLine("D;JEQ");
		
		writeLine("D=D-1");
		
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=0");
		incSP();
		
		writeGoto("INIT_LCL");
		
		
		
		writeLabel("__END");
	}
}
