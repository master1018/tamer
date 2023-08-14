public class NameDistanceTest extends TestCase {
    private NameDistance mNameDistance;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mNameDistance = new NameDistance(30);
    }
    public void testExactMatch() {
        assertFloat(1, "Dwayne", "Dwayne");
    }
    public void testWinklerBonus() {
        assertFloat(0.961f, "Martha", "Marhta");
        assertFloat(0.840f, "Dwayne", "Duane");
        assertFloat(0.813f, "DIXON", "DICKSONX");
    }
    public void testJaroDistance() {
        assertFloat(0.600f, "Donny", "Duane");
    }
    public void testPoorMatch() {
        assertFloat(0.467f, "Johny", "Duane");
    }
    public void testNoMatches() {
        assertFloat(0, "Abcd", "Efgh");
    }
    private void assertFloat(float expected, String name1, String name2) {
        byte[] s1 = Hex.decodeHex(NameNormalizer.normalize(name1));
        byte[] s2 = Hex.decodeHex(NameNormalizer.normalize(name2));
        float actual = mNameDistance.getDistance(s1, s2);
        assertTrue("Expected Jaro-Winkler distance: " + expected + ", actual: " + actual,
                Math.abs(actual - expected) < 0.001);
    }
}
