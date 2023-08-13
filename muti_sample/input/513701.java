public class AdnRecordTest extends TestCase {
    @SmallTest
    public void testBasic() throws Exception {
        AdnRecord adn;
        adn = new AdnRecord(
                IccUtils.hexStringToBytes("566F696365204D61696C07918150367742F3FFFFFFFFFFFF"));
        assertEquals("Voice Mail", adn.getAlphaTag());
        assertEquals("+18056377243", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
        assertEquals("", adn.getAlphaTag());
        assertEquals("", adn.getNumber());
        assertTrue(adn.isEmpty());
        adn = new AdnRecord(IccUtils.hexStringToBytes( "FF"));
        assertEquals("", adn.getAlphaTag());
        assertEquals("", adn.getNumber());
        assertTrue(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes("566F696365204D61696C07FF8150367742F3FFFFFFFFFFFF"));
        assertEquals("Voice Mail", adn.getAlphaTag());
        assertEquals("18056377243", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes("566F696365204D61696C07818150367742F3FFFFFFFFFFFF"));
        assertEquals("Voice Mail", adn.getAlphaTag());
        assertEquals("18056377243", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes("566F696365204D61696C0F918150367742F3FFFFFFFFFFFF"));
        assertEquals("Voice Mail", adn.getAlphaTag());
        assertEquals("", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes("566F696365204D61696C00918150367742F3FFFFFFFFFFFF"));
        assertEquals("Voice Mail", adn.getAlphaTag());
        assertEquals("", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes("566F696365204D61696C0291FF50367742F3FFFFFFFFFFFF"));
        assertEquals("Voice Mail", adn.getAlphaTag());
        assertEquals("", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes("566F696365204D61696C0291F150367742F3FFFFFFFFFFFF"));
        assertEquals("Voice Mail", adn.getAlphaTag());
        assertEquals("+1", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes(
                        "4164676A6DFFFFFFFFFFFFFFFFFFFFFF0B918188551512C221436587FF01"));
        assertEquals("Adgjm", adn.getAlphaTag());
        assertEquals("+18885551212,12345678", adn.getNumber());
        assertFalse(adn.isEmpty());
        assertTrue(adn.hasExtendedRecord());
        adn.appendExtRecord(IccUtils.hexStringToBytes("0206092143658709ffffffffff"));
        assertEquals("Adgjm", adn.getAlphaTag());
        assertEquals("+18885551212,12345678901234567890", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes(
                        "4164676A6DFFFFFFFFFFFFFFFFFFFFFF0B918188551512C221436587FF01"));
        assertEquals("Adgjm", adn.getAlphaTag());
        assertEquals("+18885551212,12345678", adn.getNumber());
        assertFalse(adn.isEmpty());
        assertTrue(adn.hasExtendedRecord());
        adn.appendExtRecord(IccUtils.hexStringToBytes("0106092143658709ffffffffff"));
        assertEquals("Adgjm", adn.getAlphaTag());
        assertEquals("+18885551212,12345678", adn.getNumber());
        assertFalse(adn.isEmpty());
        adn = new AdnRecord(
                IccUtils.hexStringToBytes(
                        "4164676A6DFFFFFFFFFFFFFFFFFFFFFF0B918188551512C221436587FF01"));
        assertEquals("Adgjm", adn.getAlphaTag());
        assertEquals("+18885551212,12345678", adn.getNumber());
        assertFalse(adn.isEmpty());
        assertTrue(adn.hasExtendedRecord());
        adn.appendExtRecord(IccUtils.hexStringToBytes("020B092143658709ffffffffff"));
        assertEquals("Adgjm", adn.getAlphaTag());
        assertEquals("+18885551212,12345678", adn.getNumber());
        assertFalse(adn.isEmpty());
    }
}
