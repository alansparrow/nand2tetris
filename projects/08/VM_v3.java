package nand2tetris.virtualmachine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		VirtualMachine myVM = new VirtualMachine();
		CodeWriter myCodeWriter = new CodeWriter();
		Shared.SOURCE_PATH = "/home/Study/nand2tetris-master/projects/08/FunctionCalls/StaticsTest";
		Shared.OUTPUT_FILE_NAME = "StaticsTest.asm";
		myCodeWriter.setFileName();
		myCodeWriter.writeInit();

		File folder = new File(Shared.SOURCE_PATH);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			String fileName = listOfFiles[i].getName();
			if (listOfFiles[i].isFile() && fileName.contains(".vm")) { // only
																		// compile
																		// .vm
																		// files
				System.out.println("Compiled " + Shared.SOURCE_PATH + "/" + fileName);
				myCodeWriter.setClassName(fileName); // set class name to make
														// label
				myVM.translateToAssembly(fileName, myCodeWriter);
			}
		}
		myCodeWriter.closeFile();
	}
}

class VirtualMachine {
	public VirtualMachine() {

	}

	void translateToAssembly(String fileName, CodeWriter myCodeWriter) throws IOException {
		Parser myParser = new Parser(fileName);

		while (myParser.hasMoreCommands()) {
			myParser.advance();
			if (myParser.commandType() != Shared.C_NONSENSE) {
				myCodeWriter.writeInstructionComment(myParser.instruction);
				if (myParser.commandType() == Shared.C_ARITHMETIC) {
					myCodeWriter.writeArithmetic(myParser.arg1());
				} else if (myParser.commandType() == Shared.C_PUSH) {
					myCodeWriter.writePushPop("push", myParser.arg1(), myParser.arg2());
				} else if (myParser.commandType() == Shared.C_POP) {
					myCodeWriter.writePushPop("pop", myParser.arg1(), myParser.arg2());
				} else if (myParser.commandType() == Shared.C_LABEL) {
					myCodeWriter.writeLabel(myParser.arg1());
				} else if (myParser.commandType() == Shared.C_GOTO) {
					myCodeWriter.writeGoto(myParser.arg1());
				} else if (myParser.commandType() == Shared.C_IF) {
					myCodeWriter.writeIf(myParser.arg1());
				} else if (myParser.commandType() == Shared.C_FUNCTION) {
					Shared.FUNCTION_SCOPE = myParser.arg1() + "."; // Change
																	// every
																	// time
																	// meets new
																	// function
																	// command
																	// (take its
																	// function
																	// name),
																	// reset to
																	// default
																	// every
																	// time
																	// meets
																	// return
																	// command.
																	// Label has
																	// function
																	// scope
					myCodeWriter.writeFunction(myParser.arg1(), myParser.arg2());
				} else if (myParser.commandType() == Shared.C_CALL) {
					myCodeWriter.writeCall(myParser.arg1(), myParser.arg2());
				} else if (myParser.commandType() == Shared.C_RETURN) {
					// Shared.FUNCTION_SCOPE = Shared.FUNCTION_SCOPE_DEFAULT;
					myCodeWriter.writeReturn();
				}
			}
		}
	}
}

class Parser {
	String filename;
	String line;
	String instruction;
	String[] instructionTokens;
	int lineNo;
	File f;
	InputStream fis;
	InputStreamReader isr;
	BufferedReader br;

	public Parser(String filename) {
		this.filename = filename;
		try {
			f = new File(Shared.SOURCE_PATH + "/" + filename);
			fis = new FileInputStream(f);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		} catch (Exception e) {
			System.out.println(e);
		}

		// Set initial values
		line = "";
		instruction = "";
		lineNo = 0;
	}

	boolean hasMoreCommands() throws IOException {
		return ((line = br.readLine()) != null);
	}

	void advance() {
		if (line.length() > 0 && line.indexOf("//") > 0)
			line = line.substring(0, line.indexOf("//"));
		instruction = line.trim();
		instructionTokens = instruction.split("\\s");
		lineNo++;
	}

