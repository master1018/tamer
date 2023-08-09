class History {
    private static final int VERSION_1 = 1;
    private static final int MAX_ENTRIES = 100;
    Vector<HistoryEntry> mEntries = new Vector<HistoryEntry>();
    int mPos;
    BaseAdapter mObserver;
    History() {
        clear();
    }
    History(int version, DataInput in) throws IOException {
        if (version >= VERSION_1) {
            int size = in.readInt();
            for (int i = 0; i < size; ++i) {
                mEntries.add(new HistoryEntry(version, in));
            }
            mPos = in.readInt();
        } else {
            throw new IOException("invalid version " + version);
        }
    }
    void setObserver(BaseAdapter observer) {
        mObserver = observer;
    }
    private void notifyChanged() {
        if (mObserver != null) {
            mObserver.notifyDataSetChanged();
        }
    }
    void clear() {
        mEntries.clear();
        mEntries.add(new HistoryEntry(""));
        mPos = 0;
        notifyChanged();
    }
    void write(DataOutput out) throws IOException {
        out.writeInt(mEntries.size());
        for (HistoryEntry entry : mEntries) {
            entry.write(out);
        }
        out.writeInt(mPos);
    }
    void update(String text) {
        current().setEdited(text);
    }
    boolean moveToPrevious() {
        if (mPos > 0) {
            --mPos;
            return true;
        }
        return false;
    }
    boolean moveToNext() {
        if (mPos < mEntries.size() - 1) {
            ++mPos;
            return true;
        }
        return false;
    }
    void enter(String text) {
        current().clearEdited();
        if (mEntries.size() >= MAX_ENTRIES) {
            mEntries.remove(0);
        }
        if (mEntries.size() < 2 ||
            !text.equals(mEntries.elementAt(mEntries.size() - 2).getBase())) {
            mEntries.insertElementAt(new HistoryEntry(text), mEntries.size() - 1);
        }
        mPos = mEntries.size() - 1;
        notifyChanged();
    }
    HistoryEntry current() {
        return mEntries.elementAt(mPos);
    }
    String getText() {
        return current().getEdited();
    }
    String getBase() {
        return current().getBase();
    }
}
