public class TwoLineListItem extends RelativeLayout {
    private TextView mText1;
    private TextView mText2;
    public TwoLineListItem(Context context) {
        this(context, null, 0);
    }
    public TwoLineListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0); 
    }
    public TwoLineListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.TwoLineListItem, defStyle, 0);
        a.recycle();
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mText1 = (TextView) findViewById(com.android.internal.R.id.text1);
        mText2 = (TextView) findViewById(com.android.internal.R.id.text2);
    }
    public TextView getText1() {
        return mText1;
    }
    public TextView getText2() {
        return mText2;
    }
}
