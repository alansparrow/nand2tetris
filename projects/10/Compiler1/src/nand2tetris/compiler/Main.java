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
import java.util.Scanner;

import nand2tetris.compiler.SharedInfo.KEYWORD;
import nand2tetris.compiler.SharedInfo.TOKEN_TYPE;


public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		SharedInfo.setup();
		SharedInfo.SOURCE_PATH = "/Users/baotrungtn/Desktop/nand2tetris/projects/10/Square";
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
		CompilationEngine ce = new CompilationEngine(jTokenizer, bw);
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


// Recursive Top-down parser
class CompilationEngine {
	JackTokenizer jTokenizer;
	BufferedWriter bw;
	
	public CompilationEngine(JackTokenizer jTokenizer, BufferedWriter bw) {
		this.jTokenizer = jTokenizer;
		this.bw = bw;
		Utility.setBufferedWriter(bw);
	}
	
	void compileClass() throws IOException {
		Utility.printNonTerminalOpen("class");
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'class'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // className
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

	}
	
	void compileClassVarDec() throws IOException {
		
		Utility.printNonTerminalOpen("classVarDec");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'static' or 'field'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // type
		while (true) {
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
			jTokenizer.advance();  
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ',' or ';'
			if (jTokenizer.token.indexOf(';') != -1) {  // , or ; Break if ;
				break;
			}
		}
		
		Utility.printNonTerminalClose("classVarDec");
	
	}
	
	void compileSubroutineDec() throws IOException {
		Utility.printNonTerminalOpen("subroutineDec");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'constructor' or 'function' or 'method'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'void' | type
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // subroutineName
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
		compileParameterList();
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
		Utility.printNonTerminalOpen("parameterList");
		
		while (true) {
			jTokenizer.advance();
			if (jTokenizer.token.indexOf(')') != -1) {
				break;
			} else if (jTokenizer.token.indexOf(',') != -1) {  // (',' type varName)
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ','
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // type
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
			} else {  // type varName, only use in the beginning
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // type
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
			}
			
		}
		
		Utility.printNonTerminalClose("parameterList");
	}
	
	void compileVarDec() throws IOException {
		Utility.printNonTerminalOpen("varDec");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'var'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // type
		
		while (true) {
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
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
		Utility.printNonTerminalOpen("doStatement");
		
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'do'
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // identifier
		
		jTokenizer.advance();
		if (jTokenizer.token.indexOf('(') != -1) {  // subroutineName '(' expressionList ')'  
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
			compileExpressionList();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
		} else if (jTokenizer.token.indexOf('.') != -1) {  // (className | varName) '.' subroutineName '(' expressionList ')'
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '.'
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // subroutineName
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
			compileExpressionList();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
		} 
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ';'
		
		Utility.printNonTerminalClose("doStatement");
	}
	
	void compileLet() throws IOException {
		Utility.printNonTerminalOpen("letStatement");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'let'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // varName
		jTokenizer.advance();
		if (jTokenizer.token.indexOf('[') != -1) {  // varName ('[' expression ']')? '=' expression ';'
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '['
			compileExpression();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ']'
			
			jTokenizer.advance();  // get '='
		} 
		
		if (jTokenizer.token.indexOf('=') != -1) {
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '='
			compileExpression();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ';'
		}
		
		Utility.printNonTerminalClose("letStatement");
	}
	
	void compileWhile() throws IOException {
		Utility.printNonTerminalOpen("whileStatement");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'while'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
		compileExpression();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '{'
		jTokenizer.advance();  // ?
		compileStatements();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '}'
		
		Utility.printNonTerminalClose("whileStatement");
	}
	
	
	void compileIf() throws IOException {
		Utility.printNonTerminalOpen("ifStatement");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'if'
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
		compileExpression();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
		
		jTokenizer.advance();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '{'
		jTokenizer.advance();  // ?
		compileStatements();
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '}'
		
		if (jTokenizer.hasNextToken("else")) {
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'else'
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '{'
			jTokenizer.advance();  // ?
			compileStatements();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '}'
		}
		
		Utility.printNonTerminalClose("ifStatement");
	}
	
	void compileReturn() throws IOException {
		Utility.printNonTerminalOpen("returnStatement");
		
		Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // 'return'
		
		if (jTokenizer.hasNextToken("\\;")) {
			jTokenizer.advance();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ';'
		} else {
			compileExpression();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ';'
		}
		
		Utility.printNonTerminalClose("returnStatement");
	}
	
	
	void compileExpression() throws IOException {
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
				compileTerm();
			}
		}
		
		
		Utility.printNonTerminalClose("expression");
	}
	
	void compileTerm() throws IOException {
		Utility.printNonTerminalOpen("term");
		
		jTokenizer.advance();
		if (jTokenizer.token.indexOf('(') != -1) {  // '(' expression ')'
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
			compileExpression();
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
			jTokenizer.advance();
		} else if (jTokenizer.token.indexOf('-') != -1 || jTokenizer.token.indexOf('~') != -1) {
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '-' or '~'
			compileTerm();
		} else {  
			Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // identifier
			jTokenizer.advance();
			if (jTokenizer.token.indexOf('[') != -1) {  // varName '[' expression ']'
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '['
				compileExpression();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ']'
				jTokenizer.advance();
			}
			// subroutineCall -> subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
			else if (jTokenizer.token.indexOf('(') != -1) {  // subroutineName '(' expressionList ')'  
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
				compileExpressionList();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
				jTokenizer.advance();
			} else if (jTokenizer.token.indexOf('.') != -1) {  // (className | varName) '.' subroutineName '(' expressionList ')'
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '.'
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // subroutineName
				jTokenizer.advance();
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // '('
				
				compileExpressionList();
				
				Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // ')'
				jTokenizer.advance();
			} 
		}
		
		Utility.printNonTerminalClose("term");
	}
	
	void compileExpressionList() throws IOException {
		Utility.printNonTerminalOpen("expressionList");
		
		if (jTokenizer.hasNextToken("\\)")) {  // ()
			jTokenizer.advance();
		} else {
			while (true) {
				compileExpression();
				if (jTokenizer.token.indexOf(')') != -1) {
					break;
				} else if (jTokenizer.token.indexOf(',') != -1) {
					Utility.printTerminal(jTokenizer.tokenTypeString(), jTokenizer.token);  // (expression (',' expression)*)?
				}
			}
		}
		
		Utility.printNonTerminalClose("expressionList");
	}
}



class SharedInfo {
	public static ArrayList<String> SYMBOL_LIST; 
	public static ArrayList<String> KEYWORD_LIST;
	
	public static enum TOKEN_TYPE {
		NONE, KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
	}
	
	public static enum KEYWORD {
		NONE, CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR,
		VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN,
		TRUE, FALSE, NULL, THIS
	}
	
	public static String SOURCE_PATH = "";
	
	public static void setup() throws FileNotFoundException {
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