public class TimeZoneTest extends junit.framework.TestCase {
    public void test_useDaylightTime() {
        TimeZone asiaTaipei = TimeZone.getTimeZone("Asia/Taipei");
        assertFalse("Taiwan doesn't use DST", asiaTaipei.useDaylightTime());
    }
}
