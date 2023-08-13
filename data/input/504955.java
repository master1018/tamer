public class DynamicLayout
extends Layout
{
    private static final int PRIORITY = 128;
    public DynamicLayout(CharSequence base,
                         TextPaint paint,
                         int width, Alignment align,
                         float spacingmult, float spacingadd,
                         boolean includepad) {
        this(base, base, paint, width, align, spacingmult, spacingadd,
             includepad);
    }
    public DynamicLayout(CharSequence base, CharSequence display,
                         TextPaint paint,
                         int width, Alignment align,
                         float spacingmult, float spacingadd,
                         boolean includepad) {
        this(base, display, paint, width, align, spacingmult, spacingadd,
             includepad, null, 0);
    }
    public DynamicLayout(CharSequence base, CharSequence display,
                         TextPaint paint,
                         int width, Alignment align,
                         float spacingmult, float spacingadd,
                         boolean includepad,
                         TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        super((ellipsize == null) 
                ? display 
                : (display instanceof Spanned) 
                    ? new SpannedEllipsizer(display) 
                    : new Ellipsizer(display),
              paint, width, align, spacingmult, spacingadd);
        mBase = base;
        mDisplay = display;
        if (ellipsize != null) {
            mInts = new PackedIntVector(COLUMNS_ELLIPSIZE);
            mEllipsizedWidth = ellipsizedWidth;
            mEllipsizeAt = ellipsize;
        } else {
            mInts = new PackedIntVector(COLUMNS_NORMAL);
            mEllipsizedWidth = width;
            mEllipsizeAt = ellipsize;
        }
        mObjects = new PackedObjectVector<Directions>(1);
        mIncludePad = includepad;
        if (ellipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = ellipsizedWidth;
            e.mMethod = ellipsize;
            mEllipsize = true;
        }
        int[] start;
        if (ellipsize != null) {
            start = new int[COLUMNS_ELLIPSIZE];
            start[ELLIPSIS_START] = ELLIPSIS_UNDEFINED;
        } else {
            start = new int[COLUMNS_NORMAL];
        }
        Directions[] dirs = new Directions[] { DIRS_ALL_LEFT_TO_RIGHT };
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        int asc = fm.ascent;
        int desc = fm.descent;
        start[DIR] = DIR_LEFT_TO_RIGHT << DIR_SHIFT;
        start[TOP] = 0;
        start[DESCENT] = desc;
        mInts.insertAt(0, start);
        start[TOP] = desc - asc;
        mInts.insertAt(1, start);
        mObjects.insertAt(0, dirs);
        reflow(base, 0, 0, base.length());
        if (base instanceof Spannable) {
            if (mWatcher == null)
                mWatcher = new ChangeWatcher(this);
            Spannable sp = (Spannable) base;
            ChangeWatcher[] spans = sp.getSpans(0, sp.length(), ChangeWatcher.class);
            for (int i = 0; i < spans.length; i++)
                sp.removeSpan(spans[i]);
            sp.setSpan(mWatcher, 0, base.length(),
                       Spannable.SPAN_INCLUSIVE_INCLUSIVE |
                       (PRIORITY << Spannable.SPAN_PRIORITY_SHIFT));
        }
    }
    private void reflow(CharSequence s, int where, int before, int after) {
        if (s != mBase)
            return;
        CharSequence text = mDisplay;
        int len = text.length();
        int find = TextUtils.lastIndexOf(text, '\n', where - 1);
        if (find < 0)
            find = 0;
        else
            find = find + 1;
        {
            int diff = where - find;
            before += diff;
            after += diff;
            where -= diff;
        }
        int look = TextUtils.indexOf(text, '\n', where + after);
        if (look < 0)
            look = len;
        else
            look++; 
        int change = look - (where + after);
        before += change;
        after += change;
        if (text instanceof Spanned) {
            Spanned sp = (Spanned) text;
            boolean again;
            do {
                again = false;
                Object[] force = sp.getSpans(where, where + after,
                                             WrapTogetherSpan.class);
                for (int i = 0; i < force.length; i++) {
                    int st = sp.getSpanStart(force[i]);
                    int en = sp.getSpanEnd(force[i]);
                    if (st < where) {
                        again = true;
                        int diff = where - st;
                        before += diff;
                        after += diff;
                        where -= diff;
                    }
                    if (en > where + after) {
                        again = true;
                        int diff = en - (where + after);
                        before += diff;
                        after += diff;
                    }
                }
            } while (again);
        }
        int startline = getLineForOffset(where);
        int startv = getLineTop(startline);
        int endline = getLineForOffset(where + before);
        if (where + after == len)
            endline = getLineCount();
        int endv = getLineTop(endline);
        boolean islast = (endline == getLineCount());
        StaticLayout reflowed;
        synchronized (sLock) {
            reflowed = sStaticLayout;
            sStaticLayout = null;
        }
        if (reflowed == null)
            reflowed = new StaticLayout(true);
        reflowed.generate(text, where, where + after,
                                      getPaint(), getWidth(), getAlignment(),
                                      getSpacingMultiplier(), getSpacingAdd(),
                                      false, true, mEllipsize,
                                      mEllipsizedWidth, mEllipsizeAt);
        int n = reflowed.getLineCount();
        if (where + after != len &&
            reflowed.getLineStart(n - 1) == where + after)
            n--;
        mInts.deleteAt(startline, endline - startline);
        mObjects.deleteAt(startline, endline - startline);
        int ht = reflowed.getLineTop(n);
        int toppad = 0, botpad = 0;
        if (mIncludePad && startline == 0) {
            toppad = reflowed.getTopPadding();
            mTopPadding = toppad;
            ht -= toppad;
        }
        if (mIncludePad && islast) {
            botpad = reflowed.getBottomPadding();
            mBottomPadding = botpad;
            ht += botpad;
        }
        mInts.adjustValuesBelow(startline, START, after - before);
        mInts.adjustValuesBelow(startline, TOP, startv - endv + ht);
        int[] ints;
        if (mEllipsize) {
            ints = new int[COLUMNS_ELLIPSIZE];
            ints[ELLIPSIS_START] = ELLIPSIS_UNDEFINED;
        } else {
            ints = new int[COLUMNS_NORMAL];
        }
        Directions[] objects = new Directions[1];
        for (int i = 0; i < n; i++) {
            ints[START] = reflowed.getLineStart(i) |
                          (reflowed.getParagraphDirection(i) << DIR_SHIFT) |
                          (reflowed.getLineContainsTab(i) ? TAB_MASK : 0);
            int top = reflowed.getLineTop(i) + startv;
            if (i > 0)
                top -= toppad;
            ints[TOP] = top;
            int desc = reflowed.getLineDescent(i);
            if (i == n - 1)
                desc += botpad;
            ints[DESCENT] = desc;
            objects[0] = reflowed.getLineDirections(i);
            if (mEllipsize) {
                ints[ELLIPSIS_START] = reflowed.getEllipsisStart(i);
                ints[ELLIPSIS_COUNT] = reflowed.getEllipsisCount(i);
            }
            mInts.insertAt(startline + i, ints);
            mObjects.insertAt(startline + i, objects);
        }
        synchronized (sLock) {
            sStaticLayout = reflowed;
        }
    }
    private void dump(boolean show) {
        int n = getLineCount();
        for (int i = 0; i < n; i++) {
            System.out.print("line " + i + ": " + getLineStart(i) + " to " + getLineEnd(i) + " ");
            if (show) {
                System.out.print(getText().subSequence(getLineStart(i),
                                                       getLineEnd(i)));
            }
            System.out.println("");
        }
        System.out.println("");
    }
    public int getLineCount() {
        return mInts.size() - 1;
    }
    public int getLineTop(int line) {
        return mInts.getValue(line, TOP);
    }
    public int getLineDescent(int line) {
        return mInts.getValue(line, DESCENT);
    }
    public int getLineStart(int line) {
        return mInts.getValue(line, START) & START_MASK;
    }
    public boolean getLineContainsTab(int line) {
        return (mInts.getValue(line, TAB) & TAB_MASK) != 0;
    }
    public int getParagraphDirection(int line) {
        return mInts.getValue(line, DIR) >> DIR_SHIFT;
    }
    public final Directions getLineDirections(int line) {
        return mObjects.getValue(line, 0);
    }
    public int getTopPadding() {
        return mTopPadding;
    }
    public int getBottomPadding() {
        return mBottomPadding;
    }
    @Override
    public int getEllipsizedWidth() {
        return mEllipsizedWidth;
    }
    private static class ChangeWatcher
    implements TextWatcher, SpanWatcher
    {
        public ChangeWatcher(DynamicLayout layout) {
            mLayout = new WeakReference(layout);
        }
        private void reflow(CharSequence s, int where, int before, int after) {
            DynamicLayout ml = (DynamicLayout) mLayout.get();
            if (ml != null)
                ml.reflow(s, where, before, after);
            else if (s instanceof Spannable)
                ((Spannable) s).removeSpan(this);
        }
        public void beforeTextChanged(CharSequence s,
                                      int where, int before, int after) {
            ;
        }
        public void onTextChanged(CharSequence s,
                                  int where, int before, int after) {
            reflow(s, where, before, after);
        }
        public void afterTextChanged(Editable s) {
            ;
        }
        public void onSpanAdded(Spannable s, Object o, int start, int end) {
            if (o instanceof UpdateLayout)
                reflow(s, start, end - start, end - start);
        }
        public void onSpanRemoved(Spannable s, Object o, int start, int end) {
            if (o instanceof UpdateLayout)
                reflow(s, start, end - start, end - start);
        }
        public void onSpanChanged(Spannable s, Object o, int start, int end,
                                  int nstart, int nend) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
                reflow(s, nstart, nend - nstart, nend - nstart);
            }
        }
        private WeakReference mLayout;
    }
    public int getEllipsisStart(int line) {
        if (mEllipsizeAt == null) {
            return 0;
        }
        return mInts.getValue(line, ELLIPSIS_START);
    }
    public int getEllipsisCount(int line) {
        if (mEllipsizeAt == null) {
            return 0;
        }
        return mInts.getValue(line, ELLIPSIS_COUNT);
    }
    private CharSequence mBase;
    private CharSequence mDisplay;
    private ChangeWatcher mWatcher;
    private boolean mIncludePad;
    private boolean mEllipsize;
    private int mEllipsizedWidth;
    private TextUtils.TruncateAt mEllipsizeAt;
    private PackedIntVector mInts;
    private PackedObjectVector<Directions> mObjects;
    private int mTopPadding, mBottomPadding;
    private static StaticLayout sStaticLayout = new StaticLayout(true);
    private static Object sLock = new Object();
    private static final int START = 0;
    private static final int DIR = START;
    private static final int TAB = START;
    private static final int TOP = 1;
    private static final int DESCENT = 2;
    private static final int COLUMNS_NORMAL = 3;
    private static final int ELLIPSIS_START = 3;
    private static final int ELLIPSIS_COUNT = 4;
    private static final int COLUMNS_ELLIPSIZE = 5;
    private static final int START_MASK = 0x1FFFFFFF;
    private static final int DIR_MASK   = 0xC0000000;
    private static final int DIR_SHIFT  = 30;
    private static final int TAB_MASK   = 0x20000000;
    private static final int ELLIPSIS_UNDEFINED = 0x80000000;
}
