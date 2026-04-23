package L5_ChomskyNormalForm;

import java.util.*;

public final class CNFConverter {

    private Grammar grammar;
    private int counter = 1;

    public CNFConverter(Grammar grammar) {
        this.grammar = grammar.deepCopy();
    }

    public Grammar getGrammar() {
        return grammar;
    }

    // EPSILON
    public void eliminateEpsilonProductions() {
        Set<String> nullable = computeNullable();
        Grammar newG = new Grammar(grammar.getStartSymbol());

        for (String nt : grammar.getNonTerminals()) {
            newG.ensureNonTerminal(nt);
        }

        for (Map.Entry<String, LinkedHashSet<List<String>>> entry : grammar.getProductions().entrySet()) {
            String left = entry.getKey();

            for (List<String> rhs : entry.getValue()) {
                if (rhs.isEmpty()) {
                    continue;
                }

                for (List<String> prod : generate(rhs, nullable)) {
                    if (!prod.isEmpty()) {
                        newG.addProduction(left, prod);
                    } else if (left.equals(grammar.getStartSymbol())) {
                        newG.addProduction(left, Collections.emptyList());
                    }
                }
            }
        }

        if (nullable.contains(grammar.getStartSymbol())) {
            newG.addProduction(grammar.getStartSymbol(), Collections.emptyList());
        }

        grammar = newG;
    }

