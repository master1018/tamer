public class PackageIconLoader implements IconLoader {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.PackageIconLoader";
    private final Context mContext;
    private final String mPackageName;
    private Context mPackageContext;
    public PackageIconLoader(Context context, String packageName) {
        mContext = context;
        mPackageName = packageName;
    }
    private boolean ensurePackageContext() {
        if (mPackageContext == null) {
            try {
                mPackageContext = mContext.createPackageContext(mPackageName,
                        Context.CONTEXT_RESTRICTED);
            } catch (PackageManager.NameNotFoundException ex) {
                Log.e(TAG, "Application not found " + mPackageName);
                return false;
            }
        }
        return true;
    }
    public Drawable getIcon(String drawableId) {
        if (DBG) Log.d(TAG, "getIcon(" + drawableId + ")");
        if (TextUtils.isEmpty(drawableId) || "0".equals(drawableId)) {
            return null;
        }
        if (!ensurePackageContext()) return null;
        try {
            int resourceId = Integer.parseInt(drawableId);
            return mPackageContext.getResources().getDrawable(resourceId);
        } catch (NumberFormatException nfe) {
            Uri uri = Uri.parse(drawableId);
            return getDrawable(uri);
        } catch (Resources.NotFoundException nfe) {
            Log.w(TAG, "Icon resource not found: " + drawableId);
            return null;
        }
    }
    public Uri getIconUri(String drawableId) {
        if (TextUtils.isEmpty(drawableId) || "0".equals(drawableId)) {
            return null;
        }
        if (!ensurePackageContext()) return null;
        try {
            int resourceId = Integer.parseInt(drawableId);
            return Util.getResourceUri(mPackageContext, resourceId);
        } catch (NumberFormatException nfe) {
            return Uri.parse(drawableId);
        }
    }
    private Drawable getDrawable(Uri uri) {
        try {
            String scheme = uri.getScheme();
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                OpenResourceIdResult r = getResourceId(uri);
                try {
                    return r.r.getDrawable(r.id);
                } catch (Resources.NotFoundException ex) {
                    throw new FileNotFoundException("Resource does not exist: " + uri);
                }
            } else {
                InputStream stream = mPackageContext.getContentResolver().openInputStream(uri);
                if (stream == null) {
                    throw new FileNotFoundException("Failed to open " + uri);
                }
                try {
                    return Drawable.createFromStream(stream, null);
                } finally {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "Error closing icon stream for " + uri, ex);
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            Log.w(TAG, "Icon not found: " + uri + ", " + fnfe.getMessage());
            return null;
        }
    }
    private class OpenResourceIdResult {
        public Resources r;
        public int id;
    }
    private OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        String authority = uri.getAuthority();
        Resources r;
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        } else {
            try {
                r = mPackageContext.getPackageManager().getResourcesForApplication(authority);
            } catch (NameNotFoundException ex) {
                throw new FileNotFoundException("Failed to get resources: " + ex);
            }
        }
        List<String> path = uri.getPathSegments();
        if (path == null) {
            throw new FileNotFoundException("No path: " + uri);
        }
        int len = path.size();
        int id;
        if (len == 1) {
            try {
                id = Integer.parseInt(path.get(0));
            } catch (NumberFormatException e) {
                throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
            }
        } else if (len == 2) {
            id = r.getIdentifier(path.get(1), path.get(0), authority);
        } else {
            throw new FileNotFoundException("More than two path segments: " + uri);
        }
        if (id == 0) {
            throw new FileNotFoundException("No resource found for: " + uri);
        }
        OpenResourceIdResult res = new OpenResourceIdResult();
        res.r = r;
        res.id = id;
        return res;
    }
}
