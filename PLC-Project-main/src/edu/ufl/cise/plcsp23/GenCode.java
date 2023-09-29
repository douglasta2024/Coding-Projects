package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;


import javax.naming.Name;
import java.awt.image.BufferedImage;
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
    boolean importFile = false;
    boolean importImage = false;
    boolean importPixel = false;
    boolean importBuffImage = false;

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
            case BANG -> {return "!";}
        }
        return "";
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        String name = statementAssign.getLv().getFirstToken().getTokenString();
        currentType = check(name);
        if(check(name) == Type.IMAGE)
        {
            if(statementAssign.getLv().getPixelSelector() == null && statementAssign.getLv().getColor() == null){
                if(statementAssign.getE().getType() == Type.STRING)
                {
                    if(!importImage) {
                        byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                        importImage = true;
                    }
                    if(!importFile) {
                        byteCode = "import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n" + byteCode;
                        importFile = true;
                    }
                    byteCode = byteCode.concat("ImageOps.copyInto(FileURLIO.readImage(");
                    statementAssign.getE().visit(this, arg);
                    byteCode = byteCode.concat("), ");
                    statementAssign.getLv().visit(this, arg);
                    byteCode = byteCode.concat(")");
                }
                else if(statementAssign.getE().getType() == Type.IMAGE)
                {
                    //ImageOps.copyInto(sourceImage, expected);
                    if(!importImage) {
                        byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                        importImage = true;
                    }
                    byteCode = byteCode.concat("ImageOps.copyInto(");
                    statementAssign.getE().visit(this, arg);
                    byteCode = byteCode.concat(", ");
                    statementAssign.getLv().visit(this, arg);
                    byteCode = byteCode.concat(")");
                }
                else if(statementAssign.getE().getType() == Type.PIXEL)
                {
                    if(!importImage) {
                        byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                        importImage = true;
                    }
                    //ImageOps.setAllPixels(kk, PixelOps.pack(255, 0, 255));
                    byteCode = byteCode.concat("ImageOps.setAllPixels(");
                    statementAssign.getLv().visit(this, arg);
                    byteCode = byteCode.concat(", ");
                    statementAssign.getE().visit(this, arg);
                    byteCode = byteCode.concat(")");
                }
            }
            else if(statementAssign.getLv().getPixelSelector() != null && statementAssign.getLv().getColor() == null){
                if(!importImage) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                    importImage = true;
                }
                byteCode = byteCode.concat("for (int y = 0; y != ");
                statementAssign.getLv().visit(this, arg);
                byteCode = byteCode.concat(".getHeight(); y++){\n");
                byteCode = byteCode.concat("for (int x = 0; x != ");
                statementAssign.getLv().visit(this, arg);
                byteCode = byteCode.concat(".getWidth(); x++){\n");
                byteCode = byteCode.concat("ImageOps.setRGB(");
                statementAssign.getLv().visit(this, arg);
                byteCode = byteCode.concat(", ");
                statementAssign.getLv().getPixelSelector().visit(this, arg);
                byteCode = byteCode.concat(", ");
                statementAssign.getE().visit(this, arg);

                byteCode = byteCode.concat(");\n");
                byteCode = byteCode.concat("}\n}\n");
                currentType = null;
                return null;

            }
            else if(statementAssign.getLv().getPixelSelector() != null && statementAssign.getLv().getColor() != null){
                if(!importImage) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                    importImage = true;
                }
                if(!importPixel) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.PixelOps;\n" + byteCode;
                    importPixel = true;
                }
                byteCode = byteCode.concat("for (int y = 0; y != ");
                statementAssign.getLv().visit(this, arg);
                byteCode = byteCode.concat(".getHeight(); y++){\n");
                byteCode = byteCode.concat("for (int x = 0; x != ");
                statementAssign.getLv().visit(this, arg);
                byteCode = byteCode.concat(".getWidth(); x++){\n");
                byteCode = byteCode.concat("ImageOps.setRGB(");
                statementAssign.getLv().visit(this, arg);
                byteCode = byteCode.concat(", ");
                statementAssign.getLv().getPixelSelector().visit(this, arg);
                byteCode = byteCode.concat(", ");
                byteCode = byteCode.concat("PixelOps.set");
                if(statementAssign.getLv().getColor() == ColorChannel.red)
                    byteCode = byteCode.concat("Red");
                else if(statementAssign.getLv().getColor() == ColorChannel.grn)
                    byteCode = byteCode.concat("Grn");
                else if(statementAssign.getLv().getColor() == ColorChannel.blu)
                    byteCode = byteCode.concat("Blu");
                byteCode = byteCode.concat("(");
                byteCode = byteCode.concat("ImageOps.getRGB(");
                statementAssign.getLv().visit(this, arg);
                byteCode = byteCode.concat(", ");
                statementAssign.getLv().getPixelSelector().visit(this, arg);
                byteCode = byteCode.concat("), ");
                statementAssign.getE().visit(this, arg);
                byteCode = byteCode.concat("));\n");
                byteCode = byteCode.concat("}\n}\n");
                currentType = null;
                return null;


            }
        }
        else {
            statementAssign.getLv().visit(this, arg);
            byteCode = byteCode.concat(" = ");
            if (check(name) == Type.STRING && statementAssign.getE().getType() != Type.STRING)
                byteCode = byteCode.concat("String.valueOf(");
            statementAssign.getE().visit(this, arg);
            if (check(name) == Type.STRING && statementAssign.getE().getType() != Type.STRING)
                byteCode = byteCode.concat(")");
        }
        byteCode = byteCode.concat(";\n");
        currentType = null;
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException { /////////////NEEED TO DO
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
                if(currentType == null && ((BinaryExpr) binaryExpr).getOp().name().equals("AND")){
                    byteCode = byteCode.concat("!= 0) ? true : false))");
                }
                else{
                    byteCode = byteCode.concat("!= 0) ? true : false)) ? 1 : 0");
                }
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
                byteCode = byteCode.concat("(");
                binaryExpr.getLeft().visit(this, arg);
                if(currentType == Type.STRING || currentType == Type.IMAGE && ((BinaryExpr) binaryExpr).getOp().name().equals("OR")){

                }
                else{
                    byteCode = byteCode.concat("!=0 ? true : false");
                }
            }
            byteCode = byteCode.concat(")");
            byteCode = byteCode.concat(" " + op + " ");
            byteCode = byteCode.concat("(");
            if(binaryExpr.getLeft() instanceof IdentExpr) {
                byteCode = byteCode.concat(" ((");
                binaryExpr.getRight().visit(this, arg);
                byteCode = byteCode.concat("!= 0) ? true : false)) ? 1 : 0)");
            }
            else
            {
                binaryExpr.getRight().visit(this, arg);
                if((currentType == Type.STRING || currentType == Type.IMAGE) && (((BinaryExpr) binaryExpr).getOp().name().equals("OR") || ((BinaryExpr) binaryExpr).getOp().name().equals("AND"))){

                }
                else{
                    byteCode = byteCode.concat("!=0 ? true : false");
                }
                byteCode = byteCode.concat(" ) ? 1 : 0)");
            }
        }
        else if(op.equals("+") || op.equals("%") || op.equals("-") || op.equals("/") || op.equals("*")){
            String op_name = "";
            switch (op){
                case "+": op_name = "ImageOps.OP.PLUS";
                    break;
                case "-": op_name = "ImageOps.OP.MINUS";
                    break;
                case "/": op_name = "ImageOps.OP.DIV";
                    break;
                case "%": op_name = "ImageOps.OP.MOD";
                    break;
                case "*": op_name = "ImageOps.OP.TIMES";
                    break;
            }
            byteCode = byteCode.concat("(");
            if(binaryExpr.getLeft().getType() == Type.IMAGE && binaryExpr.getRight().getType() == Type.IMAGE){
                byteCode = byteCode.concat("ImageOps.binaryImageImageOp(" + op_name + ", ");
                binaryExpr.getLeft().visit(this, arg);
                byteCode = byteCode.concat(", ");
                binaryExpr.getRight().visit(this, arg);
                byteCode = byteCode.concat(")");
            }
            else if(binaryExpr.getLeft().getType() == Type.IMAGE && binaryExpr.getRight().getType() == Type.INT){
                byteCode = byteCode.concat("ImageOps.binaryImageScalarOp(" + op_name + ", ");
                binaryExpr.getLeft().visit(this, arg);
                byteCode = byteCode.concat(", ");
                binaryExpr.getRight().visit(this, arg);
                byteCode = byteCode.concat(")");
            }
            else if(binaryExpr.getLeft().getType() == Type.PIXEL && binaryExpr.getRight().getType() == Type.PIXEL){
                byteCode = byteCode.concat("ImageOps.binaryPackedPixelPixelOp(" + op_name + ", ");
                binaryExpr.getLeft().visit(this, arg);
                byteCode = byteCode.concat(", ");
                binaryExpr.getRight().visit(this, arg);
                byteCode = byteCode.concat(")");
            }
            else if(binaryExpr.getLeft().getType() == Type.PIXEL && binaryExpr.getRight().getType() == Type.INT){
                byteCode = byteCode.concat("ImageOps.binaryPackedPixelIntOp(" + op_name + ", ");
                binaryExpr.getLeft().visit(this, arg);
                byteCode = byteCode.concat(", ");
                binaryExpr.getRight().visit(this, arg);
                byteCode = byteCode.concat(")");
            }
            else{ //-----------------------------------------------------regular +,-,/,%,* operations
                binaryExpr.getLeft().visit(this, arg);
                byteCode = byteCode.concat(" " + op + " ");
                binaryExpr.getRight().visit(this, arg);
            }
            byteCode = byteCode.concat(")");
        }
        else if((op.equals("==") || op.equals("!=")) && binaryExpr.getLeft().getType() == Type.IMAGE && binaryExpr.getRight().getType() == Type.IMAGE){
            byteCode = byteCode.concat("ImageOps.equalsForCodeGen(");
            binaryExpr.getLeft().visit(this, arg);
            byteCode = byteCode.concat(", ");
            binaryExpr.getRight().visit(this, arg);
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
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
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
        if(conditionalExpr.getGuard() instanceof BinaryExpr && ((BinaryExpr) conditionalExpr.getGuard()).getOp().name().equals("OR")){
            byteCode = byteCode.concat("!= 0 ? true : false ");
        }
        /*if(conditionalExpr.getGuard() instanceof BinaryExpr && ((BinaryExpr) conditionalExpr.getGuard()).getOp().name().equals("AND")){ --------------------- NEED TO CHANGE FOR &&
            byteCode = byteCode.concat("!= 0 ? true : false ");
        }*/
        byteCode = byteCode.concat(") ? ");
        String trueC = (String) conditionalExpr.getTrueCase().visit(this, arg);
        byteCode = byteCode.concat(" : ");
        String falseC = (String) conditionalExpr.getFalseCase().visit(this, arg);

        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException { //NEED TO DO BUT LET ME FINISH IT

        currentType = declaration.getNameDef().getType();
        NameDef name = declaration.getNameDef();
        add(name.getIdent().getName(), name.getType());
        name.visit(this, arg);
        if(!importImage) {
            byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
            importImage = true;
        }
        if(name.getDimension() == null && currentType == Type.IMAGE && declaration.getInitializer() != null)
        {
            byteCode = byteCode.concat(" = ");
            Type initType = declaration.getInitializer().getType();
            if(initType == Type.STRING)
            {
                if(!importFile) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n" + byteCode;
                    importFile = true;
                }
                if(!importImage) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                    importImage = true;
                }
                byteCode = byteCode.concat("FileURLIO.readImage(");
                declaration.getInitializer().visit(this, arg);
                byteCode = byteCode.concat(");\n");
            }
            if(initType == Type.IMAGE)
            {
                /*if(!importImage) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                    importImage = true;
                }*/
                byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                importImage = true;

                byteCode = byteCode.concat("ImageOps.cloneImage(");
                declaration.getInitializer().visit(this, arg);
                byteCode = byteCode.concat(");\n");
            }
            currentType = null;
            return null;
        }
        else if(name.getDimension() != null)
        {
            if (declaration.getInitializer() != null && (declaration.getInitializer().getType() == Type.STRING || declaration.getInitializer().getType() == Type.IMAGE)) {
                byteCode = byteCode.concat(" = ");
                Type initType = declaration.getInitializer().getType();
                if(initType == Type.STRING)
                {
                    if(!importFile) {
                        byteCode = "import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n" + byteCode;
                        importFile = true;
                    }
                    //BufferedImage k = FileURLIO.readImage(s, 200, 50);
                    byteCode = byteCode.concat("FileURLIO.readImage(");
                    declaration.getInitializer().visit(this, arg);
                    byteCode = byteCode.concat(",");
                    name.getDimension().getWidth().visit(this, arg);
                    byteCode = byteCode.concat(",");
                    name.getDimension().getHeight().visit(this, arg);
                    byteCode = byteCode.concat(");\n");
                }
                else if(initType == Type.IMAGE)
                {
                    //BufferedImage kk = ImageOps.copyAndResize(k, 100, 200);
                    if(!importImage) {
                        byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                        importImage = true;
                    }
                    byteCode = byteCode.concat("ImageOps.copyAndResize(");
                    declaration.getInitializer().visit(this, arg);
                    byteCode = byteCode.concat(",");
                    name.getDimension().getWidth().visit(this, arg);
                    byteCode = byteCode.concat(",");
                    name.getDimension().getHeight().visit(this, arg);
                    byteCode = byteCode.concat(");\n");
                }
            }
            else{
                if(!importImage) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                    importImage = true;
                }
                byteCode = byteCode.concat(" = ( ImageOps.makeImage(");
                name.getDimension().getWidth().visit(this, arg);
                byteCode = byteCode.concat(",");
                name.getDimension().getHeight().visit(this, arg);
                byteCode = byteCode.concat("));\n");
                if(declaration.getInitializer() != null)
                {
                    name.getIdent().visit(this,arg);
                    byteCode = byteCode.concat(" = ImageOps.setAllPixels(");
                    name.getIdent().visit(this,arg);
                    byteCode = byteCode.concat(", ");
                    declaration.getInitializer().visit(this, arg);
                    byteCode = byteCode.concat(");\n");
                }
            }
            currentType = null;
            return null;
        }
        if(declaration.getInitializer() != null && currentType != Type.IMAGE){
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
        byteCode = byteCode.concat("[");
        dimension.getWidth().visit(this, arg);
        byteCode = byteCode.concat(", ");
        dimension.getHeight().visit(this, arg);
        byteCode = byteCode.concat("]");
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        if(!importPixel) {
            byteCode = "import edu.ufl.cise.plcsp23.runtime.PixelOps;\n" + byteCode;
            importPixel = true;
        }
        byteCode = byteCode.concat("PixelOps.pack(");
        expandedPixelExpr.getRedExpr().visit(this, arg);
        byteCode = byteCode.concat(", ");
        expandedPixelExpr.getGrnExpr().visit(this, arg);
        byteCode = byteCode.concat(", ");
        expandedPixelExpr.getBluExpr().visit(this, arg);
        byteCode = byteCode.concat(")");
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
        String type = typeString(nameDef.getType());
        if(type.equals("pixel"))
        {
            type = "int";
        }
        else if (type.equals("image"))
        {
            if(!importBuffImage) {
                byteCode = "import java.awt.image.BufferedImage;\n" + byteCode;
                importBuffImage = true;
            }
            type = "BufferedImage";
        }
        byteCode = byteCode.concat(type + " ");
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
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException { //DONT DO
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        pixelSelector.getX().visit(this, arg);
        byteCode = byteCode.concat(", ");
        pixelSelector.getY().visit(this, arg);
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        if(predeclaredVarExpr.getKind() == IToken.Kind.RES_x){
            byteCode = byteCode.concat("x");
        }
        else if(predeclaredVarExpr.getKind() == IToken.Kind.RES_y){
            byteCode = byteCode.concat("y");
        }
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        rootProgram = program;
        byteCode = byteCode.concat(packageName);
        if(!packageName.equals(""))
            byteCode = byteCode.concat("\n");
        byteCode = byteCode.concat("public class " + program.getIdent().getName() + "{\n");
        byteCode = byteCode.concat("public static ");
        String pType = typeString(program.getType());
        if(pType.equals("image")) {
            if(!importBuffImage) {
                byteCode = "import java.awt.image.BufferedImage;\n" + byteCode;
                importBuffImage = true;
            }
            pType = "BufferedImage";
        }
        else if(pType.equals("pixel"))
            pType = "int";
        byteCode = byteCode.concat(pType);

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
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        if(!importRand)
        {
            byteCode = "import java.lang.Math;\n" + byteCode;
            importRand = true;
        }
        byteCode = byteCode.concat("(int) Math.floor(Math.random() * 256)");
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException { //////////NEED DO
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
        String op = convertOp(unaryExpr.getOp());
        if(op.equals("!"))
        {
            byteCode = byteCode.concat("((");
            unaryExpr.getE().visit(this, arg);
            byteCode = byteCode.concat(")==0 ? 1 : 0)");
        }
        else if(op.equals("-"))
        {
            byteCode = byteCode.concat("-(");
            unaryExpr.getE().visit(this, arg);
            byteCode = byteCode.concat(")");
        }
        return null;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        if(unaryExprPostfix.getPrimary().getType() == Type.IMAGE)
        {
            if(unaryExprPostfix.getPixel() != null && unaryExprPostfix.getColor() == null)
            {
                if(!importImage) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                    importImage = true;
                }
                byteCode = byteCode.concat("ImageOps.getRGB(");
                unaryExprPostfix.getPrimary().visit(this, arg);
                byteCode = byteCode.concat(", ");
                unaryExprPostfix.getPixel().getX().visit(this, arg);
                byteCode = byteCode.concat(", ");
                unaryExprPostfix.getPixel().getY().visit(this, arg);
                byteCode = byteCode.concat(")");
            }
            else if(unaryExprPostfix.getPixel() == null && unaryExprPostfix.getColor() != null)
            {
                if(!importImage) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                    importImage = true;
                }
                byteCode = byteCode.concat("ImageOps.extract");
                if(unaryExprPostfix.getColor() == ColorChannel.red)
                    byteCode = byteCode.concat("Red(");
                else if(unaryExprPostfix.getColor() == ColorChannel.grn)
                    byteCode = byteCode.concat("Grn(");
                else if(unaryExprPostfix.getColor() == ColorChannel.blu)
                    byteCode = byteCode.concat("Blu(");
                unaryExprPostfix.getPrimary().visit(this, arg);
                byteCode = byteCode.concat(")");
            }
            else if(unaryExprPostfix.getPixel() != null && unaryExprPostfix.getColor() != null)
            {
                if(!importImage) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n" + byteCode;
                    importImage = true;
                }
                if(!importPixel) {
                    byteCode = "import edu.ufl.cise.plcsp23.runtime.PixelOps;\n" + byteCode;
                    importPixel = true;
                }
                byteCode = byteCode.concat("PixelOps.");
                if(unaryExprPostfix.getColor() == ColorChannel.red)
                    byteCode = byteCode.concat("red(");
                else if(unaryExprPostfix.getColor() == ColorChannel.grn)
                    byteCode = byteCode.concat("grn(");
                else if(unaryExprPostfix.getColor() == ColorChannel.blu)
                    byteCode = byteCode.concat("blu(");
                byteCode = byteCode.concat("ImageOps.getRGB(");
                unaryExprPostfix.getPrimary().visit(this, arg);
                byteCode = byteCode.concat(", ");
                unaryExprPostfix.getPixel().getX().visit(this, arg);
                byteCode = byteCode.concat(", ");
                unaryExprPostfix.getPixel().getY().visit(this, arg);
                byteCode = byteCode.concat("))");
            }
        }
        else if(unaryExprPostfix.getPrimary().getType() == Type.PIXEL){
            if(!importPixel) {
                byteCode = "import edu.ufl.cise.plcsp23.runtime.PixelOps;\n" + byteCode;
                importPixel = true;
            }
            byteCode = byteCode.concat("PixelOps.");
            if(unaryExprPostfix.getColor() == ColorChannel.red)
                byteCode = byteCode.concat("red(");
            else if(unaryExprPostfix.getColor() == ColorChannel.grn)
                byteCode = byteCode.concat("grn(");
            else if(unaryExprPostfix.getColor() == ColorChannel.blu)
                byteCode = byteCode.concat("blu(");
            unaryExprPostfix.getPrimary().visit(this, arg);
            byteCode = byteCode.concat(")");
        }
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
        if(statementWrite.getE().getType() == Type.PIXEL)
        {
            byteCode = byteCode.concat("ConsoleIO.writePixel(");
        }
        else {
            byteCode = byteCode.concat("ConsoleIO.write(");
        }
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