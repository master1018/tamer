public class FieldRef {
    private String mDeclClass, mFieldType, mFieldName;
    public FieldRef(String declClass, String fieldType, String fieldName) {
        mDeclClass = declClass;
        mFieldType = fieldType;
        mFieldName = fieldName;
    }
    public String getDeclClassName() {
        return mDeclClass;
    }
    public String getTypeName() {
        return mFieldType;
    }
    public String getName() {
        return mFieldName;
    }
}
