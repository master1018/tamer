public final class UiElementPullParser extends BasePullParser {
    private final static String ATTR_PADDING = "padding"; 
    private final static Pattern sFloatPattern = Pattern.compile("(-?[0-9]+(?:\\.[0-9]+)?)(.*)"); 
    private final int[] sIntOut = new int[1];
    private final ArrayList<UiElementNode> mNodeStack = new ArrayList<UiElementNode>();
    private UiElementNode mRoot;
    private final boolean mExplodedRendering;
    private boolean mZeroAttributeIsPadding = false;
    private boolean mIncreaseExistingPadding = false;
    private List<ElementDescriptor> mLayoutDescriptors;
    private final int mDensityValue;
    private final float mXdpi;
    private final String mDefaultPaddingValue;
    public UiElementPullParser(UiElementNode top, boolean explodeRendering, int densityValue,
            float xdpi, IProject project) {
        super();
        mRoot = top;
        mExplodedRendering = explodeRendering;
        mDensityValue = densityValue;
        mXdpi = xdpi;
        mDefaultPaddingValue = ExplodedRenderingHelper.PADDING_VALUE + "px"; 
        if (mExplodedRendering) {
            IAndroidTarget target = Sdk.getCurrent().getTarget(project);
            AndroidTargetData data = Sdk.getCurrent().getTargetData(target);
            LayoutDescriptors descriptors = data.getLayoutDescriptors();
            mLayoutDescriptors = descriptors.getLayoutDescriptors();
        }
        push(mRoot);
    }
    private UiElementNode getCurrentNode() {
        if (mNodeStack.size() > 0) {
            return mNodeStack.get(mNodeStack.size()-1);
        }
        return null;
    }
    private Node getAttribute(int i) {
        if (mParsingState != START_TAG) {
            throw new IndexOutOfBoundsException();
        }
        UiElementNode uiNode = getCurrentNode();
        Node xmlNode = uiNode.getXmlNode();
        if (xmlNode != null) {
            return xmlNode.getAttributes().item(i);
        }
        return null;
    }
    private void push(UiElementNode node) {
        mNodeStack.add(node);
        mZeroAttributeIsPadding = false;
        mIncreaseExistingPadding = false;
        if (mExplodedRendering) {
            String xml = node.getDescriptor().getXmlLocalName();
            for (ElementDescriptor descriptor : mLayoutDescriptors) {
                if (xml.equals(descriptor.getXmlLocalName())) {
                    NamedNodeMap attributes = node.getXmlNode().getAttributes();
                    Node padding = attributes.getNamedItemNS(SdkConstants.NS_RESOURCES, "padding");
                    if (padding == null) {
                        mZeroAttributeIsPadding = true;
                    } else {
                        mIncreaseExistingPadding = true;
                    }
                    break;
                }
            }
        }
    }
    private UiElementNode pop() {
        return mNodeStack.remove(mNodeStack.size()-1);
    }
    public Object getViewKey() {
        return getCurrentNode();
    }
    public String getPositionDescription() {
        return "XML DOM element depth:" + mNodeStack.size();
    }
    public int getAttributeCount() {
        UiElementNode node = getCurrentNode();
        if (node != null) {
            Collection<UiAttributeNode> attributes = node.getUiAttributes();
            int count = attributes.size();
            return count + (mZeroAttributeIsPadding ? 1 : 0);
        }
        return 0;
    }
    public String getAttributeName(int i) {
        if (mZeroAttributeIsPadding) {
            if (i == 0) {
                return ATTR_PADDING;
            } else {
                i--;
            }
        }
        Node attribute = getAttribute(i);
        if (attribute != null) {
            return attribute.getLocalName();
        }
        return null;
    }
    public String getAttributeNamespace(int i) {
        if (mZeroAttributeIsPadding) {
            if (i == 0) {
                return SdkConstants.NS_RESOURCES;
            } else {
                i--;
            }
        }
        Node attribute = getAttribute(i);
        if (attribute != null) {
            return attribute.getNamespaceURI();
        }
        return ""; 
    }
    public String getAttributePrefix(int i) {
        if (mZeroAttributeIsPadding) {
            if (i == 0) {
                Document doc = mRoot.getXmlDocument();
                return doc.lookupPrefix(AndroidConstants.NS_CUSTOM_RESOURCES);
            } else {
                i--;
            }
        }
        Node attribute = getAttribute(i);
        if (attribute != null) {
            return attribute.getPrefix();
        }
        return null;
    }
    public String getAttributeValue(int i) {
        if (mZeroAttributeIsPadding) {
            if (i == 0) {
                return mDefaultPaddingValue;
            } else {
                i--;
            }
        }
        Node attribute = getAttribute(i);
        if (attribute != null) {
            String value = attribute.getNodeValue();
            if (mIncreaseExistingPadding && ATTR_PADDING.equals(attribute.getLocalName()) &&
                    SdkConstants.NS_RESOURCES.equals(attribute.getNamespaceURI())) {
                return addPaddingToValue(value);
            }
            return value;
        }
        return null;
    }
    public String getAttributeValue(String namespace, String localName) {
        if (mZeroAttributeIsPadding && ATTR_PADDING.equals(localName) &&
                SdkConstants.NS_RESOURCES.equals(namespace)) {
            return mDefaultPaddingValue;
        }
        UiElementNode uiNode = getCurrentNode();
        Node xmlNode = uiNode.getXmlNode();
        if (xmlNode != null) {
            Node attribute = xmlNode.getAttributes().getNamedItemNS(namespace, localName);
            if (attribute != null) {
                String value = attribute.getNodeValue();
                if (mIncreaseExistingPadding && ATTR_PADDING.equals(localName) &&
                        SdkConstants.NS_RESOURCES.equals(namespace)) {
                    return addPaddingToValue(value);
                }
                return value;
            }
        }
        return null;
    }
    public int getDepth() {
        return mNodeStack.size();
    }
    public String getName() {
        if (mParsingState == START_TAG || mParsingState == END_TAG) {
            return getCurrentNode().getDescriptor().getXmlLocalName();
        }
        return null;
    }
    public String getNamespace() {
        if (mParsingState == START_TAG || mParsingState == END_TAG) {
            return getCurrentNode().getDescriptor().getNamespace();
        }
        return null;
    }
    public String getPrefix() {
        if (mParsingState == START_TAG || mParsingState == END_TAG) {
            Document doc = mRoot.getXmlDocument();
            return doc.lookupPrefix(getCurrentNode().getDescriptor().getNamespace());
        }
        return null;
    }
    public boolean isEmptyElementTag() throws XmlPullParserException {
        if (mParsingState == START_TAG) {
            return getCurrentNode().getUiChildren().size() == 0;
        }
        throw new XmlPullParserException("Call to isEmptyElementTag while not in START_TAG",
                this, null);
    }
    @Override
    public void onNextFromStartDocument() {
        onNextFromStartTag();
    }
    @Override
    public void onNextFromStartTag() {
        UiElementNode node = getCurrentNode();
        List<UiElementNode> children = node.getUiChildren();
        if (children.size() > 0) {
            push(children.get(0));
            mParsingState = START_TAG;
        } else {
            if (mParsingState == START_DOCUMENT) {
                mParsingState = END_DOCUMENT;
            } else {
                mParsingState = END_TAG;
            }
        }
    }
    @Override
    public void onNextFromEndTag() {
        UiElementNode node = getCurrentNode();
        node = node.getUiNextSibling();
        if (node != null) {
            pop();
            push(node);
            mParsingState = START_TAG;
        } else {
            pop();
            if (mNodeStack.size() == 1) {
                mParsingState = END_DOCUMENT;
            } else {
                mParsingState = END_TAG;
            }
        }
    }
    private static final class DimensionEntry {
        String name;
        int type;
        DimensionEntry(String name, int unit) {
            this.name = name;
            this.type = unit;
        }
    }
    public static final int COMPLEX_UNIT_PX = 0;
    public static final int COMPLEX_UNIT_DIP = 1;
    public static final int COMPLEX_UNIT_SP = 2;
    public static final int COMPLEX_UNIT_PT = 3;
    public static final int COMPLEX_UNIT_IN = 4;
    public static final int COMPLEX_UNIT_MM = 5;
    private final static DimensionEntry[] sDimensions = new DimensionEntry[] {
        new DimensionEntry("px", COMPLEX_UNIT_PX),
        new DimensionEntry("dip", COMPLEX_UNIT_DIP),
        new DimensionEntry("dp", COMPLEX_UNIT_DIP),
        new DimensionEntry("sp", COMPLEX_UNIT_SP),
        new DimensionEntry("pt", COMPLEX_UNIT_PT),
        new DimensionEntry("in", COMPLEX_UNIT_IN),
        new DimensionEntry("mm", COMPLEX_UNIT_MM),
    };
    private String addPaddingToValue(String s) {
        int padding = ExplodedRenderingHelper.PADDING_VALUE;
        if (stringToPixel(s)) {
            padding += sIntOut[0];
        }
        return padding + "px"; 
    }
    private boolean stringToPixel(String s) {
        s.trim();
        int len = s.length();
        if (len <= 0) {
            return false;
        }
        char[] buf = s.toCharArray();
        for (int i = 0 ; i < len ; i++) {
            if (buf[i] > 255) {
                return false;
            }
        }
        if (buf[0] < '0' && buf[0] > '9' && buf[0] != '.') {
            return false;
        }
        Matcher m = sFloatPattern.matcher(s);
        if (m.matches()) {
            String f_str = m.group(1);
            String end = m.group(2);
            float f;
            try {
                f = Float.parseFloat(f_str);
            } catch (NumberFormatException e) {
                return false;
            }
            if (end.length() > 0 && end.charAt(0) != ' ') {
                DimensionEntry dimension = parseDimension(end);
                if (dimension != null) {
                    switch (dimension.type) {
                        case COMPLEX_UNIT_PX:
                            break;
                        case COMPLEX_UNIT_DIP:
                        case COMPLEX_UNIT_SP: 
                            f *= (float)mDensityValue / Density.DEFAULT_DENSITY;
                            break;
                        case COMPLEX_UNIT_PT:
                            f *= mXdpi * (1.0f / 72);
                            break;
                        case COMPLEX_UNIT_IN:
                            f *= mXdpi;
                            break;
                        case COMPLEX_UNIT_MM:
                            f *= mXdpi * (1.0f / 25.4f);
                            break;
                    }
                    sIntOut[0] = (int) (f + 0.5);
                    return true;
                }
            }
        }
        return false;
    }
    private static DimensionEntry parseDimension(String str) {
        str = str.trim();
        for (DimensionEntry d : sDimensions) {
            if (d.name.equals(str)) {
                return d;
            }
        }
        return null;
    }
}
