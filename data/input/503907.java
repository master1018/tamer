public class BluetoothOppReceiveFileInfo {
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    private static String sDesiredStoragePath = null;
    public String mFileName;
    public long mLength;
    public FileOutputStream mOutputStream;
    public int mStatus;
    public String mData;
    public BluetoothOppReceiveFileInfo(String data, long length, int status) {
        mData = data;
        mStatus = status;
        mLength = length;
    }
    public BluetoothOppReceiveFileInfo(String filename, long length, FileOutputStream outputStream,
            int status) {
        mFileName = filename;
        mOutputStream = outputStream;
        mStatus = status;
        mLength = length;
    }
    public BluetoothOppReceiveFileInfo(int status) {
        this(null, 0, null, status);
    }
    public static BluetoothOppReceiveFileInfo generateFileInfo(Context context, int id) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + id);
        String filename = null, hint = null;
        long length = 0;
        Cursor metadataCursor = contentResolver.query(contentUri, new String[] {
                BluetoothShare.FILENAME_HINT, BluetoothShare.TOTAL_BYTES, BluetoothShare.MIMETYPE
        }, null, null, null);
        if (metadataCursor != null) {
            try {
                if (metadataCursor.moveToFirst()) {
                    hint = metadataCursor.getString(0);
                    length = metadataCursor.getInt(1);
                }
            } finally {
                metadataCursor.close();
            }
        }
        File base = null;
        StatFs stat = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String root = Environment.getExternalStorageDirectory().getPath();
            base = new File(root + Constants.DEFAULT_STORE_SUBDIR);
            if (!base.isDirectory() && !base.mkdir()) {
                if (D) Log.d(Constants.TAG, "Receive File aborted - can't create base directory "
                            + base.getPath());
                return new BluetoothOppReceiveFileInfo(BluetoothShare.STATUS_FILE_ERROR);
            }
            stat = new StatFs(base.getPath());
        } else {
            if (D) Log.d(Constants.TAG, "Receive File aborted - no external storage");
            return new BluetoothOppReceiveFileInfo(BluetoothShare.STATUS_ERROR_NO_SDCARD);
        }
        if (stat.getBlockSize() * ((long)stat.getAvailableBlocks() - 4) < length) {
            if (D) Log.d(Constants.TAG, "Receive File aborted - not enough free space");
            return new BluetoothOppReceiveFileInfo(BluetoothShare.STATUS_ERROR_SDCARD_FULL);
        }
        filename = choosefilename(hint);
        if (filename == null) {
            return new BluetoothOppReceiveFileInfo(BluetoothShare.STATUS_FILE_ERROR);
        }
        String extension = null;
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex < 0) {
            return new BluetoothOppReceiveFileInfo(BluetoothShare.STATUS_FILE_ERROR);
        } else {
            extension = filename.substring(dotIndex);
            filename = filename.substring(0, dotIndex);
        }
        filename = base.getPath() + File.separator + filename;
        String fullfilename = chooseUniquefilename(filename, extension);
        if (!safeCanonicalPath(fullfilename)) {
            return new BluetoothOppReceiveFileInfo(BluetoothShare.STATUS_FILE_ERROR);
        }
        if (V) Log.v(Constants.TAG, "Generated received filename " + fullfilename);
        if (fullfilename != null) {
            try {
                new FileOutputStream(fullfilename).close();
                int index = fullfilename.lastIndexOf('/') + 1;
                if (index > 0) {
                    String displayName = fullfilename.substring(index);
                    if (V) Log.v(Constants.TAG, "New display name " + displayName);
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(BluetoothShare.FILENAME_HINT, displayName);
                    context.getContentResolver().update(contentUri, updateValues, null, null);
                }
                return new BluetoothOppReceiveFileInfo(fullfilename, length, new FileOutputStream(
                        fullfilename), 0);
            } catch (IOException e) {
                if (D) Log.e(Constants.TAG, "Error when creating file " + fullfilename);
                return new BluetoothOppReceiveFileInfo(BluetoothShare.STATUS_FILE_ERROR);
            }
        } else {
            return new BluetoothOppReceiveFileInfo(BluetoothShare.STATUS_FILE_ERROR);
        }
    }
    private static boolean safeCanonicalPath(String uniqueFileName) {
        try {
            File receiveFile = new File(uniqueFileName);
            if (sDesiredStoragePath == null) {
                sDesiredStoragePath = Environment.getExternalStorageDirectory().getPath() +
                    Constants.DEFAULT_STORE_SUBDIR;
            }
            String canonicalPath = receiveFile.getCanonicalPath();
            if (!canonicalPath.startsWith(sDesiredStoragePath)) {
                return false;
            }
	    	return true;
        } catch (IOException ioe) {
            return false;
        }
    }
    private static String chooseUniquefilename(String filename, String extension) {
        String fullfilename = filename + extension;
        if (!new File(fullfilename).exists()) {
            return fullfilename;
        }
        filename = filename + Constants.filename_SEQUENCE_SEPARATOR;
        Random rnd = new Random(SystemClock.uptimeMillis());
        int sequence = 1;
        for (int magnitude = 1; magnitude < 1000000000; magnitude *= 10) {
            for (int iteration = 0; iteration < 9; ++iteration) {
                fullfilename = filename + sequence + extension;
                if (!new File(fullfilename).exists()) {
                    return fullfilename;
                }
                if (V) Log.v(Constants.TAG, "file with sequence number " + sequence + " exists");
                sequence += rnd.nextInt(magnitude) + 1;
            }
        }
        return null;
    }
    private static String choosefilename(String hint) {
        String filename = null;
        if (filename == null && !(hint == null) && !hint.endsWith("/") && !hint.endsWith("\\")) {
            hint = hint.replace('\\', '/');
            if (V) Log.v(Constants.TAG, "getting filename from hint");
            int index = hint.lastIndexOf('/') + 1;
            if (index > 0) {
                filename = hint.substring(index);
            } else {
                filename = hint;
            }
        }
        return filename;
    }
}
