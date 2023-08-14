public final class DdmConstants {
    public final static int PLATFORM_UNKNOWN = 0;
    public final static int PLATFORM_LINUX = 1;
    public final static int PLATFORM_WINDOWS = 2;
    public final static int PLATFORM_DARWIN = 3;
    public final static int CURRENT_PLATFORM = currentPlatform();
    public final static String FN_HPROF_CONVERTER = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "hprof-conv.exe" : "hprof-conv"; 
    public final static String FN_TRACEVIEW = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "traceview.bat" : "traceview"; 
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
