package lab3;

import java.util.List;
import java.util.Scanner;

public class MainLab3 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your expressions (type END on a new line to finish):");

        StringBuilder inputBuilder = new StringBuilder();

        while (true) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("END")) {
                break;
            }
            inputBuilder.append(line).append("\n");
        }

        String source = inputBuilder.toString();

        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            System.out.println();
            System.out.println("=== TOKENS ===");
            System.out.printf("%-12s %-15s %-10s %-8s %-8s%n",
                    "TYPE", "LEXEME", "POSITION", "LINE", "COLUMN");
            System.out.println("----------------------------------------------------------------");

            for (Token token : tokens) {
                System.out.printf("%-12s %-15s %-10d %-8d %-8d%n",
                        token.getType(),
                        "'" + token.getLexeme() + "'",
                        token.getPosition(),
                        token.getLine(),
                        token.getColumn());
            }

        } catch (RuntimeException e) {
            System.out.println();
            System.out.println("Lexical error:");
            System.out.println(e.getMessage());
        }

        scanner.close();
    }
}