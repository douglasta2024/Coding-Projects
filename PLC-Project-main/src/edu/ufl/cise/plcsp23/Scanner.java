package edu.ufl.cise.plcsp23;

import java.util.Arrays;

public class Scanner implements IScanner{

    String input;
    String input1;
    char[] inputChars;
    int pos;
    char ch;

    //keeps track of the line and column. Each token stores their location, so it's fine if this changes.
    int line;
    int col;

    private enum State{
        START,
        IN_DENT,
        IN_NUM_LIT,
        IN_STRING_LIT,
        IN_COMMENT,
        HAVE_EQ,
        HAVE_AND,
        HAVE_GREAT,
        HAVE_LESS,
        HAVE_OR,
        HAVE_TIMES,
        IN_EXCHANGE
    }

    public Scanner(String input){
        this.input = input;
        inputChars = Arrays.copyOf(input.toCharArray(), input.length() + 1);
        input1 = new String(inputChars);
        pos = 0;
        ch = inputChars[pos];
        line = 1;
        col = 1;
    }

    private void error(String message) throws LexicalException{
        throw new LexicalException("Error at pos " + pos + ": " + message);
    }
    private void nextChar()
    {
        col++;
        pos++;
        ch = inputChars[pos];

    }
    private boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }
    private boolean isLetter(int ch) {
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
    }
    private boolean isIdentStart(int ch) {
        return isLetter(ch) || (ch == '$') || (ch == '_');
    }
    private boolean isInputChar(int ch){
        return ch != '\n' && ch != '\r';
    }


    private IToken.Kind reservedWords(String text)
    {
        return switch (text) {
            case "image" -> IToken.Kind.RES_image;
            case "pixel" -> IToken.Kind.RES_pixel;
            case "int" -> IToken.Kind.RES_int;
            case "string" -> IToken.Kind.RES_string;
            case "void" -> IToken.Kind.RES_void;
            case "nil" -> IToken.Kind.RES_nil;
            case "load" -> IToken.Kind.RES_load;
            case "display" -> IToken.Kind.RES_display;
            case "write" -> IToken.Kind.RES_write;
            case "x" -> IToken.Kind.RES_x;
            case "y" -> IToken.Kind.RES_y;
            case "a" -> IToken.Kind.RES_a;
            case "r" -> IToken.Kind.RES_r;
            case "X" -> IToken.Kind.RES_X;
            case "Y" -> IToken.Kind.RES_Y;
            case "Z" -> IToken.Kind.RES_Z;
            case "x_cart" -> IToken.Kind.RES_x_cart;
            case "y_cart" -> IToken.Kind.RES_y_cart;
            case "a_polar" -> IToken.Kind.RES_a_polar;
            case "r_polar" -> IToken.Kind.RES_r_polar;
            case "rand" -> IToken.Kind.RES_rand;
            case "sin" -> IToken.Kind.RES_sin;
            case "cos" -> IToken.Kind.RES_cos;
            case "atan" -> IToken.Kind.RES_atan;
            case "if" -> IToken.Kind.RES_if;
            case "while" -> IToken.Kind.RES_while;
            case "red" -> IToken.Kind.RES_red;
            case "grn" -> IToken.Kind.RES_grn;
            case "blu" -> IToken.Kind.RES_blu;
            default -> IToken.Kind.IDENT;
        };
    }
    private IToken scanToken() throws LexicalException {
        State state = State.START;
        int tokenStart = -1;
        char prev = 0;
        boolean inString = false;
        String escape = "";
        IToken.SourceLocation sourceLocation = new IToken.SourceLocation(line, col);
        while(true) {
            switch(state) {
                case START -> {
                    tokenStart = pos;
                    sourceLocation = new IToken.SourceLocation(line, col);
                    switch(ch){
                        case 0 -> {



                            return new Token(IToken.Kind.EOF, input1.substring(tokenStart,pos), sourceLocation);

                        }
                        case ' ', '\n', '\r', '\t', '\b', '\\', '\f' -> {
                            prev = ch;
                            if(ch == '\n') {
                                line++;

                                col = 0;
                            }
                            nextChar();

                        }
                        case '+' -> {
                            nextChar();
                            return new Token(IToken.Kind.PLUS, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '*' -> {
                            state = State.HAVE_TIMES;
                            nextChar();
                        }
                        case '0' -> {

                            state = State.IN_NUM_LIT;

                        }
                        case '.' -> {
                            nextChar();
                            return new Token(IToken.Kind.DOT, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case ',' -> {
                            nextChar();
                            return new Token(IToken.Kind.COMMA, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '?' -> {
                            nextChar();
                            return new Token(IToken.Kind.QUESTION, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case ':' -> {
                            nextChar();
                            return new Token(IToken.Kind.COLON, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '!' -> {
                            nextChar();
                            return new Token(IToken.Kind.BANG, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '/' -> {
                            nextChar();
                            return new Token(IToken.Kind.DIV, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '%' -> {
                            nextChar();
                            return new Token(IToken.Kind.MOD, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '-' -> {
                            nextChar();
                            return new Token(IToken.Kind.MINUS, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '(' -> {
                            nextChar();
                            return new Token(IToken.Kind.LPAREN, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case ')' -> {
                            nextChar();
                            return new Token(IToken.Kind.RPAREN, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '[' -> {
                            nextChar();
                            return new Token(IToken.Kind.LSQUARE, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case ']' -> {
                            nextChar();
                            return new Token(IToken.Kind.RSQUARE, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '{' -> {
                            nextChar();
                            return new Token(IToken.Kind.LCURLY, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '}' -> {
                            nextChar();
                            return new Token(IToken.Kind.RCURLY, input1.substring(tokenStart,pos), sourceLocation);
                        }
                        case '=' -> {
                            state = State.HAVE_EQ;
                            nextChar();
                        }
                        case '1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_NUM_LIT;
                            nextChar();
                        }
                        case '<' -> {
                            state = State.HAVE_LESS;
                            nextChar();
                        }
                        case '>' -> {
                            state = State.HAVE_GREAT;
                            nextChar();
                        }
                        case '&' -> {
                            state = State.HAVE_AND;
                            nextChar();
                        }
                        case '|' -> {
                            state = State.HAVE_OR;
                            nextChar();
                        }
                        case '"' -> {
                            state = State.IN_STRING_LIT;
                            inString = true;
                            nextChar();
                        }
                        case '~' -> {
                            state = State.IN_COMMENT;
                            nextChar();
                        }
                        default -> {
                            if(inString == false && prev == 92 && (ch == 'b' || ch == 'n' || ch == 't' || ch == 'r' || ch == '\"' || ch == '\\')){
                                error("escape is outside of string");
                            }
                            if(isLetter(ch) || ch == '_')
                            {
                                state = State.IN_DENT;
                                nextChar();
                            }
                            else {
                                error("illegal char in ascii value: " + (int)ch);
                            }
                        }

                    }
                }
                case IN_COMMENT -> {
                    if(ch == '\n')
                    {
                        line++;
                        col = 0;
                        state = State.START;
                        nextChar();
                    }
                    else {
                        nextChar();
                    }
                }
                case IN_STRING_LIT -> {
                    if(isInputChar(ch))
                    {
                        if(ch == '\\')
                        {
                            prev = ch;
                            nextChar();
                            if(prev == '\\' && (ch == 'n' || ch == 'b' || ch == 't' || ch == 'r' || ch == '\\')){
                                escape = String.valueOf(ch);
                                nextChar();
                            }
                            else if(prev == '\\' && ch == '\"'){
                                nextChar();
                            }
                            else if(ch == '"') {
                                nextChar();
                                if (ch == '\n') {
                                    String input = input1.substring(tokenStart, pos);
                                    return new StringLitToken(IToken.Kind.STRING_LIT, input, sourceLocation);
                                }
                            }
                            else if(ch != '"') {
                                error("illegal char in ascii value in string literal: " + (int) ch);
                            }
                        }
                        if(ch == '\n')
                        {

                            error("illegal char in ascii value in string literal: " + (int)ch);

                        }
                        else
                        {
                            if(ch == '"')
                            {
                                nextChar();
                                return new StringLitToken(IToken.Kind.STRING_LIT, input1.substring(tokenStart,pos), sourceLocation);
                            }
                            else {
                                if(ch == 0){
                                    error("non-terminated string");
                                }
                                nextChar();
                            }
                        }
                    }
                    if(ch == '\n' || ch == '\r'){
                        error("LF or CR passed.");
                    }
                }
                case HAVE_LESS -> {
                    if(ch == '=')
                    {
                        nextChar();
                        return new Token(IToken.Kind.LE, input1.substring(tokenStart,pos), sourceLocation);
                    }
                    else if(ch == '-')
                    {
                        state = State.IN_EXCHANGE;
                        nextChar();
                    }
                    else {
                        return new Token(IToken.Kind.LT, input1.substring(tokenStart,pos), sourceLocation);
                    }
                }
                case IN_EXCHANGE ->
                {
                    if(ch == '>')
                    {
                        nextChar();
                        return new Token(IToken.Kind.EXCHANGE, input1.substring(tokenStart,pos), sourceLocation);
                    }
                    else {
                        error("illegal char in ascii value in exchange operator: " + (int)ch);
                    }
                }
                case HAVE_GREAT -> {
                    if(ch == '=')
                    {
                        nextChar();
                        return new Token(IToken.Kind.GE, input1.substring(tokenStart,pos), sourceLocation);
                    }
                    else {
                        return new Token(IToken.Kind.GT, input1.substring(tokenStart,pos), sourceLocation);
                    }
                }
                case HAVE_AND -> {
                    if(ch == '&')
                    {
                        nextChar();
                        return new Token(IToken.Kind.AND, input1.substring(tokenStart,pos), sourceLocation);
                    }
                    else {
                        nextChar();
                        return new Token(IToken.Kind.BITAND, input1.substring(tokenStart,pos), sourceLocation);
                    }
                }
                case HAVE_OR -> {
                    if(ch == '|')
                    {
                        nextChar();
                        return new Token(IToken.Kind.OR, input1.substring(tokenStart,pos), sourceLocation);
                    }
                    else {
                        nextChar();
                        return new Token(IToken.Kind.BITOR, input1.substring(tokenStart,pos), sourceLocation);
                    }
                }
                case HAVE_TIMES -> {
                    if(ch == '*')
                    {
                        nextChar();
                        return new Token(IToken.Kind.EXP, input1.substring(tokenStart,pos), sourceLocation);
                    }
                    else {
                        //nextChar();
                        return new Token(IToken.Kind.TIMES, input1.substring(tokenStart,pos), sourceLocation);
                    }
                }
                case HAVE_EQ -> {
                    if(ch == '='){
                        nextChar();
                        return new Token(IToken.Kind.EQ, input1.substring(tokenStart,pos), sourceLocation);
                    }
                    else{
                        return new Token(IToken.Kind.ASSIGN, input1.substring(tokenStart,pos), sourceLocation);
                    }
                }
                case IN_NUM_LIT -> {
                    int length = pos-tokenStart;
                    if(ch == '0' && length == 0){
                        nextChar();
                        return new NumLitToken(Token.Kind.NUM_LIT, 0, sourceLocation);
                    }
                    if(isDigit(ch)){
                        nextChar();
                    }
                    else {
                        if(length > 11){
                            error("numLit too big");
                        }

                        String temp = input.substring(tokenStart,tokenStart + length);

                        int value = Integer.parseInt(temp);
                        return new NumLitToken(Token.Kind.NUM_LIT, value, sourceLocation);

                    }
                }
                case IN_DENT -> {
                    if(isIdentStart(ch) || isDigit(ch)){
                        nextChar();
                    }
                    else{
                        /*if(ch == '\n') {
                            line++;
                            col = 0;
                        }*/
                        state = State.START;
                        int length = pos-tokenStart;
                        String text = input1.substring(tokenStart, tokenStart + length);
                        IToken.Kind kind = reservedWords(text);
                        if(pos >= input.length())
                        {
                            pos--;
                        }
                        if(ch == '\n') {
                            line++;
                            col = 0;
                            nextChar();
                        }
                        return new Token(kind,text,sourceLocation);
                    }
                }
            }
        }
    }

    @Override
    public IToken next() throws LexicalException {
        return scanToken();
    }
}