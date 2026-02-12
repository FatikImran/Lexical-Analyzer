import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    public static class Entry {
        private final String name;
        private String type;
        private final int firstLine;
        private final int firstColumn;
        private int frequency;

        public Entry(String name, String type, int firstLine, int firstColumn) {
            this.name = name;
            this.type = type;
            this.firstLine = firstLine;
            this.firstColumn = firstColumn;
            this.frequency = 1;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getFirstLine() {
            return firstLine;
        }

        public int getFirstColumn() {
            return firstColumn;
        }

        public int getFrequency() {
            return frequency;
        }

        public void incrementFrequency() {
            frequency++;
        }
    }

    private final Map<String, Entry> entries = new LinkedHashMap<>();

    public void addOrUpdate(String name, String type, int line, int column) {
        Entry entry = entries.get(name);
        if (entry == null) {
            entries.put(name, new Entry(name, type, line, column));
        } else {
            if (type != null && !type.isEmpty()) {
                entry.setType(type);
            }
            entry.incrementFrequency();
        }
    }

    public Map<String, Entry> getEntries() {
        return entries;
    }
}
