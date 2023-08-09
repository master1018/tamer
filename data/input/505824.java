public class SlideListItemView extends LinearLayout implements SlideViewInterface {
    private static final String TAG = "SlideListItemView";
    private TextView mTextPreview;
    private ImageView mImagePreview;
    private TextView mAttachmentName;
    private ImageView mAttachmentIcon;
    public SlideListItemView(Context context) {
        super(context);
    }
    public SlideListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        mTextPreview = (TextView) findViewById(R.id.text_preview);
        mTextPreview.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        mImagePreview = (ImageView) findViewById(R.id.image_preview);
        mAttachmentName = (TextView) findViewById(R.id.attachment_name);
        mAttachmentIcon = (ImageView) findViewById(R.id.attachment_icon);
    }
    public void startAudio() {
    }
    public void startVideo() {
    }
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        if (name != null) {
            mAttachmentName.setText(name);
            mAttachmentIcon.setImageResource(R.drawable.ic_mms_music);
        } else {
            mAttachmentName.setText("");
            mAttachmentIcon.setImageDrawable(null);
        }
    }
    public void setImage(String name, Bitmap bitmap) {
        if (null == bitmap) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_missing_thumbnail_picture);
        }
        mImagePreview.setImageBitmap(bitmap);
    }
    public void setImageRegionFit(String fit) {
    }
    public void setImageVisibility(boolean visible) {
    }
    public void setText(String name, String text) {
        mTextPreview.setText(text);
        mTextPreview.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }
    public void setTextVisibility(boolean visible) {
    }
    public void setVideo(String name, Uri video) {
        if (name != null) {
            mAttachmentName.setText(name);
            mAttachmentIcon.setImageResource(R.drawable.movie);
        } else {
            mAttachmentName.setText("");
            mAttachmentIcon.setImageDrawable(null);
        }
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(mContext, video);
            mImagePreview.setImageBitmap(mp.getFrameAt(1000));
        } catch (IOException e) {
            Log.e(TAG, "Unexpected IOException.", e);
        } finally {
            mp.release();
        }
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
