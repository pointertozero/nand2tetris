// package assembler;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
	private Scanner reader;
	private String currentLine;
	
	public Parser(String filename) throws FileNotFoundException {
		File file = new File(filename);
		this.reader = new Scanner(file);
		// this.currentLine = null;
	}
	
	public boolean hasMoreCommands() {
		return reader.hasNextLine();
	}
	
	public void advance() {
		String readLine = reader.nextLine();
		readLine = readLine.replaceAll("\\s", ""); // remove whitespace
		readLine = readLine.replaceAll("//.*", ""); // remove comments
		
		currentLine = readLine;
	}
	
	public boolean isLineEmpty() {
		return currentLine.isEmpty();
	}
	
	public String commandType() {
		char firstChar = currentLine.charAt(0);
		if (firstChar == '@') {
			return "A_COMMAND";
		} else if (firstChar == '(') {
			return "L_COMMAND";
		} else {
			return "C_COMMAND";
		}
	}
	
	public String symbol() {
		if (commandType() == "A_COMMAND") {
			return currentLine.substring(1);
		} else { // assumes L_COMMAND
			return currentLine.substring(1, currentLine.length() - 1); // remove parentheses
		}
	}
	
	public String dest() {
		// assumes C_COMMAND
		String[] s = currentLine.split("=");
		if (s.length == 1) { // no dest
			return "null";
		} else {
			return splitEqualSign()[0];
		}
	}
	
	private String[] splitEqualSign() {
		return currentLine.split("=");
	}
	
	private String compAndJump() {
		// return String.join("", splitEqualSign());
		String[] s = splitEqualSign();
		return s[s.length - 1]; // get last element if no dest
	}
	
	public String comp() {
		// assumes C_COMMAND
		// System.out.println(compAndJump());
		return compAndJump().split(";")[0];
	}
	
	public String jump() {
		// assumes C_COMMAND
		String[] s = compAndJump().split(";");
		if (s.length == 1) { // no jump
			return "null";
		} else {
			return s[1];
		}
	}
}