	int commandType() {
		int result = Shared.C_NONSENSE;

		if (instruction.length() == 0)
			return result;

		if (instructionTokens.length == 1) { // arithmetic related command: add,
												// sub... | function related
												// command: return
			if (instructionTokens[0].equals("return")) {
				return Shared.C_RETURN;
			} else {
				return Shared.C_ARITHMETIC;
			}
		} else if (instructionTokens.length == 2) { // label label, goto label,
													// if-goto label
			if (instructionTokens[0].equals("label")) {
				return Shared.C_LABEL;
			} else if (instructionTokens[0].equals("goto")) {
				return Shared.C_GOTO;
			} else if (instructionTokens[0].equals("if-goto")) {
				return Shared.C_IF;
			}
		} else if (instructionTokens.length == 3) { // function related command:
													// call f n, function f k
			if (instructionTokens[0].equals("push")) { // or stack related
														// function: push
														// segment index, pop
														// segment index
				return Shared.C_PUSH;
			} else if (instructionTokens[0].equals("pop")) {
				return Shared.C_POP;
			} else if (instructionTokens[0].equals("call")) {
				return Shared.C_CALL;
			} else if (instructionTokens[0].equals("function")) {
				return Shared.C_FUNCTION;
			}
		}

		return result;
	}

	String arg1() {
		String result = "";
		int cmdType = this.commandType();

		if (cmdType != Shared.C_NONSENSE) {
			if (cmdType != Shared.C_RETURN) {
				if (cmdType == Shared.C_ARITHMETIC) {
					result = instructionTokens[0]; // In the case of
													// C_ARITHMETIC, the command
													// itself is returned (add,
													// sub, etc)
				} else {
					result = instructionTokens[1];
				}
			}
		}

		return result;
	}

	String arg2() {
		String result = "";
		int cmdType = this.commandType();

		if (cmdType != Shared.C_NONSENSE) {
			if (cmdType == Shared.C_PUSH || cmdType == Shared.C_POP || cmdType == Shared.C_FUNCTION
					|| cmdType == Shared.C_CALL) {
				result = instructionTokens[2];
			}
		}

		return result;
	}

}

class CodeWriter {
	File f;
	OutputStream fos;
	OutputStreamWriter osw;
	BufferedWriter bw;
	String className;

	public CodeWriter() {

	}

	void setFileName() throws FileNotFoundException {
		f = new File(Shared.SOURCE_PATH + "/" + Shared.OUTPUT_FILE_NAME);
		fos = new FileOutputStream(f); // open file for writing result
		osw = new OutputStreamWriter(fos);
		bw = new BufferedWriter(osw);

	}

	void setClassName(String fileName) {
		className = fileName.split("\\.")[0];
	}

	void writeInstructionComment(String instructionTxt) throws IOException {
		bw.write("//-------" + instructionTxt + "-------\n");
	}

	// Bootstrap code. This code must be placed at the beginning of the output
	// file
	void writeInit() throws IOException {

		bw.write("@256\n");
		bw.write("D=A\n");
		bw.write("@SP\n");
		bw.write("M=D\n"); // Set SP=RAM[0]=256
		bw.write("@0\n");
		bw.write("D=A\n");
		bw.write("@LCL\n");
		bw.write("M=D\n"); // Set LCL=RAM[1]=0, just random value
		bw.write("@0\n");
		bw.write("D=A\n");
		bw.write("@ARG\n");
		bw.write("M=D\n"); // Set ARG=RAM[2]=0, just random value
		bw.write("@0\n");
		bw.write("D=A\n");
		bw.write("@THIS\n");
		bw.write("M=D\n"); // Set LCL=RAM[3]=0, just random value
		bw.write("@0\n");
		bw.write("D=A\n");
		bw.write("@THAT\n");
		bw.write("M=D\n"); // Set LCL=RAM[4]=0, just random value

		// n = 0, so we don't push any argument on the stack. Normally, calling
		// a function f after n arguments have been pushed onto the stack
		writeCall("Sys.init", "0");

	}

