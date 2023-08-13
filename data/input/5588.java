public class CodePointIM {
    public static void main(String[] args) {
        try {
            ResourceBundle resource = ResourceBundle.getBundle(
                    "resources.codepoint",
                    Locale.getDefault());
            System.err.println(resource.getString("warning"));
        } catch (MissingResourceException e) {
            System.err.println(e.toString());
        }
        System.exit(1);
    }
}
