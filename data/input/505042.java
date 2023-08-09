public class IconListPreference extends ListPreference {
    private int mIconIds[];
    private int mLargeIconIds[];
    public IconListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.IconListPreference, 0, 0);
        Resources res = context.getResources();
        mIconIds = getIconIds(res, a.getResourceId(
                R.styleable.IconListPreference_icons, 0));
        mLargeIconIds = getIconIds(res, a.getResourceId(
                R.styleable.IconListPreference_largeIcons, 0));
        a.recycle();
    }
    public int[] getLargeIconIds() {
        return mLargeIconIds;
    }
    public int[] getIconIds() {
        return mIconIds;
    }
    private int[] getIconIds(Resources res, int iconsRes) {
        if (iconsRes == 0) return null;
        TypedArray array = res.obtainTypedArray(iconsRes);
        int n = array.length();
        int ids[] = new int[n];
        for (int i = 0; i < n; ++i) {
            ids[i] = array.getResourceId(i, 0);
        }
        array.recycle();
        return ids;
    }
    @Override
    public void filterUnsupported(List<String> supported) {
        CharSequence entryValues[] = getEntryValues();
        IntArray iconIds = new IntArray();
        IntArray largeIconIds = new IntArray();
        for (int i = 0, len = entryValues.length; i < len; i++) {
            if (supported.indexOf(entryValues[i].toString()) >= 0) {
                iconIds.add(mIconIds[i]);
                largeIconIds.add(mLargeIconIds[i]);
            }
        }
        int size = iconIds.size();
        mIconIds = iconIds.toArray(new int[size]);
        mLargeIconIds = iconIds.toArray(new int[size]);
        super.filterUnsupported(supported);
    }
}
