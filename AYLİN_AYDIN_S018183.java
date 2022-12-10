import java.util.*;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
public class cfgtocnf {
    //private static final int MAX = 50;
    //private static String[][] grammar = new String[MAX][MAX]; //to store entered grammar
    //private static String[] ntrm = new String[MAX];
    //private static String[] trm = new String[MAX];
    private String input; //to declare the input taken from .txt file
    private int lineCount; //to understand the context free language size
    private String epsilonFound = ""; //store the non-terminal which contains epsilon value
    private Map<String, List<String>> mapVariableProduction = new LinkedHashMap<>(); //to create new grammar in a proper way
    private static Map<String, String> map = new HashMap<String, String>(); //to take input and convert it to desire format with the help of this hashmap
    private static String start; //initial start variable

    cfgtocnf(String input, int lineCount){
        this.input = input;
        this.lineCount = lineCount;
    }
    public static void main(String args[])
    {
        String rules = "";
        int line_count = 0;
        try {
            File myObj = new File("G2.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                /*if(data.equalsIgnoreCase("NON-TERMINAL")){
                    data = myReader.nextLine();
                    int count = 0;
                    while (!data.equalsIgnoreCase("TERMINAL")){
                        ntrm[count] = data;
                        count++;
                        data = myReader.nextLine();
                    }
                }
                if(data.equalsIgnoreCase("TERMINAL")){
                    data = myReader.nextLine();
                    int count = 0;
                    while (!data.equalsIgnoreCase("RULES")){
                        trm[count] = data;
                        count++;
                        data = myReader.nextLine();
                    }
                }*/
                if(data.equalsIgnoreCase("RULES")){
                    data = myReader.nextLine();
                    while(!data.equalsIgnoreCase("START")){
                        String letter = Character.toString(data.charAt(0));
                        if (map.keySet().contains(letter)) {
                            String newValue = map.get(letter) + "|" + data.substring(2);
                            //System.out.println("new values : "+newValue);
                            map.put(letter, newValue);
                        } else {
                            String newValue = "->" + data.substring(2);
                            map.put(letter, newValue);
                        }
                        data = myReader.nextLine();
                    }
                    data = myReader.nextLine();
                    start = data;
                    for (String name : map.keySet()) {
                        rules += name + map.get(name) + "\n";
                    }
                    rules = rules.substring(0, rules.length() - 1);
                    String[] splitt = rules.split("\n");
                    line_count = splitt.length;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        cfgtocnf c= new cfgtocnf(rules,line_count);
        c.CFGtoCNF();
        //c.printMap();
    }

    public void CFGtoCNF() {
        //add new start state
        ArrayList<String> newProduction = new ArrayList<>();
        newProduction.add(start);
        String newStart = start + "0";
        mapVariableProduction.put(newStart, newProduction);

        //printMap();

        //convert the result string to map
        String[] tmpArray = new String[lineCount];
        for (int i = 0; i < lineCount; i++) {
            tmpArray = input.split("\\n");
        }

        for (int i = 0; i < tmpArray.length; i++) {
            String[] tmp = tmpArray[i].split("->|\\|");
            String var = tmp[0].trim();

            String[] production = Arrays.copyOfRange(tmp, 1, tmp.length);
            List<String> productionList = new ArrayList<String>();
            // trim the empty space
            for (int k = 0; k < production.length; k++) {
                production[k] = production[k].trim();
            }
            // import array into ArrayList
            for (int j = 0; j < production.length; j++) {
                productionList.add(production[j]);
            }
            //insert element into map
            mapVariableProduction.put(var, productionList);
        }

        //printMap();
        //remove the epsilon

        for (int i = 0; i < lineCount; i++) {
            removeEpsilon();
        }

        //remove the duplicate values
        Iterator itr = mapVariableProduction.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            ArrayList<String> production = (ArrayList<String>) entry.getValue();

            for (int i = 0; i < production.size(); i++) {
                if (production.get(i).contains(entry.getKey().toString())) {
                    production.remove(entry.getKey().toString());
                }
            }
        }
        //printMap();
        //remove single variables if there is
        for (int i = 0; i < lineCount; i++) {
            removeSingleVariable();
        }

        //printMap();
        //create only two terminal one variable
        onlyTwoTerminalandOneVariable();

        //printMap();
        //if there are three terminals eliminate it
        //System.out.println("Replace two terminal variable with new variable ... ");
        for (int i = 0; i < lineCount; i++) {
            removeThreeTerminal();
        }

        //printMap();

        //print the final chomsky normal form
        print();

        //printMap();
    }

    private void removeEpsilon() {

        Iterator itr = mapVariableProduction.entrySet().iterator();
        Iterator itr2 = mapVariableProduction.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            ArrayList<String> productionRow = (ArrayList<String>) entry.getValue();

            if (productionRow.contains("e")) {
                if (productionRow.size() > 1) {
                    productionRow.remove("e");
                    epsilonFound = entry.getKey().toString();
                } else {
                    epsilonFound = entry.getKey().toString();
                    mapVariableProduction.remove(epsilonFound);
                }
            }
        }

        // find B and eliminate them
        while (itr2.hasNext()) {

            Map.Entry entry = (Map.Entry) itr2.next();
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();

            for (int i = 0; i < productionList.size(); i++) {
                String temp = productionList.get(i);

                for (int j = 0; j < temp.length(); j++) {
                    if (epsilonFound.equals(Character.toString(productionList.get(i).charAt(j)))) {

                        if (temp.length() == 2) {

                            // remove specific character in string
                            temp = temp.replace(epsilonFound, "");

                            if (!mapVariableProduction.get(entry.getKey().toString()).contains(temp)) {
                                mapVariableProduction.get(entry.getKey().toString()).add(temp);
                            }

                        } else if (temp.length() == 3) {

                            String deletedTemp = new StringBuilder(temp).deleteCharAt(j).toString();

                            if (!mapVariableProduction.get(entry.getKey().toString()).contains(deletedTemp)) {
                                mapVariableProduction.get(entry.getKey().toString()).add(deletedTemp);
                            }

                        } else if (temp.length() == 4) {

                            String deletedTemp = new StringBuilder(temp).deleteCharAt(j).toString();

                            if (!mapVariableProduction.get(entry.getKey().toString()).contains(deletedTemp)) {
                                mapVariableProduction.get(entry.getKey().toString()).add(deletedTemp);
                            }
                        } else {

                            if (!mapVariableProduction.get(entry.getKey().toString()).contains("e")) {
                                mapVariableProduction.get(entry.getKey().toString()).add("e");
                            }
                        }
                    }
                }
            }
        }
    }

