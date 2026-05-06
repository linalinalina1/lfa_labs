package lab6;

public class VariableNode extends ASTNode {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String print(String prefix, boolean isTail) {
        return prefix + (isTail ? "└── " : "├── ") + getLabel();
    }

    @Override
    public String getLabel() {
        return "Variable(" + name + ")";
    }
}