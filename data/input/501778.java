public class ProgressCategory extends PreferenceCategory {
    private boolean mProgress = false;
    public ProgressCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_progress_category);
    }
    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        View textView = view.findViewById(R.id.scanning_text);
        View progressBar = view.findViewById(R.id.scanning_progress);
        int visibility = mProgress ? View.VISIBLE : View.INVISIBLE;
        textView.setVisibility(visibility);
        progressBar.setVisibility(visibility);
    }
    public void setProgress(boolean progressOn) {
        mProgress = progressOn;
        notifyChanged();
    }
}
