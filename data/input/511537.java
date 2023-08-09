@TestTargetClass(SSLEngineResult.class) 
public class SSLEngineResultTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLEngineResult",
        args = {javax.net.ssl.SSLEngineResult.Status.class, javax.net.ssl.SSLEngineResult.HandshakeStatus.class, int.class, int.class}
    )
    public void test_ConstructorLjavax_net_ssl_SSLEngineResult_StatusLjavax_net_ssl_SSLEngineResult_HandshakeStatusII() {
        int[] neg = { -1, -10, -1000, Integer.MIN_VALUE,
                (Integer.MIN_VALUE + 1) };
        try {
            new SSLEngineResult(null, SSLEngineResult.HandshakeStatus.FINISHED,
                    1, 1);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, null,
                    1, 1);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        for (int i = 0; i < neg.length; i++) {
            try {
                new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW,
                        SSLEngineResult.HandshakeStatus.FINISHED, neg[i], 1);
                fail("IllegalArgumentException must be thrown");
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < neg.length; i++) {
            try {
                new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW,
                        SSLEngineResult.HandshakeStatus.FINISHED, 1, neg[i]);
                fail("IllegalArgumentException must be thrown");
            } catch (IllegalArgumentException e) {
            }
        }
        try {
            SSLEngineResult res = new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW,
                    SSLEngineResult.HandshakeStatus.FINISHED, 1, 2);
            assertNotNull("Null object", res);
            assertEquals(1, res.bytesConsumed());
            assertEquals(2, res.bytesProduced());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "bytesConsumed",
        args = {}
    )
    public void test_bytesConsumed() {
        int[] pos = { 0, 1, 1000, Integer.MAX_VALUE, (Integer.MAX_VALUE - 1) };
        SSLEngineResult.Status [] enS =
            SSLEngineResult.Status.values();
        SSLEngineResult.HandshakeStatus [] enHS =
            SSLEngineResult.HandshakeStatus.values();
        for (int i = 0; i < enS.length; i++) {
            for (int j = 0; j < enHS.length; j++) {
                for (int n = 0; n < pos.length; n++) {
                    for (int l = 0; l < pos.length; l++) {
                        SSLEngineResult res = new SSLEngineResult(enS[i],
                                enHS[j], pos[n], pos[l]);
                        assertEquals("Incorrect bytesConsumed", pos[n],
                                res.bytesConsumed());
                    }
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "bytesProduced",
        args = {}
    )
    public void test_bytesProduced() {
        int[] pos = { 0, 1, 1000, Integer.MAX_VALUE, (Integer.MAX_VALUE - 1) };
        SSLEngineResult.Status [] enS =
            SSLEngineResult.Status.values();
        SSLEngineResult.HandshakeStatus [] enHS =
            SSLEngineResult.HandshakeStatus.values();
        for (int i = 0; i < enS.length; i++) {
            for (int j = 0; j < enHS.length; j++) {
                for (int n = 0; n < pos.length; n++) {
                    for (int l = 0; l < pos.length; ++l) {
                        SSLEngineResult res = new SSLEngineResult(enS[i],
                                enHS[j], pos[n], pos[l]);
                        assertEquals("Incorrect bytesProduced", pos[l],
                                res.bytesProduced());
                    }
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHandshakeStatus",
        args = {}
    )
    public void test_getHandshakeStatus() {
        int[] pos = { 0, 1, 1000, Integer.MAX_VALUE, (Integer.MAX_VALUE - 1) };
        SSLEngineResult.Status [] enS =
            SSLEngineResult.Status.values();
        SSLEngineResult.HandshakeStatus [] enHS =
            SSLEngineResult.HandshakeStatus.values();
        for (int i = 0; i < enS.length; i++) {
            for (int j = 0; j < enHS.length; j++) {
                for (int n = 0; n < pos.length; n++) {
                    for (int l = 0; l < pos.length; ++l) {
                        SSLEngineResult res = new SSLEngineResult(enS[i],
                                enHS[j], pos[n], pos[l]);
                        assertEquals("Incorrect HandshakeStatus", enHS[j],
                                res.getHandshakeStatus());
                    }
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getStatus",
        args = {}
    )
    public void test_getStatus() {
        int[] pos = { 0, 1, 1000, Integer.MAX_VALUE, (Integer.MAX_VALUE - 1) };
        SSLEngineResult.Status [] enS =
            SSLEngineResult.Status.values();
        SSLEngineResult.HandshakeStatus [] enHS =
            SSLEngineResult.HandshakeStatus.values();
        for (int i = 0; i < enS.length; i++) {
            for (int j = 0; j < enHS.length; j++) {
                for (int n = 0; n < pos.length; n++) {
                    for (int l = 0; l < pos.length; ++l) {
                        SSLEngineResult res = new SSLEngineResult(enS[i],
                                enHS[j], pos[n], pos[l]);
                        assertEquals("Incorrect Status", enS[i],
                                res.getStatus());
                    }
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        int[] pos = { 0, 1, 1000, Integer.MAX_VALUE, (Integer.MAX_VALUE - 1) };
        SSLEngineResult.Status [] enS =
            SSLEngineResult.Status.values();
        SSLEngineResult.HandshakeStatus [] enHS =
            SSLEngineResult.HandshakeStatus.values();
        for (int i = 0; i < enS.length; i++) {
            for (int j = 0; j < enHS.length; j++) {
                for (int n = 0; n < pos.length; n++) {
                    for (int l = 0; l < pos.length; ++l) {
                        SSLEngineResult res = new SSLEngineResult(enS[i],
                                enHS[j], pos[n], pos[l]);
                        assertNotNull("Result of toSring() method is null",
                                res.toString());
                    }
                }
            }
        }
    }
    private boolean findEl(Object[] arr, Object el) {
        boolean ok = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(el)) {
                ok = true;
                break;
            }
        }
        return ok;
    }
}
