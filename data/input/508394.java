public class LayoutDevice {
    private final String mName;
    private Map<String, FolderConfiguration> mEditMap = new HashMap<String, FolderConfiguration>();
    private Map<String, FolderConfiguration> mMap;
    private float mXDpi = Float.NaN;
    private float mYDpi = Float.NaN;
    LayoutDevice(String name) {
        mName = name;
    }
    void saveTo(Document doc, Element parentNode) {
        Element deviceNode = createNode(doc, parentNode, LayoutDevicesXsd.NODE_DEVICE);
        deviceNode.setAttribute(LayoutDevicesXsd.ATTR_NAME, mName);
        Element defaultNode = createNode(doc, deviceNode, LayoutDevicesXsd.NODE_DEFAULT);
        if (Float.isNaN(mXDpi) == false) {
            Element xdpiNode = createNode(doc, defaultNode, LayoutDevicesXsd.NODE_XDPI);
            xdpiNode.setTextContent(Float.toString(mXDpi));
        }
        if (Float.isNaN(mYDpi) == false) {
            Element xdpiNode = createNode(doc, defaultNode, LayoutDevicesXsd.NODE_YDPI);
            xdpiNode.setTextContent(Float.toString(mYDpi));
        }
        for (Entry<String, FolderConfiguration> entry : mEditMap.entrySet()) {
            saveConfigTo(doc, deviceNode, entry.getKey(), entry.getValue());
        }
    }
    private Element createNode(Document doc, Element parentNode, String name) {
        Element newNode = doc.createElementNS(
                LayoutDevicesXsd.NS_LAYOUT_DEVICE_XSD, name);
        newNode.setPrefix(doc.lookupPrefix(LayoutDevicesXsd.NS_LAYOUT_DEVICE_XSD));
        parentNode.appendChild(newNode);
        return newNode;
    }
    private void saveConfigTo(Document doc, Element parent, String configName,
            FolderConfiguration config) {
        Element configNode = createNode(doc, parent, LayoutDevicesXsd.NODE_CONFIG);
        configNode.setAttribute(LayoutDevicesXsd.ATTR_NAME, configName);
        CountryCodeQualifier ccq = config.getCountryCodeQualifier();
        if (ccq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_COUNTRY_CODE);
            node.setTextContent(Integer.toString(ccq.getCode()));
        }
        NetworkCodeQualifier ncq = config.getNetworkCodeQualifier();
        if (ncq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_NETWORK_CODE);
            node.setTextContent(Integer.toString(ncq.getCode()));
        }
        ScreenSizeQualifier ssq = config.getScreenSizeQualifier();
        if (ssq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_SCREEN_SIZE);
            node.setTextContent(ssq.getFolderSegment(null));
        }
        ScreenRatioQualifier srq = config.getScreenRatioQualifier();
        if (srq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_SCREEN_RATIO);
            node.setTextContent(srq.getFolderSegment(null));
        }
        ScreenOrientationQualifier soq = config.getScreenOrientationQualifier();
        if (soq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_SCREEN_ORIENTATION);
            node.setTextContent(soq.getFolderSegment(null));
        }
        PixelDensityQualifier pdq = config.getPixelDensityQualifier();
        if (pdq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_PIXEL_DENSITY);
            node.setTextContent(pdq.getFolderSegment(null));
        }
        TouchScreenQualifier ttq = config.getTouchTypeQualifier();
        if (ttq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_TOUCH_TYPE);
            node.setTextContent(ttq.getFolderSegment(null));
        }
        KeyboardStateQualifier ksq = config.getKeyboardStateQualifier();
        if (ksq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_KEYBOARD_STATE);
            node.setTextContent(ksq.getFolderSegment(null));
        }
        TextInputMethodQualifier timq = config.getTextInputMethodQualifier();
        if (timq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_TEXT_INPUT_METHOD);
            node.setTextContent(timq.getFolderSegment(null));
        }
        NavigationMethodQualifier nmq = config.getNavigationMethodQualifier();
        if (nmq != null) {
            Element node = createNode(doc, configNode, LayoutDevicesXsd.NODE_NAV_METHOD);
            node.setTextContent(nmq.getFolderSegment(null));
        }
        ScreenDimensionQualifier sdq = config.getScreenDimensionQualifier();
        if (sdq != null) {
            Element sizeNode = createNode(doc, configNode, LayoutDevicesXsd.NODE_SCREEN_DIMENSION);
            Element node = createNode(doc, sizeNode, LayoutDevicesXsd.NODE_SIZE);
            node.setTextContent(Integer.toString(sdq.getValue1()));
            node = createNode(doc, sizeNode, LayoutDevicesXsd.NODE_SIZE);
            node.setTextContent(Integer.toString(sdq.getValue2()));
        }
    }
    void addConfig(String name, FolderConfiguration config) {
        mEditMap.put(name, config);
        _seal();
    }
    void addConfigs(Map<String, FolderConfiguration> configs) {
        mEditMap.putAll(configs);
        _seal();
    }
    void removeConfig(String name) {
        mEditMap.remove(name);
        _seal();
    }
    void _addConfig(String name, FolderConfiguration config) {
        mEditMap.put(name, config);
    }
    void _seal() {
        mMap = Collections.unmodifiableMap(mEditMap);
    }
    void setXDpi(float xdpi) {
        mXDpi = xdpi;
    }
    void setYDpi(float ydpi) {
        mYDpi = ydpi;
    }
    public String getName() {
        return mName;
    }
    public Map<String, FolderConfiguration> getConfigs() {
        return mMap;
    }
    public float getXDpi() {
        return mXDpi;
    }
    public float getYDpi() {
        return mYDpi;
    }
 }
