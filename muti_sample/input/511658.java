public class ContactBadge extends QuickContactBadge {
    private View.OnClickListener mExtraOnClickListener;
    public ContactBadge(Context context) {
        super(context);
    }
    public ContactBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ContactBadge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (mExtraOnClickListener != null) {
            mExtraOnClickListener.onClick(v);
        }
    }
    public void setExtraOnClickListener(View.OnClickListener extraOnClickListener) {
        mExtraOnClickListener = extraOnClickListener;
    }
}
