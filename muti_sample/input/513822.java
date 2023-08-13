public class ConcatPromoter implements Promoter {
    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        for (SuggestionCursor c : suggestions) {
            for (int i = 0; i < c.getCount(); i++) {
                if (promoted.getCount() >= maxPromoted) {
                    return;
                }
                promoted.add(new SuggestionPosition(c, i));
            }
        }
    }
}
