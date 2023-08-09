public class ListSuggestionCursor extends AbstractSuggestionCursorWrapper {
    private static final int DEFAULT_CAPACITY = 16;
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private final ArrayList<Suggestion> mSuggestions;
    private int mPos = 0;
    public ListSuggestionCursor(String userQuery) {
        this(userQuery, DEFAULT_CAPACITY);
    }
    @VisibleForTesting
    public ListSuggestionCursor(String userQuery, Suggestion...suggestions) {
        this(userQuery, suggestions.length);
        for (Suggestion suggestion : suggestions) {
            mSuggestions.add(suggestion);
        }
    }
    public ListSuggestionCursor(String userQuery, int capacity) {
        super(userQuery);
        mSuggestions = new ArrayList<Suggestion>(capacity);
    }
    public boolean add(Suggestion suggestion) {
        mSuggestions.add(suggestion);
        return true;
    }
    public void close() {
        mSuggestions.clear();
    }
    public int getPosition() {
        return mPos;
    }
    public void moveTo(int pos) {
        mPos = pos;
    }
    public boolean moveToNext() {
        int size = mSuggestions.size();
        if (mPos >= size) {
            return false;
        }
        mPos++;
        return mPos < size;
    }
    public void removeRow() {
        mSuggestions.remove(mPos);
    }
    public void replaceRow(Suggestion suggestion) {
        mSuggestions.set(mPos, suggestion);
    }
    public int getCount() {
        return mSuggestions.size();
    }
    @Override
    protected Suggestion current() {
        return mSuggestions.get(mPos);
    }
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{[" + getUserQuery() + "] " + mSuggestions + "}";
    }
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
    protected void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }
}
