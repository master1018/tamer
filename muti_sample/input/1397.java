public class Bug4353454 {
    public static void main(String[] args) {
        Locale l = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            test();
            test();
        } finally {
            Locale.setDefault(l);
        }
    }
    private static void test() {
        ResourceBundle myResources = ResourceBundle.getBundle("RB4353454", new Locale(""));
        if (!"Got it!".equals(myResources.getString("text"))) {
            throw new RuntimeException("returned wrong resource for key 'text': "
                                       + myResources.getString("text"));
        }
    }
}
