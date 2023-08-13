class TempFileBinaryBody extends AbstractBody implements BinaryBody {
    private static Log log = LogFactory.getLog(TempFileBinaryBody.class);
    private Entity parent = null;
    private TempFile tempFile = null;
    public TempFileBinaryBody(InputStream is) throws IOException {
        TempPath tempPath = TempStorage.getInstance().getRootTempPath();
        tempFile = tempPath.createTempFile("attachment", ".bin");
        OutputStream out = tempFile.getOutputStream();
        IOUtils.copy(is, out);
        out.close();
    }
    public Entity getParent() {
        return parent;
    }
    public void setParent(Entity parent) {
        this.parent = parent;
    }
    public InputStream getInputStream() throws IOException {
        return tempFile.getInputStream();
    }
    public void writeTo(OutputStream out) throws IOException {
	IOUtils.copy(getInputStream(),out);
    }
}
