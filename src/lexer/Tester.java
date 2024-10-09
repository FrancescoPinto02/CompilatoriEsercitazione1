package lexer;

public class Tester {

    //TODO: Il file sorgente deve essere preso in input come parametro
    public static void main(String[] args) {

        Lexer lexer = new Lexer();
        String filePath = "file_input.txt";

        if (lexer.initialize(filePath)) {

            Token token;
            try {
                while ((token = lexer.nextToken()) != null) {
                    System.out.println(token);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else
            System.out.println("File not found!!");
    }

}
