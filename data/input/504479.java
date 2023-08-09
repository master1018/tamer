public class GeocoderTest extends AndroidTestCase {
    public void testGeocoder() throws Exception {
        Locale locale = new Locale("en", "us");
        Geocoder g = new Geocoder(mContext, locale);
        List<Address> addresses1 = g.getFromLocation(37.435067, -122.166767, 2);
        assertNotNull(addresses1);
        assertEquals(1, addresses1.size());
        Address addr = addresses1.get(0);
        assertEquals("94305", addr.getFeatureName());
        assertEquals("Palo Alto, CA 94305", addr.getAddressLine(0));
        assertEquals("USA", addr.getAddressLine(1));
        assertEquals("94305", addr.getPostalCode());
        assertFalse(Math.abs(addr.getLatitude() - 37.4240385) > 0.1);
        List<Address> addresses2 = g.getFromLocationName("San Francisco, CA", 1);
        assertNotNull(addresses2);
        assertEquals(1, addresses2.size());
        addr = addresses2.get(0);
        assertEquals("San Francisco", addr.getFeatureName());
        assertEquals("San Francisco, CA", addr.getAddressLine(0));
        assertEquals("United States", addr.getAddressLine(1));
        assertEquals("San Francisco", addr.getLocality());
        assertEquals("CA", addr.getAdminArea());
        assertEquals(null, addr.getPostalCode());
        assertFalse(Math.abs(addr.getLatitude() - 37.77916) > 0.1);
    }
}
