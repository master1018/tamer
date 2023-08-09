public class BluetoothOppUtility {
    private static final String TAG = "BluetoothOppUtility";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    public static BluetoothOppTransferInfo queryRecord(Context context, Uri uri) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothOppTransferInfo info = new BluetoothOppTransferInfo();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                info.mID = cursor.getInt(cursor.getColumnIndexOrThrow(BluetoothShare._ID));
                info.mStatus = cursor.getInt(cursor.getColumnIndexOrThrow(BluetoothShare.STATUS));
                info.mDirection = cursor.getInt(cursor
                        .getColumnIndexOrThrow(BluetoothShare.DIRECTION));
                info.mTotalBytes = cursor.getInt(cursor
                        .getColumnIndexOrThrow(BluetoothShare.TOTAL_BYTES));
                info.mCurrentBytes = cursor.getInt(cursor
                        .getColumnIndexOrThrow(BluetoothShare.CURRENT_BYTES));
                info.mTimeStamp = cursor.getLong(cursor
                        .getColumnIndexOrThrow(BluetoothShare.TIMESTAMP));
                info.mDestAddr = cursor.getString(cursor
                        .getColumnIndexOrThrow(BluetoothShare.DESTINATION));
                info.mFileName = cursor.getString(cursor
                        .getColumnIndexOrThrow(BluetoothShare._DATA));
                if (info.mFileName == null) {
                    info.mFileName = cursor.getString(cursor
                            .getColumnIndexOrThrow(BluetoothShare.FILENAME_HINT));
                }
                if (info.mFileName == null) {
                    info.mFileName = context.getString(R.string.unknown_file);
                }
                info.mFileUri = cursor.getString(cursor.getColumnIndexOrThrow(BluetoothShare.URI));
                if (info.mFileUri != null) {
                    Uri u = Uri.parse(info.mFileUri);
                    info.mFileType = context.getContentResolver().getType(u);
                } else {
                    Uri u = Uri.parse(info.mFileName);
                    info.mFileType = context.getContentResolver().getType(u);
                }
                if (info.mFileType == null) {
                    info.mFileType = cursor.getString(cursor
                            .getColumnIndexOrThrow(BluetoothShare.MIMETYPE));
                }
                BluetoothDevice remoteDevice = adapter.getRemoteDevice(info.mDestAddr);
                info.mDeviceName =
                        BluetoothOppManager.getInstance(context).getDeviceName(remoteDevice);
                if (V) Log.v(TAG, "Get data from db:" + info.mFileName + info.mFileType
                            + info.mDestAddr);
            }
            cursor.close();
        } else {
            info = null;
            if (V) Log.v(TAG, "BluetoothOppManager Error: not got data from db for uri:" + uri);
        }
        return info;
    }
    public static ArrayList<String> queryTransfersInBatch(Context context, Long timeStamp) {
        ArrayList<String> uris = Lists.newArrayList();
        final String WHERE = BluetoothShare.TIMESTAMP + " == " + timeStamp;
        Cursor metadataCursor = context.getContentResolver().query(BluetoothShare.CONTENT_URI,
                new String[] {
                    BluetoothShare._DATA
                }, WHERE, null, BluetoothShare._ID);
        if (metadataCursor == null) {
            return null;
        }
        for (metadataCursor.moveToFirst(); !metadataCursor.isAfterLast(); metadataCursor
                .moveToNext()) {
            String fileName = metadataCursor.getString(0);
            Uri path = Uri.parse(fileName);
            if (path.getScheme() == null) {
                path = Uri.fromFile(new File(fileName));
            }
            uris.add(path.toString());
            if (V) Log.d(TAG, "Uri in this batch: " + path.toString());
        }
        metadataCursor.close();
        return uris;
    }
    public static void openReceivedFile(Context context, String fileName, String mimetype,
            Long timeStamp, Uri uri) {
        if (fileName == null || mimetype == null) {
            Log.e(TAG, "ERROR: Para fileName ==null, or mimetype == null");
            return;
        }
        File f = new File(fileName);
        if (!f.exists()) {
            Intent in = new Intent(context, BluetoothOppBtErrorActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra("title", context.getString(R.string.not_exist_file));
            in.putExtra("content", context.getString(R.string.not_exist_file_desc));
            context.startActivity(in);
            if (V) Log.d(TAG, "This uri will be deleted: " + uri);
            context.getContentResolver().delete(uri, null, null);
            return;
        }
        Uri path = Uri.parse(fileName);
        if (path.getScheme() == null) {
            path = Uri.fromFile(new File(fileName));
        }
        if (isRecognizedFileType(context, path, mimetype)) {
            Intent activityIntent = new Intent(Intent.ACTION_VIEW);
            activityIntent.setDataAndType(path, mimetype);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                if (V) Log.d(TAG, "ACTION_VIEW intent sent out: " + path + " / " + mimetype);
                context.startActivity(activityIntent);
            } catch (ActivityNotFoundException ex) {
                if (V) Log.d(TAG, "no activity for handling ACTION_VIEW intent:  " + mimetype, ex);
            }
        } else {
            Intent in = new Intent(context, BluetoothOppBtErrorActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra("title", context.getString(R.string.unknown_file));
            in.putExtra("content", context.getString(R.string.unknown_file_desc));
            context.startActivity(in);
        }
    }
    public static boolean isRecognizedFileType(Context context, Uri fileUri, String mimetype) {
        boolean ret = true;
        if (D) Log.d(TAG, "RecognizedFileType() fileUri: " + fileUri + " mimetype: " + mimetype);
        Intent mimetypeIntent = new Intent(Intent.ACTION_VIEW);
        mimetypeIntent.setDataAndType(fileUri, mimetype);
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(mimetypeIntent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            if (D) Log.d(TAG, "NO application to handle MIME type " + mimetype);
            ret = false;
        }
        return ret;
    }
    public static void updateVisibilityToHidden(Context context, Uri uri) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(BluetoothShare.VISIBILITY, BluetoothShare.VISIBILITY_HIDDEN);
        context.getContentResolver().update(uri, updateValues, null, null);
    }
    public static String formatProgressText(long totalBytes, long currentBytes) {
        if (totalBytes <= 0) {
            return "0%";
        }
        long progress = currentBytes * 100 / totalBytes;
        StringBuilder sb = new StringBuilder();
        sb.append(progress);
        sb.append('%');
        return sb.toString();
    }
    public static String getStatusDescription(Context context, int statusCode) {
        String ret;
        if (statusCode == BluetoothShare.STATUS_PENDING) {
            ret = context.getString(R.string.status_pending);
        } else if (statusCode == BluetoothShare.STATUS_RUNNING) {
            ret = context.getString(R.string.status_running);
        } else if (statusCode == BluetoothShare.STATUS_SUCCESS) {
            ret = context.getString(R.string.status_success);
        } else if (statusCode == BluetoothShare.STATUS_NOT_ACCEPTABLE) {
            ret = context.getString(R.string.status_not_accept);
        } else if (statusCode == BluetoothShare.STATUS_FORBIDDEN) {
            ret = context.getString(R.string.status_forbidden);
        } else if (statusCode == BluetoothShare.STATUS_CANCELED) {
            ret = context.getString(R.string.status_canceled);
        } else if (statusCode == BluetoothShare.STATUS_FILE_ERROR) {
            ret = context.getString(R.string.status_file_error);
        } else if (statusCode == BluetoothShare.STATUS_ERROR_NO_SDCARD) {
            ret = context.getString(R.string.status_no_sd_card);
        } else if (statusCode == BluetoothShare.STATUS_CONNECTION_ERROR) {
            ret = context.getString(R.string.status_connection_error);
        } else if (statusCode == BluetoothShare.STATUS_ERROR_SDCARD_FULL) {
            ret = context.getString(R.string.bt_sm_2_1);
        } else if ((statusCode == BluetoothShare.STATUS_BAD_REQUEST)
                || (statusCode == BluetoothShare.STATUS_LENGTH_REQUIRED)
                || (statusCode == BluetoothShare.STATUS_PRECONDITION_FAILED)
                || (statusCode == BluetoothShare.STATUS_UNHANDLED_OBEX_CODE)
                || (statusCode == BluetoothShare.STATUS_OBEX_DATA_ERROR)) {
            ret = context.getString(R.string.status_protocol_error);
        } else {
            ret = context.getString(R.string.status_unknown_error);
        }
        return ret;
    }
    public static void retryTransfer(Context context, BluetoothOppTransferInfo transInfo) {
        ContentValues values = new ContentValues();
        values.put(BluetoothShare.URI, transInfo.mFileUri);
        values.put(BluetoothShare.MIMETYPE, transInfo.mFileType);
        values.put(BluetoothShare.DESTINATION, transInfo.mDestAddr);
        final Uri contentUri = context.getContentResolver().insert(BluetoothShare.CONTENT_URI,
                values);
        if (V) Log.v(TAG, "Insert contentUri: " + contentUri + "  to device: " +
                transInfo.mDeviceName);
    }
}
