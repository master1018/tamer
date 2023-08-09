public class BasicSlideEditorView extends LinearLayout implements
        SlideViewInterface {
    private static final String TAG = "BasicSlideEditorView";
    private ImageView mImageView;
    private View mAudioView;
    private TextView mAudioNameView;
    private EditText mEditText;
    private boolean mOnTextChangedListenerEnabled = true;
    private OnTextChangedListener mOnTextChangedListener;
    public BasicSlideEditorView(Context context) {
        super(context);
    }
    public BasicSlideEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void onFinishInflate() {
        mImageView = (ImageView) findViewById(R.id.image);
        mAudioView = findViewById(R.id.audio);
        mAudioNameView = (TextView) findViewById(R.id.audio_name);
        mEditText = (EditText) findViewById(R.id.text_message);
        mEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                if (mOnTextChangedListenerEnabled && (mOnTextChangedListener != null)) {
                    mOnTextChangedListener.onTextChanged(s.toString());
                }
            }
            public void afterTextChanged(Editable s) {
            }
        });
    }
    public void startAudio() {
    }
    public void startVideo() {
    }
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        mAudioView.setVisibility(View.VISIBLE);
        mAudioNameView.setText(name);
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
        mOnTextChangedListenerEnabled = false;
        if ((text != null) && !text.equals(mEditText.getText().toString())) {
            mEditText.setText(text);
            mEditText.setSelection(text.length());
        }
        mOnTextChangedListenerEnabled = true;
    }
    public void setTextVisibility(boolean visible) {
    }
    public void setVideo(String name, Uri video) {
        Bitmap bitmap = VideoAttachmentView.createVideoThumbnail(mContext, video);
        if (null == bitmap) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_missing_thumbnail_video);
        }
        mImageView.setImageBitmap(bitmap);
    }
    public void setVideoVisibility(boolean visible) {
    }
    public void stopAudio() {
    }
    public void stopVideo() {
    }
    public void reset() {
        mImageView.setImageDrawable(null);
        mAudioView.setVisibility(View.GONE);
        mOnTextChangedListenerEnabled = false;
        mEditText.setText("");
        mOnTextChangedListenerEnabled = true;
    }
    public void setVisibility(boolean visible) {
    }
    public void setOnTextChangedListener(OnTextChangedListener l) {
        mOnTextChangedListener = l;
    }
    public interface OnTextChangedListener {
        void onTextChanged(String s);
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
