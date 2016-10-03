package nand2tetris.compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import nand2tetris.compiler.SharedInfo.COMMAND_TYPE;
import nand2tetris.compiler.SharedInfo.FUNCTION_TYPE;
import nand2tetris.compiler.SharedInfo.IDENTIFIER_TYPE;
import nand2tetris.compiler.SharedInfo.KEYWORD;
import nand2tetris.compiler.SharedInfo.SEGMENT_TYPE;
import nand2tetris.compiler.SharedInfo.TOKEN_TYPE;


public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		SharedInfo.setup();
		SharedInfo.SOURCE_PATH = "/Users/baotrungtn/Desktop/nand2tetris/projects/11/Pong";
		File folder = new File(SharedInfo.SOURCE_PATH);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			String fileName = listOfFiles[i].getName();
			if (listOfFiles[i].isFile() && fileName.contains(".jack")) {
				String filePath = SharedInfo.SOURCE_PATH + "/" + fileName;
				System.out.println("Compiled " + filePath);
				JackAnalyzer jAnalyzer = new JackAnalyzer(filePath);
				jAnalyzer.compile();
			}
		}

	}

}

// Top-level driver that sets up and invokes the other modules
class JackAnalyzer {
	String filePath;
	String fileContent;
	
	File fi;
	InputStream fis;
	InputStreamReader isr;
	BufferedReader br;
	
	File fo;
	OutputStream fos;
	OutputStreamWriter osw;
	BufferedWriter bw;
	
	File fo1;
	OutputStream fos1;
	OutputStreamWriter osw1;
	BufferedWriter bw1;

	File fo2;
	OutputStream fos2;
	OutputStreamWriter osw2;
	BufferedWriter bw2;
	
	public JackAnalyzer(String filePath) {
		this.filePath = filePath;
	}
	
	public void compile() throws IOException {
		Scanner sc1 = new Scanner(new BufferedReader(new FileReader(filePath)));
		fileContent = sc1.useDelimiter("\\Z").next();
		sc1.close();
		
		String outputFilePath = filePath.substring(0, filePath.indexOf(".jack")) + "T.xml";
		fo = new File(outputFilePath);
		fos = new FileOutputStream(fo); // open file for writing result
		osw = new OutputStreamWriter(fos);
		bw = new BufferedWriter(osw);
		
		outputFilePath = filePath.substring(0, filePath.indexOf(".jack")) + ".xml";
		fo1 = new File(outputFilePath);
		fos1 = new FileOutputStream(fo1); // open file for writing result
		osw1 = new OutputStreamWriter(fos1);
		bw1 = new BufferedWriter(osw1);
		
		outputFilePath = filePath.substring(0, filePath.indexOf(".jack")) + ".vm";
		fo2 = new File(outputFilePath);
		fos2 = new FileOutputStream(fo2); // open file for writing result
		osw2 = new OutputStreamWriter(fos2);
		bw2 = new BufferedWriter(osw2);
		
		JackTokenizer jTokenizer = new JackTokenizer(fileContent);
		//printXML(jTokenizer);
		writeSimpleXML(jTokenizer);  // FileT.xml
		
		jTokenizer = new JackTokenizer(fileContent);
		writeFullXML(jTokenizer, bw1);  // File.xml
		
		bw.close();
		osw.close();
		fos.close();
		
		bw1.close();
		osw1.close();
		fos1.close();
		
	}
	
	void printXML(JackTokenizer jTokenizer) throws IOException {
		while (jTokenizer.hasMoreToken()) {
			jTokenizer.advance();
			System.out.print("<" + jTokenizer.tokenType().toString() + "> ");
			System.out.print(jTokenizer.token);
			System.out.print(" <" + jTokenizer.tokenType().toString() + ">");
			System.out.println();
		}
	}
	
	void writeSimpleXML(JackTokenizer jTokenizer) throws IOException {
		bw.write("<tokens>\n");
		while (jTokenizer.hasMoreToken()) {
			jTokenizer.advance();
			switch (jTokenizer.tokenType()) {
			case KEYWORD:
				bw.write("<keyword> " + jTokenizer.token + " </keyword>\n");
				break;
			case IDENTIFIER:
				bw.write("<identifier> " + jTokenizer.token + " </identifier>\n");
				break;
			case INT_CONST:
				bw.write("<integerConstant> " + jTokenizer.token + " </integerConstant>\n");
				break;
			case STRING_CONST:
				bw.write("<stringConstant> " + jTokenizer.token + " </stringConstant>\n");
				break;
			case SYMBOL:
				if (jTokenizer.token.indexOf("<") != -1) {
					bw.write("<symbol> &lt; </symbol>\n");
				} else if (jTokenizer.token.indexOf(">") != -1) {
					bw.write("<symbol> &gt; </symbol>\n");
				} else if (jTokenizer.token.indexOf("&") != -1) {
					bw.write("<symbol> &amp; </symbol>\n");
				} else 
					bw.write("<symbol> " + jTokenizer.token + " </symbol>\n");
				break;
			default:
				break;
			}
		}
		bw.write("</tokens>");
	}
	
	void writeFullXML(JackTokenizer jTokenizer, BufferedWriter bw) throws IOException {
		CompilationEngine ce = new CompilationEngine(jTokenizer, bw, bw2);
		ce.compileClass();
	}
	
	
}

