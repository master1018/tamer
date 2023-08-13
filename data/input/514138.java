class MemoryTextBody extends AbstractBody implements TextBody {
    private static Log log = LogFactory.getLog(MemoryTextBody.class);
    private String mimeCharset = null;
    private byte[] tempFile = null;
    public MemoryTextBody(InputStream is) throws IOException {
        this(is, null);
    }
    public MemoryTextBody(InputStream is, String mimeCharset) 
            throws IOException {
        this.mimeCharset = mimeCharset;
        TempPath tempPath = TempStorage.getInstance().getRootTempPath();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(is, out);
        out.close();
        tempFile = out.toByteArray();
    }
    public Reader getReader() throws UnsupportedEncodingException, IOException {
        String javaCharset = null;
        if (mimeCharset != null) {
            javaCharset = CharsetUtil.toJavaCharset(mimeCharset);
        }
        if (javaCharset == null) {
            javaCharset = "ISO-8859-1";
            if (log.isWarnEnabled()) {
                if (mimeCharset == null) {
                    log.warn("No MIME charset specified. Using " + javaCharset
                            + " instead.");
                } else {
                    log.warn("MIME charset '" + mimeCharset + "' has no "
                            + "corresponding Java charset. Using " + javaCharset
                            + " instead.");
                }
            }
        }
        return new InputStreamReader(new ByteArrayInputStream(tempFile), javaCharset);
    }
    public void writeTo(OutputStream out) throws IOException {
	IOUtils.copy(new ByteArrayInputStream(tempFile), out);	
    }
}
