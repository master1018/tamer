public class I18NImpl {
    protected static final String getString(String className, String resource_name, String key) {
        PropertyResourceBundle bundle = null;
        try {
            InputStream stream =
                Class.forName(className).getResourceAsStream(resource_name);
            bundle = new PropertyResourceBundle(stream);
        } catch(Throwable e) {
            throw new RuntimeException(e); 
        }
        return (String)bundle.handleGetObject(key);
    }
}
