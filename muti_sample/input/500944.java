public final class SdkConstants {
    public final static int PLATFORM_UNKNOWN = 0;
    public final static int PLATFORM_LINUX = 1;
    public final static int PLATFORM_WINDOWS = 2;
    public final static int PLATFORM_DARWIN = 3;
    public final static int CURRENT_PLATFORM = currentPlatform();
    public final static String INI_CHARSET = "UTF-8";
    public static final String FN_ANDROID_MANIFEST_XML= "AndroidManifest.xml";
    public final static String FN_BUILD_XML = "build.xml";
    public static final String FN_FRAMEWORK_LIBRARY = "android.jar";
    public static final String FN_ATTRS_XML = "attrs.xml";
    public static final String FN_ATTRS_MANIFEST_XML = "attrs_manifest.xml";
    public static final String FN_FRAMEWORK_AIDL = "framework.aidl";
    public static final String FN_LAYOUTLIB_JAR = "layoutlib.jar";
    public static final String FN_WIDGETS = "widgets.txt";
    public static final String FN_INTENT_ACTIONS_ACTIVITY = "activity_actions.txt";
    public static final String FN_INTENT_ACTIONS_BROADCAST = "broadcast_actions.txt";
    public static final String FN_INTENT_ACTIONS_SERVICE = "service_actions.txt";
    public static final String FN_INTENT_CATEGORIES = "categories.txt";
    public final static String FN_BUILD_PROP = "build.prop";
    public final static String FN_PLUGIN_PROP = "plugin.prop";
    public final static String FN_MANIFEST_INI = "manifest.ini";
    public final static String FN_DEVICES_XML = "devices.xml";
    public final static String FN_HARDWARE_INI = "hardware-properties.ini";
    public final static String FN_DEFAULT_PROPERTIES = "default.properties";
    public final static String FN_SKIN_LAYOUT = "layout";
    public static final String FN_DX_JAR = "dx.jar"; 
    public final static String FN_DX = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "dx.bat" : "dx"; 
    public final static String FN_AAPT = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "aapt.exe" : "aapt"; 
    public final static String FN_AIDL = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "aidl.exe" : "aidl"; 
    public final static String FN_ADB = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "adb.exe" : "adb"; 
    public final static String FN_EMULATOR = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "emulator.exe" : "emulator"; 
    public final static String FN_ZIPALIGN = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "zipalign.exe" : "zipalign"; 
    public final static String FN_SOURCE_PROP = "source.properties"; 
    public final static String FN_CONTENT_HASH_PROP = "content_hash.properties"; 
    public final static String FN_SDK_PROP = "sdk.properties"; 
    public final static String FD_RESOURCES = "res"; 
    public final static String FD_ASSETS = "assets"; 
    public final static String FD_SOURCES = "src"; 
    public final static String FD_GEN_SOURCES = "gen"; 
    public final static String FD_NATIVE_LIBS = "libs"; 
    public final static String FD_APK_NATIVE_LIBS = "lib"; 
    public final static String FD_OUTPUT = "bin"; 
    public final static String FD_ANIM = "anim"; 
    public final static String FD_COLOR = "color"; 
    public final static String FD_DRAWABLE = "drawable"; 
    public final static String FD_LAYOUT = "layout"; 
    public final static String FD_MENU = "menu"; 
    public final static String FD_VALUES = "values"; 
    public final static String FD_XML = "xml"; 
    public final static String FD_RAW = "raw"; 
    public final static String FD_PLATFORMS = "platforms";
    public final static String FD_ADDONS = "add-ons";
    public final static String FD_TOOLS = "tools";
    public final static String FD_LIB = "lib";
    public final static String FD_DOCS = "docs";
    public static final String FD_DOCS_REFERENCE = "reference";
    public final static String FD_IMAGES = "images";
    public final static String FD_SKINS = "skins";
    public final static String FD_SAMPLES = "samples";
    public final static String FD_TEMPLATES = "templates";
    public final static String FD_ANT = "ant";
    public final static String FD_DATA = "data";
    public final static String FD_RES = "res";
    public final static String FD_FONTS = "fonts";
    public static final String FD_ANDROID_SOURCES = "sources";
    public final static String FD_ADDON_LIBS = "libs";
    public final static String ANDROID_TEST_RUNNER_LIB = "android.test.runner";
    public final static String OS_SDK_DOCS_FOLDER = FD_DOCS + File.separator;
    public final static String OS_SDK_TOOLS_FOLDER = FD_TOOLS + File.separator;
    public final static String OS_SDK_TOOLS_LIB_FOLDER =
            OS_SDK_TOOLS_FOLDER + FD_LIB + File.separator;
    public final static String OS_IMAGES_FOLDER = FD_IMAGES + File.separator;
    public final static String OS_SKINS_FOLDER = FD_SKINS + File.separator;
    public final static String OS_PLATFORM_DATA_FOLDER = FD_DATA + File.separator;
    public final static String OS_PLATFORM_SAMPLES_FOLDER = FD_SAMPLES + File.separator;
    public final static String OS_PLATFORM_RESOURCES_FOLDER =
            OS_PLATFORM_DATA_FOLDER + FD_RES + File.separator;
    public final static String OS_PLATFORM_FONTS_FOLDER =
            OS_PLATFORM_DATA_FOLDER + FD_FONTS + File.separator;
    public final static String OS_PLATFORM_SOURCES_FOLDER = FD_ANDROID_SOURCES + File.separator;
    public final static String OS_PLATFORM_TEMPLATES_FOLDER = FD_TEMPLATES + File.separator;
    public final static String OS_PLATFORM_ANT_FOLDER = FD_ANT + File.separator;
    public final static String OS_PLATFORM_ATTRS_XML =
            OS_PLATFORM_RESOURCES_FOLDER + FD_VALUES + File.separator + FN_ATTRS_XML;
    public final static String OS_PLATFORM_ATTRS_MANIFEST_XML =
            OS_PLATFORM_RESOURCES_FOLDER + FD_VALUES + File.separator + FN_ATTRS_MANIFEST_XML;
    public final static String OS_PLATFORM_LAYOUTLIB_JAR =
            OS_PLATFORM_DATA_FOLDER + FN_LAYOUTLIB_JAR;
    public final static String OS_ADDON_LIBS_FOLDER = FD_ADDON_LIBS + File.separator;
    public final static String SKIN_DEFAULT = "default";
    public final static String PROP_SDK_SUPPORT_LIBRARY = "sdk.build.support.library"; 
    public final static String PROP_SDK_ANT_BUILD_REVISION = "sdk.ant.build.revision"; 
    public final static String PROP_SDK_ANT_TEMPLATES_REVISION = "sdk.ant.templates.revision"; 
    public static String androidCmdName() {
        String os = System.getProperty("os.name");
        String cmd = "android";
        if (os.startsWith("Windows")) {
            cmd += ".bat";
        }
        return cmd;
    }
    public static String mkSdCardCmdName() {
        String os = System.getProperty("os.name");
        String cmd = "mksdcard";
        if (os.startsWith("Windows")) {
            cmd += ".exe";
        }
        return cmd;
    }
    public static int currentPlatform() {
        String os = System.getProperty("os.name");          
        if (os.startsWith("Mac OS")) {                      
            return PLATFORM_DARWIN;
        } else if (os.startsWith("Windows")) {              
            return PLATFORM_WINDOWS;
        } else if (os.startsWith("Linux")) {                
            return PLATFORM_LINUX;
        }
        return PLATFORM_UNKNOWN;
    }
}
