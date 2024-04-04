/*Copyright 2023 by Beverly A Sanders
 *
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the
 * University of Florida during the spring semester 2023 as part of the course project.
 *
 * No other use is authorized.
 *
 * This code may not be posted on a public web site either during or after the course.
 */

package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.ASTVisitor; //Added this to allow for makeTypeChecker

public class CompilerComponentFactory {
	public static IScanner makeScanner(String input) {
		//Add statement to return an instance of your scanner
		Scanner scanner = new Scanner(input);
		return scanner;
	}
	public static IParser makeAssignment2Parser(String input) throws LexicalException {
		//add code to create a scanner and parser and return the parser.
		Parser parser = new Parser(input);
		return parser;
	}
	public static IParser makeParser(String input) throws LexicalException {
		//add code to create a scanner and parser and return the parser.
		Parser parser = new Parser(input);
		return parser;
	}

	public static ASTVisitor makeTypeChecker() {
		//code to instantiate a return an ASTVisitor for type checking
		ASTVisitor visator = new ASTVisitorImp();
		return visator;
	}

	public static ASTVisitor makeCodeGenerator(String packageName) {
		//code to instantiate a return an ASTVisitor for code generation
		GenCode visitor = new GenCode(packageName);
		return visitor;
	}


}