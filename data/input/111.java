public class RuleCharacterIterator {
    private String text;
    private ParsePosition pos;
    private SymbolTable sym;
    private char[] buf;
    private int bufPos;
    private boolean isEscaped;
    public static final int DONE = -1;
    public static final int PARSE_VARIABLES = 1;
    public static final int PARSE_ESCAPES   = 2;
    public static final int SKIP_WHITESPACE = 4;
    public RuleCharacterIterator(String text, SymbolTable sym,
                                 ParsePosition pos) {
        if (text == null || pos.getIndex() > text.length()) {
            throw new IllegalArgumentException();
        }
        this.text = text;
        this.sym = sym;
        this.pos = pos;
        buf = null;
    }
    public boolean atEnd() {
        return buf == null && pos.getIndex() == text.length();
    }
    public int next(int options) {
        int c = DONE;
        isEscaped = false;
        for (;;) {
            c = _current();
            _advance(UTF16.getCharCount(c));
            if (c == SymbolTable.SYMBOL_REF && buf == null &&
                (options & PARSE_VARIABLES) != 0 && sym != null) {
                String name = sym.parseReference(text, pos, text.length());
                if (name == null) {
                    break;
                }
                bufPos = 0;
                buf = sym.lookup(name);
                if (buf == null) {
                    throw new IllegalArgumentException(
                                "Undefined variable: " + name);
                }
                if (buf.length == 0) {
                    buf = null;
                }
                continue;
            }
            if ((options & SKIP_WHITESPACE) != 0 &&
                UCharacterProperty.isRuleWhiteSpace(c)) {
                continue;
            }
            if (c == '\\' && (options & PARSE_ESCAPES) != 0) {
                int offset[] = new int[] { 0 };
                c = Utility.unescapeAt(lookahead(), offset);
                jumpahead(offset[0]);
                isEscaped = true;
                if (c < 0) {
                    throw new IllegalArgumentException("Invalid escape");
                }
            }
            break;
        }
        return c;
    }
    public boolean isEscaped() {
        return isEscaped;
    }
    public boolean inVariable() {
        return buf != null;
    }
    public Object getPos(Object p) {
        if (p == null) {
            return new Object[] {buf, new int[] {pos.getIndex(), bufPos}};
        }
        Object[] a = (Object[]) p;
        a[0] = buf;
        int[] v = (int[]) a[1];
        v[0] = pos.getIndex();
        v[1] = bufPos;
        return p;
    }
    public void setPos(Object p) {
        Object[] a = (Object[]) p;
        buf = (char[]) a[0];
        int[] v = (int[]) a[1];
        pos.setIndex(v[0]);
        bufPos = v[1];
    }
    public void skipIgnored(int options) {
        if ((options & SKIP_WHITESPACE) != 0) {
            for (;;) {
                int a = _current();
                if (!UCharacterProperty.isRuleWhiteSpace(a)) break;
                _advance(UTF16.getCharCount(a));
            }
        }
    }
    public String lookahead() {
        if (buf != null) {
            return new String(buf, bufPos, buf.length - bufPos);
        } else {
            return text.substring(pos.getIndex());
        }
    }
    public void jumpahead(int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        if (buf != null) {
            bufPos += count;
            if (bufPos > buf.length) {
                throw new IllegalArgumentException();
            }
            if (bufPos == buf.length) {
                buf = null;
            }
        } else {
            int i = pos.getIndex() + count;
            pos.setIndex(i);
            if (i > text.length()) {
                throw new IllegalArgumentException();
            }
        }
    }
    private int _current() {
        if (buf != null) {
            return UTF16.charAt(buf, 0, buf.length, bufPos);
        } else {
            int i = pos.getIndex();
            return (i < text.length()) ? UTF16.charAt(text, i) : DONE;
        }
    }
    private void _advance(int count) {
        if (buf != null) {
            bufPos += count;
            if (bufPos == buf.length) {
                buf = null;
            }
        } else {
            pos.setIndex(pos.getIndex() + count);
            if (pos.getIndex() > text.length()) {
                pos.setIndex(text.length());
            }
        }
    }
}
