@TestTargetClass(CacheRequest.class) 
public class CacheRequestTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "abort",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CacheRequest",
            args = {}
        )
    })
    public void test_abort() {
        MockCacheRequest mcr = new MockCacheRequest();
        mcr.abort();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getBody",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CacheRequest",
            args = {}
        )
    })
    public void test_getBody() throws IOException {
        MockCacheRequest mcr = new MockCacheRequest();
        assertNull(mcr.getBody());
    }
    class MockCacheRequest extends CacheRequest {
        MockCacheRequest() {
            super();
        }
        @Override
        public void abort() {
        }
        @Override
        public OutputStream getBody() throws IOException {
            return null;
        }
    }
}
