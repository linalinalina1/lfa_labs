package lab6;

import java.util.regex.Pattern;

public enum TokenType {
    NUMBER("\\d+(\\.\\d+)?"),
    IDENTIFIER("[a-zA-Z_][a-zA-Z0-9_]*"),

    SIN("sin"),
    COS("cos"),
    TAN("tan"),
    SQRT("sqrt"),
    LOG("log"),

    PLUS("\\+"),
    MINUS("-"),
    MUL("\\*"),
    DIV("/"),
    MOD("%"),
    POW("\\^"),
    ASSIGN("="),

    LPAREN("\\("),
    RPAREN("\\)"),
    COMMA(","),
    SEMICOLON(";"),

    EOF("$");

    private final Pattern pattern;

    TokenType(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Pattern getPattern() {
        return pattern;
    }
}