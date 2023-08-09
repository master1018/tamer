public class TestController {
    private String mJarPath;
    private String mPackageName;
    private String mClassName;
    private String mMethodName;
    public TestController(final String jarPath, final String packageName,
            final String className, final String methodName) {
        mJarPath = jarPath;
        mPackageName = packageName;
        mClassName = className;
        mMethodName = methodName;
    }
    public String getJarPath() {
        return mJarPath;
    }
    public String getPackageName() {
        return mPackageName;
    }
    public String getClassName() {
        return mClassName;
    }
    public String getMethodName() {
        return mMethodName;
    }
    public String getFullName() {
        return mPackageName + "." + mClassName + Test.METHOD_SEPARATOR + mMethodName;
    }
    public boolean isValid() {
        if ((mJarPath == null) || (mJarPath.length() == 0)
                || (mPackageName == null) || (mPackageName.length() == 0)
                || (mClassName == null) || (mClassName.length() == 0)
                || (mMethodName == null) || (mMethodName.length() == 0)) {
            return false;
        } else {
            return true;
        }
    }
}
