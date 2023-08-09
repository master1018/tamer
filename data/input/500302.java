public class DefaultCorpusRanker extends AbstractCorpusRanker {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.DefaultCorpusRanker";
    private final ShortcutRepository mShortcuts;
    public DefaultCorpusRanker(Corpora corpora, ShortcutRepository shortcuts) {
        super(corpora);
        mShortcuts = shortcuts;
    }
    private static class CorpusComparator implements Comparator<Corpus> {
        private final Map<String,Integer> mClickScores;
        public CorpusComparator(Map<String,Integer> clickScores) {
            mClickScores = clickScores;
        }
        public int compare(Corpus corpus1, Corpus corpus2) {
            boolean corpus1IsDefault = corpus1.isCorpusDefaultEnabled();
            boolean corpus2IsDefault = corpus2.isCorpusDefaultEnabled();
            if (corpus1IsDefault != corpus2IsDefault) {
                return corpus1IsDefault ? -1 : 1;
            } else {
                return getCorpusScore(corpus2) - getCorpusScore(corpus1);
            }
        }
        private int getCorpusScore(Corpus corpus) {
            if (corpus.isWebCorpus()) {
                return Integer.MAX_VALUE;
            }
            Integer clickScore = mClickScores.get(corpus.getName());
            if (clickScore != null) {
                return clickScore;
            }
            return 0;
        }
    }
    @Override
    public List<Corpus> rankCorpora(Corpora corpora) {
        Collection<Corpus> enabledCorpora = corpora.getEnabledCorpora();
        if (DBG) Log.d(TAG, "Ranking: " + enabledCorpora);
        Map<String,Integer> clickScores = mShortcuts.getCorpusScores();
        ArrayList<Corpus> ordered = new ArrayList<Corpus>(enabledCorpora);
        Collections.sort(ordered, new CorpusComparator(clickScores));
        if (DBG) Log.d(TAG, "Click scores: " + clickScores);
        if (DBG) Log.d(TAG, "Ordered: " + ordered);
        return ordered;
    }
}
