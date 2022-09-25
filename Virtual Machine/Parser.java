import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
	private Scanner reader;
	private String[] currentLine;
	
	public Parser(File file) throws FileNotFoundException {
		// File file = new File(filename);
		this.reader = new Scanner(file);
	}
	
	public boolean hasMoreCommands() {
		return reader.hasNextLine();
	}
	
	public String DEBUGgetCommand() {
		return String.join(" ", currentLine);
	}
	
	public void advance() {
		String readLine = reader.nextLine();
		readLine = readLine.replaceAll("//.*", ""); // remove comments
		// readLine = readLine.replaceAll("\\s+$", ""); // remove whitespace at the end
		// readLine = readLine.replaceAll("^\\s+", ""); // remove whitespace at the beginning
		readLine = readLine.trim(); // remove trailing whitespace
		
		currentLine = readLine.split("\\s+");
	}
	
	public boolean isLineEmpty() {
		return currentLine[0].isEmpty();
	}
	
	public String commandType() throws Exception {
		assert isLineEmpty() == false;
		
		if (currentLine.length == 1 && !currentLine[0].equals("return")) {
			return "C_ARITHMETIC";
		} else if (currentLine[0].equals("pop")) {
			return "C_POP";
		} else if (currentLine[0].equals("push")) {
			return "C_PUSH";
		} else if (currentLine[0].equals("label")) {
			return "C_LABEL";
		} else if (currentLine[0].equals("goto")) {
			return "C_GOTO";
		} else if (currentLine[0].equals("if-goto")) {
			return "C_IF";
		} else if (currentLine[0].equals("function")) {
			return "C_FUNCTION";
		} else if (currentLine[0].equals("return")) {
			return "C_RETURN";
		} else if (currentLine[0].equals("call")) {
			return "C_CALL";
		} else {
			throw new Exception("something wrong, shouldn't be here");
		}
	}
	
	public String arg1() throws Exception {
		assert !commandType().equals("C_RETURN") : "can't pass return command to arg1()";
		
		if (commandType().equals("C_ARITHMETIC")) {
			return currentLine[0];
		} else {
			return currentLine[1];
		}
	}
	
	public int arg2() {
		
		return Integer.parseInt(currentLine[2]);
	}
	
	
}
