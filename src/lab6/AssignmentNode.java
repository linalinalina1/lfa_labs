package lab6;

public class AssignmentNode extends ASTNode {
    private final String variable;
    private final ASTNode expression;

    public AssignmentNode(String variable, ASTNode expression) {
        this.variable = variable;
        this.expression = expression;
    }

    public String getVariable() {
        return variable;
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
        return "Assignment(" + variable + ")";
    }
}