public class ImageGallery extends NoSearchActivity implements
        GridViewSpecial.Listener, GridViewSpecial.DrawAdapter {
    private static final String STATE_SCROLL_POSITION = "scroll_position";
    private static final String STATE_SELECTED_INDEX = "first_index";
    private static final String TAG = "ImageGallery";
    private static final float INVALID_POSITION = -1f;
    private ImageManager.ImageListParam mParam;
    private IImageList mAllImages;
    private int mInclusion;
    boolean mSortAscending = false;
    private View mNoImagesView;
    public static final int CROP_MSG = 2;
    private Dialog mMediaScanningDialog;
    private MenuItem mSlideShowItem;
    private SharedPreferences mPrefs;
    private long mVideoSizeLimit = Long.MAX_VALUE;
    private View mFooterOrganizeView;
    private BroadcastReceiver mReceiver = null;
    private final Handler mHandler = new Handler();
    private boolean mLayoutComplete;
    private boolean mPausing = true;
    private ImageLoader mLoader;
    private GridViewSpecial mGvs;
    private Uri mCropResultUri;
    private int mSelectedIndex = GridViewSpecial.INDEX_NONE;
    private float mScrollPosition = INVALID_POSITION;
    private boolean mConfigurationChanged = false;
    private HashSet<IImage> mMultiSelected = null;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.image_gallery);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.custom_gallery_title);
        mNoImagesView = findViewById(R.id.no_images);
        mGvs = (GridViewSpecial) findViewById(R.id.grid);
        mGvs.setListener(this);
        mFooterOrganizeView = findViewById(R.id.footer_organize);
        mFooterOrganizeView.setOnClickListener(Util.getNullOnClickListener());
        initializeFooterButtons();
        if (isPickIntent()) {
            mVideoSizeLimit = getIntent().getLongExtra(
                    MediaStore.EXTRA_SIZE_LIMIT, Long.MAX_VALUE);
        } else {
            mVideoSizeLimit = Long.MAX_VALUE;
            mGvs.setOnCreateContextMenuListener(
                    new CreateContextMenuListener());
        }
        setupInclusion();
        mLoader = new ImageLoader(getContentResolver(), mHandler);
    }
    private void initializeFooterButtons() {
        Button deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onDeleteMultipleClicked();
            }
        });
        Button shareButton = (Button) findViewById(R.id.button_share);
        shareButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onShareMultipleClicked();
            }
        });
        Button closeButton = (Button) findViewById(R.id.button_close);
        closeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                closeMultiSelectMode();
            }
        });
    }
    private MenuItem addSlideShowMenu(Menu menu) {
        return menu.add(Menu.NONE, Menu.NONE, MenuHelper.POSITION_SLIDESHOW,
                R.string.slide_show)
                .setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        return onSlideShowClicked();
                    }
                }).setIcon(android.R.drawable.ic_menu_slideshow);
    }
    public boolean onSlideShowClicked() {
        if (!canHandleEvent()) {
            return false;
        }
        IImage img = getCurrentImage();
        if (img == null) {
            img = mAllImages.getImageAt(0);
            if (img == null) {
                return true;
            }
        }
        Uri targetUri = img.fullSizeImageUri();
        Uri thisUri = getIntent().getData();
        if (thisUri != null) {
            String bucket = thisUri.getQueryParameter("bucketId");
            if (bucket != null) {
                targetUri = targetUri.buildUpon()
                        .appendQueryParameter("bucketId", bucket)
                        .build();
            }
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, targetUri);
        intent.putExtra("slideshow", true);
        startActivity(intent);
        return true;
    }
    private final Runnable mDeletePhotoRunnable = new Runnable() {
        public void run() {
            if (!canHandleEvent()) return;
            IImage currentImage = getCurrentImage();
            mGvs.stop();
            if (currentImage != null) {
                mAllImages.removeImage(currentImage);
            }
            mGvs.setImageList(mAllImages);
            mGvs.start();
            mNoImagesView.setVisibility(mAllImages.isEmpty()
                    ? View.VISIBLE
                    : View.GONE);
        }
    };
    private Uri getCurrentImageUri() {
        IImage image = getCurrentImage();
        if (image != null) {
            return image.fullSizeImageUri();
        } else {
            return null;
        }
    }
    private IImage getCurrentImage() {
        int currentSelection = mGvs.getCurrentSelection();
        if (currentSelection < 0
                || currentSelection >= mAllImages.getCount()) {
            return null;
        } else {
            return mAllImages.getImageAt(currentSelection);
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mConfigurationChanged = true;
    }
    boolean canHandleEvent() {
        return (!mPausing) && (mLayoutComplete);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!canHandleEvent()) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                IImage image = getCurrentImage();
                if (image != null) {
                    MenuHelper.deleteImage(
                            this, mDeletePhotoRunnable, getCurrentImage());
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private boolean isPickIntent() {
        String action = getIntent().getAction();
        return (Intent.ACTION_PICK.equals(action)
                || Intent.ACTION_GET_CONTENT.equals(action));
    }
    private void launchCropperOrFinish(IImage img) {
        Bundle myExtras = getIntent().getExtras();
        long size = MenuHelper.getImageFileSize(img);
        if (size < 0) {
            return;
        }
        if (size > mVideoSizeLimit) {
            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.file_info_title)
                    .setMessage(R.string.video_exceed_mms_limit)
                    .setNeutralButton(R.string.details_ok, buttonListener)
                    .show();
            return;
        }
        String cropValue = myExtras != null ? myExtras.getString("crop") : null;
        if (cropValue != null) {
            Bundle newExtras = new Bundle();
            if (cropValue.equals("circle")) {
                newExtras.putString("circleCrop", "true");
            }
            Intent cropIntent = new Intent();
            cropIntent.setData(img.fullSizeImageUri());
            cropIntent.setClass(this, CropImage.class);
            cropIntent.putExtras(newExtras);
            cropIntent.putExtras(myExtras);
            startActivityForResult(cropIntent, CROP_MSG);
        } else {
            Intent result = new Intent(null, img.fullSizeImageUri());
            if (myExtras != null && myExtras.getBoolean("return-data")) {
                Bitmap bitmap = img.fullSizeBitmap(
                        IImage.UNCONSTRAINED, 100 * 1024);
                if (bitmap != null) {
                    result.putExtra("data", bitmap);
                }
            }
            setResult(RESULT_OK, result);
            finish();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        switch (requestCode) {
            case MenuHelper.RESULT_COMMON_MENU_CROP: {
                if (resultCode == RESULT_OK) {
                    mCropResultUri = Uri.parse(data.getAction());
                }
                break;
            }
            case CROP_MSG: {
                if (resultCode == RESULT_OK) {
                    setResult(resultCode, data);
                    finish();
                }
                break;
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        mPausing = true;
        mLoader.stop();
        mGvs.stop();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mAllImages.close();
        mAllImages = null;
    }
    private void rebake(boolean unmounted, boolean scanning) {
        mGvs.stop();
        if (mAllImages != null) {
            mAllImages.close();
            mAllImages = null;
        }
        if (mMediaScanningDialog != null) {
            mMediaScanningDialog.cancel();
            mMediaScanningDialog = null;
        }
        if (scanning) {
            mMediaScanningDialog = ProgressDialog.show(
                    this,
                    null,
                    getResources().getString(R.string.wait),
                    true,
                    true);
        }
        mParam = allImages(!unmounted && !scanning);
        mAllImages = ImageManager.makeImageList(getContentResolver(), mParam);
        mGvs.setImageList(mAllImages);
        mGvs.setDrawAdapter(this);
        mGvs.setLoader(mLoader);
        mGvs.start();
        mNoImagesView.setVisibility(mAllImages.getCount() > 0
                ? View.GONE
                : View.VISIBLE);
    }
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putFloat(STATE_SCROLL_POSITION, mScrollPosition);
        state.putInt(STATE_SELECTED_INDEX, mSelectedIndex);
    }
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mScrollPosition = state.getFloat(
                STATE_SCROLL_POSITION, INVALID_POSITION);
        mSelectedIndex = state.getInt(STATE_SELECTED_INDEX, 0);
    }
    @Override
    public void onResume() {
        super.onResume();
        mGvs.setSizeChoice(Integer.parseInt(
                mPrefs.getString("pref_gallery_size_key", "1")));
        mGvs.requestFocus();
        String sortOrder = mPrefs.getString("pref_gallery_sort_key", null);
        if (sortOrder != null) {
            mSortAscending = sortOrder.equals("ascending");
        }
        mPausing = false;
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    rebake(true, false);
                } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                    rebake(false, true);
                } else if (action.equals(
                        Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                    rebake(false, false);
                } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                    rebake(true, false);
                }
            }
        };
        registerReceiver(mReceiver, intentFilter);
        rebake(false, ImageManager.isMediaScannerScanning(
                getContentResolver()));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isPickIntent()) {
            String type = getIntent().resolveType(this);
            if (type != null) {
                if (isImageType(type)) {
                    MenuHelper.addCapturePictureMenuItems(menu, this);
                } else if (isVideoType(type)) {
                    MenuHelper.addCaptureVideoMenuItems(menu, this);
                }
            }
        } else {
            MenuHelper.addCaptureMenuItems(menu, this);
            if ((mInclusion & ImageManager.INCLUDE_IMAGES) != 0) {
                mSlideShowItem = addSlideShowMenu(menu);
            }
            MenuItem item = menu.add(Menu.NONE, Menu.NONE,
                    MenuHelper.POSITION_GALLERY_SETTING,
                    R.string.camerasettings);
            item.setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    Intent preferences = new Intent();
                    preferences.setClass(ImageGallery.this,
                            GallerySettings.class);
                    startActivity(preferences);
                    return true;
                }
            });
            item.setAlphabeticShortcut('p');
            item.setIcon(android.R.drawable.ic_menu_preferences);
            item = menu.add(Menu.NONE, Menu.NONE,
                    MenuHelper.POSITION_MULTISELECT,
                    R.string.multiselect);
            item.setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (isInMultiSelectMode()) {
                        closeMultiSelectMode();
                    } else {
                        openMultiSelectMode();
                    }
                    return true;
                }
            });
            item.setIcon(R.drawable.ic_menu_multiselect_gallery);
        }
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!canHandleEvent()) return false;
        if ((mInclusion & ImageManager.INCLUDE_IMAGES) != 0) {
            boolean videoSelected = isVideoSelected();
            if (mSlideShowItem != null) {
                mSlideShowItem.setEnabled(!videoSelected);
            }
        }
        return true;
    }
    private boolean isVideoSelected() {
        IImage image = getCurrentImage();
        return (image != null) && ImageManager.isVideo(image);
    }
    private boolean isImageType(String type) {
        return type.equals("vnd.android.cursor.dir/image")
                || type.equals("image*";
    }
    private void onShareMultipleClicked() {
        if (mMultiSelected == null) return;
        if (mMultiSelected.size() > 1) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            String mimeType = getShareMultipleMimeType();
            intent.setType(mimeType);
            ArrayList<Parcelable> list = new ArrayList<Parcelable>();
            for (IImage image : mMultiSelected) {
                list.add(image.fullSizeImageUri());
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, list);
            try {
                startActivity(Intent.createChooser(
                        intent, getText(R.string.send_media_files)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, R.string.no_way_to_share,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (mMultiSelected.size() == 1) {
            IImage image = mMultiSelected.iterator().next();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            String mimeType = image.getMimeType();
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, image.fullSizeImageUri());
            boolean isImage = ImageManager.isImage(image);
            try {
                startActivity(Intent.createChooser(intent, getText(
                        isImage ? R.string.sendImage : R.string.sendVideo)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, isImage
                        ? R.string.no_way_to_share_image
                        : R.string.no_way_to_share_video,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void onDeleteMultipleClicked() {
        if (mMultiSelected == null) return;
        Runnable action = new Runnable() {
            public void run() {
                ArrayList<Uri> uriList = new ArrayList<Uri>();
                for (IImage image : mMultiSelected) {
                    uriList.add(image.fullSizeImageUri());
                }
                closeMultiSelectMode();
                Intent intent = new Intent(ImageGallery.this,
                        DeleteImage.class);
                intent.putExtra("delete-uris", uriList);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.e(TAG, "Delete images fail", ex);
                }
            }
        };
        MenuHelper.deleteMultiple(this, action);
    }
    private boolean isInMultiSelectMode() {
        return mMultiSelected != null;
    }
    private void closeMultiSelectMode() {
        if (mMultiSelected == null) return;
        mMultiSelected = null;
        mGvs.invalidate();
        hideFooter();
    }
    private void openMultiSelectMode() {
        if (mMultiSelected != null) return;
        mMultiSelected = new HashSet<IImage>();
        mGvs.invalidate();
    }
}
