public class ClasspathTest {
    public static void main(String[] s) {
        new ClasspathTest();
    }
    ClasspathTest() {
        Locale OSAKA = new Locale("ja", "JP", "osaka");
        List<Locale> availableLocales = Arrays.asList(Locale.getAvailableLocales());
        if (availableLocales.contains(OSAKA)) {
            throw new RuntimeException("LSS providers were loaded from the class path.");
        }
    }
}
