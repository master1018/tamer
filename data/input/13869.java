public class RestrictedBundleTest extends Applet {
    public void init() {
        super.init();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("unavailable.base.name");
            throw new RuntimeException("Error: MissingResourceException is not thrown");
        }
        catch (MissingResourceException e) {
            System.out.println("OK");
        }
    }
}
