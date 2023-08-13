public class IconIndicator extends ImageView {
    private Drawable[] mIcons;
    private CharSequence[] mModes;
    public IconIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.IconIndicator, defStyle, 0);
        Drawable icons[] = loadIcons(context.getResources(),
                a.getResourceId(R.styleable.IconIndicator_icons, 0));
        CharSequence modes[] =
                a.getTextArray(R.styleable.IconIndicator_modes);
        a.recycle();
        setModesAndIcons(modes, icons);
        setImageDrawable(mIcons.length > 0 ? mIcons[0] : null);
    }
    public IconIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    private Drawable[] loadIcons(Resources resources, int iconsId) {
        TypedArray array = resources.obtainTypedArray(iconsId);
        int n = array.length();
        Drawable drawable[] = new Drawable[n];
        for (int i = 0; i < n; ++i) {
            int id = array.getResourceId(i, 0);
            drawable[i] = id == 0 ? null : resources.getDrawable(id);
        }
        array.recycle();
        return drawable;
    }
    private void setModesAndIcons(CharSequence[] modes, Drawable icons[]) {
        if (modes.length != icons.length || icons.length == 0) {
            throw new IllegalArgumentException();
        }
        mIcons = icons;
        mModes = modes;
    }
    public void setMode(String mode) {
        for (int i = 0, n = mModes.length; i < n; ++i) {
            if (mModes[i].equals(mode)) {
                setImageDrawable(mIcons[i]);
                return;
            }
        }
        throw new IllegalArgumentException("unknown mode: " + mode);
    }
}
