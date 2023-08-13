class ResourceValue implements IResourceValue {
    private final String mType;
    private final String mName;
    private String mValue = null;
    ResourceValue(String name) {
        mType = null;
        mName = name;
    }
    public ResourceValue(String type, String name, String value) {
        mType = type;
        mName = name;
        mValue = value;
    }
    public String getType() {
        return mType;
    }
    public final String getName() {
        return mName;
    }
    public final String getValue() {
        return mValue;
    }
    public final void setValue(String value) {
        mValue = value;
    }
    public void replaceWith(ResourceValue value) {
        mValue = value.mValue;
    }
    public boolean isFramework() {
        return false;
    }
}
