public class Resources {
    static final String TAG = "Resources";
    private static final boolean DEBUG_LOAD = false;
    private static final boolean DEBUG_CONFIG = false;
    private static final boolean TRACE_FOR_PRELOAD = false;
    private static final int sSdkVersion = Build.VERSION.SDK_INT
            + ("REL".equals(Build.VERSION.CODENAME) ? 0 : 1);
    private static final Object mSync = new Object();
    private static Resources mSystem = null;
    private static final LongSparseArray<Drawable.ConstantState> sPreloadedDrawables
            = new LongSparseArray<Drawable.ConstantState>();
    private static final SparseArray<ColorStateList> mPreloadedColorStateLists
            = new SparseArray<ColorStateList>();
    private static boolean mPreloaded;
     final TypedValue mTmpValue = new TypedValue();
    private final LongSparseArray<WeakReference<Drawable.ConstantState> > mDrawableCache
            = new LongSparseArray<WeakReference<Drawable.ConstantState> >();
    private final SparseArray<WeakReference<ColorStateList> > mColorStateListCache
            = new SparseArray<WeakReference<ColorStateList> >();
    private boolean mPreloading;
     TypedArray mCachedStyledAttributes = null;
    private int mLastCachedXmlBlockIndex = -1;
    private final int[] mCachedXmlBlockIds = { 0, 0, 0, 0 };
    private final XmlBlock[] mCachedXmlBlocks = new XmlBlock[4];
     final AssetManager mAssets;
    private final Configuration mConfiguration = new Configuration();
     final DisplayMetrics mMetrics = new DisplayMetrics();
    PluralRules mPluralRule;
    private CompatibilityInfo mCompatibilityInfo;
    private Display mDefaultDisplay;
    private static final LongSparseArray<Object> EMPTY_ARRAY = new LongSparseArray<Object>() {
        @Override
        public void put(long k, Object o) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void append(long k, Object o) {
            throw new UnsupportedOperationException();
        }
    };
    @SuppressWarnings("unchecked")
    private static <T> LongSparseArray<T> emptySparseArray() {
        return (LongSparseArray<T>) EMPTY_ARRAY;
    }
    public static class NotFoundException extends RuntimeException {
        public NotFoundException() {
        }
        public NotFoundException(String name) {
            super(name);
        }
    }
    public Resources(AssetManager assets, DisplayMetrics metrics,
            Configuration config) {
        this(assets, metrics, config, (CompatibilityInfo) null);
    }
    public Resources(AssetManager assets, DisplayMetrics metrics,
            Configuration config, CompatibilityInfo compInfo) {
        mAssets = assets;
        mMetrics.setToDefaults();
        if (compInfo == null) {
            mCompatibilityInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        } else {
            mCompatibilityInfo = compInfo;
        }
        updateConfiguration(config, metrics);
        assets.ensureStringBlocks();
    }
    public static Resources getSystem() {
        synchronized (mSync) {
            Resources ret = mSystem;
            if (ret == null) {
                ret = new Resources();
                mSystem = ret;
            }
            return ret;
        }
    }
    public CharSequence getText(int id) throws NotFoundException {
        CharSequence res = mAssets.getResourceText(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("String resource ID #0x"
                                    + Integer.toHexString(id));
    }
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        PluralRules rule = getPluralRule();
        CharSequence res = mAssets.getResourceBagText(id, rule.attrForNumber(quantity));
        if (res != null) {
            return res;
        }
        res = mAssets.getResourceBagText(id, PluralRules.ID_OTHER);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Plural resource ID #0x" + Integer.toHexString(id)
                + " quantity=" + quantity
                + " item=" + PluralRules.stringForQuantity(rule.quantityForNumber(quantity)));
    }
    private PluralRules getPluralRule() {
        synchronized (mSync) {
            if (mPluralRule == null) {
                mPluralRule = PluralRules.ruleForLocale(mConfiguration.locale);
            }
            return mPluralRule;
        }
    }
    public String getString(int id) throws NotFoundException {
        CharSequence res = getText(id);
        if (res != null) {
            return res.toString();
        }
        throw new NotFoundException("String resource ID #0x"
                                    + Integer.toHexString(id));
    }
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        String raw = getString(id);
        return String.format(mConfiguration.locale, raw, formatArgs);
    }
    public String getQuantityString(int id, int quantity, Object... formatArgs)
            throws NotFoundException {
        String raw = getQuantityText(id, quantity).toString();
        return String.format(mConfiguration.locale, raw, formatArgs);
    }
    public String getQuantityString(int id, int quantity) throws NotFoundException {
        return getQuantityText(id, quantity).toString();
    }
    public CharSequence getText(int id, CharSequence def) {
        CharSequence res = id != 0 ? mAssets.getResourceText(id) : null;
        return res != null ? res : def;
    }
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        CharSequence[] res = mAssets.getResourceTextArray(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Text array resource ID #0x"
                                    + Integer.toHexString(id));
    }
    public String[] getStringArray(int id) throws NotFoundException {
        String[] res = mAssets.getResourceStringArray(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("String array resource ID #0x"
                                    + Integer.toHexString(id));
    }
    public int[] getIntArray(int id) throws NotFoundException {
        int[] res = mAssets.getArrayIntResource(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Int array resource ID #0x"
                                    + Integer.toHexString(id));
    }
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        int len = mAssets.getArraySize(id);
        if (len < 0) {
            throw new NotFoundException("Array resource ID #0x"
                                        + Integer.toHexString(id));
        }
        TypedArray array = getCachedStyledAttributes(len);
        array.mLength = mAssets.retrieveArray(id, array.mData);
        array.mIndices[0] = 0;
        return array;
    }
    public float getDimension(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            if (value.type == TypedValue.TYPE_DIMENSION) {
                return TypedValue.complexToDimension(value.data, mMetrics);
            }
            throw new NotFoundException(
                    "Resource ID #0x" + Integer.toHexString(id) + " type #0x"
                    + Integer.toHexString(value.type) + " is not valid");
        }
    }
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            if (value.type == TypedValue.TYPE_DIMENSION) {
                return TypedValue.complexToDimensionPixelOffset(
                        value.data, mMetrics);
            }
            throw new NotFoundException(
                    "Resource ID #0x" + Integer.toHexString(id) + " type #0x"
                    + Integer.toHexString(value.type) + " is not valid");
        }
    }
    public int getDimensionPixelSize(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            if (value.type == TypedValue.TYPE_DIMENSION) {
                return TypedValue.complexToDimensionPixelSize(
                        value.data, mMetrics);
            }
            throw new NotFoundException(
                    "Resource ID #0x" + Integer.toHexString(id) + " type #0x"
                    + Integer.toHexString(value.type) + " is not valid");
        }
    }
    public float getFraction(int id, int base, int pbase) {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            if (value.type == TypedValue.TYPE_FRACTION) {
                return TypedValue.complexToFraction(value.data, base, pbase);
            }
            throw new NotFoundException(
                    "Resource ID #0x" + Integer.toHexString(id) + " type #0x"
                    + Integer.toHexString(value.type) + " is not valid");
        }
    }
    public Drawable getDrawable(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            return loadDrawable(value, id);
        }
    }
    public Movie getMovie(int id) throws NotFoundException {
        InputStream is = openRawResource(id);
        Movie movie = Movie.decodeStream(is);
        try {
            is.close();
        }
        catch (java.io.IOException e) {
        }
        return movie;
    }
    public int getColor(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            if (value.type >= TypedValue.TYPE_FIRST_INT
                && value.type <= TypedValue.TYPE_LAST_INT) {
                return value.data;
            } else if (value.type == TypedValue.TYPE_STRING) {
                ColorStateList csl = loadColorStateList(mTmpValue, id);
                return csl.getDefaultColor();
            }
            throw new NotFoundException(
                "Resource ID #0x" + Integer.toHexString(id) + " type #0x"
                + Integer.toHexString(value.type) + " is not valid");
        }
    }
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            return loadColorStateList(value, id);
        }
    }
    public boolean getBoolean(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            if (value.type >= TypedValue.TYPE_FIRST_INT
                && value.type <= TypedValue.TYPE_LAST_INT) {
                return value.data != 0;
            }
            throw new NotFoundException(
                "Resource ID #0x" + Integer.toHexString(id) + " type #0x"
                + Integer.toHexString(value.type) + " is not valid");
        }
    }
    public int getInteger(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            if (value.type >= TypedValue.TYPE_FIRST_INT
                && value.type <= TypedValue.TYPE_LAST_INT) {
                return value.data;
            }
            throw new NotFoundException(
                "Resource ID #0x" + Integer.toHexString(id) + " type #0x"
                + Integer.toHexString(value.type) + " is not valid");
        }
    }
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        return loadXmlResourceParser(id, "layout");
    }
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        return loadXmlResourceParser(id, "anim");
    }
    public XmlResourceParser getXml(int id) throws NotFoundException {
        return loadXmlResourceParser(id, "xml");
    }
    public InputStream openRawResource(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            return openRawResource(id, mTmpValue);
        }
    }
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        getValue(id, value, true);
        try {
            return mAssets.openNonAsset(value.assetCookie, value.string.toString(),
                    AssetManager.ACCESS_STREAMING);
        } catch (Exception e) {
            NotFoundException rnf = new NotFoundException("File " + value.string.toString() +
                    " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        }
    }
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            try {
                return mAssets.openNonAssetFd(
                    value.assetCookie, value.string.toString());
            } catch (Exception e) {
                NotFoundException rnf = new NotFoundException(
                    "File " + value.string.toString()
                    + " from drawable resource ID #0x"
                    + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
    }
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        boolean found = mAssets.getResourceValue(id, outValue, resolveRefs);
        if (found) {
            return;
        }
        throw new NotFoundException("Resource ID #0x"
                                    + Integer.toHexString(id));
    }
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        int id = getIdentifier(name, "string", null);
        if (id != 0) {
            getValue(id, outValue, resolveRefs);
            return;
        }
        throw new NotFoundException("String resource name " + name);
    }
    public final class Theme {
        public void applyStyle(int resid, boolean force) {
            AssetManager.applyThemeStyle(mTheme, resid, force);
        }
        public void setTo(Theme other) {
            AssetManager.copyTheme(mTheme, other.mTheme);
        }
        public TypedArray obtainStyledAttributes(int[] attrs) {
            int len = attrs.length;
            TypedArray array = getCachedStyledAttributes(len);
            array.mRsrcs = attrs;
            AssetManager.applyStyle(mTheme, 0, 0, 0, attrs,
                    array.mData, array.mIndices);
            return array;
        }
        public TypedArray obtainStyledAttributes(int resid, int[] attrs)
                throws NotFoundException {
            int len = attrs.length;
            TypedArray array = getCachedStyledAttributes(len);
            array.mRsrcs = attrs;
            AssetManager.applyStyle(mTheme, 0, resid, 0, attrs,
                    array.mData, array.mIndices);
            if (false) {
                int[] data = array.mData;
                System.out.println("**********************************************************");
                System.out.println("**********************************************************");
                System.out.println("**********************************************************");
                System.out.println("Attributes:");
                String s = "  Attrs:";
                int i;
                for (i=0; i<attrs.length; i++) {
                    s = s + " 0x" + Integer.toHexString(attrs[i]);
                }
                System.out.println(s);
                s = "  Found:";
                TypedValue value = new TypedValue();
                for (i=0; i<attrs.length; i++) {
                    int d = i*AssetManager.STYLE_NUM_ENTRIES;
                    value.type = data[d+AssetManager.STYLE_TYPE];
                    value.data = data[d+AssetManager.STYLE_DATA];
                    value.assetCookie = data[d+AssetManager.STYLE_ASSET_COOKIE];
                    value.resourceId = data[d+AssetManager.STYLE_RESOURCE_ID];
                    s = s + " 0x" + Integer.toHexString(attrs[i])
                        + "=" + value;
                }
                System.out.println(s);
            }
            return array;
        }
        public TypedArray obtainStyledAttributes(AttributeSet set,
                int[] attrs, int defStyleAttr, int defStyleRes) {
            int len = attrs.length;
            TypedArray array = getCachedStyledAttributes(len);
            XmlBlock.Parser parser = (XmlBlock.Parser)set;
            AssetManager.applyStyle(
                mTheme, defStyleAttr, defStyleRes,
                parser != null ? parser.mParseState : 0, attrs,
                        array.mData, array.mIndices);
            array.mRsrcs = attrs;
            array.mXml = parser;
            if (false) {
                int[] data = array.mData;
                System.out.println("Attributes:");
                String s = "  Attrs:";
                int i;
                for (i=0; i<set.getAttributeCount(); i++) {
                    s = s + " " + set.getAttributeName(i);
                    int id = set.getAttributeNameResource(i);
                    if (id != 0) {
                        s = s + "(0x" + Integer.toHexString(id) + ")";
                    }
                    s = s + "=" + set.getAttributeValue(i);
                }
                System.out.println(s);
                s = "  Found:";
                TypedValue value = new TypedValue();
                for (i=0; i<attrs.length; i++) {
                    int d = i*AssetManager.STYLE_NUM_ENTRIES;
                    value.type = data[d+AssetManager.STYLE_TYPE];
                    value.data = data[d+AssetManager.STYLE_DATA];
                    value.assetCookie = data[d+AssetManager.STYLE_ASSET_COOKIE];
                    value.resourceId = data[d+AssetManager.STYLE_RESOURCE_ID];
                    s = s + " 0x" + Integer.toHexString(attrs[i])
                        + "=" + value;
                }
                System.out.println(s);
            }
            return array;
        }
        public boolean resolveAttribute(int resid, TypedValue outValue,
                boolean resolveRefs) {
            boolean got = mAssets.getThemeValue(mTheme, resid, outValue, resolveRefs);
            if (false) {
                System.out.println(
                    "resolveAttribute #" + Integer.toHexString(resid)
                    + " got=" + got + ", type=0x" + Integer.toHexString(outValue.type)
                    + ", data=0x" + Integer.toHexString(outValue.data));
            }
            return got;
        }
        public void dump(int priority, String tag, String prefix) {
            AssetManager.dumpTheme(mTheme, priority, tag, prefix);
        }
        protected void finalize() throws Throwable {
            super.finalize();
            mAssets.releaseTheme(mTheme);
        }
         Theme() {
            mAssets = Resources.this.mAssets;
            mTheme = mAssets.createTheme();
        }
        private final AssetManager mAssets;
        private final int mTheme;
    }
    public final Theme newTheme() {
        return new Theme();
    }
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        int len = attrs.length;
        TypedArray array = getCachedStyledAttributes(len);
        XmlBlock.Parser parser = (XmlBlock.Parser)set;
        mAssets.retrieveAttributes(parser.mParseState, attrs,
                array.mData, array.mIndices);
        array.mRsrcs = attrs;
        array.mXml = parser;
        return array;
    }
    public void updateConfiguration(Configuration config,
            DisplayMetrics metrics) {
        synchronized (mTmpValue) {
            int configChanges = 0xfffffff;
            if (config != null) {
                configChanges = mConfiguration.updateFrom(config);
            }
            if (mConfiguration.locale == null) {
                mConfiguration.locale = Locale.getDefault();
            }
            if (metrics != null) {
                mMetrics.setTo(metrics);
                mMetrics.updateMetrics(mCompatibilityInfo,
                        mConfiguration.orientation, mConfiguration.screenLayout);
            }
            mMetrics.scaledDensity = mMetrics.density * mConfiguration.fontScale;
            String locale = null;
            if (mConfiguration.locale != null) {
                locale = mConfiguration.locale.getLanguage();
                if (mConfiguration.locale.getCountry() != null) {
                    locale += "-" + mConfiguration.locale.getCountry();
                }
            }
            int width, height;
            if (mMetrics.widthPixels >= mMetrics.heightPixels) {
                width = mMetrics.widthPixels;
                height = mMetrics.heightPixels;
            } else {
                width = mMetrics.heightPixels;
                height = mMetrics.widthPixels;
            }
            int keyboardHidden = mConfiguration.keyboardHidden;
            if (keyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
                    && mConfiguration.hardKeyboardHidden
                            == Configuration.HARDKEYBOARDHIDDEN_YES) {
                keyboardHidden = Configuration.KEYBOARDHIDDEN_SOFT;
            }
            mAssets.setConfiguration(mConfiguration.mcc, mConfiguration.mnc,
                    locale, mConfiguration.orientation,
                    mConfiguration.touchscreen,
                    (int)(mMetrics.density*160), mConfiguration.keyboard,
                    keyboardHidden, mConfiguration.navigation, width, height,
                    mConfiguration.screenLayout, mConfiguration.uiMode, sSdkVersion);
            int N = mDrawableCache.size();
            if (DEBUG_CONFIG) {
                Log.d(TAG, "Cleaning up drawables config changes: 0x"
                        + Integer.toHexString(configChanges));
            }
            for (int i=0; i<N; i++) {
                WeakReference<Drawable.ConstantState> ref = mDrawableCache.valueAt(i);
                if (ref != null) {
                    Drawable.ConstantState cs = ref.get();
                    if (cs != null) {
                        if (Configuration.needNewResources(
                                configChanges, cs.getChangingConfigurations())) {
                            if (DEBUG_CONFIG) {
                                Log.d(TAG, "FLUSHING #0x"
                                        + Long.toHexString(mDrawableCache.keyAt(i))
                                        + " / " + cs + " with changes: 0x"
                                        + Integer.toHexString(cs.getChangingConfigurations()));
                            }
                            mDrawableCache.setValueAt(i, null);
                        } else if (DEBUG_CONFIG) {
                            Log.d(TAG, "(Keeping #0x"
                                    + Long.toHexString(mDrawableCache.keyAt(i))
                                    + " / " + cs + " with changes: 0x"
                                    + Integer.toHexString(cs.getChangingConfigurations())
                                    + ")");
                        }
                    }
                }
            }
            mDrawableCache.clear();
            mColorStateListCache.clear();
            flushLayoutCache();
        }
        synchronized (mSync) {
            if (mPluralRule != null) {
                mPluralRule = PluralRules.ruleForLocale(config.locale);
            }
        }
    }
    public static void updateSystemConfiguration(Configuration config, DisplayMetrics metrics) {
        if (mSystem != null) {
            mSystem.updateConfiguration(config, metrics);
        }
    }
    public DisplayMetrics getDisplayMetrics() {
        return mMetrics;
    }
    public Configuration getConfiguration() {
        return mConfiguration;
    }
    public CompatibilityInfo getCompatibilityInfo() {
        return mCompatibilityInfo;
    }
    public void setCompatibilityInfo(CompatibilityInfo ci) {
        mCompatibilityInfo = ci;
        updateConfiguration(mConfiguration, mMetrics);
    }
    public int getIdentifier(String name, String defType, String defPackage) {
        try {
            return Integer.parseInt(name);
        } catch (Exception e) {
        }
        return mAssets.getResourceIdentifier(name, defType, defPackage);
    }
    public String getResourceName(int resid) throws NotFoundException {
        String str = mAssets.getResourceName(resid);
        if (str != null) return str;
        throw new NotFoundException("Unable to find resource ID #0x"
                + Integer.toHexString(resid));
    }
    public String getResourcePackageName(int resid) throws NotFoundException {
        String str = mAssets.getResourcePackageName(resid);
        if (str != null) return str;
        throw new NotFoundException("Unable to find resource ID #0x"
                + Integer.toHexString(resid));
    }
    public String getResourceTypeName(int resid) throws NotFoundException {
        String str = mAssets.getResourceTypeName(resid);
        if (str != null) return str;
        throw new NotFoundException("Unable to find resource ID #0x"
                + Integer.toHexString(resid));
    }
    public String getResourceEntryName(int resid) throws NotFoundException {
        String str = mAssets.getResourceEntryName(resid);
        if (str != null) return str;
        throw new NotFoundException("Unable to find resource ID #0x"
                + Integer.toHexString(resid));
    }
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
            throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
               && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }
            String nodeName = parser.getName();
            if (nodeName.equals("extra")) {
                parseBundleExtra("extra", parser, outBundle);
                XmlUtils.skipCurrentTag(parser);
            } else {
                XmlUtils.skipCurrentTag(parser);
            }
        }        
    }
    public void parseBundleExtra(String tagName, AttributeSet attrs,
            Bundle outBundle) throws XmlPullParserException {
        TypedArray sa = obtainAttributes(attrs,
                com.android.internal.R.styleable.Extra);
        String name = sa.getString(
                com.android.internal.R.styleable.Extra_name);
        if (name == null) {
            sa.recycle();
            throw new XmlPullParserException("<" + tagName
                    + "> requires an android:name attribute at "
                    + attrs.getPositionDescription());
        }
        TypedValue v = sa.peekValue(
                com.android.internal.R.styleable.Extra_value);
        if (v != null) {
            if (v.type == TypedValue.TYPE_STRING) {
                CharSequence cs = v.coerceToString();
                outBundle.putCharSequence(name, cs);
            } else if (v.type == TypedValue.TYPE_INT_BOOLEAN) {
                outBundle.putBoolean(name, v.data != 0);
            } else if (v.type >= TypedValue.TYPE_FIRST_INT
                    && v.type <= TypedValue.TYPE_LAST_INT) {
                outBundle.putInt(name, v.data);
            } else if (v.type == TypedValue.TYPE_FLOAT) {
                outBundle.putFloat(name, v.getFloat());
            } else {
                sa.recycle();
                throw new XmlPullParserException("<" + tagName
                        + "> only supports string, integer, float, color, and boolean at "
                        + attrs.getPositionDescription());
            }
        } else {
            sa.recycle();
            throw new XmlPullParserException("<" + tagName
                    + "> requires an android:value or android:resource attribute at "
                    + attrs.getPositionDescription());
        }
        sa.recycle();
    }
    public final AssetManager getAssets() {
        return mAssets;
    }
    public final void flushLayoutCache() {
        synchronized (mCachedXmlBlockIds) {
            final int num = mCachedXmlBlockIds.length;
            for (int i=0; i<num; i++) {
                mCachedXmlBlockIds[i] = -0;
                XmlBlock oldBlock = mCachedXmlBlocks[i];
                if (oldBlock != null) {
                    oldBlock.close();
                }
                mCachedXmlBlocks[i] = null;
            }
        }
    }
    public final void startPreloading() {
        synchronized (mSync) {
            if (mPreloaded) {
                throw new IllegalStateException("Resources already preloaded");
            }
            mPreloaded = true;
            mPreloading = true;
        }
    }
    public final void finishPreloading() {
        if (mPreloading) {
            mPreloading = false;
            flushLayoutCache();
        }
    }
     Drawable loadDrawable(TypedValue value, int id)
            throws NotFoundException {
        if (TRACE_FOR_PRELOAD) {
            if ((id >>> 24) == 0x1) {
                final String name = getResourceName(id);
                if (name != null) android.util.Log.d("PreloadDrawable", name);
            }
        }
        final long key = (((long) value.assetCookie) << 32) | value.data;
        Drawable dr = getCachedDrawable(key);
        if (dr != null) {
            return dr;
        }
        Drawable.ConstantState cs = sPreloadedDrawables.get(key);
        if (cs != null) {
            dr = cs.newDrawable(this);
        } else {
            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                    value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                dr = new ColorDrawable(value.data);
            }
            if (dr == null) {
                if (value.string == null) {
                    throw new NotFoundException(
                            "Resource is not a Drawable (color or path): " + value);
                }
                String file = value.string.toString();
                if (DEBUG_LOAD) Log.v(TAG, "Loading drawable for cookie "
                        + value.assetCookie + ": " + file);
                if (file.endsWith(".xml")) {
                    try {
                        XmlResourceParser rp = loadXmlResourceParser(
                                file, id, value.assetCookie, "drawable");
                        dr = Drawable.createFromXml(this, rp);
                        rp.close();
                    } catch (Exception e) {
                        NotFoundException rnf = new NotFoundException(
                            "File " + file + " from drawable resource ID #0x"
                            + Integer.toHexString(id));
                        rnf.initCause(e);
                        throw rnf;
                    }
                } else {
                    try {
                        InputStream is = mAssets.openNonAsset(
                                value.assetCookie, file, AssetManager.ACCESS_STREAMING);
                        dr = Drawable.createFromResourceStream(this, value, is,
                                file, null);
                        is.close();
                    } catch (Exception e) {
                        NotFoundException rnf = new NotFoundException(
                            "File " + file + " from drawable resource ID #0x"
                            + Integer.toHexString(id));
                        rnf.initCause(e);
                        throw rnf;
                    }
                }
            }
        }
        if (dr != null) {
            dr.setChangingConfigurations(value.changingConfigurations);
            cs = dr.getConstantState();
            if (cs != null) {
                if (mPreloading) {
                    sPreloadedDrawables.put(key, cs);
                } else {
                    synchronized (mTmpValue) {
                        mDrawableCache.put(key, new WeakReference<Drawable.ConstantState>(cs));
                    }
                }
            }
        }
        return dr;
    }
    private Drawable getCachedDrawable(long key) {
        synchronized (mTmpValue) {
            WeakReference<Drawable.ConstantState> wr = mDrawableCache.get(key);
            if (wr != null) {   
                Drawable.ConstantState entry = wr.get();
                if (entry != null) {
                    return entry.newDrawable(this);
                }
                else {  
                    mDrawableCache.delete(key);
                }
            }
        }
        return null;
    }
     ColorStateList loadColorStateList(TypedValue value, int id)
            throws NotFoundException {
        if (TRACE_FOR_PRELOAD) {
            if ((id >>> 24) == 0x1) {
                final String name = getResourceName(id);
                if (name != null) android.util.Log.d("PreloadColorStateList", name);
            }
        }
        final int key = (value.assetCookie << 24) | value.data;
        ColorStateList csl;
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            csl = mPreloadedColorStateLists.get(key);
            if (csl != null) {
                return csl;
            }
            csl = ColorStateList.valueOf(value.data);
            if (mPreloading) {
                mPreloadedColorStateLists.put(key, csl);
            }
            return csl;
        }
        csl = getCachedColorStateList(key);
        if (csl != null) {
            return csl;
        }
        csl = mPreloadedColorStateLists.get(key);
        if (csl != null) {
            return csl;
        }
        if (value.string == null) {
            throw new NotFoundException(
                    "Resource is not a ColorStateList (color or path): " + value);
        }
        String file = value.string.toString();
        if (file.endsWith(".xml")) {
            try {
                XmlResourceParser rp = loadXmlResourceParser(
                        file, id, value.assetCookie, "colorstatelist"); 
                csl = ColorStateList.createFromXml(this, rp);
                rp.close();
            } catch (Exception e) {
                NotFoundException rnf = new NotFoundException(
                    "File " + file + " from color state list resource ID #0x"
                    + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        } else {
            throw new NotFoundException(
                    "File " + file + " from drawable resource ID #0x"
                    + Integer.toHexString(id) + ": .xml extension required");
        }
        if (csl != null) {
            if (mPreloading) {
                mPreloadedColorStateLists.put(key, csl);
            } else {
                synchronized (mTmpValue) {
                    mColorStateListCache.put(
                        key, new WeakReference<ColorStateList>(csl));
                }
            }
        }
        return csl;
    }
    private ColorStateList getCachedColorStateList(int key) {
        synchronized (mTmpValue) {
            WeakReference<ColorStateList> wr = mColorStateListCache.get(key);
            if (wr != null) {   
                ColorStateList entry = wr.get();
                if (entry != null) {
                    return entry;
                }
                else {  
                    mColorStateListCache.delete(key);
                }
            }
        }
        return null;
    }
     XmlResourceParser loadXmlResourceParser(int id, String type)
            throws NotFoundException {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValue(id, value, true);
            if (value.type == TypedValue.TYPE_STRING) {
                return loadXmlResourceParser(value.string.toString(), id,
                        value.assetCookie, type);
            }
            throw new NotFoundException(
                    "Resource ID #0x" + Integer.toHexString(id) + " type #0x"
                    + Integer.toHexString(value.type) + " is not valid");
        }
    }
     XmlResourceParser loadXmlResourceParser(String file, int id,
            int assetCookie, String type) throws NotFoundException {
        if (id != 0) {
            try {
                synchronized (mCachedXmlBlockIds) {
                    final int num = mCachedXmlBlockIds.length;
                    for (int i=0; i<num; i++) {
                        if (mCachedXmlBlockIds[i] == id) {
                            return mCachedXmlBlocks[i].newParser();
                        }
                    }
                    XmlBlock block = mAssets.openXmlBlockAsset(
                            assetCookie, file);
                    if (block != null) {
                        int pos = mLastCachedXmlBlockIndex+1;
                        if (pos >= num) pos = 0;
                        mLastCachedXmlBlockIndex = pos;
                        XmlBlock oldBlock = mCachedXmlBlocks[pos];
                        if (oldBlock != null) {
                            oldBlock.close();
                        }
                        mCachedXmlBlockIds[pos] = id;
                        mCachedXmlBlocks[pos] = block;
                        return block.newParser();
                    }
                }
            } catch (Exception e) {
                NotFoundException rnf = new NotFoundException(
                        "File " + file + " from xml type " + type + " resource ID #0x"
                        + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new NotFoundException(
                "File " + file + " from xml type " + type + " resource ID #0x"
                + Integer.toHexString(id));
    }
    public Display getDefaultDisplay(Display defaultDisplay) {
        if (mDefaultDisplay == null) {
            if (!mCompatibilityInfo.isScalingRequired() && mCompatibilityInfo.supportsScreen()) {
                mDefaultDisplay = defaultDisplay;
            } else {
                mDefaultDisplay = Display.createMetricsBasedDisplay(
                        defaultDisplay.getDisplayId(), mMetrics);
            }
        }
        return mDefaultDisplay;
    }
    private TypedArray getCachedStyledAttributes(int len) {
        synchronized (mTmpValue) {
            TypedArray attrs = mCachedStyledAttributes;
            if (attrs != null) {
                mCachedStyledAttributes = null;
                attrs.mLength = len;
                int fullLen = len * AssetManager.STYLE_NUM_ENTRIES;
                if (attrs.mData.length >= fullLen) {
                    return attrs;
                }
                attrs.mData = new int[fullLen];
                attrs.mIndices = new int[1+len];
                return attrs;
            }
            return new TypedArray(this,
                    new int[len*AssetManager.STYLE_NUM_ENTRIES],
                    new int[1+len], len);
        }
    }
    private Resources() {
        mAssets = AssetManager.getSystem();
        mConfiguration.setToDefaults();
        mMetrics.setToDefaults();
        updateConfiguration(null, null);
        mAssets.ensureStringBlocks();
        mCompatibilityInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
    }
}
