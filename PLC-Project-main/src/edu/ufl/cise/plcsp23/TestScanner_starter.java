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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.IToken.SourceLocation;

class TestScanner_starter {

	// makes it easy to turn output on and off (and less typing than
	// System.out.println)
	static final boolean VERBOSE = true;

	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}

	// check that this token has the expected kind
	void checkToken(Kind expectedKind, IToken t) {
		assertEquals(expectedKind, t.getKind());
	}

	void checkToken(Kind expectedKind, String expectedChars, SourceLocation expectedLocation, IToken t) {
		assertEquals(expectedKind, t.getKind());
		assertEquals(expectedChars, t.getTokenString());
		assertEquals(expectedLocation, t.getSourceLocation());
		;
	}

	void checkIdent(String expectedChars, IToken t) {
		checkToken(Kind.IDENT, t);
		assertEquals(expectedChars.intern(), t.getTokenString().intern());
		;
	}

	void checkString(String expectedValue, IToken t) {
		assertTrue(t instanceof IStringLitToken);
		assertEquals(expectedValue, ((IStringLitToken) t).getValue());
	}

	void checkString(String expectedChars, String expectedValue, SourceLocation expectedLocation, IToken t) {
		assertTrue(t instanceof IStringLitToken);
		assertEquals(expectedValue, ((IStringLitToken) t).getValue());
		assertEquals(expectedChars, t.getTokenString());
		assertEquals(expectedLocation, t.getSourceLocation());
	}

	void checkNUM_LIT(int expectedValue, IToken t) {
		checkToken(Kind.NUM_LIT, t);
		int value = ((INumLitToken) t).getValue();
		assertEquals(expectedValue, value);
	}

	void checkNUM_LIT(int expectedValue, SourceLocation expectedLocation, IToken t) {
		checkToken(Kind.NUM_LIT, t);
		int value = ((INumLitToken) t).getValue();
		assertEquals(expectedValue, value);
		assertEquals(expectedLocation, t.getSourceLocation());
	}

	void checkTokens(IScanner s, IToken.Kind... kinds) throws LexicalException {
		for (IToken.Kind kind : kinds) {
			checkToken(kind, s.next());
		}
	}

	void checkTokens(String input, IToken.Kind... kinds) throws LexicalException {
		IScanner s = CompilerComponentFactory.makeScanner(input);
		for (IToken.Kind kind : kinds) {
			checkToken(kind, s.next());
		}
	}

	// check that this token is the EOF token
	void checkEOF(IToken t) {
		checkToken(Kind.EOF, t);
	}


	@Test
	void emptyProg() throws LexicalException {
		String input = "";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkEOF(scanner.next());
	}

	@Test
	void onlyWhiteSpace() throws LexicalException {
		String input = " \t \r\n \f \n";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkEOF(scanner.next());
		checkEOF(scanner.next());  //repeated invocations of next after end reached should return EOF token
	}

	@Test
	void numLits1() throws LexicalException {
		String input = """
				123
				05 240
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkNUM_LIT(123, scanner.next());
		checkNUM_LIT(0, scanner.next());
		checkNUM_LIT(5, scanner.next());
		checkNUM_LIT(240, scanner.next());
		checkEOF(scanner.next());
	}

	@Test
		//Too large should still throw LexicalException
	void numLitTooBig() throws LexicalException {
		String input = "999999999999999999999";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		assertThrows(LexicalException.class, () -> {
			scanner.next();
		});
	}


	@Test
	void  identsandreservedserved() throws LexicalException {
		String input = """
				i0
				  i1  x ~~~2 spaces at beginning and after il
				y Y
				""";

		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.IDENT,"i0", new SourceLocation(1,1), scanner.next());
		checkToken(Kind.IDENT, "i1",new SourceLocation(2,3), scanner.next());
		checkToken(Kind.RES_x, "x", new SourceLocation(2,7), scanner.next());
		checkToken(Kind.RES_y, "y", new SourceLocation(3,1), scanner.next());
		checkToken(Kind.RES_Y, "Y", new SourceLocation(3,3), scanner.next());
		checkEOF(scanner.next());
	}


	@Test
	void  operators0() throws LexicalException {
		String input = """
				==
				+
				/
				====
				=
				===
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.PLUS, scanner.next());
		checkToken(Kind.DIV, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.ASSIGN, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.ASSIGN, scanner.next());
		checkEOF(scanner.next());
	}


	@Test
	void stringLiterals1() throws LexicalException {
		String input = """
				"hello"
				"\t"
				"\\""
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkString(input.substring(0, 7),"hello", new SourceLocation(1,1), scanner.next());
		checkString(input.substring(8, 11), "\t", new SourceLocation(2,1), scanner.next());
		checkString(input.substring(12, 16), "\"",  new SourceLocation(3,1), scanner.next());
		checkEOF(scanner.next());
	}


	@Test
	void illegalEscape() throws LexicalException {
		String input = """
				"\\t"
				"\\k"
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkString("\"\\t\"","\t", new SourceLocation(1,1), scanner.next());
		assertThrows(LexicalException.class, () -> {
			scanner.next();
		});
	}

	@Test
	void illegalLineTermInStringLiteral() throws LexicalException {
		String input = """
				"\\n"  ~ this one passes the escape sequence--it is OK
				"\n"   ~ this on passes the LF, it is illegal.
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkString("\"\\n\"","\n", new SourceLocation(1,1), scanner.next());
		assertThrows(LexicalException.class, () -> {
			scanner.next();
		});
	}

	@Test
	void lessThanGreaterThanExchange() throws LexicalException {
		String input = """
				<->>>>=
				<<=<
				""";
		checkTokens(input, Kind.EXCHANGE, Kind.GT, Kind.GT, Kind.GE, Kind.LT, Kind.LE, Kind.LT, Kind.EOF);
	}

	@Test
	void incompleteExchangethrowsException() throws LexicalException {
		String input = " <- ";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		assertThrows(LexicalException.class, () -> {
			scanner.next();
		});
	}

	@Test
	void illegalChar() throws LexicalException {
		String input = """
				abc
				@
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkIdent("abc", scanner.next());
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken t = scanner.next();
		});
	}
	@Test
	void equals() throws LexicalException {
		String input = """
     				==
					== ==
					==-==
					====
					""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.MINUS, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.EQ, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void singleCharTokens0() throws LexicalException{
		String input = "+00";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.PLUS, scanner.next());
		checkNUM_LIT(0, scanner.next());
		checkNUM_LIT(0, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void singleCharTokens1() throws LexicalException{ //CHECK IF TEST IS CORRECT. NOT SURE IF I SHOULD BE CHECKING FOR INDIVIDUAL PARENTHESIS EACH OR BOTH TOGETHER. IMPLEMENTED INDIVIDUAL FIRST
		String input = ".,?:()[]{}";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.DOT, scanner.next());
		checkToken(Kind.COMMA, scanner.next());
		checkToken(Kind.QUESTION, scanner.next());
		checkToken(Kind.COLON, scanner.next());
		checkTokens(scanner, Kind.LPAREN, Kind.RPAREN, Kind.LSQUARE, Kind.RSQUARE, Kind.LCURLY, Kind.RCURLY);
		checkEOF(scanner.next());
	}

	@Test
	void singleCharTokens2() throws LexicalException{
		String input = "+-/%";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkTokens(scanner, Kind.PLUS, Kind.MINUS, Kind.DIV, Kind.MOD);
		checkEOF(scanner.next());
	}
	@Test
	void singleCharTokensWithWhiteSpace() throws LexicalException{
		String input = """
				+ %
				0
				0
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.PLUS, scanner.next());
		checkToken(Kind.MOD, scanner.next());
		checkNUM_LIT(0, scanner.next());
		checkNUM_LIT(0, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void idents0() throws LexicalException{
		String input = """
				i0
				i1
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.IDENT,"i0", new SourceLocation(1,1), scanner.next());
		checkToken(Kind.IDENT,"i1", new SourceLocation(2,1), scanner.next());
		checkEOF(scanner.next());
	}

	@Test
	void identsandreserved2() throws LexicalException{ //NEED TO CHECK THIS TEST CASE AS WELL. DEF NOT FINISHED
		String input = """
				i0
				  i1  x ~~~2 spaces at beginning and after il
				  ~comment~
				  "hello there"
				sin cos
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.IDENT,"i0", new SourceLocation(1,1), scanner.next());
		checkToken(Kind.IDENT,"i1", new SourceLocation(2,3), scanner.next());
		checkToken(Kind.RES_x, "x", new SourceLocation(2,7), scanner.next());
		//checkString()
	}

	@Test
	void identsWithUnderscore() throws LexicalException{
		String input = """
				i0
				i1
				_
				__
				a_b_c
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.IDENT,"i0", new SourceLocation(1,1), scanner.next());
		checkToken(Kind.IDENT,"i1", new SourceLocation(2,1), scanner.next());
		checkToken(Kind.IDENT,"_", new SourceLocation(3,1), scanner.next());
		checkToken(Kind.IDENT,"__", new SourceLocation(4,1), scanner.next());
		checkToken(Kind.IDENT,"a_b_c", new SourceLocation(5,1), scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operators1() throws LexicalException{
		String input = """
				**** *
				& && &&&
				| || |||
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkTokens(scanner, Kind.EXP, Kind.EXP, Kind.TIMES, Kind.BITAND, Kind.AND, Kind.AND, Kind.BITAND, Kind.BITOR, Kind.OR, Kind.OR, Kind.BITOR);
		checkEOF(scanner.next());
	}
	@Test
	void comment0() throws LexicalException{
		String input = """
				==
				+ ~reandomcharse!@#$%W%$^#%&$
				/
				====
				=
				~comment at begining of line
				===
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.EQ, scanner.next());
		checkToken(Kind.PLUS, scanner.next());
		checkTokens(scanner, Kind.DIV, Kind.EQ, Kind.EQ, Kind.ASSIGN, Kind.EQ, Kind.ASSIGN);
		checkEOF(scanner.next());
	}
	@Test
	void stringEscape() throws LexicalException{ //Not sure if this is legal implemenation
		String input = """
				"\\b \\t \\n \\r \\" \\\\" 
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkString("\"\\b \\t \\n \\r \\\" \\\\\"","\b \t \n \r \\\" \\\\", new SourceLocation(1,1), scanner.next());
		/*checkString("\\t","\t", new SourceLocation(1,5), scanner.next());
		checkString("\\n","\n", new SourceLocation(1,9), scanner.next());
		checkString("\\r","\r", new SourceLocation(1,13), scanner.next());
		checkString("\\\"","\\\"", new SourceLocation(1,17), scanner.next());
		checkString("\\\\\"","\\\\\"", new SourceLocation(1,21), scanner.next());*/
		checkEOF(scanner.next());
	}
	@Test
	void escapeOutsideString() throws LexicalException{ //Not sure abt this one either
		String input = """
				\\b \\t \\n \\r \\" \\\\ 
				""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		assertThrows(LexicalException.class, () -> {
			scanner.next();
		});

	}
	@Test
	void operator1() throws LexicalException{
		String input = """
	                        .
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.DOT, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator2() throws LexicalException{
		String input = """
	                        ,
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.COMMA, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator3() throws LexicalException{
		String input = """
	                        ?
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.QUESTION, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator4() throws LexicalException{
		String input = """
	                        :
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.COLON, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator5() throws LexicalException{
		String input = """
	                        (
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.LPAREN, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator6() throws LexicalException{
		String input = """
	                        )
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RPAREN, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator7() throws LexicalException{
		String input = """
	                        <
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.LT, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator8() throws LexicalException{
		String input = """
	                        >
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.GT, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator9() throws LexicalException{
		String input = """
	                        [
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.LSQUARE, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator10() throws LexicalException{
		String input = """
	                        ]
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RSQUARE, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator11() throws LexicalException{
		String input = """
	                        {
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.LCURLY, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator12() throws LexicalException{
		String input = """
	                        }
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RCURLY, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator13() throws LexicalException{
		String input = """
	                        =
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.ASSIGN, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator14() throws LexicalException{
		String input = """
	                        ==
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.EQ, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator15() throws LexicalException{
		String input = """
	                        <->
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.EXCHANGE, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator16() throws LexicalException{
		String input = """
	                        <=
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.LE, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator17() throws LexicalException{
		String input = """
	                        >=
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.GE, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator18() throws LexicalException{
		String input = """
	                        !
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.BANG, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator19() throws LexicalException{
		String input = """
	                        &
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.BITAND, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator20() throws LexicalException{
		String input = """
	                        &&
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.AND, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator21() throws LexicalException{
		String input = """
	                        |
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.BITOR, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator22() throws LexicalException{
		String input = """
	                        ||
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.OR, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator23() throws LexicalException{
		String input = """
	                        +
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.PLUS, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator24() throws LexicalException{
		String input = """
	                        -
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.MINUS, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator25() throws LexicalException{
		String input = """
	                        *
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.TIMES, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator26() throws LexicalException{
		String input = """
	                        **
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.EXP, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator27() throws LexicalException{
		String input = """
	                        /
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.DIV, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void operator28() throws LexicalException{
		String input = """
	                        %
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.MOD, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved1() throws LexicalException{
		String input = """
	                        image
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_image, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved2() throws LexicalException{
		String input = """
	                        pixel
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_pixel, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved3() throws LexicalException{
		String input = """
	                        int
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_int, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved4() throws LexicalException{
		String input = """
	                        string
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_string, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved5() throws LexicalException{
		String input = """
	                        void
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_void, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved6() throws LexicalException{
		String input = """
	                        nil
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_nil, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved7() throws LexicalException{
		String input = """
	                        load
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_load, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved8() throws LexicalException{
		String input = """
	                        display
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_display, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved9() throws LexicalException{
		String input = """
	                        write
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_write, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved10() throws LexicalException{
		String input = """
	                        x
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_x, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved11() throws LexicalException{
		String input = """
	                        y
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_y, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved12() throws LexicalException{
		String input = """
	                        a
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_a, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved13( )throws LexicalException{
		String input = """
	                        r
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_r, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved14() throws LexicalException{
		String input = """
	                        X
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_X, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved15() throws LexicalException{
		String input = """
	                        Y
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_Y, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved16() throws LexicalException{
		String input = """
	                        Z
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_Z, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved17() throws LexicalException{
		String input = """
	                        x_cart
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_x_cart, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved18() throws LexicalException{
		String input = """
	                        y_cart
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_y_cart, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved19() throws LexicalException{
		String input = """
	                        a_polar
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_a_polar, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved20() throws LexicalException{
		String input = """
	                        r_polar
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_r_polar, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved21() throws LexicalException{
		String input = """
	                        rand
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_rand, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved22() throws LexicalException{
		String input = """
	                        sin
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_sin, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved23() throws LexicalException{
		String input = """
	                        cos
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_cos, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved24() throws LexicalException{
		String input = """
	                        atan
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_atan, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved25() throws LexicalException{
		String input = """
	                        if
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_if, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void reserved26() throws LexicalException{
		String input = """
	                        while
	                        """;
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		checkToken(Kind.RES_while, scanner.next());
		checkEOF(scanner.next());
	}
	@Test
	void nonTerminatedString() throws LexicalException{
		String input = """
				"abc""";
		IScanner scanner = CompilerComponentFactory.makeScanner(input);
		assertThrows(LexicalException.class, () -> {
			scanner.next();
		});
	}
}