// Tokenizer
class JackTokenizer {
	String fileContent;
	String token;
	Scanner myScanner;
	private String stringVal;
	private	char symbol;
	private String identifier;
	private int intVal;
	private KEYWORD keyWord;
	
	private TOKEN_TYPE tokenType;
	private String tokenTypeString;
	
	
	public JackTokenizer(String fileContent) {
		// Set default tokenType to NONE
		this.tokenType = SharedInfo.TOKEN_TYPE.NONE;
		// Remove all comments
		this.fileContent = fileContent.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","");
		// Add space to symbols for easily processing into tokens
		for (String s : SharedInfo.SYMBOL_LIST) {
			this.fileContent = this.fileContent.replaceAll(s, " " + s + " ");	
		}
		
		this.myScanner = new Scanner(this.fileContent);
	}
	
	boolean hasMoreToken() throws IOException {
		return myScanner.hasNext();
	}
	
	boolean hasNextToken(String matchStr) throws IOException {
		return myScanner.hasNext(matchStr);
	}
	
	String advance() {
		token = myScanner.next();
		
		if (token.equals("\"")) {
			tokenType = SharedInfo.TOKEN_TYPE.STRING_CONST;
			tokenTypeString = "stringConstant";
			stringVal = "";
			while (myScanner.hasNext()) {  // Get the String Constant
				String tmp = myScanner.next();
				if (!tmp.equals("\"")) {
					stringVal += tmp + " ";
				} else {
					break;
				}
			}
			token = stringVal;
		} else {
			if (SharedInfo.KEYWORD_LIST.contains(token)) {  // Keyword
				tokenType = SharedInfo.TOKEN_TYPE.KEYWORD;
				tokenTypeString = "keyword";
				keyWord = KEYWORD.valueOf(token.toUpperCase());
			} else if (SharedInfo.SYMBOL_LIST.contains("\\" + token) || SharedInfo.SYMBOL_LIST.contains(token)) {  // Symbol
				tokenType = SharedInfo.TOKEN_TYPE.SYMBOL;
				tokenTypeString = "symbol";
				symbol = token.charAt(0);
			} else {
				if (token.charAt(0) >= '0' && token.charAt(0) <= '9') {  // Number
					tokenType = SharedInfo.TOKEN_TYPE.INT_CONST;
					tokenTypeString = "integerConstant";
					intVal = Integer.valueOf(token);
				} else {  // Identifier
					tokenType = SharedInfo.TOKEN_TYPE.IDENTIFIER;
					tokenTypeString = "identifier";
					identifier = token;
				}
			}
		}
		return token;
	}
	
	String tokenTypeString() {
		return tokenTypeString;
	}
	
	TOKEN_TYPE tokenType() {
		return tokenType;
	}
	
	KEYWORD keyWord() {
		return keyWord;
	}
	
	char symbol() {
		return symbol;
	}
	
	String identifier() {
		return identifier;
	}
	
	int intVal() {
		return intVal;
	}
	
	String stringVal() {
		return stringVal;
	}
}

class IdentifierInfo {
	public String type;
	public IDENTIFIER_TYPE kind;
	public int index;
	
	public IdentifierInfo(String type, IDENTIFIER_TYPE kind, int index) {
		this.type = type;
		this.kind = kind;
		this.index = index;
	}
}

class FunctionInfo {
	public FUNCTION_TYPE functionType;
	public String returnType;
	
	public FunctionInfo(FUNCTION_TYPE functionType, String returnType) {
		this.functionType = functionType;
		this.returnType = returnType;
	}
}

class SymbolTable {
	
	
	Hashtable<String, IdentifierInfo> symbolTableClassScope;
	Hashtable<String, IdentifierInfo> symbolTableSubroutineScope;
	Hashtable<String, FunctionInfo> symbolTableFunction;
	
	public SymbolTable() {
		symbolTableClassScope = new Hashtable<String, IdentifierInfo>();
		symbolTableSubroutineScope = new Hashtable<String, IdentifierInfo>();
		symbolTableFunction = new Hashtable<String, FunctionInfo>();
	}
	
	
	// Start a new subroutine scope (i.e., resets the subroutine's symbol table)
	void startSubroutine() {
		symbolTableSubroutineScope = new Hashtable<String, IdentifierInfo>();
	}
	
	void addFunctionInfo(String functionName, FUNCTION_TYPE functionType, String returnType) {
		FunctionInfo newFunctionInfo= new FunctionInfo(functionType, returnType);
		symbolTableFunction.put(functionName, newFunctionInfo);
	}
	
	// 
	void define(String name, String type, IDENTIFIER_TYPE kind) {
		if (kind == IDENTIFIER_TYPE.STATIC || kind == IDENTIFIER_TYPE.FIELD) {
			if (symbolTableClassScope.get(name) == null) {
				IdentifierInfo newIdentifier = new IdentifierInfo(type, kind, varCount(kind));
				symbolTableClassScope.put(name, newIdentifier);
			}
		} else {
			if (symbolTableSubroutineScope.get(name) == null) {
				IdentifierInfo newIdentifier = new IdentifierInfo(type, kind, varCount(kind));
				symbolTableSubroutineScope.put(name, newIdentifier);
			}
		}
	}
	
