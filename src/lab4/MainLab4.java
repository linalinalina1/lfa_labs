package lab4;

import java.util.ArrayList;
import java.util.List;

public class MainLab4 {

    private static final boolean SHOW_PROCESSING_SEQUENCE = true;
    private static final int MAX_REPETITION = 5;

    public static void main(String[] args) {
        String[] expressions = {
                "O(P|Q|R)+2(3|4)",
                "A*B(C|D|E)F(G|H|I)^2",
                "J+K(L|M|N)*O?(P|Q)^3"
        };

        for (int i = 0; i < expressions.length; i++) {
            String expr = expressions[i];

            System.out.println("Expression " + (i + 1) + ": " + expr);

            Parser parser = new Parser(expr, SHOW_PROCESSING_SEQUENCE);
            Node root = parser.parse();

            List<String> results = root.generate();

            System.out.println("Generated valid words: " + results.size());
            for (String word : results) {
                System.out.println(word);
            }
            System.out.println();
        }
    }

    static class Parser {
        private final String input;
        private final boolean trace;
        private int pos;

        Parser(String input, boolean trace) {
            this.input = input.replaceAll("\\s+", "");
            this.trace = trace;
            this.pos = 0;
        }

        Node parse() {
            log("Start parsing regex: " + input);
            Node result = parseExpression();

            if (pos < input.length()) {
                throw new RuntimeException("Unexpected symbol at position " + pos + ": " + input.charAt(pos));
            }

            log("Finished parsing regex: " + input);
            log("");
            return result;
        }

        private Node parseExpression() {
            log("parseExpression at position " + pos);

            List<Node> alternatives = new ArrayList<>();
            alternatives.add(parseTerm());

            while (hasNext() && peek() == '|') {
                consume('|');
                log("Found alternation operator |");
                alternatives.add(parseTerm());
            }

            if (alternatives.size() == 1) {
                return alternatives.get(0);
            }

            return new AlternationNode(alternatives);
        }

        private Node parseTerm() {
            log("parseTerm at position " + pos);

            List<Node> factors = new ArrayList<>();

            while (hasNext() && peek() != ')' && peek() != '|') {
                factors.add(parseFactor());
            }

            if (factors.isEmpty()) {
                return new EmptyNode();
            }

            if (factors.size() == 1) {
                return factors.get(0);
            }

            return new SequenceNode(factors);
        }

        private Node parseFactor() {
            log("parseFactor at position " + pos);

            Node base = parseBase();

            if (!hasNext()) {
                return base;
            }

            char current = peek();

            if (current == '*') {
                consume('*');
                log("Applied operator * : repeat from 0 to " + MAX_REPETITION + " times");
                return new RepeatNode(base, 0, MAX_REPETITION);
            }

            if (current == '+') {
                consume('+');
                log("Applied operator + : repeat from 1 to " + MAX_REPETITION + " times");
                return new RepeatNode(base, 1, MAX_REPETITION);
            }

            if (current == '?') {
                consume('?');
                log("Applied operator ? : repeat from 0 to 1 time");
                return new RepeatNode(base, 0, 1);
            }

            if (current == '^') {
                consume('^');
                int number = parseNumber();
                log("Applied operator ^" + number + " : repeat exactly " + number + " times");
                return new RepeatNode(base, number, number);
            }

            return base;
        }

        private Node parseBase() {
            log("parseBase at position " + pos);

            char current = peek();

            if (current == '(') {
                consume('(');
                log("Entered group (");
                Node node = parseExpression();

                if (!hasNext() || peek() != ')') {
                    throw new RuntimeException("Missing ) at position " + pos);
                }

                consume(')');
                log("Closed group )");
                return node;
            }

            if (Character.isLetterOrDigit(current)) {
                consume(current);
                log("Read literal symbol: " + current);
                return new LiteralNode(String.valueOf(current));
            }

            throw new RuntimeException("Invalid symbol at position " + pos + ": " + current);
        }

        private int parseNumber() {
            if (!hasNext() || !Character.isDigit(peek())) {
                throw new RuntimeException("Expected a number at position " + pos);
            }

            int start = pos;
            while (hasNext() && Character.isDigit(peek())) {
                pos++;
            }

            return Integer.parseInt(input.substring(start, pos));
        }

        private boolean hasNext() {
            return pos < input.length();
        }

        private char peek() {
            return input.charAt(pos);
        }

        private void consume(char expected) {
            if (!hasNext() || input.charAt(pos) != expected) {
                throw new RuntimeException("Expected '" + expected + "' at position " + pos);
            }
            pos++;
        }

        private void log(String message) {
            if (trace) {
                System.out.println("[Processing] " + message);
            }
        }
    }

    interface Node {
        List<String> generate();
    }

    static class EmptyNode implements Node {
        @Override
        public List<String> generate() {
            List<String> result = new ArrayList<>();
            result.add("");
            return result;
        }
    }

    static class LiteralNode implements Node {
        private final String value;

        LiteralNode(String value) {
            this.value = value;
        }

        @Override
        public List<String> generate() {
            List<String> result = new ArrayList<>();
            result.add(value);
            return result;
        }
    }

    static class SequenceNode implements Node {
        private final List<Node> parts;

        SequenceNode(List<Node> parts) {
            this.parts = parts;
        }

        @Override
        public List<String> generate() {
            List<String> result = new ArrayList<>();
            result.add("");

            for (Node part : parts) {
                List<String> generatedPart = part.generate();
                List<String> combined = new ArrayList<>();

                for (String left : result) {
                    for (String right : generatedPart) {
                        combined.add(left + right);
                    }
                }

                result = combined;
            }

            return result;
        }
    }

    static class AlternationNode implements Node {
        private final List<Node> options;

        AlternationNode(List<Node> options) {
            this.options = options;
        }

        @Override
        public List<String> generate() {
            List<String> result = new ArrayList<>();

            for (Node option : options) {
                result.addAll(option.generate());
            }

            return result;
        }
    }

    static class RepeatNode implements Node {
        private final Node node;
        private final int min;
        private final int max;

        RepeatNode(Node node, int min, int max) {
            this.node = node;
            this.min = min;
            this.max = max;
        }

        @Override
        public List<String> generate() {
            List<String> result = new ArrayList<>();

            for (int count = min; count <= max; count++) {
                result.addAll(generateRepeated(count));
            }

            return result;
        }

        private List<String> generateRepeated(int count) {
            List<String> result = new ArrayList<>();
            result.add("");

            List<String> unit = node.generate();

            for (int i = 0; i < count; i++) {
                List<String> next = new ArrayList<>();

                for (String left : result) {
                    for (String right : unit) {
                        next.add(left + right);
                    }
                }

                result = next;
            }

            return result;
        }
    }
}
