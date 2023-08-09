public class Spans {
    private static final int kMaxAddsSinceSort = 256;
    private List mSpans = new Vector(kMaxAddsSinceSort);
    private int mAddsSinceSort = 0;
    public Spans() {
    }
    public void add(float start, float end) {
        if (mSpans != null) {
            mSpans.add(new Span(start, end));
            if (++mAddsSinceSort >= kMaxAddsSinceSort) {
                sortAndCollapse();
            }
        }
    }
    public void addInfinite() {
        mSpans = null;
    }
    public boolean intersects(float start, float end) {
        boolean doesIntersect;
        if (mSpans != null) {
            if (mAddsSinceSort > 0) {
                sortAndCollapse();
            }
            int found = Collections.binarySearch(mSpans,
                                                 new Span(start, end),
                                                 SpanIntersection.instance);
            doesIntersect = found >= 0;
        } else {
           doesIntersect = true;
        }
        return doesIntersect;
    }
    private void sortAndCollapse() {
        Collections.sort(mSpans);
        mAddsSinceSort = 0;
        Iterator iter = mSpans.iterator();
        Span span = null;
        if (iter.hasNext()) {
            span = (Span) iter.next();
        }
        while (iter.hasNext()) {
            Span nextSpan = (Span) iter.next();
            if (span.subsume(nextSpan)) {
                iter.remove();
            } else {
                span = nextSpan;
            }
        }
    }
    static class Span implements Comparable {
        private float mStart;
        private float mEnd;
        Span(float start, float end) {
            mStart = start;
            mEnd = end;
        }
        final float getStart() {
            return mStart;
        }
        final float getEnd() {
            return mEnd;
        }
        final void setStart(float start) {
            mStart = start;
        }
        final void setEnd(float end) {
            mEnd = end;
        }
        boolean subsume(Span otherSpan) {
            boolean isSubsumed = contains(otherSpan.mStart);
            if (isSubsumed && otherSpan.mEnd > mEnd) {
                mEnd = otherSpan.mEnd;
            }
            return isSubsumed;
        }
        boolean contains(float pos) {
            return mStart <= pos && pos < mEnd;
        }
        public int compareTo(Object o) {
            Span otherSpan = (Span) o;
            float otherStart = otherSpan.getStart();
            int result;
            if (mStart < otherStart) {
                result = -1;
            } else if (mStart > otherStart) {
                result = 1;
            } else {
                result = 0;
            }
            return result;
        }
        public String toString() {
            return "Span: " + mStart + " to " + mEnd;
        }
    }
    static class SpanIntersection implements Comparator {
        static final SpanIntersection instance =
                                      new SpanIntersection();
        private SpanIntersection() {
        }
        public int compare(Object o1, Object o2) {
            int result;
            Span span1 = (Span) o1;
            Span span2 = (Span) o2;
            if (span1.getEnd() <= span2.getStart()) {
                result = -1;
            } else if (span1.getStart() >= span2.getEnd()) {
                result = 1;
            } else {
                result = 0;
            }
            return result;
        }
    }
}
