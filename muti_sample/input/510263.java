public class MethodRef {
    private String mDeclClass, mReturnType, mMethodName;
    private String[] mArgTypes;
    public MethodRef(String declClass, String[] argTypes, String returnType,
            String methodName) {
        mDeclClass = declClass;
        mArgTypes = argTypes;
        mReturnType = returnType;
        mMethodName = methodName;
    }
    public String getDeclClassName() {
        return mDeclClass;
    }
    public String getDescriptor() {
        return descriptorFromProtoArray(mArgTypes, mReturnType);
    }
    public String getName() {
        return mMethodName;
    }
    public String[] getArgumentTypeNames() {
        return mArgTypes;
    }
    public String getReturnTypeName() {
        return mReturnType;
    }
    private static String descriptorFromProtoArray(String[] protos,
            String returnType) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < protos.length; i++) {
            builder.append(protos[i]);
        }
        builder.append(")");
        builder.append(returnType);
        return builder.toString();
    }
}
