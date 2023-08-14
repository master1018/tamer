public class UnicodeSet implements UnicodeMatcher {
    private static final int LOW = 0x000000; 
    private static final int HIGH = 0x110000; 
    public static final int MIN_VALUE = LOW;
    public static final int MAX_VALUE = HIGH - 1;
    private int len;      
    private int[] list;   
    private int[] rangeList; 
    private int[] buffer; 
    TreeSet strings = new TreeSet();
    private String pat = null;
    private static final int START_EXTRA = 16;         
    private static final int GROW_EXTRA = START_EXTRA; 
    private static UnicodeSet INCLUSIONS[] = null;
    public UnicodeSet() {
        list = new int[1 + START_EXTRA];
        list[len++] = HIGH;
    }
    public UnicodeSet(int start, int end) {
        this();
        complement(start, end);
    }
    public UnicodeSet(String pattern) {
        this();
        applyPattern(pattern, null, null, IGNORE_SPACE);
    }
    public UnicodeSet set(UnicodeSet other) {
        list = (int[]) other.list.clone();
        len = other.len;
        pat = other.pat;
        strings = (TreeSet)other.strings.clone();
        return this;
    }
    public final UnicodeSet applyPattern(String pattern) {
        return applyPattern(pattern, null, null, IGNORE_SPACE);
    }
    private static void _appendToPat(StringBuffer buf, String s, boolean escapeUnprintable) {
        for (int i = 0; i < s.length(); i += UTF16.getCharCount(i)) {
            _appendToPat(buf, UTF16.charAt(s, i), escapeUnprintable);
        }
    }
    private static void _appendToPat(StringBuffer buf, int c, boolean escapeUnprintable) {
        if (escapeUnprintable && Utility.isUnprintable(c)) {
            if (Utility.escapeUnprintable(buf, c)) {
                return;
            }
        }
        switch (c) {
        case '[': 
        case ']': 
        case '-': 
        case '^': 
        case '&': 
        case '\\': 
        case '{':
        case '}':
        case '$':
        case ':':
            buf.append('\\');
            break;
        default:
            if (UCharacterProperty.isRuleWhiteSpace(c)) {
                buf.append('\\');
            }
            break;
        }
        UTF16.append(buf, c);
    }
    private StringBuffer _toPattern(StringBuffer result,
                                    boolean escapeUnprintable) {
        if (pat != null) {
            int i;
            int backslashCount = 0;
            for (i=0; i<pat.length(); ) {
                int c = UTF16.charAt(pat, i);
                i += UTF16.getCharCount(c);
                if (escapeUnprintable && Utility.isUnprintable(c)) {
                    if ((backslashCount % 2) == 1) {
                        result.setLength(result.length() - 1);
                    }
                    Utility.escapeUnprintable(result, c);
                    backslashCount = 0;
                } else {
                    UTF16.append(result, c);
                    if (c == '\\') {
                        ++backslashCount;
                    } else {
                        backslashCount = 0;
                    }
                }
            }
            return result;
        }
        return _generatePattern(result, escapeUnprintable, true);
    }
    public StringBuffer _generatePattern(StringBuffer result,
                                         boolean escapeUnprintable, boolean includeStrings) {
        result.append('[');
        int count = getRangeCount();
        if (count > 1 &&
            getRangeStart(0) == MIN_VALUE &&
            getRangeEnd(count-1) == MAX_VALUE) {
            result.append('^');
            for (int i = 1; i < count; ++i) {
                int start = getRangeEnd(i-1)+1;
                int end = getRangeStart(i)-1;
                _appendToPat(result, start, escapeUnprintable);
                if (start != end) {
                    if ((start+1) != end) {
                        result.append('-');
                    }
                    _appendToPat(result, end, escapeUnprintable);
                }
            }
        }
        else {
            for (int i = 0; i < count; ++i) {
                int start = getRangeStart(i);
                int end = getRangeEnd(i);
                _appendToPat(result, start, escapeUnprintable);
                if (start != end) {
                    if ((start+1) != end) {
                        result.append('-');
                    }
                    _appendToPat(result, end, escapeUnprintable);
                }
            }
        }
        if (includeStrings && strings.size() > 0) {
            Iterator it = strings.iterator();
            while (it.hasNext()) {
                result.append('{');
                _appendToPat(result, (String) it.next(), escapeUnprintable);
                result.append('}');
            }
        }
        return result.append(']');
    }
    private UnicodeSet add_unchecked(int start, int end) {
        if (start < MIN_VALUE || start > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < MIN_VALUE || end > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start < end) {
            add(range(start, end), 2, 0);
        } else if (start == end) {
            add(start);
        }
        return this;
    }
    public final UnicodeSet add(int c) {
        return add_unchecked(c);
    }
    private final UnicodeSet add_unchecked(int c) {
        if (c < MIN_VALUE || c > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(c, 6));
        }
        int i = findCodePoint(c);
        if ((i & 1) != 0) return this;
        if (c == list[i]-1) {
            list[i] = c;
            if (c == MAX_VALUE) {
                ensureCapacity(len+1);
                list[len++] = HIGH;
            }
            if (i > 0 && c == list[i-1]) {
                System.arraycopy(list, i+1, list, i-1, len-i-1);
                len -= 2;
            }
        }
        else if (i > 0 && c == list[i-1]) {
            list[i-1]++;
        }
        else {
            if (len+2 > list.length) {
                int[] temp = new int[len + 2 + GROW_EXTRA];
                if (i != 0) System.arraycopy(list, 0, temp, 0, i);
                System.arraycopy(list, i, temp, i+2, len-i);
                list = temp;
            } else {
                System.arraycopy(list, i, list, i+2, len-i);
            }
            list[i] = c;
            list[i+1] = c+1;
            len += 2;
        }
        pat = null;
        return this;
    }
    public final UnicodeSet add(String s) {
        int cp = getSingleCP(s);
        if (cp < 0) {
            strings.add(s);
            pat = null;
        } else {
            add_unchecked(cp, cp);
        }
        return this;
    }
    private static int getSingleCP(String s) {
        if (s.length() < 1) {
            throw new IllegalArgumentException("Can't use zero-length strings in UnicodeSet");
        }
        if (s.length() > 2) return -1;
        if (s.length() == 1) return s.charAt(0);
        int cp = UTF16.charAt(s, 0);
        if (cp > 0xFFFF) { 
            return cp;
        }
        return -1;
    }
    public UnicodeSet complement(int start, int end) {
        if (start < MIN_VALUE || start > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < MIN_VALUE || end > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start <= end) {
            xor(range(start, end), 2, 0);
        }
        pat = null;
        return this;
    }
    public UnicodeSet complement() {
        if (list[0] == LOW) {
            System.arraycopy(list, 1, list, 0, len-1);
            --len;
        } else {
            ensureCapacity(len+1);
            System.arraycopy(list, 0, list, 1, len);
            list[0] = LOW;
            ++len;
        }
        pat = null;
        return this;
    }
    public boolean contains(int c) {
        if (c < MIN_VALUE || c > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(c, 6));
        }
        int i = findCodePoint(c);
        return ((i & 1) != 0); 
    }
    private final int findCodePoint(int c) {
        if (c < list[0]) return 0;
        if (len >= 2 && c >= list[len-2]) return len-1;
        int lo = 0;
        int hi = len - 1;
        for (;;) {
            int i = (lo + hi) >>> 1;
            if (i == lo) return hi;
            if (c < list[i]) {
                hi = i;
            } else {
                lo = i;
            }
        }
    }
    public UnicodeSet addAll(UnicodeSet c) {
        add(c.list, c.len, 0);
        strings.addAll(c.strings);
        return this;
    }
    public UnicodeSet retainAll(UnicodeSet c) {
        retain(c.list, c.len, 0);
        strings.retainAll(c.strings);
        return this;
    }
    public UnicodeSet removeAll(UnicodeSet c) {
        retain(c.list, c.len, 2);
        strings.removeAll(c.strings);
        return this;
    }
    public UnicodeSet clear() {
        list[0] = HIGH;
        len = 1;
        pat = null;
        strings.clear();
        return this;
    }
    public int getRangeCount() {
        return len/2;
    }
    public int getRangeStart(int index) {
        return list[index*2];
    }
    public int getRangeEnd(int index) {
        return (list[index*2 + 1] - 1);
    }
    UnicodeSet applyPattern(String pattern,
                      ParsePosition pos,
                      SymbolTable symbols,
                      int options) {
        boolean parsePositionWasNull = pos == null;
        if (parsePositionWasNull) {
            pos = new ParsePosition(0);
        }
        StringBuffer rebuiltPat = new StringBuffer();
        RuleCharacterIterator chars =
            new RuleCharacterIterator(pattern, symbols, pos);
        applyPattern(chars, symbols, rebuiltPat, options);
        if (chars.inVariable()) {
            syntaxError(chars, "Extra chars in variable value");
        }
        pat = rebuiltPat.toString();
        if (parsePositionWasNull) {
            int i = pos.getIndex();
            if ((options & IGNORE_SPACE) != 0) {
                i = Utility.skipWhitespace(pattern, i);
            }
            if (i != pattern.length()) {
                throw new IllegalArgumentException("Parse of \"" + pattern +
                                                   "\" failed at " + i);
            }
        }
        return this;
    }
    void applyPattern(RuleCharacterIterator chars, SymbolTable symbols,
                      StringBuffer rebuiltPat, int options) {
        int opts = RuleCharacterIterator.PARSE_VARIABLES |
                   RuleCharacterIterator.PARSE_ESCAPES;
        if ((options & IGNORE_SPACE) != 0) {
            opts |= RuleCharacterIterator.SKIP_WHITESPACE;
        }
        StringBuffer patBuf = new StringBuffer(), buf = null;
        boolean usePat = false;
        UnicodeSet scratch = null;
        Object backup = null;
        int lastItem = 0, lastChar = 0, mode = 0;
        char op = 0;
        boolean invert = false;
        clear();
        while (mode != 2 && !chars.atEnd()) {
            if (false) {
                if (!((lastItem == 0 && op == 0) ||
                      (lastItem == 1 && (op == 0 || op == '-')) ||
                      (lastItem == 2 && (op == 0 || op == '-' || op == '&')))) {
                    throw new IllegalArgumentException();
                }
            }
            int c = 0;
            boolean literal = false;
            UnicodeSet nested = null;
            int setMode = 0;
            if (resemblesPropertyPattern(chars, opts)) {
                setMode = 2;
            }
            else {
                backup = chars.getPos(backup);
                c = chars.next(opts);
                literal = chars.isEscaped();
                if (c == '[' && !literal) {
                    if (mode == 1) {
                        chars.setPos(backup); 
                        setMode = 1;
                    } else {
                        mode = 1;
                        patBuf.append('[');
                        backup = chars.getPos(backup); 
                        c = chars.next(opts);
                        literal = chars.isEscaped();
                        if (c == '^' && !literal) {
                            invert = true;
                            patBuf.append('^');
                            backup = chars.getPos(backup); 
                            c = chars.next(opts);
                            literal = chars.isEscaped();
                        }
                        if (c == '-') {
                            literal = true;
                        } else {
                            chars.setPos(backup); 
                            continue;
                        }
                    }
                } else if (symbols != null) {
                     UnicodeMatcher m = symbols.lookupMatcher(c); 
                     if (m != null) {
                         try {
                             nested = (UnicodeSet) m;
                             setMode = 3;
                         } catch (ClassCastException e) {
                             syntaxError(chars, "Syntax error");
                         }
                     }
                }
            }
            if (setMode != 0) {
                if (lastItem == 1) {
                    if (op != 0) {
                        syntaxError(chars, "Char expected after operator");
                    }
                    add_unchecked(lastChar, lastChar);
                    _appendToPat(patBuf, lastChar, false);
                    lastItem = op = 0;
                }
                if (op == '-' || op == '&') {
                    patBuf.append(op);
                }
                if (nested == null) {
                    if (scratch == null) scratch = new UnicodeSet();
                    nested = scratch;
                }
                switch (setMode) {
                case 1:
                    nested.applyPattern(chars, symbols, patBuf, options);
                    break;
                case 2:
                    chars.skipIgnored(opts);
                    nested.applyPropertyPattern(chars, patBuf, symbols);
                    break;
                case 3: 
                    nested._toPattern(patBuf, false);
                    break;
                }
                usePat = true;
                if (mode == 0) {
                    set(nested);
                    mode = 2;
                    break;
                }
                switch (op) {
                case '-':
                    removeAll(nested);
                    break;
                case '&':
                    retainAll(nested);
                    break;
                case 0:
                    addAll(nested);
                    break;
                }
                op = 0;
                lastItem = 2;
                continue;
            }
            if (mode == 0) {
                syntaxError(chars, "Missing '['");
            }
            if (!literal) {
                switch (c) {
                case ']':
                    if (lastItem == 1) {
                        add_unchecked(lastChar, lastChar);
                        _appendToPat(patBuf, lastChar, false);
                    }
                    if (op == '-') {
                        add_unchecked(op, op);
                        patBuf.append(op);
                    } else if (op == '&') {
                        syntaxError(chars, "Trailing '&'");
                    }
                    patBuf.append(']');
                    mode = 2;
                    continue;
                case '-':
                    if (op == 0) {
                        if (lastItem != 0) {
                            op = (char) c;
                            continue;
                        } else {
                            add_unchecked(c, c);
                            c = chars.next(opts);
                            literal = chars.isEscaped();
                            if (c == ']' && !literal) {
                                patBuf.append("-]");
                                mode = 2;
                                continue;
                            }
                        }
                    }
                    syntaxError(chars, "'-' not after char or set");
                case '&':
                    if (lastItem == 2 && op == 0) {
                        op = (char) c;
                        continue;
                    }
                    syntaxError(chars, "'&' not after set");
                case '^':
                    syntaxError(chars, "'^' not after '['");
                case '{':
                    if (op != 0) {
                        syntaxError(chars, "Missing operand after operator");
                    }
                    if (lastItem == 1) {
                        add_unchecked(lastChar, lastChar);
                        _appendToPat(patBuf, lastChar, false);
                    }
                    lastItem = 0;
                    if (buf == null) {
                        buf = new StringBuffer();
                    } else {
                        buf.setLength(0);
                    }
                    boolean ok = false;
                    while (!chars.atEnd()) {
                        c = chars.next(opts);
                        literal = chars.isEscaped();
                        if (c == '}' && !literal) {
                            ok = true;
                            break;
                        }
                        UTF16.append(buf, c);
                    }
                    if (buf.length() < 1 || !ok) {
                        syntaxError(chars, "Invalid multicharacter string");
                    }
                    add(buf.toString());
                    patBuf.append('{');
                    _appendToPat(patBuf, buf.toString(), false);
                    patBuf.append('}');
                    continue;
                case SymbolTable.SYMBOL_REF:
                    backup = chars.getPos(backup);
                    c = chars.next(opts);
                    literal = chars.isEscaped();
                    boolean anchor = (c == ']' && !literal);
                    if (symbols == null && !anchor) {
                        c = SymbolTable.SYMBOL_REF;
                        chars.setPos(backup);
                        break; 
                    }
                    if (anchor && op == 0) {
                        if (lastItem == 1) {
                            add_unchecked(lastChar, lastChar);
                            _appendToPat(patBuf, lastChar, false);
                        }
                        add_unchecked(UnicodeMatcher.ETHER);
                        usePat = true;
                        patBuf.append(SymbolTable.SYMBOL_REF).append(']');
                        mode = 2;
                        continue;
                    }
                    syntaxError(chars, "Unquoted '$'");
                default:
                    break;
                }
            }
            switch (lastItem) {
            case 0:
                lastItem = 1;
                lastChar = c;
                break;
            case 1:
                if (op == '-') {
                    if (lastChar >= c) {
                        syntaxError(chars, "Invalid range");
                    }
                    add_unchecked(lastChar, c);
                    _appendToPat(patBuf, lastChar, false);
                    patBuf.append(op);
                    _appendToPat(patBuf, c, false);
                    lastItem = op = 0;
                } else {
                    add_unchecked(lastChar, lastChar);
                    _appendToPat(patBuf, lastChar, false);
                    lastChar = c;
                }
                break;
            case 2:
                if (op != 0) {
                    syntaxError(chars, "Set expected after operator");
                }
                lastChar = c;
                lastItem = 1;
                break;
            }
        }
        if (mode != 2) {
            syntaxError(chars, "Missing ']'");
        }
        chars.skipIgnored(opts);
        if (invert) {
            complement();
        }
        if (usePat) {
            rebuiltPat.append(patBuf.toString());
        } else {
            _generatePattern(rebuiltPat, false, true);
        }
    }
    private static void syntaxError(RuleCharacterIterator chars, String msg) {
        throw new IllegalArgumentException("Error: " + msg + " at \"" +
                                           Utility.escape(chars.toString()) +
                                           '"');
    }
    private void ensureCapacity(int newLen) {
        if (newLen <= list.length) return;
        int[] temp = new int[newLen + GROW_EXTRA];
        System.arraycopy(list, 0, temp, 0, len);
        list = temp;
    }
    private void ensureBufferCapacity(int newLen) {
        if (buffer != null && newLen <= buffer.length) return;
        buffer = new int[newLen + GROW_EXTRA];
    }
    private int[] range(int start, int end) {
        if (rangeList == null) {
            rangeList = new int[] { start, end+1, HIGH };
        } else {
            rangeList[0] = start;
            rangeList[1] = end+1;
        }
        return rangeList;
    }
    private UnicodeSet xor(int[] other, int otherLen, int polarity) {
        ensureBufferCapacity(len + otherLen);
        int i = 0, j = 0, k = 0;
        int a = list[i++];
        int b;
        if (polarity == 1 || polarity == 2) {
            b = LOW;
            if (other[j] == LOW) { 
                ++j;
                b = other[j];
            }
        } else {
            b = other[j++];
        }
        while (true) {
            if (a < b) {
                buffer[k++] = a;
                a = list[i++];
            } else if (b < a) {
                buffer[k++] = b;
                b = other[j++];
            } else if (a != HIGH) { 
                a = list[i++];
                b = other[j++];
            } else { 
                buffer[k++] = HIGH;
                len = k;
                break;
            }
        }
        int[] temp = list;
        list = buffer;
        buffer = temp;
        pat = null;
        return this;
    }
    private UnicodeSet add(int[] other, int otherLen, int polarity) {
        ensureBufferCapacity(len + otherLen);
        int i = 0, j = 0, k = 0;
        int a = list[i++];
        int b = other[j++];
        main:
        while (true) {
            switch (polarity) {
              case 0: 
                if (a < b) { 
                    if (k > 0 && a <= buffer[k-1]) {
                        a = max(list[i], buffer[--k]);
                    } else {
                        buffer[k++] = a;
                        a = list[i];
                    }
                    i++; 
                    polarity ^= 1;
                } else if (b < a) { 
                    if (k > 0 && b <= buffer[k-1]) {
                        b = max(other[j], buffer[--k]);
                    } else {
                        buffer[k++] = b;
                        b = other[j];
                    }
                    j++;
                    polarity ^= 2;
                } else { 
                    if (a == HIGH) break main;
                    if (k > 0 && a <= buffer[k-1]) {
                        a = max(list[i], buffer[--k]);
                    } else {
                        buffer[k++] = a;
                        a = list[i];
                    }
                    i++;
                    polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 3: 
                if (b <= a) { 
                    if (a == HIGH) break main;
                    buffer[k++] = a;
                } else { 
                    if (b == HIGH) break main;
                    buffer[k++] = b;
                }
                a = list[i++]; polarity ^= 1;   
                b = other[j++]; polarity ^= 2;
                break;
              case 1: 
                if (a < b) { 
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                } else if (b < a) { 
                    b = other[j++]; polarity ^= 2;
                } else { 
                    if (a == HIGH) break main;
                    a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 2: 
                if (b < a) { 
                    buffer[k++] = b; b = other[j++]; polarity ^= 2;
                } else  if (a < b) { 
                    a = list[i++]; polarity ^= 1;
                } else { 
                    if (a == HIGH) break main;
                    a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
            }
        }
        buffer[k++] = HIGH;    
        len = k;
        int[] temp = list;
        list = buffer;
        buffer = temp;
        pat = null;
        return this;
    }
    private UnicodeSet retain(int[] other, int otherLen, int polarity) {
        ensureBufferCapacity(len + otherLen);
        int i = 0, j = 0, k = 0;
        int a = list[i++];
        int b = other[j++];
        main:
        while (true) {
            switch (polarity) {
              case 0: 
                if (a < b) { 
                    a = list[i++]; polarity ^= 1;
                } else if (b < a) { 
                    b = other[j++]; polarity ^= 2;
                } else { 
                    if (a == HIGH) break main;
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 3: 
                if (a < b) { 
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                } else if (b < a) { 
                    buffer[k++] = b; b = other[j++]; polarity ^= 2;
                } else { 
                    if (a == HIGH) break main;
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 1: 
                if (a < b) { 
                    a = list[i++]; polarity ^= 1;
                } else if (b < a) { 
                    buffer[k++] = b; b = other[j++]; polarity ^= 2;
                } else { 
                    if (a == HIGH) break main;
                    a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 2: 
                if (b < a) { 
                    b = other[j++]; polarity ^= 2;
                } else  if (a < b) { 
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                } else { 
                    if (a == HIGH) break main;
                    a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
            }
        }
        buffer[k++] = HIGH;    
        len = k;
        int[] temp = list;
        list = buffer;
        buffer = temp;
        pat = null;
        return this;
    }
    private static final int max(int a, int b) {
        return (a > b) ? a : b;
    }
    private static interface Filter {
        boolean contains(int codePoint);
    }
    static final VersionInfo NO_VERSION = VersionInfo.getInstance(0, 0, 0, 0);
    private static class VersionFilter implements Filter {
        VersionInfo version;
        VersionFilter(VersionInfo version) { this.version = version; }
        public boolean contains(int ch) {
            VersionInfo v = UCharacter.getAge(ch);
            return v != NO_VERSION &&
                   v.compareTo(version) <= 0;
        }
    }
    private static synchronized UnicodeSet getInclusions(int src) {
        if (INCLUSIONS == null) {
            INCLUSIONS = new UnicodeSet[UCharacterProperty.SRC_COUNT];
        }
        if(INCLUSIONS[src] == null) {
            UnicodeSet incl = new UnicodeSet();
            switch(src) {
            case UCharacterProperty.SRC_PROPSVEC:
                UCharacterProperty.getInstance().upropsvec_addPropertyStarts(incl);
                break;
            default:
                throw new IllegalStateException("UnicodeSet.getInclusions(unknown src "+src+")");
            }
            INCLUSIONS[src] = incl;
        }
        return INCLUSIONS[src];
    }
    private UnicodeSet applyFilter(Filter filter, int src) {
        clear();
        int startHasProperty = -1;
        UnicodeSet inclusions = getInclusions(src);
        int limitRange = inclusions.getRangeCount();
        for (int j=0; j<limitRange; ++j) {
            int start = inclusions.getRangeStart(j);
            int end = inclusions.getRangeEnd(j);
            for (int ch = start; ch <= end; ++ch) {
                if (filter.contains(ch)) {
                    if (startHasProperty < 0) {
                        startHasProperty = ch;
                    }
                } else if (startHasProperty >= 0) {
                    add_unchecked(startHasProperty, ch-1);
                    startHasProperty = -1;
                }
            }
        }
        if (startHasProperty >= 0) {
            add_unchecked(startHasProperty, 0x10FFFF);
        }
        return this;
    }
    private static String mungeCharName(String source) {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<source.length(); ) {
            int ch = UTF16.charAt(source, i);
            i += UTF16.getCharCount(ch);
            if (UCharacterProperty.isRuleWhiteSpace(ch)) {
                if (buf.length() == 0 ||
                    buf.charAt(buf.length() - 1) == ' ') {
                    continue;
                }
                ch = ' '; 
            }
            UTF16.append(buf, ch);
        }
        if (buf.length() != 0 &&
            buf.charAt(buf.length() - 1) == ' ') {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }
    public UnicodeSet applyPropertyAlias(String propertyAlias,
                                         String valueAlias, SymbolTable symbols) {
        if (valueAlias.length() > 0) {
            if (propertyAlias.equals("Age")) {
                VersionInfo version = VersionInfo.getInstance(mungeCharName(valueAlias));
                applyFilter(new VersionFilter(version), UCharacterProperty.SRC_PROPSVEC);
                return this;
            }
        }
        throw new IllegalArgumentException("Unsupported property: " + propertyAlias);
    }
    private static boolean resemblesPropertyPattern(RuleCharacterIterator chars,
                                                    int iterOpts) {
        boolean result = false;
        iterOpts &= ~RuleCharacterIterator.PARSE_ESCAPES;
        Object pos = chars.getPos(null);
        int c = chars.next(iterOpts);
        if (c == '[' || c == '\\') {
            int d = chars.next(iterOpts & ~RuleCharacterIterator.SKIP_WHITESPACE);
            result = (c == '[') ? (d == ':') :
                     (d == 'N' || d == 'p' || d == 'P');
        }
        chars.setPos(pos);
        return result;
    }
    private UnicodeSet applyPropertyPattern(String pattern, ParsePosition ppos, SymbolTable symbols) {
        int pos = ppos.getIndex();
        if ((pos+5) > pattern.length()) {
            return null;
        }
        boolean posix = false; 
        boolean isName = false; 
        boolean invert = false;
        if (pattern.regionMatches(pos, "[:", 0, 2)) {
            posix = true;
            pos = Utility.skipWhitespace(pattern, pos+2);
            if (pos < pattern.length() && pattern.charAt(pos) == '^') {
                ++pos;
                invert = true;
            }
        } else if (pattern.regionMatches(true, pos, "\\p", 0, 2) ||
                   pattern.regionMatches(pos, "\\N", 0, 2)) {
            char c = pattern.charAt(pos+1);
            invert = (c == 'P');
            isName = (c == 'N');
            pos = Utility.skipWhitespace(pattern, pos+2);
            if (pos == pattern.length() || pattern.charAt(pos++) != '{') {
                return null;
            }
        } else {
            return null;
        }
        int close = pattern.indexOf(posix ? ":]" : "}", pos);
        if (close < 0) {
            return null;
        }
        int equals = pattern.indexOf('=', pos);
        String propName, valueName;
        if (equals >= 0 && equals < close && !isName) {
            propName = pattern.substring(pos, equals);
            valueName = pattern.substring(equals+1, close);
        }
        else {
            propName = pattern.substring(pos, close);
            valueName = "";
            if (isName) {
                valueName = propName;
                propName = "na";
            }
        }
        applyPropertyAlias(propName, valueName, symbols);
        if (invert) {
            complement();
        }
        ppos.setIndex(close + (posix ? 2 : 1));
        return this;
    }
    private void applyPropertyPattern(RuleCharacterIterator chars,
                                      StringBuffer rebuiltPat, SymbolTable symbols) {
        String patStr = chars.lookahead();
        ParsePosition pos = new ParsePosition(0);
        applyPropertyPattern(patStr, pos, symbols);
        if (pos.getIndex() == 0) {
            syntaxError(chars, "Invalid property pattern");
        }
        chars.jumpahead(pos.getIndex());
        rebuiltPat.append(patStr.substring(0, pos.getIndex()));
    }
    public static final int IGNORE_SPACE = 1;
}
