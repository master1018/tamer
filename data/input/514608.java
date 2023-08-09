public final class BridgeContext extends Context {
    private final Resources mResources;
    private final Theme mTheme;
    private final HashMap<View, Object> mViewKeyMap = new HashMap<View, Object>();
    private final IStyleResourceValue mThemeValues;
    private final Object mProjectKey;
    private final Map<String, Map<String, IResourceValue>> mProjectResources;
    private final Map<String, Map<String, IResourceValue>> mFrameworkResources;
    private final Map<IStyleResourceValue, IStyleResourceValue> mStyleInheritanceMap;
    private Map<Integer, IStyleResourceValue> mDynamicIdToStyleMap;
    private Map<IStyleResourceValue, Integer> mStyleToDynamicIdMap;
    private int mDynamicIdGenerator = 0x01030000; 
    private Map<int[], Map<Integer, TypedArray>> mTypedArrayCache;
    private BridgeInflater mInflater;
    private final IProjectCallback mProjectCallback;
    private final ILayoutLog mLogger;
    private BridgeContentResolver mContentResolver;
    public BridgeContext(Object projectKey, DisplayMetrics metrics,
            IStyleResourceValue currentTheme,
            Map<String, Map<String, IResourceValue>> projectResources,
            Map<String, Map<String, IResourceValue>> frameworkResources,
            Map<IStyleResourceValue, IStyleResourceValue> styleInheritanceMap,
            IProjectCallback customViewLoader, ILayoutLog logger) {
        mProjectKey = projectKey;
        mProjectCallback = customViewLoader;
        mLogger = logger;
        Configuration config = new Configuration();
        AssetManager assetManager = BridgeAssetManager.initSystem();
        mResources = BridgeResources.initSystem(
                this,
                assetManager,
                metrics,
                config,
                customViewLoader);
        mTheme = mResources.newTheme();
        mThemeValues = currentTheme;
        mProjectResources = projectResources;
        mFrameworkResources = frameworkResources;
        mStyleInheritanceMap = styleInheritanceMap;
    }
    public void setBridgeInflater(BridgeInflater inflater) {
        mInflater = inflater;
    }
    public void addViewKey(View view, Object viewKey) {
        mViewKeyMap.put(view, viewKey);
    }
    public Object getViewKey(View view) {
        return mViewKeyMap.get(view);
    }
    public Object getProjectKey() {
        return mProjectKey;
    }
    public IProjectCallback getProjectCallback() {
        return mProjectCallback;
    }
    public ILayoutLog getLogger() {
        return mLogger;
    }
    @Override
    public Resources getResources() {
        return mResources;
    }
    @Override
    public Theme getTheme() {
        return mTheme;
    }
    @Override
    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }
    @Override
    public Object getSystemService(String service) {
        if (LAYOUT_INFLATER_SERVICE.equals(service)) {
            return mInflater;
        }
        if (WINDOW_SERVICE.equals(service)) {
            return null;
        }
        throw new UnsupportedOperationException("Unsupported Service: " + service);
    }
    @Override
    public final TypedArray obtainStyledAttributes(int[] attrs) {
        return createStyleBasedTypedArray(mThemeValues, attrs);
    }
    @Override
    public final TypedArray obtainStyledAttributes(int resid, int[] attrs)
            throws Resources.NotFoundException {
        IStyleResourceValue style = getStyleByDynamicId(resid);
        if (style == null) {
            throw new Resources.NotFoundException();
        }
        if (mTypedArrayCache == null) {
            mTypedArrayCache = new HashMap<int[], Map<Integer,TypedArray>>();
            Map<Integer, TypedArray> map = new HashMap<Integer, TypedArray>();
            mTypedArrayCache.put(attrs, map);
            BridgeTypedArray ta = createStyleBasedTypedArray(style, attrs);
            map.put(resid, ta);
            return ta;
        }
        Map<Integer, TypedArray> map = mTypedArrayCache.get(attrs);
        if (map == null) {
            map = new HashMap<Integer, TypedArray>();
            mTypedArrayCache.put(attrs, map);
        }
        TypedArray ta = map.get(resid);
        if (ta == null) {
            ta = createStyleBasedTypedArray(style, attrs);
            map.put(resid, ta);
        }
        return ta;
    }
    @Override
    public final TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs) {
        return obtainStyledAttributes(set, attrs, 0, 0);
    }
    @Override
    public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs,
            int defStyleAttr, int defStyleRes) {
        BridgeXmlBlockParser parser = null;
        if (set instanceof BridgeXmlBlockParser) {
            parser = (BridgeXmlBlockParser)set;
        } else {
            mLogger.error("Parser is not a BridgeXmlBlockParser!");
            return null;
        }
        boolean[] frameworkAttributes = new boolean[1];
        TreeMap<Integer, String> styleNameMap = searchAttrs(attrs, frameworkAttributes);
        BridgeTypedArray ta = ((BridgeResources) mResources).newTypeArray(attrs.length,
                parser.isPlatformFile());
        IStyleResourceValue defStyleValues = null;
        String customStyle = parser.getAttributeValue(null , "style");
        if (customStyle != null) {
            IResourceValue item = findResValue(customStyle);
            if (item instanceof IStyleResourceValue) {
                defStyleValues = (IStyleResourceValue)item;
            }
        }
        if (defStyleValues == null && defStyleAttr != 0) {
            String defStyleName = searchAttr(defStyleAttr);
            if (mThemeValues != null) {
                IResourceValue item = findItemInStyle(mThemeValues, defStyleName);
                if (item != null) {
                    item = findResValue(item.getValue());
                    if (item instanceof IStyleResourceValue) {
                        defStyleValues = (IStyleResourceValue)item;
                    }
                } else {
                    System.out.println("Failed to find defStyle: " + defStyleName);
                }
            }
        }
        if (defStyleRes != 0) {
            throw new UnsupportedOperationException();
        }
        String namespace = BridgeConstants.NS_RESOURCES;
        if (frameworkAttributes[0] == false) {
            namespace = mProjectCallback.getNamespace();
        }
        if (styleNameMap != null) {
            for (Entry<Integer, String> styleAttribute : styleNameMap.entrySet()) {
                int index = styleAttribute.getKey().intValue();
                String name = styleAttribute.getValue();
                String value = parser.getAttributeValue(namespace, name);
                if (value == null) {
                    IResourceValue resValue = null;
                    if (defStyleValues != null) {
                        resValue = findItemInStyle(defStyleValues, name);
                    }
                    if (resValue == null && mThemeValues != null) {
                        resValue = findItemInStyle(mThemeValues, name);
                    }
                    if (resValue != null) {
                        resValue = resolveResValue(resValue);
                    }
                    ta.bridgeSetValue(index, name, resValue);
                } else {
                    ta.bridgeSetValue(index, name, resolveValue(null, name, value));
                }
            }
        }
        ta.sealArray();
        return ta;
    }
    @Override
    public Looper getMainLooper() {
        return Looper.myLooper();
    }
    private BridgeTypedArray createStyleBasedTypedArray(IStyleResourceValue style, int[] attrs)
            throws Resources.NotFoundException {
        TreeMap<Integer, String> styleNameMap = searchAttrs(attrs, null);
        BridgeTypedArray ta = ((BridgeResources) mResources).newTypeArray(attrs.length,
                false );
        for (Entry<Integer, String> styleAttribute : styleNameMap.entrySet()) {
            int index = styleAttribute.getKey().intValue();
            String name = styleAttribute.getValue();
            IResourceValue resValue = findItemInStyle(style, name);
            ta.bridgeSetValue(index, name, resolveResValue(resValue));
        }
        ta.sealArray();
        return ta;
    }
    private IResourceValue resolveValue(String type, String name, String value) {
        if (value == null) {
            return null;
        }
        IResourceValue resValue = findResValue(value);
        if (resValue == null) {
            return new ResourceValue(type, name, value);
        }
        return resolveResValue(resValue);
    }
    IResourceValue resolveResValue(IResourceValue value) {
        if (value == null) {
            return null;
        }
        if (value instanceof IStyleResourceValue) {
            return value;
        }
        IResourceValue resolvedValue = findResValue(value.getValue());
        if (resolvedValue == null) {
            return value;
        }
        return resolveResValue(resolvedValue);
    }
    IResourceValue findResValue(String reference) {
        if (reference == null) {
            return null;
        }
        if (reference.startsWith(BridgeConstants.PREFIX_THEME_REF)) {
            if (mThemeValues == null) {
                return null;
            }
            boolean frameworkOnly = false;
            if (reference.startsWith(BridgeConstants.PREFIX_ANDROID_THEME_REF)) {
                frameworkOnly = true;
                reference = reference.substring(BridgeConstants.PREFIX_ANDROID_THEME_REF.length());
            } else {
                reference = reference.substring(BridgeConstants.PREFIX_THEME_REF.length());
            }
            String[] segments = reference.split("\\/");
            String referenceName = null;
            if (segments.length == 2) {
                if (BridgeConstants.RES_ATTR.equals(segments[0])) {
                    referenceName = segments[1];
                } else {
                    return null;
                }
            } else {
                referenceName = segments[0];
            }
            if (referenceName.startsWith(BridgeConstants.PREFIX_ANDROID)) {
                frameworkOnly = true;
                referenceName = referenceName.substring(BridgeConstants.PREFIX_ANDROID.length());
            }
            if (frameworkOnly) {
                return findItemInStyle(mThemeValues, referenceName);
            }
            return findItemInStyle(mThemeValues, referenceName);
        } else if (reference.startsWith(BridgeConstants.PREFIX_RESOURCE_REF)) {
            boolean frameworkOnly = false;
            if (BridgeConstants.REFERENCE_NULL.equals(reference)) {
                return null;
            }
            if (reference.startsWith(BridgeConstants.PREFIX_ANDROID_RESOURCE_REF)) {
                frameworkOnly = true;
                reference = reference.substring(
                        BridgeConstants.PREFIX_ANDROID_RESOURCE_REF.length());
            } else {
                reference = reference.substring(BridgeConstants.PREFIX_RESOURCE_REF.length());
            }
            String[] segments = reference.split("\\/");
            if (segments[1].startsWith(BridgeConstants.PREFIX_ANDROID)) {
                frameworkOnly = true;
                segments[1] = segments[1].substring(BridgeConstants.PREFIX_ANDROID.length());
            }
            return findResValue(segments[0], segments[1], frameworkOnly);
        }
        return null;
    }
    private IResourceValue findResValue(String resType, String resName, boolean frameworkOnly) {
        Map<String, IResourceValue> typeMap;
        if (frameworkOnly == false) {
            typeMap = mProjectResources.get(resType);
            if (typeMap != null) {
                IResourceValue item = typeMap.get(resName);
                if (item != null) {
                    return item;
                }
            }
        }
        typeMap = mFrameworkResources.get(resType);
        if (typeMap != null) {
            IResourceValue item = typeMap.get(resName);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
    public IResourceValue getFrameworkResource(String resourceType, String resourceName) {
        return getResource(resourceType, resourceName, mFrameworkResources);
    }
    public IResourceValue getProjectResource(String resourceType, String resourceName) {
        return getResource(resourceType, resourceName, mProjectResources);
    }
    IResourceValue getResource(String resourceType, String resourceName,
            Map<String, Map<String, IResourceValue>> resourceRepository) {
        Map<String, IResourceValue> typeMap = resourceRepository.get(resourceType);
        if (typeMap != null) {
            IResourceValue item = typeMap.get(resourceName);
            if (item != null) {
                item = resolveResValue(item);
                return item;
            }
        }
        return null;
    }
    IResourceValue findItemInStyle(IStyleResourceValue style, String itemName) {
        IResourceValue item = style.findItem(itemName);
        if (item == null && mStyleInheritanceMap != null) {
            IStyleResourceValue parentStyle = mStyleInheritanceMap.get(style);
            if (parentStyle != null) {
                return findItemInStyle(parentStyle, itemName);
            }
        }
        return item;
    }
    private TreeMap<Integer,String> searchAttrs(int[] attrs, boolean[] outFrameworkFlag) {
        String arrayName = Bridge.resolveResourceValue(attrs);
        if (arrayName != null) {
            TreeMap<Integer,String> attributes = new TreeMap<Integer, String>();
            for (int i = 0 ; i < attrs.length ; i++) {
                String[] info = Bridge.resolveResourceValue(attrs[i]);
                if (info != null) {
                    attributes.put(i, info[0]);
                } else {
                    attributes.put(i, null);
                }
            }
            if (outFrameworkFlag != null) {
                outFrameworkFlag[0] = true;
            }
            return attributes;
        }
        arrayName = mProjectCallback.resolveResourceValue(attrs);
        if (arrayName != null) {
            TreeMap<Integer,String> attributes = new TreeMap<Integer, String>();
            for (int i = 0 ; i < attrs.length ; i++) {
                String[] info = mProjectCallback.resolveResourceValue(attrs[i]);
                if (info != null) {
                    attributes.put(i, info[0]);
                } else {
                    attributes.put(i, null);
                }
            }
            if (outFrameworkFlag != null) {
                outFrameworkFlag[0] = false;
            }
            return attributes;
        }
        return null;
    }
    public String searchAttr(int attr) {
        String[] info = Bridge.resolveResourceValue(attr);
        if (info != null) {
            return info[0];
        }
        info = mProjectCallback.resolveResourceValue(attr);
        if (info != null) {
            return info[0];
        }
        return null;
    }
    int getDynamicIdByStyle(IStyleResourceValue resValue) {
        if (mDynamicIdToStyleMap == null) {
            mDynamicIdToStyleMap = new HashMap<Integer, IStyleResourceValue>();
            mStyleToDynamicIdMap = new HashMap<IStyleResourceValue, Integer>();
        }
        Integer id = mStyleToDynamicIdMap.get(resValue);
        if (id == null) {
            id = Integer.valueOf(++mDynamicIdGenerator);
            mDynamicIdToStyleMap.put(id, resValue);
            mStyleToDynamicIdMap.put(resValue, id);
        }
        return id;
    }
    private IStyleResourceValue getStyleByDynamicId(int i) {
        if (mDynamicIdToStyleMap != null) {
            return mDynamicIdToStyleMap.get(i);
        }
        return null;
    }
    int getFrameworkIdValue(String idName, int defValue) {
        Integer value = Bridge.getResourceValue(BridgeConstants.RES_ID, idName);
        if (value != null) {
            return value.intValue();
        }
        return defValue;
    }
    int getProjectIdValue(String idName, int defValue) {
        if (mProjectCallback != null) {
            Integer value = mProjectCallback.getResourceValue(BridgeConstants.RES_ID, idName);
            if (value != null) {
                return value.intValue();
            }
        }
        return defValue;
    }
    @Override
    public boolean bindService(Intent arg0, ServiceConnection arg1, int arg2) {
        return false;
    }
    @Override
    public int checkCallingOrSelfPermission(String arg0) {
        return 0;
    }
    @Override
    public int checkCallingOrSelfUriPermission(Uri arg0, int arg1) {
        return 0;
    }
    @Override
    public int checkCallingPermission(String arg0) {
        return 0;
    }
    @Override
    public int checkCallingUriPermission(Uri arg0, int arg1) {
        return 0;
    }
    @Override
    public int checkPermission(String arg0, int arg1, int arg2) {
        return 0;
    }
    @Override
    public int checkUriPermission(Uri arg0, int arg1, int arg2, int arg3) {
        return 0;
    }
    @Override
    public int checkUriPermission(Uri arg0, String arg1, String arg2, int arg3,
            int arg4, int arg5) {
        return 0;
    }
    @Override
    public void clearWallpaper() {
    }
    @Override
    public Context createPackageContext(String arg0, int arg1) {
        return null;
    }
    @Override
    public String[] databaseList() {
        return null;
    }
    @Override
    public boolean deleteDatabase(String arg0) {
        return false;
    }
    @Override
    public boolean deleteFile(String arg0) {
        return false;
    }
    @Override
    public void enforceCallingOrSelfPermission(String arg0, String arg1) {
    }
    @Override
    public void enforceCallingOrSelfUriPermission(Uri arg0, int arg1,
            String arg2) {
    }
    @Override
    public void enforceCallingPermission(String arg0, String arg1) {
    }
    @Override
    public void enforceCallingUriPermission(Uri arg0, int arg1, String arg2) {
    }
    @Override
    public void enforcePermission(String arg0, int arg1, int arg2, String arg3) {
    }
    @Override
    public void enforceUriPermission(Uri arg0, int arg1, int arg2, int arg3,
            String arg4) {
    }
    @Override
    public void enforceUriPermission(Uri arg0, String arg1, String arg2,
            int arg3, int arg4, int arg5, String arg6) {
    }
    @Override
    public String[] fileList() {
        return null;
    }
    @Override
    public AssetManager getAssets() {
        return null;
    }
    @Override
    public File getCacheDir() {
        return null;
    }
    @Override
    public File getExternalCacheDir() {
        return null;
    }
    @Override
    public ContentResolver getContentResolver() {
        if (mContentResolver == null) {
            mContentResolver = new BridgeContentResolver(this);
        }
        return mContentResolver;
    }
    @Override
    public File getDatabasePath(String arg0) {
        return null;
    }
    @Override
    public File getDir(String arg0, int arg1) {
        return null;
    }
    @Override
    public File getFileStreamPath(String arg0) {
        return null;
    }
    @Override
    public File getFilesDir() {
        return null;
    }
    @Override
    public File getExternalFilesDir(String type) {
        return null;
    }
    @Override
    public String getPackageCodePath() {
        return null;
    }
    @Override
    public PackageManager getPackageManager() {
        return null;
    }
    @Override
    public String getPackageName() {
        return null;
    }
    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }
    @Override
    public String getPackageResourcePath() {
        return null;
    }
    @Override
    public File getSharedPrefsFile(String name) {
        return null;
    }
    @Override
    public SharedPreferences getSharedPreferences(String arg0, int arg1) {
        return null;
    }
    @Override
    public Drawable getWallpaper() {
        return null;
    }
    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return -1;
    }
    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return -1;
    }
    @Override
    public void grantUriPermission(String arg0, Uri arg1, int arg2) {
    }
    @SuppressWarnings("unused")
    @Override
    public FileInputStream openFileInput(String arg0)
            throws FileNotFoundException {
        return null;
    }
    @SuppressWarnings("unused")
    @Override
    public FileOutputStream openFileOutput(String arg0, int arg1)
            throws FileNotFoundException {
        return null;
    }
    @Override
    public SQLiteDatabase openOrCreateDatabase(String arg0, int arg1,
            CursorFactory arg2) {
        return null;
    }
    @Override
    public Drawable peekWallpaper() {
        return null;
    }
    @Override
    public Intent registerReceiver(BroadcastReceiver arg0, IntentFilter arg1) {
        return null;
    }
    @Override
    public Intent registerReceiver(BroadcastReceiver arg0, IntentFilter arg1,
            String arg2, Handler arg3) {
        return null;
    }
    @Override
    public void removeStickyBroadcast(Intent arg0) {
    }
    @Override
    public void revokeUriPermission(Uri arg0, int arg1) {
    }
    @Override
    public void sendBroadcast(Intent arg0) {
    }
    @Override
    public void sendBroadcast(Intent arg0, String arg1) {
    }
    @Override
    public void sendOrderedBroadcast(Intent arg0, String arg1) {
    }
    @Override
    public void sendOrderedBroadcast(Intent arg0, String arg1,
            BroadcastReceiver arg2, Handler arg3, int arg4, String arg5,
            Bundle arg6) {
    }
    @Override
    public void sendStickyBroadcast(Intent arg0) {
    }
    @Override
    public void sendStickyOrderedBroadcast(Intent intent,
            BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData,
           Bundle initialExtras) {
    }
    @Override
    public void setTheme(int arg0) {
    }
    @SuppressWarnings("unused")
    @Override
    public void setWallpaper(Bitmap arg0) throws IOException {
    }
    @SuppressWarnings("unused")
    @Override
    public void setWallpaper(InputStream arg0) throws IOException {
    }
    @Override
    public void startActivity(Intent arg0) {
    }
    @Override
    public void startIntentSender(IntentSender intent,
            Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags)
            throws IntentSender.SendIntentException {
    }
    @Override
    public boolean startInstrumentation(ComponentName arg0, String arg1,
            Bundle arg2) {
        return false;
    }
    @Override
    public ComponentName startService(Intent arg0) {
        return null;
    }
    @Override
    public boolean stopService(Intent arg0) {
        return false;
    }
    @Override
    public void unbindService(ServiceConnection arg0) {
    }
    @Override
    public void unregisterReceiver(BroadcastReceiver arg0) {
    }
    @Override
    public Context getApplicationContext() {
        throw new UnsupportedOperationException();
    }
}
