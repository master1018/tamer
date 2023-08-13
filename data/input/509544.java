class TempFileTextBody extends AbstractBody implements TextBody {
    private static Log log = LogFactory.getLog(TempFileTextBody.class);
    private String mimeCharset = null;
    private TempFile tempFile = null;
    public TempFileTextBody(InputStream is) throws IOException {
        this(is, null);
    }
    public TempFileTextBody(InputStream is, String mimeCharset) 
            throws IOException {
        this.mimeCharset = mimeCharset;
        TempPath tempPath = TempStorage.getInstance().getRootTempPath();
        tempFile = tempPath.createTempFile("attachment", ".txt");
        OutputStream out = tempFile.getOutputStream();
        IOUtils.copy(is, out);
        out.close();
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
        return new InputStreamReader(tempFile.getInputStream(), javaCharset);
    }
    public void writeTo(OutputStream out) throws IOException {
	IOUtils.copy(tempFile.getInputStream(), out);	
    }
}
