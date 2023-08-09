public class CachingIconLoader implements IconLoader {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.CachingIconLoader";
    private final IconLoader mWrapped;
    private final WeakHashMap<String, Drawable.ConstantState> mIconCache;
    public CachingIconLoader(IconLoader wrapped) {
        mWrapped = wrapped;
        mIconCache = new WeakHashMap<String, Drawable.ConstantState>();
    }
    public Drawable getIcon(String drawableId) {
        if (DBG) Log.d(TAG, "getIcon(" + drawableId + ")");
        if (TextUtils.isEmpty(drawableId) || "0".equals(drawableId)) {
            return null;
        }
        Drawable drawable = checkIconCache(drawableId);
        if (drawable != null) {
            return drawable;
        }
        drawable = mWrapped.getIcon(drawableId);
        storeInIconCache(drawableId, drawable);
        return drawable;
    }
    public Uri getIconUri(String drawableId) {
        return mWrapped.getIconUri(drawableId);
    }
    private Drawable checkIconCache(String drawableId) {
        Drawable.ConstantState cached = mIconCache.get(drawableId);
        if (cached == null) {
            return null;
        }
        if (DBG) Log.d(TAG, "Found icon in cache: " + drawableId);
        return cached.newDrawable();
    }
    private void storeInIconCache(String resourceUri, Drawable drawable) {
        if (drawable != null) {
            mIconCache.put(resourceUri, drawable.getConstantState());
        }
    }
}
