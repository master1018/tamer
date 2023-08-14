@TestTargetClass(Formatter.class)
public class FormatterTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "formatFileSize",
        args = {android.content.Context.class, long.class}
    )
    public void testFormatFileSize() {
        assertEquals("", Formatter.formatFileSize(null, 0));
        MathContext mc = MathContext.DECIMAL64;
        BigDecimal bd = new BigDecimal((long) 1024, mc);
        assertEquals("0.00B", Formatter.formatFileSize(mContext, 0));
        assertEquals("899B", Formatter.formatFileSize(mContext, 899));
        assertEquals("1.00KB", Formatter.formatFileSize(mContext, bd.pow(1).longValue()));
        assertEquals("1.00MB", Formatter.formatFileSize(mContext, bd.pow(2).longValue()));
        assertEquals("1.00GB", Formatter.formatFileSize(mContext, bd.pow(3).longValue()));
        assertEquals("1.00TB", Formatter.formatFileSize(mContext, bd.pow(4).longValue()));
        assertEquals("1.00PB", Formatter.formatFileSize(mContext, bd.pow(5).longValue()));
        assertEquals("1024PB", Formatter.formatFileSize(mContext, bd.pow(6).longValue()));
        assertEquals("-1.00B", Formatter.formatFileSize(mContext, -1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "formatIpAddress",
        args = {int.class}
    )
    public void testFormatIpAddress() {
        assertEquals("1.0.168.192", Formatter.formatIpAddress(0xC0A80001));
        assertEquals("1.0.0.127", Formatter.formatIpAddress(0x7F000001));
        assertEquals("35.182.168.192", Formatter.formatIpAddress(0xC0A8B623));
        assertEquals("0.255.255.255", Formatter.formatIpAddress(0xFFFFFF00));
        assertEquals("222.5.15.10", Formatter.formatIpAddress(0x0A0F05DE));
    }
}
