public class BrandingResources {
    private static final String TAG = ImApp.LOG_TAG;
    private Map<Integer, Integer> mResMapping;
    private Resources mPackageRes;
    private int[] mSmileyIcons;
    private BrandingResources mDefaultRes;
    public BrandingResources(Context context, ImPluginInfo pluginInfo,
            BrandingResources defaultRes) {
        mDefaultRes = defaultRes;
        PackageManager pm = context.getPackageManager();
        try {
            mPackageRes = pm.getResourcesForApplication(pluginInfo.mPackageName);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Can not load resources from package: " + pluginInfo.mPackageName);
        }
        ClassLoader classLoader = context.getClassLoader();
        try {
            Class cls = classLoader.loadClass(pluginInfo.mClassName);
            Method m = cls.getMethod("onBind", Intent.class);
            ImPlugin plugin = (ImPlugin)m.invoke(cls.newInstance(), new Object[]{null});
            mResMapping = plugin.getResourceMap();
            mSmileyIcons = plugin.getSmileyIconIds();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (SecurityException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        }
    }
    public BrandingResources(Context context, Map<Integer, Integer> resMapping,
            BrandingResources defaultRes) {
        this(context.getResources(), resMapping, null, defaultRes);
    }
    public BrandingResources(Resources packageRes, Map<Integer, Integer> resMapping,
            int[] smileyIcons, BrandingResources defaultRes) {
        mPackageRes = packageRes;
        mResMapping = resMapping;
        mSmileyIcons = smileyIcons;
        mDefaultRes = defaultRes;
    }
    public Drawable getDrawable(int id) {
        int resId = getPackageResourceId(id);
        if (resId != 0) {
            return mPackageRes.getDrawable(resId);
        } else if (mDefaultRes != null){
            return mDefaultRes.getDrawable(id);
        } else {
            return null;
        }
    }
    public int[] getSmileyIcons() {
        return mSmileyIcons;
    }
    public Drawable getSmileyIcon(int smileyId){
        if (mPackageRes == null) {
            return null;
        }
        return mPackageRes.getDrawable(smileyId);
    }
    public String getString(int id, Object... formatArgs) {
        int resId = getPackageResourceId(id);
        if (resId != 0) {
            return mPackageRes.getString(resId, formatArgs);
        } else if (mDefaultRes != null){
            return  mDefaultRes.getString(id, formatArgs);
        } else {
            return null;
        }
    }
    public String[] getStringArray(int id) {
        int resId = getPackageResourceId(id);
        if (resId != 0) {
            return mPackageRes.getStringArray(resId);
        } else if (mDefaultRes != null){
            return mDefaultRes.getStringArray(id);
        } else {
            return null;
        }
    }
    private int getPackageResourceId(int id) {
        if (mResMapping == null || mPackageRes == null) {
            return 0;
        }
        Integer resId = mResMapping.get(id);
        return resId == null ? 0 : resId;
    }
}
