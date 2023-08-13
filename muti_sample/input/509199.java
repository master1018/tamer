class MemoryBinaryBody extends AbstractBody implements BinaryBody {
    private static Log log = LogFactory.getLog(MemoryBinaryBody.class);
    private Entity parent = null;
    private byte[] tempFile = null;
    public MemoryBinaryBody(InputStream is) throws IOException {
        TempPath tempPath = TempStorage.getInstance().getRootTempPath();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(is, out);
        out.close();
        tempFile = out.toByteArray();
    }
    public Entity getParent() {
        return parent;
    }
    public void setParent(Entity parent) {
        this.parent = parent;
    }
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(tempFile);
    }
    public void writeTo(OutputStream out) throws IOException {
	IOUtils.copy(getInputStream(),out);
    }
}
