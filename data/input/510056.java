public abstract class CameraPreference {
    private final String mTitle;
    private SharedPreferences mSharedPreferences;
    private final Context mContext;
    public CameraPreference(Context context, AttributeSet attrs) {
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CameraPreference, 0, 0);
        mTitle = a.getString(R.styleable.CameraPreference_title);
        a.recycle();
    }
    public String getTitle() {
        return mTitle;
    }
    public SharedPreferences getSharedPreferences() {
        if (mSharedPreferences == null) {
            mSharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        return mSharedPreferences;
    }
    public abstract void reloadValue();
}
