package lexer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.HashMap;
import java.util.Hashtable;

public class Lexer {
    private File input;
    private HashMap<String, Token> stringTable;
    private static HashMap<String, Token> keywordTable;
    private int state;
    private PushbackReader reader;

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
        input = new File(filePath);
        try{
            reader = new PushbackReader(new FileReader(filePath), 1);
        }catch (Exception e){
            //TODO migliorare gestione errori
            System.out.println("Errore Apertura file" + e);
            return false;
        }
        return true;
    }

    public Token nextToken() throws Exception {
        state = 0;
        String lexem = "";
        char c;
        int intCharacter;

        while(true){
            intCharacter = reader.read();
            if(intCharacter == -1){
                throw new Exception("File Terminato");
            }
            else{
                c = (char) intCharacter;
            }


            switch(state){
                case 0:
                    if(Character.isWhitespace(c)){
                        state = 1;
                    }
                    else{
                        state = 3;
                    }
                    break;
                case 1:
                    if(Character.isWhitespace(c)){
                        state = 1;
                        break;
                    }
                    else{
                        state = 2;
                        retrack();
                        continue;
                    }
            }

            //ID
            switch(state){
                case 3:
                    if(Character.isLetter(c) || c=='_') {
                        state = 4;
                        lexem += c;
                    }
                    else{
                        state = 6;
                    }
                    break;

                case 4:
                    if(Character.isLetter(c) || c=='_' || Character.isDigit(c)) {
                        state = 4;
                        lexem += c;
                    }
                    else {
                        state = 5;
                        retrack();
                        return installID(lexem);
                    }
                    break;
            }

            // Number
            switch (state){
                case 6:
                    if(Character.isDigit(c)){
                        state = 7;
                        lexem += c;
                    }
                    else {
                        state = 14;
                    }
                    break;

                case 7:
                    if(Character.isDigit(c)){
                        state = 7;
                        lexem += c;
                    }
                    else {
                        return new Token ("NUM", lexem);
                    }
                    break;
            }

            return new Token("ERROR");

        }
    }

    private Token installID(String lexem){
        Token token;

        if(keywordTable.containsKey(lexem))
            return keywordTable.get(lexem);
        else{
            token =  new Token("ID", lexem);
            stringTable.put(lexem, token);
            return token;
        }
    }

    private void retrack() throws IOException {
        reader.unread(1);
    }
}
