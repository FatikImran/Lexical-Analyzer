import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java Main <input-file>");
            return;
        }

        Path inputPath = Path.of(args[0]);
        String input = Files.readString(inputPath);

        ErrorHandler errorHandler = new ErrorHandler();
        SymbolTable symbolTable = new SymbolTable();
        ManualScanner scanner = new ManualScanner(input, errorHandler, symbolTable);

        List<Token> tokens = scanner.scanAll();
        for (Token token : tokens) {
            if (token.getType() != TokenType.EOF) {
                System.out.println(token);
            }
        }

        printStats(scanner);
        printSymbolTable(symbolTable);
        printErrors(errorHandler);
    }

    private static void printStats(ManualScanner scanner) {
        System.out.println("\nStatistics:");
        System.out.println("Total tokens: " + scanner.getTotalTokens());
        System.out.println("Lines processed: " + scanner.getLinesProcessed());
        System.out.println("Comments removed: " + scanner.getCommentsRemoved());
        for (Map.Entry<TokenType, Integer> entry : scanner.getTokenCounts().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static void printSymbolTable(SymbolTable symbolTable) {
        if (symbolTable.getEntries().isEmpty()) {
            return;
        }
        System.out.println("\nSymbol Table:");
        for (SymbolTable.Entry entry : symbolTable.getEntries().values()) {
            System.out.println(entry.getName() + " | type=" + entry.getType()
                    + " | first=" + entry.getFirstLine() + ":" + entry.getFirstColumn()
                    + " | freq=" + entry.getFrequency());
        }
    }

    private static void printErrors(ErrorHandler errorHandler) {
        if (!errorHandler.hasErrors()) {
            return;
        }
        System.out.println("\nErrors:");
        for (ErrorHandler.ErrorEntry entry : errorHandler.getErrors()) {
            System.out.println(entry);
        }
    }
}
