public class PromoterWrapper implements Promoter {
    private final Promoter mNextPromoter;
    public PromoterWrapper(Promoter nextPromoter) {
        mNextPromoter = nextPromoter;
    }
    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        if (promoted.getCount() < maxPromoted && mNextPromoter != null) {
            mNextPromoter.pickPromoted(shortcuts, suggestions, maxPromoted, promoted);
        }
    }
}
