public class OpenDownloadReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        ContentResolver cr = context.getContentResolver();
        Uri data = intent.getData();
        Cursor cursor = null;
        try {
            cursor = cr.query(data,
                    new String[] { Downloads.Impl._ID, Downloads.Impl._DATA,
                    Downloads.Impl.COLUMN_MIME_TYPE, Downloads.COLUMN_STATUS },
                    null, null, null);
            if (cursor.moveToFirst()) {
                String filename = cursor.getString(1);
                String mimetype = cursor.getString(2);
                String action = intent.getAction();
                if (Downloads.ACTION_NOTIFICATION_CLICKED.equals(action)) {
                    int status = cursor.getInt(3);
                    if (Downloads.isStatusCompleted(status)
                            && Downloads.isStatusSuccess(status)) {
                        Intent launchIntent = new Intent(Intent.ACTION_VIEW);
                        Uri path = Uri.parse(filename);
                        if (path.getScheme() == null) {
                            path = Uri.fromFile(new File(filename));
                        }
                        launchIntent.setDataAndType(path, mimetype);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(launchIntent);
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(context,
                                    R.string.download_no_application_title,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Intent pageView = new Intent(context,
                                BrowserDownloadPage.class);
                        pageView.setData(data);
                        pageView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(pageView);
                    }
                } else if (Intent.ACTION_DELETE.equals(action)) {
                    if (deleteFile(cr, filename, mimetype)) {
                        cr.delete(data, null, null);
                    }
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    private boolean deleteFile(ContentResolver cr, String filename,
            String mimetype) {
        Uri uri;
        if (mimetype.startsWith("image")) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (mimetype.startsWith("audio")) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else if (mimetype.startsWith("video")) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            uri = null;
        }
        return (uri != null && cr.delete(uri, MediaStore.MediaColumns.DATA
                + " = " + DatabaseUtils.sqlEscapeString(filename), null) > 0)
                || new File(filename).delete();
    }
}
