package lab1;

import java.util.*;

public class FiniteAutomaton {

    // FA = (Q, Σ, δ, q0, F)
    // δ: state -> (symbol -> set of next states)  (NDFA-capable)
    private final Set<String> states;
    private final Set<Character> alphabet;
    private final Map<String, Map<Character, Set<String>>> delta;
    private final String startState;
    private final Set<String> finalStates;

    // Main constructor (NDFA)
    public FiniteAutomaton(Set<String> states,
                           Set<Character> alphabet,
                           Map<String, Map<Character, Set<String>>> delta,
                           String startState,
                           Set<String> finalStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.delta = delta;
        this.startState = startState;
        this.finalStates = finalStates;
    }

    // Factory method to reuse your Lab 1 DFA-style delta without constructor clash
    public static FiniteAutomaton fromDfaDelta(Set<String> states,
                                               Set<Character> alphabet,
                                               Map<String, Map<Character, String>> dfaDelta,
                                               String startState,
                                               Set<String> finalStates) {

        Map<String, Map<Character, Set<String>>> ndfaDelta = new HashMap<>();

        for (Map.Entry<String, Map<Character, String>> entry : dfaDelta.entrySet()) {
            String state = entry.getKey();
            Map<Character, String> transitions = entry.getValue();

            Map<Character, Set<String>> newTransitions = new HashMap<>();
            for (Map.Entry<Character, String> t : transitions.entrySet()) {
                newTransitions.put(t.getKey(), new HashSet<>(Set.of(t.getValue())));
            }

            ndfaDelta.put(state, newTransitions);
        }

        return new FiniteAutomaton(states, alphabet, ndfaDelta, startState, finalStates);
    }

    // NDFA-capable membership check
    public boolean stringBelongToLanguage(String input) {
        Set<String> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (!alphabet.contains(ch)) {
                return false;
            }

            Set<String> nextStates = new HashSet<>();

            for (String state : currentStates) {
                Map<Character, Set<String>> transitions = delta.get(state);
                if (transitions == null) continue;

                Set<String> targets = transitions.get(ch);
                if (targets != null) nextStates.addAll(targets);
            }

            currentStates = nextStates;

            if (currentStates.isEmpty()) {
                return false;
            }
        }

