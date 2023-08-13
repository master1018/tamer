public final class AndroidTargetParser {
    private static final String TAG = "Framework Resource Parser";
    private final IAndroidTarget mAndroidTarget;
    public AndroidTargetParser(IAndroidTarget platformTarget) {
        mAndroidTarget = platformTarget;
    }
    public IStatus run(IProgressMonitor monitor) {
        try {
            SubMonitor progress = SubMonitor.convert(monitor,
                    String.format("Parsing SDK %1$s", mAndroidTarget.getName()),
                    14);
            AndroidTargetData targetData = new AndroidTargetData(mAndroidTarget);
            DexWrapper dexWrapper = new DexWrapper();
            IStatus res = dexWrapper.loadDex(mAndroidTarget.getPath(IAndroidTarget.DX_JAR));
            if (res != Status.OK_STATUS) {
                return new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                        String.format("dx.jar loading failed for target '%1$s'",
                                mAndroidTarget.getFullName()));
            }
            targetData.setDexWrapper(dexWrapper);
            progress.worked(1);
            AndroidJarLoader classLoader =
                new AndroidJarLoader(mAndroidTarget.getPath(IAndroidTarget.ANDROID_JAR));
            preload(classLoader, progress.newChild(40, SubMonitor.SUPPRESS_NONE));
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            progress.subTask("Resource IDs");
            IResourceRepository frameworkRepository = collectResourceIds(classLoader);
            progress.worked(1);
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            progress.subTask("Permissions");
            String[] permissionValues = collectPermissions(classLoader);
            progress.worked(1);
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            progress.subTask("Intents");
            ArrayList<String> activity_actions = new ArrayList<String>();
            ArrayList<String> broadcast_actions = new ArrayList<String>();
            ArrayList<String> service_actions = new ArrayList<String>();
            ArrayList<String> categories = new ArrayList<String>();
            collectIntentFilterActionsAndCategories(activity_actions, broadcast_actions,
                    service_actions, categories);
            progress.worked(1);
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            progress.subTask("Attributes definitions");
            AttrsXmlParser attrsXmlParser = new AttrsXmlParser(
                    mAndroidTarget.getPath(IAndroidTarget.ATTRIBUTES));
            attrsXmlParser.preload();
            progress.worked(1);
            progress.subTask("Manifest definitions");
            AttrsXmlParser attrsManifestXmlParser = new AttrsXmlParser(
                    mAndroidTarget.getPath(IAndroidTarget.MANIFEST_ATTRIBUTES),
                    attrsXmlParser);
            attrsManifestXmlParser.preload();
            progress.worked(1);
            Collection<ViewClassInfo> mainList = new ArrayList<ViewClassInfo>();
            Collection<ViewClassInfo> groupList = new ArrayList<ViewClassInfo>();
            progress.subTask("Widgets and layouts");
            collectLayoutClasses(classLoader, attrsXmlParser, mainList, groupList,
                    progress.newChild(1));
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            ViewClassInfo[] layoutViewsInfo = mainList.toArray(
                    new ViewClassInfo[mainList.size()]);
            ViewClassInfo[] layoutGroupsInfo = groupList.toArray(
                    new ViewClassInfo[groupList.size()]);
            mainList.clear();
            groupList.clear();
            collectPreferenceClasses(classLoader, attrsXmlParser, mainList, groupList,
                    progress.newChild(1));
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            ViewClassInfo[] preferencesInfo = mainList.toArray(new ViewClassInfo[mainList.size()]);
            ViewClassInfo[] preferenceGroupsInfo = groupList.toArray(
                    new ViewClassInfo[groupList.size()]);
            Map<String, DeclareStyleableInfo> xmlMenuMap = collectMenuDefinitions(attrsXmlParser);
            Map<String, DeclareStyleableInfo> xmlSearchableMap = collectSearchableDefinitions(
                    attrsXmlParser);
            Map<String, DeclareStyleableInfo> manifestMap = collectManifestDefinitions(
                                                                            attrsManifestXmlParser);
            Map<String, Map<String, Integer>> enumValueMap = attrsXmlParser.getEnumFlagValues();
            Map<String, DeclareStyleableInfo> xmlAppWidgetMap = null;
            if (mAndroidTarget.getVersion().getApiLevel() >= 3) {
                xmlAppWidgetMap = collectAppWidgetDefinitions(attrsXmlParser);
            }
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            AndroidManifestDescriptors manifestDescriptors = new AndroidManifestDescriptors();
            manifestDescriptors.updateDescriptors(manifestMap);
            progress.worked(1);
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            LayoutDescriptors layoutDescriptors = new LayoutDescriptors();
            layoutDescriptors.updateDescriptors(layoutViewsInfo, layoutGroupsInfo);
            progress.worked(1);
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            MenuDescriptors menuDescriptors = new MenuDescriptors();
            menuDescriptors.updateDescriptors(xmlMenuMap);
            progress.worked(1);
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            XmlDescriptors xmlDescriptors = new XmlDescriptors();
            xmlDescriptors.updateDescriptors(
                    xmlSearchableMap,
                    xmlAppWidgetMap,
                    preferencesInfo,
                    preferenceGroupsInfo);
            progress.worked(1);
            ProjectResources resources = ResourceManager.getInstance().loadFrameworkResources(
                    mAndroidTarget);
            progress.worked(1);
            LayoutBridge layoutBridge = loadLayoutBridge();
            progress.worked(1);
            targetData.setExtraData(frameworkRepository,
                    manifestDescriptors,
                    layoutDescriptors,
                    menuDescriptors,
                    xmlDescriptors,
                    enumValueMap,
                    permissionValues,
                    activity_actions.toArray(new String[activity_actions.size()]),
                    broadcast_actions.toArray(new String[broadcast_actions.size()]),
                    service_actions.toArray(new String[service_actions.size()]),
                    categories.toArray(new String[categories.size()]),
                    mAndroidTarget.getPlatformLibraries(),
                    mAndroidTarget.getOptionalLibraries(),
                    resources,
                    layoutBridge);
            Sdk.getCurrent().setTargetData(mAndroidTarget, targetData);
            return Status.OK_STATUS;
        } catch (Exception e) {
            AdtPlugin.logAndPrintError(e, TAG, "SDK parser failed"); 
            AdtPlugin.printToConsole("SDK parser failed", e.getMessage());
            return new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID, "SDK parser failed", e);
        }
    }
    private void preload(AndroidJarLoader classLoader, IProgressMonitor monitor) {
        try {
            classLoader.preLoadClasses("" ,        
                    mAndroidTarget.getName(),                       
                    monitor);
        } catch (InvalidAttributeValueException e) {
            AdtPlugin.log(e, "Problem preloading classes"); 
        } catch (IOException e) {
            AdtPlugin.log(e, "Problem preloading classes"); 
        }
    }
    private IResourceRepository collectResourceIds(
            AndroidJarLoader classLoader) {
        try {
            Class<?> r = classLoader.loadClass(AndroidConstants.CLASS_R);
            if (r != null) {
                Map<ResourceType, List<ResourceItem>> map = parseRClass(r);
                if (map != null) {
                    return new FrameworkResourceRepository(map);
                }
            }
        } catch (ClassNotFoundException e) {
            AdtPlugin.logAndPrintError(e, TAG,
                    "Collect resource IDs failed, class %1$s not found in %2$s", 
                    AndroidConstants.CLASS_R,
                    mAndroidTarget.getPath(IAndroidTarget.ANDROID_JAR));
        }
        return null;
    }
    private Map<ResourceType, List<ResourceItem>> parseRClass(Class<?> rClass) {
        Class<?>[] classes = rClass.getClasses();
        if (classes.length > 0) {
            HashMap<ResourceType, List<ResourceItem>> map =
                new HashMap<ResourceType, List<ResourceItem>>();
            for (int c = 0 ; c < classes.length ; c++) {
                Class<?> subClass = classes[c];
                String name = subClass.getSimpleName();
                ResourceType type = ResourceType.getEnum(name);
                if (type != null) {
                    List<ResourceItem> list = new ArrayList<ResourceItem>();
                    map.put(type, list);
                    Field[] fields = subClass.getFields();
                    for (Field f : fields) {
                        list.add(new ResourceItem(f.getName()));
                    }
                }
            }
            return map;
        }
        return null;
    }
    private String[] collectPermissions(AndroidJarLoader classLoader) {
        try {
            Class<?> permissionClass =
                classLoader.loadClass(AndroidConstants.CLASS_MANIFEST_PERMISSION);
            if (permissionClass != null) {
                ArrayList<String> list = new ArrayList<String>();
                Field[] fields = permissionClass.getFields();
                for (Field f : fields) {
                    int modifiers = f.getModifiers();
                    if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) &&
                            Modifier.isPublic(modifiers)) {
                        try {
                            Object value = f.get(null);
                            if (value instanceof String) {
                                list.add((String)value);
                            }
                        } catch (IllegalArgumentException e) {
                        } catch (IllegalAccessException e) {
                        } catch (NullPointerException npe) {
                        } catch (ExceptionInInitializerError  eiie) {
                        }
                    }
                }
                return list.toArray(new String[list.size()]);
            }
        } catch (ClassNotFoundException e) {
            AdtPlugin.logAndPrintError(e, TAG,
                    "Collect permissions failed, class %1$s not found in %2$s", 
                    AndroidConstants.CLASS_MANIFEST_PERMISSION,
                    mAndroidTarget.getPath(IAndroidTarget.ANDROID_JAR));
        }
        return new String[0];
    }
    private void collectIntentFilterActionsAndCategories(ArrayList<String> activityActions,
            ArrayList<String> broadcastActions,
            ArrayList<String> serviceActions, ArrayList<String> categories)  {
        collectValues(mAndroidTarget.getPath(IAndroidTarget.ACTIONS_ACTIVITY),
                activityActions);
        collectValues(mAndroidTarget.getPath(IAndroidTarget.ACTIONS_BROADCAST),
                broadcastActions);
        collectValues(mAndroidTarget.getPath(IAndroidTarget.ACTIONS_SERVICE),
                serviceActions);
        collectValues(mAndroidTarget.getPath(IAndroidTarget.CATEGORIES),
                categories);
    }
    private void collectValues(String osFilePath, ArrayList<String> values) {
        FileReader fr = null;
        BufferedReader reader = null;
        try {
            fr = new FileReader(osFilePath);
            reader = new BufferedReader(fr);
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0 && line.startsWith("#") == false) { 
                    values.add(line);
                }
            }
        } catch (IOException e) {
            AdtPlugin.log(e, "Failed to read SDK values"); 
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                AdtPlugin.log(e, "Failed to read SDK values"); 
            }
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                AdtPlugin.log(e, "Failed to read SDK values"); 
            }
        }
    }
    private void collectLayoutClasses(AndroidJarLoader classLoader,
            AttrsXmlParser attrsXmlParser,
            Collection<ViewClassInfo> mainList,
            Collection<ViewClassInfo> groupList,
            IProgressMonitor monitor) {
        LayoutParamsParser ldp = null;
        try {
            WidgetClassLoader loader = new WidgetClassLoader(
                    mAndroidTarget.getPath(IAndroidTarget.WIDGETS));
            if (loader.parseWidgetList(monitor)) {
                ldp = new LayoutParamsParser(loader, attrsXmlParser);
            }
        } catch (FileNotFoundException e) {
            AdtPlugin.log(e, "Android Framework Parser"); 
        }
        if (ldp == null) {
            ldp = new LayoutParamsParser(classLoader, attrsXmlParser);
        }
        ldp.parseLayoutClasses(monitor);
        List<ViewClassInfo> views = ldp.getViews();
        List<ViewClassInfo> groups = ldp.getGroups();
        if (views != null && groups != null) {
            mainList.addAll(views);
            groupList.addAll(groups);
        }
    }
    private void collectPreferenceClasses(AndroidJarLoader classLoader,
            AttrsXmlParser attrsXmlParser, Collection<ViewClassInfo> mainList,
            Collection<ViewClassInfo> groupList, IProgressMonitor monitor) {
        LayoutParamsParser ldp = new LayoutParamsParser(classLoader, attrsXmlParser);
        try {
            ldp.parsePreferencesClasses(monitor);
            List<ViewClassInfo> prefs = ldp.getViews();
            List<ViewClassInfo> groups = ldp.getGroups();
            if (prefs != null && groups != null) {
                mainList.addAll(prefs);
                groupList.addAll(groups);
            }
        } catch (NoClassDefFoundError e) {
            AdtPlugin.logAndPrintError(e, TAG,
                    "Collect preferences failed, class %1$s not found in %2$s",
                    e.getMessage(),
                    classLoader.getSource());
        } catch (Throwable e) {
            AdtPlugin.log(e, "Android Framework Parser: failed to collect preference classes"); 
            AdtPlugin.printErrorToConsole("Android Framework Parser",
                    "failed to collect preference classes");
        }
    }
    private Map<String, DeclareStyleableInfo> collectMenuDefinitions(
            AttrsXmlParser attrsXmlParser) {
        Map<String, DeclareStyleableInfo> map = attrsXmlParser.getDeclareStyleableList();
        Map<String, DeclareStyleableInfo> map2 = new HashMap<String, DeclareStyleableInfo>();
        for (String key : new String[] { "Menu",        
                                         "MenuItem",        
                                         "MenuGroup" }) {   
            if (map.containsKey(key)) {
                map2.put(key, map.get(key));
            } else {
                AdtPlugin.log(IStatus.WARNING,
                        "Menu declare-styleable %1$s not found in file %2$s", 
                        key, attrsXmlParser.getOsAttrsXmlPath());
                AdtPlugin.printErrorToConsole("Android Framework Parser",
                        String.format("Menu declare-styleable %1$s not found in file %2$s", 
                        key, attrsXmlParser.getOsAttrsXmlPath()));
            }
        }
        return Collections.unmodifiableMap(map2);
    }
    private Map<String, DeclareStyleableInfo> collectSearchableDefinitions(
            AttrsXmlParser attrsXmlParser) {
        Map<String, DeclareStyleableInfo> map = attrsXmlParser.getDeclareStyleableList();
        Map<String, DeclareStyleableInfo> map2 = new HashMap<String, DeclareStyleableInfo>();
        for (String key : new String[] { "Searchable",              
                                         "SearchableActionKey" }) { 
            if (map.containsKey(key)) {
                map2.put(key, map.get(key));
            } else {
                AdtPlugin.log(IStatus.WARNING,
                        "Searchable declare-styleable %1$s not found in file %2$s", 
                        key, attrsXmlParser.getOsAttrsXmlPath());
                AdtPlugin.printErrorToConsole("Android Framework Parser",
                        String.format("Searchable declare-styleable %1$s not found in file %2$s", 
                        key, attrsXmlParser.getOsAttrsXmlPath()));
            }
        }
        return Collections.unmodifiableMap(map2);
    }
    private Map<String, DeclareStyleableInfo> collectAppWidgetDefinitions(
            AttrsXmlParser attrsXmlParser) {
        Map<String, DeclareStyleableInfo> map = attrsXmlParser.getDeclareStyleableList();
        Map<String, DeclareStyleableInfo> map2 = new HashMap<String, DeclareStyleableInfo>();
        for (String key : new String[] { "AppWidgetProviderInfo" }) {  
            if (map.containsKey(key)) {
                map2.put(key, map.get(key));
            } else {
                AdtPlugin.log(IStatus.WARNING,
                        "AppWidget declare-styleable %1$s not found in file %2$s", 
                        key, attrsXmlParser.getOsAttrsXmlPath());
                AdtPlugin.printErrorToConsole("Android Framework Parser",
                        String.format("AppWidget declare-styleable %1$s not found in file %2$s", 
                        key, attrsXmlParser.getOsAttrsXmlPath()));
            }
        }
        return Collections.unmodifiableMap(map2);
    }
    private Map<String, DeclareStyleableInfo> collectManifestDefinitions(
            AttrsXmlParser attrsXmlParser) {
        return attrsXmlParser.getDeclareStyleableList();
    }
    private LayoutBridge loadLayoutBridge() {
        LayoutBridge layoutBridge = new LayoutBridge();
        try {
            File f = new File(mAndroidTarget.getPath(IAndroidTarget.LAYOUT_LIB));
            if (f.isFile() == false) {
                AdtPlugin.log(IStatus.ERROR, "layoutlib.jar is missing!"); 
            } else {
                URI uri = f.toURI();
                URL url = uri.toURL();
                layoutBridge.classLoader = new URLClassLoader(new URL[] { url },
                        this.getClass().getClassLoader());
                Class<?> clazz = layoutBridge.classLoader.loadClass(AndroidConstants.CLASS_BRIDGE);
                if (clazz != null) {
                    Constructor<?> constructor = clazz.getConstructor();
                    if (constructor != null) {
                        Object bridge = constructor.newInstance();
                        if (bridge instanceof ILayoutBridge) {
                            layoutBridge.bridge = (ILayoutBridge)bridge;
                        }
                    }
                }
                if (layoutBridge.bridge == null) {
                    layoutBridge.status = LoadStatus.FAILED;
                    AdtPlugin.log(IStatus.ERROR, "Failed to load " + AndroidConstants.CLASS_BRIDGE); 
                } else {
                    try {
                        layoutBridge.apiLevel = layoutBridge.bridge.getApiLevel();
                    } catch (AbstractMethodError e) {
                        layoutBridge.apiLevel = 1;
                    }
                    layoutBridge.status = LoadStatus.LOADED;
                }
            }
        } catch (Throwable t) {
            layoutBridge.status = LoadStatus.FAILED;
            AdtPlugin.log(t, "Failed to load the LayoutLib");
        }
        return layoutBridge;
    }
}
