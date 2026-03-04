package lab1;

import java.util.*;

public class Grammar {

    // G = (VN, VT, P, S)
    private final Set<String> VN;
    private final Set<Character> VT;
    private final Map<String, List<String>> P;
    private final String S;

    private final Random random = new Random();

    public enum ChomskyType {
        TYPE_0, TYPE_1, TYPE_2, TYPE_3
    }

    public Grammar(Set<String> VN,
                   Set<Character> VT,
                   Map<String, List<String>> P,
                   String S) {
        this.VN = VN;
        this.VT = VT;
        this.P = P;
        this.S = S;
    }

    // Generate one valid string by random derivation (works best when VN are single-letter symbols)
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
        // NOTE: this is Lab1-style (single-character VN) generation helper
        for (int i = 0; i < str.length(); i++) {
            String ch = String.valueOf(str.charAt(i));
            if (VN.contains(ch)) {
                return i;
            }
        }
        return -1;
    }

    // Chomsky classification

    public ChomskyType classifyChomsky() {
        // Evaluate from most restrictive to least:
        if (isType3RegularRightLinear()) return ChomskyType.TYPE_3;
        if (isType2ContextFree()) return ChomskyType.TYPE_2;
        if (isType1ContextSensitive()) return ChomskyType.TYPE_1;
        return ChomskyType.TYPE_0;
    }

    // Type 3 (Regular): A -> wB | w | ε, where w is terminals only, B is one nonterminal at the end
    private boolean isType3RegularRightLinear() {
        for (Map.Entry<String, List<String>> e : P.entrySet()) {
            String lhs = e.getKey();

            // LHS must be exactly one nonterminal symbol
            if (!VN.contains(lhs)) return false;

            for (String rhs : e.getValue()) {
                if (isEpsilon(rhs)) continue;

                SuffixSplit split = splitTerminalPrefixAndOptionalNonterminalSuffix(rhs);
                if (split == null) return false;

                // prefix must be terminals only
                if (!allTerminals(split.terminalPrefix)) return false;

                // suffix is either null (meaning only terminals) or exactly one VN symbol
                if (split.nonterminalSuffix != null && !VN.contains(split.nonterminalSuffix)) return false;
            }
        }
        return true;
    }

    // Type 2 (Context-Free): A -> α, where A is a single nonterminal
    private boolean isType2ContextFree() {
        for (Map.Entry<String, List<String>> e : P.entrySet()) {
            String lhs = e.getKey();
            if (!VN.contains(lhs)) return false; // must be exactly one VN symbol

            for (String rhs : e.getValue()) {
                if (isEpsilon(rhs)) continue;
                if (tokenize(rhs) == null) return false; // must be expressible using VN/VT symbols
            }
        }
        return true;
    }

    // Type 1 (Context-Sensitive):
    //  - non-contracting: |LHS| <= |RHS| (in token count)
    //  - no ε-productions except possibly S -> ε, and only if S is not on any RHS
    private boolean isType1ContextSensitive() {
        boolean hasStartEpsilon = false;

        // Check if S appears in any RHS (needed for the special epsilon allowance)
        boolean startAppearsOnRhs = startSymbolAppearsOnAnyRhs();

        for (Map.Entry<String, List<String>> e : P.entrySet()) {
            String lhs = e.getKey();
            List<Token> lhsTokens = tokenize(lhs);
            if (lhsTokens == null) return false;

            // LHS must contain at least one nonterminal
            if (!containsNonterminal(lhsTokens)) return false;

            for (String rhs : e.getValue()) {
                if (isEpsilon(rhs)) {
                    // Only allowed: S -> ε, and S must not appear on any RHS
                    if (!lhs.equals(S)) return false;
                    if (startAppearsOnRhs) return false;
                    hasStartEpsilon = true;
                    continue;
                }

                List<Token> rhsTokens = tokenize(rhs);
                if (rhsTokens == null) return false;

                // Non-contracting: |LHS| <= |RHS|
                if (lhsTokens.size() > rhsTokens.size()) return false;
            }
        }

        return true; // if all rules satisfy the constraints
    }

    private boolean startSymbolAppearsOnAnyRhs() {
        for (Map.Entry<String, List<String>> e : P.entrySet()) {
            for (String rhs : e.getValue()) {
                if (isEpsilon(rhs)) continue;

                List<Token> toks = tokenize(rhs);
                if (toks == null) continue;

                for (Token t : toks) {
                    if (!t.isTerminal && t.value.equals(S)) return true;
                }
            }
        }
        return false;
    }

    // Tokenization helpers

    private static class Token {
        final boolean isTerminal;
        final String value;

        Token(boolean isTerminal, String value) {
            this.isTerminal = isTerminal;
            this.value = value;
        }
    }

    // Split for Type 3 check: RHS = (terminalPrefix) + (optional one VN symbol at end)
    private static class SuffixSplit {
        final String terminalPrefix;
        final String nonterminalSuffix; // may be null

        SuffixSplit(String terminalPrefix, String nonterminalSuffix) {
            this.terminalPrefix = terminalPrefix;
            this.nonterminalSuffix = nonterminalSuffix;
        }
    }

    private boolean isEpsilon(String s) {
        return s == null || s.isEmpty() || s.equals("ε");
    }

    private boolean allTerminals(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!VT.contains(s.charAt(i))) return false;
        }
        return true;
    }

    // Try to interpret rhs as: terminals + optional ONE nonterminal suffix (from VN)
    // Works for VN like "A" or "q0" or "{q0,q1}" etc.
    private SuffixSplit splitTerminalPrefixAndOptionalNonterminalSuffix(String rhs) {
        if (rhs == null) return null;
        if (rhs.isEmpty() || rhs.equals("ε")) return new SuffixSplit("", null);

        String bestSuffix = null;

        // Find the longest VN symbol that matches as a suffix
        for (String nt : VN) {
            if (rhs.endsWith(nt)) {
                if (bestSuffix == null || nt.length() > bestSuffix.length()) {
                    bestSuffix = nt;
                }
            }
        }

        if (bestSuffix == null) {
            // no nonterminal suffix, entire rhs must be terminals
            return new SuffixSplit(rhs, null);
        }

        String prefix = rhs.substring(0, rhs.length() - bestSuffix.length());
        return new SuffixSplit(prefix, bestSuffix);
    }

    // Tokenize a string into terminals (single chars from VT) and nonterminals (strings from VN).
    // Greedy longest-match for VN at each position. If it can't tokenize, returns null.
    private List<Token> tokenize(String str) {
        if (str == null) return null;
        if (str.isEmpty() || str.equals("ε")) return new ArrayList<>();

        List<String> vnSorted = new ArrayList<>(VN);
        vnSorted.sort((a, b) -> Integer.compare(b.length(), a.length())); // longest first

        List<Token> out = new ArrayList<>();
        int i = 0;

        while (i < str.length()) {
            boolean matchedNonterminal = false;

            // Try match a VN symbol at this position (longest first)
            for (String nt : vnSorted) {
                if (nt.isEmpty()) continue;
                if (i + nt.length() <= str.length() && str.startsWith(nt, i)) {
                    out.add(new Token(false, nt));
                    i += nt.length();
                    matchedNonterminal = true;
                    break;
                }
            }

            if (matchedNonterminal) continue;

            // Otherwise, must be a terminal character
            char c = str.charAt(i);
            if (!VT.contains(c)) {
                return null; // unknown symbol
            }
            out.add(new Token(true, String.valueOf(c)));
            i++;
        }

        return out;
    }

    private boolean containsNonterminal(List<Token> tokens) {
        for (Token t : tokens) {
            if (!t.isTerminal) return true;
        }
        return false;
    }

    // Convert right-linear grammar → DFA (Lab1)

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

        return FiniteAutomaton.fromDfaDelta(states, alphabet, delta, S, Set.of(finalState));
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