public class
StaticLayout
extends Layout
{
    public StaticLayout(CharSequence source, TextPaint paint,
                        int width,
                        Alignment align, float spacingmult, float spacingadd,
                        boolean includepad) {
        this(source, 0, source.length(), paint, width, align,
             spacingmult, spacingadd, includepad);
    }
    public StaticLayout(CharSequence source, int bufstart, int bufend,
                        TextPaint paint, int outerwidth,
                        Alignment align,
                        float spacingmult, float spacingadd,
                        boolean includepad) {
        this(source, bufstart, bufend, paint, outerwidth, align,
             spacingmult, spacingadd, includepad, null, 0);
    }
    public StaticLayout(CharSequence source, int bufstart, int bufend,
                        TextPaint paint, int outerwidth,
                        Alignment align,
                        float spacingmult, float spacingadd,
                        boolean includepad,
                        TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        super((ellipsize == null)
                ? source 
                : (source instanceof Spanned)
                    ? new SpannedEllipsizer(source)
                    : new Ellipsizer(source),
              paint, outerwidth, align, spacingmult, spacingadd);
        if (ellipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = ellipsizedWidth;
            e.mMethod = ellipsize;
            mEllipsizedWidth = ellipsizedWidth;
            mColumns = COLUMNS_ELLIPSIZE;
        } else {
            mColumns = COLUMNS_NORMAL;
            mEllipsizedWidth = outerwidth;
        }
        mLines = new int[ArrayUtils.idealIntArraySize(2 * mColumns)];
        mLineDirections = new Directions[
                             ArrayUtils.idealIntArraySize(2 * mColumns)];
        generate(source, bufstart, bufend, paint, outerwidth, align,
                 spacingmult, spacingadd, includepad, includepad,
                 ellipsize != null, ellipsizedWidth, ellipsize);
        mChdirs = null;
        mChs = null;
        mWidths = null;
        mFontMetricsInt = null;
    }
     StaticLayout(boolean ellipsize) {
        super(null, null, 0, null, 0, 0);
        mColumns = COLUMNS_ELLIPSIZE;
        mLines = new int[ArrayUtils.idealIntArraySize(2 * mColumns)];
        mLineDirections = new Directions[
                             ArrayUtils.idealIntArraySize(2 * mColumns)];
    }
     void generate(CharSequence source, int bufstart, int bufend,
                        TextPaint paint, int outerwidth,
                        Alignment align,
                        float spacingmult, float spacingadd,
                        boolean includepad, boolean trackpad,
                        boolean breakOnlyAtSpaces,
                        float ellipsizedWidth, TextUtils.TruncateAt where) {
        mLineCount = 0;
        int v = 0;
        boolean needMultiply = (spacingmult != 1 || spacingadd != 0);
        Paint.FontMetricsInt fm = mFontMetricsInt;
        int[] choosehtv = null;
        int end = TextUtils.indexOf(source, '\n', bufstart, bufend);
        int bufsiz = end >= 0 ? end - bufstart : bufend - bufstart;
        boolean first = true;
        if (mChdirs == null) {
            mChdirs = new byte[ArrayUtils.idealByteArraySize(bufsiz + 1)];
            mChs = new char[ArrayUtils.idealCharArraySize(bufsiz + 1)];
            mWidths = new float[ArrayUtils.idealIntArraySize((bufsiz + 1) * 2)];
        }
        byte[] chdirs = mChdirs;
        char[] chs = mChs;
        float[] widths = mWidths;
        AlteredCharSequence alter = null;
        Spanned spanned = null;
        if (source instanceof Spanned)
            spanned = (Spanned) source;
        int DEFAULT_DIR = DIR_LEFT_TO_RIGHT; 
        for (int start = bufstart; start <= bufend; start = end) {
            if (first)
                first = false;
            else
                end = TextUtils.indexOf(source, '\n', start, bufend);
            if (end < 0)
                end = bufend;
            else
                end++;
            int firstWidthLineCount = 1;
            int firstwidth = outerwidth;
            int restwidth = outerwidth;
            LineHeightSpan[] chooseht = null;
            if (spanned != null) {
                LeadingMarginSpan[] sp;
                sp = spanned.getSpans(start, end, LeadingMarginSpan.class);
                for (int i = 0; i < sp.length; i++) {
                    LeadingMarginSpan lms = sp[i];
                    firstwidth -= sp[i].getLeadingMargin(true);
                    restwidth -= sp[i].getLeadingMargin(false);
                    if (lms instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                        firstWidthLineCount = ((LeadingMarginSpan.LeadingMarginSpan2)lms).getLeadingMarginLineCount();
                    }
                }
                chooseht = spanned.getSpans(start, end, LineHeightSpan.class);
                if (chooseht.length != 0) {
                    if (choosehtv == null ||
                        choosehtv.length < chooseht.length) {
                        choosehtv = new int[ArrayUtils.idealIntArraySize(
                                            chooseht.length)];
                    }
                    for (int i = 0; i < chooseht.length; i++) {
                        int o = spanned.getSpanStart(chooseht[i]);
                        if (o < start) {
                            choosehtv[i] = getLineTop(getLineForOffset(o)); 
                        } else {
                            choosehtv[i] = v;
                        }
                    }
                }
            }
            if (end - start > chdirs.length) {
                chdirs = new byte[ArrayUtils.idealByteArraySize(end - start)];
                mChdirs = chdirs;
            }
            if (end - start > chs.length) {
                chs = new char[ArrayUtils.idealCharArraySize(end - start)];
                mChs = chs;
            }
            if ((end - start) * 2 > widths.length) {
                widths = new float[ArrayUtils.idealIntArraySize((end - start) * 2)];
                mWidths = widths;
            }
            TextUtils.getChars(source, start, end, chs, 0);
            final int n = end - start;
            boolean easy = true;
            boolean altered = false;
            int dir = DEFAULT_DIR; 
            for (int i = 0; i < n; i++) {
                if (chs[i] >= FIRST_RIGHT_TO_LEFT) {
                    easy = false;
                    break;
                }
            }
            if (source instanceof Spanned) {
                Spanned sp = (Spanned) source;
                ReplacementSpan[] spans = sp.getSpans(start, end, ReplacementSpan.class);
                for (int y = 0; y < spans.length; y++) {
                    int a = sp.getSpanStart(spans[y]);
                    int b = sp.getSpanEnd(spans[y]);
                    for (int x = a; x < b; x++) {
                        chs[x - start] = '\uFFFC';
                    }
                }
            }
            if (!easy) {
                dir = bidi(dir, chs, chdirs, n, false);
                for (int i = 0; i < n; i++) {
                    if (chdirs[i] == Character.DIRECTIONALITY_RIGHT_TO_LEFT) {
                        int j;
                        for (j = i; j < n; j++) {
                            if (chdirs[j] !=
                                Character.DIRECTIONALITY_RIGHT_TO_LEFT)
                                break;
                        }
                        if (AndroidCharacter.mirror(chs, i, j - i))
                            altered = true;
                        i = j - 1;
                    }
                }
            }
            CharSequence sub;
            if (altered) {
                if (alter == null)
                    alter = AlteredCharSequence.make(source, chs, start, end);
                else
                    alter.update(chs, start, end);
                sub = alter;
            } else {
                sub = source;
            }
            int width = firstwidth;
            float w = 0;
            int here = start;
            int ok = start;
            float okwidth = w;
            int okascent = 0, okdescent = 0, oktop = 0, okbottom = 0;
            int fit = start;
            float fitwidth = w;
            int fitascent = 0, fitdescent = 0, fittop = 0, fitbottom = 0;
            boolean tab = false;
            int next;
            for (int i = start; i < end; i = next) {
                if (spanned == null)
                    next = end;
                else
                    next = spanned.nextSpanTransition(i, end,
                                                      MetricAffectingSpan.
                                                      class);
                if (spanned == null) {
                    paint.getTextWidths(sub, i, next, widths);
                    System.arraycopy(widths, 0, widths,
                                     end - start + (i - start), next - i);
                    paint.getFontMetricsInt(fm);
                } else {
                    mWorkPaint.baselineShift = 0;
                    Styled.getTextWidths(paint, mWorkPaint,
                                         spanned, i, next,
                                         widths, fm);
                    System.arraycopy(widths, 0, widths,
                                     end - start + (i - start), next - i);
                    if (mWorkPaint.baselineShift < 0) {
                        fm.ascent += mWorkPaint.baselineShift;
                        fm.top += mWorkPaint.baselineShift;
                    } else {
                        fm.descent += mWorkPaint.baselineShift;
                        fm.bottom += mWorkPaint.baselineShift;
                    }
                }
                int fmtop = fm.top;
                int fmbottom = fm.bottom;
                int fmascent = fm.ascent;
                int fmdescent = fm.descent;
                if (false) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = i; j < next; j++) {
                        sb.append(widths[j - start + (end - start)]);
                        sb.append(' ');
                    }
                    Log.e("text", sb.toString());
                }
                for (int j = i; j < next; j++) {
                    char c = chs[j - start];
                    float before = w;
                    if (c == '\n') {
                        ;
                    } else if (c == '\t') {
                        w = Layout.nextTab(sub, start, end, w, null);
                        tab = true;
                    } else if (c >= 0xD800 && c <= 0xDFFF && j + 1 < next) {
                        int emoji = Character.codePointAt(chs, j - start);
                        if (emoji >= MIN_EMOJI && emoji <= MAX_EMOJI) {
                            Bitmap bm = EMOJI_FACTORY.
                                getBitmapFromAndroidPua(emoji);
                            if (bm != null) {
                                Paint whichPaint;
                                if (spanned == null) {
                                    whichPaint = paint;
                                } else {
                                    whichPaint = mWorkPaint;
                                }
                                float wid = (float) bm.getWidth() *
                                            -whichPaint.ascent() /
                                            bm.getHeight();
                                w += wid;
                                tab = true;
                                j++;
                            } else {
                                w += widths[j - start + (end - start)];
                            }
                        } else {
                            w += widths[j - start + (end - start)];
                        }
                    } else {
                        w += widths[j - start + (end - start)];
                    }
                    if (w <= width) {
                        fitwidth = w;
                        fit = j + 1;
                        if (fmtop < fittop)
                            fittop = fmtop;
                        if (fmascent < fitascent)
                            fitascent = fmascent;
                        if (fmdescent > fitdescent)
                            fitdescent = fmdescent;
                        if (fmbottom > fitbottom)
                            fitbottom = fmbottom;
                        if (c == ' ' || c == '\t' ||
                            ((c == '.'  || c == ',' || c == ':' || c == ';') &&
                             (j - 1 < here || !Character.isDigit(chs[j - 1 - start])) &&
                             (j + 1 >= next || !Character.isDigit(chs[j + 1 - start]))) ||
                            ((c == '/' || c == '-') &&
                             (j + 1 >= next || !Character.isDigit(chs[j + 1 - start]))) ||
                            (c >= FIRST_CJK && isIdeographic(c, true) &&
                             j + 1 < next && isIdeographic(chs[j + 1 - start], false))) {
                            okwidth = w;
                            ok = j + 1;
                            if (fittop < oktop)
                                oktop = fittop;
                            if (fitascent < okascent)
                                okascent = fitascent;
                            if (fitdescent > okdescent)
                                okdescent = fitdescent;
                            if (fitbottom > okbottom)
                                okbottom = fitbottom;
                        }
                    } else if (breakOnlyAtSpaces) {
                        if (ok != here) {
                            while (ok < next && chs[ok - start] == ' ') {
                                ok++;
                            }
                            v = out(source,
                                    here, ok,
                                    okascent, okdescent, oktop, okbottom,
                                    v,
                                    spacingmult, spacingadd, chooseht,
                                    choosehtv, fm, tab,
                                    needMultiply, start, chdirs, dir, easy,
                                    ok == bufend, includepad, trackpad,
                                    widths, start, end - start,
                                    where, ellipsizedWidth, okwidth,
                                    paint);
                            here = ok;
                        } else {
                            fitwidth = w;
                            fit = j + 1;
                            if (fmtop < fittop)
                                fittop = fmtop;
                            if (fmascent < fitascent)
                                fitascent = fmascent;
                            if (fmdescent > fitdescent)
                                fitdescent = fmdescent;
                            if (fmbottom > fitbottom)
                                fitbottom = fmbottom;
                        }
                    } else {
                        if (ok != here) {
                            while (ok < next && chs[ok - start] == ' ') {
                                ok++;
                            }
                            v = out(source,
                                    here, ok,
                                    okascent, okdescent, oktop, okbottom,
                                    v,
                                    spacingmult, spacingadd, chooseht,
                                    choosehtv, fm, tab,
                                    needMultiply, start, chdirs, dir, easy,
                                    ok == bufend, includepad, trackpad,
                                    widths, start, end - start,
                                    where, ellipsizedWidth, okwidth,
                                    paint);
                            here = ok;
                        } else if (fit != here) {
                            v = out(source,
                                    here, fit,
                                    fitascent, fitdescent,
                                    fittop, fitbottom,
                                    v,
                                    spacingmult, spacingadd, chooseht,
                                    choosehtv, fm, tab,
                                    needMultiply, start, chdirs, dir, easy,
                                    fit == bufend, includepad, trackpad,
                                    widths, start, end - start,
                                    where, ellipsizedWidth, fitwidth,
                                    paint);
                            here = fit;
                        } else {
                            measureText(paint, mWorkPaint,
                                        source, here, here + 1, fm, tab,
                                        null);
                            v = out(source,
                                    here, here+1,
                                    fm.ascent, fm.descent,
                                    fm.top, fm.bottom,
                                    v,
                                    spacingmult, spacingadd, chooseht,
                                    choosehtv, fm, tab,
                                    needMultiply, start, chdirs, dir, easy,
                                    here + 1 == bufend, includepad,
                                    trackpad,
                                    widths, start, end - start,
                                    where, ellipsizedWidth,
                                    widths[here - start], paint);
                            here = here + 1;
                        }
                        if (here < i) {
                            j = next = here; 
                        } else {
                            j = here - 1;    
                        }
                        ok = fit = here;
                        w = 0;
                        fitascent = fitdescent = fittop = fitbottom = 0;
                        okascent = okdescent = oktop = okbottom = 0;
                        if (--firstWidthLineCount <= 0) {
                            width = restwidth;
                        }
                    }
                }
            }
            if (end != here) {
                if ((fittop | fitbottom | fitdescent | fitascent) == 0) {
                    paint.getFontMetricsInt(fm);
                    fittop = fm.top;
                    fitbottom = fm.bottom;
                    fitascent = fm.ascent;
                    fitdescent = fm.descent;
                }
                v = out(source,
                        here, end, fitascent, fitdescent,
                        fittop, fitbottom,
                        v,
                        spacingmult, spacingadd, chooseht,
                        choosehtv, fm, tab,
                        needMultiply, start, chdirs, dir, easy,
                        end == bufend, includepad, trackpad,
                        widths, start, end - start,
                        where, ellipsizedWidth, w, paint);
            }
            start = end;
            if (end == bufend)
                break;
        }
        if (bufend == bufstart || source.charAt(bufend - 1) == '\n') {
            paint.getFontMetricsInt(fm);
            v = out(source,
                    bufend, bufend, fm.ascent, fm.descent,
                    fm.top, fm.bottom,
                    v,
                    spacingmult, spacingadd, null,
                    null, fm, false,
                    needMultiply, bufend, chdirs, DEFAULT_DIR, true,
                    true, includepad, trackpad,
                    widths, bufstart, 0,
                    where, ellipsizedWidth, 0, paint);
        }
    }
     static int bidi(int dir, char[] chs, byte[] chInfo, int n, 
            boolean hasInfo) {
        AndroidCharacter.getDirectionalities(chs, chInfo, n);
        if (dir != DIR_REQUEST_LTR && dir != DIR_REQUEST_RTL) {
            dir = dir >= 0 ? DIR_LEFT_TO_RIGHT : DIR_RIGHT_TO_LEFT;
            for (int j = 0; j < n; j++) {
                int d = chInfo[j];
                if (d == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
                    dir = DIR_LEFT_TO_RIGHT;
                    break;
                }
                if (d == Character.DIRECTIONALITY_RIGHT_TO_LEFT) {
                    dir = DIR_RIGHT_TO_LEFT;
                    break;
                }
            }
        }
        final byte SOR = dir == DIR_LEFT_TO_RIGHT ?
                Character.DIRECTIONALITY_LEFT_TO_RIGHT :
                Character.DIRECTIONALITY_RIGHT_TO_LEFT;
        for (int j = 0; j < n; j++) {
            if (chInfo[j] == Character.NON_SPACING_MARK) {
                if (j == 0)
                    chInfo[j] = SOR;
                else
                    chInfo[j] = chInfo[j - 1];
            }
        }
        byte cur = SOR;
        for (int j = 0; j < n; j++) {
            byte d = chInfo[j];
            if (d == Character.DIRECTIONALITY_LEFT_TO_RIGHT ||
                d == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                d == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
                cur = d;
            else if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER) {
                 if (cur ==
                    Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
                    chInfo[j] = Character.DIRECTIONALITY_ARABIC_NUMBER;
            }
        }
        for (int j = 0; j < n; j++) {
            if (chInfo[j] == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
                chInfo[j] = Character.DIRECTIONALITY_RIGHT_TO_LEFT;
        }
        for (int j = 1; j < n - 1; j++) {
            byte d = chInfo[j];
            byte prev = chInfo[j - 1];
            byte next = chInfo[j + 1];
            if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR) {
                if (prev == Character.DIRECTIONALITY_EUROPEAN_NUMBER &&
                    next == Character.DIRECTIONALITY_EUROPEAN_NUMBER)
                    chInfo[j] = Character.DIRECTIONALITY_EUROPEAN_NUMBER;
            } else if (d == Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR) {
                if (prev == Character.DIRECTIONALITY_EUROPEAN_NUMBER &&
                    next == Character.DIRECTIONALITY_EUROPEAN_NUMBER)
                    chInfo[j] = Character.DIRECTIONALITY_EUROPEAN_NUMBER;
                if (prev == Character.DIRECTIONALITY_ARABIC_NUMBER &&
                    next == Character.DIRECTIONALITY_ARABIC_NUMBER)
                    chInfo[j] = Character.DIRECTIONALITY_ARABIC_NUMBER;
            }
        }
        boolean adjacent = false;
        for (int j = 0; j < n; j++) {
            byte d = chInfo[j];
            if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER)
                adjacent = true;
            else if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR && adjacent)
                chInfo[j] = Character.DIRECTIONALITY_EUROPEAN_NUMBER;
            else
                adjacent = false;
        }
        adjacent = false;
        for (int j = n - 1; j >= 0; j--) {
            byte d = chInfo[j];
            if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER)
                adjacent = true;
            else if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR) {
                if (adjacent)
                    chInfo[j] = Character.DIRECTIONALITY_EUROPEAN_NUMBER;
                else
                    chInfo[j] = Character.DIRECTIONALITY_OTHER_NEUTRALS;
            }
            else {
                adjacent = false;
                if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR ||
                    d == Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR ||
                    d == Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR ||
                    d == Character.DIRECTIONALITY_SEGMENT_SEPARATOR)
                    chInfo[j] = Character.DIRECTIONALITY_OTHER_NEUTRALS;
            }
        }
        cur = SOR;
        for (int j = 0; j < n; j++) {
            byte d = chInfo[j];
            if (d == SOR ||
                d == Character.DIRECTIONALITY_LEFT_TO_RIGHT ||
                d == Character.DIRECTIONALITY_RIGHT_TO_LEFT)
                cur = d;
            if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER)
                chInfo[j] = cur;
        }
        cur = SOR;
        for (int j = 0; j < n; j++) {
            byte d = chInfo[j];
            if (d == Character.DIRECTIONALITY_LEFT_TO_RIGHT ||
                d == Character.DIRECTIONALITY_RIGHT_TO_LEFT) {
                cur = d;
            } else if (d == Character.DIRECTIONALITY_EUROPEAN_NUMBER ||
                       d == Character.DIRECTIONALITY_ARABIC_NUMBER) {
                cur = Character.DIRECTIONALITY_RIGHT_TO_LEFT;
            } else {
                byte dd = SOR;
                int k;
                for (k = j + 1; k < n; k++) {
                    dd = chInfo[k];
                    if (dd == Character.DIRECTIONALITY_LEFT_TO_RIGHT ||
                        dd == Character.DIRECTIONALITY_RIGHT_TO_LEFT) {
                        break;
                    }
                    if (dd == Character.DIRECTIONALITY_EUROPEAN_NUMBER ||
                        dd == Character.DIRECTIONALITY_ARABIC_NUMBER) {
                        dd = Character.DIRECTIONALITY_RIGHT_TO_LEFT;
                        break;
                    }
                }
                for (int y = j; y < k; y++) {
                    if (dd == cur)
                        chInfo[y] = cur;
                    else
                        chInfo[y] = SOR;
                }
                j = k - 1;
            }
        }
        for (int j = 0; j < n; j++) {
            char c = chs[j];
            if (c == '\t' || (c >= 0xD800 && c <= 0xDFFF)) {
                chInfo[j] = SOR;
            }
        }
        return dir;
    }
    private static final char FIRST_CJK = '\u2E80';
    private static final boolean isIdeographic(char c, boolean includeNonStarters) {
        if (c >= '\u2E80' && c <= '\u2FFF') {
            return true; 
        }
        if (c == '\u3000') {
            return true; 
        }
        if (c >= '\u3040' && c <= '\u309F') {
            if (!includeNonStarters) {
                switch (c) {
                case '\u3041': 
                case '\u3043': 
                case '\u3045': 
                case '\u3047': 
                case '\u3049': 
                case '\u3063': 
                case '\u3083': 
                case '\u3085': 
                case '\u3087': 
                case '\u308E': 
                case '\u3095': 
                case '\u3096': 
                case '\u309B': 
                case '\u309C': 
                case '\u309D': 
                case '\u309E': 
                    return false;
                }
            }
            return true; 
        }
        if (c >= '\u30A0' && c <= '\u30FF') {
            if (!includeNonStarters) {
                switch (c) {
                case '\u30A0': 
                case '\u30A1': 
                case '\u30A3': 
                case '\u30A5': 
                case '\u30A7': 
                case '\u30A9': 
                case '\u30C3': 
                case '\u30E3': 
                case '\u30E5': 
                case '\u30E7': 
                case '\u30EE': 
                case '\u30F5': 
                case '\u30F6': 
                case '\u30FB': 
                case '\u30FC': 
                case '\u30FD': 
                case '\u30FE': 
                    return false;
                }
            }
            return true; 
        }
        if (c >= '\u3400' && c <= '\u4DB5') {
            return true; 
        }
        if (c >= '\u4E00' && c <= '\u9FBB') {
            return true; 
        }
        if (c >= '\uF900' && c <= '\uFAD9') {
            return true; 
        }
        if (c >= '\uA000' && c <= '\uA48F') {
            return true; 
        }
        if (c >= '\uA490' && c <= '\uA4CF') {
            return true; 
        }
        if (c >= '\uFE62' && c <= '\uFE66') {
            return true; 
        }
        if (c >= '\uFF10' && c <= '\uFF19') {
            return true; 
        }
        return false;
    }
    private static int getFit(TextPaint paint,
                              TextPaint workPaint,
                       CharSequence text, int start, int end,
                       float wid) {
        int high = end + 1, low = start - 1, guess;
        while (high - low > 1) {
            guess = (high + low) / 2;
            if (measureText(paint, workPaint,
                            text, start, guess, null, true, null) > wid)
                high = guess;
            else
                low = guess;
        }
        if (low < start)
            return start;
        else
            return low;
    }
    private int out(CharSequence text, int start, int end,
                      int above, int below, int top, int bottom, int v,
                      float spacingmult, float spacingadd,
                      LineHeightSpan[] chooseht, int[] choosehtv,
                      Paint.FontMetricsInt fm, boolean tab,
                      boolean needMultiply, int pstart, byte[] chdirs,
                      int dir, boolean easy, boolean last,
                      boolean includepad, boolean trackpad,
                      float[] widths, int widstart, int widoff,
                      TextUtils.TruncateAt ellipsize, float ellipsiswidth,
                      float textwidth, TextPaint paint) {
        int j = mLineCount;
        int off = j * mColumns;
        int want = off + mColumns + TOP;
        int[] lines = mLines;
        if (want >= lines.length) {
            int nlen = ArrayUtils.idealIntArraySize(want + 1);
            int[] grow = new int[nlen];
            System.arraycopy(lines, 0, grow, 0, lines.length);
            mLines = grow;
            lines = grow;
            Directions[] grow2 = new Directions[nlen];
            System.arraycopy(mLineDirections, 0, grow2, 0,
                             mLineDirections.length);
            mLineDirections = grow2;
        }
        if (chooseht != null) {
            fm.ascent = above;
            fm.descent = below;
            fm.top = top;
            fm.bottom = bottom;
            for (int i = 0; i < chooseht.length; i++) {
                if (chooseht[i] instanceof LineHeightSpan.WithDensity) {
                    ((LineHeightSpan.WithDensity) chooseht[i]).
                        chooseHeight(text, start, end, choosehtv[i], v, fm, paint);
                } else {
                    chooseht[i].chooseHeight(text, start, end, choosehtv[i], v, fm);
                }
            }
            above = fm.ascent;
            below = fm.descent;
            top = fm.top;
            bottom = fm.bottom;
        }
        if (j == 0) {
            if (trackpad) {
                mTopPadding = top - above;
            }
            if (includepad) {
                above = top;
            }
        }
        if (last) {
            if (trackpad) {
                mBottomPadding = bottom - below;
            }
            if (includepad) {
                below = bottom;
            }
        }
        int extra;
        if (needMultiply) {
            double ex = (below - above) * (spacingmult - 1) + spacingadd;
            if (ex >= 0) {
                extra = (int)(ex + 0.5);
            } else {
                extra = -(int)(-ex + 0.5);
            }
        } else {
            extra = 0;
        }
        lines[off + START] = start;
        lines[off + TOP] = v;
        lines[off + DESCENT] = below + extra;
        v += (below - above) + extra;
        lines[off + mColumns + START] = end;
        lines[off + mColumns + TOP] = v;
        if (tab)
            lines[off + TAB] |= TAB_MASK;
        {
            lines[off + DIR] |= dir << DIR_SHIFT;
            int cur = Character.DIRECTIONALITY_LEFT_TO_RIGHT;
            int count = 0;
            if (!easy) {
                for (int k = start; k < end; k++) {
                    if (chdirs[k - pstart] != cur) {
                        count++;
                        cur = chdirs[k - pstart];
                    }
                }
            }
            Directions linedirs;
            if (count == 0) {
                linedirs = DIRS_ALL_LEFT_TO_RIGHT;
            } else {
                short[] ld = new short[count + 1];
                cur = Character.DIRECTIONALITY_LEFT_TO_RIGHT;
                count = 0;
                int here = start;
                for (int k = start; k < end; k++) {
                    if (chdirs[k - pstart] != cur) {
                        ld[count++] = (short) (k - here);
                        cur = chdirs[k - pstart];
                        here = k;
                    }
                }
                ld[count] = (short) (end - here);
                if (count == 1 && ld[0] == 0) {
                    linedirs = DIRS_ALL_RIGHT_TO_LEFT;
                } else {
                    linedirs = new Directions(ld);
                }
            }
            mLineDirections[j] = linedirs;
            if (ellipsize != null && (ellipsize != TextUtils.TruncateAt.MARQUEE || j != 0)) {
                calculateEllipsis(start, end, widths, widstart, widoff,
                                  ellipsiswidth, ellipsize, j,
                                  textwidth, paint);
            }
        }
        mLineCount++;
        return v;
    }
    private void calculateEllipsis(int linestart, int lineend,
                                   float[] widths, int widstart, int widoff,
                                   float avail, TextUtils.TruncateAt where,
                                   int line, float textwidth, TextPaint paint) {
        int len = lineend - linestart;
        if (textwidth <= avail) {
            mLines[mColumns * line + ELLIPSIS_START] = 0;
            mLines[mColumns * line + ELLIPSIS_COUNT] = 0;
            return;
        }
        float ellipsiswid = paint.measureText("\u2026");
        int ellipsisStart, ellipsisCount;
        if (where == TextUtils.TruncateAt.START) {
            float sum = 0;
            int i;
            for (i = len; i >= 0; i--) {
                float w = widths[i - 1 + linestart - widstart + widoff];
                if (w + sum + ellipsiswid > avail) {
                    break;
                }
                sum += w;
            }
            ellipsisStart = 0;
            ellipsisCount = i;
        } else if (where == TextUtils.TruncateAt.END || where == TextUtils.TruncateAt.MARQUEE) {
            float sum = 0;
            int i;
            for (i = 0; i < len; i++) {
                float w = widths[i + linestart - widstart + widoff];
                if (w + sum + ellipsiswid > avail) {
                    break;
                }
                sum += w;
            }
            ellipsisStart = i;
            ellipsisCount = len - i;
        } else  {
            float lsum = 0, rsum = 0;
            int left = 0, right = len;
            float ravail = (avail - ellipsiswid) / 2;
            for (right = len; right >= 0; right--) {
                float w = widths[right - 1 + linestart - widstart + widoff];
                if (w + rsum > ravail) {
                    break;
                }
                rsum += w;
            }
            float lavail = avail - ellipsiswid - rsum;
            for (left = 0; left < right; left++) {
                float w = widths[left + linestart - widstart + widoff];
                if (w + lsum > lavail) {
                    break;
                }
                lsum += w;
            }
            ellipsisStart = left;
            ellipsisCount = right - left;
        }
        mLines[mColumns * line + ELLIPSIS_START] = ellipsisStart;
        mLines[mColumns * line + ELLIPSIS_COUNT] = ellipsisCount;
    }
    public int getLineForVertical(int vertical) {
        int high = mLineCount;
        int low = -1;
        int guess;
        int[] lines = mLines;
        while (high - low > 1) {
            guess = (high + low) >> 1;
            if (lines[mColumns * guess + TOP] > vertical){
                high = guess;
            } else {
                low = guess;
            }
        }
        if (low < 0) {
            return 0;
        } else {
            return low;
        }
    }
    public int getLineCount() {
        return mLineCount;
    }
    public int getLineTop(int line) {
        return mLines[mColumns * line + TOP];    
    }
    public int getLineDescent(int line) {
        return mLines[mColumns * line + DESCENT];   
    }
    public int getLineStart(int line) {
        return mLines[mColumns * line + START] & START_MASK;
    }
    public int getParagraphDirection(int line) {
        return mLines[mColumns * line + DIR] >> DIR_SHIFT;
    }
    public boolean getLineContainsTab(int line) {
        return (mLines[mColumns * line + TAB] & TAB_MASK) != 0;
    }
    public final Directions getLineDirections(int line) {
        return mLineDirections[line];
    }
    public int getTopPadding() {
        return mTopPadding;
    }
    public int getBottomPadding() {
        return mBottomPadding;
    }
    @Override
    public int getEllipsisCount(int line) {
        if (mColumns < COLUMNS_ELLIPSIZE) {
            return 0;
        }
        return mLines[mColumns * line + ELLIPSIS_COUNT];
    }
    @Override
    public int getEllipsisStart(int line) {
        if (mColumns < COLUMNS_ELLIPSIZE) {
            return 0;
        }
        return mLines[mColumns * line + ELLIPSIS_START];
    }
    @Override
    public int getEllipsizedWidth() {
        return mEllipsizedWidth;
    }
    private int mLineCount;
    private int mTopPadding, mBottomPadding;
    private int mColumns;
    private int mEllipsizedWidth;
    private static final int COLUMNS_NORMAL = 3;
    private static final int COLUMNS_ELLIPSIZE = 5;
    private static final int START = 0;
    private static final int DIR = START;
    private static final int TAB = START;
    private static final int TOP = 1;
    private static final int DESCENT = 2;
    private static final int ELLIPSIS_START = 3;
    private static final int ELLIPSIS_COUNT = 4;
    private int[] mLines;
    private Directions[] mLineDirections;
    private static final int START_MASK = 0x1FFFFFFF;
    private static final int DIR_MASK   = 0xC0000000;
    private static final int DIR_SHIFT  = 30;
    private static final int TAB_MASK   = 0x20000000;
    private static final char FIRST_RIGHT_TO_LEFT = '\u0590';
    private byte[] mChdirs;
    private char[] mChs;
    private float[] mWidths;
    private Paint.FontMetricsInt mFontMetricsInt = new Paint.FontMetricsInt();
}
