import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.FileWriter;
import java.io.IOException;

public class JackAnalyzer {
	// TODO: close the output; possibly:
	// PUT output.close inside of compileclass
	private static void analyzeFile(File inputFile, File outputFile) throws IOException {
		CompilationEngine compiler = new CompilationEngine(inputFile, outputFile);
		try {
			compiler.compileClass();
		} catch (Error e) {
			compiler.DEBUGclose();
		} finally {
			compiler.DEBUGclose();
		}
	}
	
	public static void main(String[] args) throws IOException {
		File inputFile = new File(args[0]);
		// File outputFile = new File(args[0].split(".")[0] + ".xml");
		
		// FileWriter d = new FileWriter();
		// JackTokenizer tokenizer = new JackTokenizer(inputFile);
		// CompilationEngine compiler = new CompilationEngine(inputFile, outputFile);
		
		if (inputFile.isDirectory()) {
			
			for (File f : inputFile.listFiles()) {
				if (f.getName().endsWith(".jack")) {
					File outputFile = new File(f.getName().split("\\.")[0] + ".xml");
					analyzeFile(f, outputFile);
				}
			}
		} else {
			File outputFile = new File(args[0].split("\\.")[0] + ".xml");
			
			analyzeFile(inputFile, outputFile);
		}
		
		//while (tokenizer.hasMoreTokens()) {
		//	tokenizer.advance();
			
		//	System.out.println(tokenizer.tokenType() + ": " + tokenizer.DEBUGgetCurrentToken() + "\n");
		//}
	}
}
