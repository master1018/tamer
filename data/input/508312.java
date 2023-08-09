public class ThumbnailController {
    @SuppressWarnings("unused")
    private static final String TAG = "ThumbnailController";
    private final ContentResolver mContentResolver;
    private Uri mUri;
    private Bitmap mThumb;
    private final ImageView mButton;
    private Drawable[] mThumbs;
    private TransitionDrawable mThumbTransition;
    private boolean mShouldAnimateThumb;
    private final Resources mResources;
    public ThumbnailController(Resources resources,
            ImageView button, ContentResolver contentResolver) {
        mResources = resources;
        mButton = button;
        mContentResolver = contentResolver;
    }
    public void setData(Uri uri, Bitmap original) {
        if (uri == null || original == null) {
            uri = null;
            original = null;
        }
        mUri = uri;
        updateThumb(original);
    }
    public Uri getUri() {
        return mUri;
    }
    private static final int BUFSIZE = 4096;
    public boolean storeData(String filePath) {
        if (mUri == null) {
            return false;
        }
        FileOutputStream f = null;
        BufferedOutputStream b = null;
        DataOutputStream d = null;
        try {
            f = new FileOutputStream(filePath);
            b = new BufferedOutputStream(f, BUFSIZE);
            d = new DataOutputStream(b);
            d.writeUTF(mUri.toString());
            mThumb.compress(Bitmap.CompressFormat.PNG, 100, d);
            d.close();
        } catch (IOException e) {
            return false;
        } finally {
            MenuHelper.closeSilently(f);
            MenuHelper.closeSilently(b);
            MenuHelper.closeSilently(d);
        }
        return true;
    }
    public boolean loadData(String filePath) {
        FileInputStream f = null;
        BufferedInputStream b = null;
        DataInputStream d = null;
        try {
            f = new FileInputStream(filePath);
            b = new BufferedInputStream(f, BUFSIZE);
            d = new DataInputStream(b);
            Uri uri = Uri.parse(d.readUTF());
            Bitmap thumb = BitmapFactory.decodeStream(d);
            setData(uri, thumb);
            d.close();
        } catch (IOException e) {
            return false;
        } finally {
            MenuHelper.closeSilently(f);
            MenuHelper.closeSilently(b);
            MenuHelper.closeSilently(d);
        }
        return true;
    }
    public void updateDisplayIfNeeded() {
        if (mUri == null) {
            mButton.setImageDrawable(null);
            return;
        }
        if (mShouldAnimateThumb) {
            mThumbTransition.startTransition(500);
            mShouldAnimateThumb = false;
        }
    }
    private void updateThumb(Bitmap original) {
        if (original == null) {
            mThumb = null;
            mThumbs = null;
            return;
        }
        LayoutParams param = mButton.getLayoutParams();
        final int miniThumbWidth = param.width
                - mButton.getPaddingLeft() - mButton.getPaddingRight();
        final int miniThumbHeight = param.height
                - mButton.getPaddingTop() - mButton.getPaddingBottom();
        mThumb = ThumbnailUtils.extractThumbnail(
                original, miniThumbWidth, miniThumbHeight);
        Drawable drawable;
        if (mThumbs == null) {
            mThumbs = new Drawable[2];
            mThumbs[1] = new BitmapDrawable(mResources, mThumb);
            drawable = mThumbs[1];
            mShouldAnimateThumb = false;
        } else {
            mThumbs[0] = mThumbs[1];
            mThumbs[1] = new BitmapDrawable(mResources, mThumb);
            mThumbTransition = new TransitionDrawable(mThumbs);
            drawable = mThumbTransition;
            mShouldAnimateThumb = true;
        }
        mButton.setImageDrawable(drawable);
    }
    public boolean isUriValid() {
        if (mUri == null) {
            return false;
        }
        try {
            ParcelFileDescriptor pfd =
                    mContentResolver.openFileDescriptor(mUri, "r");
            if (pfd == null) {
                Log.e(TAG, "Fail to open URI.");
                return false;
            }
            pfd.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
}
