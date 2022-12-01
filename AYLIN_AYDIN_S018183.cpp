#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector> //for use in State Moves
#include <map> //for use in State Table
#include <stack>

using namespace std;

struct DFAState {
    bool marked;
    std::vector<string> states;
    std::map<string,string> moves;
};

typedef std::map<string , DFAState> DFATableType;
typedef std::map<string, std::map<string, std::vector<string>>> NFATableType;

string INIT_STATE;
int TOTAL_STATES;
std::vector<string> FINAL_STATES;
std::vector<string> ALPHABET;
std::vector<string> STATES;
NFATableType STATE_TABLE;
DFATableType DFA_STATE_TABLE;
std::stack<string> STACK;
std::vector<string> DFA_STATES;

void printVector(std::vector<string> vec){

    for(std::vector<string>::const_iterator i=vec.begin(); i != vec.end(); i++){
        if(i != vec.end()-1){
            std::cout << *i << " ";
        }
        else{
            std::cout << *i;
        }
    }
}

bool doesVectorContain(std::vector<string> vec, string key){
    for(std::vector<string>::const_iterator k = vec.begin(); k != vec.end(); k++){
        if(*k == key){
            return true;
        }
    }
    return false;
}

void readFile(std::string filename) {

    std::string line;
    std::ifstream myfile(filename);

    if(myfile.is_open()) {

        std::getline(myfile, line);

        /*************************************
        * GET ALPHABET
        *************************************/
        if(line == "ALPHABET"){
            std::getline(myfile, line);
            while(line != "STATES"){
                std::istringstream iss(line);
                string alp;
                iss >> alp;
                ALPHABET.push_back(alp);
                std::getline(myfile, line);
            }
        }
        /*cout << "ALPHABET : " ;
        printVector(ALPHABET);*/
        /*************************************
        * GET STATES
        *************************************/
        if(line == "STATES"){
            std::getline(myfile, line);
            while(line != "START"){
                std::istringstream iss(line);
                string alp;
                iss >> alp;
                STATES.push_back(alp);
                std::getline(myfile, line);
            }
        }
        TOTAL_STATES = STATES.size();
        /*cout << "STATES : " ;
        printVector(STATES);
        cout << "TOTAL STATES : " <<TOTAL_STATES << " ";*/

        /*************************************
        * GET START STATES
        *************************************/

        if(line == "START"){
            std::getline(myfile, line);
            std::istringstream iss(line);
            string alp;
            iss >> alp;
            INIT_STATE = alp;
        }
        //cout << "INITIAL STATES : " << INIT_STATE << " ";

        /*************************************
        * GET FINAL STATES
        *************************************/

        std::getline(myfile, line);
        if(line == "FINAL"){
            std::getline(myfile, line);
            while(line != "TRANSITIONS"){
                std::istringstream iss(line);
                string alp;
                iss >> alp;
                FINAL_STATES.push_back(alp);
                std::getline(myfile, line);
            }
        }
        /*cout << "FINAL STATES : " ;
        printVector(FINAL_STATES);
        cout << line;*/

        /*************************************
        * INITIALIZE NFA STATES
        *************************************/
        if(line == "TRANSITIONS"){
            std::getline(myfile, line);
            while(line!="END"){
                std::istringstream iss(line);
                string from;
                iss >> from;
                string with;
                iss >> with;
                string to;
                iss >> to;

                //STATE_TABLE.insert(make_pair(from,map<char,vector<char>>()));
                STATE_TABLE[from][with].push_back(to);

                //print(STATE_TABLE[from][with]);
                //cout << "from : "<< from << " with : " << with << " to : " << to << " " ;
                std::getline(myfile, line);
            }
        }
    } //End If My File is_open();

} //End ReadFile()