	void writeArithmetic(String command) throws IOException {
		String uniqueString = String.valueOf(System.nanoTime());
		if (command.equals("add")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("M=M-1\n"); // sp = sp - 1; return stack[sp] or return
									// stack[--sp]
			bw.write("A=M\n"); // set A register to address of the second
								// operand
			bw.write("D=M\n"); // set D register to value of second operand
			bw.write("A=A-1\n"); // set A register to address of the first
									// operand
			bw.write("M=D+M\n"); // calculate sum and store the result in stack
									// (this has same effect as "pop, pop,
									// calculate, push"
		} else if (command.equals("sub")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("M=M-1\n"); // sp = sp - 1; return stack[sp] or return
									// stack[--sp]
			bw.write("A=M\n"); // set A register to address of the second
								// operand
			bw.write("D=M\n"); // set D register to value of second operand
			bw.write("A=A-1\n"); // set A register to address of the first
									// operand
			bw.write("M=M-D\n"); // calculate sum and store the result in stack
									// (this has same effect as "pop, pop,
									// calculate, push"
		} else if (command.equals("neg")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("A=M\n"); // set A register to address of the topmost
								// location in the stack
			bw.write("A=A-1\n"); // set A register to address of the operand
			bw.write("M=-M\n");
		} else if (command.equals("eq")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("M=M-1\n"); // sp = sp - 1; return stack[sp] or return
									// stack[--sp]
			bw.write("A=M\n"); // set A register to address of the second
								// operand
			bw.write("D=M\n"); // set D register to value of second operand
			bw.write("A=A-1\n"); // set A register to address of the first
									// operand
			bw.write("D=M-D\n"); // a == b ?
			bw.write("M=0\n"); // set result to False: 0x0000 or
								// 0000000000000000, a != b
			bw.write("@END" + uniqueString + "\n"); // if a != b then keep it
													// False
			bw.write("D;JNE\n");
			bw.write("@SP\n"); // else fix to True
			bw.write("A=M-1\n"); // set A register to address of the top item in
									// the stack
			bw.write("M=-1\n"); // set result to True: 0xFFFF or
								// 1111111111111111, a == b
			bw.write("(END" + uniqueString + ")\n"); // Just decoy
			bw.write("@SP\n"); // This does nothing
		} else if (command.equals("gt")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("M=M-1\n"); // sp = sp - 1; return stack[sp] or return
									// stack[--sp]
			bw.write("A=M\n"); // set A register to address of the second
								// operand
			bw.write("D=M\n"); // set D register to value of second operand
			bw.write("A=A-1\n"); // set A register to address of the first
									// operand
			bw.write("D=M-D\n"); // a > b ?
			bw.write("M=0\n"); // set result to False: 0x0000 or
								// 0000000000000000, a <= b
			bw.write("@END" + uniqueString + "\n"); // if a <= b then keep it
													// False
			bw.write("D;JLE\n");
			bw.write("@SP\n"); // else fix to True
			bw.write("A=M-1\n"); // set A register to address of the top item in
									// the stack
			bw.write("M=-1\n"); // set result to True: 0xFFFF or
								// 1111111111111111, a > b
			bw.write("(END" + uniqueString + ")\n"); // Just decoy
			bw.write("@SP\n"); // This does nothing
		} else if (command.equals("lt")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("M=M-1\n"); // sp = sp - 1; return stack[sp] or return
									// stack[--sp]
			bw.write("A=M\n"); // set A register to address of the second
								// operand
			bw.write("D=M\n"); // set D register to value of second operand
			bw.write("A=A-1\n"); // set A register to address of the first
									// operand
			bw.write("D=M-D\n"); // a < b ?
			bw.write("M=0\n"); // set result to False: 0x0000 or
								// 0000000000000000, a >= b
			bw.write("@END" + uniqueString + "\n"); // if a >= b then keep it
													// False
			bw.write("D;JGE\n");
			bw.write("@SP\n"); // else fix to True
			bw.write("A=M-1\n"); // set A register to address of the top item in
									// the stack
			bw.write("M=-1\n"); // set result to True: 0xFFFF or
								// 1111111111111111, a < b
			bw.write("(END" + uniqueString + ")\n"); // Just decoy
			bw.write("@SP\n"); // This does nothing
		} else if (command.equals("and")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("M=M-1\n"); // sp = sp - 1; return stack[sp] or return
									// stack[--sp]
			bw.write("A=M\n"); // set A register to address of the second
								// operand
			bw.write("D=M\n"); // set D register to value of second operand
			bw.write("A=A-1\n"); // set A register to address of the first
									// operand
			bw.write("M=D&M\n"); // calculate & and store the result in stack
									// (this has same effect as "pop, pop,
									// calculate, push"
		} else if (command.equals("or")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("M=M-1\n"); // sp = sp - 1; return stack[sp] or return
									// stack[--sp]
			bw.write("A=M\n"); // set A register to address of the second
								// operand
			bw.write("D=M\n"); // set D register to value of second operand
			bw.write("A=A-1\n"); // set A register to address of the first
									// operand
			bw.write("M=D|M\n"); // calculate | and store the result in stack
									// (this has same effect as "pop, pop,
									// calculate, push"
		} else if (command.equals("not")) {
			bw.write("@SP\n"); // get address of the next topmost location in
								// the stack
			bw.write("A=M\n"); // set A register to address of the topmost
								// location in the stack
			bw.write("A=A-1\n"); // set A register to address of the operand
			bw.write("M=!M\n");
		}

	}