	int varCount(IDENTIFIER_TYPE kind) {
		int result = 0;
		
		if (kind == IDENTIFIER_TYPE.STATIC || kind == IDENTIFIER_TYPE.FIELD) {
			for (String key: symbolTableClassScope.keySet()) {
				IdentifierInfo tmp = symbolTableClassScope.get(key);
				if (tmp != null && tmp.kind == kind) {
					result++;
				}
			}
		} else {
			for (String key: symbolTableSubroutineScope.keySet()) {
				IdentifierInfo tmp = symbolTableSubroutineScope.get(key);
				if (tmp != null && tmp.kind == kind) {
					result++;
				}
			}
		}
		
		return result;
	}
	
	IDENTIFIER_TYPE kindOf(String name) {
		IdentifierInfo info = null;
		if ((info = symbolTableClassScope.get(name)) != null) {
			return info.kind;
		} else if ((info = symbolTableSubroutineScope.get(name)) != null) {
			return info.kind;
		} else {
			return IDENTIFIER_TYPE.NONE;
		}
	}
	
	String typeOf(String name) {
		IdentifierInfo info = null;
		if ((info = symbolTableClassScope.get(name)) != null) {
			return info.type;
		} else if ((info = symbolTableSubroutineScope.get(name)) != null) {
			return info.type;
		} else {
			return "NONE";
		}
	}
	
	int indexOf(String name) {
		IdentifierInfo info = null;
		if ((info = symbolTableClassScope.get(name)) != null) {
			return info.index;
		} else if ((info = symbolTableSubroutineScope.get(name)) != null) {
			return info.index;
		} else {
			return -1;
		}
	}
	
	SEGMENT_TYPE getSegment(String name) {
		IDENTIFIER_TYPE idType = kindOf(name);
		
		if (idType == IDENTIFIER_TYPE.ARG) {
			return SEGMENT_TYPE.ARG;
		} else if (idType == IDENTIFIER_TYPE.FIELD) {
			return SEGMENT_TYPE.THIS; // fake
		} else if (idType == IDENTIFIER_TYPE.STATIC) {
			return SEGMENT_TYPE.STATIC;
		} else if (idType == IDENTIFIER_TYPE.VAR) {
			return SEGMENT_TYPE.LOCAL;
		}
		
		
		// Fake
		return SEGMENT_TYPE.LOCAL;
	}
}

class VMWriter {
	BufferedWriter bw;
	
	public VMWriter(BufferedWriter bw) {
		this.bw = bw;
	}
	
	void writePush(SEGMENT_TYPE segment, int index) throws IOException {
		String content = segment.toString().toLowerCase();
		if (content.equals("arg")) {
			content = "argument";
		} else if (content.equals("const")) {
			content = "constant";
		}
		
		content = "push " + content + " " + index;
		
		bw.write(content + "\n");
	}
	
	void writePop(SEGMENT_TYPE segment, int index) throws IOException {
		String content = segment.toString().toLowerCase();
		if (content.equals("arg")) {
			content = "argument";
		} else if (content.equals("const")) {
			content = "constant";
		}
		
		content = "pop " + content + " " + index;
		
		bw.write(content + "\n");
	}
	
	void writeArithmetic(COMMAND_TYPE cmd) throws IOException {
		String content = cmd.toString().toLowerCase();
		
		bw.write(content + "\n");
	}
	
	void writeLabel(String label) throws IOException {
		String content = "label " + label;
		
		bw.write(content + "\n");
	}
	
	void writeGoTo(String label) throws IOException {
		String content = "goto " + label;
		
		bw.write(content + "\n");
	}
	
	void writeIf(String label) throws IOException {
		String content = "if-goto " + label;
		
		bw.write(content + "\n");
	}
	
	void writeCall(String name, int nArgs) throws IOException {
		String content = "call " + name + " " + nArgs;
		
		bw.write(content + "\n");
	}
	
	void writeFunction(String name, int nLocals) throws IOException {
		String content = "function " + name + " " + nLocals;
		
		bw.write(content + "\n");
	}
	
	void writeReturn() throws IOException {
		String content = "return";
		
		bw.write(content + "\n");
	}
	
	void close() throws IOException {
		bw.close();
	}
}


// Recursive Top-down parser
class CompilationEngine {
	JackTokenizer jTokenizer;
	BufferedWriter bw;
	BufferedWriter bw2;
	SymbolTable symbolTable;
	VMWriter vmWriter;
	private String className;
	private String functionName;
	
	public CompilationEngine(JackTokenizer jTokenizer, BufferedWriter bw, BufferedWriter bw2) {
		this.jTokenizer = jTokenizer;
		this.bw = bw;
		Utility.setBufferedWriter(bw);
		this.symbolTable = new SymbolTable();
		this.vmWriter = new VMWriter(bw2);
	}
	
	void compileClass() throws IOException {
		Utility.printNonTerminalOpen("class");
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'class'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // className
		className = jTokenizer.token;
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '{'
		while (true) {
			jTokenizer.advance();
			if (jTokenizer.token.indexOf('}') != -1) {  
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '}'
				break;
			} else if (jTokenizer.token.equals("static") || jTokenizer.token.equals("field")) {
				compileClassVarDec();
			} else if (jTokenizer.token.equals("constructor") || jTokenizer.token.equals("function") 
					|| jTokenizer.token.equals("method")) {
				compileSubroutineDec();
			} else {
				Utility.error("Syntax error");
				break;
			}
		}
		
		Utility.printNonTerminalClose("class");
		vmWriter.close();
	}
	
