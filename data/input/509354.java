public class MorseCodeConverterTest extends TestCase {
    @SmallTest
    public void testCharacterS() throws Exception {
        long[] expectedBeeps = {
                MorseCodeConverter.DOT,
                MorseCodeConverter.DOT,
                MorseCodeConverter.DOT,
                MorseCodeConverter.DOT,
                MorseCodeConverter.DOT};
        long[] beeps = MorseCodeConverter.pattern('s');
        assertArraysEqual(expectedBeeps, beeps);
    }
    private void assertArraysEqual(long[] expected, long[] actual) {
        assertEquals("Unexpected array length.", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            long expectedLong = expected[i];
            long actualLong = actual[i];
            assertEquals("Unexpected long at index: " + i, expectedLong, actualLong);
        }
    }
}
