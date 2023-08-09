public class AttachmentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.android.email.attachmentprovider";
    public static final Uri CONTENT_URI = Uri.parse( "content:
    private static final String FORMAT_RAW = "RAW";
    private static final String FORMAT_THUMBNAIL = "THUMBNAIL";
    public static class AttachmentProviderColumns {
        public static final String _ID = "_id";
        public static final String DATA = "_data";
        public static final String DISPLAY_NAME = "_display_name";
        public static final String SIZE = "_size";
    }
    private static final String[] MIME_TYPE_PROJECTION = new String[] {
            AttachmentColumns.MIME_TYPE, AttachmentColumns.FILENAME };
    private static final int MIME_TYPE_COLUMN_MIME_TYPE = 0;
    private static final int MIME_TYPE_COLUMN_FILENAME = 1;
    private static final String[] PROJECTION_QUERY = new String[] { AttachmentColumns.FILENAME,
            AttachmentColumns.SIZE, AttachmentColumns.CONTENT_URI };
    public static Uri getAttachmentUri(long accountId, long id) {
        return CONTENT_URI.buildUpon()
                .appendPath(Long.toString(accountId))
                .appendPath(Long.toString(id))
                .appendPath(FORMAT_RAW)
                .build();
    }
    public static Uri getAttachmentThumbnailUri(long accountId, long id,
            int width, int height) {
        return CONTENT_URI.buildUpon()
                .appendPath(Long.toString(accountId))
                .appendPath(Long.toString(id))
                .appendPath(FORMAT_THUMBNAIL)
                .appendPath(Integer.toString(width))
                .appendPath(Integer.toString(height))
                .build();
    }
    public static File getAttachmentFilename(Context context, long accountId, long attachmentId) {
        return new File(getAttachmentDirectory(context, accountId), Long.toString(attachmentId));
    }
    public static File getAttachmentDirectory(Context context, long accountId) {
        return context.getDatabasePath(accountId + ".db_att");
    }
    @Override
    public boolean onCreate() {
        File[] files = getContext().getCacheDir().listFiles();
        for (File file : files) {
            String filename = file.getName();
            if (filename.endsWith(".tmp") || filename.startsWith("thmb_")) {
                file.delete();
            }
        }
        return true;
    }
    @Override
    public String getType(Uri uri) {
        List<String> segments = uri.getPathSegments();
        String accountId = segments.get(0);
        String id = segments.get(1);
        String format = segments.get(2);
        if (FORMAT_THUMBNAIL.equals(format)) {
            return "image/png";
        } else {
            uri = ContentUris.withAppendedId(Attachment.CONTENT_URI, Long.parseLong(id));
            Cursor c = getContext().getContentResolver().query(uri, MIME_TYPE_PROJECTION,
                    null, null, null);
            try {
                if (c.moveToFirst()) {
                    String mimeType = c.getString(MIME_TYPE_COLUMN_MIME_TYPE);
                    String fileName = c.getString(MIME_TYPE_COLUMN_FILENAME);
                    mimeType = inferMimeType(fileName, mimeType);
                    return mimeType;
                }
            } finally {
                c.close();
            }
            return null;
        }
    }
    public static String inferMimeType(String fileName, String mimeType) {
        if (!TextUtils.isEmpty(mimeType) &&
                !"application/octet-stream".equalsIgnoreCase(mimeType)) {
            return mimeType;
        }
        if (!TextUtils.isEmpty(fileName)) {
            int lastDot = fileName.lastIndexOf('.');
            String extension = null;
            if ((lastDot > 0) && (lastDot < fileName.length() - 1)) {
                extension = fileName.substring(lastDot + 1).toLowerCase();
            }
            if (!TextUtils.isEmpty(extension)) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mimeType == null) {
                    mimeType = "application/" + extension;
                }
                return mimeType;
            }
        }
        return "application/octet-stream";
    }
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        List<String> segments = uri.getPathSegments();
        String accountId = segments.get(0);
        String id = segments.get(1);
        String format = segments.get(2);
        if (FORMAT_THUMBNAIL.equals(format)) {
            int width = Integer.parseInt(segments.get(3));
            int height = Integer.parseInt(segments.get(4));
            String filename = "thmb_" + accountId + "_" + id;
            File dir = getContext().getCacheDir();
            File file = new File(dir, filename);
            if (!file.exists()) {
                Uri attachmentUri = getAttachmentUri(Long.parseLong(accountId), Long.parseLong(id));
                Cursor c = query(attachmentUri,
                        new String[] { AttachmentProviderColumns.DATA }, null, null, null);
                if (c != null) {
                    try {
                        if (c.moveToFirst()) {
                            attachmentUri = Uri.parse(c.getString(0));
                        } else {
                            return null;
                        }
                    } finally {
                        c.close();
                    }
                }
                String type = getContext().getContentResolver().getType(attachmentUri);
                try {
                    InputStream in =
                        getContext().getContentResolver().openInputStream(attachmentUri);
                    Bitmap thumbnail = createThumbnail(type, in);
                    thumbnail = Bitmap.createScaledBitmap(thumbnail, width, height, true);
                    FileOutputStream out = new FileOutputStream(file);
                    thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                    in.close();
                }
                catch (IOException ioe) {
                    return null;
                }
            }
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        }
        else {
            return ParcelFileDescriptor.open(
                    new File(getContext().getDatabasePath(accountId + ".db_att"), id),
                    ParcelFileDescriptor.MODE_READ_ONLY);
        }
    }
    @Override
    public int delete(Uri uri, String arg1, String[] arg2) {
        return 0;
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (projection == null) {
            projection =
                new String[] {
                    AttachmentProviderColumns._ID,
                    AttachmentProviderColumns.DATA,
                    };
        }
        List<String> segments = uri.getPathSegments();
        String accountId = segments.get(0);
        String id = segments.get(1);
        String format = segments.get(2);
        String name = null;
        int size = -1;
        String contentUri = null;
        uri = ContentUris.withAppendedId(Attachment.CONTENT_URI, Long.parseLong(id));
        Cursor c = getContext().getContentResolver().query(uri, PROJECTION_QUERY,
                null, null, null);
        try {
            if (c.moveToFirst()) {
                name = c.getString(0);
                size = c.getInt(1);
                contentUri = c.getString(2);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
        MatrixCursor ret = new MatrixCursor(projection);
        Object[] values = new Object[projection.length];
        for (int i = 0, count = projection.length; i < count; i++) {
            String column = projection[i];
            if (AttachmentProviderColumns._ID.equals(column)) {
                values[i] = id;
            }
            else if (AttachmentProviderColumns.DATA.equals(column)) {
                values[i] = contentUri;
            }
            else if (AttachmentProviderColumns.DISPLAY_NAME.equals(column)) {
                values[i] = name;
            }
            else if (AttachmentProviderColumns.SIZE.equals(column)) {
                values[i] = size;
            }
        }
        ret.addRow(values);
        return ret;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
    private Bitmap createThumbnail(String type, InputStream data) {
        if(MimeUtility.mimeTypeMatches(type, "image
            return null;
        }
        catch (Exception e) {
            return null;
        }
    }
    public static Uri resolveAttachmentIdToContentUri(ContentResolver resolver, Uri attachmentUri) {
        Cursor c = resolver.query(attachmentUri,
                new String[] { AttachmentProvider.AttachmentProviderColumns.DATA },
                null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    final String strUri = c.getString(0);
                    if (strUri != null) {
                        return Uri.parse(strUri);
                    } else {
                        Email.log("AttachmentProvider: attachment with null contentUri");
                    }
                }
            } finally {
                c.close();
            }
        }
        return attachmentUri;
    }
    public static void deleteAllAttachmentFiles(Context context, long accountId, long messageId) {
        Uri uri = ContentUris.withAppendedId(Attachment.MESSAGE_ID_URI, messageId);
        Cursor c = context.getContentResolver().query(uri, Attachment.ID_PROJECTION,
                null, null, null);
        try {
            while (c.moveToNext()) {
                long attachmentId = c.getLong(Attachment.ID_PROJECTION_COLUMN);
                File attachmentFile = getAttachmentFilename(context, accountId, attachmentId);
                attachmentFile.delete();
            }
        } finally {
            c.close();
        }
    }
    public static void deleteAllMailboxAttachmentFiles(Context context, long accountId,
            long mailboxId) {
        Cursor c = context.getContentResolver().query(Message.CONTENT_URI,
                Message.ID_COLUMN_PROJECTION, MessageColumns.MAILBOX_KEY + "=?",
                new String[] { Long.toString(mailboxId) }, null);
        try {
            while (c.moveToNext()) {
                long messageId = c.getLong(Message.ID_PROJECTION_COLUMN);
                deleteAllAttachmentFiles(context, accountId, messageId);
            }
        } finally {
            c.close();
        }
    }
}
