public class StateListDrawable extends DrawableContainer {
    private static final boolean DEFAULT_DITHER = true;
    private final StateListState mStateListState;
    private boolean mMutated;
    public StateListDrawable() {
        this(null, null);
    }
    public void addState(int[] stateSet, Drawable drawable) {
        if (drawable != null) {
            mStateListState.addStateSet(stateSet, drawable);
            onStateChange(getState());
        }
    }
    @Override
    public boolean isStateful() {
        return true;
    }
    @Override
    protected boolean onStateChange(int[] stateSet) {
        int idx = mStateListState.indexOfStateSet(stateSet);
        if (idx < 0) {
            idx = mStateListState.indexOfStateSet(StateSet.WILD_CARD);
        }
        if (selectDrawable(idx)) {
            return true;
        }
        return super.onStateChange(stateSet);
    }
    @Override
    public void inflate(Resources r, XmlPullParser parser,
            AttributeSet attrs)
            throws XmlPullParserException, IOException {
        TypedArray a = r.obtainAttributes(attrs,
                com.android.internal.R.styleable.StateListDrawable);
        super.inflateWithAttributes(r, parser, a,
                com.android.internal.R.styleable.StateListDrawable_visible);
        mStateListState.setVariablePadding(a.getBoolean(
                com.android.internal.R.styleable.StateListDrawable_variablePadding, false));
        mStateListState.setConstantSize(a.getBoolean(
                com.android.internal.R.styleable.StateListDrawable_constantSize, false));
        setDither(a.getBoolean(com.android.internal.R.styleable.StateListDrawable_dither,
                               DEFAULT_DITHER));
        a.recycle();
        int type;
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
            int drawableRes = 0;
            int i;
            int j = 0;
            final int numAttrs = attrs.getAttributeCount();
            int[] states = new int[numAttrs];
            for (i = 0; i < numAttrs; i++) {
                final int stateResId = attrs.getAttributeNameResource(i);
                if (stateResId == 0) break;
                if (stateResId == com.android.internal.R.attr.drawable) {
                    drawableRes = attrs.getAttributeResourceValue(i, 0);
                } else {
                    states[j++] = attrs.getAttributeBooleanValue(i, false)
                            ? stateResId
                            : -stateResId;
                }
            }
            states = StateSet.trimStateSet(states, j);
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
            mStateListState.addStateSet(states, dr);
        }
        onStateChange(getState());
    }
    StateListState getStateListState() {
        return mStateListState;
    }
    public int getStateCount() {
        return mStateListState.getChildCount();
    }
    public int[] getStateSet(int index) {
        return mStateListState.mStateSets[index];
    }
    public Drawable getStateDrawable(int index) {
        return mStateListState.getChildren()[index];
    }
    public int getStateDrawableIndex(int[] stateSet) {
        return mStateListState.indexOfStateSet(stateSet);
    }
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            final int[][] sets = mStateListState.mStateSets;
            final int count = sets.length;
            mStateListState.mStateSets = new int[count][];
            for (int i = 0; i < count; i++) {
                final int[] set = sets[i];
                if (set != null) {
                    mStateListState.mStateSets[i] = set.clone();
                }
            }
            mMutated = true;
        }
        return this;
    }
    static final class StateListState extends DrawableContainerState {
        private int[][] mStateSets;
        StateListState(StateListState orig, StateListDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                mStateSets = orig.mStateSets;
            } else {
                mStateSets = new int[getChildren().length][];
            }
        }
        int addStateSet(int[] stateSet, Drawable drawable) {
            final int pos = addChild(drawable);
            mStateSets[pos] = stateSet;
            return pos;
        }
        private int indexOfStateSet(int[] stateSet) {
            final int[][] stateSets = mStateSets;
            final int N = getChildCount();
            for (int i = 0; i < N; i++) {
                if (StateSet.stateSetMatches(stateSets[i], stateSet)) {
                    return i;
                }
            }
            return -1;
        }
        @Override
        public Drawable newDrawable() {
            return new StateListDrawable(this, null);
        }
        @Override
        public Drawable newDrawable(Resources res) {
            return new StateListDrawable(this, res);
        }
        @Override
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            final int[][] newStateSets = new int[newSize][];
            System.arraycopy(mStateSets, 0, newStateSets, 0, oldSize);
            mStateSets = newStateSets;
        }
    }
    private StateListDrawable(StateListState state, Resources res) {
        StateListState as = new StateListState(state, this, res);
        mStateListState = as;
        setConstantState(as);
        onStateChange(getState());
    }
}
