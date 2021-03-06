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
    Map<String, ArrayList<String>> follow;

    //constructor for Grammar
    public Grammar() {
        this.start = "";
        this.variables = new ArrayList<>();
        this.terminals = new ArrayList<>();
        this.productions = new HashMap<>();
        this.first = new HashMap<>();
        this.follow = new HashMap<>();
    }

    //adds a production to a variable
    public void addProduction(String v, String p) {
        ArrayList<String> l = this.productions.get(v);
        if (l == null) {
            l = new ArrayList<>();
            this.productions.put(v, l);
        }
        l.add(p);
    }

    //returns all the productions of a particualr variable
    public ArrayList<String> getAllProductionsForVariable(String v) {
        if (this.productions.get(v) == null) {
            return new ArrayList<>();
        } else {
            return this.productions.get(v);
        }
    }
}

class LL1Parser {
    Grammar grammar = new Grammar();
    Map<String, Map<String, String>> ll1ParsingTable;

    //constructor for the parser
    public LL1Parser() {
        this.grammar = new Grammar();
        this.ll1ParsingTable = new HashMap<>();
    }
}


public class GrammarParser {

    static Map<String, ArrayList<String>> firstSet;
    static Map<String, ArrayList<String>> followSet;


    public static boolean parseInput(String[] inputLines, LL1Parser ll1Parser) {

        for (int i = 0; i < inputLines.length; i++) {

            //break each input into tokens
            List<String> inputTokensList = Arrays.asList(inputLines[i].split("\\s"));
            ArrayList<String> inputTokens = new ArrayList<>();
            for (String s : inputTokensList) {
                inputTokens.add(s);
            }

            //append the stack with dollar first
            Stack<String> stack = new Stack<>();
            stack.add("$");

            stack.add(ll1Parser.grammar.start);
            inputTokens.add("$");

            //get the next token and remove it
            String nextToken = inputTokens.get(0);
            inputTokens.remove(0);

            while (!stack.isEmpty() && nextToken != null) {

                //get the variable
                String v = stack.pop();

                //only if v is a variable, proceed
                if (ll1Parser.grammar.variables.contains(v)) {
                    //get the value for the token from the table
                    String p = ll1Parser.ll1ParsingTable.get(v).get(nextToken);
                    if (p == null) {
                        return false;
                    }
                    //get the production and add the production elements to the stack
                    if (!p.equals("")) {
                        List<String> pTokens = Arrays.asList(p.split("\\s"));
                        Collections.reverse(pTokens);
                        for (String s : pTokens) {
                            stack.add(s);
                        }
                    }
                } else {
                    if (nextToken.equals(v)) {
                        if (!inputTokens.isEmpty()) {
                            //get the next token and remove it from input
                            nextToken = inputTokens.get(0);
                            inputTokens.remove(0);
                        }
                    } else {
                        return false;
                    }
                }

            }
        }


        return true;
    }


    public static LL1Parser constructLL1Parser(Grammar grammar) {

        LL1Parser ll1Parser = new LL1Parser();
        ll1Parser.grammar = grammar;

        Map<String, Map<String, String>> ll1ParsingTable = new HashMap<>();

        Map<String, Map<String, ArrayList<String>>> first = grammar.first;

        //initialize the table for every variable
        for (String variable : first.keySet()) {
            ll1ParsingTable.put(variable, new HashMap<>());

            Map<String, ArrayList<String>> firstOfVar = first.get(variable);

            for (String prodRule : firstOfVar.keySet()) {
                ArrayList<String> terms = firstOfVar.get(prodRule);

                //check if the term is there in the keys of variable (error)
                for (String term : terms) {
                    if (ll1ParsingTable.get(variable).containsKey(term)) {
                        System.out.println("Error. Given grammar is not LL1");
                    }
                    //add the rule to the term for the variable
                    if (!prodRule.equals("")) {
                        ll1ParsingTable.get(variable).put(term, prodRule);
                    }

                }
            }
        }

        Map<String, ArrayList<String>> follow = grammar.follow;
        for (String variable : follow.keySet()) {
            if (first.get(variable).containsKey("")) {
                for (String term : follow.get(variable)) {
                    ll1ParsingTable.get(variable).putIfAbsent(term, "");
                }
            }
        }

        ll1Parser.ll1ParsingTable = ll1ParsingTable;


        return ll1Parser;
    }


    public static void addFollows(Grammar grammar, String variable) {

        //initialise the follow set for the variable
        followSet.put(variable, new ArrayList<>());

        if (grammar.start.equals(variable)) {
            ArrayList<String> l = new ArrayList<>();
            l.add("$");
            followSet.put(variable, l);
        }

        Map<String, ArrayList<String>> productions = grammar.productions;

        //for each production, for each rule, get the follow
        for (String key : productions.keySet()) {
            ArrayList<String> productionRules = productions.get(key);
            for (String prodRule : productionRules) {

                String[] elements = prodRule.split("\\s");
                List<Integer> indices = new ArrayList<>();

                for (int i = 0; i < elements.length; i++) {
                    if (elements[i].equals(variable)) {
                        indices.add(i);
                    }
                }

                for (Integer index : indices) {
                    if (index == elements.length - 1) {
                        //if the follow set of the var is not done, do it first
                        if (!followSet.keySet().contains(key)) {
                            addFollows(grammar, key);
                        }
                        //add all th follows of the production to variable
                        ArrayList<String> l1 = followSet.get(variable);
                        ArrayList<String> l2 = followSet.get(key);
                        l1.addAll(l2);
                        followSet.put(variable, l1);

                    } else {
                        String str = "";
                        for (int i = index + 1; i < elements.length; i++) {
                            str += elements[i];
                            if (i != elements.length - 1) {
                                str += " ";
                            }
                        }
                        ArrayList<String> firsts = getFirst(grammar, str, firstSet);

                        //if the first of the string -> epsilon, then add the follow of the var to follow of thr variable
                        if (firsts.contains("")) {
                            firsts.remove("");
                            ArrayList<String> followOfVarriable = followSet.get(variable);
                            followOfVarriable.addAll(firsts);
                            followSet.put(variable, followOfVarriable);


                            if (!followSet.keySet().contains(key)) {
                                addFollows(grammar, key);
                            }
                            //add all th follows of the production to variable
                            ArrayList<String> list1 = followSet.get(variable);
                            ArrayList<String> list2 = followSet.get(key);
                            list1.addAll(list2);
                            followSet.put(variable, list1);

                        } else {
                            ArrayList<String> l1 = followSet.get(variable);
                            l1.addAll(firsts);
                            followSet.put(variable, l1);
                        }
                    }
                }
            }
        }

    }

