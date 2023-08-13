public final class StringCharacterIterator implements CharacterIterator {
    String string;
    int start, end, offset;
    public StringCharacterIterator(String value) {
        string = value;
        start = offset = 0;
        end = string.length();
    }
    public StringCharacterIterator(String value, int location) {
        string = value;
        start = 0;
        end = string.length();
        if (location < 0 || location > end) {
            throw new IllegalArgumentException();
        }
        offset = location;
    }
    public StringCharacterIterator(String value, int start, int end,
            int location) {
        string = value;
        if (start < 0 || end > string.length() || start > end
                || location < start || location > end) {
            throw new IllegalArgumentException();
        }
        this.start = start;
        this.end = end;
        offset = location;
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    public char current() {
        if (offset == end) {
            return DONE;
        }
        return string.charAt(offset);
    }
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StringCharacterIterator)) {
            return false;
        }
        StringCharacterIterator it = (StringCharacterIterator) object;
        return string.equals(it.string) && start == it.start && end == it.end
                && offset == it.offset;
    }
    public char first() {
        if (start == end) {
            return DONE;
        }
        offset = start;
        return string.charAt(offset);
    }
    public int getBeginIndex() {
        return start;
    }
    public int getEndIndex() {
        return end;
    }
    public int getIndex() {
        return offset;
    }
    @Override
    public int hashCode() {
        return string.hashCode() + start + end + offset;
    }
    public char last() {
        if (start == end) {
            return DONE;
        }
        offset = end - 1;
        return string.charAt(offset);
    }
    public char next() {
        if (offset >= (end - 1)) {
            offset = end;
            return DONE;
        }
        return string.charAt(++offset);
    }
    public char previous() {
        if (offset == start) {
            return DONE;
        }
        return string.charAt(--offset);
    }
    public char setIndex(int location) {
        if (location < start || location > end) {
            throw new IllegalArgumentException();
        }
        offset = location;
        if (offset == end) {
            return DONE;
        }
        return string.charAt(offset);
    }
    public void setText(String value) {
        string = value;
        start = offset = 0;
        end = value.length();
    }
}
