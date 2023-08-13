public class IIOMetadataUtils {
    private IIOMetadataUtils() {} 
    public static IIOMetadataFormat instantiateMetadataFormat(
            String formatName, boolean standardFormatSupported,
            String nativeMetadataFormatName, String nativeMetadataFormatClassName,
            String [] extraMetadataFormatNames, String [] extraMetadataFormatClassNames
    ) {
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        if (formatName.equals(IIOMetadataFormatImpl.standardMetadataFormatName)) {
            if (standardFormatSupported) {
                return IIOMetadataFormatImpl.getStandardFormatInstance();
            }
        }
        String className = null;
        if (formatName.equals(nativeMetadataFormatName)) {
            className = nativeMetadataFormatClassName;
        } else if (extraMetadataFormatNames != null) {
            for (int i = 0; i < extraMetadataFormatNames.length; i++) {
                if (formatName.equals(extraMetadataFormatNames[i])) {
                    className = extraMetadataFormatClassNames[i];
                    break;
                }
            }
        }
        if (className == null) {
            throw new IllegalArgumentException("Unsupported format name");
        }
        ClassLoader contextClassloader = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
        });
        Class cls;
        try {
            cls = Class.forName(className, true, contextClassloader);
        } catch (ClassNotFoundException e) {
            try {
                cls = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException ("Can't obtain format");
            }
        }
        try {
            return null;
        } catch (Exception e) {
            IllegalStateException e1 = new IllegalStateException("Can't obtain format");
            e1.initCause(e); 
            throw e1;
        }
    }
}
