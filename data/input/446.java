public class TestXSNonPositiveInteger extends TestCase {
    public void testSetValue() {
        XSNonPositiveInteger i = new XSNonPositiveInteger();
        Number j = i.eval(new Integer(-10)).toNumber();
        assertEquals(j.intValue(), -10);
        j = i.eval("-10").toNumber();
        assertEquals(j.intValue(), -10);
        try {
            j = i.eval("20").toNumber();
            fail("Should fail with positive int string");
        } catch (Exception e) {
        }
        try {
            j = i.eval("0").toNumber();
            fail("Should fail with 0 string");
        } catch (Exception e) {
        }
    }
}
