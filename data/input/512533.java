public class AppWidgetHostView extends FrameLayout {
    static final String TAG = "AppWidgetHostView";
    static final boolean LOGD = false;
    static final boolean CROSSFADE = false;
    static final int UPDATE_FLAGS_RESET = 0x00000001;
    static final int VIEW_MODE_NOINIT = 0;
    static final int VIEW_MODE_CONTENT = 1;
    static final int VIEW_MODE_ERROR = 2;
    static final int VIEW_MODE_DEFAULT = 3;
    static final int FADE_DURATION = 1000;
    static final LayoutInflater.Filter sInflaterFilter = new LayoutInflater.Filter() {
        public boolean onLoadClass(Class clazz) {
            return clazz.isAnnotationPresent(RemoteViews.RemoteView.class);
        }
    };
    Context mContext;
    Context mRemoteContext;
    int mAppWidgetId;
    AppWidgetProviderInfo mInfo;
    View mView;
    int mViewMode = VIEW_MODE_NOINIT;
    int mLayoutId = -1;
    long mFadeStartTime = -1;
    Bitmap mOld;
    Paint mOldPaint = new Paint();
    public AppWidgetHostView(Context context) {
        this(context, android.R.anim.fade_in, android.R.anim.fade_out);
    }
    @SuppressWarnings({"UnusedDeclaration"})
    public AppWidgetHostView(Context context, int animationIn, int animationOut) {
        super(context);
        mContext = context;
    }
    public void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        mAppWidgetId = appWidgetId;
        mInfo = info;
    }
    public int getAppWidgetId() {
        return mAppWidgetId;
    }
    public AppWidgetProviderInfo getAppWidgetInfo() {
        return mInfo;
    }
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        final ParcelableSparseArray jail = new ParcelableSparseArray();
        super.dispatchSaveInstanceState(jail);
        container.put(generateId(), jail);
    }
    private int generateId() {
        final int id = getId();
        return id == View.NO_ID ? mAppWidgetId : id;
    }
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        final Parcelable parcelable = container.get(generateId());
        ParcelableSparseArray jail = null;
        if (parcelable != null && parcelable instanceof ParcelableSparseArray) {
            jail = (ParcelableSparseArray) parcelable;
        }
        if (jail == null) jail = new ParcelableSparseArray();
        super.dispatchRestoreInstanceState(jail);
    }
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        final Context context = mRemoteContext != null ? mRemoteContext : mContext;
        return new FrameLayout.LayoutParams(context, attrs);
    }
    public void updateAppWidget(RemoteViews remoteViews) {
        updateAppWidget(remoteViews, 0);
    }
    void updateAppWidget(RemoteViews remoteViews, int flags) {
        if (LOGD) Log.d(TAG, "updateAppWidget called mOld=" + mOld + " flags=0x"
                + Integer.toHexString(flags));
        if ((flags & UPDATE_FLAGS_RESET) != 0) {
            mViewMode = VIEW_MODE_NOINIT;
        }
        boolean recycled = false;
        View content = null;
        Exception exception = null;
        if (CROSSFADE) {
            if (mFadeStartTime < 0) {
                if (mView != null) {
                    final int width = mView.getWidth();
                    final int height = mView.getHeight();
                    try {
                        mOld = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    } catch (OutOfMemoryError e) {
                        mOld = null;
                    }
                    if (mOld != null) {
                    }
                }
            }
        }
        if (remoteViews == null) {
            if (mViewMode == VIEW_MODE_DEFAULT) {
                return;
            }
            content = getDefaultView();
            mLayoutId = -1;
            mViewMode = VIEW_MODE_DEFAULT;
        } else {
            mRemoteContext = getRemoteContext(remoteViews);
            int layoutId = remoteViews.getLayoutId();
            if (content == null && layoutId == mLayoutId) {
                try {
                    remoteViews.reapply(mContext, mView);
                    content = mView;
                    recycled = true;
                    if (LOGD) Log.d(TAG, "was able to recycled existing layout");
                } catch (RuntimeException e) {
                    exception = e;
                }
            }
            if (content == null) {
                try {
                    content = remoteViews.apply(mContext, this);
                    if (LOGD) Log.d(TAG, "had to inflate new layout");
                } catch (RuntimeException e) {
                    exception = e;
                }
            }
            mLayoutId = layoutId;
            mViewMode = VIEW_MODE_CONTENT;
        }
        if (content == null) {
            if (mViewMode == VIEW_MODE_ERROR) {
                return ;
            }
            Log.w(TAG, "updateAppWidget couldn't find any view, using error view", exception);
            content = getErrorView();
            mViewMode = VIEW_MODE_ERROR;
        }
        if (!recycled) {
            prepareView(content);
            addView(content);
        }
        if (mView != content) {
            removeView(mView);
            mView = content;
        }
        if (CROSSFADE) {
            if (mFadeStartTime < 0) {
                mFadeStartTime = SystemClock.uptimeMillis();
                invalidate();
            }
        }
    }
    private Context getRemoteContext(RemoteViews views) {
        final String packageName = views.getPackage();
        if (packageName == null) return mContext;
        try {
            return mContext.createPackageContext(packageName, Context.CONTEXT_RESTRICTED);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name " + packageName + " not found");
            return mContext;
        }
    }
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (CROSSFADE) {
            int alpha;
            int l = child.getLeft();
            int t = child.getTop();
            if (mFadeStartTime > 0) {
                alpha = (int)(((drawingTime-mFadeStartTime)*255)/FADE_DURATION);
                if (alpha > 255) {
                    alpha = 255;
                }
                Log.d(TAG, "drawChild alpha=" + alpha + " l=" + l + " t=" + t
                        + " w=" + child.getWidth());
                if (alpha != 255 && mOld != null) {
                    mOldPaint.setAlpha(255-alpha);
                }
            } else {
                alpha = 255;
            }
            int restoreTo = canvas.saveLayerAlpha(l, t, child.getWidth(), child.getHeight(), alpha,
                    Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
            boolean rv = super.drawChild(canvas, child, drawingTime);
            canvas.restoreToCount(restoreTo);
            if (alpha < 255) {
                invalidate();
            } else {
                mFadeStartTime = -1;
                if (mOld != null) {
                    mOld.recycle();
                    mOld = null;
                }
            }
            return rv;
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }
    protected void prepareView(View view) {
        FrameLayout.LayoutParams requested = (FrameLayout.LayoutParams)view.getLayoutParams();
        if (requested == null) {
            requested = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
        }
        requested.gravity = Gravity.CENTER;
        view.setLayoutParams(requested);
    }
    protected View getDefaultView() {
        if (LOGD) {
            Log.d(TAG, "getDefaultView");
        }
        View defaultView = null;
        Exception exception = null;
        try {
            if (mInfo != null) {
                Context theirContext = mContext.createPackageContext(
                        mInfo.provider.getPackageName(), Context.CONTEXT_RESTRICTED);
                mRemoteContext = theirContext;
                LayoutInflater inflater = (LayoutInflater)
                        theirContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater = inflater.cloneInContext(theirContext);
                inflater.setFilter(sInflaterFilter);
                defaultView = inflater.inflate(mInfo.initialLayout, this, false);
            } else {
                Log.w(TAG, "can't inflate defaultView because mInfo is missing");
            }
        } catch (PackageManager.NameNotFoundException e) {
            exception = e;
        } catch (RuntimeException e) {
            exception = e;
        }
        if (exception != null) {
            Log.w(TAG, "Error inflating AppWidget " + mInfo + ": " + exception.toString());
        }
        if (defaultView == null) {
            if (LOGD) Log.d(TAG, "getDefaultView couldn't find any view, so inflating error");
            defaultView = getErrorView();
        }
        return defaultView;
    }
    protected View getErrorView() {
        TextView tv = new TextView(mContext);
        tv.setText(com.android.internal.R.string.gadget_host_error_inflating);
        tv.setBackgroundColor(Color.argb(127, 0, 0, 0));
        return tv;
    }
    private static class ParcelableSparseArray extends SparseArray<Parcelable> implements Parcelable {
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            final int count = size();
            dest.writeInt(count);
            for (int i = 0; i < count; i++) {
                dest.writeInt(keyAt(i));
                dest.writeParcelable(valueAt(i), 0);
            }
        }
        public static final Parcelable.Creator<ParcelableSparseArray> CREATOR =
                new Parcelable.Creator<ParcelableSparseArray>() {
                    public ParcelableSparseArray createFromParcel(Parcel source) {
                        final ParcelableSparseArray array = new ParcelableSparseArray();
                        final ClassLoader loader = array.getClass().getClassLoader();
                        final int count = source.readInt();
                        for (int i = 0; i < count; i++) {
                            array.put(source.readInt(), source.readParcelable(loader));
                        }
                        return array;
                    }
                    public ParcelableSparseArray[] newArray(int size) {
                        return new ParcelableSparseArray[size];
                    }
                };
    }
}
