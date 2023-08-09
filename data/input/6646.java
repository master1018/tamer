public class GapContent extends GapVector implements AbstractDocument.Content, Serializable {
    public GapContent() {
        this(10);
    }
    public GapContent(int initialLength) {
        super(Math.max(initialLength,2));
        char[] implied = new char[1];
        implied[0] = '\n';
        replace(0, 0, implied, implied.length);
        marks = new MarkVector();
        search = new MarkData(0);
        queue = new ReferenceQueue<StickyPosition>();
    }
    protected Object allocateArray(int len) {
        return new char[len];
    }
    protected int getArrayLength() {
        char[] carray = (char[]) getArray();
        return carray.length;
    }
    public int length() {
        int len = getArrayLength() - (getGapEnd() - getGapStart());
        return len;
    }
    public UndoableEdit insertString(int where, String str) throws BadLocationException {
        if (where > length() || where < 0) {
            throw new BadLocationException("Invalid insert", length());
        }
        char[] chars = str.toCharArray();
        replace(where, 0, chars, chars.length);
        return new InsertUndo(where, str.length());
    }
    public UndoableEdit remove(int where, int nitems) throws BadLocationException {
        if (where + nitems >= length()) {
            throw new BadLocationException("Invalid remove", length() + 1);
        }
        String removedString = getString(where, nitems);
        UndoableEdit edit = new RemoveUndo(where, removedString);
        replace(where, nitems, empty, 0);
        return edit;
    }
    public String getString(int where, int len) throws BadLocationException {
        Segment s = new Segment();
        getChars(where, len, s);
        return new String(s.array, s.offset, s.count);
    }
    public void getChars(int where, int len, Segment chars) throws BadLocationException {
        int end = where + len;
        if (where < 0 || end < 0) {
            throw new BadLocationException("Invalid location", -1);
        }
        if (end > length() || where > length()) {
            throw new BadLocationException("Invalid location", length() + 1);
        }
        int g0 = getGapStart();
        int g1 = getGapEnd();
        char[] array = (char[]) getArray();
        if ((where + len) <= g0) {
            chars.array = array;
            chars.offset = where;
        } else if (where >= g0) {
            chars.array = array;
            chars.offset = g1 + where - g0;
        } else {
            int before = g0 - where;
            if (chars.isPartialReturn()) {
                chars.array = array;
                chars.offset = where;
                chars.count = before;
                return;
            }
            chars.array = new char[len];
            chars.offset = 0;
            System.arraycopy(array, where, chars.array, 0, before);
            System.arraycopy(array, g1, chars.array, before, len - before);
        }
        chars.count = len;
    }
    public Position createPosition(int offset) throws BadLocationException {
        while ( queue.poll() != null ) {
            unusedMarks++;
        }
        if (unusedMarks > Math.max(5, (marks.size() / 10))) {
            removeUnusedMarks();
        }
        int g0 = getGapStart();
        int g1 = getGapEnd();
        int index = (offset < g0) ? offset : offset + (g1 - g0);
        search.index = index;
        int sortIndex = findSortIndex(search);
        MarkData m;
        StickyPosition position;
        if (sortIndex < marks.size()
            && (m = marks.elementAt(sortIndex)).index == index
            && (position = m.getPosition()) != null) {
        } else {
            position = new StickyPosition();
            m = new MarkData(index,position,queue);
            position.setMark(m);
            marks.insertElementAt(m, sortIndex);
        }
        return position;
    }
    final class MarkData extends WeakReference<StickyPosition> {
        MarkData(int index) {
            super(null);
            this.index = index;
        }
        MarkData(int index, StickyPosition position, ReferenceQueue<? super StickyPosition> queue) {
            super(position, queue);
            this.index = index;
        }
        public final int getOffset() {
            int g0 = getGapStart();
            int g1 = getGapEnd();
            int offs = (index < g0) ? index : index - (g1 - g0);
            return Math.max(offs, 0);
        }
        StickyPosition getPosition() {
            return get();
        }
        int index;
    }
    final class StickyPosition implements Position {
        StickyPosition() {
        }
        void setMark(MarkData mark) {
            this.mark = mark;
        }
        public final int getOffset() {
            return mark.getOffset();
        }
        public String toString() {
            return Integer.toString(getOffset());
        }
        MarkData mark;
    }
    private static final char[] empty = new char[0];
    private transient MarkVector marks;
    private transient MarkData search;
    private transient int unusedMarks = 0;
    private transient ReferenceQueue<StickyPosition> queue;
    final static int GROWTH_SIZE = 1024 * 512;
    protected void shiftEnd(int newSize) {
        int oldGapEnd = getGapEnd();
        super.shiftEnd(newSize);
        int dg = getGapEnd() - oldGapEnd;
        int adjustIndex = findMarkAdjustIndex(oldGapEnd);
        int n = marks.size();
        for (int i = adjustIndex; i < n; i++) {
            MarkData mark = marks.elementAt(i);
            mark.index += dg;
        }
    }
    int getNewArraySize(int reqSize) {
        if (reqSize < GROWTH_SIZE) {
            return super.getNewArraySize(reqSize);
        } else {
            return reqSize + GROWTH_SIZE;
        }
    }
    protected void shiftGap(int newGapStart) {
        int oldGapStart = getGapStart();
        int dg = newGapStart - oldGapStart;
        int oldGapEnd = getGapEnd();
        int newGapEnd = oldGapEnd + dg;
        int gapSize = oldGapEnd - oldGapStart;
        super.shiftGap(newGapStart);
        if (dg > 0) {
            int adjustIndex = findMarkAdjustIndex(oldGapStart);
            int n = marks.size();
            for (int i = adjustIndex; i < n; i++) {
                MarkData mark = marks.elementAt(i);
                if (mark.index >= newGapEnd) {
                    break;
                }
                mark.index -= gapSize;
            }
        } else if (dg < 0) {
            int adjustIndex = findMarkAdjustIndex(newGapStart);
            int n = marks.size();
            for (int i = adjustIndex; i < n; i++) {
                MarkData mark = marks.elementAt(i);
                if (mark.index >= oldGapEnd) {
                    break;
                }
                mark.index += gapSize;
            }
        }
        resetMarksAtZero();
    }
    protected void resetMarksAtZero() {
        if (marks != null && getGapStart() == 0) {
            int g1 = getGapEnd();
            for (int counter = 0, maxCounter = marks.size();
                 counter < maxCounter; counter++) {
                MarkData mark = marks.elementAt(counter);
                if (mark.index <= g1) {
                    mark.index = 0;
                }
                else {
                    break;
                }
            }
        }
    }
    protected void shiftGapStartDown(int newGapStart) {
        int adjustIndex = findMarkAdjustIndex(newGapStart);
        int n = marks.size();
        int g0 = getGapStart();
        int g1 = getGapEnd();
        for (int i = adjustIndex; i < n; i++) {
            MarkData mark = marks.elementAt(i);
            if (mark.index > g0) {
                break;
            }
            mark.index = g1;
        }
        super.shiftGapStartDown(newGapStart);
        resetMarksAtZero();
    }
    protected void shiftGapEndUp(int newGapEnd) {
        int adjustIndex = findMarkAdjustIndex(getGapEnd());
        int n = marks.size();
        for (int i = adjustIndex; i < n; i++) {
            MarkData mark = marks.elementAt(i);
            if (mark.index >= newGapEnd) {
                break;
            }
            mark.index = newGapEnd;
        }
        super.shiftGapEndUp(newGapEnd);
        resetMarksAtZero();
    }
    final int compare(MarkData o1, MarkData o2) {
        if (o1.index < o2.index) {
            return -1;
        } else if (o1.index > o2.index) {
            return 1;
        } else {
            return 0;
        }
    }
    final int findMarkAdjustIndex(int searchIndex) {
        search.index = Math.max(searchIndex, 1);
        int index = findSortIndex(search);
        for (int i = index - 1; i >= 0; i--) {
            MarkData d = marks.elementAt(i);
            if (d.index != search.index) {
                break;
            }
            index -= 1;
        }
        return index;
    }
    final int findSortIndex(MarkData o) {
        int lower = 0;
        int upper = marks.size() - 1;
        int mid = 0;
        if (upper == -1) {
            return 0;
        }
        int cmp;
        MarkData last = marks.elementAt(upper);
        cmp = compare(o, last);
        if (cmp > 0)
            return upper + 1;
        while (lower <= upper) {
            mid = lower + ((upper - lower) / 2);
            MarkData entry = marks.elementAt(mid);
            cmp = compare(o, entry);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                upper = mid - 1;
            } else {
                lower = mid + 1;
            }
        }
        return (cmp < 0) ? mid : mid + 1;
    }
    final void removeUnusedMarks() {
        int n = marks.size();
        MarkVector cleaned = new MarkVector(n);
        for (int i = 0; i < n; i++) {
            MarkData mark = marks.elementAt(i);
            if (mark.get() != null) {
                cleaned.addElement(mark);
            }
        }
        marks = cleaned;
        unusedMarks = 0;
    }
    static class MarkVector extends GapVector {
        MarkVector() {
            super();
        }
        MarkVector(int size) {
            super(size);
        }
        protected Object allocateArray(int len) {
            return new MarkData[len];
        }
        protected int getArrayLength() {
            MarkData[] marks = (MarkData[]) getArray();
            return marks.length;
        }
        public int size() {
            int len = getArrayLength() - (getGapEnd() - getGapStart());
            return len;
        }
        public void insertElementAt(MarkData m, int index) {
            oneMark[0] = m;
            replace(index, 0, oneMark, 1);
        }
        public void addElement(MarkData m) {
            insertElementAt(m, size());
        }
        public MarkData elementAt(int index) {
            int g0 = getGapStart();
            int g1 = getGapEnd();
            MarkData[] array = (MarkData[]) getArray();
            if (index < g0) {
                return array[index];
            } else {
                index += g1 - g0;
                return array[index];
            }
        }
        protected void replaceRange(int start, int end, Object[] marks) {
            int g0 = getGapStart();
            int g1 = getGapEnd();
            int index = start;
            int newIndex = 0;
            Object[] array = (Object[]) getArray();
            if (start >= g0) {
                index += (g1 - g0);
                end += (g1 - g0);
            }
            else if (end >= g0) {
                end += (g1 - g0);
                while (index < g0) {
                    array[index++] = marks[newIndex++];
                }
                index = g1;
            }
            else {
                while (index < end) {
                    array[index++] = marks[newIndex++];
                }
            }
            while (index < end) {
                array[index++] = marks[newIndex++];
            }
        }
        MarkData[] oneMark = new MarkData[1];
    }
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        marks = new MarkVector();
        search = new MarkData(0);
        queue = new ReferenceQueue<StickyPosition>();
    }
    protected Vector getPositionsInRange(Vector v, int offset, int length) {
        int endOffset = offset + length;
        int startIndex;
        int endIndex;
        int g0 = getGapStart();
        int g1 = getGapEnd();
        if (offset < g0) {
            if (offset == 0) {
                startIndex = 0;
            }
            else {
                startIndex = findMarkAdjustIndex(offset);
            }
            if (endOffset >= g0) {
                endIndex = findMarkAdjustIndex(endOffset + (g1 - g0) + 1);
            }
            else {
                endIndex = findMarkAdjustIndex(endOffset + 1);
            }
        }
        else {
            startIndex = findMarkAdjustIndex(offset + (g1 - g0));
            endIndex = findMarkAdjustIndex(endOffset + (g1 - g0) + 1);
        }
        Vector placeIn = (v == null) ? new Vector(Math.max(1, endIndex -
                                                           startIndex)) : v;
        for (int counter = startIndex; counter < endIndex; counter++) {
            placeIn.addElement(new UndoPosRef(marks.elementAt(counter)));
        }
        return placeIn;
    }
    protected void updateUndoPositions(Vector positions, int offset,
                                       int length) {
        int endOffset = offset + length;
        int g1 = getGapEnd();
        int startIndex;
        int endIndex = findMarkAdjustIndex(g1 + 1);
        if (offset != 0) {
            startIndex = findMarkAdjustIndex(g1);
        }
        else {
            startIndex = 0;
        }
        for(int counter = positions.size() - 1; counter >= 0; counter--) {
            UndoPosRef ref = (UndoPosRef)positions.elementAt(counter);
            ref.resetLocation(endOffset, g1);
        }
        if (startIndex < endIndex) {
            Object[] sorted = new Object[endIndex - startIndex];
            int addIndex = 0;
            int counter;
            if (offset == 0) {
                for (counter = startIndex; counter < endIndex; counter++) {
                    MarkData mark = marks.elementAt(counter);
                    if (mark.index == 0) {
                        sorted[addIndex++] = mark;
                    }
                }
                for (counter = startIndex; counter < endIndex; counter++) {
                    MarkData mark = marks.elementAt(counter);
                    if (mark.index != 0) {
                        sorted[addIndex++] = mark;
                    }
                }
            }
            else {
                for (counter = startIndex; counter < endIndex; counter++) {
                    MarkData mark = marks.elementAt(counter);
                    if (mark.index != g1) {
                        sorted[addIndex++] = mark;
                    }
                }
                for (counter = startIndex; counter < endIndex; counter++) {
                    MarkData mark = marks.elementAt(counter);
                    if (mark.index == g1) {
                        sorted[addIndex++] = mark;
                    }
                }
            }
            marks.replaceRange(startIndex, endIndex, sorted);
        }
    }
    final class UndoPosRef {
        UndoPosRef(MarkData rec) {
            this.rec = rec;
            this.undoLocation = rec.getOffset();
        }
        protected void resetLocation(int endOffset, int g1) {
            if (undoLocation != endOffset) {
                this.rec.index = undoLocation;
            }
            else {
                this.rec.index = g1;
            }
        }
        protected int undoLocation;
        protected MarkData rec;
    } 
    class InsertUndo extends AbstractUndoableEdit {
        protected InsertUndo(int offset, int length) {
            super();
            this.offset = offset;
            this.length = length;
        }
        public void undo() throws CannotUndoException {
            super.undo();
            try {
                posRefs = getPositionsInRange(null, offset, length);
                string = getString(offset, length);
                remove(offset, length);
            } catch (BadLocationException bl) {
              throw new CannotUndoException();
            }
        }
        public void redo() throws CannotRedoException {
            super.redo();
            try {
                insertString(offset, string);
                string = null;
                if(posRefs != null) {
                    updateUndoPositions(posRefs, offset, length);
                    posRefs = null;
                }
            } catch (BadLocationException bl) {
                throw new CannotRedoException();
            }
        }
        protected int offset;
        protected int length;
        protected String string;
        protected Vector posRefs;
    } 
    class RemoveUndo extends AbstractUndoableEdit {
        protected RemoveUndo(int offset, String string) {
            super();
            this.offset = offset;
            this.string = string;
            this.length = string.length();
            posRefs = getPositionsInRange(null, offset, length);
        }
        public void undo() throws CannotUndoException {
            super.undo();
            try {
                insertString(offset, string);
                if(posRefs != null) {
                    updateUndoPositions(posRefs, offset, length);
                    posRefs = null;
                }
                string = null;
            } catch (BadLocationException bl) {
              throw new CannotUndoException();
            }
        }
        public void redo() throws CannotRedoException {
            super.redo();
            try {
                string = getString(offset, length);
                posRefs = getPositionsInRange(null, offset, length);
                remove(offset, length);
            } catch (BadLocationException bl) {
              throw new CannotRedoException();
            }
        }
        protected int offset;
        protected int length;
        protected String string;
        protected Vector posRefs;
    } 
}
