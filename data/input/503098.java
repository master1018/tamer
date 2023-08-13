public class PostalSplitterForJapaneseTest extends TestCase {
    private PostalSplitter mPostalSplitter;
    private static final String COUNTRY = "\u65E5\u672C";
    private static final String POSTCODE = "163-8001";
    private static final String REGION = "\u6771\u4EAC\u90FD";
    private static final String CITY = "\u65B0\u5BBF\u533A";
    private static final String STREET = "\u897F\u65B0\u5BBF 2-8-1";
    private static final String POBOX = "\u79C1\u66F8\u7BB1";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPostalSplitter = new PostalSplitter(Locale.JAPAN);
    }
    public void testNull() {
        assertSplitPostal(null, null, null, null, null, null, null, null);
        assertJoinedPostal(null, null, null, null, null, null, null, null);
    }
    public void testEmpty() {
        assertSplitPostal("", null, null, null, null, null, null, null);
        assertJoinedPostal(null, null, null, null, null, null, null, null);
    }
    public void testSpaces() {
        assertSplitPostal(" ", " ", null, null, null, null, null, null);
        assertJoinedPostal(" ", " ", null, null, null, null, null, null);
    }
    public void testPobox() {
        assertJoinedPostal(CITY + "\n" + POBOX, null, POBOX, null, CITY, null, null, null);
    }
    public void testNormal() {
        assertJoinedPostal(POSTCODE + "\n" + REGION + " " + CITY + "\n" + STREET,
                STREET, null, null, CITY, REGION, POSTCODE, null);
    }
    public void testMissingRegion() {
        assertJoinedPostal(POSTCODE + "\n" + REGION + "\n" + STREET,
                STREET, null, null, REGION, null, POSTCODE, null);
        assertJoinedPostal(POSTCODE + "\n" + STREET,
                STREET, null, null, null, null, POSTCODE, null);
        assertJoinedPostal(COUNTRY + " " + POSTCODE + "\n" + STREET,
                STREET, null, null, null, null, POSTCODE, COUNTRY);
    }
    public void testMissingPostcode() {
        assertJoinedPostal(REGION + " " + CITY + "\n" + STREET,
                STREET, null, null, CITY, REGION, null, null);
        assertJoinedPostal(COUNTRY + "\n" + REGION + " " + CITY + "\n" + STREET,
                STREET, null, null, CITY, REGION, null, COUNTRY);
        assertJoinedPostal(COUNTRY + "\n" + STREET,
                STREET, null, null, null, null, null, COUNTRY);
    }
    public void testMissingStreet() {
        assertJoinedPostal(COUNTRY + "\n" + STREET,
                null, null, STREET, null, null, null, COUNTRY);
    }
    private void assertSplitPostal(String formattedPostal, String street, String pobox,
            String neighborhood, String city, String region, String postcode, String country) {
        final Postal postal = new Postal();
        mPostalSplitter.split(postal, formattedPostal);
        assertEquals(street, postal.street);
        assertEquals(pobox, postal.pobox);
        assertEquals(neighborhood, postal.neighborhood);
        assertEquals(city, postal.city);
        assertEquals(region, postal.region);
        assertEquals(postcode, postal.postcode);
        assertEquals(country, postal.country);
    }
    private void assertJoinedPostal(String formattedPostal, String street, String pobox,
            String neighborhood, String city, String region, String postcode, String country) {
        final Postal postal = new Postal();
        postal.street = street;
        postal.pobox = pobox;
        postal.neighborhood = neighborhood;
        postal.city = city;
        postal.region = region;
        postal.postcode = postcode;
        postal.country = country;
        final String joined = mPostalSplitter.join(postal);
        assertEquals(formattedPostal, joined);
    }
}
