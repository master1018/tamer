public class Policy {
    private Policy() {}
    static final String PRELOADED_CLASS_FILE
            = "frameworks/base/preloaded-classes";
    private static final Set<String> SERVICES = new HashSet<String>(Arrays.asList(
        "system_server",
        "com.google.process.content",
        "android.process.media",
        "com.android.bluetooth",
        "com.android.calendar",
        "com.android.inputmethod.latin",
        "com.android.phone",
        "com.google.android.apps.maps.FriendService", 
        "com.google.android.apps.maps:FriendService", 
        "com.google.android.apps.maps.LocationFriendService",
        "com.google.android.deskclock",
        "com.google.process.gapps",
        "android.tts"
    ));
    private static final Set<String> EXCLUDED_CLASSES
            = new HashSet<String>(Arrays.asList(
        "android.app.AlarmManager",
        "android.app.SearchManager",
        "android.os.FileObserver",
        "com.android.server.PackageManagerService$AppDirObserver",
        "android.os.AsyncTask",
        "android.pim.ContactsAsyncHelper",
        "java.lang.ProcessManager"
    ));
    public static boolean isService(String processName) {
        return SERVICES.contains(processName);
    }
    public static boolean isPreloadable(LoadedClass clazz) {
        return clazz.systemClass && !EXCLUDED_CLASSES.contains(clazz.name);
    }
}
