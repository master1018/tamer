public class TextUtils {
    private TextUtils() {  }
    private static String[] EMPTY_STRING_ARRAY = new String[]{};
    public static void getChars(CharSequence s, int start, int end,
                                char[] dest, int destoff) {
        Class c = s.getClass();
        if (c == String.class)
            ((String) s).getChars(start, end, dest, destoff);
        else if (c == StringBuffer.class)
            ((StringBuffer) s).getChars(start, end, dest, destoff);
        else if (c == StringBuilder.class)
            ((StringBuilder) s).getChars(start, end, dest, destoff);
        else if (s instanceof GetChars)
            ((GetChars) s).getChars(start, end, dest, destoff);
        else {
            for (int i = start; i < end; i++)
                dest[destoff++] = s.charAt(i);
        }
    }
    public static int indexOf(CharSequence s, char ch) {
        return indexOf(s, ch, 0);
    }
    public static int indexOf(CharSequence s, char ch, int start) {
        Class c = s.getClass();
        if (c == String.class)
            return ((String) s).indexOf(ch, start);
        return indexOf(s, ch, start, s.length());
    }
    public static int indexOf(CharSequence s, char ch, int start, int end) {
        Class c = s.getClass();
        if (s instanceof GetChars || c == StringBuffer.class ||
            c == StringBuilder.class || c == String.class) {
            final int INDEX_INCREMENT = 500;
            char[] temp = obtain(INDEX_INCREMENT);
            while (start < end) {
                int segend = start + INDEX_INCREMENT;
                if (segend > end)
                    segend = end;
                getChars(s, start, segend, temp, 0);
                int count = segend - start;
                for (int i = 0; i < count; i++) {
                    if (temp[i] == ch) {
                        recycle(temp);
                        return i + start;
                    }
                }
                start = segend;
            }
            recycle(temp);
            return -1;
        }
        for (int i = start; i < end; i++)
            if (s.charAt(i) == ch)
                return i;
        return -1;
    }
    public static int lastIndexOf(CharSequence s, char ch) {
        return lastIndexOf(s, ch, s.length() - 1);
    }
    public static int lastIndexOf(CharSequence s, char ch, int last) {
        Class c = s.getClass();
        if (c == String.class)
            return ((String) s).lastIndexOf(ch, last);
        return lastIndexOf(s, ch, 0, last);
    }
    public static int lastIndexOf(CharSequence s, char ch,
                                  int start, int last) {
        if (last < 0)
            return -1;
        if (last >= s.length())
            last = s.length() - 1;
        int end = last + 1;
        Class c = s.getClass();
        if (s instanceof GetChars || c == StringBuffer.class ||
            c == StringBuilder.class || c == String.class) {
            final int INDEX_INCREMENT = 500;
            char[] temp = obtain(INDEX_INCREMENT);
            while (start < end) {
                int segstart = end - INDEX_INCREMENT;
                if (segstart < start)
                    segstart = start;
                getChars(s, segstart, end, temp, 0);
                int count = end - segstart;
                for (int i = count - 1; i >= 0; i--) {
                    if (temp[i] == ch) {
                        recycle(temp);
                        return i + segstart;
                    }
                }
                end = segstart;
            }
            recycle(temp);
            return -1;
        }
        for (int i = end - 1; i >= start; i--)
            if (s.charAt(i) == ch)
                return i;
        return -1;
    }
    public static int indexOf(CharSequence s, CharSequence needle) {
        return indexOf(s, needle, 0, s.length());
    }
    public static int indexOf(CharSequence s, CharSequence needle, int start) {
        return indexOf(s, needle, start, s.length());
    }
    public static int indexOf(CharSequence s, CharSequence needle,
                              int start, int end) {
        int nlen = needle.length();
        if (nlen == 0)
            return start;
        char c = needle.charAt(0);
        for (;;) {
            start = indexOf(s, c, start);
            if (start > end - nlen) {
                break;
            }
            if (start < 0) {
                return -1;
            }
            if (regionMatches(s, start, needle, 0, nlen)) {
                return start;
            }
            start++;
        }
        return -1;
    }
    public static boolean regionMatches(CharSequence one, int toffset,
                                        CharSequence two, int ooffset,
                                        int len) {
        char[] temp = obtain(2 * len);
        getChars(one, toffset, toffset + len, temp, 0);
        getChars(two, ooffset, ooffset + len, temp, len);
        boolean match = true;
        for (int i = 0; i < len; i++) {
            if (temp[i] != temp[i + len]) {
                match = false;
                break;
            }
        }
        recycle(temp);
        return match;
    }
    public static String substring(CharSequence source, int start, int end) {
        if (source instanceof String)
            return ((String) source).substring(start, end);
        if (source instanceof StringBuilder)
            return ((StringBuilder) source).substring(start, end);
        if (source instanceof StringBuffer)
            return ((StringBuffer) source).substring(start, end);
        char[] temp = obtain(end - start);
        getChars(source, start, end, temp, 0);
        String ret = new String(temp, 0, end - start);
        recycle(temp);
        return ret;
    }
    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token: tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token: tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }
    public static String[] split(String text, String expression) {
        if (text.length() == 0) {
            return EMPTY_STRING_ARRAY;
        } else {
            return text.split(expression, -1);
        }
    }
    public static String[] split(String text, Pattern pattern) {
        if (text.length() == 0) {
            return EMPTY_STRING_ARRAY;
        } else {
            return pattern.split(text, -1);
        }
    }
    public interface StringSplitter extends Iterable<String> {
        public void setString(String string);
    }
    public static class SimpleStringSplitter implements StringSplitter, Iterator<String> {
        private String mString;
        private char mDelimiter;
        private int mPosition;
        private int mLength;
        public SimpleStringSplitter(char delimiter) {
            mDelimiter = delimiter;
        }
        public void setString(String string) {
            mString = string;
            mPosition = 0;
            mLength = mString.length();
        }
        public Iterator<String> iterator() {
            return this;
        }
        public boolean hasNext() {
            return mPosition < mLength;
        }
        public String next() {
            int end = mString.indexOf(mDelimiter, mPosition);
            if (end == -1) {
                end = mLength;
            }
            String nextString = mString.substring(mPosition, end);
            mPosition = end + 1; 
            return nextString;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    public static CharSequence stringOrSpannedString(CharSequence source) {
        if (source == null)
            return null;
        if (source instanceof SpannedString)
            return source;
        if (source instanceof Spanned)
            return new SpannedString(source);
        return source.toString();
    }
    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }
    public static int getTrimmedLength(CharSequence s) {
        int len = s.length();
        int start = 0;
        while (start < len && s.charAt(start) <= ' ') {
            start++;
        }
        int end = len;
        while (end > start && s.charAt(end - 1) <= ' ') {
            end--;
        }
        return end - start;
    }
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }
    public static CharSequence getReverse(CharSequence source,
                                          int start, int end) {
        return new Reverser(source, start, end);
    }
    private static class Reverser
    implements CharSequence, GetChars
    {
        public Reverser(CharSequence source, int start, int end) {
            mSource = source;
            mStart = start;
            mEnd = end;
        }
        public int length() {
            return mEnd - mStart;
        }
        public CharSequence subSequence(int start, int end) {
            char[] buf = new char[end - start];
            getChars(start, end, buf, 0);
            return new String(buf);
        }
        public String toString() {
            return subSequence(0, length()).toString();
        }
        public char charAt(int off) {
            return AndroidCharacter.getMirror(mSource.charAt(mEnd - 1 - off));
        }
        public void getChars(int start, int end, char[] dest, int destoff) {
            TextUtils.getChars(mSource, start + mStart, end + mStart,
                               dest, destoff);
            AndroidCharacter.mirror(dest, 0, end - start);
            int len = end - start;
            int n = (end - start) / 2;
            for (int i = 0; i < n; i++) {
                char tmp = dest[destoff + i];
                dest[destoff + i] = dest[destoff + len - i - 1];
                dest[destoff + len - i - 1] = tmp;
            }
        }
        private CharSequence mSource;
        private int mStart;
        private int mEnd;
    }
    public static final int ALIGNMENT_SPAN = 1;
    public static final int FOREGROUND_COLOR_SPAN = 2;
    public static final int RELATIVE_SIZE_SPAN = 3;
    public static final int SCALE_X_SPAN = 4;
    public static final int STRIKETHROUGH_SPAN = 5;
    public static final int UNDERLINE_SPAN = 6;
    public static final int STYLE_SPAN = 7;
    public static final int BULLET_SPAN = 8;
    public static final int QUOTE_SPAN = 9;
    public static final int LEADING_MARGIN_SPAN = 10;
    public static final int URL_SPAN = 11;
    public static final int BACKGROUND_COLOR_SPAN = 12;
    public static final int TYPEFACE_SPAN = 13;
    public static final int SUPERSCRIPT_SPAN = 14;
    public static final int SUBSCRIPT_SPAN = 15;
    public static final int ABSOLUTE_SIZE_SPAN = 16;
    public static final int TEXT_APPEARANCE_SPAN = 17;
    public static final int ANNOTATION = 18;
    public static void writeToParcel(CharSequence cs, Parcel p,
            int parcelableFlags) {
        if (cs instanceof Spanned) {
            p.writeInt(0);
            p.writeString(cs.toString());
            Spanned sp = (Spanned) cs;
            Object[] os = sp.getSpans(0, cs.length(), Object.class);
            for (int i = 0; i < os.length; i++) {
                Object o = os[i];
                Object prop = os[i];
                if (prop instanceof CharacterStyle) {
                    prop = ((CharacterStyle) prop).getUnderlying();
                }
                if (prop instanceof ParcelableSpan) {
                    ParcelableSpan ps = (ParcelableSpan)prop;
                    p.writeInt(ps.getSpanTypeId());
                    ps.writeToParcel(p, parcelableFlags);
                    writeWhere(p, sp, o);
                }
            }
            p.writeInt(0);
        } else {
            p.writeInt(1);
            if (cs != null) {
                p.writeString(cs.toString());
            } else {
                p.writeString(null);
            }
        }
    }
    private static void writeWhere(Parcel p, Spanned sp, Object o) {
        p.writeInt(sp.getSpanStart(o));
        p.writeInt(sp.getSpanEnd(o));
        p.writeInt(sp.getSpanFlags(o));
    }
    public static final Parcelable.Creator<CharSequence> CHAR_SEQUENCE_CREATOR
            = new Parcelable.Creator<CharSequence>() {
        public  CharSequence createFromParcel(Parcel p) {
            int kind = p.readInt();
            if (kind == 1)
                return p.readString();
            SpannableString sp = new SpannableString(p.readString());
            while (true) {
                kind = p.readInt();
                if (kind == 0)
                    break;
                switch (kind) {
                case ALIGNMENT_SPAN:
                    readSpan(p, sp, new AlignmentSpan.Standard(p));
                    break;
                case FOREGROUND_COLOR_SPAN:
                    readSpan(p, sp, new ForegroundColorSpan(p));
                    break;
                case RELATIVE_SIZE_SPAN:
                    readSpan(p, sp, new RelativeSizeSpan(p));
                    break;
                case SCALE_X_SPAN:
                    readSpan(p, sp, new ScaleXSpan(p));
                    break;
                case STRIKETHROUGH_SPAN:
                    readSpan(p, sp, new StrikethroughSpan(p));
                    break;
                case UNDERLINE_SPAN:
                    readSpan(p, sp, new UnderlineSpan(p));
                    break;
                case STYLE_SPAN:
                    readSpan(p, sp, new StyleSpan(p));
                    break;
                case BULLET_SPAN:
                    readSpan(p, sp, new BulletSpan(p));
                    break;
                case QUOTE_SPAN:
                    readSpan(p, sp, new QuoteSpan(p));
                    break;
                case LEADING_MARGIN_SPAN:
                    readSpan(p, sp, new LeadingMarginSpan.Standard(p));
                break;
                case URL_SPAN:
                    readSpan(p, sp, new URLSpan(p));
                    break;
                case BACKGROUND_COLOR_SPAN:
                    readSpan(p, sp, new BackgroundColorSpan(p));
                    break;
                case TYPEFACE_SPAN:
                    readSpan(p, sp, new TypefaceSpan(p));
                    break;
                case SUPERSCRIPT_SPAN:
                    readSpan(p, sp, new SuperscriptSpan(p));
                    break;
                case SUBSCRIPT_SPAN:
                    readSpan(p, sp, new SubscriptSpan(p));
                    break;
                case ABSOLUTE_SIZE_SPAN:
                    readSpan(p, sp, new AbsoluteSizeSpan(p));
                    break;
                case TEXT_APPEARANCE_SPAN:
                    readSpan(p, sp, new TextAppearanceSpan(p));
                    break;
                case ANNOTATION:
                    readSpan(p, sp, new Annotation(p));
                    break;
                default:
                    throw new RuntimeException("bogus span encoding " + kind);
                }
            }
            return sp;
        }
        public CharSequence[] newArray(int size)
        {
            return new CharSequence[size];
        }
    };
    public static void dumpSpans(CharSequence cs, Printer printer, String prefix) {
        if (cs instanceof Spanned) {
            Spanned sp = (Spanned) cs;
            Object[] os = sp.getSpans(0, cs.length(), Object.class);
            for (int i = 0; i < os.length; i++) {
                Object o = os[i];
                printer.println(prefix + cs.subSequence(sp.getSpanStart(o),
                        sp.getSpanEnd(o)) + ": "
                        + Integer.toHexString(System.identityHashCode(o))
                        + " " + o.getClass().getCanonicalName()
                         + " (" + sp.getSpanStart(o) + "-" + sp.getSpanEnd(o)
                         + ") fl=#" + sp.getSpanFlags(o));
            }
        } else {
            printer.println(prefix + cs + ": (no spans)");
        }
    }
    public static CharSequence replace(CharSequence template,
                                       String[] sources,
                                       CharSequence[] destinations) {
        SpannableStringBuilder tb = new SpannableStringBuilder(template);
        for (int i = 0; i < sources.length; i++) {
            int where = indexOf(tb, sources[i]);
            if (where >= 0)
                tb.setSpan(sources[i], where, where + sources[i].length(),
                           Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        for (int i = 0; i < sources.length; i++) {
            int start = tb.getSpanStart(sources[i]);
            int end = tb.getSpanEnd(sources[i]);
            if (start >= 0) {
                tb.replace(start, end, destinations[i]);
            }
        }
        return tb;
    }
    public static CharSequence expandTemplate(CharSequence template,
                                              CharSequence... values) {
        if (values.length > 9) {
            throw new IllegalArgumentException("max of 9 values are supported");
        }
        SpannableStringBuilder ssb = new SpannableStringBuilder(template);
        try {
            int i = 0;
            while (i < ssb.length()) {
                if (ssb.charAt(i) == '^') {
                    char next = ssb.charAt(i+1);
                    if (next == '^') {
                        ssb.delete(i+1, i+2);
                        ++i;
                        continue;
                    } else if (Character.isDigit(next)) {
                        int which = Character.getNumericValue(next) - 1;
                        if (which < 0) {
                            throw new IllegalArgumentException(
                                "template requests value ^" + (which+1));
                        }
                        if (which >= values.length) {
                            throw new IllegalArgumentException(
                                "template requests value ^" + (which+1) +
                                "; only " + values.length + " provided");
                        }
                        ssb.replace(i, i+2, values[which]);
                        i += values[which].length();
                        continue;
                    }
                }
                ++i;
            }
        } catch (IndexOutOfBoundsException ignore) {
        }
        return ssb;
    }
    public static int getOffsetBefore(CharSequence text, int offset) {
        if (offset == 0)
            return 0;
        if (offset == 1)
            return 0;
        char c = text.charAt(offset - 1);
        if (c >= '\uDC00' && c <= '\uDFFF') {
            char c1 = text.charAt(offset - 2);
            if (c1 >= '\uD800' && c1 <= '\uDBFF')
                offset -= 2;
            else
                offset -= 1;
        } else {
            offset -= 1;
        }
        if (text instanceof Spanned) {
            ReplacementSpan[] spans = ((Spanned) text).getSpans(offset, offset,
                                                       ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset && end > offset)
                    offset = start;
            }
        }
        return offset;
    }
    public static int getOffsetAfter(CharSequence text, int offset) {
        int len = text.length();
        if (offset == len)
            return len;
        if (offset == len - 1)
            return len;
        char c = text.charAt(offset);
        if (c >= '\uD800' && c <= '\uDBFF') {
            char c1 = text.charAt(offset + 1);
            if (c1 >= '\uDC00' && c1 <= '\uDFFF')
                offset += 2;
            else
                offset += 1;
        } else {
            offset += 1;
        }
        if (text instanceof Spanned) {
            ReplacementSpan[] spans = ((Spanned) text).getSpans(offset, offset,
                                                       ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset && end > offset)
                    offset = end;
            }
        }
        return offset;
    }
    private static void readSpan(Parcel p, Spannable sp, Object o) {
        sp.setSpan(o, p.readInt(), p.readInt(), p.readInt());
    }
    public static void copySpansFrom(Spanned source, int start, int end,
                                     Class kind,
                                     Spannable dest, int destoff) {
        if (kind == null) {
            kind = Object.class;
        }
        Object[] spans = source.getSpans(start, end, kind);
        for (int i = 0; i < spans.length; i++) {
            int st = source.getSpanStart(spans[i]);
            int en = source.getSpanEnd(spans[i]);
            int fl = source.getSpanFlags(spans[i]);
            if (st < start)
                st = start;
            if (en > end)
                en = end;
            dest.setSpan(spans[i], st - start + destoff, en - start + destoff,
                         fl);
        }
    }
    public enum TruncateAt {
        START,
        MIDDLE,
        END,
        MARQUEE,
    }
    public interface EllipsizeCallback {
        public void ellipsized(int start, int end);
    }
    private static String sEllipsis = null;
    public static CharSequence ellipsize(CharSequence text,
                                         TextPaint p,
                                         float avail, TruncateAt where) {
        return ellipsize(text, p, avail, where, false, null);
    }
    public static CharSequence ellipsize(CharSequence text,
                                         TextPaint p,
                                         float avail, TruncateAt where,
                                         boolean preserveLength,
                                         EllipsizeCallback callback) {
        if (sEllipsis == null) {
            Resources r = Resources.getSystem();
            sEllipsis = r.getString(R.string.ellipsis);
        }
        int len = text.length();
        if (!(text instanceof Spanned)) {
            float wid = p.measureText(text, 0, len);
            if (wid <= avail) {
                if (callback != null) {
                    callback.ellipsized(0, 0);
                }
                return text;
            }
            float ellipsiswid = p.measureText(sEllipsis);
            if (ellipsiswid > avail) {
                if (callback != null) {
                    callback.ellipsized(0, len);
                }
                if (preserveLength) {
                    char[] buf = obtain(len);
                    for (int i = 0; i < len; i++) {
                        buf[i] = '\uFEFF';
                    }
                    String ret = new String(buf, 0, len);
                    recycle(buf);
                    return ret;
                } else {
                    return "";
                }
            }
            if (where == TruncateAt.START) {
                int fit = p.breakText(text, 0, len, false,
                                      avail - ellipsiswid, null);
                if (callback != null) {
                    callback.ellipsized(0, len - fit);
                }
                if (preserveLength) {
                    return blank(text, 0, len - fit);
                } else {
                    return sEllipsis + text.toString().substring(len - fit, len);
                }
            } else if (where == TruncateAt.END) {
                int fit = p.breakText(text, 0, len, true,
                                      avail - ellipsiswid, null);
                if (callback != null) {
                    callback.ellipsized(fit, len);
                }
                if (preserveLength) {
                    return blank(text, fit, len);
                } else {
                    return text.toString().substring(0, fit) + sEllipsis;
                } 
            } else  {
                int right = p.breakText(text, 0, len, false,
                                        (avail - ellipsiswid) / 2, null);
                float used = p.measureText(text, len - right, len);
                int left = p.breakText(text, 0, len - right, true,
                                       avail - ellipsiswid - used, null);
                if (callback != null) {
                    callback.ellipsized(left, len - right);
                }
                if (preserveLength) {
                    return blank(text, left, len - right);
                } else {
                    String s = text.toString();
                    return s.substring(0, left) + sEllipsis +
                           s.substring(len - right, len);
                }
            }
        }
        float[] wid = new float[len * 2];
        TextPaint temppaint = new TextPaint();
        Spanned sp = (Spanned) text;
        int next;
        for (int i = 0; i < len; i = next) {
            next = sp.nextSpanTransition(i, len, MetricAffectingSpan.class);
            Styled.getTextWidths(p, temppaint, sp, i, next, wid, null);
            System.arraycopy(wid, 0, wid, len + i, next - i);
        }
        float sum = 0;
        for (int i = 0; i < len; i++) {
            sum += wid[len + i];
        }
        if (sum <= avail) {
            if (callback != null) {
                callback.ellipsized(0, 0);
            }
            return text;
        }
        float ellipsiswid = p.measureText(sEllipsis);
        if (ellipsiswid > avail) {
            if (callback != null) {
                callback.ellipsized(0, len);
            }
            if (preserveLength) {
                char[] buf = obtain(len);
                for (int i = 0; i < len; i++) {
                    buf[i] = '\uFEFF';
                }
                SpannableString ss = new SpannableString(new String(buf, 0, len));
                recycle(buf);
                copySpansFrom(sp, 0, len, Object.class, ss, 0);
                return ss;
            } else {
                return "";
            }
        }
        if (where == TruncateAt.START) {
            sum = 0;
            int i;
            for (i = len; i >= 0; i--) {
                float w = wid[len + i - 1];
                if (w + sum + ellipsiswid > avail) {
                    break;
                }
                sum += w;
            }
            if (callback != null) {
                callback.ellipsized(0, i);
            }
            if (preserveLength) {
                SpannableString ss = new SpannableString(blank(text, 0, i));
                copySpansFrom(sp, 0, len, Object.class, ss, 0);
                return ss;
            } else {
                SpannableStringBuilder out = new SpannableStringBuilder(sEllipsis);
                out.insert(1, text, i, len);
                return out;
            }
        } else if (where == TruncateAt.END) {
            sum = 0;
            int i;
            for (i = 0; i < len; i++) {
                float w = wid[len + i];
                if (w + sum + ellipsiswid > avail) {
                    break;
                }
                sum += w;
            }
            if (callback != null) {
                callback.ellipsized(i, len);
            }
            if (preserveLength) {
                SpannableString ss = new SpannableString(blank(text, i, len));
                copySpansFrom(sp, 0, len, Object.class, ss, 0);
                return ss;
            } else {
                SpannableStringBuilder out = new SpannableStringBuilder(sEllipsis);
                out.insert(0, text, 0, i);
                return out;
            }
        } else  {
            float lsum = 0, rsum = 0;
            int left = 0, right = len;
            float ravail = (avail - ellipsiswid) / 2;
            for (right = len; right >= 0; right--) {
                float w = wid[len + right - 1];
                if (w + rsum > ravail) {
                    break;
                }
                rsum += w;
            }
            float lavail = avail - ellipsiswid - rsum;
            for (left = 0; left < right; left++) {
                float w = wid[len + left];
                if (w + lsum > lavail) {
                    break;
                }
                lsum += w;
            }
            if (callback != null) {
                callback.ellipsized(left, right);
            }
            if (preserveLength) {
                SpannableString ss = new SpannableString(blank(text, left, right));
                copySpansFrom(sp, 0, len, Object.class, ss, 0);
                return ss;
            } else {
                SpannableStringBuilder out = new SpannableStringBuilder(sEllipsis);
                out.insert(0, text, 0, left);
                out.insert(out.length(), text, right, len);
                return out;
            }
        }
    }
    private static String blank(CharSequence source, int start, int end) {
        int len = source.length();
        char[] buf = obtain(len);
        if (start != 0) {
            getChars(source, 0, start, buf, 0);
        }
        if (end != len) {
            getChars(source, end, len, buf, end);
        }
        if (start != end) {
            buf[start] = '\u2026';
            for (int i = start + 1; i < end; i++) {
                buf[i] = '\uFEFF';
            }
        }
        String ret = new String(buf, 0, len);
        recycle(buf);
        return ret;
    }
    public static CharSequence commaEllipsize(CharSequence text,
                                              TextPaint p, float avail,
                                              String oneMore,
                                              String more) {
        int len = text.length();
        char[] buf = new char[len];
        TextUtils.getChars(text, 0, len, buf, 0);
        int commaCount = 0;
        for (int i = 0; i < len; i++) {
            if (buf[i] == ',') {
                commaCount++;
            }
        }
        float[] wid;
        if (text instanceof Spanned) {
            Spanned sp = (Spanned) text;
            TextPaint temppaint = new TextPaint();
            wid = new float[len * 2];
            int next;
            for (int i = 0; i < len; i = next) {
                next = sp.nextSpanTransition(i, len, MetricAffectingSpan.class);
                Styled.getTextWidths(p, temppaint, sp, i, next, wid, null);
                System.arraycopy(wid, 0, wid, len + i, next - i);
            }
            System.arraycopy(wid, len, wid, 0, len);
        } else {
            wid = new float[len];
            p.getTextWidths(text, 0, len, wid);
        }
        int ok = 0;
        int okRemaining = commaCount + 1;
        String okFormat = "";
        int w = 0;
        int count = 0;
        for (int i = 0; i < len; i++) {
            w += wid[i];
            if (buf[i] == ',') {
                count++;
                int remaining = commaCount - count + 1;
                float moreWid;
                String format;
                if (remaining == 1) {
                    format = " " + oneMore;
                } else {
                    format = " " + String.format(more, remaining);
                }
                moreWid = p.measureText(format);
                if (w + moreWid <= avail) {
                    ok = i + 1;
                    okRemaining = remaining;
                    okFormat = format;
                }
            }
        }
        if (w <= avail) {
            return text;
        } else {
            SpannableStringBuilder out = new SpannableStringBuilder(okFormat);
            out.insert(0, text, 0, ok);
            return out;
        }
    }
     static char[] obtain(int len) {
        char[] buf;
        synchronized (sLock) {
            buf = sTemp;
            sTemp = null;
        }
        if (buf == null || buf.length < len)
            buf = new char[ArrayUtils.idealCharArraySize(len)];
        return buf;
    }
     static void recycle(char[] temp) {
        if (temp.length > 1000)
            return;
        synchronized (sLock) {
            sTemp = temp;
        }
    }
    public static String htmlEncode(String s) {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            switch (c) {
            case '<':
                sb.append("&lt;"); 
                break;
            case '>':
                sb.append("&gt;"); 
                break;
            case '&':
                sb.append("&amp;"); 
                break;
            case '\'':
                sb.append("&apos;"); 
                break;
            case '"':
                sb.append("&quot;"); 
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public static CharSequence concat(CharSequence... text) {
        if (text.length == 0) {
            return "";
        }
        if (text.length == 1) {
            return text[0];
        }
        boolean spanned = false;
        for (int i = 0; i < text.length; i++) {
            if (text[i] instanceof Spanned) {
                spanned = true;
                break;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length; i++) {
            sb.append(text[i]);
        }
        if (!spanned) {
            return sb.toString();
        }
        SpannableString ss = new SpannableString(sb);
        int off = 0;
        for (int i = 0; i < text.length; i++) {
            int len = text[i].length();
            if (text[i] instanceof Spanned) {
                copySpansFrom((Spanned) text[i], 0, len, Object.class, ss, off);
            }
            off += len;
        }
        return new SpannedString(ss);
    }
    public static boolean isGraphic(CharSequence str) {
        final int len = str.length();
        for (int i=0; i<len; i++) {
            int gc = Character.getType(str.charAt(i));
            if (gc != Character.CONTROL
                    && gc != Character.FORMAT
                    && gc != Character.SURROGATE
                    && gc != Character.UNASSIGNED
                    && gc != Character.LINE_SEPARATOR
                    && gc != Character.PARAGRAPH_SEPARATOR
                    && gc != Character.SPACE_SEPARATOR) {
                return true;
            }
        }
        return false;
    }
    public static boolean isGraphic(char c) {
        int gc = Character.getType(c);
        return     gc != Character.CONTROL
                && gc != Character.FORMAT
                && gc != Character.SURROGATE
                && gc != Character.UNASSIGNED
                && gc != Character.LINE_SEPARATOR
                && gc != Character.PARAGRAPH_SEPARATOR
                && gc != Character.SPACE_SEPARATOR;
    }
    public static boolean isDigitsOnly(CharSequence str) {
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    public static boolean isPrintableAscii(final char c) {
        final int asciiFirst = 0x20;
        final int asciiLast = 0x7E;  
        return (asciiFirst <= c && c <= asciiLast) || c == '\r' || c == '\n';
    }
    public static boolean isPrintableAsciiOnly(final CharSequence str) {
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!isPrintableAscii(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    public static final int CAP_MODE_CHARACTERS
            = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
    public static final int CAP_MODE_WORDS
            = InputType.TYPE_TEXT_FLAG_CAP_WORDS;
    public static final int CAP_MODE_SENTENCES
            = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
    public static int getCapsMode(CharSequence cs, int off, int reqModes) {
        if (off < 0) {
            return 0;
        }
        int i;
        char c;
        int mode = 0;
        if ((reqModes&CAP_MODE_CHARACTERS) != 0) {
            mode |= CAP_MODE_CHARACTERS;
        }
        if ((reqModes&(CAP_MODE_WORDS|CAP_MODE_SENTENCES)) == 0) {
            return mode;
        }
        for (i = off; i > 0; i--) {
            c = cs.charAt(i - 1);
            if (c != '"' && c != '\'' &&
                Character.getType(c) != Character.START_PUNCTUATION) {
                break;
            }
        }
        int j = i;
        while (j > 0 && ((c = cs.charAt(j - 1)) == ' ' || c == '\t')) {
            j--;
        }
        if (j == 0 || cs.charAt(j - 1) == '\n') {
            return mode | CAP_MODE_WORDS;
        }
        if ((reqModes&CAP_MODE_SENTENCES) == 0) {
            if (i != j) mode |= CAP_MODE_WORDS;
            return mode;
        }
        if (i == j) {
            return mode;
        }
        for (; j > 0; j--) {
            c = cs.charAt(j - 1);
            if (c != '"' && c != '\'' &&
                Character.getType(c) != Character.END_PUNCTUATION) {
                break;
            }
        }
        if (j > 0) {
            c = cs.charAt(j - 1);
            if (c == '.' || c == '?' || c == '!') {
                if (c == '.') {
                    for (int k = j - 2; k >= 0; k--) {
                        c = cs.charAt(k);
                        if (c == '.') {
                            return mode;
                        }
                        if (!Character.isLetter(c)) {
                            break;
                        }
                    }
                }
                return mode | CAP_MODE_SENTENCES;
            }
        }
        return mode;
    }
    private static Object sLock = new Object();
    private static char[] sTemp = null;
}
