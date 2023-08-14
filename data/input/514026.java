public final class AndroidManifest {
    public final static String NODE_MANIFEST = "manifest"; 
    public final static String NODE_APPLICATION = "application"; 
    public final static String NODE_ACTIVITY = "activity"; 
    public final static String NODE_SERVICE = "service"; 
    public final static String NODE_RECEIVER = "receiver"; 
    public final static String NODE_PROVIDER = "provider"; 
    public final static String NODE_INTENT = "intent-filter"; 
    public final static String NODE_ACTION = "action"; 
    public final static String NODE_CATEGORY = "category"; 
    public final static String NODE_USES_SDK = "uses-sdk"; 
    public final static String NODE_INSTRUMENTATION = "instrumentation"; 
    public final static String NODE_USES_LIBRARY = "uses-library"; 
    public final static String ATTRIBUTE_PACKAGE = "package"; 
    public final static String ATTRIBUTE_NAME = "name"; 
    public final static String ATTRIBUTE_PROCESS = "process"; 
    public final static String ATTRIBUTE_DEBUGGABLE = "debuggable"; 
    public final static String ATTRIBUTE_MIN_SDK_VERSION = "minSdkVersion"; 
    public final static String ATTRIBUTE_TARGET_PACKAGE = "targetPackage"; 
    public final static String ATTRIBUTE_EXPORTED = "exported"; 
    public static String getPackage(IAbstractFolder projectFolder)
            throws XPathExpressionException, StreamException {
        IAbstractFile file = projectFolder.getFile(SdkConstants.FN_ANDROID_MANIFEST_XML);
        return getPackage(file);
    }
    public static String getPackage(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        XPath xPath = AndroidXPathFactory.newXPath();
        return xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/@" + ATTRIBUTE_PACKAGE,
                new InputSource(manifestFile.getContents()));
    }
    public static String combinePackageAndClassName(String javaPackage, String className) {
        if (className == null || className.length() == 0) {
            return javaPackage;
        }
        if (javaPackage == null || javaPackage.length() == 0) {
            return className;
        }
        boolean startWithDot = (className.charAt(0) == '.');
        boolean hasDot = (className.indexOf('.') != -1);
        if (startWithDot || hasDot == false) {
            if (startWithDot) {
                return javaPackage + className;
            } else {
                return javaPackage + '.' + className;
            }
        } else {
            return className;
        }
    }
    public static String extractActivityName(String fullActivityName, String packageName) {
        if (packageName != null && fullActivityName != null) {
            if (packageName.length() > 0 && fullActivityName.startsWith(packageName)) {
                String name = fullActivityName.substring(packageName.length());
                if (name.length() > 0 && name.charAt(0) == '.') {
                    return name;
                }
            }
        }
        return fullActivityName;
    }
}
