public class LocalSocketTest extends TestCase {
    @SmallTest
    public void testBasic() throws Exception {
        LocalServerSocket ss;
        LocalSocket ls;
        LocalSocket ls1;
        ss = new LocalServerSocket("android.net.LocalSocketTest");
        ls = new LocalSocket();
        ls.connect(new LocalSocketAddress("android.net.LocalSocketTest"));
        ls1 = ss.accept();
        ls.getOutputStream().write(42);
        assertEquals(42, ls1.getInputStream().read());
        Credentials c = ls1.getPeerCredentials();
        MoreAsserts.assertNotEqual(0, c.getPid());
        ls.setFileDescriptorsForSend(
                new FileDescriptor[]{FileDescriptor.in});
        ls.getOutputStream().write(42);
        assertEquals(42, ls1.getInputStream().read());
        FileDescriptor[] out = ls1.getAncillaryFileDescriptors();
        assertEquals(1, out.length);
        ls1.getOutputStream().write(new byte[]{0, 1, 2, 3, 4, 5}, 1, 5);
        assertEquals(1, ls.getInputStream().read());
        assertEquals(4, ls.getInputStream().available());
        byte[] buffer = new byte[16];
        int countRead;
        countRead = ls.getInputStream().read(buffer, 1, 15);
        assertEquals(4, countRead);
        assertEquals(2, buffer[1]);
        assertEquals(3, buffer[2]);
        assertEquals(4, buffer[3]);
        assertEquals(5, buffer[4]);
        try {
            ls.getInputStream().read(buffer, 1, 16);
            fail("expected exception");
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        try {
            ls.getOutputStream().write(buffer, 1, 16);
            fail("expected exception");
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        try {
            ls.getOutputStream().write(buffer, -1, 15);
            fail("expected exception");
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        try {
            ls.getOutputStream().write(buffer, 0, -1);
            fail("expected exception");
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        try {
            ls.getInputStream().read(buffer, -1, 15);
            fail("expected exception");
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        try {
            ls.getInputStream().read(buffer, 0, -1);
            fail("expected exception");
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        ls.getOutputStream().write(42);
        countRead = ls1.getInputStream().read(buffer, 0, 0);
        assertEquals(0, countRead);
        assertEquals(42, ls1.getInputStream().read());
        ss.close();
        ls.close();
        try {
            ls.getOutputStream().write(42);
            fail("expected exception");
        } catch (IOException ex) {
        }
        try {
            ls.getInputStream().read();
            fail("expected exception");
        } catch (IOException ex) {
        }
        try {
            ls1.getOutputStream().write(42);
            fail("expected exception");
        } catch (IOException ex) {
        }
        assertEquals(-1, ls1.getInputStream().read());
        ls1.close();
    }
}
