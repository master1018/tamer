public class Bug6299235Test {
    public static void main(String args[]) throws Exception {
        ResourceBundle russionAwtRes = ResourceBundle.getBundle("sun.awt.resources.awt",
                                                                new Locale("ru", "RU"),
                                                                CoreResourceBundleControl.getRBControlInstance());
        if (russionAwtRes != null) {
            String result = russionAwtRes.getString("foo");
            if (result.equals("bar")) {
                System.out.println("Bug6299235Test passed");
            } else {
                System.err.println("Bug6299235Test failed");
                throw new Exception("Resource found, but value of key foo is not correct\n");
            }
        }
    }
}
