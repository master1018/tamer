@TestTargetClass(HttpRetryException.class) 
public class HttpRetryExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "HttpRetryException",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "responseCode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getReason",
            args = {}
        )
    })
    public void test_ConstructorLStringI() {
        String [] message = {"Test message", "", "Message", "~!@#$% &*(", null};
        int [] codes = {400, 404, 200, 500, 0};
        for(int i = 0; i < message.length; i++) {
            HttpRetryException hre = new HttpRetryException(message[i], 
                    codes[i]);
            assertEquals(message[i], hre.getReason());
            assertTrue("responseCode is incorrect: " + hre.responseCode(), 
                    hre.responseCode() == codes[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "HttpRetryException",
            args = {java.lang.String.class, int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getLocation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "responseCode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getReason",
            args = {}
        )
    })
    public void test_ConstructorLStringILString() {
        String [] message = {"Test message", "", "Message", "~!@#$% &*(", null};
        int [] codes = {400, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        String [] locations = {"file:\\error.txt", "http:\\localhost", 
                "", null, ""}; 
        for(int i = 0; i < message.length; i++) {
            HttpRetryException hre = new HttpRetryException(message[i], 
                    codes[i], locations[i]);
            assertEquals(message[i], hre.getReason());
            assertTrue("responseCode is incorrect: " + hre.responseCode(), 
                    hre.responseCode() == codes[i]);
            assertEquals(locations[i], hre.getLocation());
        }
    }
}
