class SuggestionsAdapter extends ResourceCursorAdapter {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "SuggestionsAdapter";
    private static final int QUERY_LIMIT = 50;
    private SearchManager mSearchManager;
    private SearchDialog mSearchDialog;
    private SearchableInfo mSearchable;
    private Context mProviderContext;
    private WeakHashMap<String, Drawable.ConstantState> mOutsideDrawablesCache;
    private SparseArray<Drawable.ConstantState> mBackgroundsCache;
    private boolean mClosed = false;
    private ColorStateList mUrlColor;
    private int mText1Col;
    private int mText2Col;
    private int mText2UrlCol;
    private int mIconName1Col;
    private int mIconName2Col;
    private int mBackgroundColorCol;
    static final int NONE = -1;
    private final Runnable mStartSpinnerRunnable;
    private final Runnable mStopSpinnerRunnable;
    private static final long DELETE_KEY_POST_DELAY = 500L;
    public SuggestionsAdapter(Context context, SearchDialog searchDialog,
            SearchableInfo searchable,
            WeakHashMap<String, Drawable.ConstantState> outsideDrawablesCache) {
        super(context,
                com.android.internal.R.layout.search_dropdown_item_icons_2line,
                null,   
                true);  
        mSearchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        mSearchDialog = searchDialog;
        mSearchable = searchable;
        Context activityContext = mSearchable.getActivityContext(mContext);
        mProviderContext = mSearchable.getProviderContext(mContext, activityContext);
        mOutsideDrawablesCache = outsideDrawablesCache;
        mBackgroundsCache = new SparseArray<Drawable.ConstantState>();
        mStartSpinnerRunnable = new Runnable() {
                public void run() {
                    mSearchDialog.setWorking(true);
                }
            };
        mStopSpinnerRunnable = new Runnable() {
            public void run() {
                mSearchDialog.setWorking(false);
            }
        };
        getFilter().setDelayer(new Filter.Delayer() {
            private int mPreviousLength = 0;
            public long getPostingDelay(CharSequence constraint) {
                if (constraint == null) return 0;
                long delay = constraint.length() < mPreviousLength ? DELETE_KEY_POST_DELAY : 0;
                mPreviousLength = constraint.length();
                return delay;
            }
        });
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (DBG) Log.d(LOG_TAG, "runQueryOnBackgroundThread(" + constraint + ")");
        String query = (constraint == null) ? "" : constraint.toString();
        Cursor cursor = null;
        mSearchDialog.getWindow().getDecorView().post(mStartSpinnerRunnable);
        try {
            cursor = mSearchManager.getSuggestions(mSearchable, query, QUERY_LIMIT);
            if (cursor != null) {
                cursor.getCount();
                return cursor;
            }
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "Search suggestions query threw an exception.", e);
        }
        mSearchDialog.getWindow().getDecorView().post(mStopSpinnerRunnable);
        return null;
    }
    public void close() {
        if (DBG) Log.d(LOG_TAG, "close()");
        changeCursor(null);
        mClosed = true;
    }
    @Override
    public void notifyDataSetChanged() {
        if (DBG) Log.d(LOG_TAG, "notifyDataSetChanged");
        super.notifyDataSetChanged();
        mSearchDialog.onDataSetChanged();
        updateSpinnerState(getCursor());
    }
    @Override
    public void notifyDataSetInvalidated() {
        if (DBG) Log.d(LOG_TAG, "notifyDataSetInvalidated");
        super.notifyDataSetInvalidated();
        updateSpinnerState(getCursor());
    }
    private void updateSpinnerState(Cursor cursor) {
        Bundle extras = cursor != null ? cursor.getExtras() : null;
        if (DBG) {
            Log.d(LOG_TAG, "updateSpinnerState - extra = "
                + (extras != null
                        ? extras.getBoolean(SearchManager.CURSOR_EXTRA_KEY_IN_PROGRESS)
                        : null));
        }
        if (extras != null
                && extras.getBoolean(SearchManager.CURSOR_EXTRA_KEY_IN_PROGRESS)) {
            mSearchDialog.getWindow().getDecorView().post(mStartSpinnerRunnable);
            return;
        }
        mSearchDialog.getWindow().getDecorView().post(mStopSpinnerRunnable);
    }
    @Override
    public void changeCursor(Cursor c) {
        if (DBG) Log.d(LOG_TAG, "changeCursor(" + c + ")");
        if (mClosed) {
            Log.w(LOG_TAG, "Tried to change cursor after adapter was closed.");
            if (c != null) c.close();
            return;
        }
        try {
            super.changeCursor(c);
            if (c != null) {
                mText1Col = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
                mText2Col = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2);
                mText2UrlCol = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2_URL);
                mIconName1Col = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1);
                mIconName2Col = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_2);
                mBackgroundColorCol =
                        c.getColumnIndex(SearchManager.SUGGEST_COLUMN_BACKGROUND_COLOR);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "error changing cursor and caching columns", e);
        }
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = super.newView(context, cursor, parent);
        v.setTag(new ChildViewCache(v));
        return v;
    }
    private final static class ChildViewCache {
        public final TextView mText1;
        public final TextView mText2;
        public final ImageView mIcon1;
        public final ImageView mIcon2;
        public ChildViewCache(View v) {
            mText1 = (TextView) v.findViewById(com.android.internal.R.id.text1);
            mText2 = (TextView) v.findViewById(com.android.internal.R.id.text2);
            mIcon1 = (ImageView) v.findViewById(com.android.internal.R.id.icon1);
            mIcon2 = (ImageView) v.findViewById(com.android.internal.R.id.icon2);
        }
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ChildViewCache views = (ChildViewCache) view.getTag();
        int backgroundColor = 0;
        if (mBackgroundColorCol != -1) {
            backgroundColor = cursor.getInt(mBackgroundColorCol);
        }
        Drawable background = getItemBackground(backgroundColor);
        view.setBackgroundDrawable(background);
        if (views.mText1 != null) {
            String text1 = getStringOrNull(cursor, mText1Col);
            setViewText(views.mText1, text1);
        }
        if (views.mText2 != null) {
            CharSequence text2 = getStringOrNull(cursor, mText2UrlCol);
            if (text2 != null) {
                text2 = formatUrl(text2);
            } else {
                text2 = getStringOrNull(cursor, mText2Col);
            }
            if (TextUtils.isEmpty(text2)) {
                if (views.mText1 != null) {
                    views.mText1.setSingleLine(false);
                    views.mText1.setMaxLines(2);
                }
            } else {
                if (views.mText1 != null) {
                    views.mText1.setSingleLine(true);
                    views.mText1.setMaxLines(1);
                }
            }
            setViewText(views.mText2, text2);
        }
        if (views.mIcon1 != null) {
            setViewDrawable(views.mIcon1, getIcon1(cursor));
        }
        if (views.mIcon2 != null) {
            setViewDrawable(views.mIcon2, getIcon2(cursor));
        }
    }
    private CharSequence formatUrl(CharSequence url) {
        if (mUrlColor == null) {
            TypedValue colorValue = new TypedValue();
            mContext.getTheme().resolveAttribute(R.attr.textColorSearchUrl, colorValue, true);
            mUrlColor = mContext.getResources().getColorStateList(colorValue.resourceId);
        }
        SpannableString text = new SpannableString(url);
        text.setSpan(new TextAppearanceSpan(null, 0, 0, mUrlColor, null),
                0, url.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }
    private Drawable getItemBackground(int backgroundColor) {
        if (backgroundColor == 0) {
            return null;
        } else {
            Drawable.ConstantState cachedBg = mBackgroundsCache.get(backgroundColor);
            if (cachedBg != null) {
                if (DBG) Log.d(LOG_TAG, "Background cache hit for color " + backgroundColor);
                return cachedBg.newDrawable(mProviderContext.getResources());
            }
            if (DBG) Log.d(LOG_TAG, "Creating new background for color " + backgroundColor);
            ColorDrawable transparent = new ColorDrawable(0);
            ColorDrawable background = new ColorDrawable(backgroundColor);
            StateListDrawable newBg = new StateListDrawable();
            newBg.addState(new int[]{android.R.attr.state_selected}, transparent);
            newBg.addState(new int[]{android.R.attr.state_pressed}, transparent);
            newBg.addState(new int[]{}, background);
            mBackgroundsCache.put(backgroundColor, newBg.getConstantState());
            return newBg;
        }
    }
    private void setViewText(TextView v, CharSequence text) {
        v.setText(text);
        if (TextUtils.isEmpty(text)) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }
    private Drawable getIcon1(Cursor cursor) {
        if (mIconName1Col < 0) {
            return null;
        }
        String value = cursor.getString(mIconName1Col);
        Drawable drawable = getDrawableFromResourceValue(value);
        if (drawable != null) {
            return drawable;
        }
        return getDefaultIcon1(cursor);
    }
    private Drawable getIcon2(Cursor cursor) {
        if (mIconName2Col < 0) {
            return null;
        }
        String value = cursor.getString(mIconName2Col);
        return getDrawableFromResourceValue(value);
    }
    private void setViewDrawable(ImageView v, Drawable drawable) {
        v.setImageDrawable(drawable);
        if (drawable == null) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
            drawable.setVisible(false, false);
            drawable.setVisible(true, false);
        }
    }
    @Override
    public CharSequence convertToString(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        String query = getColumnString(cursor, SearchManager.SUGGEST_COLUMN_QUERY);
        if (query != null) {
            return query;
        }
        if (mSearchable.shouldRewriteQueryFromData()) {
            String data = getColumnString(cursor, SearchManager.SUGGEST_COLUMN_INTENT_DATA);
            if (data != null) {
                return data;
            }
        }
        if (mSearchable.shouldRewriteQueryFromText()) {
            String text1 = getColumnString(cursor, SearchManager.SUGGEST_COLUMN_TEXT_1);
            if (text1 != null) {
                return text1;
            }
        }
        return null;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            return super.getView(position, convertView, parent);
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "Search suggestions cursor threw exception.", e);
            View v = newView(mContext, mCursor, parent);
            if (v != null) {
                ChildViewCache views = (ChildViewCache) v.getTag();
                TextView tv = views.mText1;
                tv.setText(e.toString());
            }
            return v;
        }
    }
    private Drawable getDrawableFromResourceValue(String drawableId) {
        if (drawableId == null || drawableId.length() == 0 || "0".equals(drawableId)) {
            return null;
        }
        try {
            int resourceId = Integer.parseInt(drawableId);
            String drawableUri = ContentResolver.SCHEME_ANDROID_RESOURCE
                    + ":
            Drawable drawable = checkIconCache(drawableUri);
            if (drawable != null) {
                return drawable;
            }
            drawable = mProviderContext.getResources().getDrawable(resourceId);
            storeInIconCache(drawableUri, drawable);
            return drawable;
        } catch (NumberFormatException nfe) {
            Drawable drawable = checkIconCache(drawableId);
            if (drawable != null) {
                return drawable;
            }
            Uri uri = Uri.parse(drawableId);
            drawable = getDrawable(uri);
            storeInIconCache(drawableId, drawable);
            return drawable;
        } catch (Resources.NotFoundException nfe) {
            Log.w(LOG_TAG, "Icon resource not found: " + drawableId);
            return null;
        }
    }
    private Drawable getDrawable(Uri uri) {
        try {
            String scheme = uri.getScheme();
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                OpenResourceIdResult r =
                    mProviderContext.getContentResolver().getResourceId(uri);
                try {
                    return r.r.getDrawable(r.id);
                } catch (Resources.NotFoundException ex) {
                    throw new FileNotFoundException("Resource does not exist: " + uri);
                }
            } else {
                InputStream stream = mProviderContext.getContentResolver().openInputStream(uri);
                if (stream == null) {
                    throw new FileNotFoundException("Failed to open " + uri);
                }
                try {
                    return Drawable.createFromStream(stream, null);
                } finally {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, "Error closing icon stream for " + uri, ex);
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            Log.w(LOG_TAG, "Icon not found: " + uri + ", " + fnfe.getMessage());
            return null;
        }
    }
    private Drawable checkIconCache(String resourceUri) {
        Drawable.ConstantState cached = mOutsideDrawablesCache.get(resourceUri);
        if (cached == null) {
            return null;
        }
        if (DBG) Log.d(LOG_TAG, "Found icon in cache: " + resourceUri);
        return cached.newDrawable();
    }
    private void storeInIconCache(String resourceUri, Drawable drawable) {
        if (drawable != null) {
            mOutsideDrawablesCache.put(resourceUri, drawable.getConstantState());
        }
    }
    private Drawable getDefaultIcon1(Cursor cursor) {
        String c = getColumnString(cursor, SearchManager.SUGGEST_COLUMN_INTENT_COMPONENT_NAME);
        if (c != null) {
            ComponentName component = ComponentName.unflattenFromString(c);
            if (component != null) {
                Drawable drawable = getActivityIconWithCache(component);
                if (drawable != null) {
                    return drawable;
                }
            } else {
                Log.w(LOG_TAG, "Bad component name: " + c);
            }
        }
        Drawable drawable = getActivityIconWithCache(mSearchable.getSearchActivity());
        if (drawable != null) {
            return drawable;
        }
        return mContext.getPackageManager().getDefaultActivityIcon();
    }
    private Drawable getActivityIconWithCache(ComponentName component) {
        String componentIconKey = component.flattenToShortString();
        if (mOutsideDrawablesCache.containsKey(componentIconKey)) {
            Drawable.ConstantState cached = mOutsideDrawablesCache.get(componentIconKey);
            return cached == null ? null : cached.newDrawable(mProviderContext.getResources());
        }
        Drawable drawable = getActivityIcon(component);
        Drawable.ConstantState toCache = drawable == null ? null : drawable.getConstantState();
        mOutsideDrawablesCache.put(componentIconKey, toCache);
        return drawable;
    }
    private Drawable getActivityIcon(ComponentName component) {
        PackageManager pm = mContext.getPackageManager();
        final ActivityInfo activityInfo;
        try {
            activityInfo = pm.getActivityInfo(component, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException ex) {
            Log.w(LOG_TAG, ex.toString());
            return null;
        }
        int iconId = activityInfo.getIconResource();
        if (iconId == 0) return null;
        String pkg = component.getPackageName();
        Drawable drawable = pm.getDrawable(pkg, iconId, activityInfo.applicationInfo);
        if (drawable == null) {
            Log.w(LOG_TAG, "Invalid icon resource " + iconId + " for "
                    + component.flattenToShortString());
            return null;
        }
        return drawable;
    }
    public static String getColumnString(Cursor cursor, String columnName) {
        int col = cursor.getColumnIndex(columnName);
        return getStringOrNull(cursor, col);
    }
    private static String getStringOrNull(Cursor cursor, int col) {
        if (col == NONE) {
            return null;
        }
        try {
            return cursor.getString(col);
        } catch (Exception e) {
            Log.e(LOG_TAG,
                    "unexpected error retrieving valid column from cursor, "
                            + "did the remote process die?", e);
            return null;
        }
    }
}
