import com.sun.deploy.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

class Grammar {
    String start;
    List<String> variables;
    List<String> terminals;
    Map<String, ArrayList<String>> productions;
    Map<String, Map<String, ArrayList<String>>> first;
    List<String> follow;

    public Grammar() {
        this.start = "";
        this.variables = new ArrayList<>();
        this.terminals = new ArrayList<>();
        this.productions = new HashMap<>();
        this.first = new HashMap<>();
        this.follow = new ArrayList<>();
    }

    public void addProduction(String v, String p) {
        ArrayList<String> l = this.productions.get(v);
        if (l == null) {
            l = new ArrayList<>();
            this.productions.put(v, l);
        }
        l.add(p);
    }

    public ArrayList<String> getAllProductionsForVariable(String v) {
        if (this.productions.get(v) == null) {
            return new ArrayList<>();
        } else {
            return this.productions.get(v);
        }
    }
}

public class LL1Parser {

    public static ArrayList<String> getFirst(Grammar grammar, String str, Map<String, ArrayList<String>> firstSet) {

        ArrayList<String> retList = new ArrayList<>();

        String[] elements = str.split("\\s");

        for (String e : elements) {
            List<String> l = firstSet.get(e);
            retList.addAll(l);
            if (grammar.terminals.contains(e) || (!grammar.getAllProductionsForVariable(e).contains(""))) {
                break;
            }
        }

        return retList;
    }

    public static Map<String, ArrayList<String>> addFirstSetOfVar(Grammar grammar, Map<String, ArrayList<String>> firstSet, String variable) {

        if (firstSet.containsKey(variable)) {
            return firstSet;
        }

        ArrayList<String> productions = grammar.getAllProductionsForVariable(variable);
        firstSet.put(variable, new ArrayList<>());

        for (String p : productions) {
            String[] elements = p.split("\\s");
            for (String e : elements) {
                firstSet = addFirstSetOfVar(grammar, firstSet, e);
                ArrayList<String> l1 = firstSet.get(variable);
                ArrayList<String> l2 = firstSet.get(e);
                l1.addAll(l2);
                if (grammar.terminals.contains(e) || (!grammar.getAllProductionsForVariable(e).contains(""))) {
                    break;
                }

            }
        }


        return firstSet;
    }

    public static Map<String, Map<String, ArrayList<String>>> getFirstSet(Grammar grammar) {

        Map<String, ArrayList<String>> firstSet = new HashMap<>();

        for (String terminal : grammar.terminals) {
            ArrayList<String> l = new ArrayList<>();
            l.add(terminal);
            firstSet.put(terminal, l);
        }

        for (String variable : grammar.variables) {
            if (!firstSet.containsKey(variable)) {
                firstSet = addFirstSetOfVar(grammar, firstSet, variable);
            }
        }

        for (String f : firstSet.keySet()) {
            List<String> l = firstSet.get(f);
            l = l.stream().distinct().collect(Collectors.toList());
            firstSet.put(f, (ArrayList<String>) l);
        }

        Map<String, Map<String, ArrayList<String>>> finalFirstSet = new HashMap<>();

        for (String variable : grammar.variables) {
            finalFirstSet.put(variable, new HashMap<>());

            for (String p : grammar.getAllProductionsForVariable(variable)) {
                finalFirstSet.get(variable).put(p, getFirst(grammar, p, firstSet));
            }

        }
        return finalFirstSet;
    }

    public static void printGrammar(Grammar grammar) {
        System.out.println("Start: " + grammar.start);
        System.out.println("Variables: ");
        for (String var : grammar.variables) {
            System.out.println(var);
        }
        System.out.println("Terminals: ");
        for (String term : grammar.terminals) {
            System.out.println(term);
        }
        System.out.println("Productions: ");
        for (Map.Entry entry : grammar.productions.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
        System.out.println("First set: ");
        for (String key : grammar.first.keySet()) {
            System.out.println("    " + key + ":");
            Map<String, ArrayList<String>> map = grammar.first.get(key);
            for (String k : map.keySet()) {
                System.out.println("        " + k + ": " + map.get(k));
            }
        }

    }

    public static Grammar constructGrammar(String[] rules) {

        Grammar grammar = new Grammar();
        int numOfRules = rules.length;

        for (int i = 0; i < numOfRules; i++) {
            String[] ruleTokens = rules[i].split("\\s->\\s");

            String variable = ruleTokens[0];
            if (!grammar.variables.contains(variable)) {
                grammar.variables.add(variable);
            }
            String rightToken = ruleTokens[1];

            ruleTokens = StringUtils.splitString(ruleTokens[1], "|");
            int count = 0;
            for (char c : rightToken.toCharArray()) {
                if (c == '|') {
                    count++;
                }
            }
            if (count == ruleTokens.length) {
                String[] tempArr = new String[ruleTokens.length + 1];
                for (int itr = 0; itr < ruleTokens.length; itr++) {
                    tempArr[itr] = ruleTokens[itr];
                }
                tempArr[ruleTokens.length] = "";
                ruleTokens = tempArr;
            }

            for (int l = 0; l < ruleTokens.length; l++) {
                ruleTokens[l] = ruleTokens[l].trim();
            }

            for (int j = 0; j < ruleTokens.length; j++) {
                String production = ruleTokens[j];
                grammar.addProduction(variable, production);
                String[] terms = production.split("\\s");
                for (int k = 0; k < terms.length; k++) {
                    String term = terms[k];
                    if (!grammar.variables.contains(term)) {
                        if (!grammar.terminals.contains(term))
                            grammar.terminals.add(term);
                    }
                }

            }

            for (String var : grammar.variables) {
                if (grammar.terminals.contains(var)) {
                    grammar.terminals.remove(var);
                }
            }

        }
        grammar.start = grammar.variables.get(0);
        grammar.first = getFirstSet(grammar);
        return grammar;
    }

    public static void main(String[] args) {
        try {
            String fileName = args[0];
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            List<String> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                list.add(line);
            }

            String[] lines = list.toArray(new String[0]);

            Grammar grammar = constructGrammar(lines);
            printGrammar(grammar);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}