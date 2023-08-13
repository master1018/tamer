public final class ViewTreeObserver {
    private CopyOnWriteArrayList<OnGlobalFocusChangeListener> mOnGlobalFocusListeners;
    private CopyOnWriteArrayList<OnGlobalLayoutListener> mOnGlobalLayoutListeners;
    private CopyOnWriteArrayList<OnPreDrawListener> mOnPreDrawListeners;
    private CopyOnWriteArrayList<OnTouchModeChangeListener> mOnTouchModeChangeListeners;
    private CopyOnWriteArrayList<OnComputeInternalInsetsListener> mOnComputeInternalInsetsListeners;
    private CopyOnWriteArrayList<OnScrollChangedListener> mOnScrollChangedListeners;
    private boolean mAlive = true;
    public interface OnGlobalFocusChangeListener {
        public void onGlobalFocusChanged(View oldFocus, View newFocus);
    }
    public interface OnGlobalLayoutListener {
        public void onGlobalLayout();
    }
    public interface OnPreDrawListener {
        public boolean onPreDraw();
    }
    public interface OnTouchModeChangeListener {
        public void onTouchModeChanged(boolean isInTouchMode);
    }
    public interface OnScrollChangedListener {
        public void onScrollChanged();
    }
    public final static class InternalInsetsInfo {
        public final Rect contentInsets = new Rect();
        public final Rect visibleInsets = new Rect();
        public static final int TOUCHABLE_INSETS_FRAME = 0;
        public static final int TOUCHABLE_INSETS_CONTENT = 1;
        public static final int TOUCHABLE_INSETS_VISIBLE = 2;
        public void setTouchableInsets(int val) {
            mTouchableInsets = val;
        }
        public int getTouchableInsets() {
            return mTouchableInsets;
        }
        int mTouchableInsets;
        void reset() {
            final Rect givenContent = contentInsets;
            final Rect givenVisible = visibleInsets;
            givenContent.left = givenContent.top = givenContent.right
                    = givenContent.bottom = givenVisible.left = givenVisible.top
                    = givenVisible.right = givenVisible.bottom = 0;
            mTouchableInsets = TOUCHABLE_INSETS_FRAME;
        }
        @Override public boolean equals(Object o) {
            try {
                if (o == null) {
                    return false;
                }
                InternalInsetsInfo other = (InternalInsetsInfo)o;
                if (!contentInsets.equals(other.contentInsets)) {
                    return false;
                }
                if (!visibleInsets.equals(other.visibleInsets)) {
                    return false;
                }
                return mTouchableInsets == other.mTouchableInsets;
            } catch (ClassCastException e) {
                return false;
            }
        }
        void set(InternalInsetsInfo other) {
            contentInsets.set(other.contentInsets);
            visibleInsets.set(other.visibleInsets);
            mTouchableInsets = other.mTouchableInsets;
        }
    }
    public interface OnComputeInternalInsetsListener {
        public void onComputeInternalInsets(InternalInsetsInfo inoutInfo);
    }
    ViewTreeObserver() {
    }
    void merge(ViewTreeObserver observer) {
        if (observer.mOnGlobalFocusListeners != null) {
            if (mOnGlobalFocusListeners != null) {
                mOnGlobalFocusListeners.addAll(observer.mOnGlobalFocusListeners);
            } else {
                mOnGlobalFocusListeners = observer.mOnGlobalFocusListeners;
            }
        }
        if (observer.mOnGlobalLayoutListeners != null) {
            if (mOnGlobalLayoutListeners != null) {
                mOnGlobalLayoutListeners.addAll(observer.mOnGlobalLayoutListeners);
            } else {
                mOnGlobalLayoutListeners = observer.mOnGlobalLayoutListeners;
            }
        }
        if (observer.mOnPreDrawListeners != null) {
            if (mOnPreDrawListeners != null) {
                mOnPreDrawListeners.addAll(observer.mOnPreDrawListeners);
            } else {
                mOnPreDrawListeners = observer.mOnPreDrawListeners;
            }
        }
        if (observer.mOnTouchModeChangeListeners != null) {
            if (mOnTouchModeChangeListeners != null) {
                mOnTouchModeChangeListeners.addAll(observer.mOnTouchModeChangeListeners);
            } else {
                mOnTouchModeChangeListeners = observer.mOnTouchModeChangeListeners;
            }
        }
        if (observer.mOnComputeInternalInsetsListeners != null) {
            if (mOnComputeInternalInsetsListeners != null) {
                mOnComputeInternalInsetsListeners.addAll(observer.mOnComputeInternalInsetsListeners);
            } else {
                mOnComputeInternalInsetsListeners = observer.mOnComputeInternalInsetsListeners;
            }
        }
        observer.kill();
    }
    public void addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener listener) {
        checkIsAlive();
        if (mOnGlobalFocusListeners == null) {
            mOnGlobalFocusListeners = new CopyOnWriteArrayList<OnGlobalFocusChangeListener>();
        }
        mOnGlobalFocusListeners.add(listener);
    }
    public void removeOnGlobalFocusChangeListener(OnGlobalFocusChangeListener victim) {
        checkIsAlive();
        if (mOnGlobalFocusListeners == null) {
            return;
        }
        mOnGlobalFocusListeners.remove(victim);
    }
    public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener) {
        checkIsAlive();
        if (mOnGlobalLayoutListeners == null) {
            mOnGlobalLayoutListeners = new CopyOnWriteArrayList<OnGlobalLayoutListener>();
        }
        mOnGlobalLayoutListeners.add(listener);
    }
    public void removeGlobalOnLayoutListener(OnGlobalLayoutListener victim) {
        checkIsAlive();
        if (mOnGlobalLayoutListeners == null) {
            return;
        }
        mOnGlobalLayoutListeners.remove(victim);
    }
    public void addOnPreDrawListener(OnPreDrawListener listener) {
        checkIsAlive();
        if (mOnPreDrawListeners == null) {
            mOnPreDrawListeners = new CopyOnWriteArrayList<OnPreDrawListener>();
        }
        mOnPreDrawListeners.add(listener);
    }
    public void removeOnPreDrawListener(OnPreDrawListener victim) {
        checkIsAlive();
        if (mOnPreDrawListeners == null) {
            return;
        }
        mOnPreDrawListeners.remove(victim);
    }
    public void addOnScrollChangedListener(OnScrollChangedListener listener) {
        checkIsAlive();
        if (mOnScrollChangedListeners == null) {
            mOnScrollChangedListeners = new CopyOnWriteArrayList<OnScrollChangedListener>();
        }
        mOnScrollChangedListeners.add(listener);
    }
    public void removeOnScrollChangedListener(OnScrollChangedListener victim) {
        checkIsAlive();
        if (mOnScrollChangedListeners == null) {
            return;
        }
        mOnScrollChangedListeners.remove(victim);
    }
    public void addOnTouchModeChangeListener(OnTouchModeChangeListener listener) {
        checkIsAlive();
        if (mOnTouchModeChangeListeners == null) {
            mOnTouchModeChangeListeners = new CopyOnWriteArrayList<OnTouchModeChangeListener>();
        }
        mOnTouchModeChangeListeners.add(listener);
    }
    public void removeOnTouchModeChangeListener(OnTouchModeChangeListener victim) {
        checkIsAlive();
        if (mOnTouchModeChangeListeners == null) {
            return;
        }
        mOnTouchModeChangeListeners.remove(victim);
    }
    public void addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
        checkIsAlive();
        if (mOnComputeInternalInsetsListeners == null) {
            mOnComputeInternalInsetsListeners =
                    new CopyOnWriteArrayList<OnComputeInternalInsetsListener>();
        }
        mOnComputeInternalInsetsListeners.add(listener);
    }
    public void removeOnComputeInternalInsetsListener(OnComputeInternalInsetsListener victim) {
        checkIsAlive();
        if (mOnComputeInternalInsetsListeners == null) {
            return;
        }
        mOnComputeInternalInsetsListeners.remove(victim);
    }
    private void checkIsAlive() {
        if (!mAlive) {
            throw new IllegalStateException("This ViewTreeObserver is not alive, call "
                    + "getViewTreeObserver() again");
        }
    }
    public boolean isAlive() {
        return mAlive;
    }
    private void kill() {
        mAlive = false;
    }
    final void dispatchOnGlobalFocusChange(View oldFocus, View newFocus) {
        final CopyOnWriteArrayList<OnGlobalFocusChangeListener> listeners = mOnGlobalFocusListeners;
        if (listeners != null) {
            for (OnGlobalFocusChangeListener listener : listeners) {
                listener.onGlobalFocusChanged(oldFocus, newFocus);
            }
        }
    }
    public final void dispatchOnGlobalLayout() {
        final CopyOnWriteArrayList<OnGlobalLayoutListener> listeners = mOnGlobalLayoutListeners;
        if (listeners != null) {
            for (OnGlobalLayoutListener listener : listeners) {
                listener.onGlobalLayout();
            }
        }
    }
    public final boolean dispatchOnPreDraw() {
        boolean cancelDraw = false;
        final CopyOnWriteArrayList<OnPreDrawListener> listeners = mOnPreDrawListeners;
        if (listeners != null) {
            for (OnPreDrawListener listener : listeners) {
                cancelDraw |= !listener.onPreDraw();
            }
        }
        return cancelDraw;
    }
    final void dispatchOnTouchModeChanged(boolean inTouchMode) {
        final CopyOnWriteArrayList<OnTouchModeChangeListener> listeners =
                mOnTouchModeChangeListeners;
        if (listeners != null) {
            for (OnTouchModeChangeListener listener : listeners) {
                listener.onTouchModeChanged(inTouchMode);
            }
        }
    }
    final void dispatchOnScrollChanged() {
        final CopyOnWriteArrayList<OnScrollChangedListener> listeners = mOnScrollChangedListeners;
        if (listeners != null) {
            for (OnScrollChangedListener listener : listeners) {
                listener.onScrollChanged();
            }
        }
    }
    final boolean hasComputeInternalInsetsListeners() {
        final CopyOnWriteArrayList<OnComputeInternalInsetsListener> listeners =
                mOnComputeInternalInsetsListeners;
        return (listeners != null && listeners.size() > 0);
    }
    final void dispatchOnComputeInternalInsets(InternalInsetsInfo inoutInfo) {
        final CopyOnWriteArrayList<OnComputeInternalInsetsListener> listeners =
                mOnComputeInternalInsetsListeners;
        if (listeners != null) {
            for (OnComputeInternalInsetsListener listener : listeners) {
                listener.onComputeInternalInsets(inoutInfo);
            }
        }
    }
}
