public class PropertiesSheet {
    public static void initializeSheet(final Properties properties, Sheet s) {
        Sheet.Set set1 = Sheet.createPropertiesSet();
        set1.setDisplayName("Properties");
        for (final Property p : properties) {
            Node.Property<String> prop = new Node.Property<String>(String.class) {
                @Override
                public boolean canRead() {
                    return true;
                }
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return p.getValue();
                }
                @Override
                public boolean canWrite() {
                    return false;
                }
                @Override
                public void setValue(String arg0) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    properties.setProperty(p.getName(), arg0);
                }
            };
            prop.setName(p.getName());
            set1.put(prop);
        }
        s.put(set1);
    }
}
