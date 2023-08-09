public final class ViewStub extends View {
    private int mLayoutResource = 0;
    private int mInflatedId;
    private WeakReference<View> mInflatedViewRef;
    private OnInflateListener mInflateListener;
    public ViewStub(Context context) {
        initialize(context);
    }
    public ViewStub(Context context, int layoutResource) {
        mLayoutResource = layoutResource;
        initialize(context);
    }
    public ViewStub(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    @SuppressWarnings({"UnusedDeclaration"})
    public ViewStub(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ViewStub,
                defStyle, 0);
        mInflatedId = a.getResourceId(R.styleable.ViewStub_inflatedId, NO_ID);
        mLayoutResource = a.getResourceId(R.styleable.ViewStub_layout, 0);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View, defStyle, 0);
        mID = a.getResourceId(R.styleable.View_id, NO_ID);
        a.recycle();
        initialize(context);
    }
    private void initialize(Context context) {
        mContext = context;
        setVisibility(GONE);
        setWillNotDraw(true);
    }
    public int getInflatedId() {
        return mInflatedId;
    }
    public void setInflatedId(int inflatedId) {
        mInflatedId = inflatedId;
    }
    public int getLayoutResource() {
        return mLayoutResource;
    }
    public void setLayoutResource(int layoutResource) {
        mLayoutResource = layoutResource;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }
    @Override
    public void draw(Canvas canvas) {
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
    }
    @Override
    public void setVisibility(int visibility) {
        if (mInflatedViewRef != null) {
            View view = mInflatedViewRef.get();
            if (view != null) {
                view.setVisibility(visibility);
            } else {
                throw new IllegalStateException("setVisibility called on un-referenced view");
            }
        } else {
            super.setVisibility(visibility);
            if (visibility == VISIBLE || visibility == INVISIBLE) {
                inflate();
            }
        }
    }
    public View inflate() {
        final ViewParent viewParent = getParent();
        if (viewParent != null && viewParent instanceof ViewGroup) {
            if (mLayoutResource != 0) {
                final ViewGroup parent = (ViewGroup) viewParent;
                final LayoutInflater factory = LayoutInflater.from(mContext);
                final View view = factory.inflate(mLayoutResource, parent,
                        false);
                if (mInflatedId != NO_ID) {
                    view.setId(mInflatedId);
                }
                final int index = parent.indexOfChild(this);
                parent.removeViewInLayout(this);
                final ViewGroup.LayoutParams layoutParams = getLayoutParams();
                if (layoutParams != null) {
                    parent.addView(view, index, layoutParams);
                } else {
                    parent.addView(view, index);
                }
                mInflatedViewRef = new WeakReference<View>(view);
                if (mInflateListener != null) {
                    mInflateListener.onInflate(this, view);
                }
                return view;
            } else {
                throw new IllegalArgumentException("ViewStub must have a valid layoutResource");
            }
        } else {
            throw new IllegalStateException("ViewStub must have a non-null ViewGroup viewParent");
        }
    }
    public void setOnInflateListener(OnInflateListener inflateListener) {
        mInflateListener = inflateListener;
    }
    public static interface OnInflateListener {
        void onInflate(ViewStub stub, View inflated);
    }
}