	void writePushPop(String command, String segment, String index) throws IOException {
		if (command.equals("push")) {
			if (segment.equals("constant")) {
				bw.write("@" + index + "\n");
				bw.write("D=A\n");
				bw.write("@SP\n");
				bw.write("A=M\n");
				bw.write("M=D\n");
				bw.write("@SP\n");
				bw.write("M=M+1\n");
			} else if (segment.equals("local")) {
				bw.write("@" + index + "\n");
				bw.write("D=A\n"); // set i
				bw.write("@LCL\n"); // set register A to LOCAL segment pointer
				bw.write("A=D+M\n"); // (base + i)
				bw.write("D=M\n");
				bw.write("@SP\n"); // set register A to STACK segment pointer
				bw.write("A=M\n"); // set register A to topmost location of the
									// stack
				bw.write("M=D\n"); // push stack
				bw.write("@SP\n");
				bw.write("M=M+1\n"); // increase 1

			} else if (segment.equals("argument")) {
				bw.write("@" + index + "\n");
				bw.write("D=A\n"); // set i
				bw.write("@ARG\n"); // set register A to ARGUMENT segment
									// pointer
				bw.write("A=D+M\n"); // (base + i)
				bw.write("D=M\n");
				bw.write("@SP\n"); // set register A to STACK segment pointer
				bw.write("A=M\n"); // set register A to topmost location of the
									// stack
				bw.write("M=D\n"); // push stack
				bw.write("@SP\n");
				bw.write("M=M+1\n"); // increase 1
			} else if (segment.equals("this")) {
				bw.write("@" + index + "\n");
				bw.write("D=A\n"); // set i
				bw.write("@THIS\n"); // set register A to THIS segment pointer
				bw.write("A=D+M\n"); // (base + i)
				bw.write("D=M\n");
				bw.write("@SP\n"); // set register A to STACK segment pointer
				bw.write("A=M\n"); // set register A to topmost location of the
									// stack
				bw.write("M=D\n"); // push stack
				bw.write("@SP\n");
				bw.write("M=M+1\n"); // increase 1
			} else if (segment.equals("that")) {
				bw.write("@" + index + "\n");
				bw.write("D=A\n"); // set i
				bw.write("@THAT\n"); // set register A to THAT segment pointer
				bw.write("A=D+M\n"); // (base + i)
				bw.write("D=M\n");
				bw.write("@SP\n"); // set register A to STACK segment pointer
				bw.write("A=M\n"); // set register A to topmost location of the
									// stack
				bw.write("M=D\n"); // push stack
				bw.write("@SP\n");
				bw.write("M=M+1\n"); // increase 1
			} else if (segment.equals("pointer")) {
				String newAddress = String.valueOf(3 + Integer.valueOf(index)); // pointer
																				// is
																				// mapped
																				// on
																				// RAM
																				// location
																				// 3~4,
																				// p142
				bw.write("@" + newAddress + "\n");
				bw.write("D=M\n");
				bw.write("@SP\n"); // set register A to STACK segment pointer
				bw.write("A=M\n"); // set register A to topmost location of the
									// stack
				bw.write("M=D\n"); // push stack
				bw.write("@SP\n");
				bw.write("M=M+1\n"); // increase 1
			} else if (segment.equals("temp")) {
				String newAddress = String.valueOf(5 + Integer.valueOf(index)); // pointer
																				// is
																				// mapped
																				// on
																				// RAM
																				// location
																				// 5~12,
																				// p142
				bw.write("@" + newAddress + "\n");
				bw.write("D=M\n");
				bw.write("@SP\n"); // set register A to STACK segment pointer
				bw.write("A=M\n"); // set register A to topmost location of the
									// stack
				bw.write("M=D\n"); // push stack
				bw.write("@SP\n");
				bw.write("M=M+1\n"); // increase 1
			} else if (segment.equals("static")) {
				String newAddress = className + "." + index; // The trick: p.143
				bw.write("@" + newAddress + "\n");
				bw.write("D=M\n");
				bw.write("@SP\n"); // set register A to STACK segment pointer
				bw.write("A=M\n"); // set register A to topmost location of the
									// stack
				bw.write("M=D\n"); // push stack
				bw.write("@SP\n");
				bw.write("M=M+1\n"); // increase 1
			}
		} else if (command.equals("pop")) {
			if (segment.equals("local")) {
				bw.write("@SP\n");
				bw.write("M=M-1\n");
				bw.write("A=M\n");
				bw.write("D=M\n"); // pop from stack
				bw.write("@R13\n"); // put into tempt location
				bw.write("M=D\n");

				bw.write("@" + index + "\n");
				bw.write("D=A\n");
				bw.write("@LCL\n");
				bw.write("D=D+M\n");
				bw.write("@R14\n");
				bw.write("M=D\n"); // get desired local address and put into
									// tempt location

				bw.write("@R13\n");
				bw.write("D=M\n");
				bw.write("@R14\n");
				bw.write("A=M\n");
				bw.write("M=D\n");

			} else if (segment.equals("argument")) {
				bw.write("@SP\n");
				bw.write("M=M-1\n");
				bw.write("A=M\n");
				bw.write("D=M\n"); // pop from stack
				bw.write("@R13\n"); // put into tempt location
				bw.write("M=D\n");

				bw.write("@" + index + "\n");
				bw.write("D=A\n");
				bw.write("@ARG\n");
				bw.write("D=D+M\n");
				bw.write("@R14\n");
				bw.write("M=D\n"); // get desired local address and put into
									// tempt location

				bw.write("@R13\n");
				bw.write("D=M\n");
				bw.write("@R14\n");
				bw.write("A=M\n");
				bw.write("M=D\n");
			} else if (segment.equals("this")) {
				bw.write("@SP\n");
				bw.write("M=M-1\n");
				bw.write("A=M\n");
				bw.write("D=M\n"); // pop from stack
				bw.write("@R13\n"); // put into tempt location
				bw.write("M=D\n");

				bw.write("@" + index + "\n");
				bw.write("D=A\n");
				bw.write("@THIS\n");
				bw.write("D=D+M\n");
				bw.write("@R14\n");
				bw.write("M=D\n"); // get desired local address and put into
									// tempt location

				bw.write("@R13\n");
				bw.write("D=M\n");
				bw.write("@R14\n");
				bw.write("A=M\n");
				bw.write("M=D\n");
			} else if (segment.equals("that")) {
				bw.write("@SP\n");
				bw.write("M=M-1\n");
				bw.write("A=M\n");
				bw.write("D=M\n"); // pop from stack
				bw.write("@R13\n"); // put into tempt location
				bw.write("M=D\n");

				bw.write("@" + index + "\n");
				bw.write("D=A\n");
				bw.write("@THAT\n");
				bw.write("D=D+M\n");
				bw.write("@R14\n");
				bw.write("M=D\n"); // get desired local address and put into
									// tempt location

				bw.write("@R13\n");
				bw.write("D=M\n");
				bw.write("@R14\n");
				bw.write("A=M\n");
				bw.write("M=D\n");
			} else if (segment.equals("pointer")) {
				String newAddress = String.valueOf(3 + Integer.valueOf(index)); // pointer
																				// is
																				// mapped
																				// on
																				// RAM
																				// location
																				// 3~4,
																				// p142
				bw.write("@SP\n");
				bw.write("M=M-1\n");
				bw.write("A=M\n");
				bw.write("D=M\n"); // pop from stack

				bw.write("@" + newAddress + "\n");
				bw.write("M=D\n");
			} else if (segment.equals("temp")) {
				String newAddress = String.valueOf(5 + Integer.valueOf(index)); // pointer
																				// is
																				// mapped
																				// on
																				// RAM
																				// location
																				// 5~12,
																				// p142
				bw.write("@SP\n");
				bw.write("M=M-1\n");
				bw.write("A=M\n");
				bw.write("D=M\n"); // pop from stack

				bw.write("@" + newAddress + "\n");
				bw.write("M=D\n");
			} else if (segment.equals("static")) {
				String newAddress = className + "." + index; // The trick: p.143
				bw.write("@SP\n");
				bw.write("M=M-1\n");
				bw.write("A=M\n");
				bw.write("D=M\n"); // pop from stack

				bw.write("@" + newAddress + "\n");
				bw.write("M=D\n");
			}
		}
	}

