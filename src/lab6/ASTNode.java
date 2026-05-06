package lab6;

public abstract class ASTNode {
    public abstract String print(String prefix, boolean isTail);

    public String printTree() {
        return print("", true);
    }

    public abstract String getLabel();
}