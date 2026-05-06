package lab6;

import java.io.FileWriter;
import java.io.IOException;

public class ASTVisualizer {
    private static int nodeId;

    public static void generateDot(ASTNode root, String fileName) throws IOException {
        nodeId = 0;

        StringBuilder sb = new StringBuilder();

        sb.append("digraph AST {\n");
        sb.append("    graph [rankdir=TB];\n");
        sb.append("    node [shape=box, style=rounded];\n");
        sb.append("    edge [arrowhead=none];\n\n");

        buildTree(root, sb);

        sb.append("}\n");

        FileWriter writer = new FileWriter(fileName);
        writer.write(sb.toString());
        writer.close();
    }

    private static int buildTree(ASTNode node, StringBuilder sb) {
        int currentId = nodeId++;

        sb.append("    node")
                .append(currentId)
                .append(" [label=\"")
                .append(escape(node.getLabel()))
                .append("\"];\n");

        if (node instanceof AssignmentNode assignmentNode) {
            int childId = buildTree(assignmentNode.getExpression(), sb);
            connect(sb, currentId, childId);
        } else if (node instanceof BinaryOperationNode binaryOperationNode) {
            int leftId = buildTree(binaryOperationNode.getLeft(), sb);
            int rightId = buildTree(binaryOperationNode.getRight(), sb);

            connect(sb, currentId, leftId);
            connect(sb, currentId, rightId);
        } else if (node instanceof UnaryOperationNode unaryOperationNode) {
            int childId = buildTree(unaryOperationNode.getExpression(), sb);
            connect(sb, currentId, childId);
        } else if (node instanceof FunctionCallNode functionCallNode) {
            int childId = buildTree(functionCallNode.getArgument(), sb);
            connect(sb, currentId, childId);
        }

        return currentId;
    }

    private static void connect(StringBuilder sb, int parentId, int childId) {
        sb.append("    node")
                .append(parentId)
                .append(" -> node")
                .append(childId)
                .append(";\n");
    }

    private static String escape(String text) {
        return text.replace("\"", "\\\"");
    }
}