        // accept if ANY current state is final
        for (String s : currentStates) {
            if (finalStates.contains(s)) return true;
        }
        return false;
    }

    public boolean isDeterministic() {
        for (Map<Character, Set<String>> trans : delta.values()) {
            for (Set<String> targets : trans.values()) {
                if (targets != null && targets.size() > 1) return false;
            }
        }
        return true;
    }

    private static String nameOf(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return "{" + String.join(",", list) + "}";
    }

    private static Set<String> parseName(String name) {
        // converts "{q0,q1}" -> set("q0","q1")
        if (name == null || name.length() < 2) return Set.of();
        String inside = name.substring(1, name.length() - 1).trim();
        if (inside.isEmpty()) return Set.of();
        String[] parts = inside.split(",");
        Set<String> out = new HashSet<>();
        for (String p : parts) out.add(p.trim());
        return out;
    }

    // Subset construction NDFA -> DFA
    public FiniteAutomaton toDFA() {
        Map<String, Map<Character, Set<String>>> dfaDelta = new HashMap<>();
        Set<String> dfaStates = new HashSet<>();
        Set<String> dfaFinals = new HashSet<>();

        // start DFA state = {startState}
        Set<String> startSet = new HashSet<>();
        startSet.add(startState);
        String dfaStart = nameOf(startSet);

        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(dfaStart);
        visited.add(dfaStart);
        dfaStates.add(dfaStart);

        while (!queue.isEmpty()) {
            String currentDfaStateName = queue.poll();
            Set<String> currentNfaStates = parseName(currentDfaStateName);

            // final if contains any NFA final state
            for (String s : currentNfaStates) {
                if (finalStates.contains(s)) {
                    dfaFinals.add(currentDfaStateName);
                    break;
                }
            }

            Map<Character, Set<String>> transForThisDfaState = new HashMap<>();

            for (Character sym : alphabet) {
                Set<String> nextNfaStates = new HashSet<>();

                for (String nfaState : currentNfaStates) {
                    Map<Character, Set<String>> trans = delta.get(nfaState);
                    if (trans == null) continue;

                    Set<String> targets = trans.get(sym);
                    if (targets != null) nextNfaStates.addAll(targets);
                }

                if (!nextNfaStates.isEmpty()) {
                    String nextDfaName = nameOf(nextNfaStates);

                    // DFA transition goes to singleton set containing the DFA-state name
                    transForThisDfaState.put(sym, Set.of(nextDfaName));

                    if (!visited.contains(nextDfaName)) {
                        visited.add(nextDfaName);
                        dfaStates.add(nextDfaName);
                        queue.add(nextDfaName);
                    }
                }
            }

            dfaDelta.put(currentDfaStateName, transForThisDfaState);
        }

        return new FiniteAutomaton(dfaStates, alphabet, dfaDelta, dfaStart, dfaFinals);
    }

    // Convert FA -> Regular Grammar (Lab 2)
    public Grammar toRegularGrammar() {
        // Non-terminals are states
        Set<String> VN = new HashSet<>(states);

        // Terminals are alphabet symbols
        Set<Character> VT = new HashSet<>(alphabet);

        // Productions: state -> list of rhs strings
        Map<String, List<String>> P = new HashMap<>();
        for (String st : states) P.put(st, new ArrayList<>());

        // For each transition p --a--> q:
        // add p -> a q
        // if q is final, also add p -> a
        for (Map.Entry<String, Map<Character, Set<String>>> e : delta.entrySet()) {
            String p = e.getKey();
            for (Map.Entry<Character, Set<String>> t : e.getValue().entrySet()) {
                char a = t.getKey();
                for (String q : t.getValue()) {
                    P.get(p).add("" + a + q);

                    if (finalStates.contains(q)) {
                        P.get(p).add("" + a);
                    }
                }
            }
        }

        // Start symbol is the start state
        return new Grammar(VN, VT, P, startState);
    }

    public void printDefinition() {
        System.out.println("States Q = " + states);
        System.out.println("Alphabet Σ = " + alphabet);
        System.out.println("Start state q0 = " + startState);
        System.out.println("Final states F = " + finalStates);
        System.out.println("Transitions δ:");

        for (String state : delta.keySet()) {
            for (Map.Entry<Character, Set<String>> entry : delta.get(state).entrySet()) {
                System.out.println("  δ(" + state + ", " + entry.getKey() + ") = " + entry.getValue());
            }
        }
    }

    // BONUS: Graphviz DOT export
    public String toDot(String graphName) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph ").append(graphName).append(" {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=circle];\n\n");

        // Invisible start arrow
        sb.append("  __start__ [shape=none,label=\"\"];\n");
        sb.append("  __start__ -> \"").append(escapeDot(startState)).append("\";\n\n");

        // Final states as doublecircle
        if (!finalStates.isEmpty()) {
            sb.append("  node [shape=doublecircle];\n");
            for (String f : finalStates) {
                sb.append("  \"").append(escapeDot(f)).append("\";\n");
            }
            sb.append("  node [shape=circle];\n\n");
        }

        // Combine labels for same (from,to)
        Map<String, Map<String, Set<Character>>> combined = new HashMap<>();
        for (Map.Entry<String, Map<Character, Set<String>>> e : delta.entrySet()) {
            String from = e.getKey();
            for (Map.Entry<Character, Set<String>> t : e.getValue().entrySet()) {
                char sym = t.getKey();
                for (String to : t.getValue()) {
                    combined
                            .computeIfAbsent(from, k -> new HashMap<>())
                            .computeIfAbsent(to, k -> new HashSet<>())
                            .add(sym);
                }
            }
        }

        for (Map.Entry<String, Map<String, Set<Character>>> fromEntry : combined.entrySet()) {
            String from = fromEntry.getKey();
            for (Map.Entry<String, Set<Character>> toEntry : fromEntry.getValue().entrySet()) {
                String to = toEntry.getKey();

                List<Character> labels = new ArrayList<>(toEntry.getValue());
                Collections.sort(labels);

                StringBuilder label = new StringBuilder();
                for (int i = 0; i < labels.size(); i++) {
                    if (i > 0) label.append(",");
                    label.append(labels.get(i));
                }

                sb.append("  \"").append(escapeDot(from)).append("\"")
                        .append(" -> ")
                        .append("\"").append(escapeDot(to)).append("\"")
                        .append(" [label=\"").append(label).append("\"];\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static String escapeDot(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Getters (useful for Lab 2 conversions)
    public Set<String> getStates() { return states; }
    public Set<Character> getAlphabet() { return alphabet; }
    public Map<String, Map<Character, Set<String>>> getDelta() { return delta; }
    public String getStartState() { return startState; }
    public Set<String> getFinalStates() { return finalStates; }
}