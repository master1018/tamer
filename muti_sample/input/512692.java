public class AndroidTargetData {
    public final static int DESCRIPTOR_MANIFEST = 1;
    public final static int DESCRIPTOR_LAYOUT = 2;
    public final static int DESCRIPTOR_MENU = 3;
    public final static int DESCRIPTOR_XML = 4;
    public final static int DESCRIPTOR_RESOURCES = 5;
    public final static int DESCRIPTOR_SEARCHABLE = 6;
    public final static int DESCRIPTOR_PREFERENCES = 7;
    public final static int DESCRIPTOR_APPWIDGET_PROVIDER = 8;
    public final static class LayoutBridge {
        public ILayoutBridge bridge;
        public LoadStatus status = LoadStatus.LOADING;
        public ClassLoader classLoader;
        public int apiLevel;
    }
    private final IAndroidTarget mTarget;
    private DexWrapper mDexWrapper;
    private Hashtable<String, String[]> mAttributeValues = new Hashtable<String, String[]>();
    private IResourceRepository mSystemResourceRepository;
    private AndroidManifestDescriptors mManifestDescriptors;
    private LayoutDescriptors mLayoutDescriptors;
    private MenuDescriptors mMenuDescriptors;
    private XmlDescriptors mXmlDescriptors;
    private Map<String, Map<String, Integer>> mEnumValueMap;
    private ProjectResources mFrameworkResources;
    private LayoutBridge mLayoutBridge;
    private boolean mLayoutBridgeInit = false;
    AndroidTargetData(IAndroidTarget androidTarget) {
        mTarget = androidTarget;
    }
    void setDexWrapper(DexWrapper wrapper) {
        mDexWrapper = wrapper;
    }
    void setExtraData(IResourceRepository systemResourceRepository,
            AndroidManifestDescriptors manifestDescriptors,
            LayoutDescriptors layoutDescriptors,
            MenuDescriptors menuDescriptors,
            XmlDescriptors xmlDescriptors,
            Map<String, Map<String, Integer>> enumValueMap,
            String[] permissionValues,
            String[] activityIntentActionValues,
            String[] broadcastIntentActionValues,
            String[] serviceIntentActionValues,
            String[] intentCategoryValues,
            String[] platformLibraries,
            IOptionalLibrary[] optionalLibraries,
            ProjectResources resources,
            LayoutBridge layoutBridge) {
        mSystemResourceRepository = systemResourceRepository;
        mManifestDescriptors = manifestDescriptors;
        mLayoutDescriptors = layoutDescriptors;
        mMenuDescriptors = menuDescriptors;
        mXmlDescriptors = xmlDescriptors;
        mEnumValueMap = enumValueMap;
        mFrameworkResources = resources;
        mLayoutBridge = layoutBridge;
        setPermissions(permissionValues);
        setIntentFilterActionsAndCategories(activityIntentActionValues, broadcastIntentActionValues,
                serviceIntentActionValues, intentCategoryValues);
        setOptionalLibraries(platformLibraries, optionalLibraries);
    }
    public DexWrapper getDexWrapper() {
        return mDexWrapper;
    }
    public IResourceRepository getSystemResources() {
        return mSystemResourceRepository;
    }
    public IDescriptorProvider getDescriptorProvider(int descriptorId) {
        switch (descriptorId) {
            case DESCRIPTOR_MANIFEST:
                return mManifestDescriptors;
            case DESCRIPTOR_LAYOUT:
                return mLayoutDescriptors;
            case DESCRIPTOR_MENU:
                return mMenuDescriptors;
            case DESCRIPTOR_XML:
                return mXmlDescriptors;
            case DESCRIPTOR_RESOURCES:
                return ResourcesDescriptors.getInstance();
            case DESCRIPTOR_PREFERENCES:
                return mXmlDescriptors.getPreferencesProvider();
            case DESCRIPTOR_APPWIDGET_PROVIDER:
                return mXmlDescriptors.getAppWidgetProvider();
            case DESCRIPTOR_SEARCHABLE:
                return mXmlDescriptors.getSearchableProvider();
            default :
                 throw new IllegalArgumentException();
        }
    }
    public AndroidManifestDescriptors getManifestDescriptors() {
        return mManifestDescriptors;
    }
    public LayoutDescriptors getLayoutDescriptors() {
        return mLayoutDescriptors;
    }
    public MenuDescriptors getMenuDescriptors() {
        return mMenuDescriptors;
    }
    public XmlDescriptors getXmlDescriptors() {
        return mXmlDescriptors;
    }
    public String[] getAttributeValues(String elementName, String attributeName) {
        String key = String.format("(%1$s,%2$s)", elementName, attributeName); 
        return mAttributeValues.get(key);
    }
    public String[] getAttributeValues(String elementName, String attributeName,
            String greatGrandParentElementName) {
        if (greatGrandParentElementName != null) {
            String key = String.format("(%1$s,%2$s,%3$s)", 
                    greatGrandParentElementName, elementName, attributeName);
            String[] values = mAttributeValues.get(key);
            if (values != null) {
                return values;
            }
        }
        return getAttributeValues(elementName, attributeName);
    }
    public Map<String, Map<String, Integer>> getEnumValueMap() {
        return mEnumValueMap;
    }
    public ProjectResources getFrameworkResources() {
        return mFrameworkResources;
    }
    public synchronized LayoutBridge getLayoutBridge() {
        if (mLayoutBridgeInit == false && mLayoutBridge.bridge != null) {
            mLayoutBridge.bridge.init(mTarget.getPath(IAndroidTarget.FONTS),
                    getEnumValueMap());
            mLayoutBridgeInit = true;
        }
        return mLayoutBridge;
    }
    private void setPermissions(String[] permissionValues) {
        setValues("(uses-permission,android:name)", permissionValues);   
        setValues("(application,android:permission)", permissionValues); 
        setValues("(activity,android:permission)", permissionValues);    
        setValues("(receiver,android:permission)", permissionValues);    
        setValues("(service,android:permission)", permissionValues);     
        setValues("(provider,android:permission)", permissionValues);    
    }
    private void setIntentFilterActionsAndCategories(String[] activityIntentActions,
            String[] broadcastIntentActions, String[] serviceIntentActions,
            String[] intentCategoryValues) {
        setValues("(activity,action,android:name)", activityIntentActions);  
        setValues("(receiver,action,android:name)", broadcastIntentActions); 
        setValues("(service,action,android:name)", serviceIntentActions);    
        setValues("(category,android:name)", intentCategoryValues);          
    }
    private void setOptionalLibraries(String[] platformLibraries,
            IOptionalLibrary[] optionalLibraries) {
        ArrayList<String> libs = new ArrayList<String>();
        if (platformLibraries != null) {
            for (String name : platformLibraries) {
                libs.add(name);
            }
        }
        if (optionalLibraries != null) {
            for (int i = 0; i < optionalLibraries.length; i++) {
                libs.add(optionalLibraries[i].getName());
            }
        }
        setValues("(uses-library,android:name)",  libs.toArray(new String[libs.size()]));
    }
    private void setValues(String name, String[] values) {
        mAttributeValues.remove(name);
        mAttributeValues.put(name, values);
    }
}
