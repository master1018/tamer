class LayoutDeviceHandler extends DefaultHandler {
    private List<LayoutDevice> mDevices = new ArrayList<LayoutDevice>();
    private LayoutDevice mCurrentDevice;
    private FolderConfiguration mDefaultConfig;
    private FolderConfiguration mCurrentConfig;
    private final StringBuilder mStringAccumulator = new StringBuilder();
    private String mSize1, mSize2;
    public List<LayoutDevice> getDevices() {
        return mDevices;
    }
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
        if (LayoutDevicesXsd.NODE_DEVICE.equals(localName)) {
            String deviceName = attributes.getValue("", LayoutDevicesXsd.ATTR_NAME);
            mCurrentDevice = new LayoutDevice(deviceName);
            mDevices.add(mCurrentDevice);
        } else if (LayoutDevicesXsd.NODE_DEFAULT.equals(localName)) {
            mDefaultConfig = mCurrentConfig = new FolderConfiguration();
        } else if (LayoutDevicesXsd.NODE_CONFIG.equals(localName)) {
            mCurrentConfig = new FolderConfiguration();
            if (mDefaultConfig != null) {
                mCurrentConfig.set(mDefaultConfig);
            }
            String deviceName = attributes.getValue("", LayoutDevicesXsd.ATTR_NAME);
            mCurrentDevice.addConfig(deviceName, mCurrentConfig);
        } else if (LayoutDevicesXsd.NODE_SCREEN_DIMENSION.equals(localName)) {
            mSize1 = mSize2 = null;
        }
        mStringAccumulator.setLength(0);
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        mStringAccumulator.append(ch, start, length);
    }
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (LayoutDevicesXsd.NODE_DEVICE.equals(localName)) {
            mCurrentDevice = null;
            mDefaultConfig = null;
        } else if (LayoutDevicesXsd.NODE_CONFIG.equals(localName)) {
            mCurrentConfig = null;
        } else if (LayoutDevicesXsd.NODE_COUNTRY_CODE.equals(localName)) {
            CountryCodeQualifier ccq = new CountryCodeQualifier(
                    Integer.parseInt(mStringAccumulator.toString()));
            mCurrentConfig.setCountryCodeQualifier(ccq);
        } else if (LayoutDevicesXsd.NODE_NETWORK_CODE.equals(localName)) {
            NetworkCodeQualifier ncq = new NetworkCodeQualifier(
                    Integer.parseInt(mStringAccumulator.toString()));
            mCurrentConfig.setNetworkCodeQualifier(ncq);
        } else if (LayoutDevicesXsd.NODE_SCREEN_SIZE.equals(localName)) {
            ScreenSizeQualifier ssq = new ScreenSizeQualifier(
                    ScreenSize.getEnum(mStringAccumulator.toString()));
            mCurrentConfig.setScreenSizeQualifier(ssq);
        } else if (LayoutDevicesXsd.NODE_SCREEN_RATIO.equals(localName)) {
            ScreenRatioQualifier srq = new ScreenRatioQualifier(
                    ScreenRatio.getEnum(mStringAccumulator.toString()));
            mCurrentConfig.setScreenRatioQualifier(srq);
        } else if (LayoutDevicesXsd.NODE_SCREEN_ORIENTATION.equals(localName)) {
            ScreenOrientationQualifier soq = new ScreenOrientationQualifier(
                    ScreenOrientation.getEnum(mStringAccumulator.toString()));
            mCurrentConfig.setScreenOrientationQualifier(soq);
        } else if (LayoutDevicesXsd.NODE_PIXEL_DENSITY.equals(localName)) {
            PixelDensityQualifier pdq = new PixelDensityQualifier(
                    Density.getEnum(mStringAccumulator.toString()));
            mCurrentConfig.setPixelDensityQualifier(pdq);
        } else if (LayoutDevicesXsd.NODE_TOUCH_TYPE.equals(localName)) {
            TouchScreenQualifier tsq = new TouchScreenQualifier(
                    TouchScreenType.getEnum(mStringAccumulator.toString()));
            mCurrentConfig.setTouchTypeQualifier(tsq);
        } else if (LayoutDevicesXsd.NODE_KEYBOARD_STATE.equals(localName)) {
            KeyboardStateQualifier ksq = new KeyboardStateQualifier(
                    KeyboardState.getEnum(mStringAccumulator.toString()));
            mCurrentConfig.setKeyboardStateQualifier(ksq);
        } else if (LayoutDevicesXsd.NODE_TEXT_INPUT_METHOD.equals(localName)) {
            TextInputMethodQualifier timq = new TextInputMethodQualifier(
                    TextInputMethod.getEnum(mStringAccumulator.toString()));
            mCurrentConfig.setTextInputMethodQualifier(timq);
        } else if (LayoutDevicesXsd.NODE_NAV_METHOD.equals(localName)) {
            NavigationMethodQualifier nmq = new NavigationMethodQualifier(
                    NavigationMethod.getEnum(mStringAccumulator.toString()));
            mCurrentConfig.setNavigationMethodQualifier(nmq);
        } else if (LayoutDevicesXsd.NODE_SCREEN_DIMENSION.equals(localName)) {
            ScreenDimensionQualifier qual = ScreenDimensionQualifier.getQualifier(mSize1, mSize2);
            if (qual != null) {
                mCurrentConfig.setScreenDimensionQualifier(qual);
            }
        } else if (LayoutDevicesXsd.NODE_XDPI.equals(localName)) {
            mCurrentDevice.setXDpi(Float.parseFloat(mStringAccumulator.toString()));
        } else if (LayoutDevicesXsd.NODE_YDPI.equals(localName)) {
            mCurrentDevice.setYDpi(Float.parseFloat(mStringAccumulator.toString()));
        } else if (LayoutDevicesXsd.NODE_SIZE.equals(localName)) {
            if (mSize1 == null) {
                mSize1 = mStringAccumulator.toString();
            } else if (mSize2 == null) {
                mSize2 = mStringAccumulator.toString();
            }
        }
    }
}
