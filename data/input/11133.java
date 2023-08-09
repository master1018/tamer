public class ClosedChannelTransfer {
    public static void main (String args []) throws Exception {
        File file = File.createTempFile("test1", null);
        file.deleteOnExit();
        FileChannel channel = (new RandomAccessFile("aaa","rw")).getChannel();
        test1(channel);
        test2(channel);
        channel.close();
        file.delete();
    }
    static void test1(FileChannel channel) throws Exception {
        ByteArrayInputStream istr = new ByteArrayInputStream(
            new byte [] {1, 2, 3, 4}
        );
        ReadableByteChannel rbc = Channels.newChannel(istr);
        rbc.close();
        try {
            channel.transferFrom(rbc, 0, 2);
            throw new Exception("Test1: No ClosedChannelException was thrown");
        } catch (ClosedChannelException cce) {
        }
    }
    static void test2(FileChannel channel) throws Exception {
        ByteArrayOutputStream istr = new ByteArrayOutputStream(4);
        WritableByteChannel wbc = Channels.newChannel(istr);
        wbc.close();
        try {
            channel.transferTo(0, 2, wbc);
            throw new Exception("Test2: No ClosedChannelException was thrown");
        } catch (ClosedChannelException cce) {
        }
    }
}
