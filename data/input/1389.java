public class Segment implements Cloneable, CharacterIterator, CharSequence {
    public char[] array;
    public int offset;
    public int count;
    private boolean partialReturn;
    public Segment() {
        this(null, 0, 0);
    }
    public Segment(char[] array, int offset, int count) {
        this.array = array;
        this.offset = offset;
        this.count = count;
        partialReturn = false;
    }
    public void setPartialReturn(boolean p) {
        partialReturn = p;
    }
    public boolean isPartialReturn() {
        return partialReturn;
    }
    public String toString() {
        if (array != null) {
            return new String(array, offset, count);
        }
        return "";
    }
    public char first() {
        pos = offset;
        if (count != 0) {
            return array[pos];
        }
        return DONE;
    }
    public char last() {
        pos = offset + count;
        if (count != 0) {
            pos -= 1;
            return array[pos];
        }
        return DONE;
    }
    public char current() {
        if (count != 0 && pos < offset + count) {
            return array[pos];
        }
        return DONE;
    }
    public char next() {
        pos += 1;
        int end = offset + count;
        if (pos >= end) {
            pos = end;
            return DONE;
        }
        return current();
    }
    public char previous() {
        if (pos == offset) {
            return DONE;
        }
        pos -= 1;
        return current();
    }
    public char setIndex(int position) {
        int end = offset + count;
        if ((position < offset) || (position > end)) {
            throw new IllegalArgumentException("bad position: " + position);
        }
        pos = position;
        if ((pos != end) && (count != 0)) {
            return array[pos];
        }
        return DONE;
    }
    public int getBeginIndex() {
        return offset;
    }
    public int getEndIndex() {
        return offset + count;
    }
    public int getIndex() {
        return pos;
    }
    public char charAt(int index) {
        if (index < 0
            || index >= count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return array[offset + index];
    }
    public int length() {
        return count;
    }
    public CharSequence subSequence(int start, int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > count) {
            throw new StringIndexOutOfBoundsException(end);
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException(end - start);
        }
        Segment segment = new Segment();
        segment.array = this.array;
        segment.offset = this.offset + start;
        segment.count = end - start;
        return segment;
    }
    public Object clone() {
        Object o;
        try {
            o = super.clone();
        } catch (CloneNotSupportedException cnse) {
            o = null;
        }
        return o;
    }
    private int pos;
}
