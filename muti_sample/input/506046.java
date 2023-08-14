public class CurrencyTest extends junit.framework.TestCase {
    public void test_getSymbol_fallback() throws Exception {
        assertEquals("AED", Currency.getInstance("AED").getSymbol(Locale.CANADA));
    }
}
