public class ContextThemeWrapper extends ContextWrapper {
    private Context mBase;
    private int mThemeResource;
    private Resources.Theme mTheme;
    private LayoutInflater mInflater;
    public ContextThemeWrapper() {
        super(null);
    }
    public ContextThemeWrapper(Context base, int themeres) {
        super(base);
        mBase = base;
        mThemeResource = themeres;
    }
    @Override protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        mBase = newBase;
    }
    @Override public void setTheme(int resid) {
        mThemeResource = resid;
        initializeTheme();
    }
    @Override public Resources.Theme getTheme() {
        if (mTheme != null) {
            return mTheme;
        }
        if (mThemeResource == 0) {
            mThemeResource = com.android.internal.R.style.Theme;
        }
        initializeTheme();
        return mTheme;
    }
    @Override public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(mBase).cloneInContext(this);
            }
            return mInflater;
        }
        return mBase.getSystemService(name);
    }
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        theme.applyStyle(resid, true);
    }
    private void initializeTheme() {
        final boolean first = mTheme == null;
        if (first) {
            mTheme = getResources().newTheme();
            Resources.Theme theme = mBase.getTheme();
            if (theme != null) {
                mTheme.setTo(theme);
            }
        }
        onApplyThemeResource(mTheme, mThemeResource, first);
    }
}
