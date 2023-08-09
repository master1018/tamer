@TestTargetClass(SSLEngineResult.Status.class) 
public class SSLEngineResultStatusTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void test_SSLEngineResultStatus_values() {
        boolean flag = false;
        String[] str = {"BUFFER_OVERFLOW", "BUFFER_UNDERFLOW", "CLOSED", "OK"};
        SSLEngineResult.Status[] enS = SSLEngineResult.Status.values();
        if (enS.length == str.length) {
            for (int i = 0; i < enS.length; i++) {
                flag = false;
                for (int j = 0; j < str.length; j++) {
                    if (enS[i].toString() == str[j]) {
                        flag = true;
                        break;
                    }
                }
            }
            assertTrue("Incorrect Status", flag);
        } else {
            fail("Incorrect number of enum constant was returned");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {String.class}
    )
    public void test_SSLEngineResultStatus_valueOf() {
        String[] str = {"BUFFER_OVERFLOW", "BUFFER_UNDERFLOW", "CLOSED", "OK"};
        String[] str_invalid = {"", "OK1", "BUFFER_overflow", "BUFFER_UND",
                "CLOSED_CLOSED", "Bad string for verification valueOf method"};
        SSLEngineResult.Status enS;
        for (int i = 0; i < str.length; i++) {
            try {
                enS = SSLEngineResult.Status.valueOf(str[i]);
                assertEquals("Incorrect Status", enS.toString(), str[i]);
            } catch (Exception e) {
                fail("Unexpected exception " + e + " was thrown for " + str[i]);
            }
        }
        for (int i = 0; i < str_invalid.length; i++) {
            try {
                enS = SSLEngineResult.Status.valueOf(str_invalid[i]);
                fail("IllegalArgumentException should be thrown for " + str_invalid[i]);
            } catch (IllegalArgumentException iae) {
            }
        }
        try {
            enS = SSLEngineResult.Status.valueOf(null);
            fail("NullPointerException/IllegalArgumentException should be thrown for NULL parameter");
        } catch (NullPointerException npe) {
        } catch (IllegalArgumentException iae) {
        }
    }
}