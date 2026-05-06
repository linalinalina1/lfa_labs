package lab6;

public class FunctionCallNode extends ASTNode {
    private final String functionName;
    private final ASTNode argument;

    public FunctionCallNode(String functionName, ASTNode argument) {
        this.functionName = functionName;
        this.argument = argument;
    }

    public String getFunctionName() {
        return functionName;
    }

    public ASTNode getArgument() {
        return argument;
    }

    @Override
    public String print(String prefix, boolean isTail) {
        StringBuilder sb = new StringBuilder();

        sb.append(prefix)
                .append(isTail ? "└── " : "├── ")
                .append(getLabel());

        String childPrefix = prefix + (isTail ? "    " : "│   ");

        sb.append("\n").append(argument.print(childPrefix, true));

        return sb.toString();
    }

    @Override
    public String getLabel() {
        return "FunctionCall(" + functionName + ")";
    }
}