    private Set<String> computeNullable() {
        Set<String> res = new LinkedHashSet<>();
        boolean changed;

        do {
            changed = false;
            for (Map.Entry<String, LinkedHashSet<List<String>>> e : grammar.getProductions().entrySet()) {
                for (List<String> rhs : e.getValue()) {
                    if (rhs.isEmpty() || rhs.stream().allMatch(res::contains)) {
                        if (res.add(e.getKey())) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        return res;
    }

    private Set<List<String>> generate(List<String> rhs, Set<String> nullable) {
        Set<List<String>> result = new LinkedHashSet<>();
        backtrack(rhs, 0, new ArrayList<>(), nullable, result);
        return result;
    }

    private void backtrack(List<String> rhs, int i, List<String> cur,
                           Set<String> nullable, Set<List<String>> result) {
        if (i == rhs.size()) {
            result.add(new ArrayList<>(cur));
            return;
        }

        String s = rhs.get(i);

        cur.add(s);
        backtrack(rhs, i + 1, cur, nullable, result);
        cur.remove(cur.size() - 1);

        if (nullable.contains(s)) {
            backtrack(rhs, i + 1, cur, nullable, result);
        }
    }

    // UNIT
    public void eliminateUnitProductions() {
        Grammar newG = new Grammar(grammar.getStartSymbol());

        for (String nt : grammar.getNonTerminals()) {
            newG.ensureNonTerminal(nt);
        }

        for (String nt : grammar.getNonTerminals()) {
            Set<String> closure = closure(nt);

            for (String target : closure) {
                for (List<String> rhs : grammar.getProductions().getOrDefault(target, new LinkedHashSet<>())) {
                    if (!(rhs.size() == 1 && grammar.isNonTerminal(rhs.get(0)))) {
                        newG.addProduction(nt, rhs);
                    }
                }
            }
        }

        grammar = newG;
    }

    private Set<String> closure(String start) {
        Set<String> res = new LinkedHashSet<>();
        Queue<String> q = new LinkedList<>();

        res.add(start);
        q.add(start);

        while (!q.isEmpty()) {
            String cur = q.poll();

            for (List<String> rhs : grammar.getProductions().getOrDefault(cur, new LinkedHashSet<>())) {
                if (rhs.size() == 1 && grammar.isNonTerminal(rhs.get(0))) {
                    String next = rhs.get(0);
                    if (res.add(next)) {
                        q.add(next);
                    }
                }
            }
        }

        return res;
    }

    // CLEAN
    public void removeInaccessibleSymbols() {
        Set<String> reachable = new LinkedHashSet<>();
        Queue<String> q = new LinkedList<>();

        reachable.add(grammar.getStartSymbol());
        q.add(grammar.getStartSymbol());

        while (!q.isEmpty()) {
            String cur = q.poll();

            for (List<String> rhs : grammar.getProductions().getOrDefault(cur, new LinkedHashSet<>())) {
                for (String s : rhs) {
                    if (grammar.isNonTerminal(s) && reachable.add(s)) {
                        q.add(s);
                    }
                }
            }
        }

        Grammar newG = new Grammar(grammar.getStartSymbol());

        for (String nt : grammar.getNonTerminals()) {
            if (reachable.contains(nt)) {
                newG.ensureNonTerminal(nt);
                for (List<String> rhs : grammar.getProductions().getOrDefault(nt, new LinkedHashSet<>())) {
                    newG.addProduction(nt, rhs);
                }
            }
        }

        grammar = newG;
    }

    public void removeNonProductiveSymbols() {
        Set<String> productive = new LinkedHashSet<>();
        boolean changed;

        do {
            changed = false;

            for (Map.Entry<String, LinkedHashSet<List<String>>> e : grammar.getProductions().entrySet()) {
                for (List<String> rhs : e.getValue()) {
                    if (rhs.stream().allMatch(s -> !grammar.isNonTerminal(s) || productive.contains(s))) {
                        if (productive.add(e.getKey())) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        Grammar newG = new Grammar(grammar.getStartSymbol());

        for (String nt : grammar.getNonTerminals()) {
            if (productive.contains(nt)) {
                newG.ensureNonTerminal(nt);

                for (List<String> rhs : grammar.getProductions().getOrDefault(nt, new LinkedHashSet<>())) {
                    if (rhs.stream().allMatch(s -> !grammar.isNonTerminal(s) || productive.contains(s))) {
                        newG.addProduction(nt, rhs);
                    }
                }
            }
        }

        grammar = newG;
    }

    // CNF
    public void convertToCNF() {
        Grammar working = grammar.deepCopy();

        if (startAppearsOnRightHandSide(working, working.getStartSymbol())) {
            String newStart = freshNonTerminal(working, "S0");
            Grammar updated = new Grammar(newStart);
            updated.ensureNonTerminal(newStart);
            updated.addProduction(newStart, List.of(working.getStartSymbol()));

            for (String nt : working.getNonTerminals()) {
                updated.ensureNonTerminal(nt);
                for (List<String> rhs : working.getProductions().getOrDefault(nt, new LinkedHashSet<>())) {
                    updated.addProduction(nt, rhs);
                }
            }

            working = updated;
        }

        Map<String, String> terminalToVar = new LinkedHashMap<>();
        Grammar replaced = new Grammar(working.getStartSymbol());

        for (String nt : working.getNonTerminals()) {
            replaced.ensureNonTerminal(nt);
        }

        for (Map.Entry<String, LinkedHashSet<List<String>>> entry : working.getProductions().entrySet()) {
            String left = entry.getKey();

            for (List<String> rhs : entry.getValue()) {
                if (rhs.size() <= 1) {
                    replaced.addProduction(left, rhs);
                    continue;
                }

                List<String> newRhs = new ArrayList<>();
                for (String symbol : rhs) {
                    if (working.isNonTerminal(symbol)) {
                        newRhs.add(symbol);
                    } else {
                        String var = terminalToVar.computeIfAbsent(
                                symbol,
                                t -> freshNonTerminal(replaced, "T_" + sanitize(t))
                        );
                        replaced.ensureNonTerminal(var);
                        newRhs.add(var);
                    }
                }

                replaced.addProduction(left, newRhs);
            }
        }

        for (Map.Entry<String, String> entry : terminalToVar.entrySet()) {
            replaced.addProduction(entry.getValue(), List.of(entry.getKey()));
        }

        Grammar binary = new Grammar(replaced.getStartSymbol());
        for (String nt : replaced.getNonTerminals()) {
            binary.ensureNonTerminal(nt);
        }

        Map<List<String>, String> suffixCache = new LinkedHashMap<>();

        for (Map.Entry<String, LinkedHashSet<List<String>>> entry : replaced.getProductions().entrySet()) {
            String left = entry.getKey();

            for (List<String> rhs : entry.getValue()) {
                if (rhs.size() <= 2) {
                    binary.addProduction(left, rhs);
                } else {
                    String currentLeft = left;
                    List<String> current = new ArrayList<>(rhs);

                    while (current.size() > 2) {
                        List<String> suffix = List.copyOf(current.subList(1, current.size()));
                        String helper = suffixCache.get(suffix);

                        if (helper == null) {
                            helper = freshNonTerminal(binary, "N");
                            suffixCache.put(suffix, helper);
                            binary.ensureNonTerminal(helper);
                        }

                        binary.addProduction(currentLeft, List.of(current.get(0), helper));

                        currentLeft = helper;
                        current = new ArrayList<>(current.subList(1, current.size()));
                    }

                    binary.addProduction(currentLeft, current);
                }
            }
        }

        grammar = binary;
    }

    private boolean startAppearsOnRightHandSide(Grammar g, String start) {
        for (LinkedHashSet<List<String>> rhsSet : g.getProductions().values()) {
            for (List<String> rhs : rhsSet) {
                if (rhs.contains(start)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String freshNonTerminal(Grammar g, String base) {
        String candidate = base;
        while (g.getProductions().containsKey(candidate)) {
            candidate = base + "_" + counter++;
        }
        return candidate;
    }

    private String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9]", "_");
    }
}