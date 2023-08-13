public class IndexRangeIterator<E> implements Iterator<E> {
    private final E[] mList;
    private IndexRange mRange;
    private int mPos;
    public IndexRangeIterator(E[] list) {
        super();
        mList = list;
        mRange = new IndexRange();
        mPos = mRange.begin - 1;
    }
    public void rewind() {
        mPos = mRange.begin - 1;
    }
    public void setRange(int begin, int end) {
        mRange.begin = begin;
        mRange.end = end;
        mPos = begin - 1;
    }
    public int getBegin() {
        return mRange.begin;
    }
    public int getEnd() {
        return mRange.end;
    }
    public boolean hasNext() {
        int pos = mPos + 1;
        return (pos < mList.length && pos < mRange.end);
    }
    public int getCurrentPosition() {
        return mPos;
    }
    public E next() {
        int pos = mPos + 1;
        ++mPos;
        return mList[pos];
    }
    public void remove() {
        throw new ConcurrentModificationException();
    }
}
