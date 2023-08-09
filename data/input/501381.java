public class AndroidConstants {
    public static final String EDITORS_NAMESPACE = "com.android.ide.eclipse.editors"; 
    public final static String NATURE = "com.android.ide.eclipse.adt.AndroidNature"; 
    public final static String WS_SEP = "/"; 
    public final static char WS_SEP_CHAR = '/';
    public final static String EXT_ANDROID_PACKAGE = "apk"; 
    public final static String EXT_JAVA = "java"; 
    public final static String EXT_CLASS = "class"; 
    public final static String EXT_XML = "xml"; 
    public final static String EXT_JAR = "jar"; 
    public final static String EXT_AIDL = "aidl"; 
    public final static String EXT_NATIVE_LIB = "so"; 
    private final static String DOT = "."; 
    public final static String DOT_ANDROID_PACKAGE = DOT + EXT_ANDROID_PACKAGE;
    public final static String DOT_JAVA = DOT + EXT_JAVA;
    public final static String DOT_CLASS = DOT + EXT_CLASS;
    public final static String DOT_XML = DOT + EXT_XML;
    public final static String DOT_JAR = DOT + EXT_JAR;
    public final static String DOT_AIDL = DOT + EXT_AIDL;
    public static final String FN_ANDROID_MANIFEST = "AndroidManifest.xml"; 
    public static final String FD_ANDROID_SOURCES = "sources"; 
    public final static String FN_RESOURCE_CLASS = "R.java"; 
    public final static String FN_COMPILED_RESOURCE_CLASS = "R.class"; 
    public final static String FN_MANIFEST_CLASS = "Manifest.java"; 
    public final static String FN_CLASSES_DEX = "classes.dex"; 
    public final static String FN_RESOURCES_AP_ = "resources.ap_"; 
    public final static String FN_RESOURCES_S_AP_ = "resources-%s.ap_"; 
    public final static Pattern PATTERN_RESOURCES_S_AP_ =
        Pattern.compile("resources-.*\\.ap_", Pattern.CASE_INSENSITIVE); 
    public final static String FN_TRACEVIEW =
        (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS) ?
            "traceview.exe" : "traceview"; 
    public final static String WS_ROOT = WS_SEP;
    public final static String WS_RESOURCES = WS_SEP + SdkConstants.FD_RESOURCES;
    public final static String WS_ASSETS = WS_SEP + SdkConstants.FD_ASSETS;
    public final static String WS_JAVADOC_FOLDER_LEAF = SdkConstants.FD_DOCS + "/" + 
            SdkConstants.FD_DOCS_REFERENCE;
    public final static String OS_SDK_SAMPLES_FOLDER = SdkConstants.FD_SAMPLES + File.separator;
    public final static String RE_DOT = "\\."; 
    public final static String RE_JAVA_EXT = "\\.java$"; 
    public final static String RE_AIDL_EXT = "\\.aidl$"; 
    private static final String LEGACY_PLUGIN_ID = "com.android.ide.eclipse.common"; 
    public final static String MARKER_ADT = AdtPlugin.PLUGIN_ID + ".adtProblem"; 
    public final static String MARKER_TARGET = AdtPlugin.PLUGIN_ID + ".targetProblem"; 
    public final static String MARKER_AAPT_COMPILE = LEGACY_PLUGIN_ID + ".aaptProblem"; 
    public final static String MARKER_XML = LEGACY_PLUGIN_ID + ".xmlProblem"; 
    public final static String MARKER_AIDL = LEGACY_PLUGIN_ID + ".aidlProblem"; 
    public final static String MARKER_ANDROID = LEGACY_PLUGIN_ID + ".androidProblem"; 
    public final static String MARKER_AAPT_PACKAGE = LEGACY_PLUGIN_ID + ".aapt2Problem"; 
    public final static String MARKER_PACKAGING = AdtPlugin.PLUGIN_ID + ".packagingProblem"; 
    public final static String MARKER_ATTR_TYPE = "android.type"; 
    public final static String MARKER_ATTR_CLASS = "android.class"; 
    public final static String MARKER_ATTR_TYPE_ACTIVITY = "activity"; 
    public final static String MARKER_ATTR_TYPE_SERVICE = "service"; 
    public final static String MARKER_ATTR_TYPE_RECEIVER = "receiver"; 
    public final static String MARKER_ATTR_TYPE_PROVIDER = "provider"; 
    public final static String CLASS_ACTIVITY = "android.app.Activity"; 
    public final static String CLASS_SERVICE = "android.app.Service"; 
    public final static String CLASS_BROADCASTRECEIVER = "android.content.BroadcastReceiver"; 
    public final static String CLASS_CONTENTPROVIDER = "android.content.ContentProvider"; 
    public final static String CLASS_INSTRUMENTATION = "android.app.Instrumentation"; 
    public final static String CLASS_INSTRUMENTATION_RUNNER =
        "android.test.InstrumentationTestRunner"; 
    public final static String CLASS_BUNDLE = "android.os.Bundle"; 
    public final static String CLASS_R = "android.R"; 
    public final static String CLASS_MANIFEST_PERMISSION = "android.Manifest$permission"; 
    public final static String CLASS_INTENT = "android.content.Intent"; 
    public final static String CLASS_CONTEXT = "android.content.Context"; 
    public final static String CLASS_VIEW = "android.view.View"; 
    public final static String CLASS_VIEWGROUP = "android.view.ViewGroup"; 
    public final static String CLASS_NAME_LAYOUTPARAMS = "LayoutParams"; 
    public final static String CLASS_VIEWGROUP_LAYOUTPARAMS =
        CLASS_VIEWGROUP + "$" + CLASS_NAME_LAYOUTPARAMS; 
    public final static String CLASS_NAME_FRAMELAYOUT = "FrameLayout"; 
    public final static String CLASS_FRAMELAYOUT =
        "android.widget." + CLASS_NAME_FRAMELAYOUT; 
    public final static String CLASS_PREFERENCE = "android.preference.Preference"; 
    public final static String CLASS_NAME_PREFERENCE_SCREEN = "PreferenceScreen"; 
    public final static String CLASS_PREFERENCES =
        "android.preference." + CLASS_NAME_PREFERENCE_SCREEN; 
    public final static String CLASS_PREFERENCEGROUP = "android.preference.PreferenceGroup"; 
    public final static String CLASS_PARCELABLE = "android.os.Parcelable"; 
    public final static String CLASS_BRIDGE = "com.android.layoutlib.bridge.Bridge"; 
    public final static String COMPILER_COMPLIANCE_PREFERRED = "1.5"; 
    public final static String[] COMPILER_COMPLIANCE = {
        "1.5", 
        "1.6", 
    };
    public static final String CODESITE_BASE_URL = "http:
    public static final String LIBRARY_TEST_RUNNER = "android.test.runner"; 
}
