public class ImageAttachmentView extends LinearLayout implements SlideViewInterface {
    private ImageView mImageView;
    public ImageAttachmentView(Context context) {
        super(context);
    }
    public ImageAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        mImageView = (ImageView) findViewById(R.id.image_content);
    }
    public void startAudio() {
    }
    public void startVideo() {
    }
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
    }
    public void setImage(String name, Bitmap bitmap) {
        if (null == bitmap) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_missing_thumbnail_picture);
        }
        mImageView.setImageBitmap(bitmap);
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
    public void stopAudio() {
    }
    public void stopVideo() {
    }
    public void reset() {
        mImageView.setImageDrawable(null);
    }
    public void setVisibility(boolean visible) {
        setVisibility(visible ? View.VISIBLE : View.GONE);
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
