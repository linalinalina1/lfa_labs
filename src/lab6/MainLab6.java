package lab6;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class MainLab6 {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter expression:");

        String input = scanner.nextLine();

        try {

            Lexer lexer = new Lexer(input);

            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);

            ASTNode ast = parser.parse();

            System.out.println("\nABSTRACT SYNTAX TREE ");

            System.out.println(ast.printTree());

            File directory = new File("docs/lab6/images");

            if (!directory.exists()) {
                directory.mkdirs();
            }

            String dotPath = "docs/lab6/images/ast.dot";

            ASTVisualizer.generateDot(ast, dotPath);

            System.out.println("\nDOT file generated:");
            System.out.println(dotPath);

            System.out.println("\nGenerate PNG with:");

            System.out.println(
                    "dot -Tpng docs/lab6/images/ast.dot " +
                            "-o docs/lab6/images/ast.png"
            );

        } catch (Exception e) {

            System.out.println("\nError:");
            System.out.println(e.getMessage());
        }

        scanner.close();
    }
}