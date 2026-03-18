package lab3;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos;
    private int line;
    private int column;
    private char currentChar;

    public Lexer(String input) {
        this.input = input == null ? "" : input;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.currentChar = this.input.isEmpty() ? '\0' : this.input.charAt(0);
    }

    private void advance() {
        if (currentChar == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        pos++;

        if (pos >= input.length()) {
            currentChar = '\0';
        } else {
            currentChar = input.charAt(pos);
        }
    }

    private char peek() {
        int nextPos = pos + 1;
        if (nextPos >= input.length()) {
            return '\0';
        }
        return input.charAt(nextPos);
    }

    private void skipWhitespace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    private void skipComment() {
        while (currentChar != '\0' && currentChar != '\n') {
            advance();
        }
    }

    private Token number() {
        int startPos = pos;
        int startLine = line;
        int startColumn = column;

        StringBuilder sb = new StringBuilder();
        boolean hasDot = false;

        while (currentChar != '\0' && (Character.isDigit(currentChar) || currentChar == '.')) {
            if (currentChar == '.') {
                if (hasDot) {
                    throw error("Invalid number format");
                }
                hasDot = true;
            }

            sb.append(currentChar);
            advance();
        }

        String lexeme = sb.toString();

        if (lexeme.equals(".")) {
            throw error("Invalid standalone '.' in number");
        }

        return new Token(TokenType.NUMBER, lexeme, startPos, startLine, startColumn);
    }

    private Token identifierOrKeyword() {
        int startPos = pos;
        int startLine = line;
        int startColumn = column;

        StringBuilder sb = new StringBuilder();

        while (currentChar != '\0'
                && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            sb.append(currentChar);
            advance();
        }

        String word = sb.toString();

        return switch (word) {
            case "sin" -> new Token(TokenType.SIN, word, startPos, startLine, startColumn);
            case "cos" -> new Token(TokenType.COS, word, startPos, startLine, startColumn);
            case "tan" -> new Token(TokenType.TAN, word, startPos, startLine, startColumn);
            case "sqrt" -> new Token(TokenType.SQRT, word, startPos, startLine, startColumn);
            case "log" -> new Token(TokenType.LOG, word, startPos, startLine, startColumn);
            default -> new Token(TokenType.IDENTIFIER, word, startPos, startLine, startColumn);
        };
    }

    private RuntimeException error(String message) {
        return new RuntimeException(
                message + " at line " + line + ", column " + column + " (position " + pos + ")"
        );
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (currentChar != '\0') {
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }

            if (currentChar == '#') {
                skipComment();
                continue;
            }

            if (Character.isDigit(currentChar)
                    || (currentChar == '.' && Character.isDigit(peek()))) {
                tokens.add(number());
                continue;
            }

            if (Character.isLetter(currentChar) || currentChar == '_') {
                tokens.add(identifierOrKeyword());
                continue;
            }

            int startPos = pos;
            int startLine = line;
            int startColumn = column;

            switch (currentChar) {
                case '+' -> {
                    tokens.add(new Token(TokenType.PLUS, "+", startPos, startLine, startColumn));
                    advance();
                }
                case '-' -> {
                    tokens.add(new Token(TokenType.MINUS, "-", startPos, startLine, startColumn));
                    advance();
                }
                case '*' -> {
                    tokens.add(new Token(TokenType.MUL, "*", startPos, startLine, startColumn));
                    advance();
                }
                case '/' -> {
                    tokens.add(new Token(TokenType.DIV, "/", startPos, startLine, startColumn));
                    advance();
                }
                case '%' -> {
                    tokens.add(new Token(TokenType.MOD, "%", startPos, startLine, startColumn));
                    advance();
                }
                case '^' -> {
                    tokens.add(new Token(TokenType.POW, "^", startPos, startLine, startColumn));
                    advance();
                }
                case '=' -> {
                    tokens.add(new Token(TokenType.ASSIGN, "=", startPos, startLine, startColumn));
                    advance();
                }
                case '(' -> {
                    tokens.add(new Token(TokenType.LPAREN, "(", startPos, startLine, startColumn));
                    advance();
                }
                case ')' -> {
                    tokens.add(new Token(TokenType.RPAREN, ")", startPos, startLine, startColumn));
                    advance();
                }
                case ',' -> {
                    tokens.add(new Token(TokenType.COMMA, ",", startPos, startLine, startColumn));
                    advance();
                }
                case ';' -> {
                    tokens.add(new Token(TokenType.SEMICOLON, ";", startPos, startLine, startColumn));
                    advance();
                }
                default -> throw error("Unexpected character '" + currentChar + "'");
            }
        }

        tokens.add(new Token(TokenType.EOF, "", pos, line, column));
        return tokens;
    }
}