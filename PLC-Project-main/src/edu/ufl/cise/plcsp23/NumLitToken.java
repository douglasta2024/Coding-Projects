package edu.ufl.cise.plcsp23;

public class NumLitToken implements INumLitToken{
    /* TODO:
            Add Constructor
            Add tests
     */

    private SourceLocation sourceLoc;
    private Kind kind;
    private int value;

    public NumLitToken(Kind kind, int value, SourceLocation sourceLoc){
        super();
        this.kind = kind;
        this.sourceLoc = sourceLoc;
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
    public SourceLocation getSourceLocation()
    {
        return sourceLoc;
    }
    public Kind getKind()
    {
        return kind;
    }
    public String getTokenString() {
        return String.valueOf(value);
    }
}