	void compileClassVarDec() throws IOException {
		String name = null;
		String type = null;
		IDENTIFIER_TYPE kind = null;
		
		Utility.printNonTerminalOpen("classVarDec");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'static' or 'field'
		if (jTokenizer.token.equals("static")) {
			kind = IDENTIFIER_TYPE.STATIC;
		} else if (jTokenizer.token.equals("field")) {
			kind = IDENTIFIER_TYPE.FIELD;
		}
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // type
		type = jTokenizer.token;
		while (true) {
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
			name = jTokenizer.token;
			symbolTable.define(name, type, kind);  // add new entry into symbol table
//			vmWriter.writePush(SEGMENT_TYPE.CONST, 0);
//			vmWriter.writePush(SEGMENT_TYPE.STATIC, symbolTable.indexOf(name));  // -1 if not exist
			jTokenizer.advance();  
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ',' or ';'
			if (jTokenizer.token.indexOf(';') != -1) {  // , or ; Break if ;
				break;
			}
		}
		
		Utility.printNonTerminalClose("classVarDec");
	
	}
	
	void compileSubroutineDec() throws IOException {
		String funcName = "";
		String returnType = "";
		FUNCTION_TYPE functionType = FUNCTION_TYPE.FUNCTION;
		
		Utility.printNonTerminalOpen("subroutineDec");
		
		symbolTable.startSubroutine();  // Reset symbol table for subroutine scope
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'constructor' or 'function' or 'method'
		if (jTokenizer.token.equals("function")) {
			functionType = FUNCTION_TYPE.FUNCTION;
		} else if (jTokenizer.token.equals("constructor")) {
			functionType = FUNCTION_TYPE.CONSTRUCTOR;
		} else if (jTokenizer.token.equals("method")) {
			functionType = FUNCTION_TYPE.METHOD;
		}
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'void' | type
		returnType = jTokenizer.token;
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // subroutineName
		funcName = jTokenizer.token;
		functionName = funcName;

		symbolTable.addFunctionInfo(funcName, functionType, returnType);  // add new function info
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
		compileParameterList();  // calling a function f after n arguments have been pushed onto the stack
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')', advance() is done in compileParameterList()

		compileSubroutineBody();
		
		Utility.printNonTerminalClose("subroutineDec");
	}
	
	void compileSubroutineBody() throws IOException {
		
		Utility.printNonTerminalOpen("subroutineBody");
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '{'
		
		while (true) {
			jTokenizer.advance();
			if (jTokenizer.token.indexOf('}') != -1) {
				break;
			} else if (jTokenizer.token.equals("var")) {  // varDec -> 'var' type varName (',' varName)*';'
				compileVarDec();
			} else {  // statements
				vmWriter.writeFunction(className + "." + functionName, symbolTable.varCount(IDENTIFIER_TYPE.VAR));
				// When compiling a Jack method into a VM function, the compiler must insert VM code that set the base of the THIS SEGMENT properly
				// A Jack method with k arguments is compiled into a VM function that operates on k + 1 arguments. 
				// The first argument (argument number 0) always refers to the THIS object
				if (symbolTable.symbolTableFunction.get(functionName).functionType == FUNCTION_TYPE.METHOD) {
					vmWriter.writePush(SEGMENT_TYPE.ARG, 0);
					vmWriter.writePop(SEGMENT_TYPE.POINTER, 0);
				} 
				// When compiling a Jack constructor, the compiler must insert VM code that allocates a memory block for the new object 
				// and then sets the base of the THIS SEGMENT to point at its base
				else if (symbolTable.symbolTableFunction.get(functionName).functionType == FUNCTION_TYPE.CONSTRUCTOR) {
					vmWriter.writePush(SEGMENT_TYPE.CONST, symbolTable.varCount(IDENTIFIER_TYPE.FIELD));
					vmWriter.writeCall("Memory.alloc", 1);
					vmWriter.writePop(SEGMENT_TYPE.POINTER, 0);
//					vmWriter.writePush(SEGMENT_TYPE.POINTER, 0);  // Push this reference into ARG 0 -> simulate method call
//					vmWriter.writePop(SEGMENT_TYPE.ARG, 0);
				} else if (symbolTable.symbolTableFunction.get(functionName).functionType == FUNCTION_TYPE.FUNCTION) {
					// do nothing
				}
				
				compileStatements();
				if (jTokenizer.token.indexOf('}') != -1) {  // Last '}' of SubroutineBody
					break;
				}
			}
		}
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '}'
		Utility.printNonTerminalClose("subroutineBody");
	}
	
