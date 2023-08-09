public class SampleBundleClass {
    private static SampleBundleClass singleton;
    private static ResourceBundle bundle;
    public SampleBundleClass() {
        super();
        if (singleton != null) {
            throw new RuntimeException();
        }
        singleton = this;
        try {
            bundle = ResourceBundle.getBundle("tests.api.simple.SampleBundleClass");
        } catch (MissingResourceException x) {
            System.out.println("Missing resource");
            bundle = null;
        }
    }
}
