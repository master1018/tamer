public final class DrmStore
{
    private static final String TAG = "DrmStore";
    public static final String AUTHORITY = "drm";
    private static final String ACCESS_DRM_PERMISSION = "android.permission.ACCESS_DRM";
    public interface Columns extends BaseColumns {
        public static final String DATA = "_data";
        public static final String SIZE = "_size";
        public static final String TITLE = "title";
        public static final String MIME_TYPE = "mime_type";
    }
    public interface Images extends Columns {
        public static final Uri CONTENT_URI = Uri.parse("content:
    }
    public interface Audio extends Columns {
        public static final Uri CONTENT_URI = Uri.parse("content:
    }
    public static final Intent addDrmFile(ContentResolver cr, File file, String title) {
        FileInputStream fis = null;
        Intent result = null;
        try {
            fis = new FileInputStream(file);
            if (title == null) {
                title = file.getName();
                int lastDot = title.lastIndexOf('.');
                if (lastDot > 0) {
                    title = title.substring(0, lastDot);
                }
            }
            result = addDrmFile(cr, fis, title);
        } catch (Exception e) {
            Log.e(TAG, "pushing file failed", e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException in DrmStore.addDrmFile()", e);
            }
        }
        return result;
    }
    public static final Intent addDrmFile(ContentResolver cr, FileInputStream fis, String title) {
        OutputStream os = null;
        Intent result = null;
        try {
            DrmRawContent content = new DrmRawContent(fis, (int) fis.available(),
                    DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING);
            String mimeType = content.getContentType();
            DrmRightsManager manager = manager = DrmRightsManager.getInstance();
            DrmRights rights = manager.queryRights(content);
            InputStream stream = content.getContentInputStream(rights);
            long size = stream.available();
            Uri contentUri = null;
            if (mimeType.startsWith("audio/")) {
                contentUri = DrmStore.Audio.CONTENT_URI;
            } else if (mimeType.startsWith("image/")) {
                contentUri = DrmStore.Images.CONTENT_URI;
            } else {
                Log.w(TAG, "unsupported mime type " + mimeType);
            }
            if (contentUri != null) {
                ContentValues values = new ContentValues(3);
                values.put(DrmStore.Columns.TITLE, title);
                values.put(DrmStore.Columns.SIZE, size);
                values.put(DrmStore.Columns.MIME_TYPE, mimeType);
                Uri uri = cr.insert(contentUri, values);
                if (uri != null) {
                    os = cr.openOutputStream(uri);
                    byte[] buffer = new byte[1000];
                    int count;
                    while ((count = stream.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
                    result = new Intent();
                    result.setDataAndType(uri, mimeType);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "pushing file failed", e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (os != null)
                    os.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException in DrmStore.addDrmFile()", e);
            }
        }
        return result;
    }
    public static void enforceAccessDrmPermission(Context context) {
        if (context.checkCallingOrSelfPermission(ACCESS_DRM_PERMISSION) !=
                PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires DRM permission");
        }
    }
}
