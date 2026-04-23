package L5_ChomskyNormalForm;

import java.util.List;

public class ChomskyNormalForm {

    public static void main(String[] args) {

        List<String> rules = List.of(
                "1. S -> d B",
                "2. S -> B",
                "3. A -> d",
                "4. A -> d S",
                "5. A -> a A d C B",
                "6. B -> a C",
                "7. B -> b A",
                "8. B -> A C",
                "9. C -> ε",
                "10. E -> A S"
        );

        Grammar grammar = Grammar.fromRules("S", rules);

        System.out.println("INITIAL GRAMMAR ");
        grammar.print();

        CNFConverter converter = new CNFConverter(grammar);

        converter.eliminateEpsilonProductions();
        System.out.println("\n1. AFTER EPSILON REMOVAL ");
        converter.getGrammar().print();

        converter.eliminateUnitProductions();
        System.out.println("\n2. AFTER UNIT REMOVAL ");
        converter.getGrammar().print();

        converter.removeInaccessibleSymbols();
        System.out.println("\n3.AFTER REMOVING INACCESSIBLE ");
        converter.getGrammar().print();

        converter.removeNonProductiveSymbols();
        System.out.println("\n4. AFTER REMOVING NON-PRODUCTIVE ");
        converter.getGrammar().print();

        converter.convertToCNF();
        System.out.println("\n5. CHOMSKY NORMAL FORM ");
        converter.getGrammar().print();
    }
}