package lab2;

import lab1.FiniteAutomaton;
import lab1.Grammar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MainLab2 {
    public static void main(String[] args) {

        // Variant 19 NDFA
        // Q = {q0,q1,q2}
        // Σ = {a,b}
        // F = {q2}
        // δ(q0,a) = {q0,q1}
        // δ(q0,b) = {q0}
        // δ(q1,b) = {q1,q2}
        // δ(q2,b) = {q2}
        // Start = q0

        Set<String> Q = new HashSet<>(Set.of("q0", "q1", "q2"));
        Set<Character> Sigma = new HashSet<>(Set.of('a', 'b'));
        String q0 = "q0";
        Set<String> F = new HashSet<>(Set.of("q2"));

        Map<String, Map<Character, Set<String>>> delta = new HashMap<>();

        delta.computeIfAbsent("q0", k -> new HashMap<>())
                .put('a', new HashSet<>(Set.of("q0", "q1")));

        delta.get("q0").put('b', new HashSet<>(Set.of("q0")));

        delta.computeIfAbsent("q1", k -> new HashMap<>())
                .put('b', new HashSet<>(Set.of("q1", "q2")));

        delta.computeIfAbsent("q2", k -> new HashMap<>())
                .put('b', new HashSet<>(Set.of("q2")));

        FiniteAutomaton ndfa = new FiniteAutomaton(Q, Sigma, delta, q0, F);

        System.out.println(" Variant 19 NDFA ");
        ndfa.printDefinition();
        System.out.println("Deterministic? - " + ndfa.isDeterministic());

        List<String> tests = List.of(
                "", "a", "b", "ab", "ba",
                "bb", "abb", "abbb", "aab",
                "aabb", "bbbb"
        );

        System.out.println("\nNDFA membership tests:");
        for (String s : tests) {
            System.out.println("'" + s + "' -> " + ndfa.stringBelongToLanguage(s));
        }

        // NDFA → DFA
        FiniteAutomaton dfa = ndfa.toDFA();

        System.out.println("\n Converted DFA (from NDFA) ");
        dfa.printDefinition();
        System.out.println("Deterministic? - " + dfa.isDeterministic());

        System.out.println("\nDFA membership tests:");
        for (String s : tests) {
            System.out.println("'" + s + "' -> " + dfa.stringBelongToLanguage(s));
        }

        // FA → Regular Grammar
        System.out.println("\n Regular Grammar from NDFA ");
        Grammar g1 = ndfa.toRegularGrammar();
        g1.printDefinition();
        System.out.println("Chomsky type (Grammar from NDFA): " + g1.classifyChomsky());

        System.out.println("\n Regular Grammar from DFA ");
        Grammar g2 = dfa.toRegularGrammar();
        g2.printDefinition();
        System.out.println("Chomsky type (Grammar from DFA): " + g2.classifyChomsky());

        // Also show Lab1 grammar type (Variant 19)
        System.out.println("\nChomsky type (Lab1 Grammar variant19): " + Grammar.variant19().classifyChomsky());

        // BONUS: DOT export
        try {
            Files.createDirectories(Path.of("docs/lab2/images"));

            Files.writeString(Path.of("docs/lab2/images/variant19_ndfa.dot"),
                    ndfa.toDot("Variant19_NDFA"));

            Files.writeString(Path.of("docs/lab2/images/variant19_dfa.dot"),
                    dfa.toDot("Variant19_DFA"));

            System.out.println("\nDOT files created:");
            System.out.println(" - docs/lab2/images/variant19_ndfa.dot");
            System.out.println(" - docs/lab2/images/variant19_dfa.dot");
        } catch (Exception ex) {
            System.out.println("Failed to write DOT files: " + ex.getMessage());
        }
    }
}