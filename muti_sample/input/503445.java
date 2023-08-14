public class RankAwarePromoter implements Promoter {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.RankAwarePromoter";
    private final Config mConfig;
    private final Corpora mCorpora;
    public RankAwarePromoter(Config config, Corpora corpora) {
        mConfig = config;
        mCorpora = corpora;
    }
    public void pickPromoted(SuggestionCursor shortcuts, ArrayList<CorpusResult> suggestions,
            int maxPromoted, ListSuggestionCursor promoted) {
        if (DBG) Log.d(TAG, "Available results: " + suggestions);
        LinkedList<CorpusResult> defaultResults = new LinkedList<CorpusResult>();
        LinkedList<CorpusResult> otherResults = new LinkedList<CorpusResult>();
        for (CorpusResult result : suggestions) {
            if (result.getCount() > 0) {
                result.moveTo(0);
                Corpus corpus = result.getCorpus();
                if (corpus == null || corpus.isCorpusDefaultEnabled()) {
                    defaultResults.add(result);
                } else {
                    otherResults.add(result);
                }
            }
        }
        if (maxPromoted > 0 && !defaultResults.isEmpty()) {
            int slotsToFill = Math.min(getSlotsAboveKeyboard() - promoted.getCount(), maxPromoted);
            if (slotsToFill > 0) {
                int stripeSize = Math.max(1, slotsToFill / defaultResults.size());
                maxPromoted -= roundRobin(defaultResults, slotsToFill, stripeSize, promoted);
            }
        }
        if (maxPromoted > 0 && !defaultResults.isEmpty()) {
            int stripeSize = Math.max(1, maxPromoted / defaultResults.size());
            maxPromoted -= roundRobin(defaultResults, maxPromoted, stripeSize, promoted);
            maxPromoted -= roundRobin(defaultResults, maxPromoted, maxPromoted, promoted);
        }
        if (maxPromoted > 0 && !otherResults.isEmpty()) {
            int stripeSize = Math.max(1, maxPromoted / otherResults.size());
            maxPromoted -= roundRobin(otherResults, maxPromoted, stripeSize, promoted);
            maxPromoted -= roundRobin(otherResults, maxPromoted, maxPromoted, promoted);
        }
        if (DBG) Log.d(TAG, "Returning " + promoted.toString());
    }
    private int getSlotsAboveKeyboard() {
        return mConfig.getNumSuggestionsAboveKeyboard();
    }
    private int roundRobin(LinkedList<CorpusResult> results, int maxPromoted, int stripeSize,
            ListSuggestionCursor promoted) {
        int count = 0;
        if (maxPromoted > 0 && !results.isEmpty()) {
            for (Iterator<CorpusResult> iter = results.iterator();
                 count < maxPromoted && iter.hasNext();) {
                CorpusResult result = iter.next();
                count += promote(result, stripeSize, promoted);
                if (result.getPosition() == result.getCount()) {
                    iter.remove();
                }
            }
        }
        return count;
    }
    private int promote(SuggestionCursor cursor, int count, ListSuggestionCursor promoted) {
        if (count < 1 || cursor.getPosition() >= cursor.getCount()) {
            return 0;
        }
        int i = 0;
        do {
            promoted.add(new SuggestionPosition(cursor));
            i++;
        } while (cursor.moveToNext() && i < count);
        return i;
    }
}
