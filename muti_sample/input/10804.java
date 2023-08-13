public class Position {
    public static final int NOPOS        = -1;
    public static final int FIRSTPOS     = 0;
    public static final int FIRSTLINE    = 1;
    public static final int FIRSTCOLUMN  = 1;
    public static final int LINESHIFT    = 10;
    public static final int MAXCOLUMN    = (1<<LINESHIFT) - 1;
    public static final int MAXLINE      = (1<<(Integer.SIZE-LINESHIFT)) - 1;
    public static final int MAXPOS       = Integer.MAX_VALUE;
    private Position() {}
    public static LineMap makeLineMap(char[] src, int max, boolean expandTabs) {
        LineMapImpl lineMap = expandTabs ?
            new LineTabMapImpl(max) : new LineMapImpl();
        lineMap.build(src, max);
        return lineMap;
    }
    public static int encodePosition(int line, int col) {
        if (line < 1)
            throw new IllegalArgumentException("line must be greater than 0");
        if (col < 1)
            throw new IllegalArgumentException("column must be greater than 0");
        if (line > MAXLINE || col > MAXCOLUMN) {
            return NOPOS;
        }
        return (line << LINESHIFT) + col;
    }
    public static interface LineMap extends com.sun.source.tree.LineMap {
        int getStartPosition(int line);
        int getPosition(int line, int column);
        int getLineNumber(int pos);
        int getColumnNumber(int pos);
    }
    static class LineMapImpl implements LineMap {
        protected int[] startPosition; 
        protected LineMapImpl() {}
        protected void build(char[] src, int max) {
            int c = 0;
            int i = 0;
            int[] linebuf = new int[max];
            while (i < max) {
                linebuf[c++] = i;
                do {
                    char ch = src[i];
                    if (ch == '\r' || ch == '\n') {
                        if (ch == '\r' && (i+1) < max && src[i+1] == '\n')
                            i += 2;
                        else
                            ++i;
                        break;
                    }
                    else if (ch == '\t')
                        setTabPosition(i);
                } while (++i < max);
            }
            this.startPosition = new int[c];
            System.arraycopy(linebuf, 0, startPosition, 0, c);
        }
        public int getStartPosition(int line) {
            return startPosition[line - FIRSTLINE];
        }
        public long getStartPosition(long line) {
            return getStartPosition(longToInt(line));
        }
        public int getPosition(int line, int column) {
            return startPosition[line - FIRSTLINE] + column - FIRSTCOLUMN;
        }
        public long getPosition(long line, long column) {
            return getPosition(longToInt(line), longToInt(column));
        }
        private int lastPosition = Position.FIRSTPOS;
        private int lastLine = Position.FIRSTLINE;
        public int getLineNumber(int pos) {
            if (pos == lastPosition) {
                return lastLine;
            }
            lastPosition = pos;
            int low = 0;
            int high = startPosition.length-1;
            while (low <= high) {
                int mid = (low + high) >> 1;
                int midVal = startPosition[mid];
                if (midVal < pos)
                    low = mid + 1;
                else if (midVal > pos)
                    high = mid - 1;
                else {
                    lastLine = mid + 1; 
                    return lastLine;
                }
            }
            lastLine = low;
            return lastLine;  
        }
        public long getLineNumber(long pos) {
            return getLineNumber(longToInt(pos));
        }
        public int getColumnNumber(int pos) {
            return pos - startPosition[getLineNumber(pos) - FIRSTLINE] + FIRSTCOLUMN;
        }
        public long getColumnNumber(long pos) {
            return getColumnNumber(longToInt(pos));
        }
        private static int longToInt(long longValue) {
            int intValue = (int)longValue;
            if (intValue != longValue)
                throw new IndexOutOfBoundsException();
            return intValue;
        }
        protected void setTabPosition(int offset) {}
    }
    public static class LineTabMapImpl extends LineMapImpl {
        private BitSet tabMap;       
        public LineTabMapImpl(int max) {
            super();
            tabMap = new BitSet(max);
        }
        protected void setTabPosition(int offset) {
            tabMap.set(offset);
        }
        public int getColumnNumber(int pos) {
            int lineStart = startPosition[getLineNumber(pos) - FIRSTLINE];
            int column = 0;
            for (int bp = lineStart; bp < pos; bp++) {
                if (tabMap.get(bp))
                    column = (column / TabInc * TabInc) + TabInc;
                else
                    column++;
            }
            return column + FIRSTCOLUMN;
        }
        public int getPosition(int line, int column) {
            int pos = startPosition[line - FIRSTLINE];
            column -= FIRSTCOLUMN;
            int col = 0;
            while (col < column) {
                pos++;
                if (tabMap.get(pos))
                    col = (col / TabInc * TabInc) + TabInc;
                else
                    col++;
            }
            return pos;
        }
    }
}
