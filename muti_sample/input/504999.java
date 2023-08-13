public class ShortcutLimitingPromoter extends PromoterWrapper {
    private static final String TAG = "QSB.ShortcutLimitingPromoter";
    private static final boolean DBG = false;
    private final int mMaxShortcutsPerWebSource;
    private final int mMaxShortcutsPerNonWebSource;
    public ShortcutLimitingPromoter(int maxShortcutsPerWebSource,
            int maxShortcutsPerNonWebSource, Promoter nextPromoter) {
        super(nextPromoter);
        mMaxShortcutsPerWebSource = maxShortcutsPerWebSource;
        mMaxShortcutsPerNonWebSource = maxShortcutsPerNonWebSource;
    }
    @Override
    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        final int shortcutCount = shortcuts == null ? 0 : shortcuts.getCount();
        ListSuggestionCursor filteredShortcuts = null;
        if (shortcutCount > 0) {
            filteredShortcuts = new ListSuggestionCursor(shortcuts.getUserQuery());
            HashMultiset<Source> sourceShortcutCounts = HashMultiset.create(shortcutCount);
            int numPromoted = 0;
            for (int i = 0; i < shortcutCount; i++) {
                shortcuts.moveTo(i);
                Source source = shortcuts.getSuggestionSource();
                if (source != null) {
                    int prevCount = sourceShortcutCounts.add(source, 1);
                    if (DBG) Log.d(TAG, "Source: " + source + ", count: " + prevCount);
                    int maxShortcuts = source.isWebSuggestionSource()
                            ? mMaxShortcutsPerWebSource : mMaxShortcutsPerNonWebSource;
                    if (prevCount < maxShortcuts) {
                        numPromoted++;
                        filteredShortcuts.add(new SuggestionPosition(shortcuts));
                    }
                    if (numPromoted >= maxPromoted) break;
                }
            }
        }
        if (DBG) {
            Log.d(TAG, "pickPromoted shortcuts=" + shortcutCount + " filtered=" +
                    filteredShortcuts.getCount());
        }
        super.pickPromoted(filteredShortcuts, suggestions, maxPromoted, promoted);
    }
}
