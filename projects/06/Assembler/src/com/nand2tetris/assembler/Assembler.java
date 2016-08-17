package com.nand2tetris.assembler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

public class Assembler {
	static int romAddr;
	static int ramAddr;
	static SymbolTable symbolTable;
	static Parser ps;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// filename = "Add1100.asm";
		// filename = "Add.asm";
		// filename = "Max.asm";
		// filename = "Pong.asm";
		
		String filenames[] = {"Add", "Add1100", "Max", "Pong", "Rect"};
		
		for (int i = 0; i < filenames.length; i++)
			generateMachineCode(filenames[i]);
		
	}
	
	static void generateMachineCode(String filename) {

		OutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		
		romAddr = 0; // ROM starts at 0
		ramAddr = 16; // RAM starts at 16

		symbolTable = new SymbolTable(); // New SymbolTable

		
		String machineCode;

		try {
			// First Pass
			ps = new Parser(filename);
			while (ps.hasMoreCommands()) {
				ps.advance();
				ps.commandType();
				if (ps.commandType == ps.A_COMMAND) {
					ps.symbol();
					romAddr++;
				} else if (ps.commandType == ps.C_COMMAND) {
					ps.parseC_COMMAND();
					romAddr++;
				} else if (ps.commandType == ps.L_COMMAND) {  // Add addr in ROM of next instruction (for jump/goto)
					ps.symbol();
					symbolTable.addEntry(ps.symbol, romAddr);
				}
			}

			
			// Second Pass
			fos = new FileOutputStream(filename + ".hack");  // open file for writing result
			osw = new OutputStreamWriter(fos);
			bw = new BufferedWriter(osw);
			ps = new Parser(filename);
			while (ps.hasMoreCommands()) {
				ps.advance();
				ps.commandType();
				if (ps.commandType == ps.A_COMMAND) {  // Replace or add new RAM addr for this variable into Symbol Table
					ps.symbol();
					if (isStringANumber(ps.symbol) == false) {  // Not a Number, must be a symbol
						String replacedSymbol = "";
						
						if (symbolTable.contains(ps.symbol)) {
							replacedSymbol = String.valueOf(symbolTable.getAddress(ps.symbol));
						} else {
							symbolTable.addEntry(ps.symbol, ramAddr++);  // Assign a new address in RAM for new variable
							replacedSymbol = String.valueOf(symbolTable.getAddress(ps.symbol));
						}
						ps.symbol = replacedSymbol;
					}
				} else if (ps.commandType == ps.C_COMMAND) {
					ps.parseC_COMMAND();
				} else if (ps.commandType == ps.L_COMMAND) {  
					ps.symbol();
				}
				if ((machineCode = ps.toMachineCode()).length() > 0) {
					//System.out.println(machineCode);
					bw.write(machineCode + "\n");
				}
			}
			
			bw.close();
			osw.close();
			fos.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static boolean isStringANumber(String str) {
		boolean result = true;
		try {
			Integer.parseInt(str);
		} catch (Exception e) {
			result = false;
		}
		return result;
	}
}

class Parser {

	public final int A_COMMAND = 0;
	public final int C_COMMAND = 1;
	public final int L_COMMAND = 2;
	public final int COMMENT = 3;
	public String filename;
	public InputStream fis;
	public InputStreamReader isr;
	public BufferedReader br;
	public String line;
	public String instruction;
	public String destMnemonic;
	public String compMnemonic;
	public String jumpMnemonic;
	public String symbol;
	public int commandType;
	public String machineCode;
	public int lineNo;

	public Code myCode;

	public Parser(String fname) {
		
		filename = fname;
		lineNo = 0;
		try {
			fis = new FileInputStream(filename + ".asm");
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		
			myCode = new Code();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	boolean hasMoreCommands() throws IOException {
		return ((line = br.readLine()) != null);
	}

	void advance() {
		if (line.length() > 0 && line.indexOf("//") > 0)
			line = line.substring(0, line.indexOf("//"));
		instruction = line.trim();
		lineNo++;
	}

	int commandType() {
		if (instruction.length() == 0)
			return -1;
		if (instruction.charAt(0) == '@') {
			// System.out.println(lineNo + ": " + "A_COMMAND");
			this.commandType = A_COMMAND;
			return this.commandType;
		} else if (instruction.charAt(0) == '(') {
			// System.out.println(lineNo + ": " + "L_COMMAND");
			this.commandType = L_COMMAND;
			return this.commandType;
		} else if (instruction.charAt(0) == '/') {
			// System.out.println(lineNo + ": " + "COMMENT");
			this.commandType = COMMENT;
			return this.commandType;
		} else {
			// System.out.println(lineNo + ": " + "C_COMMAND");
			this.commandType = C_COMMAND;
			return this.commandType;
		}
	}

	String symbol() {
		if (commandType == A_COMMAND) {
			this.symbol = instruction.substring(1);
			return this.symbol;
		} else if (commandType == L_COMMAND) {
			this.symbol = instruction.substring(1, instruction.length() - 1);
			return symbol;
		} else {
			return "";
		}
	}

	void parseC_COMMAND() {
		destMnemonic = "";
		compMnemonic = "";
		jumpMnemonic = "";

		if (commandType == C_COMMAND) {
			if (instruction.indexOf('=') != -1 && instruction.indexOf(';') != -1) {
				destMnemonic = instruction.substring(0, instruction.indexOf('='));
				compMnemonic = instruction.substring(instruction.indexOf('=') + 1, instruction.indexOf(';'));
				jumpMnemonic = instruction.substring(instruction.indexOf(';') + 1);
			} else if (instruction.indexOf('=') != -1) {
				destMnemonic = instruction.substring(0, instruction.indexOf('='));
				compMnemonic = instruction.substring(instruction.indexOf('=') + 1);
			} else if (instruction.indexOf(';') != -1) {
				compMnemonic = instruction.substring(0, instruction.indexOf(';'));
				jumpMnemonic = instruction.substring(instruction.indexOf(';') + 1);
			} else {
				destMnemonic = "";
				compMnemonic = "";
				jumpMnemonic = "";
			}
		}
	}

	String dest() {
		return destMnemonic;
	}

	String comp() {
		return compMnemonic;
	}

	String jump() {
		return jumpMnemonic;
	}

	String toMachineCode() {
		String dest = "";
		String comp = "";
		String jump = "";
		String result = "";

		if (this.commandType == A_COMMAND) {
			result = myCode.decToBinary(this.symbol);
		} else if (this.commandType == C_COMMAND) {
			dest = myCode.dest(this.dest());
			comp = myCode.comp(this.comp());
			jump = myCode.jump(this.jump());
			result = "111" + comp + dest + jump;
		}

		return result;
	}
}

class SymbolTable {
	Hashtable<String, Integer> symbolTable;

	public SymbolTable() {
		symbolTable = new Hashtable<String, Integer>();

		this.addEntry("SP", 0);
		this.addEntry("LCL", 1);
		this.addEntry("ARG", 2);
		this.addEntry("THIS", 3);
		this.addEntry("THAT", 4);

		this.addEntry("R0", 0);
		this.addEntry("R1", 1);
		this.addEntry("R2", 2);
		this.addEntry("R3", 3);
		this.addEntry("R4", 4);
		this.addEntry("R5", 5);
		this.addEntry("R6", 6);
		this.addEntry("R7", 7);
		this.addEntry("R8", 8);
		this.addEntry("R9", 9);
		this.addEntry("R10", 10);
		this.addEntry("R11", 11);
		this.addEntry("R12", 12);
		this.addEntry("R13", 13);
		this.addEntry("R14", 14);
		this.addEntry("R15", 15);

		this.addEntry("SCREEN", 16384);
		this.addEntry("KBD", 24576);
	}

	boolean contains(String symbol) {
		return symbolTable.containsKey(symbol);
	}

	void addEntry(String symbol, int address) {
		symbolTable.put(symbol, address);
	}

	int getAddress(String symbol) {
		return symbolTable.get(symbol);
	}
}

class Code {
	public Code() {
	}

	public String decToBinary(String num) {
		String result = Integer.toBinaryString(Integer.valueOf(num));
		int len = 16 - result.length();

		for (int i = 0; i < len; i++)
			result = "0" + result;

		return result;
	}

	public String dest(String destMnemonic) {
		switch (destMnemonic) {
		case "":
			return "000";
		case "M":
			return "001";
		case "D":
			return "010";
		case "MD":
			return "011";
		case "A":
			return "100";
		case "AM":
			return "101";
		case "AD":
			return "110";
		case "AMD":
			return "111";
		default:
			return "000";
		}
	}

	public String comp(String compMnemonic) {
		switch (compMnemonic) {
		case "":
			return "xxxxxxx";
		default:
			return "xxxxxxx";
		// a=0
		case "0":
			return "0101010";
		case "1":
			return "0111111";
		case "-1":
			return "0111010";
		case "D":
			return "0001100";
		case "A":
			return "0110000";
		case "!D":
			return "0001101";
		case "!A":
			return "0110001";
		case "-D":
			return "0001111";
		case "-A":
			return "0110011";
		case "D+1":
			return "0011111";
		case "A+1":
			return "0110111";
		case "D-1":
			return "0001110";
		case "A-1":
			return "0110010";
		case "D+A":
			return "0000010";
		case "D-A":
			return "0010011";
		case "A-D":
			return "0000111";
		case "D&A":
			return "0000000";
		case "D|A":
			return "0010101";
		// a=1
		case "M":
			return "1110000";
		case "!M":
			return "1110001";
		case "-M":
			return "1110011";
		case "M+1":
			return "1110111";
		case "M-1":
			return "1110010";
		case "D+M":
			return "1000010";
		case "D-M":
			return "1010011";
		case "M-D":
			return "1000111";
		case "D&M":
			return "1000000";
		case "D|M":
			return "1010101";
		}
	}

	public String jump(String jumpMnemonic) {
		switch (jumpMnemonic) {
		case "":
			return "000";
		case "JGT":
			return "001";
		case "JEQ":
			return "010";
		case "JGE":
			return "011";
		case "JLT":
			return "100";
		case "JNE":
			return "101";
		case "JLE":
			return "110";
		case "JMP":
			return "111";
		default:
			return "000";
		}
	}
}