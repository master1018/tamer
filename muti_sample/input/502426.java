class ShortcutCursor extends ListSuggestionCursor {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.ShortcutCursor";
    private final SuggestionCursor mShortcuts;
    private final HashSet<SuggestionCursor> mRefreshed;
    private final ShortcutRefresher mRefresher;
    private final ShortcutRepository mShortcutRepo;
    private final Handler mUiThread;
    private boolean mClosed;
    private ShortcutCursor(String query, SuggestionCursor shortcuts, Handler uiThread,
            ShortcutRefresher refresher, ShortcutRepository repository) {
        super(query);
        mShortcuts = shortcuts;
        mUiThread = uiThread;
        mRefresher = refresher;
        mShortcutRepo = repository;
        mRefreshed = new HashSet<SuggestionCursor>();
    }
    @VisibleForTesting
    public ShortcutCursor(String query, Handler uiThread,
            ShortcutRefresher refresher, ShortcutRepository repository) {
        this(query, null, uiThread, refresher, repository);
    }
    public ShortcutCursor(SuggestionCursor suggestions, Handler uiThread,
            ShortcutRefresher refresher, ShortcutRepository repository) {
        this(suggestions.getUserQuery(), suggestions, uiThread, refresher, repository);
        if (suggestions == null) return;
        int count = suggestions.getCount();
        if (DBG) Log.d(TAG, "Total shortcuts: " + count);
        for (int i = 0; i < count; i++) {
            suggestions.moveTo(i);
            if (suggestions.getSuggestionSource() != null) {
                add(new SuggestionPosition(suggestions));
            } else {
                if (DBG) Log.d(TAG, "Skipping shortcut " + i);
            }
        }
    }
    public void refresh(Suggestion shortcut) {
        mRefresher.refresh(shortcut, new ShortcutRefresher.Listener() {
            public void onShortcutRefreshed(final Source source,
                    final String shortcutId, final SuggestionCursor refreshed) {
                if (DBG) Log.d(TAG, "Shortcut refreshed: " + shortcutId);
                mShortcutRepo.updateShortcut(source, shortcutId, refreshed);
                mUiThread.post(new Runnable() {
                    public void run() {
                        refresh(source, shortcutId, refreshed);
                    }
                });
            }
        });
    }
    private void refresh(Source source, String shortcutId, SuggestionCursor refreshed) {
        if (DBG) Log.d(TAG, "refresh " + shortcutId);
        if (mClosed) {
            if (refreshed != null) {
                refreshed.close();
            }
            return;
        }
        if (refreshed != null) {
            mRefreshed.add(refreshed);
        }
        for (int i = 0; i < getCount(); i++) {
            moveTo(i);
            if (shortcutId.equals(getShortcutId()) && source.equals(getSuggestionSource())) {
                if (refreshed != null && refreshed.getCount() > 0) {
                    if (DBG) Log.d(TAG, "replacing row " + i);
                    replaceRow(new SuggestionPosition(refreshed));
                } else {
                    if (DBG) Log.d(TAG, "removing row " + i);
                    removeRow();
                }
                notifyDataSetChanged();
                break;
            }
        }
    }
    @Override
    public void close() {
        if (DBG) Log.d(TAG, "close()");
        if (mClosed) {
            throw new IllegalStateException("Double close()");
        }
        super.close();
        mClosed = true;
        if (mShortcuts != null) {
            mShortcuts.close();
        }
        for (SuggestionCursor cursor : mRefreshed) {
             cursor.close();
        }
        super.close();
    }
}