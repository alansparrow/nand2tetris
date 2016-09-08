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
		SharedInfo.SOURCE_PATH = "/Users/baotrungtn/Desktop/nand2tetris/projects/10/ExpressionlessSquare";
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
	


	
	public JackAnalyzer(String filePath) {
		this.filePath = filePath;
	}
	
	public void compile() throws IOException {
		Scanner sc1 = new Scanner(new BufferedReader(new FileReader(filePath)));
		fileContent = sc1.useDelimiter("\\Z").next();
		sc1.close();
		
		String outputFilePath = filePath.substring(0, filePath.indexOf(".jack")) + ".xml";
		fo = new File(outputFilePath);
		fos = new FileOutputStream(fo); // open file for writing result
		osw = new OutputStreamWriter(fos);
		bw = new BufferedWriter(osw);
		
		JackTokenizer jTokenizer = new JackTokenizer(fileContent);
		//printXML(jTokenizer);
		writeXML(jTokenizer);
		bw.close();
		osw.close();
		fos.close();
		
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
	
	void writeXML(JackTokenizer jTokenizer) throws IOException {
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
	
	void advance() {
		token = myScanner.next();
		
		if (token.equals("\"")) {
			tokenType = SharedInfo.TOKEN_TYPE.STRING_CONST;
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
				keyWord = KEYWORD.valueOf(token.toUpperCase());
			} else if (SharedInfo.SYMBOL_LIST.contains("\\" + token) || SharedInfo.SYMBOL_LIST.contains(token)) {  // Symbol
				tokenType = SharedInfo.TOKEN_TYPE.SYMBOL;
				symbol = token.charAt(0);
			} else {
				if (token.charAt(0) >= '0' && token.charAt(0) <= '9') {  // Number
					tokenType = SharedInfo.TOKEN_TYPE.INT_CONST;
					intVal = Integer.valueOf(token);
				} else {  // Identifier
					tokenType = SharedInfo.TOKEN_TYPE.IDENTIFIER;
					identifier = token;
				}
			}
		}
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
	public CompilationEngine() {
		
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