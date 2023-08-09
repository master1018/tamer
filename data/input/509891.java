@TestTargetClass(SelectorProvider.class)
public class SelectorProviderTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SelectorProvider",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "provider",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "inheritedChannel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "openDatagramChannel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "openPipe",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "openServerSocketChannel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "openSocketChannel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "openSelector",
            args = {}
        )
    })
    public void test_open_methods() throws Exception {
        assertNotNull(SelectorProvider.provider());
        SelectorProvider.provider().inheritedChannel();
        assertNotNull(SelectorProvider.provider().openDatagramChannel());
        assertNotNull(SelectorProvider.provider().openPipe());
        assertNotNull(SelectorProvider.provider().openServerSocketChannel());
        assertNotNull(SelectorProvider.provider().openSocketChannel());
        assertNotNull(SelectorProvider.provider().openSelector());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "provider",
        args = {}
    )
    public void test_provider_security() {        
        SecurityManager originalSecuirtyManager = System.getSecurityManager();
        System.setSecurityManager(new MockSelectorProviderSecurityManager());
        try {
            new MockSelectorProvider();
            fail("should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(originalSecuirtyManager);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "provider",
        args = {}
    )
    public void test_provider_security_twice() {
        SelectorProvider.provider();
        SecurityManager originalSecuirtyManager = System.getSecurityManager();
        System.setSecurityManager(new MockSelectorProviderSecurityManager());
        try {
            SelectorProvider testProvider = SelectorProvider.provider();
            assertNotNull(testProvider);
        } finally {
            System.setSecurityManager(originalSecuirtyManager);
        }
    }
    private static class MockSelectorProviderSecurityManager extends
            SecurityManager {
        public MockSelectorProviderSecurityManager() {
            super();
        }
        public void checkPermission(Permission perm) {
            if (perm instanceof RuntimePermission) {
                if ("selectorProvider".equals(perm.getName())) {
                    throw new SecurityException();
                }
            }
        }
        public void checkPermission(Permission perm, Object context) {
            if (perm instanceof RuntimePermission) {
                if ("selectorProvider".equals(perm.getName())) {
                    throw new SecurityException();
                }
            }
        }
    }
    private class MockSelectorProvider extends SelectorProvider {
        public MockSelectorProvider() {
            super();
        }
        public DatagramChannel openDatagramChannel() throws IOException {
            return null;
        }
        public Pipe openPipe() throws IOException {
            return null;
        }
        public AbstractSelector openSelector() throws IOException {
            return MockAbstractSelector.openSelector();
        }
        public ServerSocketChannel openServerSocketChannel() throws IOException {
            return null;
        }
        public SocketChannel openSocketChannel() throws IOException {
            return null;
        }
    }
}