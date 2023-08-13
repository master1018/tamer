@TestTargetClass(LocalSocketAddress.class)
public class LocalSocketAddressTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test LocalSocketAddress",
            method = "LocalSocketAddress",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test LocalSocketAddress",
            method = "LocalSocketAddress",
            args = {java.lang.String.class, android.net.LocalSocketAddress.Namespace.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test LocalSocketAddress",
            method = "getName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test LocalSocketAddress",
            method = "getNamespace",
            args = {}
        )
    })
    public void testNewLocalSocketAddressWithDefaultNamespace() {
        LocalSocketAddress localSocketAddress = new LocalSocketAddress("name");
        assertEquals("name", localSocketAddress.getName());
        assertEquals(Namespace.ABSTRACT, localSocketAddress.getNamespace());
        LocalSocketAddress localSocketAddress2 =
                new LocalSocketAddress("name2", Namespace.ABSTRACT);
        assertEquals("name2", localSocketAddress2.getName());
        assertEquals(Namespace.ABSTRACT, localSocketAddress2.getNamespace());
        LocalSocketAddress localSocketAddress3 =
                new LocalSocketAddress("name3", Namespace.FILESYSTEM);
        assertEquals("name3", localSocketAddress3.getName());
        assertEquals(Namespace.FILESYSTEM, localSocketAddress3.getNamespace());
        LocalSocketAddress localSocketAddress4 =
                new LocalSocketAddress("name4", Namespace.RESERVED);
        assertEquals("name4", localSocketAddress4.getName());
        assertEquals(Namespace.RESERVED, localSocketAddress4.getNamespace());
    }
}
