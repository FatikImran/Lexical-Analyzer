import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class JFlexRunner {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java JFlexRunner <input-file>");
            return;
        }

        Path inputPath = Path.of(args[0]);
        String input = Files.readString(inputPath);
        Yylex lexer = new Yylex(new StringReader(input));

        Token token;
        while ((token = lexer.yylex()) != null) {
            if (token.getType() == TokenType.EOF) {
                break;
            }
            System.out.println(token);
        }
    }
}
