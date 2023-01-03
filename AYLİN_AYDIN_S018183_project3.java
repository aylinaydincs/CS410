import java.io.*;
import java.util.*;

import java.util.ArrayList;

class Transitions {
    String transitions;

    public Transitions(String transitions){
        this.transitions = transitions;
    }

    public String getTransitions() {
        return transitions;
    }

    public String toString()
    {
        return(this.transitions);
    }
}

class Tape {

    private static String BLANK;
    private static int EDGE = 0;
    private int pointer = 0;
    private LinkedList<String> tape;
    private int origin = EDGE;

    public Tape(String s,String blank) {
        BLANK = blank;
        //The string tokenizer class allows an application to break a string into tokens.
        StringTokenizer stok = new StringTokenizer(s);
        tape = new LinkedList<String>();

        int i = -1;
        for (i = 0; i < EDGE; i++) {
            tape.add(BLANK);
        }
        //The hasMoreTokens() method of StringTokenizer class checks whether there are any more tokens available
        for (i = EDGE; stok.hasMoreTokens(); i++) {
            String symbol = stok.nextToken();
            if (symbol.startsWith("[") && symbol.endsWith("]")) {
                pointer = i;
                tape.add(symbol.substring(1, symbol.length() - 1));
            }
            else {
                tape.add(symbol);
            }
        }
        for (int len = i; i < len + EDGE; i++) {
            tape.add(BLANK);
        }
    }
    public String getCurrentSymbol() {
        return tape.get(pointer);
    }
    public int getSize() {
        return tape.size();
    }
    public void deleteCell() {
        tape.remove(pointer);
        if (pointer != 0)
            pointer--;
        else
            origin--;
    }
    public void shiftLeft() {
        if (pointer == 0) {
            addFirst(BLANK);
        } else {
            pointer--;
        }
    }
    public void addFirst(String s) {
        origin++;
        tape.addFirst(s);
    }
    public void shiftRight() {
        if (pointer == tape.size() - 1) {
            addLast(BLANK);
        }
        pointer++;
    }
    public void addLast(String s) {
        tape.addLast(s);
    }
    public void writeSymbol(String symbol) {
        if (!(tape.get(pointer)).equals(symbol)) {
            tape.remove(pointer);
            tape.add(pointer, symbol);
        }
    }

}



public class TuringMachine {
    public static void main(String[] args) {
        int numVar = 0;
        int numTape = 0;
        ArrayList tapeAlphabet = new ArrayList();
        String blankSymbol = "";
        int numStates = 0;

        String startState = "";
        String acceptState = "";
        String rejectState = "";

        ArrayList states = new ArrayList();
        ArrayList detected = new ArrayList();
        ArrayList var = new ArrayList();
        ArrayList <Transitions> transitions = new ArrayList<Transitions>();
        ArrayList skippedRoute = new ArrayList();

        String fileName = "input2.txt";
        List<String> input = new ArrayList<>();
        try {
            File Inputfile = new File(fileName);
            Scanner myReader = new Scanner(Inputfile);
            while (myReader.hasNextLine()) {
                //file contains all characters in the input file
                input.add(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        }

        for (int i = 0; i < input.size(); i++) {
            String inputLines = input.get(i);
            String arr[] = inputLines.split(" ", 2);
            if(inputLines.contains("number of variables in input alphabet")){
                numVar = Integer.parseInt(arr[0]);
            }
            if(inputLines.contains("number of variables in tape alphabet")){
                numTape = Integer.parseInt(arr[0]);
            }
            if(inputLines.contains("number of states")){
                numStates = Integer.parseInt(arr[0]);
            }
            if(inputLines.contains("blank symbol")) {
                blankSymbol = arr[0];
            }
            if(inputLines.contains("start state")) {
                startState = arr[0];
            }
            if(inputLines.contains("accept state")){
                acceptState = arr[0];
            }
            if(inputLines.contains("reject state")) {
                rejectState = arr[0];
            }
            if(inputLines.contains("string to be detected")) {
                detected.add(arr[0]);
            }
        }
        for (int i = 0; i < input.size(); i++) {
            String inputLines = input.get(i);
            if(inputLines.contains("(the input alphabet)")){
                String arr[] = inputLines.split(" ", numVar + 1);
                for (int j = 0; j < numVar; j++) {
                    var.add(arr[j]);
                }
            }
            if(inputLines.contains("(the tape alphabet)")){
                String arr[] = inputLines.split(" ", numTape + 1);
                for (int j = 0; j < numTape; j++) {
                    tapeAlphabet.add(arr[j]);
                }
            }
            if(inputLines.contains("(states)")){
                String arr[] = inputLines.split(" ", numStates + 1);
                for (int j = 0; j < numStates; j++) {
                    states.add(arr[j]);
                }
            }

            Transitions transition = new Transitions(inputLines);
            transitions.add(transition);
        }
        for (int x = 0; x < detected.size(); x++) {
            ArrayList route = new ArrayList();
            route.add(startState);
            String firstWord = detected.get(x).toString();
            String arr[] = firstWord.split("(?!^)"); //it matches all 0-length strings except that at the start of the string
            Tape tape = new Tape(arr[0],blankSymbol);
            for (int i = 1; i < arr.length; i++) {
                tape.addLast(arr[i]);
            }
            if(arr[0].equals("")){
                for (int i = 0; i < tape.getSize(); i++) {
                    tape.deleteCell();
                }
                tape.addLast("b");
            }

            int checkPoint=0;
            int countLetter =0;
            while(checkPoint==0){
                for (int j = 0; j < transitions.size(); j++) {
                    String trans = transitions.get(j).getTransitions();
                    String fromState = (String) route.get(countLetter);

                    if(fromState.trim().equals(acceptState.trim())||fromState.trim().equals(rejectState.trim())){
                        checkPoint=1;
                    }

                    if (trans.contains(fromState.trim() + " " + tape.getCurrentSymbol().trim())) {
                        String my[] = trans.split(" ", 10);
                        if(tape.getCurrentSymbol().equals(my[1].trim())){
                            tape.writeSymbol(my[2].trim());
                            //right
                            if(my[3].trim().equals("R")){
                                tape.shiftRight();
                            }
                            //left
                            else if(my[3].trim().equals("L")){
                                tape.shiftLeft();
                            }
                            route.add(my[4].trim());
                            countLetter++;
                            break;
                        }
                    }
                }
            }

            String myRoute ="";
            for (int i = 0; i < route.size(); i++) {
                myRoute = myRoute+" "+route.get(i);
            }
            skippedRoute.add(myRoute);
        }

        String [] result = new String[detected.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = "rejected";
        }
        for (int i = 0; i < skippedRoute.size(); i++) {
            String lastLetter = skippedRoute.get(i).toString();
            String ısIt;
            if(lastLetter.length()>3){
                ısIt = lastLetter.substring(lastLetter.length() - 2);
            }
            //only for the routes which have only one state
            else{
                ısIt = lastLetter.substring(lastLetter.length()-3,lastLetter.length() - 1);;
            }

            if(ısIt.equals(acceptState)){
                result[i] = "accepted";
            }
        }

        String outputFile ="";

        for (int i = 0; i <skippedRoute.size() ; i++) {
            outputFile= outputFile + skippedRoute.get(i);
            System.out.println("------------------------------------------------");
            System.out.println("ROUT : " + outputFile);
            System.out.println("RESULT : " + result[i]);
            System.out.println("------------------------------------------------");
        }

    }
}