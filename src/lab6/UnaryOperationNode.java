package lab6;

public class UnaryOperationNode extends ASTNode {
    private final Token operator;
    private final ASTNode expression;

    public UnaryOperationNode(Token operator, ASTNode expression) {
        this.operator = operator;
        this.expression = expression;
    }

    public Token getOperator() {
        return operator;
    }

    public ASTNode getExpression() {
        return expression;
    }

    @Override
    public String print(String prefix, boolean isTail) {
        StringBuilder sb = new StringBuilder();

        sb.append(prefix)
                .append(isTail ? "└── " : "├── ")
                .append(getLabel());

        String childPrefix = prefix + (isTail ? "    " : "│   ");

        sb.append("\n").append(expression.print(childPrefix, true));

        return sb.toString();
    }

    @Override
    public String getLabel() {
        return "UnaryOperation(" + operator.getLexeme() + ")";
    }
}