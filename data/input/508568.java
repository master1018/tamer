class AndroidManifestWriter {
    private static final Logger sLogger = Logger.getLogger(AndroidManifestWriter.class.getName());
    private final Document mDoc;
    private final String mOsManifestFilePath;
    private AndroidManifestWriter(Document doc, String osManifestFilePath) {
        mDoc = doc;
        mOsManifestFilePath = osManifestFilePath;
    }
    public boolean setMinSdkVersion(String minSdkVersion) {
        Element usesSdkElement = null;
        NodeList nodeList = mDoc.getElementsByTagName(AndroidManifest.NODE_USES_SDK);
        if (nodeList.getLength() > 0) {
            usesSdkElement = (Element) nodeList.item(0);
        } else {
            usesSdkElement = mDoc.createElement(AndroidManifest.NODE_USES_SDK);
            mDoc.getDocumentElement().appendChild(usesSdkElement);
        }
        Attr minSdkAttr = mDoc.createAttributeNS(SdkConstants.NS_RESOURCES,
                AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION);
        String prefix = mDoc.lookupPrefix(SdkConstants.NS_RESOURCES);
        minSdkAttr.setPrefix(prefix);
        minSdkAttr.setValue(minSdkVersion);
        usesSdkElement.setAttributeNodeNS(minSdkAttr);
        return saveXmlToFile();
    }
    private boolean saveXmlToFile() {
        try {
            Source source = new DOMSource(mDoc);
            File file = new File(mOsManifestFilePath);
            Result result = new StreamResult(file);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            sLogger.log(Level.SEVERE, "Failed to write xml file", e);
            return false;
        } catch (TransformerException e) {
            sLogger.log(Level.SEVERE, "Failed to write xml file", e);
            return false;
        }
        return true;
    }
    public static AndroidManifestWriter parse(String osManifestFilePath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(osManifestFilePath);
            return new AndroidManifestWriter(doc, osManifestFilePath);
        } catch (ParserConfigurationException e) {
            sLogger.log(Level.SEVERE, "Error parsing file", e);
            return null;
        } catch (SAXException e) {
            sLogger.log(Level.SEVERE, "Error parsing file", e);
            return null;
        } catch (IOException e) {
            sLogger.log(Level.SEVERE, "Error parsing file", e);
            return null;
        }
    }
}
