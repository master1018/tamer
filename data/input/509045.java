public class DrmUtils {
    private static final String TAG = "DrmUtils";
    private static final Uri DRM_TEMP_URI = Uri.parse("content:
    private DrmUtils() {
    }
    public static void cleanupStorage(Context context) {
        SqliteWrapper.delete(context, context.getContentResolver(),
                DRM_TEMP_URI, null, null);
    }
    public static Uri insert(Context context, DrmWrapper drmObj)
            throws IOException {
        ContentResolver cr = context.getContentResolver();
        Uri uri = SqliteWrapper.insert(context, cr, DRM_TEMP_URI,
                                       new ContentValues(0));
        OutputStream os = null;
        try {
            os = cr.openOutputStream(uri);
            byte[] data = drmObj.getDecryptedData();
            if (data != null) {
                os.write(data);
            }
            return uri;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }
}