    public static Map<String, ArrayList<String>> getFollowSet(Grammar grammar) {

        followSet = new HashMap<>();

        for (String variable : grammar.variables) {
            addFollows(grammar, variable);
        }

        //remove duplicates from the follow set of each variable
        Map<String, ArrayList<String>> finalFollowSet = new HashMap<>();
        for (String variable : grammar.variables) {
            ArrayList<String> list = followSet.get(variable);
            ArrayList<String> uniqueElements = new ArrayList<>();
            for (String s : list) {
                if (!uniqueElements.contains(s)) {
                    uniqueElements.add(s);
                }
            }
            finalFollowSet.put(variable, uniqueElements);
        }

        return finalFollowSet;

    }

    //returns the first for a given string
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

    public static void addFirstSetOfVar(Grammar grammar, String variable) {

        if (!(firstSet.containsKey(variable))) {

            //get all the productions for the variable
            ArrayList<String> productions = grammar.getAllProductionsForVariable(variable);

            //initialise the first set of the variable
            firstSet.put(variable, new ArrayList<>());

            //for each production, get the first terminal and add it the the first set
            for (String p : productions) {
                String[] elements = p.split("\\s");
                for (String e : elements) {
                    //recursively add the first terminal
                    addFirstSetOfVar(grammar, e);
                    ArrayList<String> l1 = firstSet.get(variable);
                    ArrayList<String> l2 = firstSet.get(e);
                    l1.addAll(l2);
                    //if e is a terminal or if e does not -> epsilon, then break
                    if (grammar.terminals.contains(e) || (!grammar.getAllProductionsForVariable(e).contains(""))) {
                        break;
                    }

                }
            }
        }

    }

    public static Map<String, Map<String, ArrayList<String>>> getFirstSet(Grammar grammar) {

        firstSet = new HashMap<>();

        for (String terminal : grammar.terminals) {
            ArrayList<String> l = new ArrayList<>();
            l.add(terminal);
            firstSet.put(terminal, l);
        }

        //for every variable, get the first set
        for (String variable : grammar.variables) {
            if (!firstSet.containsKey(variable)) {
                addFirstSetOfVar(grammar, variable);
            }
        }

        //remove the duplications in the first sets
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
        System.out.println("Variables: " + grammar.variables);
        System.out.println("Terminals: " + grammar.terminals);
        System.out.println("Productions: " + grammar.productions);
        System.out.println("First set: " + grammar.first);
        System.out.println("Follow set: " + grammar.follow);

    }

    public static Grammar constructGrammar(String[] rules) {

        Grammar grammar = new Grammar();
        int numOfRules = rules.length;

        for (int i = 0; i < numOfRules; i++) {

            //split each rule by '->'
            String[] ruleTokens = rules[i].split("\\s->\\s");

            //left of the rule is variable, add it to the variables of grammar
            String variable = ruleTokens[0];
            if (!grammar.variables.contains(variable)) {
                grammar.variables.add(variable);
            }
            String rightToken = ruleTokens[1];

            //split the right side of the rule by "|" to get all productions
            ruleTokens = StringUtils.splitString(ruleTokens[1], "|");
            int count = 0;
            //check for Variable -> epsilon.
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

            //link all the possible productions to the variable
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

            //if an element is in variables and terminals, remove it from terminals
            for (String var : grammar.variables) {
                if (grammar.terminals.contains(var)) {
                    grammar.terminals.remove(var);
                }
            }

        }
        //set the first variable to start variable
        grammar.start = grammar.variables.get(0);
        grammar.first = getFirstSet(grammar);
        grammar.follow = getFollowSet(grammar);
        return grammar;
    }

    public static void main(String[] args) {
        try {
            //first arguments is the file in which the grammar is present
            String grammarFileName = args[0];
            //second file: input strings
            String inputFileName = args[1];
            BufferedReader br = new BufferedReader(new FileReader(grammarFileName));
            String line;

            List<String> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            br.close();

            String[] lines = list.toArray(new String[0]);

            //construct the grammar objectt
            Grammar grammar = constructGrammar(lines);
            printGrammar(grammar);

            //construct the parser object
            LL1Parser ll1Parser = constructLL1Parser(grammar);
            System.out.println("Table: " + ll1Parser.ll1ParsingTable);

            br = new BufferedReader(new FileReader(inputFileName));
            list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            br.close();

            String[] inputLines = list.toArray(new String[0]);

            //parse the input
            boolean result = parseInput(inputLines, ll1Parser);
            System.out.println("Parse result: " + result);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}