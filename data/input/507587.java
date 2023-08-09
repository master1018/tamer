public final class StackTraceElement implements Serializable {
    private static final long serialVersionUID = 6992337162326171013L;
    private static final int NATIVE_LINE_NUMBER = -2;
    String declaringClass;
    String methodName;
    String fileName;
    int lineNumber;
    public StackTraceElement(String cls, String method, String file, int line) {
        super();
        if (cls == null || method == null) {
            throw new NullPointerException();
        }
        declaringClass = cls;
        methodName = method;
        fileName = file;
        lineNumber = line;
    }
    private StackTraceElement() {
        super();
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StackTraceElement)) {
            return false;
        }
        StackTraceElement castObj = (StackTraceElement) obj;
        if ((methodName == null) || (castObj.methodName == null)) {
            return false;
        }
        if (!getMethodName().equals(castObj.getMethodName())) {
            return false;
        }
        if (!getClassName().equals(castObj.getClassName())) {
            return false;
        }
        String localFileName = getFileName();
        if (localFileName == null) {
            if (castObj.getFileName() != null) {
                return false;
            }
        } else {
            if (!localFileName.equals(castObj.getFileName())) {
                return false;
            }
        }
        if (getLineNumber() != castObj.getLineNumber()) {
            return false;
        }
        return true;
    }
    public String getClassName() {
        return (declaringClass == null) ? "<unknown class>" : declaringClass;
    }
    public String getFileName() {
        return fileName;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    public String getMethodName() {
        return (methodName == null) ? "<unknown method>" : methodName;
    }
    @Override
    public int hashCode() {
        if (methodName == null) {
            return 0;
        }
        return methodName.hashCode() ^ declaringClass.hashCode();
    }
    public boolean isNativeMethod() {
        return lineNumber == NATIVE_LINE_NUMBER;
    }
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(80);
        buf.append(getClassName());
        buf.append('.');
        buf.append(getMethodName());
        if (isNativeMethod()) {
            buf.append("(Native Method)");
        } else {
            String fName = getFileName();
            if (fName == null) {
                buf.append("(Unknown Source)");
            } else {
                int lineNum = getLineNumber();
                buf.append('(');
                buf.append(fName);
                if (lineNum >= 0) {
                    buf.append(':');
                    buf.append(lineNum);
                }
                buf.append(')');
            }
        }
        return buf.toString();
    }
}
