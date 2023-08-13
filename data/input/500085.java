@TestTargetClass(LocalServerSocket.class)
public class LocalServerSocketTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "accept",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFileDescriptor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalSocketAddress",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "LocalServerSocket",
            args = {java.io.FileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LocalServerSocket",
            args = {java.lang.String.class}
        )
    })
    @ToBeFixed(bug = "1520987", explanation = "Cannot find a proper FileDescriptor for " +
            "android.net.LocalServerSocket constructor")
    public void testLocalServerSocket() throws IOException {
        LocalServerSocket localServerSocket = new LocalServerSocket(LocalSocketTest.mSockAddr);
        assertNotNull(localServerSocket.getLocalSocketAddress());
        commonFunctions(localServerSocket);
    }
    public void commonFunctions(LocalServerSocket localServerSocket) throws IOException {
        LocalSocket clientSocket = new LocalSocket();
        clientSocket.connect(new LocalSocketAddress(LocalSocketTest.mSockAddr));
        LocalSocket serverSocket = localServerSocket.accept();
        OutputStream clientOutStream = clientSocket.getOutputStream();
        clientOutStream.write(12);
        InputStream serverInStream = serverSocket.getInputStream();
        assertEquals(12, serverInStream.read());
        OutputStream serverOutStream = serverSocket.getOutputStream();
        serverOutStream.write(3);
        InputStream clientInStream = clientSocket.getInputStream();
        assertEquals(3, clientInStream.read());
        assertNotNull(localServerSocket.getFileDescriptor());
        localServerSocket.close();
        assertNull(localServerSocket.getFileDescriptor());
    }
}
