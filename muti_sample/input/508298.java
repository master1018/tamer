public abstract class ThresholdingOutputStream
    extends OutputStream
{
    private int threshold;
    private long written;
    private boolean thresholdExceeded;
    public ThresholdingOutputStream(int threshold)
    {
        this.threshold = threshold;
    }
    public void write(int b) throws IOException
    {
        checkThreshold(1);
        getStream().write(b);
        written++;
    }
    public void write(byte b[]) throws IOException
    {
        checkThreshold(b.length);
        getStream().write(b);
        written += b.length;
    }
    public void write(byte b[], int off, int len) throws IOException
    {
        checkThreshold(len);
        getStream().write(b, off, len);
        written += len;
    }
    public void flush() throws IOException
    {
        getStream().flush();
    }
    public void close() throws IOException
    {
        try
        {
            flush();
        }
        catch (IOException ignored)
        {
        }
        getStream().close();
    }
    public int getThreshold()
    {
        return threshold;
    }
    public long getByteCount()
    {
        return written;
    }
    public boolean isThresholdExceeded()
    {
        return (written > threshold);
    }
    protected void checkThreshold(int count) throws IOException
    {
        if (!thresholdExceeded && (written + count > threshold))
        {
            thresholdExceeded = true;
            thresholdReached();
        }
    }
    protected void resetByteCount() 
    {
        this.thresholdExceeded = false;
        this.written = 0;
    }
    protected abstract OutputStream getStream() throws IOException;
    protected abstract void thresholdReached() throws IOException;
}
