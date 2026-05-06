package lab6;

public class BinaryOperationNode extends ASTNode {
    private final ASTNode left;
    private final Token operator;
    private final ASTNode right;

    public BinaryOperationNode(ASTNode left, Token operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ASTNode getLeft() {
        return left;
    }

    public Token getOperator() {
        return operator;
    }

    public ASTNode getRight() {
        return right;
    }

    @Override
    public String print(String prefix, boolean isTail) {
        StringBuilder sb = new StringBuilder();

        sb.append(prefix)
                .append(isTail ? "└── " : "├── ")
                .append(getLabel());

        String childPrefix = prefix + (isTail ? "    " : "│   ");

        sb.append("\n").append(left.print(childPrefix, false));
        sb.append("\n").append(right.print(childPrefix, true));

        return sb.toString();
    }

    @Override
    public String getLabel() {
        return "BinaryOperation(" + operator.getLexeme() + ")";
    }
}