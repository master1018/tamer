public class AndroidManifestParser {
    private final static int LEVEL_MANIFEST = 0;
    private final static int LEVEL_APPLICATION = 1;
    private final static int LEVEL_ACTIVITY = 2;
    private final static int LEVEL_INTENT_FILTER = 3;
    private final static int LEVEL_CATEGORY = 4;
    private final static String ACTION_MAIN = "android.intent.action.MAIN"; 
    private final static String CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"; 
    public static class Instrumentation {
        private final String mName;
        private final String mTargetPackage;
        Instrumentation(String name, String targetPackage) {
            mName = name;
            mTargetPackage = targetPackage;
        }
        public String getName() {
            return mName;
        }
        public String getTargetPackage() {
            return mTargetPackage;
        }
    }
    public static class Activity {
        private final String mName;
        private final boolean mIsExported;
        private boolean mHasAction = false;
        private boolean mHasMainAction = false;
        private boolean mHasLauncherCategory = false;
        public Activity(String name, boolean exported) {
            mName = name;
            mIsExported = exported;
        }
        public String getName() {
            return mName;
        }
        public boolean isExported() {
            return mIsExported;
        }
        public boolean hasAction() {
            return mHasAction;
        }
        public boolean isHomeActivity() {
            return mHasMainAction && mHasLauncherCategory;
        }
        void setHasAction(boolean hasAction) {
            mHasAction = hasAction;
        }
        void resetIntentFilter() {
            if (isHomeActivity() == false) {
                mHasMainAction = mHasLauncherCategory = false;
            }
        }
        void setHasMainAction(boolean hasMainAction) {
            mHasMainAction = hasMainAction;
        }
        void setHasLauncherCategory(boolean hasLauncherCategory) {
            mHasLauncherCategory = hasLauncherCategory;
        }
    }
    private static class ManifestHandler extends XmlErrorHandler {
        private String mPackage;
        private final ArrayList<Activity> mActivities = new ArrayList<Activity>();
        private Activity mLauncherActivity = null;
        private Set<String> mProcesses = null;
        private Boolean mDebuggable = null;
        private String mApiLevelRequirement = null;
        private final ArrayList<Instrumentation> mInstrumentations =
            new ArrayList<Instrumentation>();
        private final ArrayList<String> mLibraries = new ArrayList<String>();
        private IJavaProject mJavaProject;
        private boolean mGatherData = false;
        private boolean mMarkErrors = false;
        private int mCurrentLevel = 0;
        private int mValidLevel = 0;
        private Activity mCurrentActivity = null;
        private Locator mLocator;
        ManifestHandler(IFile manifestFile, XmlErrorListener errorListener,
                boolean gatherData, IJavaProject javaProject, boolean markErrors) {
            super(manifestFile, errorListener);
            mGatherData = gatherData;
            mJavaProject = javaProject;
            mMarkErrors = markErrors;
        }
        String getPackage() {
            return mPackage;
        }
        Activity[] getActivities() {
            return mActivities.toArray(new Activity[mActivities.size()]);
        }
        Activity getLauncherActivity() {
            return mLauncherActivity;
        }
        String[] getProcesses() {
            if (mProcesses != null) {
                return mProcesses.toArray(new String[mProcesses.size()]);
            }
            return new String[0];
        }
        Boolean getDebuggable() {
            return mDebuggable;
        }
        String getApiLevelRequirement() {
            return mApiLevelRequirement;
        }
        Instrumentation[] getInstrumentations() {
            return mInstrumentations.toArray(new Instrumentation[mInstrumentations.size()]);
        }
        String[] getUsesLibraries() {
            return mLibraries.toArray(new String[mLibraries.size()]);
        }
        @Override
        public void setDocumentLocator(Locator locator) {
            mLocator = locator;
            super.setDocumentLocator(locator);
        }
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {
            try {
                if (mGatherData == false) {
                    return;
                }
                if (mValidLevel == mCurrentLevel) {
                    String value;
                    switch (mValidLevel) {
                        case LEVEL_MANIFEST:
                            if (AndroidManifest.NODE_MANIFEST.equals(localName)) {
                                mPackage = getAttributeValue(attributes,
                                        AndroidManifest.ATTRIBUTE_PACKAGE,
                                        false );
                                mValidLevel++;
                            }
                            break;
                        case LEVEL_APPLICATION:
                            if (AndroidManifest.NODE_APPLICATION.equals(localName)) {
                                value = getAttributeValue(attributes,
                                        AndroidManifest.ATTRIBUTE_PROCESS,
                                        true );
                                if (value != null) {
                                    addProcessName(value);
                                }
                                value = getAttributeValue(attributes,
                                        AndroidManifest.ATTRIBUTE_DEBUGGABLE,
                                        true );
                                if (value != null) {
                                    mDebuggable = Boolean.parseBoolean(value);
                                }
                                mValidLevel++;
                            } else if (AndroidManifest.NODE_USES_SDK.equals(localName)) {
                                mApiLevelRequirement = getAttributeValue(attributes,
                                        AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION,
                                        true );
                            } else if (AndroidManifest.NODE_INSTRUMENTATION.equals(localName)) {
                                processInstrumentationNode(attributes);
                            }
                            break;
                        case LEVEL_ACTIVITY:
                            if (AndroidManifest.NODE_ACTIVITY.equals(localName)) {
                                processActivityNode(attributes);
                                mValidLevel++;
                            } else if (AndroidManifest.NODE_SERVICE.equals(localName)) {
                                processNode(attributes, AndroidConstants.CLASS_SERVICE);
                                mValidLevel++;
                            } else if (AndroidManifest.NODE_RECEIVER.equals(localName)) {
                                processNode(attributes, AndroidConstants.CLASS_BROADCASTRECEIVER);
                                mValidLevel++;
                            } else if (AndroidManifest.NODE_PROVIDER.equals(localName)) {
                                processNode(attributes, AndroidConstants.CLASS_CONTENTPROVIDER);
                                mValidLevel++;
                            } else if (AndroidManifest.NODE_USES_LIBRARY.equals(localName)) {
                                value = getAttributeValue(attributes,
                                        AndroidManifest.ATTRIBUTE_NAME,
                                        true );
                                if (value != null) {
                                    mLibraries.add(value);
                                }
                            }
                            break;
                        case LEVEL_INTENT_FILTER:
                            if (mCurrentActivity != null &&
                                    AndroidManifest.NODE_INTENT.equals(localName)) {
                                mCurrentActivity.resetIntentFilter();
                                mValidLevel++;
                            }
                            break;
                        case LEVEL_CATEGORY:
                            if (mCurrentActivity != null) {
                                if (AndroidManifest.NODE_ACTION.equals(localName)) {
                                    String action = getAttributeValue(attributes,
                                            AndroidManifest.ATTRIBUTE_NAME,
                                            true );
                                    if (action != null) {
                                        mCurrentActivity.setHasAction(true);
                                        mCurrentActivity.setHasMainAction(
                                                ACTION_MAIN.equals(action));
                                    }
                                } else if (AndroidManifest.NODE_CATEGORY.equals(localName)) {
                                    String category = getAttributeValue(attributes,
                                            AndroidManifest.ATTRIBUTE_NAME,
                                            true );
                                    if (CATEGORY_LAUNCHER.equals(category)) {
                                        mCurrentActivity.setHasLauncherCategory(true);
                                    }
                                }
                            }
                            break;
                    }
                }
                mCurrentLevel++;
            } finally {
                super.startElement(uri, localName, name, attributes);
            }
        }
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            try {
                if (mGatherData == false) {
                    return;
                }
                if (mValidLevel == mCurrentLevel) {
                    mValidLevel--;
                }
                mCurrentLevel--;
                if (mValidLevel == mCurrentLevel) {
                    switch (mValidLevel) {
                        case LEVEL_ACTIVITY:
                            mCurrentActivity = null;
                            break;
                        case LEVEL_INTENT_FILTER:
                            if (mLauncherActivity == null &&
                                    mCurrentActivity != null &&
                                    mCurrentActivity.isHomeActivity() &&
                                    mCurrentActivity.isExported()) {
                                mLauncherActivity = mCurrentActivity;
                            }
                            break;
                        default:
                            break;
                    }
                }
            } finally {
                super.endElement(uri, localName, name);
            }
        }
        @Override
        public void error(SAXParseException e) {
            if (mMarkErrors) {
                handleError(e, e.getLineNumber());
            }
        }
        @Override
        public void fatalError(SAXParseException e) {
            if (mMarkErrors) {
                handleError(e, e.getLineNumber());
            }
        }
        @Override
        public void warning(SAXParseException e) throws SAXException {
            if (mMarkErrors) {
                super.warning(e);
            }
        }
        private void processActivityNode(Attributes attributes) {
            String activityName = getAttributeValue(attributes, AndroidManifest.ATTRIBUTE_NAME,
                    true );
            if (activityName != null) {
                activityName = AndroidManifest.combinePackageAndClassName(mPackage, activityName);
                String exportedStr = getAttributeValue(attributes,
                        AndroidManifest.ATTRIBUTE_EXPORTED, true);
                boolean exported = exportedStr == null ||
                        exportedStr.toLowerCase().equals("true"); 
                mCurrentActivity = new Activity(activityName, exported);
                mActivities.add(mCurrentActivity);
                if (mMarkErrors) {
                    checkClass(activityName, AndroidConstants.CLASS_ACTIVITY,
                            true );
                }
            } else {
                mCurrentActivity = null;
            }
            String processName = getAttributeValue(attributes, AndroidManifest.ATTRIBUTE_PROCESS,
                    true );
            if (processName != null) {
                addProcessName(processName);
            }
        }
        private void processNode(Attributes attributes, String superClassName) {
            String serviceName = getAttributeValue(attributes, AndroidManifest.ATTRIBUTE_NAME,
                    true );
            if (serviceName != null) {
                serviceName = AndroidManifest.combinePackageAndClassName(mPackage, serviceName);
                if (mMarkErrors) {
                    checkClass(serviceName, superClassName, false );
                }
            }
            String processName = getAttributeValue(attributes, AndroidManifest.ATTRIBUTE_PROCESS,
                    true );
            if (processName != null) {
                addProcessName(processName);
            }
        }
        private void processInstrumentationNode(Attributes attributes) {
            String instrumentationName = getAttributeValue(attributes,
                    AndroidManifest.ATTRIBUTE_NAME,
                    true );
            if (instrumentationName != null) {
                String instrClassName = AndroidManifest.combinePackageAndClassName(mPackage,
                        instrumentationName);
                String targetPackage = getAttributeValue(attributes,
                        AndroidManifest.ATTRIBUTE_TARGET_PACKAGE,
                        true );
                mInstrumentations.add(new Instrumentation(instrClassName, targetPackage));
                if (mMarkErrors) {
                    checkClass(instrClassName, AndroidConstants.CLASS_INSTRUMENTATION,
                            true );
                }
            }
        }
        private void checkClass(String className, String superClassName, boolean testVisibility) {
            if (mJavaProject == null) {
                return;
            }
            String result = BaseProjectHelper.testClassForManifest(mJavaProject,
                    className, superClassName, testVisibility);
            if (result != BaseProjectHelper.TEST_CLASS_OK) {
                int line = mLocator.getLineNumber();
                IMarker marker = BaseProjectHelper.markResource(getFile(),
                        AndroidConstants.MARKER_ANDROID, result, line, IMarker.SEVERITY_ERROR);
                if (marker != null) {
                    try {
                        marker.setAttribute(AndroidConstants.MARKER_ATTR_TYPE,
                                AndroidConstants.MARKER_ATTR_TYPE_ACTIVITY);
                        marker.setAttribute(AndroidConstants.MARKER_ATTR_CLASS, className);
                    } catch (CoreException e) {
                    }
                }
            }
        }
        private String getAttributeValue(Attributes attributes, String attributeName,
                boolean hasNamespace) {
            int count = attributes.getLength();
            for (int i = 0 ; i < count ; i++) {
                if (attributeName.equals(attributes.getLocalName(i)) &&
                        ((hasNamespace &&
                                SdkConstants.NS_RESOURCES.equals(attributes.getURI(i))) ||
                                (hasNamespace == false && attributes.getURI(i).length() == 0))) {
                    return attributes.getValue(i);
                }
            }
            return null;
        }
        private void addProcessName(String processName) {
            if (mProcesses == null) {
                mProcesses = new TreeSet<String>();
            }
            mProcesses.add(processName);
        }
    }
    private static SAXParserFactory sParserFactory;
    private final String mJavaPackage;
    private final Activity[] mActivities;
    private final Activity mLauncherActivity;
    private final String[] mProcesses;
    private final Boolean mDebuggable;
    private final String mApiLevelRequirement;
    private final Instrumentation[] mInstrumentations;
    private final String[] mLibraries;
    static {
        sParserFactory = SAXParserFactory.newInstance();
        sParserFactory.setNamespaceAware(true);
    }
    public static AndroidManifestParser parse(
                IJavaProject javaProject,
                IFile manifestFile,
                XmlErrorListener errorListener,
                boolean gatherData,
                boolean markErrors)
            throws CoreException {
        try {
            if (manifestFile != null) {
                SAXParser parser = sParserFactory.newSAXParser();
                ManifestHandler manifestHandler = new ManifestHandler(manifestFile,
                        errorListener, gatherData, javaProject, markErrors);
                parser.parse(new InputSource(manifestFile.getContents()), manifestHandler);
                return new AndroidManifestParser(manifestHandler.getPackage(),
                        manifestHandler.getActivities(),
                        manifestHandler.getLauncherActivity(),
                        manifestHandler.getProcesses(),
                        manifestHandler.getDebuggable(),
                        manifestHandler.getApiLevelRequirement(),
                        manifestHandler.getInstrumentations(),
                        manifestHandler.getUsesLibraries());
            }
        } catch (ParserConfigurationException e) {
            AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                    "Bad parser configuration for %s: %s",
                    manifestFile.getFullPath(),
                    e.getMessage());
        } catch (SAXException e) {
            AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                    "Parser exception for %s: %s",
                    manifestFile.getFullPath(),
                    e.getMessage());
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                        "I/O error for %s: %s",
                        manifestFile.getFullPath(),
                        e.getMessage());
            }
        }
        return null;
    }
    private static AndroidManifestParser parse(File manifestFile)
            throws CoreException {
        try {
            SAXParser parser = sParserFactory.newSAXParser();
            ManifestHandler manifestHandler = new ManifestHandler(
                    null, 
                    null, 
                    true, 
                    null, 
                    false 
                    );
            parser.parse(new InputSource(new FileReader(manifestFile)), manifestHandler);
            return new AndroidManifestParser(manifestHandler.getPackage(),
                    manifestHandler.getActivities(),
                    manifestHandler.getLauncherActivity(),
                    manifestHandler.getProcesses(),
                    manifestHandler.getDebuggable(),
                    manifestHandler.getApiLevelRequirement(),
                    manifestHandler.getInstrumentations(),
                    manifestHandler.getUsesLibraries());
        } catch (ParserConfigurationException e) {
            AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                    "Bad parser configuration for %s: %s",
                    manifestFile.getAbsolutePath(),
                    e.getMessage());
        } catch (SAXException e) {
            AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                    "Parser exception for %s: %s",
                    manifestFile.getAbsolutePath(),
                    e.getMessage());
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                        "I/O error for %s: %s",
                        manifestFile.getAbsolutePath(),
                        e.getMessage());
            }
        }
        return null;
    }
    public static AndroidManifestParser parse(
                IJavaProject javaProject,
                XmlErrorListener errorListener,
                boolean gatherData,
                boolean markErrors)
            throws CoreException {
        IFile manifestFile = getManifest(javaProject.getProject());
        try {
            SAXParser parser = sParserFactory.newSAXParser();
            if (manifestFile != null) {
                ManifestHandler manifestHandler = new ManifestHandler(manifestFile,
                        errorListener, gatherData, javaProject, markErrors);
                parser.parse(new InputSource(manifestFile.getContents()), manifestHandler);
                return new AndroidManifestParser(manifestHandler.getPackage(),
                        manifestHandler.getActivities(), manifestHandler.getLauncherActivity(),
                        manifestHandler.getProcesses(), manifestHandler.getDebuggable(),
                        manifestHandler.getApiLevelRequirement(),
                        manifestHandler.getInstrumentations(), manifestHandler.getUsesLibraries());
            }
        } catch (ParserConfigurationException e) {
            AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                    "Bad parser configuration for %s", manifestFile.getFullPath());
        } catch (SAXException e) {
            AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                    "Parser exception for %s", manifestFile.getFullPath());
        } catch (IOException e) {
            AdtPlugin.logAndPrintError(e, AndroidManifestParser.class.getCanonicalName(),
                    "I/O error for %s", manifestFile.getFullPath());
        }
        return null;
    }
    public static AndroidManifestParser parseForError(IJavaProject javaProject, IFile manifestFile,
            XmlErrorListener errorListener) throws CoreException {
        return parse(javaProject, manifestFile, errorListener, true, true);
    }
    public static AndroidManifestParser parseForData(IFile manifestFile) throws CoreException {
        return parse(null , manifestFile, null ,
                true , false );
    }
    public static AndroidManifestParser parseForData(String osManifestFilePath) {
        try {
            return parse(new File(osManifestFilePath));
        } catch (CoreException e) {
            return null;
        }
    }
    public String getPackage() {
        return mJavaPackage;
    }
    public Activity[] getActivities() {
        return mActivities;
    }
    public Activity getLauncherActivity() {
        return mLauncherActivity;
    }
    public String[] getProcesses() {
        return mProcesses;
    }
    public Boolean getDebuggable() {
        return mDebuggable;
    }
    public String getApiLevelRequirement() {
        return mApiLevelRequirement;
    }
    public Instrumentation[] getInstrumentations() {
        return mInstrumentations;
    }
    public String[] getUsesLibraries() {
        return mLibraries;
    }
    private AndroidManifestParser(String javaPackage, Activity[] activities,
            Activity launcherActivity, String[] processes, Boolean debuggable,
            String apiLevelRequirement, Instrumentation[] instrumentations, String[] libraries) {
        mJavaPackage = javaPackage;
        mActivities = activities;
        mLauncherActivity = launcherActivity;
        mProcesses = processes;
        mDebuggable = debuggable;
        mApiLevelRequirement = apiLevelRequirement;
        mInstrumentations = instrumentations;
        mLibraries = libraries;
    }
    public static IFile getManifest(IProject project) {
        IResource r = project.findMember(AndroidConstants.WS_SEP
                + AndroidConstants.FN_ANDROID_MANIFEST);
        if (r == null || r.exists() == false || (r instanceof IFile) == false) {
            return null;
        }
        return (IFile) r;
    }
}
