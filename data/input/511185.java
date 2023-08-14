public class MccTableTest extends AndroidTestCase {
    private final static String LOG_TAG = "GSM";
    @SmallTest
    public void testTimeZone() throws Exception {
        assertEquals(MccTable.defaultTimeZoneForMcc(208), "Europe/Paris");
        assertEquals(MccTable.defaultTimeZoneForMcc(232), "Europe/Vienna");
        assertEquals(MccTable.defaultTimeZoneForMcc(655), "Africa/Johannesburg");
        assertEquals(MccTable.defaultTimeZoneForMcc(440), "Asia/Tokyo");
        assertEquals(MccTable.defaultTimeZoneForMcc(441), "Asia/Tokyo");
        assertEquals(MccTable.defaultTimeZoneForMcc(525), "Asia/Singapore");
        assertEquals(MccTable.defaultTimeZoneForMcc(240), null);  
        assertEquals(MccTable.defaultTimeZoneForMcc(0), null);    
        assertEquals(MccTable.defaultTimeZoneForMcc(2000), null); 
    }
    @SmallTest
    public void testCountryCode() throws Exception {
        assertEquals(MccTable.countryCodeForMcc(270), "lu");
        assertEquals(MccTable.countryCodeForMcc(202), "gr");
        assertEquals(MccTable.countryCodeForMcc(750), "fk");
        assertEquals(MccTable.countryCodeForMcc(646), "mg");
        assertEquals(MccTable.countryCodeForMcc(314), "us");
        assertEquals(MccTable.countryCodeForMcc(300), "");  
        assertEquals(MccTable.countryCodeForMcc(0), "");    
        assertEquals(MccTable.countryCodeForMcc(2000), ""); 
    }
    @SmallTest
    public void testLang() throws Exception {
        assertEquals(MccTable.defaultLanguageForMcc(311), "en");
        assertEquals(MccTable.defaultLanguageForMcc(232), "de");
        assertEquals(MccTable.defaultLanguageForMcc(230), "cs");
        assertEquals(MccTable.defaultLanguageForMcc(204), "nl");
        assertEquals(MccTable.defaultLanguageForMcc(274), null);  
        assertEquals(MccTable.defaultLanguageForMcc(0), null);    
        assertEquals(MccTable.defaultLanguageForMcc(2000), null); 
    }
    @SmallTest
    public void testSmDigits() throws Exception {
        assertEquals(MccTable.smallestDigitsMccForMnc(312), 3);
        assertEquals(MccTable.smallestDigitsMccForMnc(430), 2);
        assertEquals(MccTable.smallestDigitsMccForMnc(365), 3);
        assertEquals(MccTable.smallestDigitsMccForMnc(536), 2);
        assertEquals(MccTable.smallestDigitsMccForMnc(352), 2);  
        assertEquals(MccTable.smallestDigitsMccForMnc(0), 2);    
        assertEquals(MccTable.smallestDigitsMccForMnc(2000), 2); 
    }
    @SmallTest
    public void testWifi() throws Exception {
        assertEquals(MccTable.wifiChannelsForMcc(262), 13);
        assertEquals(MccTable.wifiChannelsForMcc(234), 13);
        assertEquals(MccTable.wifiChannelsForMcc(505), 11);
        assertEquals(MccTable.wifiChannelsForMcc(313), 11);
        assertEquals(MccTable.wifiChannelsForMcc(330), 0);  
        assertEquals(MccTable.wifiChannelsForMcc(0), 0);    
        assertEquals(MccTable.wifiChannelsForMcc(2000), 0); 
    }
}
