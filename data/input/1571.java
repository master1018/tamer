public class StreamEncoderClose {
    public static void main( String arg[] ) throws Exception {
        byte[] expected = {(byte)0x1b,(byte)0x24,(byte)0x42,
                           (byte)0x30,(byte)0x6c,
                           (byte)0x1b,(byte)0x28,(byte)0x42};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MyBufferedOutputStream mbos = new MyBufferedOutputStream(baos);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(mbos, "ISO-2022-JP"));
        mbos.dontClose();
        pw.write("\u4e00");
        pw.close();             
        mbos.canClose();
        pw.close();             
        byte[] out = baos.toByteArray();
        if (out.length != expected.length) {
            throw new IOException("Failed");
        }
        for (int i = 0; i < out.length; i++) {
            if (out[i] != expected[i])
                throw new IOException("Failed");
        }
    }
    static class MyBufferedOutputStream extends BufferedOutputStream {
        MyBufferedOutputStream(OutputStream os) {
            super(os);
        }
        private boolean status;
        public void dontClose() {
            status = false;
        }
        public void canClose() {
            status = true;
        }
        public void close() throws IOException {
            if ( status == false ) {
                throw new IOException("Can't close ");
            }
            super.close();
        }
    }
}
