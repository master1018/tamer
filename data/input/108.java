public class Test4318520 {
    public static void main(String[] args) {
        test(Locale.GERMAN);
        test(Locale.ENGLISH);
    }
    private static void test(Locale locale) {
        Locale.setDefault(locale);
        ResourceBundle myResources =
                ResourceBundle.getBundle("Test4318520RB", Locale.FRENCH);
        String actualLocale = myResources.getString("name");
        if (!actualLocale.equals(locale.toString())) {
            System.out.println("expected: " + locale + ", got: " + actualLocale);
            throw new RuntimeException();
        }
    }
}
