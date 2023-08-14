public class TransferToNonWritable  {
    public static void main(String args[]) throws Exception {
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        FileInputStream fis = new FileInputStream(blah);
        FileChannel channel = fis.getChannel();
        try {
            channel.transferTo((long)0, (long)2, channel);
            throw new RuntimeException("Test failed");
        } catch (NonWritableChannelException nwce) {
        } finally {
            channel.close();
            blah.delete();
        }
    }
}