    private void removeSingleVariable() {

        Iterator itr4 = mapVariableProduction.entrySet().iterator();
        String key = null;


        while (itr4.hasNext()) {

            Map.Entry entry = (Map.Entry) itr4.next();
            Set set = mapVariableProduction.keySet();
            ArrayList<String> keySet = new ArrayList<String>(set);
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();

            for (int i = 0; i < productionList.size(); i++) {
                String temp = productionList.get(i);

                for (int j = 0; j < temp.length(); j++) {

                    for (int k = 0; k < keySet.size(); k++) {
                        if (keySet.get(k).equals(temp)) {

                            key = entry.getKey().toString();
                            List<String> productionValue = mapVariableProduction.get(temp);
                            productionList.remove(temp);

                            for (int l = 0; l < productionValue.size(); l++) {
                                mapVariableProduction.get(key).add(productionValue.get(l));
                            }
                        }
                    }
                }
            }
        }
    }

    private Boolean checkDuplicateInProductionList(Map<String, List<String>> map, String key) {

        Boolean notFound = true;

        Iterator itr = map.entrySet().iterator();
        outerloop:

        while (itr.hasNext()) {

            Map.Entry entry = (Map.Entry) itr.next();
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();

            for (int i = 0; i < productionList.size(); i++) {
                if (productionList.size() < 2) {

                    if (productionList.get(i).equals(key)) {
                        notFound = false;
                        break outerloop;
                    } else {
                        notFound = true;
                    }
                }
            }
        }

        return notFound;
    }

