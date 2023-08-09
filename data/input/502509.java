public class SeekBarPreference extends DialogPreference {
    private static final String TAG = "SeekBarPreference";
    private Drawable mMyIcon;
    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(com.android.internal.R.layout.seekbar_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        mMyIcon = getDialogIcon();
        setDialogIcon(null);
    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
        if (mMyIcon != null) {
            iconView.setImageDrawable(mMyIcon);
        } else {
            iconView.setVisibility(View.GONE);
        }
    }
    protected static SeekBar getSeekBar(View dialogView) {
        return (SeekBar) dialogView.findViewById(com.android.internal.R.id.seekbar);
    }
}
