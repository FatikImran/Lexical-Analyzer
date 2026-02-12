import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorHandler {
    public static class ErrorEntry {
        private final String type;
        private final int line;
        private final int column;
        private final String lexeme;
        private final String reason;

        public ErrorEntry(String type, int line, int column, String lexeme, String reason) {
            this.type = type;
            this.line = line;
            this.column = column;
            this.lexeme = lexeme;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "<" + type + ", Line: " + line + ", Col: " + column + ", \"" + escape(lexeme) + "\", " + reason + ">";
        }

        private String escape(String text) {
            return text
                    .replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\"", "\\\"");
        }
    }

    private final List<ErrorEntry> errors = new ArrayList<>();

    public void report(String type, int line, int column, String lexeme, String reason) {
        errors.add(new ErrorEntry(type, line, column, lexeme, reason));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ErrorEntry> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
