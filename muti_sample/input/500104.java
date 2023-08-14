abstract class TestHelper {
    public void assertSourceEquals(String expected, String actual) {
        String en = expected.replaceAll("[\\s]+", " ").trim();
        String an = actual.replaceAll(  "[\\s]+", " ").trim();
        Assert.assertEquals(
                String.format("Source comparison failure: expected:<%s> but was:<%s>", expected, actual),
                en, an);
    }
}
