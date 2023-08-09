@TestTargetClass(DecimalFormatSymbols.class) 
public class DecimalFormatSymbolsTest extends TestCase {
    DecimalFormatSymbols dfs;
    DecimalFormatSymbols dfsUS;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DecimalFormatSymbols",
        args = {}
    )
    public void test_Constructor() {
        try {
            new DecimalFormatSymbols();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DecimalFormatSymbols",
        args = {java.util.Locale.class}
    )
    public void test_ConstructorLjava_util_Locale() {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en",
                "us"));
        assertEquals("Returned incorrect symbols", '%', dfs.getPercent());
        try {
            new DecimalFormatSymbols(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        try {
            DecimalFormatSymbols fs = new DecimalFormatSymbols(Locale.US);
            DecimalFormatSymbols fsc = (DecimalFormatSymbols) fs.clone();
            assertEquals(fs.getCurrency(), fsc.getCurrency());
            fs = new DecimalFormatSymbols();
            DecimalFormatSymbols fsc2 = (DecimalFormatSymbols) (fs.clone());
            assertTrue("Object's clone isn't equal!", fs.equals(fsc2));
            fs.setNaN("not-a-number");
            assertTrue("Object's changed clone should not be equal!", !fs
                    .equals(fsc2));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        assertTrue("Equal objects returned false", dfs.equals(dfs.clone()));
        dfs.setDigit('B');
        assertTrue("Un-Equal objects returned true", !dfs
                .equals(new DecimalFormatSymbols()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCurrency",
        args = {}
    )
    public void test_getCurrency() {
        Locale csCzLocale = new Locale("cs", "CZ");
        Locale czLocale = new Locale("", "CZ");
        Locale csLocale = new Locale("cs", "");
        Locale deLocale = new Locale("de", "AT");
        Locale[] requiredLocales = {Locale.US, csCzLocale, czLocale, csLocale, deLocale};
        if (!Support_Locale.areLocalesAvailable(requiredLocales)) {
            return;
        }
        Currency currency = Currency.getInstance("USD");
        assertEquals("Returned incorrect currency",
                dfsUS.getCurrency(), currency);
        Currency currC = Currency.getInstance("CZK");
        Currency currX = Currency.getInstance("XXX");
        Currency currE = Currency.getInstance("EUR");
        DecimalFormatSymbols dfs1 = new DecimalFormatSymbols(csCzLocale);
        assertTrue("Test1: Returned incorrect currency",
                dfs1.getCurrency() == currC);
        assertEquals("Test1: Returned incorrect currencySymbol", "K\u010d", dfs1
                .getCurrencySymbol());
        assertEquals("Test1: Returned incorrect intlCurrencySymbol", "CZK",
                dfs1.getInternationalCurrencySymbol());
        dfs1 = new DecimalFormatSymbols(czLocale);
        assertTrue("Test2: Returned incorrect currency",
                dfs1.getCurrency() == currC);
        assertEquals("Test2: Returned incorrect currencySymbol", "K\u010d", dfs1
                .getCurrencySymbol());
        assertEquals("Test2: Returned incorrect intlCurrencySymbol", "CZK",
                dfs1.getInternationalCurrencySymbol());
        dfs1 = new DecimalFormatSymbols(csLocale);
        assertEquals("Test3: Returned incorrect currency",
                currX, dfs1.getCurrency());
        assertEquals("Test3: Returned incorrect currencySymbol", "\u00a4", dfs1
                .getCurrencySymbol());
        assertEquals("Test3: Returned incorrect intlCurrencySymbol", "XXX",
                dfs1.getInternationalCurrencySymbol());
        dfs1 = new DecimalFormatSymbols(deLocale);
        assertTrue("Test4: Returned incorrect currency",
                dfs1.getCurrency() == currE);
        assertEquals("Test4: Returned incorrect currencySymbol", "\u20ac", dfs1
                .getCurrencySymbol());
        assertEquals("Test4: Returned incorrect intlCurrencySymbol", "EUR",
                dfs1.getInternationalCurrencySymbol());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCurrencySymbol",
        args = {}
    )
    public void test_getCurrencySymbol() {
        assertEquals("Returned incorrect currencySymbol", "$", dfsUS
                .getCurrencySymbol());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDecimalSeparator",
        args = {}
    )
    public void test_getDecimalSeparator() {
        dfs.setDecimalSeparator('*');
        assertEquals("Returned incorrect DecimalSeparator symbol", '*', dfs
                .getDecimalSeparator());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDigit",
        args = {}
    )
    public void test_getDigit() {
        dfs.setDigit('*');
        assertEquals("Returned incorrect Digit symbol", '*', dfs.getDigit());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getGroupingSeparator",
        args = {}
    )
    public void test_getGroupingSeparator() {
        dfs.setGroupingSeparator('*');
        assertEquals("Returned incorrect GroupingSeparator symbol", '*', dfs
                .getGroupingSeparator());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInfinity",
        args = {}
    )
    public void test_getInfinity() {
        dfs.setInfinity("&");
        assertTrue("Returned incorrect Infinity symbol",
                dfs.getInfinity() == "&");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInternationalCurrencySymbol",
        args = {}
    )
    public void test_getInternationalCurrencySymbol() {
        assertEquals("Returned incorrect InternationalCurrencySymbol", "USD",
                dfsUS.getInternationalCurrencySymbol());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMinusSign",
        args = {}
    )
    public void test_getMinusSign() {
        dfs.setMinusSign('&');
        assertEquals("Returned incorrect MinusSign symbol", '&', dfs
                .getMinusSign());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMonetaryDecimalSeparator",
        args = {}
    )
    public void test_getMonetaryDecimalSeparator() {
        try {
            dfs.setMonetaryDecimalSeparator(',');
            assertEquals("Returned incorrect MonetaryDecimalSeparator symbol",
                    ',', dfs.getMonetaryDecimalSeparator());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getNaN",
        args = {}
    )
    public void test_getNaN() {
        dfs.setNaN("NAN!!");
        assertEquals("Returned incorrect nan symbol", "NAN!!", dfs.getNaN());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPatternSeparator",
        args = {}
    )
    public void test_getPatternSeparator() {
        dfs.setPatternSeparator('X');
        assertEquals("Returned incorrect PatternSeparator symbol", 'X', dfs
                .getPatternSeparator());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPercent",
        args = {}
    )
    public void test_getPercent() {
        dfs.setPercent('*');
        assertEquals("Returned incorrect Percent symbol", '*', dfs.getPercent());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPerMill",
        args = {}
    )
    public void test_getPerMill() {
        dfs.setPerMill('#');
        assertEquals("Returned incorrect PerMill symbol", '#', dfs.getPerMill());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getZeroDigit",
        args = {}
    )
    public void test_getZeroDigit() {
        dfs.setZeroDigit('*');
        assertEquals("Returned incorrect ZeroDigit symbol", '*', dfs
                .getZeroDigit());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    @AndroidOnly("Succeeds against Android.")
    public void test_hashCode() {
        try {
            DecimalFormatSymbols dfs1 = new DecimalFormatSymbols();
            DecimalFormatSymbols dfs2 = (DecimalFormatSymbols) dfs1.clone();
            assertTrue("Hash codes of equal object are equal", dfs2
                    .hashCode() == dfs1.hashCode());
            dfs1.setInfinity("infinity_infinity");
            assertTrue("Hash codes of non-equal objects are equal", dfs2
                    .hashCode() != dfs1.hashCode());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCurrency",
        args = {java.util.Currency.class}
    )
    public void test_setCurrencyLjava_util_Currency() {
        Locale locale = Locale.CANADA;
        DecimalFormatSymbols dfs = ((DecimalFormat) NumberFormat
                .getCurrencyInstance(locale)).getDecimalFormatSymbols();
        try {
            dfs.setCurrency(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        Currency currency = Currency.getInstance("JPY");
        dfs.setCurrency(currency);
        assertTrue("Returned incorrect currency", currency == dfs.getCurrency());
        assertEquals("Returned incorrect currency symbol", currency.getSymbol(
                locale), dfs.getCurrencySymbol());
        assertTrue("Returned incorrect international currency symbol", currency
                .getCurrencyCode().equals(dfs.getInternationalCurrencySymbol()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCurrencySymbol",
        args = {java.lang.String.class}
    )
    public void test_setCurrencySymbolLjava_lang_String() {
        try {
            dfs.setCurrencySymbol("$");
            assertEquals("Returned incorrect CurrencySymbol symbol", "$", dfs
                    .getCurrencySymbol());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDecimalSeparator",
        args = {char.class}
    )
    public void test_setDecimalSeparatorC() {
        dfs.setDecimalSeparator('*');
        assertEquals("Returned incorrect DecimalSeparator symbol", '*', dfs
                .getDecimalSeparator());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDigit",
        args = {char.class}
    )
    public void test_setDigitC() {
        dfs.setDigit('*');
        assertEquals("Returned incorrect Digit symbol", '*', dfs.getDigit());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setGroupingSeparator",
        args = {char.class}
    )
    public void test_setGroupingSeparatorC() {
        dfs.setGroupingSeparator('*');
        assertEquals("Returned incorrect GroupingSeparator symbol", '*', dfs
                .getGroupingSeparator());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setInfinity",
        args = {java.lang.String.class}
    )
    public void test_setInfinityLjava_lang_String() {
        dfs.setInfinity("&");
        assertTrue("Returned incorrect Infinity symbol",
                dfs.getInfinity() == "&");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setInternationalCurrencySymbol",
        args = {java.lang.String.class}
    )
    @KnownFailure("getCurrency() doesn't return null for bogus currency code.")
    public void test_setInternationalCurrencySymbolLjava_lang_String() {
        Locale locale = Locale.CANADA;
        DecimalFormatSymbols dfs = ((DecimalFormat) NumberFormat
                .getCurrencyInstance(locale)).getDecimalFormatSymbols();
        Currency currency = Currency.getInstance("JPY");
        dfs.setInternationalCurrencySymbol(currency.getCurrencyCode());
        assertTrue("Test1: Returned incorrect currency", currency == dfs
                .getCurrency());
        assertEquals("Test1: Returned incorrect currency symbol", currency
                .getSymbol(locale), dfs.getCurrencySymbol());
        assertTrue("Test1: Returned incorrect international currency symbol",
                currency.getCurrencyCode().equals(
                        dfs.getInternationalCurrencySymbol()));
        String symbol = dfs.getCurrencySymbol();
        dfs.setInternationalCurrencySymbol("bogus");
        assertNull("Test2: Returned incorrect currency", dfs.getCurrency());
        assertTrue("Test2: Returned incorrect currency symbol", dfs
                .getCurrencySymbol().equals(symbol));
        assertEquals("Test2: Returned incorrect international currency symbol",
                "bogus", dfs.getInternationalCurrencySymbol());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMinusSign",
        args = {char.class}
    )
    public void test_setMinusSignC() {
        dfs.setMinusSign('&');
        assertEquals("Returned incorrect MinusSign symbol", '&', dfs
                .getMinusSign());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMonetaryDecimalSeparator",
        args = {char.class}
    )
    public void test_setMonetaryDecimalSeparatorC() {
        try {
            dfs.setMonetaryDecimalSeparator('#');
            assertEquals("Returned incorrect MonetaryDecimalSeparator symbol",
                    '#', dfs.getMonetaryDecimalSeparator());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setNaN",
        args = {java.lang.String.class}
    )
    public void test_setNaNLjava_lang_String() {
        dfs.setNaN("NAN!!");
        assertEquals("Returned incorrect nan symbol", "NAN!!", dfs.getNaN());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPatternSeparator",
        args = {char.class}
    )
    public void test_setPatternSeparatorC() {
        dfs.setPatternSeparator('X');
        assertEquals("Returned incorrect PatternSeparator symbol", 'X', dfs
                .getPatternSeparator());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPercent",
        args = {char.class}
    )
    public void test_setPercentC() {
        dfs.setPercent('*');
        assertEquals("Returned incorrect Percent symbol", '*', dfs.getPercent());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPerMill",
        args = {char.class}
    )
    public void test_setPerMillC() {
        dfs.setPerMill('#');
        assertEquals("Returned incorrect PerMill symbol", '#', dfs.getPerMill());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setZeroDigit",
        args = {char.class}
    )
    public void test_setZeroDigitC() {
        dfs.setZeroDigit('*');
        assertEquals("Set incorrect ZeroDigit symbol", '*', dfs.getZeroDigit());
    }
    protected void setUp() {
        dfs = new DecimalFormatSymbols();
        dfsUS = new DecimalFormatSymbols(new Locale("en", "us"));
    }
    protected void tearDown() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks serialization mechanism.",
        method = "!SerializationSelf",
        args = {}
    )
    public void test_serialization() throws Exception {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        Currency currency = symbols.getCurrency();
        assertNotNull(currency);
        ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOStream = new ObjectOutputStream(byteOStream);
        objectOStream.writeObject(symbols);
        ObjectInputStream objectIStream = new ObjectInputStream(
                new ByteArrayInputStream(byteOStream.toByteArray()));
        DecimalFormatSymbols symbolsD = (DecimalFormatSymbols) objectIStream
                .readObject();
        currency = symbolsD.getCurrency();
        assertNotNull(currency);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Make sure all fields have non default values.",
        method = "!SerializationGolden",
        args = {}
    )
    @KnownFailure("Deserialized object is not equal to the original object." +
            "Test passes on RI.")
    public void test_RIHarmony_compatible() throws Exception {
        ObjectInputStream i = null;
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(
                    Locale.FRANCE);
            i = new ObjectInputStream(
                    getClass()
                            .getClassLoader()
                            .getResourceAsStream(
                    "serialization/java/text/DecimalFormatSymbols.ser"));
            DecimalFormatSymbols symbolsD = (DecimalFormatSymbols) i
                    .readObject();
            assertEquals(symbols, symbolsD);
        } catch(NullPointerException e) {
            assertNotNull("Failed to load /serialization/java/text/" +
                    "DecimalFormatSymbols.ser", i);
        } finally {
            try {
                if (i != null) {
                    i.close();
                }
            } catch (Exception e) {
            }
        }
        assertDecimalFormatSymbolsRIFrance(dfs);
    }
    static void assertDecimalFormatSymbolsRIFrance(DecimalFormatSymbols dfs) {
        assertEquals("EUR", dfs.getCurrency().getCurrencyCode());
        assertEquals("\u20AC", dfs.getCurrencySymbol());
        assertEquals(',', dfs.getDecimalSeparator());
        assertEquals('#', dfs.getDigit());
        assertEquals('\u00a0', dfs.getGroupingSeparator());
        assertEquals("\u221e", dfs.getInfinity());
        assertEquals("EUR", dfs.getInternationalCurrencySymbol());
        assertEquals('-', dfs.getMinusSign());
        assertEquals(',', dfs.getMonetaryDecimalSeparator());
        assertEquals("\uFFFD", dfs.getNaN());
        assertEquals('\u003b', dfs.getPatternSeparator());
        assertEquals('\u2030', dfs.getPerMill());
        assertEquals('%', dfs.getPercent());
        assertEquals('0', dfs.getZeroDigit());
    }
}
