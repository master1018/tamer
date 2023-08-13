@TestTargetClass(SSLEngineResult.HandshakeStatus.class) 
public class SSLEngineResultHandshakeStatusTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void test_SSLEngineResultHandshakeStatus_values() {
        String[] str = {"NOT_HANDSHAKING", "FINISHED", "NEED_TASK", "NEED_WRAP", "NEED_UNWRAP"};
        SSLEngineResult.HandshakeStatus[] enS = SSLEngineResult.HandshakeStatus.values();
        if (enS.length == str.length) {
            for (int i = 0; i < enS.length; i++) {
                assertEquals("Incorrect Status", enS[i].toString(), str[i]);
            }
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
        String[] str = {"FINISHED", "NEED_TASK", "NEED_UNWRAP", "NEED_WRAP", "NOT_HANDSHAKING"};
        String[] str_invalid = {"", "FINISHED1", "NEED_task", "NEED_UN",
                "NEED_WRAP_WRAP", "not_HANDSHAKING", "Bad string for verification valueOf method"};
        SSLEngineResult.HandshakeStatus enS;
        for (int i = 0; i < str.length; i++) {
            try {
                enS = SSLEngineResult.HandshakeStatus.valueOf(str[i]);
                assertEquals("Incorrect Status", enS.toString(), str[i]);
            } catch (Exception e) {
                fail("Unexpected exception " + e + " was thrown for " + str[i]);
            }
        }
        for (int i = 0; i < str_invalid.length; i++) {
            try {
                enS = SSLEngineResult.HandshakeStatus.valueOf(str_invalid[i]);
                fail("IllegalArgumentException should be thrown for " + str_invalid[i]);
            } catch (IllegalArgumentException iae) {
            }
        }
        try {
            enS = SSLEngineResult.HandshakeStatus.valueOf(null);
            fail("NullPointerException/IllegalArgumentException should be thrown for NULL parameter");
        } catch (NullPointerException npe) {
        } catch (IllegalArgumentException iae) {
        }
    }
}