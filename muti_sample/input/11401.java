public class Bug4083270Test extends RBTestFmwk {
    public static void main(String[] args) throws Exception {
        new Bug4083270Test(true).run(args);
    }
    public Bug4083270Test(boolean dummy) {
    }
    public Bug4083270Test() throws Exception {
        errln("ResourceBundle loaded a non-ResourceBundle class");
    }
    public void testRecursiveResourceLoads() throws Exception {
        final String className = getClass().getName();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(className, Locale.getDefault());
            if (bundle == null) {
                errln("ResourceBundle did not find properties file");
            } else if (!(bundle instanceof PropertyResourceBundle)) {
                errln("ResourceBundle loaded a non-ResourceBundle class");
            }
        } catch (MissingResourceException e) {
            errln("ResourceBundle threw a MissingResourceException");
        }
    }
}
