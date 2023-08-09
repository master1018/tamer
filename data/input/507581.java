public class BasicHeaderIterator implements HeaderIterator {
    protected final Header[] allHeaders;
    protected int currentIndex;
    protected String headerName;
    public BasicHeaderIterator(Header[] headers, String name) {
        if (headers == null) {
            throw new IllegalArgumentException
                ("Header array must not be null.");
        }
        this.allHeaders = headers;
        this.headerName = name;
        this.currentIndex = findNext(-1);
    }
    protected int findNext(int from) {
        if (from < -1)
            return -1;
        final int to = this.allHeaders.length-1;
        boolean found = false;
        while (!found && (from < to)) {
            from++;
            found = filterHeader(from);
        }
        return found ? from : -1;
    }
    protected boolean filterHeader(int index) {
        return (this.headerName == null) ||
            this.headerName.equalsIgnoreCase(this.allHeaders[index].getName());
    }
    public boolean hasNext() {
        return (this.currentIndex >= 0);
    }
    public Header nextHeader()
        throws NoSuchElementException {
        final int current = this.currentIndex;
        if (current < 0) {
            throw new NoSuchElementException("Iteration already finished.");
        }
        this.currentIndex = findNext(current);
        return this.allHeaders[current];
    }
    public final Object next()
        throws NoSuchElementException {
        return nextHeader();
    }
    public void remove()
        throws UnsupportedOperationException {
        throw new UnsupportedOperationException
            ("Removing headers is not supported.");
    }
}