	void compileParameterList() throws IOException {
		String name = null;
		String type = null;
		IDENTIFIER_TYPE kind = IDENTIFIER_TYPE.ARG;
		
		Utility.printNonTerminalOpen("parameterList");
		
		while (true) {
			jTokenizer.advance();
			if (jTokenizer.token.indexOf(')') != -1) {
				break;
			} else if (jTokenizer.token.indexOf(',') != -1) {  // (',' type varName)
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ','
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // type
				type = jTokenizer.token;
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
				name = jTokenizer.token;
				symbolTable.define(name, type, kind); 
			} else {  // type varName, only use in the beginning
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // type
				type = jTokenizer.token;
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
				name = jTokenizer.token;
				symbolTable.define(name, type, kind);
//				System.out.println(name + "   " + type + "   " + kind); 
			}
			
		}
		
		Utility.printNonTerminalClose("parameterList");
	}
	
	void compileVarDec() throws IOException {
		String name = null;
		String type = null;
		IDENTIFIER_TYPE kind = null;
		
		Utility.printNonTerminalOpen("varDec");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'var'
		kind = IDENTIFIER_TYPE.VAR;
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // type
		type = jTokenizer.token;
		
		while (true) {
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
			name = jTokenizer.token;
			symbolTable.define(name, type, kind);  // add new entry into symbol table
			
			jTokenizer.advance();  
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ',' or ';'
			if (jTokenizer.token.indexOf(';') != -1) {  // , or ; Break if ;
				break;
			}
		}
		
		Utility.printNonTerminalClose("varDec");
	}
	
	void compileStatements() throws IOException {
		Utility.printNonTerminalOpen("statements");
		
		while (true) {
			if (jTokenizer.token.equals("let")) {
				compileLet();
			} else if (jTokenizer.token.equals("if")) {
				compileIf();
			} else if (jTokenizer.token.equals("while")) {
				compileWhile();
			} else if (jTokenizer.token.equals("do")) {
				compileDo();
			} else if (jTokenizer.token.equals("return")) {
				compileReturn();
			} else {
				break;  // '}'
			} 
			jTokenizer.advance();
		}
		 
		Utility.printNonTerminalClose("statements");
	}
	
	void compileDo() throws IOException {
		int nArgs = 0;
		String name1 = "";  // method/instance/class name
		String name2 = "";
		String fName = "";
		
		Utility.printNonTerminalOpen("doStatement");
		
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'do'
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // identifier
		name1 = jTokenizer.token;
		
//		FunctionInfo funcInfo = symbolTable.symbolTableFunction.get(name1);
//		if (funcInfo != null) {
//			if (funcInfo.functionType == FUNCTION_TYPE.METHOD) {
//				vmWriter.writePush(SEGMENT_TYPE.ARG, 0);  // push reference to the object
//			}
//		}
		
		jTokenizer.advance();
		if (jTokenizer.token.indexOf('(') != -1) {  // subroutineName '(' expressionList ')'  
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
			
			vmWriter.writePush(SEGMENT_TYPE.POINTER, 0);
			nArgs = 1 + compileExpressionList();  // first argument is THIS
			vmWriter.writeCall(className + "." + name1, nArgs);
			
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
		} else if (jTokenizer.token.indexOf('.') != -1) {  // (className | varName) '.' subroutineName '(' expressionList ')'
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '.'
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // subroutineName
			name2 = jTokenizer.token;
			if (symbolTable.kindOf(name1) == IDENTIFIER_TYPE.NONE) {  // Class Name
				fName = name1 + "." + name2;
				nArgs = 0;
			} else {
				fName = symbolTable.typeOf(name1) + "." + name2;  // Example: Ball.show(); not b.show()
				nArgs = 1;  // THIS
				vmWriter.writePush(SharedInfo.segmentTranslateTable.get(symbolTable.kindOf(name1)), 
						symbolTable.indexOf(name1));  // push THIS onto stack
			}
			
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
			
			nArgs = nArgs + compileExpressionList();
			vmWriter.writeCall(fName, nArgs);
						
			
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
		} 
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ';'
		
		// When translating a do sub statement where sub is a void method or function, the caller of the corresponding VM function
		// must pop (and ignore) the returned value (which is always the constant 0)
		vmWriter.writePop(SEGMENT_TYPE.TEMP, 1);
		
		Utility.printNonTerminalClose("doStatement");
	}
	
	void compileLet() throws IOException {
		int idx = -1;
		IDENTIFIER_TYPE idType = IDENTIFIER_TYPE.NONE;
		SEGMENT_TYPE segmentType = null;  // fake
		String name1;
		boolean isArray = false;
		
		Utility.printNonTerminalOpen("letStatement");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'let'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
		name1 = jTokenizer.token;
		idx = symbolTable.indexOf(name1);
		segmentType = symbolTable.getSegment(name1);
		
		if (segmentType == SEGMENT_TYPE.THIS) {

		} else {

		}
		
		jTokenizer.advance();
		if (jTokenizer.token.indexOf('[') != -1) {  // varName ('[' expression ']')? '=' expression ';'
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '['
			
			isArray = true;
			
			compileExpression();
			
			if (segmentType == SEGMENT_TYPE.THIS) {
				vmWriter.writePush(SEGMENT_TYPE.THIS, symbolTable.indexOf(name1));
				vmWriter.writeArithmetic(COMMAND_TYPE.ADD);
			} else {
				vmWriter.writePush(segmentType, idx);  // value of pointer (to an array)
				vmWriter.writeArithmetic(COMMAND_TYPE.ADD);
			}			
			
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ']'
			
			jTokenizer.advance();  // get '='
		} 
		
		if (jTokenizer.token.indexOf('=') != -1) {
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '='
			
			compileExpression();
			
			if (segmentType == SEGMENT_TYPE.THIS) {
				if (isArray) {
					vmWriter.writePop(SEGMENT_TYPE.TEMP, 2);
					vmWriter.writePop(SEGMENT_TYPE.POINTER, 1);
					vmWriter.writePush(SEGMENT_TYPE.TEMP, 2);
					vmWriter.writePop(SEGMENT_TYPE.THAT, 0);
				} else {
					vmWriter.writePop(SEGMENT_TYPE.THIS, symbolTable.indexOf(name1));
				}				
			} else {
				if (isArray) {
					vmWriter.writePop(SEGMENT_TYPE.TEMP, 2);
					vmWriter.writePop(SEGMENT_TYPE.POINTER, 1);
					vmWriter.writePush(SEGMENT_TYPE.TEMP, 2);
					vmWriter.writePop(SEGMENT_TYPE.THAT, 0);
				} else {
					vmWriter.writePop(segmentType, idx); 
				}
				
			}
			
			
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ';'
		}
		
		Utility.printNonTerminalClose("letStatement");
	}
	
