public class DeclareStyleableInfo {
    private String mStyleName;
    private AttributeInfo[] mAttributes;
    private String mJavaDoc;
    private String[] mParents;    
    public static class AttributeInfo {
        private String mName;
        public enum Format {
            STRING,
            BOOLEAN,
            INTEGER,
            FLOAT,
            REFERENCE,
            COLOR,
            DIMENSION,
            FRACTION,
            ENUM,
            FLAG,
        }
        private Format[] mFormats;
        private String[] mEnumValues;
        private String[] mFlagValues;
        private String mJavaDoc;
        private String mDeprecatedDoc;
        public AttributeInfo(String name, Format[] formats) {
            mName = name;
            mFormats = formats;
        }
        public AttributeInfo(String name, Format[] formats, String javadoc) {
            mName = name;
            mFormats = formats;
            mJavaDoc = javadoc;
        }
        public AttributeInfo(AttributeInfo info) {
            mName = info.mName;
            mFormats = info.mFormats;
            mEnumValues = info.mEnumValues;
            mFlagValues = info.mFlagValues;
            mJavaDoc = info.mJavaDoc;
            mDeprecatedDoc = info.mDeprecatedDoc;
        }
        public String getName() {
            return mName;
        }
        public Format[] getFormats() {
            return mFormats;
        }
        public String[] getEnumValues() {
            return mEnumValues;
        }
        public String[] getFlagValues() {
            return mFlagValues;
        }
        public String getJavaDoc() {
            return mJavaDoc;
        }
        public String getDeprecatedDoc() {
            return mDeprecatedDoc;
        }
        public void setEnumValues(String[] values) {
            mEnumValues = values;
        }
        public void setFlagValues(String[] values) {
            mFlagValues = values;
        }
        public void setJavaDoc(String javaDoc) {
            mJavaDoc = javaDoc;
        }
        public void setDeprecatedDoc(String deprecatedDoc) {
            mDeprecatedDoc = deprecatedDoc;
        }
    }
    public DeclareStyleableInfo(String styleName, AttributeInfo[] attributes) {
        mStyleName = styleName;
        mAttributes = attributes == null ? new AttributeInfo[0] : attributes;
    }
    public String getStyleName() {
        return mStyleName;
    }
    public AttributeInfo[] getAttributes() {
        return mAttributes;
    }
    public void setAttributes(AttributeInfo[] attributes) {
        mAttributes = attributes;
    }
    public String getJavaDoc() {
        return mJavaDoc;
    }
    public void setJavaDoc(String javaDoc) {
        mJavaDoc = javaDoc;
    }
    public void setParents(String[] parents) {
        mParents = parents;
    }
    public String[] getParents() {
        return mParents;
    }
}
