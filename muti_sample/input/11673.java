public class MirroredBreakIterator extends BreakIterator {
    private final List<Integer> boundaries;
    private int charIndex;
    private int boundaryIndex;
    MirroredBreakIterator(BreakIterator bi) {
        List<Integer> b = new ArrayList<Integer>();
        int i = bi.first();
        charIndex = i;
        for (; i != DONE; i = bi.next()) {
            b.add(i);
        }
        boundaries = Collections.unmodifiableList(b);
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            throw new RuntimeException("clone failed", e);
        }
    }
    @Override
    public int first() {
        return changeIndices(0);
    }
    @Override
    public int last() {
        return changeIndices(boundaries.size() - 1);
    }
    @Override
    public int next(int n) {
        if (n == 0) {
            return current();
        }
        int newBoundary = boundaryIndex + n;
        if (newBoundary < 0) {
            first();
            return DONE;
        }
        if (newBoundary > lastBoundary()) {
            last();
            return DONE;
        }
        return changeIndices(newBoundary);
    }
    @Override
    public int next() {
        if (boundaryIndex == lastBoundary()) {
            return DONE;
        }
        return changeIndices(boundaryIndex + 1);
    }
    @Override
    public int previous() {
        if (boundaryIndex == 0) {
            return DONE;
        }
        return changeIndices(boundaryIndex - 1);
    }
    @Override
    public int following(int offset) {
        validateOffset(offset);
        for (int b = 0; b <= lastBoundary(); b++) {
            int i = boundaries.get(b);
            if (i > offset) {
                return changeIndices(i, b);
            }
        }
        return DONE;
    }
    @Override
    public int preceding(int offset) {
        validateOffset(offset);
        for (int b = lastBoundary(); b >= 0; b--) {
            int i = boundaries.get(b);
            if (i < offset) {
                return changeIndices(i, b);
            }
        }
        return DONE;
    }
    @Override
    public boolean isBoundary(int offset) {
        return super.isBoundary(offset);
    }
    @Override
    public int current() {
        return charIndex;
    }
    @Override
    public CharacterIterator getText() {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setText(CharacterIterator newText) {
        throw new UnsupportedOperationException();
    }
    private int lastBoundary() {
        return boundaries.size() - 1;
    }
    private int changeIndices(int newCharIndex, int newBoundary) {
        boundaryIndex = newBoundary;
        return charIndex = newCharIndex;
    }
    private int changeIndices(int newBoundary) {
        try {
            return changeIndices(boundaries.get(newBoundary), newBoundary);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }
    private void validateOffset(int offset) {
        if (offset < boundaries.get(0) || offset > boundaries.get(lastBoundary())) {
            throw new IllegalArgumentException();
        }
    }
}