	void compileWhile() throws IOException {
		String WHILE_START = "WHILE_" + String.valueOf(System.nanoTime());
		String WHILE_END = WHILE_START + "_END";
		
		Utility.printNonTerminalOpen("whileStatement");
		
				
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'while'
		vmWriter.writeLabel(WHILE_START);
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
		compileExpression();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
		
		// if FALSE -> finish LOOP 
		vmWriter.writePush(SEGMENT_TYPE.CONST, 0);  // True: -1 : 111111 | False: 0 : 0000000
		vmWriter.writeArithmetic(COMMAND_TYPE.EQ);
		vmWriter.writeIf(WHILE_END);  // Jump to Label if True (-1)
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '{'
		jTokenizer.advance();  // ?
		compileStatements();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '}'
		
		
		// Continue LOOP
		vmWriter.writeGoTo(WHILE_START);
		
		Utility.printNonTerminalClose("whileStatement");
		vmWriter.writeLabel(WHILE_END);
	}
	
	
	void compileIf() throws IOException {
		String L1 = "IF_" + String.valueOf(System.nanoTime());
		String L2 = "IF_" + String.valueOf(System.nanoTime() + 10);
		
		Utility.printNonTerminalOpen("ifStatement");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'if'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
		
		compileExpression();
		vmWriter.writePush(SEGMENT_TYPE.CONST, 0);
		vmWriter.writeArithmetic(COMMAND_TYPE.EQ);
		vmWriter.writeIf(L1);
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '{'
		jTokenizer.advance();  // ?
		
		compileStatements();
		vmWriter.writeGoTo(L2);
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '}'
		
		
		vmWriter.writeLabel(L1);
		
		if (jTokenizer.hasNextToken("else")) {
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'else'
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '{'
			jTokenizer.advance();  // ?
			compileStatements();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '}'
		}
		
		vmWriter.writeLabel(L2); 
		
		Utility.printNonTerminalClose("ifStatement");
	}
	
	void compileReturn() throws IOException {
		Utility.printNonTerminalOpen("returnStatement");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'return'
		
		if (jTokenizer.hasNextToken("\\;")) {
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ';'
			
			// VM function corresponding to void Jack methods and functions must return the constant 0 as their return value 
			vmWriter.writePush(SEGMENT_TYPE.CONST, 0);
			vmWriter.writeReturn();
		} else {
			compileExpression();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ';'
			
			vmWriter.writeReturn();
		}
		
		Utility.printNonTerminalClose("returnStatement");
	}
	
	
	void compileExpression() throws IOException {
		String savedToken = "";
		
		Utility.printNonTerminalOpen("expression");
		
		compileTerm();
		
		while (true) {  // term (op term)*
			//jTokenizer.advance();
			if (jTokenizer.token.indexOf(')') != -1  // '(' expression ')'
					|| jTokenizer.token.indexOf(']') != -1  // '[' expression ']'
					|| jTokenizer.token.indexOf(',') != -1  // expressionList -> (expression (',' expression)*)?
					|| jTokenizer.token.indexOf(';') != -1) {  // expression ';'
				break;
			} else if (jTokenizer.token.indexOf('+') != -1  
					|| jTokenizer.token.indexOf('-') != -1  
					|| jTokenizer.token.indexOf('*') != -1
					|| jTokenizer.token.indexOf('/') != -1
					|| jTokenizer.token.indexOf('&') != -1
					|| jTokenizer.token.indexOf('|') != -1
					|| jTokenizer.token.indexOf('<') != -1
					|| jTokenizer.token.indexOf('>') != -1
					|| jTokenizer.token.indexOf('=') != -1) {  
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // term (op term)* 
				savedToken = jTokenizer.token;
				compileTerm();
				
				if (!savedToken.equals("*") && !savedToken.equals("/")) {
					vmWriter.writeArithmetic(COMMAND_TYPE.valueOf(SharedInfo.cmdTable.get(savedToken).toUpperCase()));
				} else if (savedToken.equals("*")) {
					vmWriter.writeCall("Math.multiply", 2);
				} else if (savedToken.equals("/")) {
					vmWriter.writeCall("Math.divide", 2);
				}
			}
		}
		
		
		Utility.printNonTerminalClose("expression");
	}
	
