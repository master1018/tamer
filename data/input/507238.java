public class DelayingSuggestionsAdapter extends SuggestionsAdapter {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.DelayingSuggestionsAdapter";
    private DataSetObserver mPendingDataSetObserver;
    private Suggestions mPendingSuggestions;
    public DelayingSuggestionsAdapter(SuggestionViewFactory viewFactory) {
        super(viewFactory);
    }
    @Override
    public void close() {
        setPendingSuggestions(null);
        super.close();
    }
    @Override
    public void setSuggestions(Suggestions suggestions) {
        if (suggestions == null) {
            super.setSuggestions(null);
            setPendingSuggestions(null);
            return;
        }
        if (shouldPublish(suggestions)) {
            if (DBG) Log.d(TAG, "Publishing suggestions immediately: " + suggestions);
            super.setSuggestions(suggestions);
            setPendingSuggestions(null);
        } else {
            if (DBG) Log.d(TAG, "Delaying suggestions publishing: " + suggestions);
            setPendingSuggestions(suggestions);
        }
    }
    private boolean shouldPublish(Suggestions suggestions) {
        if (suggestions.isDone()) return true;
        SuggestionCursor cursor = getCorpusCursor(suggestions, getCorpus());
        return cursor != null && cursor.getCount() > 0;
    }
    private void setPendingSuggestions(Suggestions suggestions) {
        if (mPendingSuggestions == suggestions) {
            return;
        }
        if (isClosed()) {
            if (suggestions != null) {
                suggestions.close();
            }
            return;
        }
        if (mPendingDataSetObserver == null) {
            mPendingDataSetObserver = new PendingSuggestionsObserver();
        }
        if (mPendingSuggestions != null) {
            mPendingSuggestions.unregisterDataSetObserver(mPendingDataSetObserver);
            if (mPendingSuggestions != getSuggestions()) {
                mPendingSuggestions.close();
            }
        }
        mPendingSuggestions = suggestions;
        if (mPendingSuggestions != null) {
            mPendingSuggestions.registerDataSetObserver(mPendingDataSetObserver);
        }
    }
    protected void onPendingSuggestionsChanged() {
        if (DBG) {
            Log.d(TAG, "onPendingSuggestionsChanged(), mPendingSuggestions="
                    + mPendingSuggestions);
        }
        if (shouldPublish(mPendingSuggestions)) {
            if (DBG) Log.d(TAG, "Suggestions now available, publishing: " + mPendingSuggestions);
            super.setSuggestions(mPendingSuggestions);
            setPendingSuggestions(null);
        }
    }
    private class PendingSuggestionsObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            onPendingSuggestionsChanged();
        }
    }
}
