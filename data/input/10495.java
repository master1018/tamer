public class Test4314141 {
    public static void main(String[] args) {
        testCandidateOmission();
        testExample();
    }
    static void testCandidateOmission() {
        Locale.setDefault(Locale.US);
        doTestCandidateOmission("de", "DE", "EURO", new String[] {"_de", ""});
        doTestCandidateOmission("de", "DE", "", new String[] {"_de", ""});
        doTestCandidateOmission("de", "", "EURO", new String[] {"_de", ""});
        doTestCandidateOmission("de", "", "", new String[] {"_de", ""});
        doTestCandidateOmission("", "DE", "EURO", new String[] {"__DE", ""});
        doTestCandidateOmission("", "DE", "", new String[] {"__DE", ""});
        doTestCandidateOmission("", "", "EURO", new String[] {"___EURO", ""});
        doTestCandidateOmission("", "", "", new String[] {""});
    }
    static void doTestCandidateOmission(String language, String country, String variant,
            String[] expectedSuffixes) {
        doTest("Test4314141A", language, country, variant, expectedSuffixes);
    }
    static void testExample() {
        Locale.setDefault(new Locale("en", "UK"));
        doTestExample("fr", "CH", new String[] {"_fr_CH.class", "_fr.properties", ".class"});
        doTestExample("fr", "FR", new String[] {"_fr.properties", ".class"});
        doTestExample("de", "DE", new String[] {"_en.properties", ".class"});
        doTestExample("en", "US", new String[] {"_en.properties", ".class"});
        doTestExample("es", "ES", new String[] {"_es_ES.class", ".class"});
    }
    static void doTestExample(String language, String country, String[] expectedSuffixes) {
        doTest("Test4314141B", language, country, "", expectedSuffixes);
    }
    static void doTest(String baseName, String language, String country, String variant,
            String[] expectedSuffixes) {
        System.out.print("Looking for " + baseName + " \"" + language + "\", \"" + country + "\", \"" + variant + "\"");
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, new Locale(language, country, variant));
        System.out.print(" => got ");
        String previousName = null;
        int nameCount = 0;
        for (int i = 3; i >= 0; i--) {
            String name = bundle.getString("name" + i);
            if (!name.equals(previousName)) {
                if (previousName != null) {
                    System.out.print(", ");
                }
                System.out.print(name);
                if (!name.equals(baseName + expectedSuffixes[nameCount++])) {
                    System.out.println();
                    throw new RuntimeException("Error: got unexpected resource bundle");
                }
                previousName = name;
            }
        }
        System.out.println();
        if (nameCount != expectedSuffixes.length) {
            throw new RuntimeException("Error: parent chain too short");
        }
    }
}
