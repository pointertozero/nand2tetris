// package assembler;

// import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

public class Main {

	public static void main(String[] args) throws IOException {
		
		Parser input = new Parser(args[0]);
		int pc = 0; // counter
		
		while (input.hasMoreCommands()) {
			
			input.advance();
			if (input.isLineEmpty()) {
				continue;
			}
			
			if (input.commandType() == "L_COMMAND") {
				SymbolTable.addEntry(input.symbol(), pc);
			} else {
				++pc;
			}
		}
		
		
		input = new Parser(args[0]);
		FileWriter output = new FileWriter(args[1]);
		
		while (input.hasMoreCommands()) {
			
			input.advance();
			if (input.isLineEmpty()) {
				continue;
			}
			
			if (input.commandType() == "A_COMMAND") {
				
				String symbol = input.symbol();
				if (!symbol.replaceAll("\\d", "").equals("")) { // not decimal
					
					if (!SymbolTable.contains(symbol)) {
						SymbolTable.addEntry(symbol, SymbolTable.getFreeAddress());
					}
					
					symbol = Integer.toString(SymbolTable.getAddress(symbol));
				}
				
				int decimal = Integer.parseInt(symbol);
				String binary = Integer.toBinaryString(decimal);
				String binary15 = String.format("%015d", new BigInteger(binary));
				String instruction = "0" + binary15;
				
				output.write(instruction + "\n");
				
			} else if (input.commandType() == "C_COMMAND") {
				String comp = input.comp();
				String dest = input.dest();
				String jump = input.jump();
				
				String transComp = Code.comp(comp);
				String transDest = Code.dest(dest);
				String transJump = Code.jump(jump);
				
				String instruction = "111" + transComp
										   + transDest
										   + transJump
										   + "\n";
				output.write(instruction);
			}
		}
		output.close();
	}

}
