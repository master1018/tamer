public class SimUtilsTest extends TestCase {
    @SmallTest
    public void testBasic() throws Exception {
        byte[] data, data2;
        data = IccUtils.hexStringToBytes("981062400510444868f2");
        assertEquals("8901260450014484862", IccUtils.bcdToString(data, 0, data.length));
        assertEquals("0126045001448486", IccUtils.bcdToString(data, 1, data.length - 2));
        data = IccUtils.hexStringToBytes("98F062400510444868f2");
        assertEquals("890", IccUtils.bcdToString(data, 0, data.length));
        assertEquals(98, IccUtils.gsmBcdByteToInt((byte) 0x89));
        assertEquals(8, IccUtils.gsmBcdByteToInt((byte) 0x8c));
        assertEquals(89, IccUtils.cdmaBcdByteToInt((byte) 0x89));
        assertEquals(80, IccUtils.cdmaBcdByteToInt((byte) 0x8c));
        data = IccUtils.hexStringToBytes("00566f696365204d61696c07918150367742f3ffffffffffff");
        assertEquals("Voice Mail", IccUtils.adnStringFieldToString(data, 1, data.length - 15));
        data = IccUtils.hexStringToBytes("809673539A5764002F004DFFFFFFFFFF");
        assertEquals("\u9673\u539A\u5764/M", IccUtils.adnStringFieldToString(data, 0, data.length));
        data = IccUtils.hexStringToBytes("810A01566fec6365204de0696cFFFFFF");
        assertEquals("Vo\u00ECce M\u00E0il", IccUtils.adnStringFieldToString(data, 0, data.length));
        data = IccUtils.hexStringToBytes("820505302D82d32d31");
        assertEquals("-\u0532\u0583-1", IccUtils.adnStringFieldToString(data, 0, data.length));
    }
}
