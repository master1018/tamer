public class MockShortcutRepository implements ShortcutRepository {
    public void clearHistory() {
    }
    public void close() {
    }
    public ShortcutCursor getShortcutsForQuery(String query, Collection<Corpus> corporaToQuery) {
        ShortcutCursor cursor = new ShortcutCursor(query, null, null, null);
        cursor.add(MockSource.SOURCE_1.createSuggestion(query + "_1_shortcut"));
        cursor.add(MockSource.SOURCE_2.createSuggestion(query + "_2_shortcut"));
        return cursor;
    }
    public void updateShortcut(Source source, String shortcutId, SuggestionCursor refreshed) {
    }
    public Map<String, Integer> getCorpusScores() {
        return null;
    }
    public boolean hasHistory() {
        return false;
    }
    public void reportClick(SuggestionCursor suggestions, int position) {
    }
}