void NFA2DFA(string STRAT_STATE,std::stack<string> stack,NFATableType &NFATable, DFATableType &DFATable){
    stack.push(STRAT_STATE);
    DFAState init;
    init.marked = false;
    init.states.push_back(STRAT_STATE);
    DFA_STATES.push_back(STRAT_STATE);
    DFATable[STRAT_STATE] = init;

    while(!stack.empty()){
        string state = stack.top();
        stack.pop();
        DFATable[state].marked = true;
        for(std::vector<string>::const_iterator i=ALPHABET.begin(); i != ALPHABET.end(); i++){
            string symbol = *i; //symbol = 0 or 1
            std::vector<string> toStates;
            for(std::vector<string>::const_iterator j=DFATable[state].states.begin(); j != DFATable[state].states.end(); j++){
                string fromState = *j; //fromState = q0 or q1
                for(std::vector<string>::const_iterator k=NFATable[fromState][symbol].begin(); k != NFATable[fromState][symbol].end(); k++){
                    string toState = *k; //toState = q0 or q1
                    toStates.push_back(toState);
                }
            }
            if(toStates.size() > 0){
                string toState = toStates[0];
                for(std::vector<string>::const_iterator j=toStates.begin(); j != toStates.end(); j++){
                    if(toState != *j){
                        toState = toState + *j;
                    }
                }
                DFATable[state].moves[symbol] = toState;
                if(DFATable.find(toState) == DFATable.end()){
                    DFAState newState;
                    newState.marked = false;
                    newState.states = toStates;
                    DFATable[toState] = newState;
                    stack.push(toState);
                    DFA_STATES.push_back(toState);
                    //NFA2DFA(toState,stack,NFATable,DFATable);
                    //break;
                }
            }

        }
    }
}

std::vector<string> findFinalDFAStates(DFATableType DFATable, std::vector<string> finalStates){
    std::vector<string> finals;
    int n = 0;
    for(std::vector<DFATableType ::value_type> w(DFA_STATE_TABLE.begin(),DFA_STATE_TABLE.end()); n < w.size(); n++){
        for(std::vector<string>::const_iterator k = finalStates.begin(); k != finalStates.end(); k++){
            if( doesVectorContain(DFATable[w[n].first].states, *k) ){
                finals.push_back(w[n].first);
            }
        }
    }
    return finals;
}

void printDFA(string init, vector<string> final, DFATableType &DFATable){
    std::cout << "ALPHABET "<< std::endl;
    printVector(ALPHABET);
    std::cout << std::endl;
    std::cout << "STATES "<< std::endl;
    printVector(DFA_STATES);
    std::cout << std::endl;
    std::cout << "START STATE "  << std::endl;
    std::cout <<  init << std::endl;
    std::cout << "FINAL STATE " << std::endl;
    printVector(final);
    std::cout << std::endl;
    std::cout << "TRANSITIONS " << std::endl;
    int n = 0;
    for(std::vector<DFATableType ::value_type> w(DFA_STATE_TABLE.begin(),DFA_STATE_TABLE.end()); n < w.size(); n++){
        int m = 0;
        for (std::vector<std::map<string,string>::value_type> x(w[n].second.moves.begin(),w[n].second.moves.end());
             m < x.size(); m++) {
            cout<< w[n].first << " "<<x[m].first << " "<< x[m].second<< endl;

        }
    }
    std::cout << "END " << std::endl;
}

int main() {
    string file = "C:\\Users\\aylin.LAPTOP-TJ9MCDMK\\CLionProjects\\410_project1\\NFA1.txt";
    readFile(file);

    NFA2DFA(INIT_STATE,STACK,STATE_TABLE,DFA_STATE_TABLE);
    vector<string> FINAL_STATE = findFinalDFAStates(DFA_STATE_TABLE,FINAL_STATES);
    printDFA(INIT_STATE,FINAL_STATE,DFA_STATE_TABLE);

    /*int i = 0;
    for(std::vector<NFATableType ::value_type> v(STATE_TABLE.begin(),
                                                 STATE_TABLE.end()); i < v.size(); i++){
        std::cout<< "STATE : " << v[i].first << " " << endl;
        int j = 0;
        for (std::vector<std::map<string, std::vector<string>>::value_type> n(v[i].second.begin(),v[i].second.end());
             j < n.size(); j++) {

            cout<< "Move : " <<n[j].first << " ";
            printVector(n[j].second);
            cout << endl;
        }
    }*/
    //std::cout << "DFA STATE TABLE : " << endl;
    /* int n = 0;
    for(std::vector<DFATableType ::value_type> w(DFA_STATE_TABLE.begin(),
                                                 DFA_STATE_TABLE.end()); n < w.size(); n++){
        std::cout<< "STATE : " << w[n].first << " " << endl;
        //std::cout<< "Moves : " ;
        //printVector(w[n].second.states);
        std::cout<< " " << endl;


        int m = 0;
        for (std::vector<std::map<string,string>::value_type> x(w[n].second.moves.begin(),w[n].second.moves.end());
             m < x.size(); m++) {

            cout<< "Move : " <<x[m].first << " "<< x[m].second << endl;

            cout << endl;
        }
    }*/
    return 0;
}
