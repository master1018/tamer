public class PreferenceCategory extends PreferenceGroup {
    private static final String TAG = "PreferenceCategory";
    public PreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public PreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.preferenceCategoryStyle);
    }
    public PreferenceCategory(Context context) {
        this(context, null);
    }
    @Override
    protected boolean onPrepareAddPreference(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            throw new IllegalArgumentException(
                    "Cannot add a " + TAG + " directly to a " + TAG);
        }
        return super.onPrepareAddPreference(preference);
    }
    @Override
    public boolean isEnabled() {
        return false;
    }
}
