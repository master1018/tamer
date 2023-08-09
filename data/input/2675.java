public class Bug4165815Test extends RBTestFmwk {
    public static void main(String[] args) throws Exception {
        new Bug4165815Test().run(args);
    }
    private static final String bundleName = "/Bug4165815Bundle";
    public void testIt() throws Exception {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName, new Locale("en", "US"));
            errln("ResourceBundle returned a bundle when it should not have.");
        } catch (IllegalArgumentException e) {
        } catch (MissingResourceException e) {
            errln("ResourceBundle threw a MissingResourceException when it should have thrown an IllegalArgumentException.");
        }
    }
}
