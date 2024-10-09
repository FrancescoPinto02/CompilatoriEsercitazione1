package lexer;

import java.io.*;
import java.util.HashMap;

public class Lexer {
    private FileInputStream fileInput;
    private HashMap<String, Token> stringTable;
    private static HashMap<String, Token> keywordTable;
    private int state;

    public Lexer() {
        stringTable = new HashMap<>();
        keywordTable = new HashMap<>();
        state = 0;
        initializeKeywordTable();
    }

    private void initializeKeywordTable(){
        keywordTable.put("if", new Token("IF"));
        keywordTable.put("then", new Token("THEN"));
        keywordTable.put("else", new Token("ELSE"));
        keywordTable.put("while", new Token("WHILE"));
        keywordTable.put("int", new Token("INT"));
        keywordTable.put("float", new Token("FLOAT"));
    }

    public Boolean initialize(String filePath){
        try {
            fileInput = new FileInputStream(filePath);
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //TODO: Bisogna gestire anche i separatori (fare riferimento alla tabella)
    public Token nextToken() throws Exception {
        state = 0;
        StringBuilder lexeme = new StringBuilder();
        char c;
        int intCharacter;

        while(true){
            intCharacter = fileInput.read();

            // End Of File
            if(intCharacter == -1){
                // Check if there's a token in progress and return it
                switch (state) {
                    case 4: // State for identifiers
                        return installID(lexeme.toString());
                    case 7: // State for integer numbers
                    case 9: // State for decimal numbers (after a dot)
                    case 12: // State for numbers (after exponent)
                        return new Token("NUM", lexeme.toString());
                    case 15: // State for operator '<'
                        return new Token("RELOP", "LT");
                    case 18: // State for operator '<='
                        return new Token("RELOP", "LE");
                    case 21: // State for operator '=='
                        return new Token("RELOP", "EQ");
                    case 23: // State for operator '!='
                        return new Token("RELOP", "NE");
                    case 25: // State for operator '>='
                        return new Token("RELOP", "GE");
                    case 26: // State for operator '>'
                        return new Token("RELOP", "GT");
                    default:
                        return null; // If no token is in progress, return null for EOF
                }
            } else{
                c = (char) intCharacter;
            }

            //############################### WHITESPACES #############################
            switch(state){
                case 0:
                    if(Character.isWhitespace(c)){
                        state = 1;
                    } else{
                        state = 3;
                    }
                    break;
                case 1:
                    if(Character.isWhitespace(c)){
                        state = 1;
                    } else{
                        state = 0;
                        retrack();
                        continue; //Consume all whitespaces
                    }
                    break;
            }
            //#########################################################################

            //############################# ID ########################################
            switch(state){
                case 3:
                    if(Character.isLetter(c) || c=='_') {
                        state = 4;
                        lexeme.append(c);
                    } else{
                        state = 6;
                    }
                    break;

                case 4:
                    if(Character.isLetterOrDigit(c) || c=='_') {
                        state = 4;
                        lexeme.append(c);
                    } else {
                        state = 5;
                        retrack();
                        return installID(lexeme.toString());
                    }
                    break;
            }
            //###########################################################################

            //################################## NUMBERS ################################
            switch (state){
                case 6:
                    if(Character.isDigit(c)){
                        state = 7;
                        lexeme.append(c);
                    } else {
                        state = 14;
                    }
                    break;

                case 7:
                    if(Character.isDigit(c)){
                        state = 7;
                        lexeme.append(c);
                    } else if(c == '.'){
                        state = 8;
                        lexeme.append(c);
                    } else {
                        state = 13;
                        retrack();
                        return new Token ("NUM", lexeme.toString());
                    }
                    break;

                case 8:
                    if(Character.isDigit(c)){
                        state = 9;
                        lexeme.append(c);
                    } else {
                        throw new Exception("Error: Token not recognized!");
                    }
                    break;

                case 9:
                    if(Character.isDigit(c)){
                        state = 9;
                        lexeme.append(c);
                    } else if(c == 'E'){
                        state = 10;
                        lexeme.append(c);
                    } else {
                        state = 13;
                        retrack();
                        return new Token("NUM", lexeme.toString());
                    }
                    break;

                case 10:
                    if(c == '+' || c=='-'){
                        state = 11;
                        lexeme.append(c);
                    } else {
                        throw new Exception("Error: Token not recognized!");
                    }
                    break;

                case 11:
                    if(Character.isDigit(c)){
                        state = 12;
                        lexeme.append(c);
                    } else {
                        throw new Exception("Error: Token not recognized!");
                    }
                    break;

                case 12:
                    if(Character.isDigit(c)){
                        state = 12;
                        lexeme.append(c);
                    } else {
                        state = 13;
                        retrack();
                        return new Token("NUM", lexeme.toString());
                    }
                    break;
            }
            //####################################################################################

            //################################## OPERATORS #######################################
            switch(state){
                case 14:
                    if(c == '<'){
                        state = 15;
                        lexeme.append(c);
                    } else if(c == '='){
                        state = 20;
                        lexeme.append(c);
                    } else if (c == '!') {
                        state = 22;
                        lexeme.append(c);
                    } else if (c == '>') {
                        state = 24;
                        lexeme.append(c);
                    }else{
                        // There are no more Pattern to match
                        throw new Exception("Error: Token not recognized!");
                    }
                    break;

                case 15:
                    if(c == '-'){
                        state = 16;
                        lexeme.append(c);
                    }else if (c == '='){
                        state = 18;
                        lexeme.append(c);
                        return new Token("RELOP", "LE");
                    }else{
                        state = 19;
                        retrack();
                        return new Token("RELOP", "LT");
                    }
                    break;

                case 16:
                    if(c == '-'){
                        state = 17;
                        lexeme.append(c);
                        return new Token("ASSIGN");
                    }else {
                        throw new Exception("Error: Token not recognized!");
                    }

                case 20:
                    if(c == '='){
                        state = 21;
                        lexeme.append(c);
                        return new Token("RELOP", "EQ");
                    }else{
                        throw new Exception("Error: Token not recognized!");
                    }

                case 22:
                    if(c == '='){
                        state = 23;
                        lexeme.append(c);
                        return new Token("RELOP", "NE");
                    }else{
                        throw new Exception("Error: Token not recognized!");
                    }

                case 24:
                    if(c == '='){
                        state = 25;
                        lexeme.append(c);
                        return new Token("RELOP", "GE");
                    }else{
                        state = 26;
                        retrack();
                        return new Token("RELOP", "GT");
                    }
            }
            //########################################################################
        }
    }

    private Token installID(String lexeme){
        Token token;

        if(keywordTable.containsKey(lexeme))
            return keywordTable.get(lexeme);
        else{
            token =  new Token("ID", lexeme);
            stringTable.put(lexeme, token);
            return token;
        }
    }

    private void retrack() throws IOException {
        if (fileInput != null && fileInput.getChannel().position() > 0) {
            fileInput.getChannel().position(fileInput.getChannel().position() - 1);
        }
    }
}
