public class XMLResourceBundleTest {
    public static void main(String[] args) {
        ResourceBundle.Control xmlControl = new ResourceBundle.Control() {
                   @Override
                   public List<String> getFormats(String baseName) {
                       if (baseName == null) {
                           throw new NullPointerException();
                       }
                       return Arrays.asList("xml");
                   }
                   @Override
                   public ResourceBundle newBundle(String baseName, Locale locale,
                                                   String format,
                                                   ClassLoader loader,
                                                   boolean reload)
                       throws IllegalAccessException,
                              InstantiationException, IOException {
                       if (baseName == null || locale == null
                           || format == null || loader == null) {
                           throw new NullPointerException();
                       }
                       ResourceBundle bundle = null;
                       if (format.equals("xml")) {
                           String bundleName = toBundleName(baseName, locale);
                           String resourceName = toResourceName(bundleName, format);
                           URL url = loader.getResource(resourceName);
                           if (url != null) {
                               URLConnection connection = url.openConnection();
                               if (connection != null) {
                                   if (reload) {
                                       connection.setUseCaches(false);
                                   }
                                   InputStream stream = connection.getInputStream();
                                   if (stream != null) {
                                       BufferedInputStream bis = new BufferedInputStream(stream);
                                       bundle = new XMLResourceBundle(bis);
                                       bis.close();
                                   }
                               }
                           }
                       }
                       return bundle;
                   }
               };
        ResourceBundle rb = ResourceBundle.getBundle("XmlRB", new Locale(""), xmlControl);
        String type = rb.getString("type");
        if (!type.equals("XML")) {
            throw new RuntimeException("Root Locale: type: got " + type
                                       + ", expected XML (ASCII)");
        }
        rb = ResourceBundle.getBundle("XmlRB", Locale.JAPAN, xmlControl);
        type = rb.getString("type");
        if (!type.equals("\uff38\uff2d\uff2c")) {
            throw new RuntimeException("Locale.JAPAN: type: got " + type
                                       + ", expected \uff38\uff2d\uff2c (fullwidth XML)");
        }
    }
    private static class XMLResourceBundle extends ResourceBundle {
        private Properties props;
        XMLResourceBundle(InputStream stream) throws IOException {
            props = new Properties();
            props.loadFromXML(stream);
        }
        protected Object handleGetObject(String key) {
            if (key == null) {
                throw new NullPointerException();
            }
            return props.get(key);
        }
        public Enumeration<String> getKeys() {
            return null;
        }
    }
}
