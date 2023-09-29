package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;


import javax.naming.Name;
import java.util.*;
import java.io.Console;

public class GenCode implements ASTVisitor {

    Program rootProgram;
    String byteCode = "";
    String packageName;

    HashMap<String, Integer> scopeMap = new HashMap<>();

    public void fixScopes()
    {
        Iterator hashIter = scopeMap.entrySet().iterator();
        while(hashIter.hasNext()){
            Map.Entry ele = (Map.Entry)hashIter.next();
            int scope1 = (int)ele.getValue();
            if(scope1 == scope)
            {
                scope1 = scope-1;
                ele.setValue(scope1);
            }
        }
    }

    int scope = 0;

    boolean inWhile = false;
    boolean inReturn = false;

    Type currentType = null;

    boolean importWrite = false;
    boolean importRand = false;

    HashMap<String, Type> types = new HashMap<>();

    public void add(String name, Type type)
    {
        types.put(name, type);
    }
    public Type check(String name)
    {
        return(types.get(name));
    }
    public GenCode(String packageName){
        this.packageName = packageName;
    }

    public String typeString(Type type)
    {
        switch (type){
            case STRING -> {
                return "String";
            }
            case INT -> {
                return "int";
            }
            case IMAGE -> {
                return "image";
            }
            case PIXEL -> {
                return "pixel";
            }
            case VOID -> {
                return "void";
            }
        }
        return null;
    }
    public String convertOp(IToken.Kind op)
    {
        switch (op){
            case MINUS -> {return "-";}
            case PLUS -> {return "+";}
            case TIMES -> {return "*";}
            case DIV -> {return "/";}
            case MOD -> {return "%";}
            case LT -> {return "<";}
            case GT -> {return ">";}
            case LE -> {return "<=";}
            case GE -> {return ">=";}
            case EQ -> {return "==";}
            case EXP -> {return "**";}
            case BITAND -> {return "&";}
            case OR -> {return "||";}
            case AND -> {return "&&";}
            case BITOR -> {return "|";}
        }
        return "";
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        statementAssign.getLv().visit(this, arg);
        String name = statementAssign.getLv().getFirstToken().getTokenString();
        currentType = check(name);
        byteCode = byteCode.concat(" = ");
        if(check(name) == Type.STRING && statementAssign.getE().getType() != Type.STRING)
            byteCode = byteCode.concat("String.valueOf(");
        statementAssign.getE().visit(this,arg);
        if(check(name) == Type.STRING && statementAssign.getE().getType() != Type.STRING)
            byteCode = byteCode.concat(")");
        byteCode = byteCode.concat(";\n");
        currentType = null;
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException { //--------- not finished
        String op = convertOp(binaryExpr.getOp());
        if(op.equals("**"))
        {
            if(!importRand)
            {
                byteCode = "import java.lang.Math;\n" + byteCode;
                importRand = true;
            }
            byteCode = byteCode.concat("(int) Math.pow(");
            binaryExpr.getLeft().visit(this, arg);
            byteCode = byteCode.concat(", ");
            binaryExpr.getRight().visit(this,arg);
            byteCode = byteCode.concat(")");
        }
        else if(op.equals("&&"))
        {
            if(binaryExpr.getLeft() instanceof IdentExpr) {
                byteCode = byteCode.concat("(((");
                binaryExpr.getLeft().visit(this, arg);
                byteCode = byteCode.concat("!= 0) ? true : false) ");
            }
            else
            {
                binaryExpr.getLeft().visit(this, arg);
            }
            byteCode = byteCode.concat(" " + op + " ");
            if(binaryExpr.getLeft() instanceof IdentExpr) {
                byteCode = byteCode.concat(" ((");
                binaryExpr.getRight().visit(this, arg);
                byteCode = byteCode.concat("!= 0) ? true : false)) ? 1 : 0");
            }
            else
            {
                binaryExpr.getRight().visit(this, arg);
            }
        }
        else if( op.equals("||"))
        {
            byteCode = byteCode.concat("(");
            if(binaryExpr.getLeft() instanceof IdentExpr) {
                byteCode = byteCode.concat("(((");
                binaryExpr.getLeft().visit(this, arg);
                byteCode = byteCode.concat("!= 0) ? true : false) ");
            }
            else
            {
                binaryExpr.getLeft().visit(this, arg);
            }
            byteCode = byteCode.concat(")");
            byteCode = byteCode.concat(" " + op + " ");
            byteCode = byteCode.concat("(");
            if(binaryExpr.getLeft() instanceof IdentExpr) {
                byteCode = byteCode.concat(" ((");
                binaryExpr.getRight().visit(this, arg);
                byteCode = byteCode.concat("!= 0) ? true : false)) ? 1 : 0");
            }
            else
            {
                binaryExpr.getRight().visit(this, arg);
            }
            byteCode = byteCode.concat(")");
        }
        else {
            byteCode = byteCode.concat("(");
            binaryExpr.getLeft().visit(this, arg);
            byteCode = byteCode.concat(" " + op + " ");
            if(op.equals(">") || op.equals(">=") || op.equals("<") || op.equals("<=") || op.equals("==")){
                if(binaryExpr.getLeft() instanceof BinaryExpr left)
                {
                    String op2 = convertOp(left.getOp());
                    if(op2.equals(">") || op2.equals(">=") || op2.equals("<") || op2.equals("<=") || op2.equals("==") || op.equals("!="))
                    {
                        byteCode = byteCode.concat(" ? 1 : 0");
                    }
                }
            }
            binaryExpr.getRight().visit(this, arg);
            if(op.equals(">") || op.equals(">=") || op.equals("<") || op.equals("<=") || op.equals("==") || op.equals("!="))
            {
                if(currentType != null) {
                    if (currentType == Type.INT) {
                        byteCode = byteCode.concat(" ? 1 : 0");
                    }
                }
                if(inReturn)
                {
                    if(binaryExpr.getLeft().getType() == Type.STRING && rootProgram.getType() == Type.INT)
                    {
                        byteCode = byteCode.concat(" ? 1 : 0");
                    }
                    else if(binaryExpr.getLeft().getType() == Type.PIXEL && rootProgram.getType() == Type.INT)
                    {
                        byteCode = byteCode.concat(" ? 1 : 0");
                    }
                    else if(binaryExpr.getLeft().getType() == Type.IMAGE && rootProgram.getType() == Type.INT)
                    {
                        byteCode = byteCode.concat(" ? 1 : 0");
                    }
                    else if(binaryExpr.getLeft().getType() == Type.INT && rootProgram.getType() == Type.INT)
                    {
                        byteCode = byteCode.concat(" ? 1 : 0");
                    }
                }
            }
            byteCode = byteCode.concat(")");
        }
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        List<Declaration> declarations = block.getDecList();
        List<Statement> statements = block.getStatementList();
        for(Declaration nodes : declarations){
            nodes.visit(this, arg);
        }
        for(Statement nodes : statements){
            nodes.visit(this, arg);
        }
        return byteCode;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException { //--------- not finished
        byteCode = byteCode.concat("(");
        String conditional = "";
        String guard = (String) conditionalExpr.getGuard().visit(this, arg);
        if(conditionalExpr.getGuard().getType() == Type.INT && !(conditionalExpr.getGuard() instanceof BinaryExpr))
        {
            if(rootProgram.getType() == Type.INT) {
                byteCode = byteCode.concat("!= 0 ? true : false ");
            }
        }
        if(conditionalExpr.getGuard() instanceof NumLitExpr || conditionalExpr.getGuard() instanceof ZExpr || (conditionalExpr.getGuard() instanceof  BinaryExpr e && (e.getOp() == IToken.Kind.EXP)))
        {
            byteCode = byteCode.concat("!= 0 ? true : false ");
        }

        byteCode = byteCode.concat(") ? ");
        String trueC = (String) conditionalExpr.getTrueCase().visit(this, arg);
        byteCode = byteCode.concat(" : ");
        String falseC = (String) conditionalExpr.getFalseCase().visit(this, arg);

        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {

        currentType = declaration.getNameDef().getType();
        NameDef name = declaration.getNameDef();
        add(name.getIdent().getName(), name.getType());
        name.visit(this, arg);
        if(declaration.getInitializer() != null){
            byteCode = byteCode.concat(" = ");
            if(check(name.getIdent().getName()) == Type.STRING && declaration.getInitializer().getType() != Type.STRING)
                byteCode = byteCode.concat("String.valueOf(");
            declaration.getInitializer().visit(this, arg);
            if(check(name.getIdent().getName()) == Type.STRING && declaration.getInitializer().getType() != Type.STRING)
                byteCode = byteCode.concat(")");
        }
        byteCode = byteCode.concat(";\n");
        currentType = null;
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException { //Function might be unnecessary
        byteCode = byteCode.concat(ident.getName() + "_" + scopeMap.get(ident.getName()));
        return ident.getName();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        byteCode = byteCode.concat(identExpr.getName() + "_" + scopeMap.get(identExpr.getName()));
        return identExpr.getName();
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        lValue.getIdent().visit(this, arg);
        return lValue.getIdent().getName();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        byteCode = byteCode.concat(typeString(nameDef.getType()) + " ");
        scopeMap.put(nameDef.getIdent().getName(),scope);
        nameDef.getIdent().visit(this, arg);
        /*if(nameDef.getDimension() != null){  --------------------- Documentation said not to implement dimensions for 5
            nameDef.getDimension().visit(this, arg);
        }*/
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        byteCode = byteCode.concat(String.valueOf(numLitExpr.getValue()));
        return String.valueOf(numLitExpr.getValue());
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        rootProgram = program;
        byteCode = byteCode.concat(packageName);
        if(!packageName.equals(""))
            byteCode = byteCode.concat("\n");
        byteCode = byteCode.concat("public class " + program.getIdent().getName() + "{\n");
        byteCode = byteCode.concat("public static " + typeString(program.getType()));

        byteCode = byteCode.concat(" apply(");
        List<NameDef> params = program.getParamList();
        boolean toDelete = false;
        for (NameDef node :params) {
            node.visit(this, arg);
            byteCode = byteCode.concat(", ");
            toDelete = true;
        }
        if(toDelete)
            byteCode = byteCode.substring(0, Math.max(byteCode.length() - 2, 0));
        byteCode = byteCode.concat("){\n");
        program.getBlock().visit(this,arg);
        byteCode = byteCode.concat("}\n}");
        return byteCode;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException { //------- need to add import statement somehow
        if(!importRand)
        {
            byteCode = "import java.lang.Math;\n" + byteCode;
            importRand = true;
        }
        byteCode = byteCode.concat("(int) Math.floor(Math.random() * 256)");
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        inReturn = true;
        byteCode = byteCode.concat("return (");
        returnStatement.getE().visit(this, arg);
        byteCode = byteCode.concat(");");
        inReturn = false;
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        byteCode = byteCode.concat("\"" + stringLitExpr.getValue() + "\"");
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException { //--------- not finished
        inWhile = true;
        byteCode = byteCode.concat("while(");
        whileStatement.getGuard().visit(this,arg);
        if(whileStatement.getGuard() instanceof IdentExpr ident)
        {
            if(ident.getType() == Type.INT) {
                byteCode = byteCode.concat("!= 0 ? true : false");
            }
        }
        if(whileStatement.getGuard() instanceof BinaryExpr ident)
        {
            if(ident.getOp() == IToken.Kind.LT || ident.getOp() == IToken.Kind.EQ || ident.getOp() == IToken.Kind.LE || ident.getOp() == IToken.Kind.GE || ident.getOp() == IToken.Kind.GT || ident.getOp() == IToken.Kind.AND || ident.getOp() == IToken.Kind.OR) {
            }
            else{
                byteCode = byteCode.concat(" != 0 ? true : false");
            }
        }
        scope++;
        byteCode = byteCode.concat("){\n");
        inWhile = false;
        whileStatement.getBlock().visit(this,arg);
        byteCode = byteCode.concat("}\n");
        fixScopes();
        scope--;
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        if(!importWrite) {
            byteCode = "import edu.ufl.cise.plcsp23.runtime.ConsoleIO;\n" + byteCode;
            importWrite = true;
        }
        byteCode = byteCode.concat("ConsoleIO.write(");
        statementWrite.getE().visit(this, arg);
        byteCode = byteCode.concat(");\n");
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        byteCode = byteCode.concat(String.valueOf(zExpr.getValue()));
        return String.valueOf(zExpr.getValue());
    }
}