class WhitespaceBasedBreakIterator extends BreakIterator {
    private char[] text = new char[0];
    private int[] breaks = new int[] { 0 } ;
    private int pos = 0;
    public void setText(CharacterIterator ci) {
        int begin = ci.getBeginIndex();
        text = new char[ci.getEndIndex() - begin];
        int[] breaks0 = new int[text.length + 1];
        int brIx = 0;
        breaks0[brIx++] = begin;
        int charIx = 0;
        boolean inWs = false;
        for (char c = ci.first(); c != CharacterIterator.DONE; c = ci.next()) {
            text[charIx] = c;
            boolean ws = Character.isWhitespace(c);
            if (inWs && !ws) {
                breaks0[brIx++] = charIx + begin;
            }
            inWs = ws;
            charIx++;
        }
        if (text.length > 0) {
            breaks0[brIx++] = text.length + begin;
        }
        System.arraycopy(breaks0, 0, breaks = new int[brIx], 0, brIx);
    }
    public CharacterIterator getText() {
        return new StringCharacterIterator(new String(text));
    }
    public int first() {
        return breaks[pos = 0];
    }
    public int last() {
        return breaks[pos = breaks.length - 1];
    }
    public int current() {
        return breaks[pos];
    }
    public int next() {
        return (pos == breaks.length - 1 ? DONE : breaks[++pos]);
    }
    public int previous() {
        return (pos == 0 ? DONE : breaks[--pos]);
    }
    public int next(int n) {
        return checkhit(pos + n);
    }
    public int following(int n) {
        return adjacent(n, 1);
    }
    public int preceding(int n) {
        return adjacent(n, -1);
    }
    private int checkhit(int hit) {
        if ((hit < 0) || (hit >= breaks.length)) {
            return DONE;
        } else {
            return breaks[pos = hit];
        }
    }
    private int adjacent(int n, int bias) {
        int hit = Arrays.binarySearch(breaks, n);
        int offset = (hit < 0 ? (bias < 0 ? -1 : -2) : 0);
        return checkhit(Math.abs(hit) + bias + offset);
    }
}
