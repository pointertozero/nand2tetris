import java.io.File;
import java.io.IOException;

public class JackCompiler {
	
	private static void compileFile(File inputFile) throws IOException {
		
		// xxx.jack --> xxx.vm
		String outputFileName = inputFile.getName().split("\\.")[0] + ".vm";
		File outputFile = new File(outputFileName);
		
		CompilationEngine compiler = new CompilationEngine(inputFile, outputFile);
		
		compiler.compileClass();
	}
	
	public static void main(String[] args) throws IOException {
		
		if (args.length != 1) {
			System.out.println("Usage: JackCompiler Foo.jack | directory");
			return;
		}
		
		File inputFile = new File(args[0]);
		
		if (!inputFile.exists()) {
			System.out.println("File or directory doesn't exist");
			return;
		}
		
		if (inputFile.isDirectory()) {
			for (File f : inputFile.listFiles()) {
				if (f.getName().endsWith(".jack")) {
					compileFile(f);
				}
			}
		} else {
			// file
			
			if (!args[0].endsWith(".jack")) {
				System.out.println("JackCompiler accepts only .jack files");
				return;
			}
			
			compileFile(inputFile);
		}
	}

}
