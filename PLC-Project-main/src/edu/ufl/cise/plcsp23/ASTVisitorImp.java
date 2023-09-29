package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class ASTVisitorImp implements ASTVisitor {
    Program rootProgram;
    String declarationName = "";
    boolean inDeclaration = false;

    public static class SymbolTable {
        Stack<Integer> scopeStack = new Stack<>();
        Stack<HashMap<String,NameDef>> scopes = new Stack<>();
        int currS = 0;
        int nextS = 0;
        HashMap<String,NameDef> entries;
        //returns true if name successfully inserted in symbol table, false if already present
        public boolean insert(String name, NameDef declaration) {
            return (entries.putIfAbsent(name,declaration) == null);
        }
        public void enter()
        {
            currS = nextS++;
            HashMap<String,NameDef> newEntries = new HashMap<>();
            entries = newEntries;
            scopes.push(entries);
            scopeStack.push(currS);
        }
        public void leave()
        {
            currS = scopeStack.pop();
            entries = scopes.pop();
        }
        //returns Declaration if present, or null if name not declared.
        public NameDef lookup(String name) {
            Stack<HashMap<String,NameDef>> temp = new Stack<>();
            NameDef d = null;
            while(!scopes.empty())
            {
                if(scopes.peek().get(name) == null)
                {
                    temp.push(scopes.pop());
                }
                else
                {
                    d = scopes.peek().get(name);
                    while(!temp.empty())
                    {
                        scopes.push(temp.pop());
                    }
                    return d;
                }
            }
            while(!temp.empty())
            {
                scopes.push(temp.pop());
            }
            return d;
        }
    }
    SymbolTable symbolTable = new SymbolTable();
    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (! condition) { throw new TypeCheckException(message); }
    }

    private boolean assignmentCompatible(Type targetType, Type rhsType) {
        return (targetType == rhsType
                || targetType==Type.STRING && rhsType==Type.INT
                || targetType==Type.STRING && rhsType==Type.IMAGE
                || targetType==Type.STRING && rhsType==Type.PIXEL
                || targetType==Type.IMAGE && rhsType==Type.PIXEL
                || targetType==Type.IMAGE && rhsType==Type.STRING
                || targetType==Type.PIXEL && rhsType==Type.INT
                || targetType==Type.INT && rhsType==Type.PIXEL
        );
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException{
        String name = statementAssign.getLv().getFirstToken().getTokenString();
        //Declaration declaration = (Declaration) symbolTable.lookup(name);
        //check(declaration != null, statementAssign, "undeclared variable " + name);
        Type LvType= (Type) statementAssign.getLv().visit(this, arg);
        Type expressionType= (Type) statementAssign.getE().visit(this, arg);
        check(assignmentCompatible(LvType, expressionType), statementAssign,
                "incompatible types in assignment");
        //declaration.setAssigned(true);
        return null;

    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException{
        IToken.Kind op = binaryExpr.getOp();
        String name1 = binaryExpr.getLeft().getFirstToken().getTokenString();
        String name2 = binaryExpr.getRight().getFirstToken().getTokenString();
        checkName(name1);
        checkName(name2);
        Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
        Type resultType = null;
        switch(op) {//AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE
            case EQ  -> {
                check(leftType == rightType, binaryExpr, "incompatible types for comparison");
                resultType = Type.INT;
            }
            case PLUS -> {
                check(leftType == rightType, binaryExpr, "incompatible types for comparison");
                resultType = leftType;
            }
            case  MINUS -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
                else if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case TIMES, DIV, MOD -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
                else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
                else if (leftType == Type.PIXEL && rightType == Type.INT) resultType = Type.PIXEL;
                else if (leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case LT, LE, GT, GE, OR, AND -> {
                if (leftType == rightType && leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case EXP -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.PIXEL && rightType == Type.INT) resultType = Type.PIXEL;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case BITAND, BITOR -> {
                if (leftType == rightType && leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            default -> {
                throw new TypeCheckException("Compiler Error");
            }
        }
        binaryExpr.setType(resultType);
        return resultType;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException{
        List<Declaration> DecList = block.getDecList();
        List<Statement> statementList = block.getStatementList();
        for (Declaration node :DecList) {
            node.visit(this, arg);
        }
        for (Statement node :statementList) {
            node.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException{
        Type guard = (Type) conditionalExpr.getGuard().visit(this, arg);
        Type trueCase = (Type) conditionalExpr.getTrueCase().visit(this, arg);
        Type falseCase = (Type) conditionalExpr.getFalseCase().visit(this, arg);
        String name1 = conditionalExpr.getGuard().getFirstToken().getTokenString();
        String name2 = conditionalExpr.getTrueCase().getFirstToken().getTokenString();
        String name3 = conditionalExpr.getFalseCase().getFirstToken().getTokenString();
        checkName(name1);
        checkName(name2);
        checkName(name3);
        check(guard == Type.INT, conditionalExpr, "Expression guard is invalid type.");
        check(trueCase == falseCase, conditionalExpr, "trueCase != falseCase.");
        conditionalExpr.setType(trueCase);
        return trueCase;
    }

    private void hasName(String name, Expr initializer) throws TypeCheckException {
        if(initializer instanceof BinaryExpr)
        {
            String expr0 = ((BinaryExpr) initializer).getLeft().getFirstToken().getTokenString();
            String expr1 = ((BinaryExpr) initializer).getRight().getFirstToken().getTokenString();
            check(!expr0.equals(name), initializer,"cannot have same name in definition and declaration.");
            check(!expr1.equals(name), initializer,"cannot have same name in definition and declaration.");

        }
        else if(initializer instanceof StringLitExpr)
        {
            String n = ((StringLitExpr) initializer).getFirstToken().getTokenString();
            check(!n.equals(name), initializer,"cannot have same name in definition and declaration.");
        }
        else if(initializer instanceof IdentExpr)
        {
            String n = ((IdentExpr) initializer).getFirstToken().getTokenString();
            check(!n.equals(name), initializer,"cannot have same name in definition and declaration.");
        }
        else if(initializer instanceof UnaryExprPostfix)
        {
            String n = ((UnaryExprPostfix) initializer).getPixel().getFirstToken().getTokenString();
            check(!n.equals(name), initializer,"cannot have same name in definition and declaration.");
        }
    }

    private void checkName(String Name) throws TypeCheckException {
        if(inDeclaration) {
            check(!Name.equals(declarationName), this.rootProgram, "cannot have same name in definition and declaration.");
        }
    }
    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException{
        String name = declaration.getNameDef().getIdent().getName();
        declarationName = name;
        inDeclaration = true;
        NameDef nameD = declaration.getNameDef();
        Type nameDt = (Type) nameD.visit(this, arg);
        //boolean inserted = symbolTable.insert(name,declaration);
        //check(inserted, declaration, "variable " + name + " already declared"); //========= changed inserted to !inserted
        Expr initializer = declaration.getInitializer();
        if (initializer != null) {
            //infer type of initializer
            Type initializerType = (Type) initializer.visit(this,arg);
            check(assignmentCompatible(declaration.getNameDef().getType(), initializerType),declaration,
                    "type of expression and declared type do not match");
            //declaration.setAssigned(true);hasName(name, initializer);
        }
        if(nameDt == Type.IMAGE)
        {
            if(initializer == null && nameD.getDimension() == null)
            {
                check(false, nameD, "Image declaration incorrect");
            }
        }
        inDeclaration = false;
        return null;

    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException{
        Type height = (Type) dimension.getHeight().visit(this, arg);
        Type width = (Type) dimension.getWidth().visit(this, arg);
        check(height == Type.INT, dimension, "Expression height is invalid type.");
        check(width == Type.INT, dimension, "Expression width is invalid type.");
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException{
        Type red = (Type) expandedPixelExpr.getRedExpr().visit(this, arg);
        Type blue = (Type) expandedPixelExpr.getBluExpr().visit(this, arg);
        Type green = (Type) expandedPixelExpr.getGrnExpr().visit(this, arg);
        check(red == Type.INT, expandedPixelExpr, "Expression red is invalid type.");
        check(blue == Type.INT, expandedPixelExpr, "Expression blue is invalid type.");
        check(green == Type.INT, expandedPixelExpr, "Expression green is invalid type.");
        expandedPixelExpr.setType(Type.PIXEL);
        return Type.PIXEL;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException{
        return ident.getDef().getType();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException{
        String name = identExpr.getName();
        checkName(name);
        NameDef dec = (NameDef) symbolTable.lookup(name);
        check(dec != null, identExpr, "undefined identifier " + name);
        //check(dec.isAssigned(), identExpr, "using uninitialized variable");
        //identExpr.setDec(dec);  //save declaration--will be useful later.
        Type type = dec.getType();
        identExpr.setType(type);
        return type;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException{
        String name = lValue.getFirstToken().getTokenString();
        NameDef dec = (NameDef) symbolTable.lookup(name);
        check(dec != null, lValue, "undefined identifier " + name);
        Type identType = dec.getType(); //problem here
        PixelSelector pixelSelector = lValue.getPixelSelector();
        ColorChannel colorChannel = lValue.getColor();
        Type lValueType = null;
        switch (identType){
            case IMAGE -> {
                if(pixelSelector == null && colorChannel == null)
                {
                    lValueType = Type.IMAGE;
                }
                else if(pixelSelector != null && colorChannel == null)
                {
                    lValueType = Type.PIXEL;
                    Type pixelT = (Type) pixelSelector.visit(this, arg);

                }
                else if(pixelSelector == null && colorChannel != null)
                {
                    lValueType = Type.IMAGE;
                }
                else {
                    lValueType = Type.INT;
                    Type pixelT = (Type) pixelSelector.visit(this, arg);
                }
            }
            case INT -> {
                if(pixelSelector == null && colorChannel == null)
                {
                    lValueType = Type.INT;
                }
                else{
                    check(false, lValue, "Invalid integer identifier");
                }
            }
            case PIXEL -> {
                if(pixelSelector == null && colorChannel == null)
                {
                    lValueType = Type.PIXEL;
                }
                else if(pixelSelector == null && colorChannel != null){
                    lValueType = Type.INT;
                    //Type pixelT = (Type) pixelSelector.visit(this, arg);
                }
                else{
                    check(false, lValue, "Invalid pixel identifier");
                }
            }
            case STRING -> {
                if(pixelSelector == null && colorChannel == null)
                {
                    lValueType = Type.STRING;
                }
                else{
                    check(false, lValue, "Invalid String identifier");
                }
            }
            default -> {
                throw new TypeCheckException("Compiler Error");
            }
        }

        return lValueType;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException{
        Dimension dim = nameDef.getDimension();
        if(dim != null)
        {
            check(nameDef.getType()==Type.IMAGE, nameDef, "invalid type");
            Type dimT = (Type) dim.visit(this, arg);
        }
        //String name = nameDef.getFirstToken().getTokenString();
        String name = nameDef.getIdent().getName();
        NameDef dec = (NameDef) symbolTable.lookup(nameDef.getFirstToken().getTokenString());
        check(dec == null, nameDef, "variable already declared in scope");
        check(nameDef.getType() != Type.VOID, nameDef, "variable cannot be of type VOID");

        if(symbolTable.insert(name, nameDef) == false){
            throw new TypeCheckException("variable already declared");
        }
        return nameDef.getType();
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException{
        numLitExpr.setType(Type.INT);
        String name1 = numLitExpr.getFirstToken().getTokenString();
        checkName(name1);
        return Type.INT;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException{
        Type pixel = (Type) pixelFuncExpr.getSelector().visit(this, arg);
        pixelFuncExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException{
        String name1 = pixelSelector.getX().getFirstToken().getTokenString();
        String name2 = pixelSelector.getY().getFirstToken().getTokenString();
        checkName(name1);
        checkName(name2);
        Type x = (Type) pixelSelector.getX().visit(this, arg);
        Type y = (Type) pixelSelector.getY().visit(this, arg);
        check(x == Type.INT, pixelSelector, "Expression x is invalid type.");
        check(y == Type.INT, pixelSelector, "Expression y is invalid type.");
        //pixelSelector.getX().setType(Type.INT);
        //pixelSelector.getY().setType(Type.INT);
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException{
        predeclaredVarExpr.setType(Type.INT);
        String name1 = predeclaredVarExpr.getFirstToken().getTokenString();
        checkName(name1);
        return Type.INT;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException{
        //NEED TO ADD SCOPE STUFF
        rootProgram = program;
        symbolTable.enter();
        List<NameDef> params = program.getParamList();
        for (NameDef node :params) {
            node.visit(this, arg);
        }
        Type blockT = (Type) program.getBlock().visit(this,arg);
        return program.getType();
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException{
        randomExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg)throws PLCException{
        //Type programType = (Type) returnStatement.visit(this,arg); // ----------------------------this maybe wrong lol
        Type expressionType = (Type) returnStatement.getE().visit(this, arg);
        check(assignmentCompatible(rootProgram.getType(), expressionType),returnStatement, "expr and program type not compatible.");
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException{
        stringLitExpr.setType(Type.STRING);
        String name1 = stringLitExpr.getFirstToken().getTokenString();
        checkName(name1);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException{
        Type expr = (Type) unaryExpr.getE().visit(this, arg);
        //Type unary = (Type) unaryExpr.visit(this, arg);
        String name1 = unaryExpr.getE().getFirstToken().getTokenString();
        checkName(name1);
        IToken.Kind op = unaryExpr.getOp();
        Type resultType = null;
        switch (op){
            case BANG ->  {
                if(expr == Type.INT){
                    resultType = Type.INT;
                }
                else if(expr == Type.PIXEL)
                {
                    resultType = Type.PIXEL;
                }
                else{
                    check(false, unaryExpr, "does not match.");
                }
            }
            case MINUS, RES_cos, RES_atan, RES_sin -> {
                if(expr == Type.INT){
                    resultType = Type.INT;
                }
                else{
                    check(false, unaryExpr, "does not match");
                }
            }
            default -> {
                throw new TypeCheckException("Compiler Error");
            }
        }
        unaryExpr.setType(resultType);
        return resultType;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException{
        Type primary = (Type) unaryExprPostfix.getPrimary().visit(this, arg);
        PixelSelector pixelSelector = unaryExprPostfix.getPixel();
        if(pixelSelector != null){
            pixelSelector.getX().setType(Type.INT);
            pixelSelector.getY().setType(Type.INT);
        }
        ColorChannel colorChannel = unaryExprPostfix.getColor();
        Type upfType = null;
        switch (primary){
            case IMAGE -> {
                if(pixelSelector == null && colorChannel != null)
                {
                    upfType = Type.IMAGE;
                }
                else if(pixelSelector != null && colorChannel == null)
                {
                    if(pixelSelector.getX().getFirstToken().getKind() == IToken.Kind.STRING_LIT || pixelSelector.getY().getFirstToken().getKind() == IToken.Kind.STRING_LIT){
                        throw new TypeCheckException("Invalid pixel dimensions");
                    }
                    upfType = Type.PIXEL;
                    Type pixelT = (Type) pixelSelector.visit(this, arg);

                }
                else if(pixelSelector != null && colorChannel != null)
                {
                    upfType = Type.INT;
                    Type pixelT = (Type) pixelSelector.visit(this, arg);
                }
                else {
                    check(false, unaryExprPostfix, "Missing pixel selector or color channel");
                }
            }
            case PIXEL -> {
                if(pixelSelector == null && colorChannel != null)
                {
                    upfType = Type.INT;
                }
                else{
                    check(false, unaryExprPostfix, "Missing pixel selector or color channel");
                }
            }
            default -> {
                throw new TypeCheckException("Compiler Error");
            }
        }
        unaryExprPostfix.setType(upfType);
        return unaryExprPostfix.getType();
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException{
        Type exprType = (Type) whileStatement.getGuard().visit(this, arg);
        check(exprType == Type.INT, whileStatement, "expression must be of type INT");
        symbolTable.enter();
        /////////////////////////////////need to enter scope here
        Type blockType = (Type) whileStatement.getBlock().visit(this, arg);
        symbolTable.leave();
        ////////////////////////////////need to leave scope here
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException{ //---------------------Not finished
        Type expressionType = (Type) statementWrite.getE().visit(this, arg);
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException{
        zExpr.setType(Type.INT);
        return Type.INT;
    }
}