public class BluetoothOppSendFileInfo {
    private static final String TAG = "BluetoothOppSendFileInfo";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    public final String mFileName;
    public final FileInputStream mInputStream;
    public final String mData;
    public final int mStatus;
    public final String mMimetype;
    public final long mLength;
    public final String mDestAddr;
    public BluetoothOppSendFileInfo(String fileName, String type, long length,
            FileInputStream inputStream, int status, String dest) {
        mFileName = fileName;
        mMimetype = type;
        mLength = length;
        mInputStream = inputStream;
        mStatus = status;
        mDestAddr = dest;
        mData = null;
    }
    public BluetoothOppSendFileInfo(String data, String type, long length, int status,
            String dest) {
        mFileName = null;
        mInputStream = null;
        mData = data;
        mMimetype = type;
        mLength = length;
        mStatus = status;
        mDestAddr = dest;
    }
    public static BluetoothOppSendFileInfo generateFileInfo(Context context, String uri,
            String type, String dest) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri u = Uri.parse(uri);
        String scheme = u.getScheme();
        String fileName = null;
        String contentType = null;
        long length = 0;
        if (scheme.equals("content")) {
            contentType = contentResolver.getType(u);
            Cursor metadataCursor = contentResolver.query(u, new String[] {
                    OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            }, null, null, null);
            if (metadataCursor != null) {
                try {
                    if (metadataCursor.moveToFirst()) {
                        fileName = metadataCursor.getString(0);
                        length = metadataCursor.getInt(1);
                        if (D) Log.d(TAG, "fileName = " + fileName + " length = " + length);
                    }
                } finally {
                    metadataCursor.close();
                }
            }
        } else if (scheme.equals("file")) {
            fileName = u.getLastPathSegment();
            contentType = type;
            File f = new File(u.getPath());
            length = f.length();
        } else {
            return new BluetoothOppSendFileInfo(null, null, 0, null,
                    BluetoothShare.STATUS_FILE_ERROR, dest);
        }
        FileInputStream is;
        try {
            is = (FileInputStream)contentResolver.openInputStream(u);
        } catch (FileNotFoundException e) {
            return new BluetoothOppSendFileInfo(null, null, 0, null,
                    BluetoothShare.STATUS_FILE_ERROR, dest);
        }
        if (length == 0) {
            try {
                length = is.available();
                if (V) Log.v(TAG, "file length is " + length);
            } catch (IOException e) {
                Log.e(TAG, "Read stream exception: ", e);
                return new BluetoothOppSendFileInfo(null, null, 0, null,
                        BluetoothShare.STATUS_FILE_ERROR, dest);
            }
        }
        return new BluetoothOppSendFileInfo(fileName, contentType, length, is, 0, dest);
    }
}
