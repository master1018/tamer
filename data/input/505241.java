public class UrlInterceptRegistryTest extends AndroidTestCase {
    private static class MockUrlInterceptHandler implements UrlInterceptHandler {
        private PluginData mData;
        private String mUrl;
        public MockUrlInterceptHandler(PluginData data, String url) {
            mData = data;
            mUrl = url;
        }
        public CacheResult service(String url, Map<String, String> headers) {
            return null;
        }
        public PluginData getPluginData(String url,
                                        Map<String,
                                        String> headers) {
            if (mUrl.equals(url)) {
                return mData;
            }
            return null;
        }
    }
    public void testGetPluginData() {
        PluginData data = new PluginData(null, 0 , null, 200);
        String url = new String("url1");
        MockUrlInterceptHandler handler1 =
                new MockUrlInterceptHandler(data, url);
        data = new PluginData(null, 0 , null, 404);
        url = new String("url2");
        MockUrlInterceptHandler handler2 =
                new MockUrlInterceptHandler(data, url);
        assertTrue(UrlInterceptRegistry.registerHandler(handler1));
        assertTrue(UrlInterceptRegistry.registerHandler(handler2));
        data = UrlInterceptRegistry.getPluginData("url1", null);
        assertTrue(data != null);
        assertTrue(data.getStatusCode() == 200);
        data = UrlInterceptRegistry.getPluginData("url2", null);
        assertTrue(data != null);
        assertTrue(data.getStatusCode() == 404);
        assertTrue(UrlInterceptRegistry.unregisterHandler(handler1));
        assertTrue(UrlInterceptRegistry.unregisterHandler(handler2));
    }
}
