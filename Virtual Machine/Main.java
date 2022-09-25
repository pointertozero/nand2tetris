import java.io.File;

// arithmetic operations use RAM[5] and RAM[6]
public class Main {
	
	private static void translateFile(CodeWriter output, File file, String filename) throws Exception {
		
		Parser input = new Parser(file);
		output.setFileName(filename);
		
		while (input.hasMoreCommands()) {
			
			input.advance();
			// skip empty lines
			if (input.isLineEmpty()) {
				continue;
			}
			
			output.writeLine("// " + input.DEBUGgetCommand());
			
			switch (input.commandType()) {
				case "C_ARITHMETIC":
					output.writeArithmetic(input.arg1());
					break;
				case "C_POP": case "C_PUSH":
					output.writePushPop(input.commandType(), input.arg1(), input.arg2());
					break;
				case "C_LABEL":
					output.writeLabel(input.arg1());
					break;
				case "C_GOTO":
					output.writeGoto(input.arg1());
					break;
				case "C_IF":
					output.writeIf(input.arg1());
					break;
				case "C_FUNCTION":
					output.writeFunction(input.arg1(), input.arg2());
					break;
				case "C_RETURN":
					output.writeReturn();
					break;
				case "C_CALL":
					output.writeCall(input.arg1(), input.arg2());
					break;
				default:
					System.out.println(input.commandType());
					throw new Exception("shouldn't be here");
			}
			
		}
	}

	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		CodeWriter output = new CodeWriter(args[0]); // removed init then added it again
		
		// output.setFileName(args[0]);
		
		if (file.isDirectory()) {
			
			for (File f : file.listFiles()) {
				if (f.getName().endsWith(".vm")) {
					translateFile(output, f, f.getName());
				}
			}
			
		} else {
			translateFile(output, file, args[0]);
		}
		
		output.close();
	}

}
