package edu.ufl.cise.plcsp23;

public class Token implements IToken{
    private SourceLocation sourceLoc;
    private Kind kind;
    private String token;

    public Token(Kind kind, String token, SourceLocation sourceLoc){
        super();
        this.kind = kind;
        this.sourceLoc = sourceLoc;
        this.token = token;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLoc;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getTokenString() {
        return token;
    }
}
