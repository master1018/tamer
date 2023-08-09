public class AnimationDrawable extends DrawableContainer implements Runnable, Animatable {
    private final AnimationState mAnimationState;
    private int mCurFrame = -1;
    private boolean mMutated;
    public AnimationDrawable() {
        this(null, null);
    }
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (visible) {
            if (changed || restart) {
                setFrame(0, true, true);
            }
        } else {
            unscheduleSelf(this);
        }
        return changed;
    }
    public void start() {
        if (!isRunning()) {
            run();
        }
    }
    public void stop() {
        if (isRunning()) {
            unscheduleSelf(this);
        }
    }
    public boolean isRunning() {
        return mCurFrame > -1;
    }
    public void run() {
        nextFrame(false);
    }
    @Override
    public void unscheduleSelf(Runnable what) {
        mCurFrame = -1;
        super.unscheduleSelf(what);
    }
    public int getNumberOfFrames() {
        return mAnimationState.getChildCount();
    }
    public Drawable getFrame(int index) {
        return mAnimationState.getChildren()[index];
    }
    public int getDuration(int i) {
        return mAnimationState.mDurations[i];
    }
    public boolean isOneShot() {
        return mAnimationState.mOneShot;
    }
    public void setOneShot(boolean oneShot) {
        mAnimationState.mOneShot = oneShot;
    }
    public void addFrame(Drawable frame, int duration) {
        mAnimationState.addFrame(frame, duration);
    }
    private void nextFrame(boolean unschedule) {
        int next = mCurFrame+1;
        final int N = mAnimationState.getChildCount();
        if (next >= N) {
            next = 0;
        }
        setFrame(next, unschedule, !mAnimationState.mOneShot || next < (N - 1));
    }
    private void setFrame(int frame, boolean unschedule, boolean animate) {
        if (frame >= mAnimationState.getChildCount()) {
            return;
        }
        mCurFrame = frame;
        selectDrawable(frame);
        if (unschedule) {
            unscheduleSelf(this);
        }
        if (animate) {
            scheduleSelf(this, SystemClock.uptimeMillis() + mAnimationState.mDurations[frame]);
        }
    }
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        TypedArray a = r.obtainAttributes(attrs,
                com.android.internal.R.styleable.AnimationDrawable);
        super.inflateWithAttributes(r, parser, a,
                com.android.internal.R.styleable.AnimationDrawable_visible);
        mAnimationState.setVariablePadding(a.getBoolean(
                com.android.internal.R.styleable.AnimationDrawable_variablePadding, false));
        mAnimationState.mOneShot = a.getBoolean(
                com.android.internal.R.styleable.AnimationDrawable_oneshot, false);
        a.recycle();
        int type;
        final int innerDepth = parser.getDepth()+1;
        int depth;
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT &&
                ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }
            a = r.obtainAttributes(attrs, com.android.internal.R.styleable.AnimationDrawableItem);
            int duration = a.getInt(
                    com.android.internal.R.styleable.AnimationDrawableItem_duration, -1);
            if (duration < 0) {
                throw new XmlPullParserException(
                        parser.getPositionDescription()
                        + ": <item> tag requires a 'duration' attribute");
            }
            int drawableRes = a.getResourceId(
                    com.android.internal.R.styleable.AnimationDrawableItem_drawable, 0);
            a.recycle();
            Drawable dr;
            if (drawableRes != 0) {
                dr = r.getDrawable(drawableRes);
            } else {
                while ((type=parser.next()) == XmlPullParser.TEXT) {
                }
                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(parser.getPositionDescription() +
                            ": <item> tag requires a 'drawable' attribute or child tag" +
                            " defining a drawable");
                }
                dr = Drawable.createFromXmlInner(r, parser, attrs);
            }
            mAnimationState.addFrame(dr, duration);
            if (dr != null) {
                dr.setCallback(this);
            }
        }
        setFrame(0, true, false);
    }
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mAnimationState.mDurations = mAnimationState.mDurations.clone();
            mMutated = true;
        }
        return this;
    }
    private final static class AnimationState extends DrawableContainerState {
        private int[] mDurations;
        private boolean mOneShot;
        AnimationState(AnimationState orig, AnimationDrawable owner,
                Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                mDurations = orig.mDurations;
                mOneShot = orig.mOneShot;
            } else {
                mDurations = new int[getChildren().length];
                mOneShot = true;
            }
        }
        @Override
        public Drawable newDrawable() {
            return new AnimationDrawable(this, null);
        }
        @Override
        public Drawable newDrawable(Resources res) {
            return new AnimationDrawable(this, res);
        }
        public void addFrame(Drawable dr, int dur) {
            int pos = super.addChild(dr);
            mDurations[pos] = dur;
        }
        @Override
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[] newDurations = new int[newSize];
            System.arraycopy(mDurations, 0, newDurations, 0, oldSize);
            mDurations = newDurations;
        }
    }
    private AnimationDrawable(AnimationState state, Resources res) {
        AnimationState as = new AnimationState(state, this, res);
        mAnimationState = as;
        setConstantState(as);
        if (state != null) {
            setFrame(0, true, false);
        }
    }
}
