public class NameNormalizerTest extends TestCase {
    public void testDifferent() {
        final String name1 = NameNormalizer.normalize("Helene");
        final String name2 = NameNormalizer.normalize("Francesca");
        assertFalse(name2.equals(name1));
    }
    public void testAccents() {
        final String name1 = NameNormalizer.normalize("Helene");
        final String name2 = NameNormalizer.normalize("H\u00e9l\u00e8ne");
        assertTrue(name2.equals(name1));
    }
    public void testMixedCase() {
        final String name1 = NameNormalizer.normalize("Helene");
        final String name2 = NameNormalizer.normalize("hELENE");
        assertTrue(name2.equals(name1));
    }
    public void testNonLetters() {
        final String name1 = NameNormalizer.normalize("h-e?l e+n=e");
        final String name2 = NameNormalizer.normalize("helene");
        assertTrue(name2.equals(name1));
    }
    public void testComplexityCase() {
        assertTrue(NameNormalizer.compareComplexity("Helene", "helene") > 0);
    }
    public void testComplexityAccent() {
        assertTrue(NameNormalizer.compareComplexity("H\u00e9lene", "Helene") > 0);
    }
    public void testComplexityLength() {
        assertTrue(NameNormalizer.compareComplexity("helene2009", "helene") > 0);
    }
}
