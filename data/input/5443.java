abstract public class AbstractObjectOutputStream extends ObjectOutputStream
{
    protected OutputStream out;
    public AbstractObjectOutputStream(OutputStream out) throws IOException {
        this.out = out;
    }
    abstract public void reset() throws IOException;
    abstract protected void drain() throws IOException;
    abstract public void close() throws IOException;
    protected void writeObjectOverride(Object obj)
        throws IOException
    {
    }
    abstract public void defaultWriteObject() throws IOException;
    abstract public ObjectOutputStream.PutField putFields() throws IOException;
    abstract public void writeFields() throws IOException;
    abstract protected boolean enableReplaceObject(boolean enable) throws SecurityException;
    abstract public void write(int data) throws IOException;
    abstract public void write(byte b[]) throws IOException;
    abstract public void write(byte b[], int off, int len) throws IOException;
    abstract public void writeBoolean(boolean data) throws IOException;
    abstract public void writeByte(int data) throws IOException;
    abstract public void writeShort(int data)  throws IOException;
    abstract public void writeChar(int data)  throws IOException;
    abstract public void writeInt(int data)  throws IOException;
    abstract public void writeLong(long data)  throws IOException;
    abstract public void writeFloat(float data) throws IOException;
    abstract public void writeDouble(double data) throws IOException;
    abstract public void writeBytes(String data) throws IOException;
    abstract public void writeChars(String data) throws IOException;
    abstract public void writeUTF(String data) throws IOException;
};
