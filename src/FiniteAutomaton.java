import java.util.*;

public class FiniteAutomaton {

    // DFA = (Q, Σ, δ, q0, F)
    private final Set<String> states;
    private final Set<Character> alphabet;
    private final Map<String, Map<Character, String>> delta;
    private final String startState;
    private final Set<String> finalStates;

    public FiniteAutomaton(Set<String> states,
                           Set<Character> alphabet,
                           Map<String, Map<Character, String>> delta,
                           String startState,
                           Set<String> finalStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.delta = delta;
        this.startState = startState;
        this.finalStates = finalStates;
    }

    public boolean stringBelongToLanguage(String input) {
        String current = startState;

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (!alphabet.contains(ch)) {
                return false;
            }

            Map<Character, String> transitions = delta.get(current);
            if (transitions == null) {
                return false;
            }

            String next = transitions.get(ch);
            if (next == null) {
                return false;
            }

            current = next;
        }

        return finalStates.contains(current);
    }

    public void printDefinition() {
        System.out.println("States Q = " + states);
        System.out.println("Alphabet Σ = " + alphabet);
        System.out.println("Start state q0 = " + startState);
        System.out.println("Final states F = " + finalStates);
        System.out.println("Transitions δ:");

        for (String state : delta.keySet()) {
            for (Map.Entry<Character, String> entry : delta.get(state).entrySet()) {
                System.out.println("  δ(" + state + ", " + entry.getKey() + ") = " + entry.getValue());
            }
        }
    }
}
