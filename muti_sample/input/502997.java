public class MockResolver implements EntityResolver {
    private Map<String, InputSource> entities = new HashMap<String, InputSource>();
    public void addEntity(String publicId, String systemId, InputSource source) {
        entities.put("[" + publicId + ":" + systemId + "]", source);
    }
    public void removeEntity(String publicId, String systemId) {
        entities.remove("[" + publicId + ":" + systemId + "]");
    }            
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        return entities.get("[" + publicId + ":" + systemId + "]");            
    }
}
