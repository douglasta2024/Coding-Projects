package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.ast.Dimension;

import java.awt.*;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser{
    private String input;
    private List<IToken> tokens;
    private int current = 0;



    private void error(String message) throws PLCException{
        throw new SyntaxException("Error at token " + current + ": " + message);
    }
    private boolean checkEOF(IToken token)
    {
        return IToken.Kind.EOF == token.getKind();
    }
    public Parser(String input) throws LexicalException {
        this.input = input;
        tokens = new ArrayList<>();
        IScanner scanner = CompilerComponentFactory.makeScanner(input);
        IToken currentToken = scanner.next();
        while(!checkEOF(currentToken)){
            tokens.add(currentToken);
            //AT THE END DO A SCANNER.NEXT
            currentToken = scanner.next();
        }
        tokens.add(currentToken);
    }
    private IToken previous() {
        return tokens.get(current-1);
    }
    private boolean isAtEnd(){
        return peek().getKind() == IToken.Kind.EOF;
    }
    private IToken peek(){
        return tokens.get(current);
    }
    private IToken advance(){
        if(!checkEOF(peek()))
        {
            current++;
        }
        return previous();
    }
    private IToken retreat(){
        if(current > 0)
        {
            current--;
        }
        return previous();
    }

    private boolean check(IToken.Kind type) {
        if (checkEOF(peek())){
            return false;
        }
        return peek().getKind() == type;
    }
    private boolean match(IToken.Kind... types) {
        for (IToken.Kind type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    private Program program() throws PLCException {
        //CHECK FOR TYPE USING type.getType AND A TRY CATCH TO CATCH THE RUNTIME ERROR
        try{
            Type t = Type.getType(peek());
            advance();
            IToken firstToken = previous();
            if(match(IToken.Kind.IDENT)){
                Ident i = new Ident(previous());
                if(match(IToken.Kind.LPAREN)){
                    List<NameDef> paramList = paramList();
                    Block b = block();
                    return new Program(firstToken,t,i,paramList,b);

                    /*if(match(IToken.Kind.RCURLY))
                    {
                        Block b = block();
                        return new Program(firstToken,t,i,paramList,b);
                    }
                    else{
                        error("Expected '}' after parameter list.");
                    }*/
                }
                else{
                    error("Expected '{' after identifier.");
                }
            }
            else {
                error("Invalid program identifier.");
            }
        }
        catch(RuntimeException e){
            error("Invalid return type.");
        }
        return null;
    }
    private Block block() throws PLCException {
        if(match(IToken.Kind.LCURLY)) {
            IToken e = previous();
            List<Declaration> dec_List = decList();
            List<Statement> stateList = statementList();
            if(match(IToken.Kind.RCURLY))
            {
                return new Block(e,dec_List, stateList);
            }
            else {
                error("'}' Expected after expression." );
            }
        }
        else{
            error("wrong condition");
        }
        return null;
    }
    private List<Declaration> decList() throws PLCException {
        List<Declaration> declarations = new ArrayList<>();
        Declaration d = declaration();
        while(d != null)
        {
            declarations.add(d);
            d = declaration();
        }
        return declarations;
    }
    private List<Statement> statementList() throws PLCException {
        List<Statement> statements = new ArrayList<>();
        Statement s = statement();
        while(s != null)
        {
            statements.add(s);
            s = statement();
        }
        return statements;
    }
    private List<NameDef> paramList() throws PLCException {
        List<NameDef> params = new ArrayList<>();
        boolean commaTrack = true;
        while(!match(IToken.Kind.RPAREN))
        {
            if(commaTrack == false)
            {
                error("Expected parameter after comma");
            }
            NameDef n = nameDef();
            params.add(n);
            if(!match(IToken.Kind.COMMA))
            {
                commaTrack = false;
            }
        }
        return params;
    }
    private NameDef nameDef() throws PLCException {
        try {
            Type t = Type.getType(peek());
            advance(); //advances from type
            IToken firstToken = previous();
            if(match(IToken.Kind.IDENT)){
                IToken secondToken = previous();
                Ident ident = new Ident(secondToken);
                NameDef d = new NameDef(firstToken, t, null, ident);
                ident.setDef(d);
                return d;
            }
            Dimension d = dimension();
            if(match(IToken.Kind.IDENT)) {
                Ident i = new Ident(previous());
                return new NameDef(firstToken,t,d,i);
            }
            else {
                error("Invalid identifier");
            }
        }
        catch(RuntimeException e){
            return null;
        }
        return null;
    }
    private Declaration declaration() throws PLCException {
        IToken firstToken = peek();
        NameDef n = null;
        if(firstToken.getKind() == IToken.Kind.RCURLY){
            return null;
        }
        if(firstToken.getKind() == IToken.Kind.RES_while || firstToken.getKind() == IToken.Kind.RES_write){
            return null;
        }
        /*if(match(IToken.Kind.DOT)){
            if(!match(IToken.Kind.RES_image, IToken.Kind.RES_string, IToken.Kind.RES_int, IToken.Kind.RES_void, IToken.Kind.RES_pixel)){
                return null;
            }
        }*/
        n = nameDef();
        if(n != null)
        {
            Expr e = null;
            if(match(IToken.Kind.ASSIGN))
            {
                e = expression();
            }
            if(match(IToken.Kind.DOT)){

                Declaration d = new Declaration(firstToken,n,e);
                return d;
            }
            else{
                error("Expected .");
            }
            //Declaration d = new Declaration(firstToken,n,e);
            //return d;
        }
        return null;
    }
    private Expr expression() throws PLCException{
        if(match(IToken.Kind.RES_if)){
            return conditional();
        }
        else{
            return or();
        }
    }
    private Expr conditional() throws PLCException{

        IToken firstToken = previous();
        Expr guard = expression();
        if(match(IToken.Kind.QUESTION)){
            Expr trueCase = expression();
            if(match(IToken.Kind.QUESTION)){
                Expr falseCase = expression();
                return new ConditionalExpr(firstToken, guard, trueCase, falseCase);
            }
            else{
                error("wrong condition");
            }
        }
        else{
            error("wrong condition");
        }
        return null;
    }
    private Expr or() throws PLCException{
        Expr exp = and();
        while(match(IToken.Kind.OR) || match(IToken.Kind.BITOR)){
            IToken firstToken = previous();
            Expr right = and();
            IToken.Kind operator = firstToken.getKind();
            exp = new BinaryExpr(firstToken, exp, operator, right);
        }
        return exp;
    }
    private Expr and() throws PLCException{
        Expr exp = comparison();
        while(match(IToken.Kind.AND) || match(IToken.Kind.BITAND)){
            IToken firstToken = previous();
            Expr right = comparison();
            IToken.Kind operator = firstToken.getKind();
            exp = new BinaryExpr(firstToken, exp, operator, right);
        }
        return exp;
    }
    private Expr comparison() throws PLCException{
        Expr exp = power();
        while((match(IToken.Kind.GT) || match(IToken.Kind.LT) || match(IToken.Kind.GE) || match(IToken.Kind.LE) || match(IToken.Kind.EQ))){
            IToken firstToken = previous();
            Expr right = power();
            IToken.Kind operator = firstToken.getKind();
            exp = new BinaryExpr(firstToken, exp, operator, right);
        }
        return exp;
    }
    private Expr power() throws PLCException{ // power_expr = additive_exp(**(power_expr) | e)
        Expr e = additive();
        if(match(IToken.Kind.EXP)){
            IToken firstToken = previous();
            Expr right = power();
            IToken.Kind operator = firstToken.getKind();
            return new BinaryExpr(firstToken, e, operator, right);
        }
        else{
            return e;
        }
    }
    private Expr additive() throws PLCException{
        Expr exp = multiplicative();
        while((match(IToken.Kind.PLUS) || match(IToken.Kind.MINUS))){
            IToken firstToken = previous();
            Expr right = multiplicative();
            IToken.Kind operator = firstToken.getKind();
            exp = new BinaryExpr(firstToken, exp, operator, right);
        }
        return exp;
    }
    private Expr multiplicative() throws PLCException{
        Expr exp = unary();
        while((match(IToken.Kind.TIMES) || match(IToken.Kind.DIV) || match(IToken.Kind.MOD))){
            IToken firstToken = previous();
            Expr right = unary();
            IToken.Kind operator = firstToken.getKind();
            exp = new BinaryExpr(firstToken, exp, operator, right);
        }
        return exp;
    }
    private Expr unary() throws PLCException {
        if(match(IToken.Kind.BANG, IToken.Kind.MINUS, IToken.Kind.RES_sin, IToken.Kind.RES_cos, IToken.Kind.RES_atan))
        {
            IToken firstToken = previous();
            IToken.Kind operator = firstToken.getKind();
            Expr e = unary();
            return new UnaryExpr(firstToken, operator, e);
        }
        else {
            Expr expr = unaryPost();
            return expr;
        }
    }
    private PixelSelector pixelSelector() throws PLCException {
        if(match(IToken.Kind.LSQUARE)){
            Expr e = expression();
            if(match(IToken.Kind.COMMA))
            {
                Expr e2 = expression();
                if(match(IToken.Kind.RSQUARE))
                {
                    return new PixelSelector(previous(),e,e2);
                }
                else {
                    error("']' Expected after expression.");
                }
            }
            else {
                error("',' Expected after 1st expression.");
            }
        }
        return null;
    }
    private Expr unaryPost() throws PLCException {
        //CHECK FOR color USING ColorChannel.getColor AND A TRY CATCH TO CATCH THE RUNTIME ERROR
        Expr primary = primary();
        IToken firstToken = previous();
        if(firstToken.getKind() == IToken.Kind.RSQUARE){ //This lowkey shouldn't work. Used to fix problem for 11
            return primary;
        }
        if(firstToken.getKind() == IToken.Kind.NUM_LIT){
            return primary;
        }
        if(match(IToken.Kind.DOT)){
            retreat(); ///////////////////////////////////////////////
            return primary;
        }
        if(firstToken.getKind() == IToken.Kind.RES_x || firstToken.getKind() ==  IToken.Kind.RES_y || firstToken.getKind() == IToken.Kind.RES_a || firstToken.getKind() ==  IToken.Kind.RES_r){
            return primary;
        }
        else{
            PixelSelector pixel = pixelSelector();
            ColorChannel channel = null;
            try{
                IToken secondToken = peek();
                if(secondToken.getKind() == IToken.Kind.COLON){
                    IToken temp = firstToken;
                    firstToken = secondToken;
                    advance();
                    secondToken = peek();
                    firstToken = temp;
                    channel = ColorChannel.getColor(secondToken);
                    advance();
                }
                //return channel; Need to fix this
            }
            catch(Exception e){
                channel = null;
            }
            if(pixel == null && channel == null){
                return primary;
            }
            return new UnaryExprPostfix(firstToken, primary, pixel, channel);

        }
    }
    private Expr primary() throws PLCException {
        if(match(IToken.Kind.STRING_LIT)) return new StringLitExpr(previous());
        if(match(IToken.Kind.NUM_LIT)) return new NumLitExpr(previous());
        if(match(IToken.Kind.IDENT)) return new IdentExpr(previous());

        if(match(IToken.Kind.LPAREN)){
            Expr e = expression();
            if(match(IToken.Kind.RPAREN))
            {
                return e;
            }
            else {
                error("')' Expected after expression.");
            }
        }

        if(match(IToken.Kind.RES_Z)) return new ZExpr(previous());
        if(match(IToken.Kind.RES_rand)) return new RandomExpr(previous());
        if(match(IToken.Kind.RES_x)) return new PredeclaredVarExpr(previous());
        if(match(IToken.Kind.RES_y)) return new PredeclaredVarExpr(previous());
        if(match(IToken.Kind.RES_a)) return new PredeclaredVarExpr(previous());
        if(match(IToken.Kind.RES_r)) return new PredeclaredVarExpr(previous());

        Expr exp = expandedPixel();
        if(exp == null)
        {
            exp = pixelFunctionExpr();
            if(exp == null)
            {
                error("invalid Syntax, expression not valid");
            }
        }
        return exp;
    }
    private ColorChannel channelSelector() throws PLCException{
        IToken firstToken = previous();
        if(match(IToken.Kind.COLON)){
            ColorChannel color = ColorChannel.getColor(firstToken);
            return color;
        }
        //Might need to put error catcher here
        return null;
    }
    private Expr expandedPixel() throws PLCException {
        if(match(IToken.Kind.LSQUARE)){
            Expr e = expression();
            if(match(IToken.Kind.COMMA))
            {
                Expr e2 = expression();
                if(match(IToken.Kind.COMMA))
                {
                    Expr e3 = expression();
                    if(match(IToken.Kind.RSQUARE))
                    {
                        return new ExpandedPixelExpr(previous(),e,e2,e3);
                    }
                    else {
                        error("']' Expected after expressions.");
                    }
                }
                else {
                    error("',' Expected after 2nd expression.");
                }
            }
            else {
                error("',' Expected after 1st expression.");
            }
        }
        return null;
    }
    private Expr pixelFunctionExpr() throws PLCException {
        if(match(IToken.Kind.RES_x_cart, IToken.Kind.RES_y_cart, IToken.Kind.RES_a_polar, IToken.Kind.RES_r_polar))
        {
            IToken firstToken = previous();
            IToken.Kind operator = firstToken.getKind();
            PixelSelector e = pixelSelector();
            if(e == null){
                error("Missing pixel selector");
            }
            return new PixelFuncExpr(firstToken, operator, e);
        }
        return null;
    }
    private Dimension dimension() throws PLCException {
        if(match(IToken.Kind.LSQUARE)){
            Expr e = expression();
            if(match(IToken.Kind.COMMA))
            {
                Expr e2 = expression();
                if(match(IToken.Kind.RSQUARE))
                {
                    return new Dimension(previous(),e,e2);
                }
                else {
                    error("']' Expected after expression.");
                }
            }
            else {
                error("',' Expected after 1st expression.");
            }
        }
        return null;
    }
    private LValue lValue() throws PLCException {
        IToken token = peek();
        if(token.getKind() == IToken.Kind.DOT){
            advance();
        }
        if(match(IToken.Kind.IDENT)) {
            IToken firstToken = previous();
            Ident i = new Ident(previous());
            PixelSelector pixel = null;
            ColorChannel channel = null;
            pixel = pixelSelector();
            try{
                IToken secondToken = previous();
                if(match(IToken.Kind.COLON)) {
                    channel = ColorChannel.getColor(peek());
                    advance();
                }
                //return channel; Need to fix this
            }
            catch(Exception e){
                channel = null;
            }
            return new LValue(firstToken,i,pixel,channel);

        }
        return null;
    }
    private Statement statement() throws PLCException {
        LValue l = lValue();
        if(l == null)
        {
            if(match(IToken.Kind.RES_write))
            {
                IToken firstToken = previous();
                Expr e = expression();
                if(match(IToken.Kind.DOT)){
                    return new WriteStatement(firstToken, e);
                }
                else{
                    error("Expected .");
                }
                //return new WriteStatement(firstToken, e);
            }
            if(match(IToken.Kind.RES_while))
            {
                IToken firstToken = previous();
                Expr e = expression();
                Block b = block();
                if(match(IToken.Kind.DOT)){
                    return new WhileStatement(firstToken, e, b);
                }
                else{
                    error("Expected .");
                }
                //return new WhileStatement(firstToken, e, b);
            }
            if(match(IToken.Kind.COLON)){
                IToken firstToken = previous();
                Expr e = expression();
                if(match(IToken.Kind.DOT)){
                    return new ReturnStatement(firstToken, e);
                }
                else{
                    error("Expected .");
                }
            }
        }
        else {
            IToken firstToken = previous();
            if(match(IToken.Kind.ASSIGN))
            {
                Expr e = expression();
                if(e == null)
                {
                    error("Expected expression after '='");
                }
                if(match(IToken.Kind.DOT)){
                    return new AssignmentStatement(firstToken, l, e);
                }
                else{
                    error("Expected .");
                }
                //return new AssignmentStatement(firstToken, l, e);
            }
            else {
                error("Expected '=' after identifier");
            }
        }
        return null;
    }

    /*
    ASSOCIATIVITY:
    BANG, MINUS, RES_SIN, RES_COS, RES_ATAN |   RIGHT
    TIMES, DIV, MOD                         |   LEFT
    PLUS, MINUS                             |   LEFT
    EXP                                     |   RIGHT
    GT,LT,GE,LE,EXCHANGE,EQ                 |   LEFT
    BITOR, BITAND                           |   LEFT
    AND                                     |   LEFT
    OR                                      |   LEFT
    QUESTION                                |   LEFT
    ONLY POWER AND QUESTION MARK IS RIGHT
     */
    @Override
    public AST parse() throws PLCException{
        /*List<Statement> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(statement());
        }*/
        return program();
    }
}