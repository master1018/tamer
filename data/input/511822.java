public class LayoutDeviceManager {
    private static class CaptureErrorHandler implements ErrorHandler {
        private final String mSourceLocation;
        private boolean mFoundError = false;
        CaptureErrorHandler(String sourceLocation) {
            mSourceLocation = sourceLocation;
        }
        public boolean foundError() {
            return mFoundError;
        }
        public void error(SAXParseException ex) throws SAXException {
            mFoundError = true;
            AdtPlugin.log(ex, "Error validating %1$s", mSourceLocation);
        }
        public void fatalError(SAXParseException ex) throws SAXException {
            mFoundError = true;
            AdtPlugin.log(ex, "Error validating %1$s", mSourceLocation);
        }
        public void warning(SAXParseException ex) throws SAXException {
        }
    }
    private final SAXParserFactory mParserFactory;
    private List<LayoutDevice> mDefaultLayoutDevices =
        new ArrayList<LayoutDevice>();
    private List<LayoutDevice> mAddOnLayoutDevices =
        new ArrayList<LayoutDevice>();
    private final List<LayoutDevice> mUserLayoutDevices =
        new ArrayList<LayoutDevice>();
    private List<LayoutDevice> mLayoutDevices;
    LayoutDeviceManager() {
        mParserFactory = SAXParserFactory.newInstance();
        mParserFactory.setNamespaceAware(true);
    }
    public List<LayoutDevice> getCombinedList() {
        return mLayoutDevices;
    }
    public List<LayoutDevice> getDefaultLayoutDevices() {
        return mDefaultLayoutDevices;
    }
    public List<LayoutDevice> getAddOnLayoutDevice() {
        return mAddOnLayoutDevices;
    }
    public List<LayoutDevice> getUserLayoutDevices() {
        return mUserLayoutDevices;
    }
    public LayoutDevice getUserLayoutDevice(String name) {
        for (LayoutDevice d : mUserLayoutDevices) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        return null;
    }
    public LayoutDevice addUserDevice(String name, float xdpi, float ydpi) {
        LayoutDevice d = new LayoutDevice(name);
        d.setXDpi(xdpi);
        d.setYDpi(ydpi);
        mUserLayoutDevices.add(d);
        combineLayoutDevices();
        return d;
    }
    public void removeUserDevice(LayoutDevice device) {
        if (mUserLayoutDevices.remove(device)) {
            combineLayoutDevices();
        }
    }
    public LayoutDevice replaceUserDevice(LayoutDevice device, String newName,
            float newXDpi, float newYDpi) {
        if (device.getName().equals(newName) && device.getXDpi() == newXDpi &&
                device.getYDpi() == newYDpi) {
            return device;
        }
        LayoutDevice newDevice = new LayoutDevice(newName);
        newDevice.setXDpi(newXDpi);
        newDevice.setYDpi(newYDpi);
        Map<String, FolderConfiguration> configs = device.getConfigs();
        newDevice.addConfigs(configs);
        mUserLayoutDevices.remove(device);
        mUserLayoutDevices.add(newDevice);
        combineLayoutDevices();
        return newDevice;
    }
    public void addUserConfiguration(LayoutDevice device, String configName,
            FolderConfiguration config) {
        if (mUserLayoutDevices.contains(device)) {
            device.addConfig(configName, config);
        }
    }
    public void replaceUserConfiguration(LayoutDevice device, String oldConfigName,
            String newConfigName, FolderConfiguration config) {
        if (mUserLayoutDevices.contains(device)) {
            if (oldConfigName != null && oldConfigName.equals(newConfigName) == false) {
                device.removeConfig(oldConfigName);
            }
            device.addConfig(newConfigName, config);
        }
    }
    public void removeUserConfiguration(LayoutDevice device, String configName) {
        if (mUserLayoutDevices.contains(device)) {
            device.removeConfig(configName);
        }
    }
    public void save() {
        try {
            String userFolder = AndroidLocation.getFolder();
            File deviceXml = new File(userFolder, SdkConstants.FN_DEVICES_XML);
            if (deviceXml.isDirectory() == false) {
                write(deviceXml, mUserLayoutDevices);
            }
        } catch (AndroidLocationException e) {
            AdtPlugin.log(e, "Unable to find user directory");
        }
    }
    void loadDefaultAndUserDevices(String sdkOsLocation) {
        loadDefaultLayoutDevices(sdkOsLocation);
        try {
            String userFolder = AndroidLocation.getFolder();
            File deviceXml = new File(userFolder, SdkConstants.FN_DEVICES_XML);
            if (deviceXml.isFile()) {
                parseLayoutDevices(deviceXml, mUserLayoutDevices);
            }
        } catch (AndroidLocationException e) {
            AdtPlugin.log(e, "Unable to find user directory");
        }
    }
    void parseAddOnLayoutDevice(File deviceXml) {
        parseLayoutDevices(deviceXml, mAddOnLayoutDevices);
    }
    void sealAddonLayoutDevices() {
        mAddOnLayoutDevices = Collections.unmodifiableList(mAddOnLayoutDevices);
        combineLayoutDevices();
    }
    private void parseLayoutDevices(File deviceXml, List<LayoutDevice> list) {
        try {
            Source source = new StreamSource(new FileReader(deviceXml));
            CaptureErrorHandler errorHandler = new CaptureErrorHandler(deviceXml.getAbsolutePath());
            Validator validator = LayoutDevicesXsd.getValidator(errorHandler);
            validator.validate(source);
            if (errorHandler.foundError() == false) {
                LayoutDeviceHandler handler = new LayoutDeviceHandler();
                SAXParser parser = mParserFactory.newSAXParser();
                parser.parse(new InputSource(new FileInputStream(deviceXml)), handler);
                list.addAll(handler.getDevices());
            }
        } catch (SAXException e) {
            AdtPlugin.log(e, "Error parsing %1$s", deviceXml.getAbsoluteFile());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            AdtPlugin.log(e, "Error reading %1$s", deviceXml.getAbsoluteFile());
        } catch (ParserConfigurationException e) {
            AdtPlugin.log(e, "Error parsing %1$s", deviceXml.getAbsoluteFile());
        }
    }
    private void loadDefaultLayoutDevices(String sdkOsLocation) {
        ArrayList<LayoutDevice> list = new ArrayList<LayoutDevice>();
        File toolsFolder = new File(sdkOsLocation, SdkConstants.OS_SDK_TOOLS_LIB_FOLDER);
        if (toolsFolder.isDirectory()) {
            File deviceXml = new File(toolsFolder, SdkConstants.FN_DEVICES_XML);
            if (deviceXml.isFile()) {
                parseLayoutDevices(deviceXml, list);
            }
        }
        mDefaultLayoutDevices = Collections.unmodifiableList(list);
    }
    private void combineLayoutDevices() {
        ArrayList<LayoutDevice> list = new ArrayList<LayoutDevice>();
        list.addAll(mDefaultLayoutDevices);
        list.addAll(mAddOnLayoutDevices);
        list.addAll(mUserLayoutDevices);
        mLayoutDevices = Collections.unmodifiableList(list);
    }
    private void write(File deviceXml, List<LayoutDevice> deviceList) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element baseNode = doc.createElementNS(
                    LayoutDevicesXsd.NS_LAYOUT_DEVICE_XSD,
                    LayoutDevicesXsd.NODE_LAYOUT_DEVICES);
            baseNode.setPrefix("d");
            doc.appendChild(baseNode);
            for (LayoutDevice device : deviceList) {
                device.saveTo(doc, baseNode);
            }
            Source source = new DOMSource(doc);
            File file = new File(deviceXml.getAbsolutePath());
            Result result = new StreamResult(file);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (Exception e) {
            AdtPlugin.log(e, "Failed to write %s", deviceXml.getAbsolutePath());
        }
    }
}
