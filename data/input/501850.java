public class AudioAttachmentView extends LinearLayout implements
        SlideViewInterface {
    private static final String TAG = "AudioAttachmentView";
    private final Resources mRes;
    private TextView mNameView;
    private TextView mAlbumView;
    private TextView mArtistView;
    private TextView mErrorMsgView;
    private Uri mAudioUri;
    private MediaPlayer mMediaPlayer;
    private boolean mIsPlaying;
    public AudioAttachmentView(Context context) {
        super(context);
        mRes = context.getResources();
    }
    public AudioAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRes = context.getResources();
    }
    @Override
    protected void onFinishInflate() {
        mNameView = (TextView) findViewById(R.id.audio_name);
        mAlbumView = (TextView) findViewById(R.id.album_name);
        mArtistView = (TextView) findViewById(R.id.artist_name);
        mErrorMsgView = (TextView) findViewById(R.id.audio_error_msg);
    }
    private void onPlaybackError() {
        Log.e(TAG, "Error occurred while playing audio.");
        showErrorMessage(mRes.getString(R.string.cannot_play_audio));
        stopAudio();
    }
    private void cleanupMediaPlayer() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } finally {
                mMediaPlayer = null;
            }
        }
    }
    synchronized public void startAudio() {
        if (!mIsPlaying && (mAudioUri != null)) {
            mMediaPlayer = MediaPlayer.create(mContext, mAudioUri);
            if (mMediaPlayer != null) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        stopAudio();
                    }
                });
                mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        onPlaybackError();
                        return true;
                    }
                });
                mIsPlaying = true;
                mMediaPlayer.start();
            }
        }
    }
    public void startVideo() {
    }
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        synchronized (this) {
            mAudioUri = audio;
        }
        mNameView.setText(name);
        mAlbumView.setText((String) extras.get("album"));
        mArtistView.setText((String) extras.get("artist"));
    }
    public void setImage(String name, Bitmap bitmap) {
    }
    public void setImageRegionFit(String fit) {
    }
    public void setImageVisibility(boolean visible) {
    }
    public void setText(String name, String text) {
    }
    public void setTextVisibility(boolean visible) {
    }
    public void setVideo(String name, Uri video) {
    }
    public void setVideoVisibility(boolean visible) {
    }
    synchronized public void stopAudio() {
        try {
            cleanupMediaPlayer();
        } finally {
            mIsPlaying = false;
        }
    }
    public void stopVideo() {
    }
    public void reset() {
        synchronized (this) {
            if (mIsPlaying) {
                stopAudio();
            }
        }
        mErrorMsgView.setVisibility(GONE);
    }
    public void setVisibility(boolean visible) {
    }
    private void showErrorMessage(String msg) {
        mErrorMsgView.setText(msg);
        mErrorMsgView.setVisibility(VISIBLE);
    }
    public void pauseAudio() {
    }
    public void pauseVideo() {
    }
    public void seekAudio(int seekTo) {
    }
    public void seekVideo(int seekTo) {
    }
}