	void writeLabel(String label) throws IOException {
		bw.write("(" + Shared.FUNCTION_SCOPE + label + ")\n");
	}

	void writeGoto(String label) throws IOException {
		bw.write("@" + Shared.FUNCTION_SCOPE + label + "\n");
		bw.write("0;JMP\n");
	}

	void writeIf(String label) throws IOException { // Must pop
		bw.write("@SP\n"); // Get the topmost data of stack
		bw.write("M=M-1\n");
		bw.write("A=M\n");
		bw.write("D=M\n");
		bw.write("@" + Shared.FUNCTION_SCOPE + label + "\n");
		bw.write("D;JNE\n");
	}

	void writePushSelf(String registerName) throws IOException {
		bw.write("@" + registerName + "\n");
		bw.write("D=M\n");
		bw.write("@SP\n");
		bw.write("M=M+1\n");
		bw.write("A=M\n");
		bw.write("A=A-1\n");
		bw.write("M=D\n");
	}

	void writeCall(String functionName, String numArgs) throws IOException {
		// ---call f n---
		// push return-address
		// push LCL
		// push ARG
		// push THIS
		// push THAT
		// ARG = SP-n-5
		// LCL = SP
		// goto f
		// (return-address)
		String uniqueString = String.valueOf(System.nanoTime());
		// push return-address
		bw.write("@" + uniqueString + "\n");
		bw.write("D=A\n");
		bw.write("@SP\n");
		bw.write("M=M+1\n");
		bw.write("A=M\n");
		bw.write("A=A-1\n");
		bw.write("M=D\n");
		// push LCL
		writePushSelf("LCL");
		// push ARG
		writePushSelf("ARG");
		// push THIS
		writePushSelf("THIS");
		// push THAT
		writePushSelf("THAT");
		// ARG = SP - n - 5: reposition ARG
		bw.write("@SP\n");
		bw.write("D=M\n");
		bw.write("@ARG\n");
		bw.write("M=D\n");
		bw.write("@5\n");
		bw.write("D=A\n");
		bw.write("@ARG\n");
		bw.write("M=M-D\n");
		bw.write("@" + numArgs + "\n");
		bw.write("D=A\n");
		bw.write("@ARG\n");
		bw.write("M=M-D\n");
		// LCL = SP: reposition LCL
		bw.write("@SP\n");
		bw.write("D=M\n");
		bw.write("@LCL\n");
		bw.write("M=D\n");
		// goto f
		bw.write("@" + functionName + "\n");
		bw.write("0;JMP\n");

		// (return-address)
		bw.write("(" + uniqueString + ")\n");

	}

