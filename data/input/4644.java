public class Bug4195978Test extends ListResourceBundle {
    public static void main(final String args[]) throws Exception {
        new Bug4195978Test().test();
    }
    public void test() throws Exception {
        try {
            final ResourceBundle bundle = ResourceBundle.getBundle("bug4195978Test");
            final ResourceBundle bundle2 = ResourceBundle.getBundle("Bug4195978Test");
            String b1 = bundle.getString("test");
            String b2 = bundle2.getString("test");
            if (b1.equals("test") && b2.equals("TEST")) {
                System.out.println("Passed");
            }
        } catch (Exception e) {
            System.err.println("Failed");
            System.err.println(e);
            throw e;
        }
    }
    public Object[][] getContents() {
        return contents;
    }
    Object[][] contents = {
        {"test", "TEST"},
    };
}
