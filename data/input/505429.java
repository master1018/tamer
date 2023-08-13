public class MockIconLoader implements IconLoader {
    private final Context mContext;
    public MockIconLoader(Context context) {
        mContext = context;
    }
    public Drawable getIcon(String drawableId) {
        if (TextUtils.isEmpty(drawableId) || "0".equals(drawableId)) {
            return null;
        }
        return mContext.getResources().getDrawable(android.R.drawable.star_on);
    }
    public Uri getIconUri(String drawableId) {
        if (TextUtils.isEmpty(drawableId) || "0".equals(drawableId)) {
            return null;
        }
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(mContext.getPackageName())
                .appendEncodedPath(String.valueOf(android.R.drawable.star_on))
                .build();
    }
}