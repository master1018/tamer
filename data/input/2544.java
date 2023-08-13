public class Bug4396021 {
    private static ResourceBundle bundle;
    public static void main(String[] args) throws Exception {
        bundle = ResourceBundle.getBundle("Bug4396021SpecialMessages");
        checkValue("special_key", "special_value");
        checkValue("general_key", "general_value");
    }
    private static void checkValue(String key, String expected) throws Exception {
        String result = bundle.getString(key);
        if (!result.equals(expected)) {
            throw new RuntimeException("Got wrong value from resource bundle"
                    + " - key: " + key + ", expected: " + expected
                    + ", got: " + result);
        }
    }
}
