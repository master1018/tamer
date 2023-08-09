public class BinaryTempFileBody implements Body {
    private static File mTempDirectory;
    private File mFile;
    public static void setTempDirectory(File tempDirectory) {
        mTempDirectory = tempDirectory;
    }
    public BinaryTempFileBody() throws IOException {
        if (mTempDirectory == null) {
            throw new
                RuntimeException("setTempDirectory has not been called on BinaryTempFileBody!");
        }
    }
    public void setFile(String filePath) {
        mFile = new File(filePath);
    }
    public OutputStream getOutputStream() throws IOException {
        mFile = File.createTempFile("body", null, mTempDirectory);
        mFile.deleteOnExit();
        return new FileOutputStream(mFile);
    }
    public InputStream getInputStream() throws MessagingException {
        try {
            return new BinaryTempFileBodyInputStream(new FileInputStream(mFile));
        }
        catch (IOException ioe) {
            throw new MessagingException("Unable to open body", ioe);
        }
    }
    public void writeTo(OutputStream out) throws IOException, MessagingException {
        InputStream in = getInputStream();
        Base64OutputStream base64Out = new Base64OutputStream(
            out, Base64.CRLF | Base64.NO_CLOSE);
        IOUtils.copy(in, base64Out);
        base64Out.close();
        mFile.delete();
    }
    class BinaryTempFileBodyInputStream extends FilterInputStream {
        public BinaryTempFileBodyInputStream(InputStream in) {
            super(in);
        }
        @Override
        public void close() throws IOException {
            super.close();
            mFile.delete();
        }
    }
}
