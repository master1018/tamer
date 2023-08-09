public class CorpusView extends RelativeLayout {
    private ImageView mIcon;
    private TextView mLabel;
    public CorpusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CorpusView(Context context) {
        super(context);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIcon = (ImageView) findViewById(R.id.source_icon);
        mLabel = (TextView) findViewById(R.id.source_label);
    }
    public void setLabel(CharSequence label) {
        mLabel.setText(label);
    }
    public void setIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
    }
}
