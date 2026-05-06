package lab6;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    private Token currentToken() {
        return tokens.get(pos);
    }

    private Token peek() {
        if (pos + 1 >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(pos + 1);
    }

    private void eat(TokenType type) {
        if (currentToken().getType() == type) {
            pos++;
        } else {
            throw new RuntimeException(
                    "Expected " + type +
                            " but found " + currentToken().getType() +
                            " at line " + currentToken().getLine() +
                            ", column " + currentToken().getColumn()
            );
        }
    }

    public ASTNode parse() {
        ASTNode node = statement();

        if (currentToken().getType() == TokenType.SEMICOLON) {
            eat(TokenType.SEMICOLON);
        }

        if (currentToken().getType() != TokenType.EOF) {
            throw new RuntimeException(
                    "Unexpected token '" + currentToken().getLexeme() +
                            "' at line " + currentToken().getLine() +
                            ", column " + currentToken().getColumn()
            );
        }

        return node;
    }

    private ASTNode statement() {
        if (currentToken().getType() == TokenType.IDENTIFIER
                && peek().getType() == TokenType.ASSIGN) {

            String variableName = currentToken().getLexeme();

            eat(TokenType.IDENTIFIER);
            eat(TokenType.ASSIGN);

            ASTNode expression = expression();

            return new AssignmentNode(variableName, expression);
        }

        return expression();
    }

    private ASTNode expression() {
        ASTNode node = term();

        while (currentToken().getType() == TokenType.PLUS
                || currentToken().getType() == TokenType.MINUS) {

            Token operator = currentToken();

            if (operator.getType() == TokenType.PLUS) {
                eat(TokenType.PLUS);
            } else {
                eat(TokenType.MINUS);
            }

            node = new BinaryOperationNode(node, operator, term());
        }

        return node;
    }

    private ASTNode term() {
        ASTNode node = power();

        while (currentToken().getType() == TokenType.MUL
                || currentToken().getType() == TokenType.DIV
                || currentToken().getType() == TokenType.MOD) {

            Token operator = currentToken();

            if (operator.getType() == TokenType.MUL) {
                eat(TokenType.MUL);
            } else if (operator.getType() == TokenType.DIV) {
                eat(TokenType.DIV);
            } else {
                eat(TokenType.MOD);
            }

            node = new BinaryOperationNode(node, operator, power());
        }

        return node;
    }

    private ASTNode power() {
        ASTNode node = factor();

        if (currentToken().getType() == TokenType.POW) {
            Token operator = currentToken();
            eat(TokenType.POW);

            node = new BinaryOperationNode(node, operator, power());
        }

        return node;
    }

    private ASTNode factor() {
        Token token = currentToken();

        switch (token.getType()) {
            case PLUS -> {
                eat(TokenType.PLUS);
                return new UnaryOperationNode(token, factor());
            }

            case MINUS -> {
                eat(TokenType.MINUS);
                return new UnaryOperationNode(token, factor());
            }

            case NUMBER -> {
                eat(TokenType.NUMBER);
                return new NumberNode(token.getLexeme());
            }

            case IDENTIFIER -> {
                eat(TokenType.IDENTIFIER);
                return new VariableNode(token.getLexeme());
            }

            case SIN, COS, TAN, SQRT, LOG -> {
                String functionName = token.getLexeme();

                eat(token.getType());
                eat(TokenType.LPAREN);

                ASTNode argument = expression();

                eat(TokenType.RPAREN);

                return new FunctionCallNode(functionName, argument);
            }

            case LPAREN -> {
                eat(TokenType.LPAREN);

                ASTNode node = expression();

                eat(TokenType.RPAREN);

                return node;
            }

            default -> throw new RuntimeException(
                    "Unexpected token '" + token.getLexeme() +
                            "' at line " + token.getLine() +
                            ", column " + token.getColumn()
            );
        }
    }
}