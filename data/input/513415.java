public class BridgeXmlPullAttributes extends XmlPullAttributes {
    private final BridgeContext mContext;
    private final boolean mPlatformFile;
    public BridgeXmlPullAttributes(XmlPullParser parser, BridgeContext context,
            boolean platformFile) {
        super(parser);
        mContext = context;
        mPlatformFile = platformFile;
    }
    @Override
    public int getAttributeNameResource(int index) {
        String name = getAttributeName(index);
        String ns = mParser.getAttributeNamespace(index);
        if (BridgeConstants.NS_RESOURCES.equals(ns)) {
            Integer v = Bridge.getResourceValue(BridgeConstants.RES_ATTR, name);
            if (v != null) {
                return v.intValue();
            }
            return 0;
        }
        if (mContext.getProjectCallback().getNamespace().equals(ns)) {
            Integer v = mContext.getProjectCallback().getResourceValue(BridgeConstants.RES_ATTR,
                    name);
            if (v != null) {
                return v.intValue();
            }
        }
        return 0;
    }
    @Override
    public int getAttributeResourceValue(int index, int defaultValue) {
        String value = getAttributeValue(index);
        return resolveResourceValue(value, defaultValue);
    }
    @Override
    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        String value = getAttributeValue(namespace, attribute);
        return resolveResourceValue(value, defaultValue);
    }
    private int resolveResourceValue(String value, int defaultValue) {
        IResourceValue resource = mContext.resolveResValue(mContext.findResValue(value));
        if (resource != null) {
            Integer id = null;
            if (mPlatformFile || resource.isFramework()) {
                id = Bridge.getResourceValue(resource.getType(), resource.getName());
            } else {
                id = mContext.getProjectCallback().getResourceValue(
                        resource.getType(), resource.getName());
            }
            if (id != null) {
                return id;
            }
        }
        return defaultValue;
    }
}
