class SourceShortcutRefresher implements ShortcutRefresher {
    private static final String TAG = "QSB.SourceShortcutRefresher";
    private static final boolean DBG = false;
    private final NamedTaskExecutor mExecutor;
    private final Set<String> mRefreshed = Collections.synchronizedSet(new HashSet<String>());
    private final Set<String> mRefreshing = Collections.synchronizedSet(new HashSet<String>());
    public SourceShortcutRefresher(NamedTaskExecutor executor) {
        mExecutor = executor;
    }
    public void refresh(Suggestion shortcut, Listener listener) {
        Source source = shortcut.getSuggestionSource();
        if (source == null) {
            throw new NullPointerException("source");
        }
        String shortcutId = shortcut.getShortcutId();
        if (shouldRefresh(source, shortcutId) && !isRefreshing(source, shortcutId)) {
            if (DBG) {
                Log.d(TAG, "Refreshing shortcut  " + shortcutId + " '" +
                        shortcut.getSuggestionText1() + "'");
            }
            markShortcutRefreshing(source, shortcutId);
            String extraData = shortcut.getSuggestionIntentExtraData();
            ShortcutRefreshTask refreshTask = new ShortcutRefreshTask(
                    source, shortcutId, extraData, listener);
            mExecutor.execute(refreshTask);
        }
    }
    public boolean shouldRefresh(Source source, String shortcutId) {
        return source != null && shortcutId != null
                && !mRefreshed.contains(makeKey(source, shortcutId));
    }
    public boolean isRefreshing(Source source, String shortcutId) {
        return source != null && shortcutId != null
                && mRefreshing.contains(makeKey(source, shortcutId));
    }
    private void markShortcutRefreshing(Source source, String shortcutId) {
        mRefreshing.add(makeKey(source, shortcutId));
    }
    public void markShortcutRefreshed(Source source, String shortcutId) {
        String key = makeKey(source, shortcutId);
        mRefreshed.add(key);
        mRefreshing.remove(key);
    }
    public void reset() {
        mRefreshed.clear();
    }
    public void cancelPendingTasks() {
        mExecutor.cancelPendingTasks();
    }
    private static String makeKey(Source source, String shortcutId) {
        return source.getName() + "#" + shortcutId;
    }
    private class ShortcutRefreshTask implements NamedTask {
        private final Source mSource;
        private final String mShortcutId;
        private final String mExtraData;
        private final Listener mListener;
        ShortcutRefreshTask(Source source, String shortcutId, String extraData,
                Listener listener) {
            mSource = source;
            mShortcutId = shortcutId;
            mExtraData = extraData;
            mListener = listener;
        }
        public String getName() {
            return mSource.getName();
        }
        public void run() {
            SuggestionCursor refreshed = mSource.refreshShortcut(mShortcutId, mExtraData);
            if (refreshed != null && refreshed.getCount() == 0) {
                refreshed.close();
                refreshed = null;
            }
            markShortcutRefreshed(mSource, mShortcutId);
            mListener.onShortcutRefreshed(mSource, mShortcutId, refreshed);
        }
    }
}
