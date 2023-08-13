public abstract class MediaModel extends Model implements EventListener {
    protected static final String TAG = "Mms/media";
    private final static String MUSIC_SERVICE_ACTION = "com.android.music.musicservicecommand";
    protected Context mContext;
    protected int mBegin;
    protected int mDuration;
    protected String mTag;
    protected String mSrc;
    protected String mContentType;
    private Uri mUri;
    private byte[] mData;
    protected short mFill;
    protected int mSize;
    protected int mSeekTo;
    protected DrmWrapper mDrmObjectWrapper;
    protected boolean mMediaResizeable;
    private final ArrayList<MediaAction> mMediaActions;
    public static enum MediaAction {
        NO_ACTIVE_ACTION,
        START,
        STOP,
        PAUSE,
        SEEK,
    }
    public MediaModel(Context context, String tag, String contentType,
            String src, Uri uri) throws MmsException {
        mContext = context;
        mTag = tag;
        mContentType = contentType;
        mSrc = src;
        mUri = uri;
        initMediaSize();
        mMediaActions = new ArrayList<MediaAction>();
    }
    public MediaModel(Context context, String tag, String contentType,
            String src, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null.");
        }
        mContext = context;
        mTag = tag;
        mContentType = contentType;
        mSrc = src;
        mData = data;
        mSize = data.length;
        mMediaActions = new ArrayList<MediaAction>();
    }
    public MediaModel(Context context, String tag, String contentType,
            String src, DrmWrapper wrapper) throws IOException {
        mContext = context;
        mTag = tag;
        mContentType = contentType;
        mSrc = src;
        mDrmObjectWrapper = wrapper;
        mUri = DrmUtils.insert(context, wrapper);
        mSize = wrapper.getOriginalData().length;
        mMediaActions = new ArrayList<MediaAction>();
    }
    public int getBegin() {
        return mBegin;
    }
    public void setBegin(int begin) {
        mBegin = begin;
        notifyModelChanged(true);
    }
    public int getDuration() {
        return mDuration;
    }
    public void setDuration(int duration) {
        if (isPlayable() && (duration < 0)) {
            try {
                initMediaDuration();
            } catch (MmsException e) {
                Log.e(TAG, e.getMessage(), e);
                return;
            }
        } else {
            mDuration = duration;
        }
        notifyModelChanged(true);
    }
    public String getTag() {
        return mTag;
    }
    public String getContentType() {
        return mContentType;
    }
    public Uri getUri() {
        return mUri;
    }
    public Uri getUriWithDrmCheck() throws DrmException {
        if (mUri != null) {
            if (isDrmProtected() && !mDrmObjectWrapper.consumeRights()) {
                throw new DrmException("Insufficient DRM rights.");
            }
        }
        return mUri;
    }
    public byte[] getData() throws DrmException {
        if (mData != null) {
            if (isDrmProtected() && !mDrmObjectWrapper.consumeRights()) {
                throw new DrmException(
                        mContext.getString(R.string.insufficient_drm_rights));
            }
            byte[] data = new byte[mData.length];
            System.arraycopy(mData, 0, data, 0, mData.length);
            return data;
        }
        return null;
    }
    void setUri(Uri uri) {
        mUri = uri;
    }
    public String getSrc() {
        return mSrc;
    }
    public short getFill() {
        return mFill;
    }
    public void setFill(short fill) {
        mFill = fill;
        notifyModelChanged(true);
    }
    public boolean getMediaResizable() {
        return mMediaResizeable;
    }
    public int getMediaSize() {
        return mSize;
    }
    public boolean isText() {
        return mTag.equals(SmilHelper.ELEMENT_TAG_TEXT);
    }
    public boolean isImage() {
        return mTag.equals(SmilHelper.ELEMENT_TAG_IMAGE);
    }
    public boolean isVideo() {
        return mTag.equals(SmilHelper.ELEMENT_TAG_VIDEO);
    }
    public boolean isAudio() {
        return mTag.equals(SmilHelper.ELEMENT_TAG_AUDIO);
    }
    public boolean isDrmProtected() {
        return mDrmObjectWrapper != null;
    }
    public boolean isAllowedToForward() {
        return mDrmObjectWrapper.isAllowedToForward();
    }
    protected void initMediaDuration() throws MmsException {
        if (mUri == null) {
            throw new IllegalArgumentException("Uri may not be null.");
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setMode(MediaMetadataRetriever.MODE_GET_METADATA_ONLY);
        int duration = 0;
        try {
            retriever.setDataSource(mContext, mUri);
            String dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (dur != null) {
                duration = Integer.parseInt(dur);
            }
            mDuration = duration;
        } catch (Exception ex) {
            Log.e(TAG, "MediaMetadataRetriever failed to get duration for " + mUri.getPath(), ex);
            throw new MmsException(ex);
        } finally {
            retriever.release();
        }
    }
    private void initMediaSize() throws MmsException {
        ContentResolver cr = mContext.getContentResolver();
        InputStream input = null;
        try {
            input = cr.openInputStream(mUri);
            if (input instanceof FileInputStream) {
                FileInputStream f = (FileInputStream) input;
                mSize = (int) f.getChannel().size();
            } else {
                while (-1 != input.read()) {
                    mSize++;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            if (e instanceof FileNotFoundException) {
                throw new MmsException(e.getMessage());
            }
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException caught while closing stream", e);
                }
            }
        }
    }
    public static boolean isMmsUri(Uri uri) {
        return uri.getAuthority().startsWith("mms");
    }
    public int getSeekTo() {
        return mSeekTo;
    }
    public void appendAction(MediaAction action) {
        mMediaActions.add(action);
    }
    public MediaAction getCurrentAction() {
        if (0 == mMediaActions.size()) {
            return MediaAction.NO_ACTIVE_ACTION;
        }
        return mMediaActions.remove(0);
    }
    protected boolean isPlayable() {
        return false;
    }
    public DrmWrapper getDrmObject() {
        return mDrmObjectWrapper;
    }
    protected void pauseMusicPlayer() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "pauseMusicPlayer");
        }
        Intent i = new Intent(MUSIC_SERVICE_ACTION);
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
    }
    protected void resizeMedia(int byteLimit, long messageId) throws MmsException {
    }
}