	void compileTerm() throws IOException {
		String name1 = "";  // object name or class name
		String name2 = "";  // method name
		Utility.printNonTerminalOpen("term");
		
		jTokenizer.advance();
		if (jTokenizer.token.indexOf('(') != -1) {  // '(' expression ')'
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
			compileExpression();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
			jTokenizer.advance();
		} else if (jTokenizer.token.length() == 1 && (jTokenizer.token.indexOf('-') == 0 || jTokenizer.token.indexOf('~') == 0)) {  // Unary 
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '-' or '~'
			String savedToken = jTokenizer.token;
			compileTerm();
			if (savedToken.equals("-")) {
				vmWriter.writeArithmetic(COMMAND_TYPE.NEG);
			} else if (savedToken.equals("~")) {
				vmWriter.writeArithmetic(COMMAND_TYPE.NOT);
			}
			
		} else {  
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // identifier
			name1 = jTokenizer.token;
			
			if (jTokenizer.tokenTypeString().equals("integerConstant")) {
				vmWriter.writePush(SEGMENT_TYPE.CONST, Integer.valueOf(jTokenizer.token));
			} else if (jTokenizer.tokenTypeString().equals("stringConstant")) {
				// String constants are created using the OS constructor String.new(length).
				// String assignments like x="cc..c" are handled using a series of calls to the 
				// OS routine String.appendChar(nextChar).
				
				vmWriter.writePush(SEGMENT_TYPE.CONST, jTokenizer.token.length());
				vmWriter.writeCall("String.new", 1);
				for (int i = 0; i < jTokenizer.token.length(); i++) {
					vmWriter.writePush(SEGMENT_TYPE.CONST, jTokenizer.token.charAt(i));
					vmWriter.writeCall("String.appendChar", 2);
				}
			} else if (jTokenizer.tokenTypeString().equals("keyword")) {
				if (jTokenizer.keyWord() == KEYWORD.TRUE) {
					vmWriter.writePush(SEGMENT_TYPE.CONST, 1);  // ...00000000001 -> 1 (THIS IS NOT not)
					vmWriter.writeArithmetic(COMMAND_TYPE.NEG);  // 1111111111... -> -1
				} else if ((jTokenizer.keyWord() == KEYWORD.FALSE) || 
						(jTokenizer.keyWord() == KEYWORD.NULL)) {
					vmWriter.writePush(SEGMENT_TYPE.CONST, 0);  
				} else if (jTokenizer.keyWord() == KEYWORD.THIS) {
					vmWriter.writePush(SEGMENT_TYPE.POINTER, 0);
				}
			}
			
			
			jTokenizer.advance();
			if (jTokenizer.token.indexOf('[') != -1) {  // varName '[' expression ']'
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '['
				
				int idx = symbolTable.indexOf(name1);
				SEGMENT_TYPE segmentType = symbolTable.getSegment(name1);
				if (idx != -1) {
					if (segmentType == SEGMENT_TYPE.THIS) {
						vmWriter.writePush(SEGMENT_TYPE.ARG, 0);  // this obj
						vmWriter.writePop(SEGMENT_TYPE.POINTER, 0);  // set pointer at the base address of the object
					}
				}
				
				compileExpression();
				
				vmWriter.writePush(segmentType, idx);  //  get value (address) // This is base address
				vmWriter.writeArithmetic(COMMAND_TYPE.ADD);  // calculate the index
				vmWriter.writePop(SEGMENT_TYPE.POINTER, 1);  // pop to THAT
				vmWriter.writePush(SEGMENT_TYPE.THAT, 0);
				
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ']'
				jTokenizer.advance();
			}
			// subroutineCall -> subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
			else if (jTokenizer.token.indexOf('(') != -1) {  // subroutineName '(' expressionList ')'  
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
				vmWriter.writePush(SEGMENT_TYPE.POINTER, 0);  // push this as first arg
				int nArgs = 1 + compileExpressionList();
//				String className = symbolTable.typeOf(symbolTable.symbolTableFunction.get(name1).);
				
				vmWriter.writeCall(className + "." + name1, nArgs);
				
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
				jTokenizer.advance();
			} else if (jTokenizer.token.indexOf('.') != -1) {  // (className | varName) '.' subroutineName '(' expressionList ')'
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '.'
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // subroutineName
				name2 = jTokenizer.token;
				
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
				
				
				
				if (symbolTable.kindOf(name1) == IDENTIFIER_TYPE.NONE) {  // Class function
					int nArgs = compileExpressionList();
					vmWriter.writeCall(name1 + "." + name2, nArgs);
				} else {  // object method
					vmWriter.writePush(SEGMENT_TYPE.POINTER, 0);  // push this as first arg
					int nArgs = 1 + compileExpressionList();
					vmWriter.writeCall(symbolTable.typeOf(name1) + "." + name2, nArgs);
				}
				
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
				jTokenizer.advance();
			} else {  // just identifier (identifier)
				int idx = symbolTable.indexOf(name1);
				SEGMENT_TYPE segmentType = symbolTable.getSegment(name1);
				if (idx != -1)
					vmWriter.writePush(segmentType, idx);
			}
		}
		
		Utility.printNonTerminalClose("term");
	}
	
