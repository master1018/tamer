public class LevelListDrawable extends DrawableContainer {
    private final LevelListState mLevelListState;
    private boolean mMutated;
    public LevelListDrawable() {
        this(null, null);
    }
    public void addLevel(int low, int high, Drawable drawable) {
        if (drawable != null) {
            mLevelListState.addLevel(low, high, drawable);
            onLevelChange(getLevel());
        }
    }
    @Override
    protected boolean onLevelChange(int level) {
        int idx = mLevelListState.indexOfLevel(level);
        if (selectDrawable(idx)) {
            return true;
        }
        return super.onLevelChange(level);
    }
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);
        int type;
        int low = 0;
        final int innerDepth = parser.getDepth() + 1;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth
                || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }
            TypedArray a = r.obtainAttributes(attrs,
                    com.android.internal.R.styleable.LevelListDrawableItem);
            low = a.getInt(
                    com.android.internal.R.styleable.LevelListDrawableItem_minLevel, 0);
            int high = a.getInt(
                    com.android.internal.R.styleable.LevelListDrawableItem_maxLevel, 0);
            int drawableRes = a.getResourceId(
                    com.android.internal.R.styleable.LevelListDrawableItem_drawable, 0);
            a.recycle();
            if (high < 0) {
                throw new XmlPullParserException(parser.getPositionDescription()
                        + ": <item> tag requires a 'maxLevel' attribute");
            }
            Drawable dr;
            if (drawableRes != 0) {
                dr = r.getDrawable(drawableRes);
            } else {
                while ((type = parser.next()) == XmlPullParser.TEXT) {
                }
                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(
                            parser.getPositionDescription()
                                    + ": <item> tag requires a 'drawable' attribute or "
                                    + "child tag defining a drawable");
                }
                dr = Drawable.createFromXmlInner(r, parser, attrs);
            }
            mLevelListState.addLevel(low, high, dr);
        }
        onLevelChange(getLevel());
    }
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mLevelListState.mLows = mLevelListState.mLows.clone();
            mLevelListState.mHighs = mLevelListState.mHighs.clone();
            mMutated = true;
        }
        return this;
    }
    private final static class LevelListState extends DrawableContainerState {
        private int[] mLows;
        private int[] mHighs;
        LevelListState(LevelListState orig, LevelListDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                mLows = orig.mLows;
                mHighs = orig.mHighs;
            } else {
                mLows = new int[getChildren().length];
                mHighs = new int[getChildren().length];
            }
        }
        public void addLevel(int low, int high, Drawable drawable) {
            int pos = addChild(drawable);
            mLows[pos] = low;
            mHighs[pos] = high;
        }
        public int indexOfLevel(int level) {
            final int[] lows = mLows;
            final int[] highs = mHighs;
            final int N = getChildCount();
            for (int i = 0; i < N; i++) {
                if (level >= lows[i] && level <= highs[i]) {
                    return i;
                }
            }
            return -1;
        }
        @Override
        public Drawable newDrawable() {
            return new LevelListDrawable(this, null);
        }
        @Override
        public Drawable newDrawable(Resources res) {
            return new LevelListDrawable(this, res);
        }
        @Override
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[] newInts = new int[newSize];
            System.arraycopy(mLows, 0, newInts, 0, oldSize);
            mLows = newInts;
            newInts = new int[newSize];
            System.arraycopy(mHighs, 0, newInts, 0, oldSize);
            mHighs = newInts;
        }
    }
    private LevelListDrawable(LevelListState state, Resources res) {
        LevelListState as = new LevelListState(state, this, res);
        mLevelListState = as;
        setConstantState(as);
        onLevelChange(getLevel());
    }
}
