public class WidgetPullParser extends BasePullParser {
    private final ViewElementDescriptor mDescriptor;
    private String[][] mAttributes = new String[][] {
            { "text", null },
            { "layout_width", "wrap_content" },
            { "layout_height", "wrap_content" },
    };
    public WidgetPullParser(ViewElementDescriptor descriptor) {
        mDescriptor = descriptor;
        String[] segments = mDescriptor.getFullClassName().split(AndroidConstants.RE_DOT);
        mAttributes[0][1] = segments[segments.length-1];
    }
    public Object getViewKey() {
        return mDescriptor;
    }
    public int getAttributeCount() {
        return mAttributes.length; 
    }
    public String getAttributeName(int index) {
        if (index < mAttributes.length) {
            return mAttributes[index][0];
        }
        return null;
    }
    public String getAttributeNamespace(int index) {
        return SdkConstants.NS_RESOURCES;
    }
    public String getAttributePrefix(int index) {
        return null;
    }
    public String getAttributeValue(int index) {
        if (index < mAttributes.length) {
            return mAttributes[index][1];
        }
        return null;
    }
    public String getAttributeValue(String ns, String name) {
        if (SdkConstants.NS_RESOURCES.equals(ns)) {
            for (String[] attribute : mAttributes) {
                if (name.equals(attribute[0])) {
                    return attribute[1];
                }
            }
        }
        return null;
    }
    public int getDepth() {
        return 0;
    }
    public String getName() {
        return mDescriptor.getXmlLocalName();
    }
    public String getNamespace() {
        return null;
    }
    public String getPositionDescription() {
        return null;
    }
    public String getPrefix() {
        return null;
    }
    public boolean isEmptyElementTag() throws XmlPullParserException {
        if (mParsingState == START_TAG) {
            return true;
        }
        throw new XmlPullParserException("Call to isEmptyElementTag while not in START_TAG",
                this, null);
    }
    @Override
    public void onNextFromStartDocument() {
        mParsingState = START_TAG;
    }
    @Override
    public void onNextFromStartTag() {
        mParsingState = END_TAG;
    }
    @Override
    public void onNextFromEndTag() {
        mParsingState = END_DOCUMENT;
    }
}
