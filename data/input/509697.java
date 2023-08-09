public class NoLogger implements Logger {
    public NoLogger() {
    }
    public void logStart(int latency, String intentSource, Corpus corpus,
            List<Corpus> orderedCorpora) {
    }
    public void logSuggestionClick(int position,
            SuggestionCursor suggestionCursor, Collection<Corpus> queriedCorpora,
            int clickType) {
    }
    public void logSearch(Corpus corpus, int startMethod, int numChars) {
    }
    public void logVoiceSearch(Corpus corpus) {
    }
    public void logExit(SuggestionCursor suggestionCursor, int numChars) {
    }
    public void logLatency(CorpusResult result) {
    }
}
