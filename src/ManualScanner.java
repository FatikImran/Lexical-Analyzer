import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManualScanner {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "start", "finish", "loop", "condition", "declare", "output", "input",
            "function", "return", "break", "continue", "else"
    ));
    private static final Set<String> BOOLEAN_LITERALS = new HashSet<>(Arrays.asList("true", "false"));

    private final String input;
    private final ErrorHandler errorHandler;
    private final SymbolTable symbolTable;
    private final Map<TokenType, Integer> tokenCounts = new EnumMap<>(TokenType.class);

    private int index = 0;
    private int line = 1;
    private int column = 1;
    private int totalTokens = 0;
    private int commentsRemoved = 0;

    public ManualScanner(String input, ErrorHandler errorHandler, SymbolTable symbolTable) {
        this.input = input;
        this.errorHandler = errorHandler;
        this.symbolTable = symbolTable;
    }

    public List<Token> scanAll() {
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = nextToken();
            tokens.add(token);
        } while (token.getType() != TokenType.EOF);
        return tokens;
    }

    public Token nextToken() {
        while (true) {
            if (index >= input.length()) {
                return new Token(TokenType.EOF, "", line, column);
            }

            Token token = scanToken();
            if (token == null) {
                continue;
            }
            recordToken(token);
            return token;
        }
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public int getCommentsRemoved() {
        return commentsRemoved;
    }

    public int getLinesProcessed() {
        return line;
    }

    public Map<TokenType, Integer> getTokenCounts() {
        return Collections.unmodifiableMap(tokenCounts);
    }

    private Token scanToken() {
        if (matchWhitespace()) {
            return null;
        }

        int startLine = line;
        int startColumn = column;

        if (startsWith("#*")) {
            Token commentToken = consumeMultiLineComment(startLine, startColumn);
            if (commentToken != null) {
                return commentToken;
            }
            commentsRemoved++;
            return null;
        }

        if (matchSingleLineComment()) {
            commentsRemoved++;
            return null;
        }

        Token token = matchMultiCharOperator(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchKeyword(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchBooleanLiteral(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchInvalidIdentifier(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchInvalidUpperIdentifier(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchIdentifier(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchFloatLiteral(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchIntLiteral(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchStringLiteral(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchCharLiteral(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchSingleCharOperator(startLine, startColumn);
        if (token != null) {
            return token;
        }

        token = matchPunctuator(startLine, startColumn);
        if (token != null) {
            return token;
        }

        char current = input.charAt(index);
        advance(1);
        errorHandler.report("InvalidCharacter", startLine, startColumn, String.valueOf(current),
                "Unrecognized character");
        return new Token(TokenType.ERROR, String.valueOf(current), startLine, startColumn);
    }

    private void advance(int count) {
        for (int i = 0; i < count; i++) {
            if (index >= input.length()) {
                return;
            }
            char c = input.charAt(index);
            index++;
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
    }

    private Token matchKeyword(int startLine, int startColumn) {
        for (String keyword : KEYWORDS) {
            if (startsWith(keyword) && !isWordChar(peek(keyword.length()))) {
                advance(keyword.length());
                return new Token(TokenType.KEYWORD, keyword, startLine, startColumn);
            }
        }
        return null;
    }

    private Token matchBooleanLiteral(int startLine, int startColumn) {
        for (String literal : BOOLEAN_LITERALS) {
            if (startsWith(literal) && !isWordChar(peek(literal.length()))) {
                advance(literal.length());
                return new Token(TokenType.BOOLEAN_LITERAL, literal, startLine, startColumn);
            }
        }
        return null;
    }

    private Token matchIdentifier(int startLine, int startColumn) {
        if (!isUpper(peek())) {
            return null;
        }

        int startIndex = index;
        advance(1);
        int count = 1;
        while (count < 31 && isIdentifierBody(peek())) {
            advance(1);
            count++;
        }

        if (isIdentifierBody(peek())) {
            while (isIdentifierBody(peek())) {
                advance(1);
                count++;
            }
            String lexeme = input.substring(startIndex, index);
            errorHandler.report("InvalidIdentifier", startLine, startColumn, lexeme,
                    "Identifier length exceeds 31 characters");
            return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
        }

        String lexeme = input.substring(startIndex, index);
        symbolTable.addOrUpdate(lexeme, "", startLine, startColumn);
        return new Token(TokenType.IDENTIFIER, lexeme, startLine, startColumn);
    }

    private Token matchInvalidIdentifier(int startLine, int startColumn) {
        if (!isLower(peek())) {
            return null;
        }

        int startIndex = index;
        advance(1);
        while (isWordChar(peek())) {
            advance(1);
        }
        String lexeme = input.substring(startIndex, index);
        errorHandler.report("InvalidIdentifier", startLine, startColumn, lexeme,
                "Identifier must start with an uppercase letter");
        return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
    }

    private Token matchInvalidUpperIdentifier(int startLine, int startColumn) {
        if (!isUpper(peek())) {
            return null;
        }

        int i = index + 1;
        boolean seenUpper = false;
        while (isWordChar(peek(i - index))) {
            if (isUpper(peek(i - index))) {
                seenUpper = true;
            }
            i++;
        }

        if (!seenUpper) {
            return null;
        }

        String lexeme = input.substring(index, i);
        advance(i - index);
        errorHandler.report("InvalidIdentifier", startLine, startColumn, lexeme,
                "Identifier body contains uppercase letters");
        return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
    }

    private Token matchFloatLiteral(int startLine, int startColumn) {
        int startIndex = index;
        int i = index;
        if (isSign(peek()) && isDigit(peek(1))) {
            i++;
        }
        if (!isDigit(peek(i - index))) {
            return null;
        }

        int digitsBefore = 0;
        while (isDigit(peek(i - index))) {
            i++;
            digitsBefore++;
        }

        if (peek(i - index) != '.') {
            return null;
        }

        i++;
        int digitsAfter = 0;
        while (isDigit(peek(i - index))) {
            i++;
            digitsAfter++;
        }

        boolean malformed = false;
        String reason = "";
        if (digitsAfter == 0) {
            malformed = true;
            reason = "Missing digits after decimal point";
        } else if (digitsAfter > 6) {
            malformed = true;
            reason = "Too many digits after decimal point";
        }

        if (peek(i - index) == 'e' || peek(i - index) == 'E') {
            int expStart = i;
            i++;
            if (isSign(peek(i - index))) {
                i++;
            }
            int expDigits = 0;
            while (isDigit(peek(i - index))) {
                i++;
                expDigits++;
            }
            if (expDigits == 0) {
                malformed = true;
                reason = "Malformed exponent in floating literal";
                i = expStart + 1;
            }
        }

        String lexeme = input.substring(startIndex, i);
        advance(i - startIndex);

        if (malformed) {
            errorHandler.report("MalformedLiteral", startLine, startColumn, lexeme, reason);
            return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
        }

        return new Token(TokenType.FLOAT_LITERAL, lexeme, startLine, startColumn);
    }

    private Token matchIntLiteral(int startLine, int startColumn) {
        if (!(isDigit(peek()) || (isSign(peek()) && isDigit(peek(1))))) {
            return null;
        }

        int startIndex = index;
        if (isSign(peek())) {
            advance(1);
        }
        while (isDigit(peek())) {
            advance(1);
        }

        String lexeme = input.substring(startIndex, index);
        return new Token(TokenType.INT_LITERAL, lexeme, startLine, startColumn);
    }

    private Token matchStringLiteral(int startLine, int startColumn) {
        if (peek() != '"') {
            return null;
        }

        int startIndex = index;
        advance(1);
        boolean invalidEscape = false;
        while (!isAtEnd()) {
            char c = peek();
            if (c == '\n' || c == '\r') {
                String lexeme = input.substring(startIndex, index);
                errorHandler.report("MalformedLiteral", startLine, startColumn, lexeme,
                        "Unterminated string literal");
                return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
            }
            if (c == '"') {
                advance(1);
                String lexeme = input.substring(startIndex, index);
                if (invalidEscape) {
                    errorHandler.report("MalformedLiteral", startLine, startColumn, lexeme,
                            "Invalid escape sequence in string literal");
                    return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
                }
                return new Token(TokenType.STRING_LITERAL, lexeme, startLine, startColumn);
            }
            if (c == '\\') {
                advance(1);
                char next = peek();
                if (!(next == '"' || next == '\\' || next == 'n' || next == 't' || next == 'r')) {
                    invalidEscape = true;
                }
                advance(1);
                continue;
            }
            advance(1);
        }

        String lexeme = input.substring(startIndex, index);
        errorHandler.report("MalformedLiteral", startLine, startColumn, lexeme,
                "Unterminated string literal");
        return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
    }

    private Token matchCharLiteral(int startLine, int startColumn) {
        if (peek() != '\'') {
            return null;
        }

        int startIndex = index;
        advance(1);
        if (isAtEnd() || peek() == '\n' || peek() == '\r') {
            String lexeme = input.substring(startIndex, index);
            errorHandler.report("MalformedLiteral", startLine, startColumn, lexeme,
                    "Unterminated character literal");
            return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
        }

        boolean invalidEscape = false;
        if (peek() == '\\') {
            advance(1);
            char next = peek();
            if (!(next == '\'' || next == '\\' || next == 'n' || next == 't' || next == 'r')) {
                invalidEscape = true;
            }
            advance(1);
        } else {
            advance(1);
        }

        if (peek() != '\'') {
            while (!isAtEnd() && peek() != '\n' && peek() != '\r' && peek() != '\'') {
                advance(1);
            }
            String lexeme = input.substring(startIndex, index);
            errorHandler.report("MalformedLiteral", startLine, startColumn, lexeme,
                    "Invalid character literal");
            return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
        }

        advance(1);
        String lexeme = input.substring(startIndex, index);
        if (invalidEscape) {
            errorHandler.report("MalformedLiteral", startLine, startColumn, lexeme,
                    "Invalid escape sequence in character literal");
            return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
        }
        return new Token(TokenType.CHAR_LITERAL, lexeme, startLine, startColumn);
    }

    private Token matchMultiCharOperator(int startLine, int startColumn) {
        String[] operators = {"**", "==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "*=", "/="};
        for (String op : operators) {
            if (startsWith(op)) {
                advance(op.length());
                return new Token(resolveOperatorType(op), op, startLine, startColumn);
            }
        }
        return null;
    }

    private Token matchSingleCharOperator(int startLine, int startColumn) {
        char c = peek();
        switch (c) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '%':
                advance(1);
                return new Token(TokenType.OP_ARITHMETIC, String.valueOf(c), startLine, startColumn);
            case '<':
            case '>':
                advance(1);
                return new Token(TokenType.OP_RELATIONAL, String.valueOf(c), startLine, startColumn);
            case '!':
                advance(1);
                return new Token(TokenType.OP_LOGICAL, String.valueOf(c), startLine, startColumn);
            case '=':
                advance(1);
                return new Token(TokenType.OP_ASSIGNMENT, String.valueOf(c), startLine, startColumn);
            default:
                return null;
        }
    }

    private Token matchPunctuator(int startLine, int startColumn) {
        char c = peek();
        if (c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' ||
                c == ',' || c == ';' || c == ':') {
            advance(1);
            return new Token(TokenType.PUNCTUATOR, String.valueOf(c), startLine, startColumn);
        }
        return null;
    }

    private boolean matchSingleLineComment() {
        if (!startsWith("##")) {
            return false;
        }
        advance(2);
        while (!isAtEnd() && peek() != '\n') {
            advance(1);
        }
        return true;
    }

    private Token consumeMultiLineComment(int startLine, int startColumn) {
        int startIndex = index;
        advance(2);
        while (!isAtEnd()) {
            if (startsWith("*#")) {
                advance(2);
                return null;
            }
            advance(1);
        }
        String lexeme = input.substring(startIndex, index);
        errorHandler.report("UnclosedComment", startLine, startColumn, lexeme,
                "Unclosed multi-line comment");
        return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
    }

    private boolean matchWhitespace() {
        if (!isWhitespace(peek())) {
            return false;
        }
        while (isWhitespace(peek())) {
            advance(1);
        }
        return true;
    }

    private TokenType resolveOperatorType(String op) {
        switch (op) {
            case "**":
            case "+=":
            case "-=":
            case "*=":
            case "/=":
                return op.endsWith("=") && op.length() == 2 ? TokenType.OP_ASSIGNMENT : TokenType.OP_ARITHMETIC;
            case "==":
            case "!=":
            case "<=":
            case ">=":
                return TokenType.OP_RELATIONAL;
            case "&&":
            case "||":
                return TokenType.OP_LOGICAL;
            case "++":
            case "--":
                return TokenType.OP_INC_DEC;
            default:
                return TokenType.OP_ARITHMETIC;
        }
    }

    private boolean startsWith(String value) {
        return input.startsWith(value, index);
    }

    private boolean isAtEnd() {
        return index >= input.length();
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return input.charAt(index);
    }

    private char peek(int offset) {
        int pos = index + offset;
        if (pos < 0 || pos >= input.length()) {
            return '\0';
        }
        return input.charAt(pos);
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private boolean isLower(char c) {
        return c >= 'a' && c <= 'z';
    }

    private boolean isWordChar(char c) {
        return isUpper(c) || isLower(c) || isDigit(c) || c == '_';
    }

    private boolean isIdentifierBody(char c) {
        return isLower(c) || isDigit(c) || c == '_';
    }

    private boolean isSign(char c) {
        return c == '+' || c == '-';
    }

    private void recordToken(Token token) {
        totalTokens++;
        tokenCounts.put(token.getType(), tokenCounts.getOrDefault(token.getType(), 0) + 1);
    }
}
