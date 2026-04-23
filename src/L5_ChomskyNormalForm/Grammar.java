package L5_ChomskyNormalForm;

import java.util.*;
import java.util.stream.Collectors;

public final class Grammar {

    private final String startSymbol;
    private final LinkedHashMap<String, LinkedHashSet<List<String>>> productions;

    public Grammar(String startSymbol) {
        this.startSymbol = startSymbol;
        this.productions = new LinkedHashMap<>();
    }

    public String getStartSymbol() {
        return startSymbol;
    }

    public Set<String> getNonTerminals() {
        return productions.keySet();
    }

    public Map<String, LinkedHashSet<List<String>>> getProductions() {
        return productions;
    }

    public void ensureNonTerminal(String nt) {
        productions.computeIfAbsent(nt, k -> new LinkedHashSet<>());
    }

    public void addProduction(String left, List<String> right) {
        ensureNonTerminal(left);
        productions.get(left).add(List.copyOf(right));
    }

    public Grammar deepCopy() {
        Grammar copy = new Grammar(startSymbol);
        for (Map.Entry<String, LinkedHashSet<List<String>>> entry : productions.entrySet()) {
            copy.ensureNonTerminal(entry.getKey());
            for (List<String> rhs : entry.getValue()) {
                copy.addProduction(entry.getKey(), rhs);
            }
        }
        return copy;
    }

    public boolean isNonTerminal(String s) {
        return productions.containsKey(s);
    }

    public void print() {
        int i = 1;
        for (Map.Entry<String, LinkedHashSet<List<String>>> entry : productions.entrySet()) {
            for (List<String> rhs : entry.getValue()) {
                System.out.println(i++ + ". " + entry.getKey() + " -> " + format(rhs));
            }
        }
    }

    private static String format(List<String> rhs) {
        return rhs.isEmpty() ? "ε" : String.join(" ", rhs);
    }

    public static Grammar fromRules(String startSymbol, List<String> rules) {
        Grammar g = new Grammar(startSymbol);

        for (String rule : rules) {
            String cleaned = rule.replaceFirst("^\\d+\\.\\s*", "").trim();
            String[] parts = cleaned.split("->");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid rule: " + rule);
            }
            g.ensureNonTerminal(parts[0].trim());
        }

        for (String rule : rules) {
            String cleaned = rule.replaceFirst("^\\d+\\.\\s*", "").trim();
            String[] parts = cleaned.split("->");

            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid rule: " + rule);
            }

            String left = parts[0].trim();
            String right = parts[1].trim();

            if (right.equals("ε")) {
                g.addProduction(left, Collections.emptyList());
            } else {
                List<String> symbols = Arrays.stream(right.split("\\s+"))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());
                g.addProduction(left, symbols);
            }
        }

        return g;
    }
}