public class SlideView extends AbsoluteLayout implements
        AdaptableSlideViewInterface {
    private static final String TAG = "SlideView";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private static final int AUDIO_INFO_HEIGHT = 82;
    private View mAudioInfoView;
    private ImageView mImageView;
    private VideoView mVideoView;
    private ScrollView mScrollText;
    private TextView mTextView;
    private OnSizeChangedListener mSizeChangedListener;
    private MediaPlayer mAudioPlayer;
    private boolean mIsPrepared;
    private boolean mStartWhenPrepared;
    private int     mSeekWhenPrepared;
    private boolean mStopWhenPrepared;
    private ScrollView mScrollViewPort;
    private LinearLayout mViewPort;
    private boolean mConformanceMode;
    private MediaController mMediaController;
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mIsPrepared = true;
            if (mSeekWhenPrepared > 0) {
                mAudioPlayer.seekTo(mSeekWhenPrepared);
                mSeekWhenPrepared = 0;
            }
            if (mStartWhenPrepared) {
                mAudioPlayer.start();
                mStartWhenPrepared = false;
                displayAudioInfo();
            }
            if (mStopWhenPrepared) {
                mAudioPlayer.stop();
                mAudioPlayer.release();
                mAudioPlayer = null;
                mStopWhenPrepared = false;
                hideAudioInfo();
            }
        }
    };
    public SlideView(Context context) {
        super(context);
    }
    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setImage(String name, Bitmap bitmap) {
        if (mImageView == null) {
            mImageView = new ImageView(mContext);
            mImageView.setPadding(0, 5, 0, 5);
            addView(mImageView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0));
            if (DEBUG) {
                mImageView.setBackgroundColor(0xFFFF0000);
            }
        }
        if (null == bitmap) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_missing_thumbnail_picture);
        }
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageBitmap(bitmap);
    }
    public void setImageRegion(int left, int top, int width, int height) {
        if (mImageView != null && !mConformanceMode) {
            mImageView.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }
    public void setImageRegionFit(String fit) {
    }
    public void setVideo(String name, Uri video) {
        if (mVideoView == null) {
            mVideoView = new VideoView(mContext);
            addView(mVideoView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0));
            if (DEBUG) {
                mVideoView.setBackgroundColor(0xFFFF0000);
            }
        }
        if (LOCAL_LOGV) {
            Log.v(TAG, "Changing video source to " + video);
        }
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setVideoURI(video);
    }
    public void setMediaController(MediaController mediaController) {
        mMediaController = mediaController;
    }
    private void initAudioInfoView(String name) {
        if (null == mAudioInfoView) {
            LayoutInflater factory = LayoutInflater.from(getContext());
            mAudioInfoView = factory.inflate(R.layout.playing_audio_info, null);
            int height = mAudioInfoView.getHeight();
            TextView audioName = (TextView) mAudioInfoView.findViewById(R.id.name);
            audioName.setText(name);
            if (mConformanceMode) {
                mViewPort.addView(mAudioInfoView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        AUDIO_INFO_HEIGHT));
            } else {
                addView(mAudioInfoView, new LayoutParams(
                        LayoutParams.MATCH_PARENT, AUDIO_INFO_HEIGHT,
                        0, getHeight() - AUDIO_INFO_HEIGHT));
                if (DEBUG) {
                    mAudioInfoView.setBackgroundColor(0xFFFF0000);
                }
            }
        }
        mAudioInfoView.setVisibility(View.GONE);
    }
    private void displayAudioInfo() {
        if (null != mAudioInfoView) {
            mAudioInfoView.setVisibility(View.VISIBLE);
        }
    }
    private void hideAudioInfo() {
        if (null != mAudioInfoView) {
            mAudioInfoView.setVisibility(View.GONE);
        }
    }
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        if (audio == null) {
            throw new IllegalArgumentException("Audio URI may not be null.");
        }
        if (LOCAL_LOGV) {
            Log.v(TAG, "Changing audio source to " + audio);
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.reset();
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
        mIsPrepared = false;
        try {
            mAudioPlayer = new MediaPlayer();
            mAudioPlayer.setOnPreparedListener(mPreparedListener);
            mAudioPlayer.setDataSource(mContext, audio);
            mAudioPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Unexpected IOException.", e);
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
        initAudioInfoView(name);
    }
    public void setText(String name, String text) {
        if (!mConformanceMode) {
            if (null == mScrollText) {
                mScrollText = new ScrollView(mContext);
                mScrollText.setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);
                addView(mScrollText, new LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0));
                if (DEBUG) {
                    mScrollText.setBackgroundColor(0xFF00FF00);
                }
            }
            if (null == mTextView) {
                mTextView = new TextView(mContext);
                mTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                mScrollText.addView(mTextView);
            }
            mScrollText.requestFocus();
        }
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setText(text);
    }
    public void setTextRegion(int left, int top, int width, int height) {
        if (mScrollText != null && !mConformanceMode) {
            mScrollText.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }
    public void setVideoRegion(int left, int top, int width, int height) {
        if (mVideoView != null && !mConformanceMode) {
            mVideoView.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }
    public void setImageVisibility(boolean visible) {
        if (mImageView != null) {
            if (mConformanceMode) {
                mImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
            } else {
                mImageView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }
    public void setTextVisibility(boolean visible) {
        if (mScrollText != null) {
            if (mConformanceMode) {
                mTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
            } else {
                mScrollText.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }
    public void setVideoVisibility(boolean visible) {
        if (mVideoView != null) {
            if (mConformanceMode) {
                mVideoView.setVisibility(visible ? View.VISIBLE : View.GONE);
            } else {
                mVideoView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }
    public void startAudio() {
        if ((mAudioPlayer != null) && mIsPrepared) {
            mAudioPlayer.start();
            mStartWhenPrepared = false;
            displayAudioInfo();
        } else {
            mStartWhenPrepared = true;
        }
    }
    public void stopAudio() {
        if ((mAudioPlayer != null) && mIsPrepared) {
            mAudioPlayer.stop();
            mAudioPlayer.release();
            mAudioPlayer = null;
            hideAudioInfo();
        } else {
            mStopWhenPrepared = true;
        }
    }
    public void pauseAudio() {
        if ((mAudioPlayer != null) && mIsPrepared) {
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.pause();
            }
        }
        mStartWhenPrepared = false;
    }
    public void seekAudio(int seekTo) {
        if ((mAudioPlayer != null) && mIsPrepared) {
            mAudioPlayer.seekTo(seekTo);
        } else {
            mSeekWhenPrepared = seekTo;
        }
    }
    public void startVideo() {
        if (mVideoView != null) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Starting video playback.");
            }
            mVideoView.start();
        }
    }
    public void stopVideo() {
        if ((mVideoView != null)) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Stopping video playback.");
            }
            mVideoView.stopPlayback();
        }
    }
    public void pauseVideo() {
        if (mVideoView != null) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Pausing video playback.");
            }
            mVideoView.pause();
        }
    }
    public void seekVideo(int seekTo) {
        if (mVideoView != null) {
            if (seekTo > 0) {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "Seeking video playback to " + seekTo);
                }
                mVideoView.seekTo(seekTo);
            }
        }
    }
    public void reset() {
        if (null != mScrollText) {
            mScrollText.setVisibility(View.GONE);
        }
        if (null != mImageView) {
            mImageView.setVisibility(View.GONE);
        }
        if (null != mAudioPlayer) {
            stopAudio();
        }
        if (null != mVideoView) {
            stopVideo();
            mVideoView.setVisibility(View.GONE);
        }
        if (null != mTextView) {
            mTextView.setVisibility(View.GONE);
        }
        if (mScrollViewPort != null) {
            mScrollViewPort.scrollTo(0, 0);
            mScrollViewPort.setLayoutParams(
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
        }
    }
    public void setVisibility(boolean visible) {
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mSizeChangedListener != null) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "new size=" + w + "x" + h);
            }
            mSizeChangedListener.onSizeChanged(w, h - AUDIO_INFO_HEIGHT);
        }
    }
    public void setOnSizeChangedListener(OnSizeChangedListener l) {
        mSizeChangedListener = l;
    }
    private class Position {
        public Position(int left, int top) {
            mTop = top;
            mLeft = left;
        }
        public int mTop;
        public int mLeft;
    }
    public void enableMMSConformanceMode(int textLeft, int textTop,
            int imageLeft, int imageTop) {
        mConformanceMode = true;
        if (mScrollViewPort == null) {
            mScrollViewPort = new ScrollView(mContext) {
                private int mBottomY;
                @Override
                protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                    super.onLayout(changed, left, top, right, bottom);
                    if (getChildCount() > 0) {
                        int childHeight = getChildAt(0).getHeight();
                        int height = getHeight();
                        mBottomY = height < childHeight ? childHeight - height : 0;
                    }
                }
                @Override
                protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                    if (t == 0 || t >= mBottomY){
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    }
                }
            };
            mScrollViewPort.setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);
            mViewPort = new LinearLayout(mContext);
            mViewPort.setOrientation(LinearLayout.VERTICAL);
            mViewPort.setGravity(Gravity.CENTER);
            mViewPort.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mMediaController != null) {
                        mMediaController.show();
                    }
                }
            });
            mScrollViewPort.addView(mViewPort, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            addView(mScrollViewPort);
        }
        TreeMap<Position, View> viewsByPosition = new TreeMap<Position, View>(new Comparator<Position>() {
            public int compare(Position p1, Position p2) {
                int l1 = p1.mLeft;
                int t1 = p1.mTop;
                int l2 = p2.mLeft;
                int t2 = p2.mTop;
                int res = t1 - t2;
                if (res == 0) {
                    res = l1 - l2;
                }
                if (res == 0) {
                    return -1;
                }
                return res;
            }
        });
        if (textLeft >=0 && textTop >=0) {
            mTextView = new TextView(mContext);
            mTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mTextView.setTextSize(18);
            mTextView.setPadding(5, 5, 5, 5);
            viewsByPosition.put(new Position(textLeft, textTop), mTextView);
        }
        if (imageLeft >=0 && imageTop >=0) {
            mImageView = new ImageView(mContext);
            mImageView.setPadding(0, 5, 0, 5);
            viewsByPosition.put(new Position(imageLeft, imageTop), mImageView);
            mVideoView = new VideoView(mContext);
            viewsByPosition.put(new Position(imageLeft + 1, imageTop), mVideoView);
        }
        for (View view : viewsByPosition.values()) {
            if (view instanceof VideoView) {
                mViewPort.addView(view, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutManager.getInstance().getLayoutParameters().getHeight()));
            } else {
                mViewPort.addView(view, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
            }
            view.setVisibility(View.GONE);
        }
    }
}
