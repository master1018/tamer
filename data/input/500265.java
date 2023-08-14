public class DeferredFileOutputStream
    extends ThresholdingOutputStream
{
    private ByteArrayOutputStream memoryOutputStream;
    private OutputStream currentOutputStream;
    private File outputFile;
    private String prefix;
    private String suffix;
    private File directory;
    private boolean closed = false;
    public DeferredFileOutputStream(int threshold, File outputFile)
    {
        super(threshold);
        this.outputFile = outputFile;
        memoryOutputStream = new ByteArrayOutputStream();
        currentOutputStream = memoryOutputStream;
    }
    public DeferredFileOutputStream(int threshold, String prefix, String suffix, File directory)
    {
        this(threshold, (File)null);
        if (prefix == null) {
            throw new IllegalArgumentException("Temporary file prefix is missing");
        }
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = directory;
    }
    protected OutputStream getStream() throws IOException
    {
        return currentOutputStream;
    }
    protected void thresholdReached() throws IOException
    {
        if (prefix != null) {
            outputFile = File.createTempFile(prefix, suffix, directory);
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        memoryOutputStream.writeTo(fos);
        currentOutputStream = fos;
        memoryOutputStream = null;
    }
    public boolean isInMemory()
    {
        return (!isThresholdExceeded());
    }
    public byte[] getData()
    {
        if (memoryOutputStream != null)
        {
            return memoryOutputStream.toByteArray();
        }
        return null;
    }
    public File getFile()
    {
        return outputFile;
    }
    public void close() throws IOException
    {
        super.close();
        closed = true;
    }
    public void writeTo(OutputStream out) throws IOException 
    {
        if (!closed)
        {
            throw new IOException("Stream not closed");
        }
        if(isInMemory())
        {
            memoryOutputStream.writeTo(out);
        }
        else
        {
            FileInputStream fis = new FileInputStream(outputFile);
            try {
                IOUtils.copy(fis, out);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
    }
}
