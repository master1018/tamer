public class ProfileNode {
    private String mLabel;
    private MethodData mMethodData;
    private ProfileData[] mChildren;
    private boolean mIsParent;
    private boolean mIsRecursive;
    public ProfileNode(String label, MethodData methodData,
            ProfileData[] children, boolean isParent, boolean isRecursive) {
        mLabel = label;
        mMethodData = methodData;
        mChildren = children;
        mIsParent = isParent;
        mIsRecursive = isRecursive;
    }
    public String getLabel() {
        return mLabel;
    }
    public ProfileData[] getChildren() {
        return mChildren;
    }
    public boolean isParent() {
        return mIsParent;
    }
    public boolean isRecursive() {
        return mIsRecursive;
    }
}
