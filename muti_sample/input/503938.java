public class FileContentProvider extends ContentProvider {
    public static final String BASE_URI = 
            "content:
    public static final int BASE_URI_LEN = BASE_URI.length();
    @Override
    public String getType(Uri uri) {
        String mimetype = uri.getQuery();
        return mimetype == null ? "" : mimetype;
    }
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new FileNotFoundException("Bad mode for " + uri + ": " + mode);
        }
        String filename = uri.toString().substring(BASE_URI_LEN);
        return ParcelFileDescriptor.open(new File(filename),
            ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean onCreate() {
        return true;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
