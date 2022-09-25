import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
	private FileWriter output;
	
	// can take file/stream/name
	public VMWriter(File outputFile) throws IOException {
		this.output = new FileWriter(outputFile);
	}
	
	private void printLine(String line) throws IOException {
		output.write(line + '\n');
	}
	
	public void close() throws IOException {
		output.close();
	}
	
	public void writePush(String segment, int index) throws IOException {
		// push segment index
		printLine("push " + segment + ' ' + index);
	}
	
	public void writePop(String segment, int index) throws IOException {
		// pop segment index
		printLine("pop " + segment + ' ' + index);
	}
	
	public void writeArithmetic(String command) throws IOException {
		printLine(command);
	}
	
	public void writeLabel(String label) throws IOException {
		printLine("label " + label);
	}
	
	public void writeGoto(String label) throws IOException {
		printLine("goto " + label);
	}
	
	public void writeIf(String label) throws IOException {
		// write if-goto
		printLine("if-goto " + label);
	}
	
	public void writeCall(String functionName, int nArgs) throws IOException {
		printLine("call " + functionName + ' ' + nArgs);
	}
	
	public void writeFunction(String functionName, int nLocals) throws IOException {
		printLine("function " + functionName + ' ' + nLocals);
	}
	
	public void writeReturn() throws IOException {
		printLine("return");
	}
}
