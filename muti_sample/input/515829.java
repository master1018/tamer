public class ViewClassInfo {
    private boolean mIsLayout;
    private String mFullClassName;
    private String mShortClassName;
    private ViewClassInfo mSuperClass;
    private String mJavaDoc;
    private AttributeInfo[] mAttributes;
    public static class LayoutParamsInfo {
        private String mShortClassName;
        private ViewClassInfo mViewLayoutClass;
        private LayoutParamsInfo mSuperClass;
        private AttributeInfo[] mAttributes;
        public LayoutParamsInfo(ViewClassInfo enclosingViewClassInfo,
                String shortClassName, LayoutParamsInfo superClassInfo) {
            mShortClassName = shortClassName;
            mViewLayoutClass = enclosingViewClassInfo;
            mSuperClass = superClassInfo;
            mAttributes = new AttributeInfo[0];
        }
        public String getShortClassName() {
            return mShortClassName;
        }
        public ViewClassInfo getViewLayoutClass() {
            return mViewLayoutClass;
        }
        public LayoutParamsInfo getSuperClass() {
            return mSuperClass;
        }
        public AttributeInfo[] getAttributes() {
            return mAttributes;
        }
        public void setAttributes(AttributeInfo[] attributes) {
            mAttributes = attributes;
        }
    }
    public LayoutParamsInfo mLayoutData;
    public ViewClassInfo(boolean isLayout, String fullClassName, String shortClassName) {
        mIsLayout = isLayout;
        mFullClassName = fullClassName;
        mShortClassName = shortClassName;
        mAttributes = new AttributeInfo[0];
    }
    public boolean isLayout() {
        return mIsLayout;
    }
    public String getFullClassName() {
        return mFullClassName;
    }
    public String getShortClassName() {
        return mShortClassName;
    }
    public ViewClassInfo getSuperClass() {
        return mSuperClass;
    }
    public String getJavaDoc() {
        return mJavaDoc;
    }
    public AttributeInfo[] getAttributes() {
        return mAttributes;
    }
    public LayoutParamsInfo getLayoutData() {
        return mLayoutData;
    }
    public void setSuperClass(ViewClassInfo superClass) {
        mSuperClass = superClass;
    }
    public void setJavaDoc(String javaDoc) {
        mJavaDoc = javaDoc;
    }
    public void setAttributes(AttributeInfo[] attributes) {
        mAttributes = attributes;
    }
    public void setLayoutParams(LayoutParamsInfo layoutData) {
        if (mIsLayout) {
            mLayoutData = layoutData;
        }
    }
}