	int compileExpressionList() throws IOException {
		int nArgs = 0;
		Utility.printNonTerminalOpen("expressionList");
		
		if (jTokenizer.hasNextToken("\\)")) {  // ()
			jTokenizer.advance();
		} else {
			while (true) {
				compileExpression();
				nArgs++;
				if (jTokenizer.token.indexOf(')') != -1) {
					break;
				} else if (jTokenizer.token.indexOf(',') != -1) {
					Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // (expression (',' expression)*)?
				}
			}
		}
		
		Utility.printNonTerminalClose("expressionList");
		
		return nArgs;
	}
}



class SharedInfo {
	public static ArrayList<String> SYMBOL_LIST; 
	public static ArrayList<String> KEYWORD_LIST;
	
	public static enum FUNCTION_TYPE {
		FUNCTION, CONSTRUCTOR, METHOD
	}
	
	public static enum SEGMENT_TYPE {
		CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP
	}
	
	public static enum COMMAND_TYPE {
		ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT
	}
	
	public static enum IDENTIFIER_TYPE {
		STATIC, FIELD, ARG, VAR, NONE
	}
	
	public static enum TOKEN_TYPE {
		NONE, KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
	}
	
	public static enum KEYWORD {
		NONE, CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR,
		VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN,
		TRUE, FALSE, NULL, THIS
	}
	
	public static Hashtable<String, String> cmdTable;
	public static Hashtable<IDENTIFIER_TYPE, SEGMENT_TYPE> segmentTranslateTable;
	
	public static String SOURCE_PATH = "";
	
	public static SEGMENT_TYPE getSegment(IDENTIFIER_TYPE idType) {
		if (idType == IDENTIFIER_TYPE.ARG) {
			// fake
		} else if (idType == IDENTIFIER_TYPE.FIELD) {
			return SEGMENT_TYPE.THIS; // fake
		} else if (idType == IDENTIFIER_TYPE.STATIC) {
			return SEGMENT_TYPE.STATIC;
		} else if (idType == IDENTIFIER_TYPE.VAR) {
			return SEGMENT_TYPE.LOCAL;
		}
		
		
		// Fake
		return SEGMENT_TYPE.LOCAL;
	}
	
	public static void setup() throws FileNotFoundException {
		cmdTable = new Hashtable<String, String>();
		cmdTable.put("+", "add");
		cmdTable.put("-", "sub");
		cmdTable.put("*", "Math.multiply");
		cmdTable.put("/", "Math.divide");
		cmdTable.put("=", "eq");
		cmdTable.put(">", "gt");
		cmdTable.put("<", "lt");
		cmdTable.put("&", "and");
		cmdTable.put("|", "or");
		
		segmentTranslateTable = new Hashtable<IDENTIFIER_TYPE,SEGMENT_TYPE>();
		segmentTranslateTable.put(IDENTIFIER_TYPE.ARG, SEGMENT_TYPE.ARG);
		segmentTranslateTable.put(IDENTIFIER_TYPE.STATIC, SEGMENT_TYPE.STATIC);
		segmentTranslateTable.put(IDENTIFIER_TYPE.VAR, SEGMENT_TYPE.LOCAL);
		segmentTranslateTable.put(IDENTIFIER_TYPE.FIELD, SEGMENT_TYPE.THIS);
		
		
		SYMBOL_LIST = new ArrayList<String>();
		KEYWORD_LIST = new ArrayList<String>();
		Scanner sc1 = new Scanner(new BufferedReader(new FileReader("./symbols.txt")));
		String s = sc1.useDelimiter("\\Z").next();
		Scanner sc2 = new Scanner(s);
		while (sc2.hasNext()) {
			SYMBOL_LIST.add(sc2.next());
		}
		sc1.close();
		sc2.close();
		
		sc1 = new Scanner(new BufferedReader(new FileReader("./keywords.txt")));
		s = sc1.useDelimiter("\\Z").next();
		sc2 = new Scanner(s);
		while (sc2.hasNext()) {
			KEYWORD_LIST.add(sc2.next());
		}
		
		sc1.close();
		sc2.close();
	}
}

class Utility {
	private static BufferedWriter bw;
	
	public static void setBufferedWriter(BufferedWriter bw) {
		Utility.bw = bw;
	}
	
	public static void error(String msg) {
		System.out.println(msg);
	}
	
	public static void printTerminal(String tagName, String elementName) throws IOException {
		System.out.println("<" + tagName + "> " + elementName + " </" + tagName + ">");
		if (elementName.indexOf("<") != -1) {
			elementName = "&lt;";
		} else if (elementName.indexOf(">") != -1) {
			elementName = "&gt;";
		} else if (elementName.indexOf("&") != -1) {
			elementName = "&amp;";
		} 
		bw.write("<" + tagName + "> " + elementName + " </" + tagName + ">\n");
	}
	
	public static void printNonTerminalOpen(String tagName) throws IOException {
		System.out.println("<" + tagName + ">");
		bw.write("<" + tagName + ">\n");
	}
	
	public static void printNonTerminalClose(String tagName) throws IOException {
		System.out.println("</" + tagName + ">");
		bw.write("</" + tagName + ">\n");
	}
}