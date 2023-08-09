public class GridLayoutAnimationController extends LayoutAnimationController {
    public static final int DIRECTION_LEFT_TO_RIGHT = 0x0;
    public static final int DIRECTION_RIGHT_TO_LEFT = 0x1;
    public static final int DIRECTION_TOP_TO_BOTTOM = 0x0;
    public static final int DIRECTION_BOTTOM_TO_TOP = 0x2;
    public static final int DIRECTION_HORIZONTAL_MASK = 0x1;
    public static final int DIRECTION_VERTICAL_MASK   = 0x2;
    public static final int PRIORITY_NONE   = 0;
    public static final int PRIORITY_COLUMN = 1;
    public static final int PRIORITY_ROW    = 2;
    private float mColumnDelay;
    private float mRowDelay;
    private int mDirection;
    private int mDirectionPriority;
    public GridLayoutAnimationController(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.GridLayoutAnimation);
        Animation.Description d = Animation.Description.parseValue(
                a.peekValue(com.android.internal.R.styleable.GridLayoutAnimation_columnDelay));
        mColumnDelay = d.value;
        d = Animation.Description.parseValue(
                a.peekValue(com.android.internal.R.styleable.GridLayoutAnimation_rowDelay));
        mRowDelay = d.value;
        mDirection = a.getInt(com.android.internal.R.styleable.GridLayoutAnimation_direction,
                DIRECTION_LEFT_TO_RIGHT | DIRECTION_TOP_TO_BOTTOM);
        mDirectionPriority = a.getInt(com.android.internal.R.styleable.GridLayoutAnimation_directionPriority,
                PRIORITY_NONE);
        a.recycle();
    }
    public GridLayoutAnimationController(Animation animation) {
        this(animation, 0.5f, 0.5f);
    }
    public GridLayoutAnimationController(Animation animation, float columnDelay, float rowDelay) {
        super(animation);
        mColumnDelay = columnDelay;
        mRowDelay = rowDelay;
    }
    public float getColumnDelay() {
        return mColumnDelay;
    }
    public void setColumnDelay(float columnDelay) {
        mColumnDelay = columnDelay;
    }
    public float getRowDelay() {
        return mRowDelay;
    }
    public void setRowDelay(float rowDelay) {
        mRowDelay = rowDelay;
    }
    public int getDirection() {
        return mDirection;
    }
    public void setDirection(int direction) {
        mDirection = direction;
    }
    public int getDirectionPriority() {
        return mDirectionPriority;
    }
    public void setDirectionPriority(int directionPriority) {
        mDirectionPriority = directionPriority;
    }
    @Override
    public boolean willOverlap() {
        return mColumnDelay < 1.0f || mRowDelay < 1.0f;
    }
    @Override
    protected long getDelayForView(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        AnimationParameters params = (AnimationParameters) lp.layoutAnimationParameters;
        if (params == null) {
            return 0;
        }
        final int column = getTransformedColumnIndex(params);
        final int row = getTransformedRowIndex(params);
        final int rowsCount = params.rowsCount;
        final int columnsCount = params.columnsCount;
        final long duration = mAnimation.getDuration();
        final float columnDelay = mColumnDelay * duration;
        final float rowDelay = mRowDelay * duration;
        float totalDelay;
        long viewDelay;
        if (mInterpolator == null) {
            mInterpolator = new LinearInterpolator();
        }
        switch (mDirectionPriority) {
            case PRIORITY_COLUMN:
                viewDelay = (long) (row * rowDelay + column * rowsCount * rowDelay);
                totalDelay = rowsCount * rowDelay + columnsCount * rowsCount * rowDelay;
                break;
            case PRIORITY_ROW:
                viewDelay = (long) (column * columnDelay + row * columnsCount * columnDelay);
                totalDelay = columnsCount * columnDelay + rowsCount * columnsCount * columnDelay;
                break;
            case PRIORITY_NONE:
            default:
                viewDelay = (long) (column * columnDelay + row * rowDelay);
                totalDelay = columnsCount * columnDelay + rowsCount * rowDelay;
                break;
        }
        float normalizedDelay = viewDelay / totalDelay;
        normalizedDelay = mInterpolator.getInterpolation(normalizedDelay);
        return (long) (normalizedDelay * totalDelay);
    }
    private int getTransformedColumnIndex(AnimationParameters params) {
        int index;
        switch (getOrder()) {
            case ORDER_REVERSE:
                index = params.columnsCount - 1 - params.column;
                break;
            case ORDER_RANDOM:
                if (mRandomizer == null) {
                    mRandomizer = new Random();
                }
                index = (int) (params.columnsCount * mRandomizer.nextFloat());
                break;
            case ORDER_NORMAL:
            default:
                index = params.column;
                break;
        }
        int direction = mDirection & DIRECTION_HORIZONTAL_MASK;
        if (direction == DIRECTION_RIGHT_TO_LEFT) {
            index = params.columnsCount - 1 - index;
        }
        return index;
    }
    private int getTransformedRowIndex(AnimationParameters params) {
        int index;
        switch (getOrder()) {
            case ORDER_REVERSE:
                index = params.rowsCount - 1 - params.row;
                break;
            case ORDER_RANDOM:
                if (mRandomizer == null) {
                    mRandomizer = new Random();
                }
                index = (int) (params.rowsCount * mRandomizer.nextFloat());
                break;
            case ORDER_NORMAL:
            default:
                index = params.row;
                break;
        }
        int direction = mDirection & DIRECTION_VERTICAL_MASK;
        if (direction == DIRECTION_BOTTOM_TO_TOP) {
            index = params.rowsCount - 1 - index;
        }
        return index;
    }
    public static class AnimationParameters extends
            LayoutAnimationController.AnimationParameters {
        public int column;
        public int row;
        public int columnsCount;
        public int rowsCount;
    }
}
