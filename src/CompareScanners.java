import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CompareScanners {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java CompareScanners <input-file>");
            return;
        }

        Path inputPath = Path.of(args[0]);
        String input = Files.readString(inputPath);

        ErrorHandler errorHandler = new ErrorHandler();
        SymbolTable symbolTable = new SymbolTable();
        ManualScanner manual = new ManualScanner(input, errorHandler, symbolTable);
        List<Token> manualTokens = manual.scanAll();

        Yylex lexer = new Yylex(new StringReader(input));
        List<Token> jflexTokens = new ArrayList<>();
        Token token;
        while ((token = lexer.yylex()) != null) {
            jflexTokens.add(token);
            if (token.getType() == TokenType.EOF) {
                break;
            }
        }

        compareTokens(manualTokens, jflexTokens);
    }

    private static void compareTokens(List<Token> manualTokens, List<Token> jflexTokens) {
        int max = Math.max(manualTokens.size(), jflexTokens.size());
        boolean mismatch = false;
        for (int i = 0; i < max; i++) {
            Token manual = i < manualTokens.size() ? manualTokens.get(i) : null;
            Token jflex = i < jflexTokens.size() ? jflexTokens.get(i) : null;
            if (!tokenEquals(manual, jflex)) {
                mismatch = true;
                System.out.println("Mismatch at index " + i + ":");
                System.out.println("  Manual: " + (manual == null ? "<none>" : manual));
                System.out.println("  JFlex:  " + (jflex == null ? "<none>" : jflex));
                break;
            }
        }

        if (!mismatch) {
            System.out.println("Manual scanner and JFlex outputs match.");
        }
    }

    private static boolean tokenEquals(Token a, Token b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getType() == b.getType()
                && a.getLexeme().equals(b.getLexeme())
                && a.getLine() == b.getLine()
                && a.getColumn() == b.getColumn();
    }
}
