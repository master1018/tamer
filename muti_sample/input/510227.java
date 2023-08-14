public class LocaleTest extends TestCase {
    @SmallTest
    public void testLocale() throws Exception {
        Locale locale = new Locale("en");
        assertEquals("en", locale.toString());
        locale = new Locale("en", "US");
        assertEquals("en_US", locale.toString());
        locale = new Locale("en", "", "POSIX");
        assertEquals("en__POSIX", locale.toString());
        locale = new Locale("en", "US", "POSIX");
        assertEquals("en_US_POSIX", locale.toString());
    }
    @LargeTest
    public void testResourceBundles() throws Exception {
        Locale eng = new Locale("en", "US");
        DateFormatSymbols engSymbols = new DateFormatSymbols(eng);
        TimeZone berlin = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals("January", engSymbols.getMonths()[0]);
        assertEquals("Sunday", engSymbols.getWeekdays()[Calendar.SUNDAY]);
        assertEquals("Central European Time",
                berlin.getDisplayName(false, TimeZone.LONG, eng));
        assertEquals("Central European Summer Time",
                berlin.getDisplayName(true, TimeZone.LONG, eng));
        assertTrue(engSymbols.getZoneStrings().length > 100);
    }
    @Suppress
    public void testICULocales() {
        String[] locales = new String[] {
                "en_US", "es_US", "en_GB", "fr_FR", "de_DE", "de_AT", "cs_CZ", "nl_NL" };
        String[] mondays = new String[] {
                "Monday", "lunes", "Monday", "lundi", "Montag", "Montag", "pond\u011bl\u00ed", "maandag" };
        String[] currencies = new String[] {
                "USD", "USD", "GBP", "EUR", "EUR", "EUR", "CZK", "EUR"};
        for (int i = 0; i < locales.length; i++) {
            Locale l = new Locale(locales[i].substring(0, 2), locales[i].substring(3));
            DateFormatSymbols d = new DateFormatSymbols(l);
            assertEquals("Monday name for " + locales[i] + " must match",
                    mondays[i], d.getWeekdays()[2]);
            Currency c = Currency.getInstance(l);
            assertEquals("Currency code for " + locales[i] + " must match",
                    currencies[i], c.getCurrencyCode());
        }
    }
    @MediumTest
    public void testICUConverters() {
        String[] encodings = new String[] {
                "US-ASCII",
                "UTF-8",
                "UTF-16",
                "UTF-16BE",
                "UTF-16LE",
                "ISO-8859-1",
                "ISO-8859-2",
                "ISO-8859-3",
                "ISO-8859-4",
                "ISO-8859-5",
                "ISO-8859-6",
                "ISO-8859-7",
                "ISO-8859-8",
                "ISO-8859-8-I",
                "ISO-8859-9",
                "ISO-8859-10", 
                "ISO-8859-11", 
                "ISO-8859-13",
                "ISO-8859-14", 
                "ISO-8859-15",
                "ISO-8859-16", 
                "ISO-2022-JP",
                "Windows-950",
                "Windows-1250",
                "Windows-1251",
                "Windows-1252",
                "Windows-1253",
                "Windows-1254",
                "Windows-1255",
                "Windows-1256",
                "Windows-1257",
                "Windows-1258",              
                "Big5",
                "CP864",
                "CP874",
                "EUC-CN",
                "EUC-JP",
                "KOI8-R",
                "Macintosh",
                "GBK",
                "GB2312",
                "EUC-KR",
                "GSM0338" };
        for (int i = 0; i < encodings.length; i++) {
            assertTrue("Charset " + encodings[i] + " must be supported",
                    Charset.isSupported(encodings[i]));
            Charset cs = Charset.forName(encodings[i]);
            android.util.Log.d("LocaleTest", cs.name());
            Set<String> aliases = cs.aliases();
            for (String s: aliases) {
                android.util.Log.d("LocaleTest", " - " + s);
            }
        }
        assertFalse("Charset IBM-37 must not be supported",
                Charset.isSupported("IBM-37"));
        assertFalse("Charset KLINGON must not be supported",
                Charset.isSupported("KLINGON"));
        Charset cs = Charset.forName("EUC-JP");
        assertTrue("EUC-JP must use 'ibm-954_P101-2007'", cs.aliases().contains("ibm-954_P101-2007"));
    }
}
