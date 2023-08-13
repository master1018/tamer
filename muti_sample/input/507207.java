class XmlStringFileHelper {
    private HashMap<String, Map<String, String>> mResIdCache =
        new HashMap<String, Map<String, String>>();
    private XPath mXPath;
    public XmlStringFileHelper() {
    }
    public String valueOfStringId(IProject project, String xmlFileWsPath, String stringId) {
        Map<String, String> cache = getResIdsForFile(project, xmlFileWsPath);
        return cache.get(stringId);
    }
    public Map<String, String> getResIdsForFile(IProject project, String xmlFileWsPath) {
        Map<String, String> cache = mResIdCache.get(xmlFileWsPath);
        if (cache == null) {
            cache = internalGetResIdsForFile(project, xmlFileWsPath);
            mResIdCache.put(xmlFileWsPath, cache);
        }
        return cache;
    }
    private Map<String, String> internalGetResIdsForFile(IProject project, String xmlFileWsPath) {
        TreeMap<String, String> ids = new TreeMap<String, String>();
        if (mXPath == null) {
            mXPath = AndroidXPathFactory.newXPath();
        }
        IResource resource = project.getFile(xmlFileWsPath);
        if (resource != null && resource.exists() && resource.getType() == IResource.FILE) {
            InputSource source;
            try {
                source = new InputSource(((IFile) resource).getContents());
                String xpathExpr = "/resources/string";                         
                Object result = mXPath.evaluate(xpathExpr, source, XPathConstants.NODESET);
                if (result instanceof NodeList) {
                    NodeList list = (NodeList) result;
                    for (int n = list.getLength() - 1; n >= 0; n--) {
                        Node strNode = list.item(n);
                        NamedNodeMap attrs = strNode.getAttributes();
                        Node nameAttr = attrs.getNamedItem("name");             
                        if (nameAttr != null) {
                            String id = nameAttr.getNodeValue();
                            String text = strNode.getTextContent();
                            ids.put(id, text);
                        }
                    }
                }
            } catch (CoreException e1) {
            } catch (XPathExpressionException e2) {
            }
        }
        return ids;
    }
}