    private void onlyTwoTerminalandOneVariable() {

        //System.out.println("Assign new variable for two non-terminal or one terminal ... ");

        Iterator itr5 = mapVariableProduction.entrySet().iterator();
        String key = null;
        int asciiBegin = 84; //T

        Map<String, List<String>> tempList = new LinkedHashMap<>();

        while (itr5.hasNext()) {

            Map.Entry entry = (Map.Entry) itr5.next();
            Set set = mapVariableProduction.keySet();

            ArrayList<String> keySet = new ArrayList<String>(set);
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();
            Boolean found1 = false;
            Boolean found2 = false;
            Boolean found = false;


            for (int i = 0; i < productionList.size(); i++) {
                String temp = productionList.get(i);

                for (int j = 0; j < temp.length(); j++) {

                    if (temp.length() == 3) {

                        String newProduction = temp.substring(1, 3); // SA

                        if (checkDuplicateInProductionList(tempList, newProduction) && checkDuplicateInProductionList(mapVariableProduction, newProduction)) {
                            found = true;
                        } else {
                            found = false;
                        }

                        if (found) {

                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newProduction);
                            key = Character.toString((char) asciiBegin);

                            tempList.put(key, newVariable);
                            asciiBegin++;
                        }

                    } else if (temp.length() == 2) { // if only two substring

                        for (int k = 0; k < keySet.size(); k++) {

                            if (!keySet.get(k).equals(Character.toString(productionList.get(i).charAt(j)))) { // if substring not equals to keySet
                                found = false;

                            } else {
                                found = true;
                                break;
                            }

                        }

                        if (!found) {
                            String newProduction = Character.toString(productionList.get(i).charAt(j));

                            if (checkDuplicateInProductionList(tempList, newProduction) && checkDuplicateInProductionList(mapVariableProduction, newProduction)) {

                                ArrayList<String> newVariable = new ArrayList<>();
                                newVariable.add(newProduction);
                                key = Character.toString((char) asciiBegin);

                                tempList.put(key, newVariable);

                                asciiBegin++;

                            }
                        }
                    } else if (temp.length() == 4) {

                        String newProduction1 = temp.substring(0, 2); // SA
                        String newProduction2 = temp.substring(2, 4); // SA

                        if (checkDuplicateInProductionList(tempList, newProduction1) && checkDuplicateInProductionList(mapVariableProduction, newProduction1)) {
                            found1 = true;
                        } else {
                            found1 = false;
                        }

                        if (checkDuplicateInProductionList(tempList, newProduction2) && checkDuplicateInProductionList(mapVariableProduction, newProduction2)) {
                            found2 = true;
                        } else {
                            found2 = false;
                        }


                        if (found1) {

                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newProduction1);
                            key = Character.toString((char) asciiBegin);

                            tempList.put(key, newVariable);
                            asciiBegin++;
                        }

                        if (found2) {
                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newProduction2);
                            key = Character.toString((char) asciiBegin);

                            tempList.put(key, newVariable);
                            asciiBegin++;
                        }
                    }
                }
                /*for (List<String> keys :temp.){
                    if(keys.length()>2){
                        for(int i = 0; i<keys.length(); i++){
                            char a = keys.charAt(i);
                            System.out.println(a);
                        }
                    }
                }*/
            }

        }
        mapVariableProduction.putAll(tempList);
        //printMap();

    }

    private void removeThreeTerminal() {

        Iterator itr = mapVariableProduction.entrySet().iterator();
        ArrayList<String> keyList = new ArrayList<>();
        Iterator itr2 = mapVariableProduction.entrySet().iterator();

        // obtain key that use to eliminate two terminal and above
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            ArrayList<String> productionRow = (ArrayList<String>) entry.getValue();

            if (productionRow.size() < 2) {
                keyList.add(entry.getKey().toString());
            }
        }

        // find more than three terminal or combination of variable and terminal to
        // eliminate them
        while (itr2.hasNext()) {

            Map.Entry entry = (Map.Entry) itr2.next();
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();

            if (productionList.size() > 1) {
                for (int i = 0; i < productionList.size(); i++) {
                    String temp = productionList.get(i);

                    for (int j = 0; j < temp.length(); j++) {

                        if (temp.length() > 2) {
                            String stringToBeReplaced1 = temp.substring(j, temp.length());
                            String stringToBeReplaced2 = temp.substring(0, temp.length() - j);

                            for (String key : keyList) {

                                List<String> keyValues = new ArrayList<>();
                                keyValues = mapVariableProduction.get(key);
                                String[] values = keyValues.toArray(new String[keyValues.size()]);
                                String value = values[0];

                                if (stringToBeReplaced1.equals(value)) {

                                    mapVariableProduction.get(entry.getKey().toString()).remove(temp);
                                    temp = temp.replace(stringToBeReplaced1, key);

                                    if (!mapVariableProduction.get(entry.getKey().toString()).contains(temp)) {
                                        mapVariableProduction.get(entry.getKey().toString()).add(i, temp);
                                    }
                                } else if (stringToBeReplaced2.equals(value)) {

                                    mapVariableProduction.get(entry.getKey().toString()).remove(temp);
                                    temp = temp.replace(stringToBeReplaced2, key);

                                    if (!mapVariableProduction.get(entry.getKey().toString()).contains(temp)) {
                                        mapVariableProduction.get(entry.getKey().toString()).add(i, temp);
                                    }
                                }
                            }
                        } else if (temp.length() == 2) {

                            for (String key : keyList) {

                                List<String> keyValues = new ArrayList<>();
                                keyValues = mapVariableProduction.get(key);
                                String[] values = keyValues.toArray(new String[keyValues.size()]);
                                String value = values[0];

                                for (int pos = 0; pos < temp.length(); pos++) {
                                    String tempChar = Character.toString(temp.charAt(pos));

                                    if (value.equals(tempChar)) {

                                        mapVariableProduction.get(entry.getKey().toString()).remove(temp);
                                        temp = temp.replace(tempChar, key);

                                        if (!mapVariableProduction.get(entry.getKey().toString()).contains(temp)) {
                                            mapVariableProduction.get(entry.getKey().toString()).add(i, temp);
                                        }
                                    }
                                }
                            }
                        } else if (temp.length() == 1) {

                            for (String key : keyList) {

                                List<String> keyValues = new ArrayList<>();
                                keyValues = mapVariableProduction.get(key);
                                String[] values = keyValues.toArray(new String[keyValues.size()]);
                                String value = values[0];

                                if (value.equals(temp)) {

                                    mapVariableProduction.get(entry.getKey().toString()).remove(temp);
                                    temp = temp.replace(temp, key);

                                    if (!mapVariableProduction.get(entry.getKey().toString()).contains(temp)) {
                                        mapVariableProduction.get(entry.getKey().toString()).add(i, temp);
                                    }
                                }
                            }
                        }

                    }
                }
            } else if (productionList.size() == 1) {

                for (int i = 0; i < productionList.size(); i++) {
                    String temp = productionList.get(i);

                    if (temp.length() == 2) {

                        for (String key : keyList) {

                            List<String> keyValues = new ArrayList<>();
                            keyValues = mapVariableProduction.get(key);
                            String[] values = keyValues.toArray(new String[keyValues.size()]);
                            String value = values[0];

                            for (int pos = 0; pos < temp.length(); pos++) {
                                String tempChar = Character.toString(temp.charAt(pos));

                                if (value.equals(tempChar)) {

                                    mapVariableProduction.get(entry.getKey().toString()).remove(temp);
                                    temp = temp.replace(tempChar, key);

                                    if (!mapVariableProduction.get(entry.getKey().toString()).contains(temp)) {
                                        mapVariableProduction.get(entry.getKey().toString()).add(i, temp);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    public void print() {
        Set nonTerminals = mapVariableProduction.keySet();
        List<String> terminals = new ArrayList<String>();
        for (String key : mapVariableProduction.keySet()) {
            List<String> values = mapVariableProduction.get(key);
            for (String value : values) {
                if(value.length() == 1) {
                    if((Character.isLowerCase(value.charAt(0)) || Character.isDigit(value.charAt(0)))) {
                        terminals.add(value);
                    }
                }
            }
        }
        String nonTerminalText = "NON-TERMINAL\n";
        HashSet<String> hset1 = new HashSet<String>(nonTerminals);
        for (Object nonTerminal : hset1) {
            nonTerminalText += nonTerminal.toString() + "\n";
        }

        String terminalText = "TERMINAL\n";
        HashSet<String> hset = new HashSet<String>(terminals);
        for (String terminal : hset) {

            terminalText += terminal + "\n";
        }
        String rulesText = "RULES\n";
        for (String key : mapVariableProduction.keySet()) {
            for (String value : mapVariableProduction.get(key)) {
                rulesText += key + ":" + value + "\n";
            }
        }
        String firstElement = "START\n" + (String)nonTerminals.iterator().next();
        System.out.println(nonTerminalText + terminalText + rulesText + firstElement);

    }
    private void printMap() {

        Iterator it = mapVariableProduction.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " -> " + pair.getValue());
        }

        System.out.println(" ");
    }


}

