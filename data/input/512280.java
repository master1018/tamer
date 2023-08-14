public class CropImage extends MonitoredActivity {
    private static final String TAG = "CropImage";
	public static final int CROP_MSG = 10;	
    public static final int CROP_MSG_INTERNAL = 100;    
    private App mApp = null;     
    private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG; 
    private Uri mSaveUri = null;
    private int mAspectX, mAspectY; 
    private boolean mDoFaceDetection = true;
    private boolean mCircleCrop = false;
    private final Handler mHandler = new Handler();
    private int mOutputX, mOutputY;
    private boolean mScale;
    private boolean mScaleUp = true;
    boolean mWaitingToPick; 
    boolean mSaving; 
    private CropImageView mImageView;
    private ContentResolver mContentResolver;
    private Bitmap mBitmap;
    private MediaItem mItem;
    private final BitmapManager.ThreadSet mDecodingThreads = new BitmapManager.ThreadSet();
    HighlightView mCrop;
    static private final HashMap<Context, MediaScannerConnection> mConnectionMap = new HashMap<Context, MediaScannerConnection>();
    static public void launchCropperOrFinish(final Context context, final MediaItem item) {
    	final Bundle myExtras = ((Activity) context).getIntent().getExtras();
    	String cropValue = myExtras != null ? myExtras.getString("crop") : null;
    	final String contentUri = item.mContentUri;
    	if (contentUri == null)
    		return;
    	if (cropValue != null) {
    		Bundle newExtras = new Bundle();
    		if (cropValue.equals("circle")) {
    			newExtras.putString("circleCrop", "true");
    		}
    		Intent cropIntent = new Intent();
    		cropIntent.setData(Uri.parse(contentUri));
    		cropIntent.setClass(context, CropImage.class);
    		cropIntent.putExtras(newExtras);
    		cropIntent.putExtras(myExtras);
    		((Activity) context).startActivityForResult(cropIntent, CropImage.CROP_MSG);
    	} else {
    		if (contentUri.startsWith("http:
    			final ProgressDialog dialog = ProgressDialog.show(context, context.getResources().getString(Res.string.initializing),
    					context.getResources().getString(Res.string.running_face_detection), true, false);
    			if (contentUri != null) {
    				MediaScannerConnection.MediaScannerConnectionClient client = new MediaScannerConnection.MediaScannerConnectionClient() {
    					public void onMediaScannerConnected() {
    						MediaScannerConnection connection = mConnectionMap.get(context);    						
    						if (connection != null) {
    							try {
    								final String path = UriTexture.writeHttpDataInDirectory(context, contentUri,
    										LocalDataSource.DOWNLOAD_BUCKET_NAME);
    								if (path != null) {
    									connection.scanFile(path, item.mMimeType);
    								} else {
    									shutdown("");
    								}
    							} catch (Exception e) {
    								shutdown("");
    							}
    						}
    					}
    					public void onScanCompleted(String path, Uri uri) {
    						shutdown(uri.toString());
    					}
    					public void shutdown(String uri) {
    						dialog.dismiss();
    						performReturn(context, myExtras, uri.toString());
    						MediaScannerConnection connection = mConnectionMap.get(context);
    						if (connection != null) {
    							connection.disconnect();
    							mConnectionMap.put(context, null);
    						}
    					}
    				};
    				MediaScannerConnection connection = new MediaScannerConnection(context, client);
    				mConnectionMap.put(context, connection); 
    				connection.connect();
    			}
    		} else {
    			performReturn(context, myExtras, contentUri);
    		}
    	}
    }    
    static private void performReturn(Context context, Bundle myExtras, String contentUri) {
    	Intent result = new Intent(null, Uri.parse(contentUri));
    	boolean resultSet = false;
        if (myExtras != null) {
            final Uri outputUri = (Uri)myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
            if (outputUri != null) {
                Bundle extras = new Bundle();
                OutputStream outputStream = null;
                try {
                    outputStream = context.getContentResolver().openOutputStream(outputUri);
                    if (outputStream != null) {
                        InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(contentUri));
                        Utils.copyStream(inputStream, outputStream);
                        Util.closeSilently(inputStream);
                    }
                    ((Activity) context).setResult(Activity.RESULT_OK, new Intent(outputUri.toString())
                    .putExtras(extras));
                    resultSet = true;
                } catch (Exception ex) {
                    Log.e(TAG, "Cannot save to uri " + outputUri.toString());
                } finally {
                    Util.closeSilently(outputStream);
                }
            }
        }
    	if (!resultSet && myExtras != null && myExtras.getBoolean("return-data")) {
    		Bitmap bitmap = null;
    		try {
    			bitmap = UriTexture.createFromUri(context, contentUri, 1024, 1024, 0, null);
    		} catch (IOException e) {
    			;
    		} catch (URISyntaxException e) {
    			;
    		}
    		if (bitmap != null) {
    			result.putExtra("data", bitmap);
    		}
    	}
    	if (!resultSet)
    	    ((Activity) context).setResult(Activity.RESULT_OK, result);
    	((Activity) context).finish();
    }        
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mApp = new App(CropImage.this);
        mContentResolver = getContentResolver();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(Res.layout.cropimage);
        mImageView = (CropImageView) findViewById(Res.id.image);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.getString("circleCrop") != null) {
                mCircleCrop = true;
                mAspectX = 1;
                mAspectY = 1;
            }
            mSaveUri = (Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT);
            if (mSaveUri != null) {
                String outputFormatString = extras.getString("outputFormat");
                if (outputFormatString != null) {
                    mOutputFormat = Bitmap.CompressFormat.valueOf(outputFormatString);
                }
            }
            mBitmap = (Bitmap) extras.getParcelable("data");
            mAspectX = extras.getInt("aspectX");
            mAspectY = extras.getInt("aspectY");
            mOutputX = extras.getInt("outputX");
            mOutputY = extras.getInt("outputY");
            mScale = extras.getBoolean("scale", true);
            mScaleUp = extras.getBoolean("scaleUpIfNeeded", true);
            mDoFaceDetection = extras.containsKey("noFaceDetection") ? !extras.getBoolean("noFaceDetection") : true;
        }
        if (mBitmap == null) {
            Uri target = intent.getData();
            String targetScheme = target.getScheme();
            int rotation = 0;
            if (targetScheme.equals("content")) {
                mItem = LocalDataSource.createMediaItemFromUri(this, target, MediaItem.MEDIA_TYPE_IMAGE);
            }
            try {
                if (mItem != null) {
                    mBitmap = UriTexture.createFromUri(this, mItem.mContentUri, 1024, 1024, 0, null);
                    rotation = (int) mItem.mRotation;
                } else {
                    mBitmap = UriTexture.createFromUri(this, target.toString(), 1024, 1024, 0, null);
                    if (targetScheme.equals("file")) {
                        ExifInterface exif = new ExifInterface(target.getPath());
                        rotation = (int) Shared.exifOrientationToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL));
                    }
                }
            } catch (IOException e) {
            } catch (URISyntaxException e) {
            }
            if (mBitmap != null && rotation != 0f) {
                mBitmap = Util.rotate(mBitmap, rotation);
            }
        }
        if (mBitmap == null) {
            Log.e(TAG, "Cannot load bitmap, exiting.");
            finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        findViewById(Res.id.discard).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        findViewById(Res.id.save).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSaveClicked();
            }
        });
        startFaceDetection();
    }
    private void startFaceDetection() {
        if (isFinishing()) {
            return;
        }
        mImageView.setImageBitmapResetBase(mBitmap, true);
        Util.startBackgroundJob(this, null, getResources().getString(Res.string.running_face_detection), new Runnable() {
            public void run() {
                final CountDownLatch latch = new CountDownLatch(1);
                final Bitmap b = mBitmap;
                mHandler.post(new Runnable() {
                    public void run() {
                        if (b != mBitmap && b != null) {
                            mImageView.setImageBitmapResetBase(b, true);
                            mBitmap.recycle();
                            mBitmap = b;
                        }
                        if (mImageView.getScale() == 1.0f) {
                            mImageView.center(true, true);
                        }
                        latch.countDown();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                mRunFaceDetection.run();
            }
        }, mHandler);
    }
    private void onSaveClicked() {
        if (mSaving)
            return;
        if (mCrop == null) {
            return;
        }
        mSaving = true;
        Rect r = mCrop.getCropRect();
        int width = r.width(); 
        int height = r.height();
        Bitmap croppedImage = Bitmap.createBitmap(width, height, mCircleCrop ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        {
            Canvas canvas = new Canvas(croppedImage);
            Rect dstRect = new Rect(0, 0, width, height);
            canvas.drawBitmap(mBitmap, r, dstRect, null);
        }
        if (mCircleCrop) {
            Canvas c = new Canvas(croppedImage);
            Path p = new Path();
            p.addCircle(width / 2F, height / 2F, width / 2F, Path.Direction.CW);
            c.clipPath(p, Region.Op.DIFFERENCE);
            c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        }
        if (mOutputX != 0 && mOutputY != 0) {
            if (mScale) {
                Bitmap old = croppedImage;
                croppedImage = Util.transform(new Matrix(), croppedImage, mOutputX, mOutputY, mScaleUp);
                if (old != croppedImage) {
                    old.recycle();
                }
            } else {
                Bitmap b = Bitmap.createBitmap(mOutputX, mOutputY, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(b);
                Rect srcRect = mCrop.getCropRect();
                Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);
                int dx = (srcRect.width() - dstRect.width()) / 2;
                int dy = (srcRect.height() - dstRect.height()) / 2;
                srcRect.inset(Math.max(0, dx), Math.max(0, dy));
                dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));
                canvas.drawBitmap(mBitmap, srcRect, dstRect, null);
                croppedImage.recycle();
                croppedImage = b;
            }
        }
        Bundle myExtras = getIntent().getExtras();
        if (myExtras != null && (myExtras.getParcelable("data") != null || myExtras.getBoolean("return-data"))) {
            Bundle extras = new Bundle();
            extras.putParcelable("data", croppedImage);
            setResult(RESULT_OK, (new Intent()).setAction("inline-data").putExtras(extras));
            finish();
        } else {
            final Bitmap b = croppedImage;
            final Runnable save = new Runnable() {
                public void run() {
                    saveOutput(b);
                }
            };
            Util.startBackgroundJob(this, null, getResources().getString(Res.string.saving_image), save, mHandler);
        }
    }
    private void saveOutput(Bitmap croppedImage) {
        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(mSaveUri);
                if (outputStream != null) {
                    croppedImage.compress(mOutputFormat, 75, outputStream);
                }
            } catch (IOException ex) {
                Log.e(TAG, "Cannot open file: " + mSaveUri, ex);
            } finally {
                Util.closeSilently(outputStream);
            }
            Bundle extras = new Bundle();
            setResult(RESULT_OK, new Intent(mSaveUri.toString()).putExtras(extras));
        } else {
            Bundle extras = new Bundle();
            extras.putString("rect", mCrop.getCropRect().toString());
            if (mItem == null) {
            } else {
                File oldPath = new File(mItem.mFilePath);
                File directory = new File(oldPath.getParent());
                int x = 0;
                String fileName = oldPath.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                while (true) {
                    x += 1;
                    String candidate = directory.toString() + "/" + fileName + "-" + x + ".jpg";
                    boolean exists = (new File(candidate)).exists();
                    if (!exists) { 
                        break;
                    }
                }
                MediaItem item = mItem;
                String title = fileName + "-" + x;
                String finalFileName = title + ".jpg";
                int[] degree = new int[1];
                Double latitude = null;
                Double longitude = null;
                if (item.isLatLongValid()) {
                    latitude = new Double(item.mLatitude);
                    longitude = new Double(item.mLongitude);
                }
                Uri newUri = ImageManager.addImage(mContentResolver, title,
                        item.mDateAddedInSec, item.mDateTakenInMs, latitude,
                        longitude, directory.toString(), finalFileName,
                        croppedImage, null, degree);
                if (newUri != null) {
                    setResult(RESULT_OK, new Intent().setAction(newUri.toString()).putExtras(extras));
                } else {
                    setResult(RESULT_OK, new Intent().setAction(null));
                }
            }
        }
        croppedImage.recycle();
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
    	mApp.onResume();
    }    
    @Override
    protected void onPause() {
        super.onPause();
        BitmapManager.instance().cancelThreadDecoding(mDecodingThreads);
    	mApp.onPause();
    }
    @Override
    protected void onDestroy() {
        mApp.shutdown();
        super.onDestroy();
    }
    Runnable mRunFaceDetection = new Runnable() {
        float mScale = 1F;
        Matrix mImageMatrix;
        FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
        int mNumFaces;
        private void handleFace(FaceDetector.Face f) {
            PointF midPoint = new PointF();
            int r = ((int) (f.eyesDistance() * mScale)) * 2;
            f.getMidPoint(midPoint);
            midPoint.x *= mScale;
            midPoint.y *= mScale;
            int midX = (int) midPoint.x;
            int midY = (int) midPoint.y;
            HighlightView hv = new HighlightView(mImageView);
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            Rect imageRect = new Rect(0, 0, width, height);
            RectF faceRect = new RectF(midX, midY, midX, midY);
            faceRect.inset(-r, -r);
            if (faceRect.left < 0) {
                faceRect.inset(-faceRect.left, -faceRect.left);
            }
            if (faceRect.top < 0) {
                faceRect.inset(-faceRect.top, -faceRect.top);
            }
            if (faceRect.right > imageRect.right) {
                faceRect.inset(faceRect.right - imageRect.right, faceRect.right - imageRect.right);
            }
            if (faceRect.bottom > imageRect.bottom) {
                faceRect.inset(faceRect.bottom - imageRect.bottom, faceRect.bottom - imageRect.bottom);
            }
            hv.setup(mImageMatrix, imageRect, faceRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
            mImageView.add(hv);
        }
        private void makeDefault() {
            HighlightView hv = new HighlightView(mImageView);
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            Rect imageRect = new Rect(0, 0, width, height);
            int cropWidth = Math.min(width, height) * 4 / 5;
            int cropHeight = cropWidth;
            if (mAspectX != 0 && mAspectY != 0) {
                if (mAspectX > mAspectY) {
                    cropHeight = cropWidth * mAspectY / mAspectX;
                } else {
                    cropWidth = cropHeight * mAspectX / mAspectY;
                }
            }
            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;
            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
            mImageView.add(hv);
        }
        private Bitmap prepareBitmap() {
            if (mBitmap == null) {
                return null;
            }
            if (mBitmap.getWidth() > 256) {
                mScale = 256.0F / mBitmap.getWidth(); 
            }
            Matrix matrix = new Matrix();
            matrix.setScale(mScale, mScale);
            Bitmap faceBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            return faceBitmap;
        }
        public void run() {
            mImageMatrix = mImageView.getImageMatrix();
            Bitmap faceBitmap = prepareBitmap();
            mScale = 1.0F / mScale;
            if (faceBitmap != null && mDoFaceDetection) {
                FaceDetector detector = new FaceDetector(faceBitmap.getWidth(), faceBitmap.getHeight(), mFaces.length);
                mNumFaces = detector.findFaces(faceBitmap, mFaces);
            }
            if (faceBitmap != null && faceBitmap != mBitmap) {
                faceBitmap.recycle();
            }
            mHandler.post(new Runnable() {
                public void run() {
                    mWaitingToPick = mNumFaces > 1;
                    if (mNumFaces > 0) {
                        for (int i = 0; i < mNumFaces; i++) {
                            handleFace(mFaces[i]);
                        }
                    } else {
                        makeDefault();
                    }
                    mImageView.invalidate();
                    if (mImageView.mHighlightViews.size() == 1) {
                        mCrop = mImageView.mHighlightViews.get(0);
                        mCrop.setFocus(true);
                    }
                    if (mNumFaces > 1) {
                        Toast t = Toast.makeText(CropImage.this, Res.string.multiface_crop_help, Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            });
        }
    };
}
class CropImageView extends ImageViewTouchBase {
    ArrayList<HighlightView> mHighlightViews = new ArrayList<HighlightView>();
    HighlightView mMotionHighlightView = null;
    float mLastX, mLastY;
    int mMotionEdge;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mBitmapDisplayed.getBitmap() != null) {
            for (HighlightView hv : mHighlightViews) {
                hv.mMatrix.set(getImageMatrix());
                hv.invalidate();
                if (hv.mIsFocused) {
                    centerBasedOnHighlightView(hv);
                }
            }
        }
    }
    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }
    @Override
    protected void zoomIn() {
        super.zoomIn();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }
    @Override
    protected void zoomOut() {
        super.zoomOut();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }
    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }
    private void recomputeFocus(MotionEvent event) {
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.setFocus(false);
            hv.invalidate();
        }
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            int edge = hv.getHit(event.getX(), event.getY());
            if (edge != HighlightView.GROW_NONE) {
                if (!hv.hasFocus()) {
                    hv.setFocus(true);
                    hv.invalidate();
                }
                break;
            }
        }
        invalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        CropImage cropImage = (CropImage) getContext();
        if (cropImage.mSaving) {
            return false;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: 
            if (cropImage.mWaitingToPick) {
                recomputeFocus(event);
            } else {
                for (int i = 0; i < mHighlightViews.size(); i++) { 
                    HighlightView hv = mHighlightViews.get(i);
                    int edge = hv.getHit(event.getX(), event.getY());
                    if (edge != HighlightView.GROW_NONE) {
                        mMotionEdge = edge;
                        mMotionHighlightView = hv;
                        mLastX = event.getX();
                        mLastY = event.getY();
                        mMotionHighlightView.setMode((edge == HighlightView.MOVE) ? HighlightView.ModifyMode.Move
                                : HighlightView.ModifyMode.Grow);
                        break;
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (cropImage.mWaitingToPick) {
                for (int i = 0; i < mHighlightViews.size(); i++) {
                    HighlightView hv = mHighlightViews.get(i);
                    if (hv.hasFocus()) {
                        cropImage.mCrop = hv;
                        for (int j = 0; j < mHighlightViews.size(); j++) {
                            if (j == i) { 
                                continue;
                            }
                            mHighlightViews.get(j).setHidden(true);
                        }
                        centerBasedOnHighlightView(hv);
                        ((CropImage) getContext()).mWaitingToPick = false;
                        return true;
                    }
                }
            } else if (mMotionHighlightView != null) {
                centerBasedOnHighlightView(mMotionHighlightView);
                mMotionHighlightView.setMode(HighlightView.ModifyMode.None);
            }
            mMotionHighlightView = null;
            break;
        case MotionEvent.ACTION_MOVE:
            if (cropImage.mWaitingToPick) {
                recomputeFocus(event);
            } else if (mMotionHighlightView != null) {
                mMotionHighlightView.handleMotion(mMotionEdge, event.getX() - mLastX, event.getY() - mLastY);
                mLastX = event.getX();
                mLastY = event.getY();
                if (true) {
                    ensureVisible(mMotionHighlightView);
                }
            }
            break;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            center(true, true);
            break;
        case MotionEvent.ACTION_MOVE:
            if (getScale() == 1F) {
                center(true, true);
            }
            break;
        }
        return true;
    }
    private void ensureVisible(HighlightView hv) {
        Rect r = hv.mDrawRect;
        int panDeltaX1 = Math.max(0, getLeft() - r.left);
        int panDeltaX2 = Math.min(0, getRight() - r.right);
        int panDeltaY1 = Math.max(0, getTop() - r.top);
        int panDeltaY2 = Math.min(0, getBottom() - r.bottom);
        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;
        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }
    private void centerBasedOnHighlightView(HighlightView hv) {
        Rect drawRect = hv.mDrawRect;
        float width = drawRect.width();
        float height = drawRect.height();
        float thisWidth = getWidth();
        float thisHeight = getHeight();
        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;
        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);
        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float[] coordinates = new float[] { hv.mCropRect.centerX(), hv.mCropRect.centerY() };
            getImageMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F); 
        }
        ensureVisible(hv);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mHighlightViews.size(); i++) {
            mHighlightViews.get(i).draw(canvas);
        }
    }
    public void add(HighlightView hv) {
        mHighlightViews.add(hv);
        invalidate();
    }
}
