public class ShortcutPromoter extends PromoterWrapper {
    private static final String TAG = "QSB.ShortcutPromoter";
    private static final boolean DBG = false;
    public ShortcutPromoter(Promoter nextPromoter) {
        super(nextPromoter);
    }
    @Override
    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        int shortcutCount = shortcuts == null ? 0 : shortcuts.getCount();
        int promotedShortcutCount = Math.min(shortcutCount, maxPromoted);
        if (DBG) {
            Log.d(TAG, "pickPromoted(shortcutCount = " + shortcutCount +
                    ", maxPromoted = " + maxPromoted + ")");
        }
        for (int i = 0; i < promotedShortcutCount; i++) {
            promoted.add(new SuggestionPosition(shortcuts, i));
        }
        super.pickPromoted(null, suggestions, maxPromoted, promoted);
    }
}
