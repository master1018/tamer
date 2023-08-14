public final class FontRunIterator {
    CompositeFont font;
    char[] text;
    int start;
    int limit;
    CompositeGlyphMapper mapper; 
    int slot = -1;
    int pos;
    public void init(CompositeFont font, char[] text, int start, int limit) {
        if (font == null || text == null || start < 0 || limit < start || limit > text.length) {
            throw new IllegalArgumentException();
        }
        this.font = font;
        this.text = text;
        this.start = start;
        this.limit = limit;
        this.mapper = (CompositeGlyphMapper)font.getMapper();
        this.slot = -1;
        this.pos = start;
    }
    public PhysicalFont getFont() {
        return slot == -1 ? null : font.getSlotFont(slot);
    }
    public int getGlyphMask() {
        return slot << 24;
    }
    public int getPos() {
        return pos;
    }
    public boolean next(int script, int lim) {
        if (pos == lim) {
            return false;
        }
        int ch = nextCodePoint(lim);
        int sl = mapper.charToGlyph(ch) & CompositeGlyphMapper.SLOTMASK;
        slot = sl >>> 24;
        while ((ch = nextCodePoint(lim)) != DONE && (mapper.charToGlyph(ch) & CompositeGlyphMapper.SLOTMASK) == sl);
        pushback(ch);
        return true;
    }
    public boolean next() {
        return next(Script.COMMON, limit);
    }
    static final int SURROGATE_START = 0x10000;
    static final int LEAD_START = 0xd800;
    static final int LEAD_LIMIT = 0xdc00;
    static final int TAIL_START = 0xdc00;
    static final int TAIL_LIMIT = 0xe000;
    static final int LEAD_SURROGATE_SHIFT = 10;
    static final int SURROGATE_OFFSET = SURROGATE_START - (LEAD_START << LEAD_SURROGATE_SHIFT) - TAIL_START;
    static final int DONE = -1;
    final int nextCodePoint() {
        return nextCodePoint(limit);
    }
    final int nextCodePoint(int lim) {
        if (pos >= lim) {
            return DONE;
        }
        int ch = text[pos++];
        if (ch >= LEAD_START && ch < LEAD_LIMIT && pos < lim) {
            int nch = text[pos];
            if (nch >= TAIL_START && nch < TAIL_LIMIT) {
                ++pos;
                ch = (ch << LEAD_SURROGATE_SHIFT) + nch + SURROGATE_OFFSET;
            }
        }
        return ch;
    }
    final void pushback(int ch) {
        if (ch >= 0) {
            if (ch >= 0x10000) {
                pos -= 2;
            } else {
                pos -= 1;
            }
        }
    }
}
