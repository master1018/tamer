@TestTargetClass(DecimalFormat.class)
public class DecimalFormatTestICU extends TestCase {
    DecimalFormat format;
    protected void setUp() {
        format = (DecimalFormat) NumberFormat.getNumberInstance();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Regression test.",
            method = "format",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Regression test.",
            method = "parse",
            args = {java.lang.String.class}
        )
    })
    @AndroidOnly("special feature of icu4c")
    public void test_sigDigitPatterns() throws Exception {
        DecimalFormat format = (DecimalFormat) NumberFormat
        .getInstance(Locale.US);
        format.applyPattern("@@@");
        assertEquals("sigDigit doesn't work", "12300", format.format(12345));
        assertEquals("sigDigit doesn't work", "0.123", format.format(0.12345));
        format.applyPattern("@@##");
        assertEquals("sigDigit doesn't work", "3.142", format.format(3.14159));
        assertEquals("sigDigit doesn't work", "1.23", format.format(1.23004));
        format.applyPattern("@@###E0");
        assertEquals("1.23E1", format.format(12.3));
        format.applyPattern("0.0###E0");
        assertEquals("1.23E1", format.format(12.3));
        try {
            format.applyPattern("@00");
            fail("expected IllegalArgumentException was not thrown for "
                    + "pattern \"@00\".");
        } catch (IllegalArgumentException e) {
        }
        try {
            format.applyPattern("@.###");
            fail("expected IllegalArgumentException was not thrown for "
                    + "pattern \"@.###\".");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Regression test.",
            method = "format",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Regression test.",
            method = "parse",
            args = {java.lang.String.class}
        )
    })
    @AndroidOnly("special feature of icu4c")
    public void test_paddingPattern() throws Exception {
        format.applyPattern("*x##,##,#,##0.0#");
        assertEquals("xxxxxxxxx123.0", format.format(123));
        assertEquals(123, format.parse("xxxxxxxxx123.0").intValue());
        format.applyPattern("$*x#,##0.00");
        assertEquals("$xx123.00", format.format(123));
        assertEquals("$1,234.00", format.format(1234));
        format.applyPattern("*\u00e7#0 o''clock");
        assertEquals("\u00e72 o'clock", format.format(2));
        assertEquals("12 o'clock", format.format(12));
        assertEquals(2, format.parse("\u00e72 o'clock").intValue());
        assertEquals(12, format.parse("12 o'clock").intValue());
        try {
            format.applyPattern("#0.##*xE0");
            fail("expected IllegalArgumentException was not thrown for"
                    + "pattern \"#0.##*xE0\".");
        } catch (IllegalArgumentException e) {
        }
        try {
            format.applyPattern("##0.## *");
            fail("expected IllegalArgumentException was not thrown for "
                    + "pattern \"##0.## *\".");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Regression test.",
            method = "format",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Regression test.",
            method = "parse",
            args = {java.lang.String.class}
        )
    })
    @AndroidOnly("special feature of icu4c")
    public void test_positiveExponentSign() throws Exception {
        format.applyPattern("0.###E+0");
        assertEquals("1E+2", format.format(100));
        assertEquals("1E-2", format.format(0.01));
        assertEquals(100, format.parse("1E+2").intValue());
        assertEquals(0.01f, format.parse("1E-2").floatValue());
        format.applyPattern("0.###E0 m/s");
        assertEquals("1E2 m/s", format.format(100));
        assertEquals(100, format.parse("1E2 m/s").intValue());
        format.applyPattern("00.###E0");
        assertEquals("12.3E-4", format.format(0.00123));
        assertEquals(0.00123f, format.parse("12.3E-4").floatValue());
        format.applyPattern("##0.####E0");
        assertEquals("12.345E3", format.format(12345));
        assertEquals(12345, format.parse("12.345E3").intValue());
        try {
            format.applyPattern("#,##0.##E0");
            fail("expected IllegalArgumentException was not thrown for "
                    + "pattern \"#,##0.##E0\".");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies the grouping size.",
            method = "format",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies the grouping size.",
            method = "parse",
            args = {java.lang.String.class}
        )
    })
    @AndroidOnly("special feature of icu4c")
    public void test_secondaryGroupingSize() throws Exception {
        format.applyPattern("#,##,###,####");
        assertEquals("123,456,7890", format.format(1234567890));
        assertEquals(1234567890, format.parse("123,456,7890").intValue());
        format.applyPattern("##,#,###,####");
        assertEquals("123,456,7890", format.format(1234567890));
        assertEquals(1234567890, format.parse("123,456,7890").intValue());
        format.applyPattern("###,###,####");
        assertEquals("123,456,7890", format.format(1234567890));
        assertEquals(1234567890, format.parse("123,456,7890").intValue());
        format.applyPattern("###,##,###.#");
        assertEquals("12,34,567.8", format.format(1234567.8));
        format.applyPattern("##,#,##,###.#");
        assertEquals("12,34,567.8", format.format(1234567.8));
        format.applyPattern("#,##,##,###.#");
        assertEquals("12,34,567.8", format.format(1234567.8));
    }
}
