import java.util.*;

public class Main {

    public static void main(String[] args) {

        Grammar grammar = Grammar.variant19();

        System.out.println("Grammar (Variant 19)");
        grammar.printDefinition();

        FiniteAutomaton fa = grammar.toFiniteAutomaton();

        System.out.println("\nConverted DFA");
        fa.printDefinition();

        System.out.println("\n5 Generated Strings");

        // generate + check immediately
        List<String> words = grammar.generate5Strings();
        for (String word : words) {
            System.out.println(word + " -> " +
                    fa.stringBelongToLanguage(word));
        }

        System.out.println("\nMembership Tests: ");

        // predefined tests
        List<String> tests = List.of("", "bbb", "aba", "a", "ab", "aabb", "abababb");

        for (String test : tests) {
            System.out.println(test + " -> " +
                    fa.stringBelongToLanguage(test));
        }

        // keyboard input tests

//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("\nEnter a string to test (type 'exit' to stop):");
//
//        while (true) {
//            System.out.print("> ");
//            String input = scanner.nextLine();
//
//            if (input.equalsIgnoreCase("exit")) {
//                break;
//            }
//
//            boolean result = fa.stringBelongToLanguage(input);
//            System.out.println("Accepted? " + result);
//        }
//
//        scanner.close();

    }
}
