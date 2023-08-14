@TestTargetClass(CacheResponse.class) 
public class CacheResponseTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBody",
        args = {}
    )
    public void test_getBody() throws IOException {
        MockCacheResponse mcr = new MockCacheResponse();
        assertNull(mcr.getBody());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getHeaders",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CacheResponse",
            args = {}
        )
    })
    public void test_getHeaders() throws IOException {
        MockCacheResponse mcr = new MockCacheResponse();
        assertNull(mcr.getHeaders());
    }
    class MockCacheResponse extends CacheResponse {
        MockCacheResponse() {
            super();
        }
        @Override
        public Map<String,List<String>> getHeaders() throws IOException {
            return null;
        }
        @Override
        public InputStream getBody() throws IOException {
            return null;
        }
    }    
}
