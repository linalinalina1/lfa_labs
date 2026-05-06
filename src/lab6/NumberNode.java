package lab6;

public class NumberNode extends ASTNode {
    private final String value;

    public NumberNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String print(String prefix, boolean isTail) {
        return prefix + (isTail ? "└── " : "├── ") + getLabel();
    }

    @Override
    public String getLabel() {
        return "Number(" + value + ")";
    }
}