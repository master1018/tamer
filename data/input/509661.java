public final class NativeStackCallInfo {
    private final static Pattern SOURCE_NAME_PATTERN = Pattern.compile("^(.+):(\\d+)$");
    private String mLibrary;
    private String mMethod;
    private String mSourceFile;
    private int mLineNumber = -1; 
    public NativeStackCallInfo(String lib, String method, String sourceFile) {
        mLibrary = lib;
        mMethod = method;
        Matcher m = SOURCE_NAME_PATTERN.matcher(sourceFile);
        if (m.matches()) {
            mSourceFile = m.group(1);
            try {
                mLineNumber = Integer.parseInt(m.group(2));
            } catch (NumberFormatException e) {
            }
        } else {
            mSourceFile = sourceFile;
        }
    }
    public String getLibraryName() {
        return mLibrary;
    }
    public String getMethodName() {
        return mMethod;
    }
    public String getSourceFile() {
        return mSourceFile;
    }
    public int getLineNumber() {
        return mLineNumber;
    }
}
