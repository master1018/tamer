public class ApplicationsStackLayout extends ViewGroup implements View.OnClickListener {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private View mButton;
    private LayoutInflater mInflater;
    private int mFavoritesEnd;
    private int mFavoritesStart;
    private List<ApplicationInfo> mFavorites;
    private List<ApplicationInfo> mRecents;
    private int mOrientation = VERTICAL;
    private int mMarginLeft;
    private int mMarginTop;
    private int mMarginRight;
    private int mMarginBottom;
    private Rect mDrawRect = new Rect();
    private Drawable mBackground;
    private int mIconSize;
    public ApplicationsStackLayout(Context context) {
        super(context);
        initLayout();
    }
    public ApplicationsStackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.ApplicationsStackLayout);
        mOrientation = a.getInt(R.styleable.ApplicationsStackLayout_stackOrientation, VERTICAL);
        mMarginLeft = a.getDimensionPixelSize(R.styleable.ApplicationsStackLayout_marginLeft, 0);
        mMarginTop = a.getDimensionPixelSize(R.styleable.ApplicationsStackLayout_marginTop, 0);
        mMarginRight = a.getDimensionPixelSize(R.styleable.ApplicationsStackLayout_marginRight, 0);
        mMarginBottom = a.getDimensionPixelSize(R.styleable.ApplicationsStackLayout_marginBottom, 0);
        a.recycle();
        mIconSize = 42; 
        initLayout();
    }
    private void initLayout() {
        mInflater = LayoutInflater.from(getContext());
        mButton = mInflater.inflate(R.layout.all_applications_button, this, false);
        addView(mButton);
        mBackground = getBackground();
        setBackgroundDrawable(null);
        setWillNotDraw(false);
    }
    public int getOrientation() {
        return mOrientation;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        final Drawable background = mBackground;
        final int right = getWidth();
        final int bottom = getHeight();
        if (mOrientation == VERTICAL) {
            mDrawRect.set(0, 0, right, mFavoritesStart);
        } else {
            mDrawRect.set(0, 0, mFavoritesStart, bottom);
        }
        background.setBounds(mDrawRect);
        background.draw(canvas);
        if (mFavoritesStart > -1) {
            if (mOrientation == VERTICAL) {
                mDrawRect.set(0, mFavoritesStart, right, mFavoritesEnd);
            } else {
                mDrawRect.set(mFavoritesStart, 0, mFavoritesEnd, bottom);
            }
            background.setBounds(mDrawRect);
            background.draw(canvas);
        }
        super.onDraw(canvas);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ApplicationsStackLayout can only be used with "
                    + "measure spec mode=EXACTLY");
        }
        setMeasuredDimension(widthSize, heightSize);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        removeAllApplications();
        LayoutParams layoutParams = mButton.getLayoutParams();
        final int widthSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
        final int heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
        mButton.measure(widthSpec, heightSpec);
        if (mOrientation == VERTICAL) {
            layoutVertical();
        } else {
            layoutHorizontal();
        }
    }
    private void layoutVertical() {
        int childLeft = 0;
        int childTop = getHeight();
        int childWidth = mButton.getMeasuredWidth();
        int childHeight = mButton.getMeasuredHeight();
        childTop -= childHeight + mMarginBottom;
        mButton.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        childTop -= mMarginTop;
        mFavoritesEnd = childTop - mMarginBottom;
        int oldChildTop = childTop;
        childTop = stackApplications(mFavorites, childLeft, childTop);
        if (childTop != oldChildTop) {
            mFavoritesStart = childTop + mMarginTop;
        } else {
            mFavoritesStart = -1;
        }
        stackApplications(mRecents, childLeft, childTop);
    }
    private void layoutHorizontal() {
        int childLeft = getWidth();
        int childTop = 0;
        int childWidth = mButton.getMeasuredWidth();
        int childHeight = mButton.getMeasuredHeight();
        childLeft -= childWidth;
        mButton.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        childLeft -= mMarginLeft;
        mFavoritesEnd = childLeft - mMarginRight;
        int oldChildLeft = childLeft;
        childLeft = stackApplications(mFavorites, childLeft, childTop);
        if (childLeft != oldChildLeft) {
            mFavoritesStart = childLeft + mMarginLeft;
        } else {
            mFavoritesStart = -1;
        }
        stackApplications(mRecents, childLeft, childTop);
    }
    private int stackApplications(List<ApplicationInfo> applications, int childLeft, int childTop) {
        LayoutParams layoutParams;
        int widthSpec;
        int heightSpec;
        int childWidth;
        int childHeight;
        final boolean isVertical = mOrientation == VERTICAL;
        final int count = applications.size();
        for (int i = count - 1; i >= 0; i--) {
            final ApplicationInfo info = applications.get(i);
            final View view = createApplicationIcon(mInflater, this, info);
            layoutParams = view.getLayoutParams();
            widthSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
            heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);
            childWidth = view.getMeasuredWidth();
            childHeight = view.getMeasuredHeight();
            if (isVertical) {
                childTop -= childHeight + mMarginBottom;
                if (childTop < 0) {
                    childTop += childHeight + mMarginBottom;
                    break;
                }
            } else {
                childLeft -= childWidth + mMarginRight;
                if (childLeft < 0) {
                    childLeft += childWidth + mMarginRight;
                    break;
                }
            }
            addViewInLayout(view, -1, layoutParams);
            view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            if (isVertical) {
                childTop -= mMarginTop;
            } else {
                childLeft -= mMarginLeft;
            }
        }
        return isVertical ? childTop : childLeft;
    }
    private void removeAllApplications() {
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View view = getChildAt(i);
            if (view != mButton) {
                removeViewAt(i);
            }
        }
    }
    private View createApplicationIcon(LayoutInflater inflater,
            ViewGroup group, ApplicationInfo info) {
        TextView textView = (TextView) inflater.inflate(R.layout.favorite, group, false);
        info.icon.setBounds(0, 0, mIconSize, mIconSize);
        textView.setCompoundDrawables(null, info.icon, null, null);
        textView.setText(info.title);
        textView.setTag(info.intent);
        textView.setOnClickListener(this);
        return textView;
    }
    public void setFavorites(List<ApplicationInfo> applications) {
        mFavorites = applications;
        requestLayout();
    }
    public void setRecents(List<ApplicationInfo> applications) {
        mRecents = applications;
        requestLayout();
    }
    public void onClick(View v) {
        getContext().startActivity((Intent) v.getTag());
    }
}
