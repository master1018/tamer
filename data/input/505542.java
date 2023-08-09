public class RoundRobinPromoter implements Promoter {
    private static final String TAG = "QSB.RoundRobinPromoter";
    private static final boolean DBG = false;
    public RoundRobinPromoter() {
    }
    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        if (DBG) Log.d(TAG, "pickPromoted(maxPromoted = " + maxPromoted + ")");
        final int sourceCount = suggestions.size();
        if (sourceCount == 0) return;
        int sourcePos = 0;
        int suggestionPos = 0;
        int maxCount = 0;
        while (promoted.getCount() < maxPromoted) {
            SuggestionCursor sourceResult = suggestions.get(sourcePos);
            int count = sourceResult.getCount();
            if (count > maxCount) maxCount = count;
            if (suggestionPos < count) {
                if (DBG) Log.d(TAG, "Promoting " + sourcePos + ":" + suggestionPos);
                promoted.add(new SuggestionPosition(sourceResult, suggestionPos));
            }
            sourcePos++;
            if (sourcePos >= sourceCount) {
                sourcePos = 0;
                suggestionPos++;
                if (suggestionPos >= maxCount) break;
            }
        }
    }
}
