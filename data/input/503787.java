public class VideoAttachmentView extends LinearLayout implements
        SlideViewInterface {
    private static final String TAG = "VideoAttachmentView";
    private ImageView mThumbnailView;
    public VideoAttachmentView(Context context) {
        super(context);
    }
    public VideoAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        mThumbnailView = (ImageView) findViewById(R.id.video_thumbnail);
    }
    public void startAudio() {
    }
    public void startVideo() {
    }
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
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
        Bitmap bitmap = createVideoThumbnail(mContext, video);
        if (null == bitmap) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_missing_thumbnail_video);
        }
        mThumbnailView.setImageBitmap(bitmap);
    }
    public static Bitmap createVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setMode(MediaMetadataRetriever.MODE_CAPTURE_FRAME_ONLY);
            retriever.setDataSource(context, uri);
            bitmap = retriever.captureFrame();
        } catch (RuntimeException ex) {
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return bitmap;
    }
    public void setVideoVisibility(boolean visible) {
    }
    public void stopAudio() {
    }
    public void stopVideo() {
    }
    public void reset() {
    }
    public void setVisibility(boolean visible) {
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
