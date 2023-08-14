public class QuickContactWindow implements Window.Callback,
        NotifyingAsyncQueryHandler.AsyncQueryListener, View.OnClickListener,
        AbsListView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, KeyEvent.Callback,
        OnGlobalLayoutListener {
    private static final String TAG = "QuickContactWindow";
    public interface OnDismissListener {
        public void onDismiss(QuickContactWindow dialog);
    }
    public static class RootLayout extends RelativeLayout {
        QuickContactWindow mQuickContactWindow;
        public RootLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        @Override
        public boolean dispatchKeyEventPreIme(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                mQuickContactWindow.onBackPressed();
                return true;
            } else {
                return super.dispatchKeyEventPreIme(event);
            }
        }
    }
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final WindowManager mWindowManager;
    private Window mWindow;
    private View mDecor;
    private final Rect mRect = new Rect();
    private boolean mDismissed = false;
    private boolean mQuerying = false;
    private boolean mShowing = false;
    private NotifyingAsyncQueryHandler mHandler;
    private OnDismissListener mDismissListener;
    private ResolveCache mResolveCache;
    private Uri mLookupUri;
    private Rect mAnchor;
    private int mShadowHoriz;
    private int mShadowVert;
    private int mShadowTouch;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mRequestedY;
    private boolean mHasValidSocial = false;
    private boolean mHasData = false;
    private boolean mMakePrimary = false;
    private ImageView mArrowUp;
    private ImageView mArrowDown;
    private int mMode;
    private RootLayout mRootView;
    private View mHeader;
    private HorizontalScrollView mTrackScroll;
    private ViewGroup mTrack;
    private Animation mTrackAnim;
    private View mFooter;
    private View mFooterDisambig;
    private ListView mResolveList;
    private CheckableImageView mLastAction;
    private CheckBox mSetPrimaryCheckBox;
    private int mWindowRecycled = 0;
    private int mActionRecycled = 0;
    private ActionMap mActions = new ActionMap();
    private LinkedList<View> mActionPool = new LinkedList<View>();
    private String[] mExcludeMimes;
    private static final String[] PRECEDING_MIMETYPES = new String[] {
            Phone.CONTENT_ITEM_TYPE,
            Contacts.CONTENT_ITEM_TYPE,
            Constants.MIME_SMS_ADDRESS,
            Email.CONTENT_ITEM_TYPE,
    };
    private static final String[] FOLLOWING_MIMETYPES = new String[] {
            StructuredPostal.CONTENT_ITEM_TYPE,
            Website.CONTENT_ITEM_TYPE,
    };
    private static final HashSet<String> sPreferResolve = Sets.newHashSet(
            "com.android.email",
            "com.android.calendar",
            "com.android.contacts",
            "com.android.mms",
            "com.android.phone",
            "com.android.browser");
    private static final int TOKEN_DATA = 1;
    static final boolean LOGD = false;
    static final boolean TRACE_LAUNCH = false;
    static final String TRACE_TAG = "quickcontact";
    public QuickContactWindow(Context context) {
        mContext = new ContextThemeWrapper(context, R.style.QuickContact);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindow = PolicyManager.makeNewWindow(mContext);
        mWindow.setCallback(this);
        mWindow.setWindowManager(mWindowManager, null, null);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        mWindow.setContentView(R.layout.quickcontact);
        mRootView = (RootLayout)mWindow.findViewById(R.id.root);
        mRootView.mQuickContactWindow = this;
        mRootView.setFocusable(true);
        mRootView.setFocusableInTouchMode(true);
        mRootView.setDescendantFocusability(RootLayout.FOCUS_AFTER_DESCENDANTS);
        mArrowUp = (ImageView)mWindow.findViewById(R.id.arrow_up);
        mArrowDown = (ImageView)mWindow.findViewById(R.id.arrow_down);
        mResolveCache = new ResolveCache(mContext);
        final Resources res = mContext.getResources();
        mShadowHoriz = res.getDimensionPixelSize(R.dimen.quickcontact_shadow_horiz);
        mShadowVert = res.getDimensionPixelSize(R.dimen.quickcontact_shadow_vert);
        mShadowTouch = res.getDimensionPixelSize(R.dimen.quickcontact_shadow_touch);
        mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
        mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
        mTrack = (ViewGroup)mWindow.findViewById(R.id.quickcontact);
        mTrackScroll = (HorizontalScrollView)mWindow.findViewById(R.id.scroll);
        mFooter = mWindow.findViewById(R.id.footer);
        mFooterDisambig = mWindow.findViewById(R.id.footer_disambig);
        mResolveList = (ListView)mWindow.findViewById(android.R.id.list);
        mSetPrimaryCheckBox = (CheckBox)mWindow.findViewById(android.R.id.checkbox);
        mSetPrimaryCheckBox.setOnCheckedChangeListener(this);
        mTrackAnim = AnimationUtils.loadAnimation(mContext, R.anim.quickcontact);
        mTrackAnim.setInterpolator(new Interpolator() {
            public float getInterpolation(float t) {
                final float inner = (t * 1.55f) - 1.1f;
                return 1.2f - inner * inner;
            }
        });
        mHandler = new NotifyingAsyncQueryHandler(mContext, this);
    }
    public QuickContactWindow(Context context, OnDismissListener dismissListener) {
        this(context);
        mDismissListener = dismissListener;
    }
    private View getHeaderView(int mode) {
        View header = null;
        switch (mode) {
            case QuickContact.MODE_SMALL:
                header = mWindow.findViewById(R.id.header_small);
                break;
            case QuickContact.MODE_MEDIUM:
                header = mWindow.findViewById(R.id.header_medium);
                break;
            case QuickContact.MODE_LARGE:
                header = mWindow.findViewById(R.id.header_large);
                break;
        }
        if (header instanceof ViewStub) {
            final ViewStub stub = (ViewStub)header;
            header = stub.inflate();
        } else if (header != null) {
            header.setVisibility(View.VISIBLE);
        }
        return header;
    }
    public synchronized void show(Uri lookupUri, Rect anchor, int mode, String[] excludeMimes) {
        if (mQuerying || mShowing) {
            Log.w(TAG, "dismissing before showing");
            dismissInternal();
        }
        if (TRACE_LAUNCH && !android.os.Debug.isMethodTracingActive()) {
            android.os.Debug.startMethodTracing(TRACE_TAG);
        }
        final boolean validMode = (mode == QuickContact.MODE_SMALL
                || mode == QuickContact.MODE_MEDIUM || mode == QuickContact.MODE_LARGE);
        if (!validMode) {
            throw new IllegalArgumentException("Invalid mode, expecting MODE_LARGE, "
                    + "MODE_MEDIUM, or MODE_SMALL");
        }
        if (anchor == null) {
            throw new IllegalArgumentException("Missing anchor rectangle");
        }
        mLookupUri = lookupUri;
        mAnchor = new Rect(anchor);
        mMode = mode;
        mExcludeMimes = excludeMimes;
        mHeader = getHeaderView(mode);
        setHeaderText(R.id.name, R.string.quickcontact_missing_name);
        setHeaderText(R.id.status, null);
        setHeaderText(R.id.timestamp, null);
        setHeaderImage(R.id.presence, null);
        resetTrack();
        mRootView.requestFocus();
        mHasValidSocial = false;
        mDismissed = false;
        mQuerying = true;
        final Uri dataUri = getDataUri(lookupUri);
        mHandler.cancelOperation(TOKEN_DATA);
        if (mMode == QuickContact.MODE_LARGE) {
            mHandler.startQuery(TOKEN_DATA, lookupUri, dataUri, DataQuery.PROJECTION, Data.MIMETYPE
                    + "!=? OR (" + Data.MIMETYPE + "=? AND " + Data._ID + "=" + Contacts.PHOTO_ID
                    + ")", new String[] { Photo.CONTENT_ITEM_TYPE, Photo.CONTENT_ITEM_TYPE }, null);
        } else {
            mHandler.startQuery(TOKEN_DATA, lookupUri, dataUri, DataQuery.PROJECTION, Data.MIMETYPE
                    + "!=?", new String[] { Photo.CONTENT_ITEM_TYPE }, null);
        }
    }
    private Uri getDataUri(Uri lookupUri) {
        final List<String> path = lookupUri.getPathSegments();
        final boolean validLookup = path.size() >= 3 && "lookup".equals(path.get(1));
        if (!validLookup) {
            throw new IllegalArgumentException("Expecting lookup-style Uri");
        } else if (path.size() == 3) {
            lookupUri = Contacts.lookupContact(mContext.getContentResolver(), lookupUri);
        }
        final long contactId = ContentUris.parseId(lookupUri);
        return Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                Contacts.Data.CONTENT_DIRECTORY);
    }
    private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;
        final int arrowWidth = mArrowUp.getMeasuredWidth();
        showArrow.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
        param.leftMargin = requestedX - arrowWidth / 2;
        hideArrow.setVisibility(View.INVISIBLE);
    }
    private void showInternal() {
        mDecor = mWindow.getDecorView();
        mDecor.getViewTreeObserver().addOnGlobalLayoutListener(this);
        WindowManager.LayoutParams l = mWindow.getAttributes();
        l.width = mScreenWidth + mShadowHoriz + mShadowHoriz;
        l.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mDecor.measure(l.width, l.height);
        final int blockHeight = mDecor.getMeasuredHeight();
        l.gravity = Gravity.TOP | Gravity.LEFT;
        l.x = -mShadowHoriz;
        if (mAnchor.top > blockHeight) {
            showArrow(R.id.arrow_down, mAnchor.centerX());
            l.y = mAnchor.top - blockHeight + mShadowVert;
            l.windowAnimations = R.style.QuickContactAboveAnimation;
        } else {
            showArrow(R.id.arrow_up, mAnchor.centerX());
            l.y = mAnchor.bottom - mShadowVert;
            l.windowAnimations = R.style.QuickContactBelowAnimation;
        }
        l.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mRequestedY = l.y;
        mWindowManager.addView(mDecor, l);
        mShowing = true;
        mQuerying = false;
        mDismissed = false;
        mTrack.startAnimation(mTrackAnim);
        if (TRACE_LAUNCH) {
            android.os.Debug.stopMethodTracing();
            Log.d(TAG, "Window recycled " + mWindowRecycled + " times, chiclets "
                    + mActionRecycled + " times");
        }
    }
    public void onGlobalLayout() {
        layoutInScreen();
    }
    private void layoutInScreen() {
        if (!mShowing) return;
        final WindowManager.LayoutParams l = mWindow.getAttributes();
        final int originalY = l.y;
        final int blockHeight = mDecor.getHeight();
        l.y = mRequestedY;
        if (mRequestedY + blockHeight > mScreenHeight) {
            l.y = mScreenHeight - blockHeight;
        }
        if (originalY != l.y) {
            mWindow.setAttributes(l);
        }
    }
    public synchronized void dismiss() {
        if (mDismissListener != null) {
            mDismissListener.onDismiss(this);
        }
        dismissInternal();
    }
    private void dismissInternal() {
        boolean hadDecor = mDecor != null;
        if (hadDecor) {
            mWindowManager.removeView(mDecor);
            mWindowRecycled++;
            mDecor.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            mDecor = null;
            mWindow.closeAllPanels();
        }
        mShowing = false;
        mDismissed = true;
        mHandler.cancelOperation(TOKEN_DATA);
        mQuerying = false;
        mHeader.setVisibility(View.GONE);
        resetTrack();
    }
    private void resetTrack() {
        mLastAction = null;
        mResolveCache.clear();
        mActions.clear();
        while (mTrack.getChildCount() > 2) {
            this.releaseView(mTrack.getChildAt(1));
            mTrack.removeViewAt(1);
        }
        mTrackScroll.fullScroll(View.FOCUS_LEFT);
        mWasDownArrow = false;
        mMakePrimary = false;
        mSetPrimaryCheckBox.setChecked(false);
        setResolveVisible(false, null);
    }
    private void considerShowing() {
        if (mHasData && !mShowing && !mDismissed) {
            if (mMode == QuickContact.MODE_MEDIUM && !mHasValidSocial) {
                mHeader.setVisibility(View.GONE);
                mHeader = getHeaderView(QuickContact.MODE_SMALL);
            }
            showInternal();
        }
    }
    public synchronized void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (cookie != mLookupUri) return;
        if (cursor == null) {
            Log.w(TAG, "Missing cursor for token=" + token);
            this.dismiss();
            return;
        }
        handleData(cursor);
        mHasData = true;
        if (!cursor.isClosed()) {
            cursor.close();
        }
        considerShowing();
    }
    private void setHeaderText(int id, int resId) {
        setHeaderText(id, mContext.getResources().getText(resId));
    }
    private void setHeaderText(int id, CharSequence value) {
        final View view = mHeader.findViewById(id);
        if (view instanceof TextView) {
            ((TextView)view).setText(value);
            view.setVisibility(TextUtils.isEmpty(value) ? View.GONE : View.VISIBLE);
        }
    }
    private void setHeaderImage(int id, Drawable drawable) {
        final View view = mHeader.findViewById(id);
        if (view instanceof ImageView) {
            ((ImageView)view).setImageDrawable(drawable);
            view.setVisibility(drawable == null ? View.GONE : View.VISIBLE);
        }
    }
    private Drawable getTrackPresenceIcon(int status) {
        int resId;
        switch (status) {
            case StatusUpdates.AVAILABLE:
                resId = R.drawable.quickcontact_slider_presence_active;
                break;
            case StatusUpdates.IDLE:
            case StatusUpdates.AWAY:
                resId = R.drawable.quickcontact_slider_presence_away;
                break;
            case StatusUpdates.DO_NOT_DISTURB:
                resId = R.drawable.quickcontact_slider_presence_busy;
                break;
            case StatusUpdates.INVISIBLE:
                resId = R.drawable.quickcontact_slider_presence_inactive;
                break;
            case StatusUpdates.OFFLINE:
            default:
                resId = R.drawable.quickcontact_slider_presence_inactive;
        }
        return mContext.getResources().getDrawable(resId);
    }
    private static String getAsString(Cursor cursor, String columnName) {
        final int index = cursor.getColumnIndex(columnName);
        return cursor.getString(index);
    }
    private static int getAsInt(Cursor cursor, String columnName) {
        final int index = cursor.getColumnIndex(columnName);
        return cursor.getInt(index);
    }
    private interface Action extends Collapser.Collapsible<Action> {
        public CharSequence getHeader();
        public CharSequence getBody();
        public String getMimeType();
        public Drawable getFallbackIcon();
        public Intent getIntent();
        public Boolean isPrimary();
        public Uri getDataUri();
    }
    private static class DataAction implements Action {
        private final Context mContext;
        private final DataKind mKind;
        private final String mMimeType;
        private CharSequence mHeader;
        private CharSequence mBody;
        private Intent mIntent;
        private boolean mAlternate;
        private Uri mDataUri;
        private boolean mIsPrimary;
        public DataAction(Context context, String mimeType, DataKind kind,
                long dataId, Cursor cursor) {
            mContext = context;
            mKind = kind;
            mMimeType = mimeType;
            mAlternate = Constants.MIME_SMS_ADDRESS.equals(mimeType);
            if (mAlternate && mKind.actionAltHeader != null) {
                mHeader = mKind.actionAltHeader.inflateUsing(context, cursor);
            } else if (mKind.actionHeader != null) {
                mHeader = mKind.actionHeader.inflateUsing(context, cursor);
            }
            if (getAsInt(cursor, Data.IS_SUPER_PRIMARY) != 0) {
                mIsPrimary = true;
            }
            if (mKind.actionBody != null) {
                mBody = mKind.actionBody.inflateUsing(context, cursor);
            }
            mDataUri = ContentUris.withAppendedId(Data.CONTENT_URI, dataId);
            if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final String number = getAsString(cursor, Phone.NUMBER);
                if (!TextUtils.isEmpty(number)) {
                    final Uri callUri = Uri.fromParts(Constants.SCHEME_TEL, number, null);
                    mIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, callUri);
                }
            } else if (Constants.MIME_SMS_ADDRESS.equals(mimeType)) {
                final String number = getAsString(cursor, Phone.NUMBER);
                if (!TextUtils.isEmpty(number)) {
                    final Uri smsUri = Uri.fromParts(Constants.SCHEME_SMSTO, number, null);
                    mIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
                }
            } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final String address = getAsString(cursor, Email.DATA);
                if (!TextUtils.isEmpty(address)) {
                    final Uri mailUri = Uri.fromParts(Constants.SCHEME_MAILTO, address, null);
                    mIntent = new Intent(Intent.ACTION_SENDTO, mailUri);
                }
            } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final String url = getAsString(cursor, Website.URL);
                if (!TextUtils.isEmpty(url)) {
                    mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                }
            } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final boolean isEmail = Email.CONTENT_ITEM_TYPE.equals(
                        getAsString(cursor, Data.MIMETYPE));
                if (isEmail || isProtocolValid(cursor)) {
                    final int protocol = isEmail ? Im.PROTOCOL_GOOGLE_TALK :
                            getAsInt(cursor, Im.PROTOCOL);
                    if (isEmail) {
                        mHeader = context.getText(R.string.chat_gtalk);
                        mDataUri = null;
                    }
                    String host = getAsString(cursor, Im.CUSTOM_PROTOCOL);
                    String data = getAsString(cursor, isEmail ? Email.DATA : Im.DATA);
                    if (protocol != Im.PROTOCOL_CUSTOM) {
                        host = ContactsUtils.lookupProviderNameFromId(protocol);
                    }
                    if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(data)) {
                        final String authority = host.toLowerCase();
                        final Uri imUri = new Uri.Builder().scheme(Constants.SCHEME_IMTO).authority(
                                authority).appendPath(data).build();
                        mIntent = new Intent(Intent.ACTION_SENDTO, imUri);
                    }
                }
            }
            if (mIntent == null) {
                mIntent = new Intent(Intent.ACTION_VIEW, mDataUri);
            }
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        private boolean isProtocolValid(Cursor cursor) {
            final int columnIndex = cursor.getColumnIndex(Im.PROTOCOL);
            if (cursor.isNull(columnIndex)) {
                return false;
            }
            try {
                Integer.valueOf(cursor.getString(columnIndex));
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        public CharSequence getHeader() {
            return mHeader;
        }
        public CharSequence getBody() {
            return mBody;
        }
        public String getMimeType() {
            return mMimeType;
        }
        public Uri getDataUri() {
            return mDataUri;
        }
        public Boolean isPrimary() {
            return mIsPrimary;
        }
        public Drawable getFallbackIcon() {
            final String resPackageName = mKind.resPackageName;
            if (resPackageName == null) return null;
            final PackageManager pm = mContext.getPackageManager();
            if (mAlternate && mKind.iconAltRes != -1) {
                return pm.getDrawable(resPackageName, mKind.iconAltRes, null);
            } else if (mKind.iconRes != -1) {
                return pm.getDrawable(resPackageName, mKind.iconRes, null);
            } else {
                return null;
            }
        }
        public Intent getIntent() {
            return mIntent;
        }
        public boolean collapseWith(Action other) {
            if (!shouldCollapseWith(other)) {
                return false;
            }
            return true;
        }
        public boolean shouldCollapseWith(Action t) {
            if (t == null) {
                return false;
            }
            if (!(t instanceof DataAction)) {
                Log.e(TAG, "t must be DataAction");
                return false;
            }
            DataAction other = (DataAction)t;
            if (!ContactsUtils.areObjectsEqual(mKind, other.mKind)) {
                return false;
            }
            if (!ContactsUtils.shouldCollapse(mContext, mMimeType, mBody, other.mMimeType,
                    other.mBody)) {
                return false;
            }
            if (!TextUtils.equals(mMimeType, other.mMimeType)
                    || !ContactsUtils.areIntentActionEqual(mIntent, other.mIntent)
                    ) {
                return false;
            }
            return true;
        }
    }
    private static class ProfileAction implements Action {
        private final Context mContext;
        private final Uri mLookupUri;
        public ProfileAction(Context context, Uri lookupUri) {
            mContext = context;
            mLookupUri = lookupUri;
        }
        public CharSequence getHeader() {
            return null;
        }
        public CharSequence getBody() {
            return null;
        }
        public String getMimeType() {
            return Contacts.CONTENT_ITEM_TYPE;
        }
        public Drawable getFallbackIcon() {
            return mContext.getResources().getDrawable(R.drawable.ic_contacts_details);
        }
        public Intent getIntent() {
            final Intent intent = new Intent(Intent.ACTION_VIEW, mLookupUri);
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    return intent;
        }
        public Boolean isPrimary() {
            return null;
        }
        public Uri getDataUri() {
            return null;
        }
        public boolean collapseWith(Action t) {
            return false; 
        }
        public boolean shouldCollapseWith(Action t) {
            return false; 
        }
    }
    private static class ResolveCache {
        private PackageManager mPackageManager;
        private static class Entry {
            public ResolveInfo bestResolve;
            public SoftReference<Drawable> icon;
        }
        private HashMap<String, Entry> mCache = new HashMap<String, Entry>();
        public ResolveCache(Context context) {
            mPackageManager = context.getPackageManager();
        }
        protected Entry getEntry(Action action) {
            final String mimeType = action.getMimeType();
            Entry entry = mCache.get(mimeType);
            if (entry != null) return entry;
            entry = new Entry();
            final Intent intent = action.getIntent();
            if (intent != null) {
                final List<ResolveInfo> matches = mPackageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                ResolveInfo bestResolve = null;
                final int size = matches.size();
                if (size == 1) {
                    bestResolve = matches.get(0);
                } else if (size > 1) {
                    bestResolve = getBestResolve(intent, matches);
                }
                if (bestResolve != null) {
                    final Drawable icon = bestResolve.loadIcon(mPackageManager);
                    entry.bestResolve = bestResolve;
                    entry.icon = new SoftReference<Drawable>(icon);
                }
            }
            mCache.put(mimeType, entry);
            return entry;
        }
        protected ResolveInfo getBestResolve(Intent intent, List<ResolveInfo> matches) {
            final ResolveInfo foundResolve = mPackageManager.resolveActivity(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            final boolean foundDisambig = (foundResolve.match &
                    IntentFilter.MATCH_CATEGORY_MASK) == 0;
            if (!foundDisambig) {
                return foundResolve;
            }
            ResolveInfo firstSystem = null;
            for (ResolveInfo info : matches) {
                final boolean isSystem = (info.activityInfo.applicationInfo.flags
                        & ApplicationInfo.FLAG_SYSTEM) != 0;
                final boolean isPrefer = QuickContactWindow.sPreferResolve
                        .contains(info.activityInfo.applicationInfo.packageName);
                if (isPrefer) return info;
                if (isSystem && firstSystem != null) firstSystem = info;
            }
            return firstSystem != null ? firstSystem : matches.get(0);
        }
        public boolean hasResolve(Action action) {
            return getEntry(action).bestResolve != null;
        }
        public CharSequence getDescription(Action action) {
            final CharSequence actionHeader = action.getHeader();
            final ResolveInfo info = getEntry(action).bestResolve;
            if (!TextUtils.isEmpty(actionHeader)) {
                return actionHeader;
            } else if (info != null) {
                return info.loadLabel(mPackageManager);
            } else {
                return null;
            }
        }
        public Drawable getIcon(Action action) {
            final SoftReference<Drawable> iconRef = getEntry(action).icon;
            return (iconRef == null) ? null : iconRef.get();
        }
        public void clear() {
            mCache.clear();
        }
    }
    private class ActionList extends ArrayList<Action> {
    }
    private class ActionMap extends HashMap<String, ActionList> {
        private void collect(String mimeType, Action info) {
            ActionList collectList = get(mimeType);
            if (collectList == null) {
                collectList = new ActionList();
                put(mimeType, collectList);
            }
            collectList.add(info);
        }
    }
    private boolean isMimeExcluded(String mimeType) {
        if (mExcludeMimes == null) return false;
        for (String excludedMime : mExcludeMimes) {
            if (TextUtils.equals(excludedMime, mimeType)) {
                return true;
            }
        }
        return false;
    }
    private void handleData(Cursor cursor) {
        if (cursor == null) return;
        if (!isMimeExcluded(Contacts.CONTENT_ITEM_TYPE)) {
            final Action action = new ProfileAction(mContext, mLookupUri);
            mActions.collect(Contacts.CONTENT_ITEM_TYPE, action);
        }
        final DataStatus status = new DataStatus();
        final Sources sources = Sources.getInstance(mContext);
        final ImageView photoView = (ImageView)mHeader.findViewById(R.id.photo);
        Bitmap photoBitmap = null;
        while (cursor.moveToNext()) {
            final long dataId = cursor.getLong(DataQuery._ID);
            final String accountType = cursor.getString(DataQuery.ACCOUNT_TYPE);
            final String mimeType = cursor.getString(DataQuery.MIMETYPE);
            status.possibleUpdate(cursor);
            if (isMimeExcluded(mimeType)) continue;
            if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final int colPhoto = cursor.getColumnIndex(Photo.PHOTO);
                final byte[] photoBlob = cursor.getBlob(colPhoto);
                if (photoBlob != null) {
                    photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
                }
                continue;
            }
            final DataKind kind = sources.getKindOrFallback(accountType, mimeType, mContext,
                    ContactsSource.LEVEL_MIMETYPES);
            if (kind != null) {
                final Action action = new DataAction(mContext, mimeType, kind, dataId, cursor);
                considerAdd(action, mimeType);
            }
            if (Phone.CONTENT_ITEM_TYPE.equals(mimeType) && kind != null) {
                final Action action = new DataAction(mContext, Constants.MIME_SMS_ADDRESS,
                        kind, dataId, cursor);
                considerAdd(action, Constants.MIME_SMS_ADDRESS);
            }
            final boolean hasPresence = !cursor.isNull(DataQuery.PRESENCE);
            if (hasPresence && Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final DataKind imKind = sources.getKindOrFallback(accountType,
                        Im.CONTENT_ITEM_TYPE, mContext, ContactsSource.LEVEL_MIMETYPES);
                if (imKind != null) {
                    final Action action = new DataAction(mContext, Im.CONTENT_ITEM_TYPE, imKind,
                            dataId, cursor);
                    considerAdd(action, Im.CONTENT_ITEM_TYPE);
                }
            }
        }
        if (cursor.moveToLast()) {
            final String name = cursor.getString(DataQuery.DISPLAY_NAME);
            final int presence = cursor.getInt(DataQuery.CONTACT_PRESENCE);
            final Drawable statusIcon = ContactPresenceIconUtil.getPresenceIcon(mContext, presence);
            setHeaderText(R.id.name, name);
            setHeaderImage(R.id.presence, statusIcon);
        }
        if (photoView != null) {
            photoView.setVisibility(photoBitmap != null ? View.VISIBLE : View.GONE);
            photoView.setImageBitmap(photoBitmap);
        }
        mHasValidSocial = status.isValid();
        if (mHasValidSocial && mMode != QuickContact.MODE_SMALL) {
            setHeaderText(R.id.status, status.getStatus());
            setHeaderText(R.id.timestamp, status.getTimestampLabel(mContext));
        }
        int index = mTrack.getChildCount() - 1;
        final Set<String> containedTypes = new HashSet<String>(mActions.keySet());
        for (String mimeType : PRECEDING_MIMETYPES) {
            if (containedTypes.contains(mimeType)) {
                mTrack.addView(inflateAction(mimeType), index++);
                containedTypes.remove(mimeType);
            }
        }
        final int indexAfterPreceding = index;
        for (String mimeType : FOLLOWING_MIMETYPES) {
            if (containedTypes.contains(mimeType)) {
                mTrack.addView(inflateAction(mimeType), index++);
                containedTypes.remove(mimeType);
            }
        }
        index = indexAfterPreceding;
        final String[] remainingTypes = containedTypes.toArray(new String[containedTypes.size()]);
        Arrays.sort(remainingTypes);
        for (String mimeType : remainingTypes) {
            mTrack.addView(inflateAction(mimeType), index++);
        }
    }
    private void considerAdd(Action action, String mimeType) {
        if (mResolveCache.hasResolve(action)) {
            mActions.collect(mimeType, action);
        }
    }
    private synchronized View obtainView() {
        View view = mActionPool.poll();
        if (view == null || QuickContactActivity.FORCE_CREATE) {
            view = mInflater.inflate(R.layout.quickcontact_item, mTrack, false);
        }
        return view;
    }
    private synchronized void releaseView(View view) {
        mActionPool.offer(view);
        mActionRecycled++;
    }
    private View inflateAction(String mimeType) {
        final CheckableImageView view = (CheckableImageView)obtainView();
        boolean isActionSet = false;
        ActionList children = mActions.get(mimeType);
        if (children.size() > 1) {
            Collapser.collapseList(children);
        }
        Action firstInfo = children.get(0);
        if (children.size() == 1) {
            view.setTag(firstInfo);
        } else {
            for (Action action : children) {
                if (action.isPrimary()) {
                    view.setTag(action);
                    isActionSet = true;
                    break;
                }
            }
            if (!isActionSet) {
                view.setTag(children);
            }
        }
        final CharSequence descrip = mResolveCache.getDescription(firstInfo);
        final Drawable icon = mResolveCache.getIcon(firstInfo);
        view.setChecked(false);
        view.setContentDescription(descrip);
        view.setImageDrawable(icon);
        view.setOnClickListener(this);
        return view;
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onClick(view);
    }
    private boolean mWasDownArrow = false;
    private void setResolveVisible(boolean visible, CheckableImageView actionView) {
        boolean visibleNow = mFooterDisambig.getVisibility() == View.VISIBLE;
        if (mLastAction != null) mLastAction.setChecked(false);
        if (actionView != null) actionView.setChecked(true);
        mLastAction = actionView;
        if (visible == visibleNow) return;
        mFooter.setVisibility(visible ? View.GONE : View.VISIBLE);
        mFooterDisambig.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            mWasDownArrow = mWasDownArrow || (mArrowDown.getVisibility() == View.VISIBLE);
            mArrowDown.setVisibility(View.INVISIBLE);
        } else {
            mArrowDown.setVisibility(mWasDownArrow ? View.VISIBLE : View.INVISIBLE);
        }
    }
    public void onClick(View view) {
        final boolean isActionView = (view instanceof CheckableImageView);
        final CheckableImageView actionView = isActionView ? (CheckableImageView)view : null;
        final Object tag = view.getTag();
        if (tag instanceof Action) {
            final Action action = (Action)tag;
            final boolean makePrimary = mMakePrimary;
            try {
                mContext.startActivity(action.getIntent());
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, R.string.quickcontact_missing_app, Toast.LENGTH_SHORT)
                        .show();
            }
            setResolveVisible(false, null);
            this.dismiss();
            if (makePrimary) {
                ContentValues values = new ContentValues(1);
                values.put(Data.IS_SUPER_PRIMARY, 1);
                final Uri dataUri = action.getDataUri();
                if (dataUri != null) {
                    mContext.getContentResolver().update(dataUri, values, null, null);
                }
            }
        } else if (tag instanceof ActionList) {
            final ActionList children = (ActionList)tag;
            setResolveVisible(true, actionView);
            mResolveList.setOnItemClickListener(this);
            mResolveList.setAdapter(new BaseAdapter() {
                public int getCount() {
                    return children.size();
                }
                public Object getItem(int position) {
                    return children.get(position);
                }
                public long getItemId(int position) {
                    return position;
                }
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = mInflater.inflate(
                                R.layout.quickcontact_resolve_item, parent, false);
                    }
                    final Action action = (Action)getItem(position);
                    TextView text1 = (TextView)convertView.findViewById(android.R.id.text1);
                    TextView text2 = (TextView)convertView.findViewById(android.R.id.text2);
                    text1.setText(action.getHeader());
                    text2.setText(action.getBody());
                    convertView.setTag(action);
                    return convertView;
                }
            });
            mDecor.forceLayout();
            mDecor.invalidate();
        }
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mMakePrimary = isChecked;
    }
    private void onBackPressed() {
        if (mFooterDisambig.getVisibility() == View.VISIBLE) {
            setResolveVisible(false, null);
            mDecor.forceLayout();
            mDecor.invalidate();
        } else {
            dismiss();
        }
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mWindow.superDispatchKeyEvent(event)) {
            return true;
        }
        return event.dispatch(this, mDecor != null
                ? mDecor.getKeyDispatcherState() : null, this);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            event.startTracking();
            return true;
        }
        return false;
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            onBackPressed();
            return true;
        }
        return false;
    }
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return false;
    }
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }
    protected void detectEventOutside(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && mDecor != null) {
            mDecor.getHitRect(mRect);
            mRect.top = mRect.top + mShadowTouch;
            mRect.bottom = mRect.bottom - mShadowTouch;
            final int x = (int)event.getX();
            final int y = (int)event.getY();
            if (!mRect.contains(x, y)) {
                event.setAction(MotionEvent.ACTION_OUTSIDE);
            }
        }
    }
    public boolean dispatchTouchEvent(MotionEvent event) {
        detectEventOutside(event);
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            dismiss();
            return true;
        } else {
            return mWindow.superDispatchTouchEvent(event);
        }
    }
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return mWindow.superDispatchTrackballEvent(event);
    }
    public void onContentChanged() {
    }
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return false;
    }
    public View onCreatePanelView(int featureId) {
        return null;
    }
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
    public void onPanelClosed(int featureId, Menu menu) {
    }
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return false;
    }
    public boolean onSearchRequested() {
        return false;
    }
    public void onWindowAttributesChanged(android.view.WindowManager.LayoutParams attrs) {
        if (mDecor != null) {
            mWindowManager.updateViewLayout(mDecor, attrs);
        }
    }
    public void onWindowFocusChanged(boolean hasFocus) {
    }
    public void onAttachedToWindow() {
    }
    public void onDetachedFromWindow() {
    }
    private interface DataQuery {
        final String[] PROJECTION = new String[] {
                Data._ID,
                RawContacts.ACCOUNT_TYPE,
                Contacts.STARRED,
                Contacts.DISPLAY_NAME,
                Contacts.CONTACT_PRESENCE,
                Data.STATUS,
                Data.STATUS_RES_PACKAGE,
                Data.STATUS_ICON,
                Data.STATUS_LABEL,
                Data.STATUS_TIMESTAMP,
                Data.PRESENCE,
                Data.RES_PACKAGE,
                Data.MIMETYPE,
                Data.IS_PRIMARY,
                Data.IS_SUPER_PRIMARY,
                Data.RAW_CONTACT_ID,
                Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5,
                Data.DATA6, Data.DATA7, Data.DATA8, Data.DATA9, Data.DATA10, Data.DATA11,
                Data.DATA12, Data.DATA13, Data.DATA14, Data.DATA15,
        };
        final int _ID = 0;
        final int ACCOUNT_TYPE = 1;
        final int STARRED = 2;
        final int DISPLAY_NAME = 3;
        final int CONTACT_PRESENCE = 4;
        final int STATUS = 5;
        final int STATUS_RES_PACKAGE = 6;
        final int STATUS_ICON = 7;
        final int STATUS_LABEL = 8;
        final int STATUS_TIMESTAMP = 9;
        final int PRESENCE = 10;
        final int RES_PACKAGE = 11;
        final int MIMETYPE = 12;
        final int IS_PRIMARY = 13;
        final int IS_SUPER_PRIMARY = 14;
    }
}
