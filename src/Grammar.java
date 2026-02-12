import java.util.*;

public class Grammar {

    // G = (VN, VT, P, S)
    private final Set<String> VN;
    private final Set<Character> VT;
    private final Map<String, List<String>> P;
    private final String S;

    private final Random random = new Random();

    public Grammar(Set<String> VN,
                   Set<Character> VT,
                   Map<String, List<String>> P,
                   String S) {
        this.VN = VN;
        this.VT = VT;
        this.P = P;
        this.S = S;
    }

    // Generate one valid string by random derivation
    public String generateString(int maxSteps) {
        String current = S;
        int steps = 0;

        while (steps < maxSteps) {
            steps++;

            int idx = firstNonTerminalIndex(current);
            if (idx == -1) {
                return current; // only terminals
            }

            String nonTerminal = String.valueOf(current.charAt(idx));
            List<String> options = P.get(nonTerminal);

            if (options == null || options.isEmpty()) {
                return "";
            }

            String rhs = options.get(random.nextInt(options.size()));

            current = current.substring(0, idx)
                    + rhs
                    + current.substring(idx + 1);
        }

        return "";
    }

    public List<String> generate5Strings() {
        List<String> result = new ArrayList<>();

        while (result.size() < 5) {
            String word = generateString(200);

            if (!word.isEmpty() && !result.contains(word)) {
                result.add(word);
            }
        }

        return result;
    }

    private int firstNonTerminalIndex(String str) {
        for (int i = 0; i < str.length(); i++) {
            String ch = String.valueOf(str.charAt(i));
            if (VN.contains(ch)) {
                return i;
            }
        }
        return -1;
    }

    // Convert right-linear grammar → DFA
    public FiniteAutomaton toFiniteAutomaton() {

        String finalState = "X";

        Set<String> states = new HashSet<>(VN);
        states.add(finalState);

        Set<Character> alphabet = new HashSet<>(VT);

        Map<String, Map<Character, String>> delta = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : P.entrySet()) {

            String left = entry.getKey();

            for (String rhs : entry.getValue()) {

                // Case: A → a
                if (rhs.length() == 1 && alphabet.contains(rhs.charAt(0))) {

                    char terminal = rhs.charAt(0);

                    delta
                            .computeIfAbsent(left, k -> new HashMap<>())
                            .put(terminal, finalState);
                }

                // Case: A → aB
                else if (rhs.length() == 2 && alphabet.contains(rhs.charAt(0))) {

                    char terminal = rhs.charAt(0);
                    String nextState = String.valueOf(rhs.charAt(1));

                    delta
                            .computeIfAbsent(left, k -> new HashMap<>())
                            .put(terminal, nextState);
                }

                else {
                    throw new IllegalArgumentException("Invalid production: "
                            + left + " -> " + rhs);
                }
            }
        }

        return new FiniteAutomaton(
                states,
                alphabet,
                delta,
                S,
                Set.of(finalState)
        );
    }

    public void printDefinition() {
        System.out.println("VN = " + VN);
        System.out.println("VT = " + VT);
        System.out.println("S  = " + S);
        System.out.println("P  = " + P);
    }

    // Variant 19 Grammar
    public static Grammar variant19() {

        Set<String> VN = Set.of("S", "A", "B", "C");
        Set<Character> VT = Set.of('a', 'b');

        Map<String, List<String>> P = new HashMap<>();

        P.put("S", List.of("aA"));
        P.put("A", List.of("bS", "aB"));
        P.put("B", List.of("bC"));
        P.put("C", List.of("aA", "b"));

        return new Grammar(VN, VT, P, "S");
    }
}