	void writeReturn() throws IOException {
		// FRAME = LCL
		// RET = *(FRAME - 5)
		// *ARG = pop()
		// SP = ARG + 1
		// THAT = *(FRAME - 1)
		// THIS = *(FRAME - 2)
		// ARG = *(FRAME - 3)
		// LCL = *(FRAME - 4)
		// goto RET

		// FRAME = LCL: FRAME (R11) is a temporary variable
		bw.write("@LCL\n");
		bw.write("D=M\n");
		bw.write("@R11\n");
		bw.write("M=D\n");
		// RET = *(FRAME - 5): put the return-address in a temp var (R12)
		bw.write("@5\n");
		bw.write("D=A\n");
		bw.write("@R11\n");
		bw.write("A=M\n");
		bw.write("A=A-D\n");
		bw.write("D=M\n");
		bw.write("@R12\n");
		bw.write("M=D\n");
		// *ARG = pop()
		writePushPop("pop", "argument", "0");
		// SP = ARG + 1
		bw.write("@ARG\n");
		bw.write("D=M+1\n");
		bw.write("@SP\n");
		bw.write("M=D\n");
		// THAT = *(FRAME - 1)
		bw.write("@1\n");
		bw.write("D=A\n");
		bw.write("@R11\n");
		bw.write("A=M\n");
		bw.write("A=A-D\n");
		bw.write("D=M\n");
		bw.write("@THAT\n");
		bw.write("M=D\n");
		// THIS = *(FRAME - 2)
		bw.write("@2\n");
		bw.write("D=A\n");
		bw.write("@R11\n");
		bw.write("A=M\n");
		bw.write("A=A-D\n");
		bw.write("D=M\n");
		bw.write("@THIS\n");
		bw.write("M=D\n");
		// ARG = *(FRAME - 3)
		bw.write("@3\n");
		bw.write("D=A\n");
		bw.write("@R11\n");
		bw.write("A=M\n");
		bw.write("A=A-D\n");
		bw.write("D=M\n");
		bw.write("@ARG\n");
		bw.write("M=D\n");
		// LCL = *(FRAME - 4)
		bw.write("@4\n");
		bw.write("D=A\n");
		bw.write("@R11\n");
		bw.write("A=M\n");
		bw.write("A=A-D\n");
		bw.write("D=M\n");
		bw.write("@LCL\n");
		bw.write("M=D\n");
		// goto RET
		bw.write("@R12\n");
		bw.write("A=M\n");
		bw.write("0;JMP\n");

	}

	void writeFunction(String functionName, String numLocals) throws IOException {
		// (f)
		// repeat k times:
		// push 0
		bw.write("(" + functionName + ")\n");
		int n = Integer.valueOf(numLocals);
		for (int i = 0; i < n; i++) {
			writePushPop("push", "constant", "0");
		}
	}

	void closeFile() throws IOException {
		bw.close();
		osw.close();
		fos.close();
	}
}

class Shared {
	public static int C_NONSENSE = -1;
	public static int C_ARITHMETIC = 0;
	public static int C_PUSH = 1;
	public static int C_POP = 2;
	public static int C_LABEL = 3;
	public static int C_GOTO = 4;
	public static int C_IF = 5;
	public static int C_FUNCTION = 6;
	public static int C_RETURN = 7;
	public static int C_CALL = 8;
	public static String FUNCTION_SCOPE_DEFAULT = "Sys.init."; // Default
	public static String FUNCTION_SCOPE = "Sys.init."; // Change every time
														// meets new function
														// command (take its
														// function name), reset
														// to default every time
														// meets return command
	public static String SOURCE_PATH = ".";
	public static String OUTPUT_FILE_NAME = "out.asm";
}
