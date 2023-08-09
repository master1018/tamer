public class Ringtone {
    private static String TAG = "Ringtone";
    private static final String[] MEDIA_COLUMNS = new String[] {
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE
    };
    private static final String[] DRM_COLUMNS = new String[] {
        DrmStore.Audio._ID,
        DrmStore.Audio.DATA,
        DrmStore.Audio.TITLE
    };
    private MediaPlayer mAudio;
    private Uri mUri;
    private String mTitle;
    private FileDescriptor mFileDescriptor;
    private AssetFileDescriptor mAssetFileDescriptor;
    private int mStreamType = AudioManager.STREAM_RING;
    private AudioManager mAudioManager;
    private Context mContext;
    Ringtone(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }
    public void setStreamType(int streamType) {
        mStreamType = streamType;
        if (mAudio != null) {
            try {
                openMediaPlayer();
            } catch (IOException e) {
                Log.w(TAG, "Couldn't set the stream type", e);
            }
        }
    }
    public int getStreamType() {
        return mStreamType;
    }
    public String getTitle(Context context) {
        if (mTitle != null) return mTitle;
        return mTitle = getTitle(context, mUri, true);
    }
    private static String getTitle(Context context, Uri uri, boolean followSettingsUri) {
        Cursor cursor = null;
        ContentResolver res = context.getContentResolver();
        String title = null;
        if (uri != null) {
            String authority = uri.getAuthority();
            if (Settings.AUTHORITY.equals(authority)) {
                if (followSettingsUri) {
                    Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context,
                            RingtoneManager.getDefaultType(uri));
                    String actualTitle = getTitle(context, actualUri, false);
                    title = context
                            .getString(com.android.internal.R.string.ringtone_default_with_actual,
                                    actualTitle);
                }
            } else {
                if (DrmStore.AUTHORITY.equals(authority)) {
                    cursor = res.query(uri, DRM_COLUMNS, null, null, null);
                } else if (MediaStore.AUTHORITY.equals(authority)) {
                    cursor = res.query(uri, MEDIA_COLUMNS, null, null, null);
                }
                try {
                    if (cursor != null && cursor.getCount() == 1) {
                        cursor.moveToFirst();
                        return cursor.getString(2);
                    } else {
                        title = uri.getLastPathSegment();
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        if (title == null) {
            title = context.getString(com.android.internal.R.string.ringtone_unknown);
            if (title == null) {
                title = "";
            }
        }
        return title;
    }
    private void openMediaPlayer() throws IOException {
        mAudio = new MediaPlayer();
        if (mUri != null) {
            mAudio.setDataSource(mContext, mUri);
        } else if (mFileDescriptor != null) {
            mAudio.setDataSource(mFileDescriptor);
        } else if (mAssetFileDescriptor != null) {
            if (mAssetFileDescriptor.getDeclaredLength() < 0) {
                mAudio.setDataSource(mAssetFileDescriptor.getFileDescriptor());
            } else {
                mAudio.setDataSource(mAssetFileDescriptor.getFileDescriptor(),
                        mAssetFileDescriptor.getStartOffset(),
                        mAssetFileDescriptor.getDeclaredLength());
            }
        } else {
            throw new IOException("No data source set.");
        }
        mAudio.setAudioStreamType(mStreamType);
        mAudio.prepare();
    }
    void open(FileDescriptor fd) throws IOException {
        mFileDescriptor = fd;
        openMediaPlayer();
    }
    void open(AssetFileDescriptor fd) throws IOException {
        mAssetFileDescriptor = fd;
        openMediaPlayer();
    }
    void open(Uri uri) throws IOException {
        mUri = uri;
        openMediaPlayer();
    }
    public void play() {
        if (mAudio == null) {
            try {
                openMediaPlayer();
            } catch (Exception ex) {
                Log.e(TAG, "play() caught ", ex);
                mAudio = null;
            }
        }
        if (mAudio != null) {
            if (mAudioManager.getStreamVolume(mStreamType) != 0) {
                mAudio.start();
            }
        }
    }
    public void stop() {
        if (mAudio != null) {
            mAudio.reset();
            mAudio.release();
            mAudio = null;
        }
    }
    public boolean isPlaying() {
        return mAudio != null && mAudio.isPlaying();
    }
    void setTitle(String title) {
        mTitle = title;
    }
}
