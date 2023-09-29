
package edu.ufl.cise.plcsp23;

public class StringLitToken implements IStringLitToken{

    public StringLitToken(Kind kind, String value, SourceLocation sourceLoc){
        this.kind = kind;
        this.sourceLoc = sourceLoc;
        this.value = String.valueOf(value.substring(1,value.length()-1));
        string = value;
    }
    private IToken.SourceLocation sourceLoc;
    private IToken.Kind kind;
    private String value;


    private String string;
    public String getValue()
    {
        if(value.equals("\\\""))
        {
            return "\"";
        }

        switch(value){
            case "\\n" -> { return "\n";}
            case "\\t" -> { return "\t";}
            case "\\b" -> { return "\b";}
            case "\\r" -> { return "\r";}
            case "\\\\" -> { return "\\\\";}
        }
        value = value.replaceAll("\\\"","\"");
        value = value.replaceAll("\\\\n","\n");
        value = value.replaceAll("\\\\t","\t");
        value = value.replaceAll("\\\\b","\b");
        value = value.replaceAll("\\\\r","\r");
        value = value.replaceAll("\\\\","\\\\");

        return String.valueOf(value);
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
        return string;
    }
}