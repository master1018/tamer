abstract public class AbstractObjectInputStream extends ObjectInputStream
{
    protected InputStream in;
    public AbstractObjectInputStream(InputStream in)
        throws IOException, StreamCorruptedException
        {
            this.in = in;
        }
    abstract public void close() throws IOException;
    protected Object readObjectOverride()
        throws OptionalDataException, ClassNotFoundException, IOException {
            return null;
    }
    abstract public void defaultReadObject()
        throws IOException, ClassNotFoundException, NotActiveException;
    final protected native Object
        allocateNewObject(Class ofClass, Class ctorClass)
        throws InstantiationException, IllegalAccessException;
    final protected native Object
        allocateNewArray(Class componentClass, int length)
        throws InstantiationException, IllegalAccessException;
    abstract public ObjectInputStream.GetField readFields()
        throws IOException, ClassNotFoundException, NotActiveException;
    abstract protected boolean enableResolveObject(boolean enable) throws SecurityException;
    abstract public void registerValidation(ObjectInputValidation obj,
                                            int prio)
        throws NotActiveException, InvalidObjectException;
    abstract public int read() throws IOException;
    abstract public int read(byte[] data, int offset, int length)
        throws IOException;
    abstract public boolean readBoolean() throws IOException;
    abstract public byte readByte() throws IOException;
    abstract public int readUnsignedByte()  throws IOException;
    abstract public short readShort()  throws IOException;
    abstract public int readUnsignedShort() throws IOException;
    abstract public char readChar()  throws IOException;
    abstract public int readInt()  throws IOException;
    abstract public long readLong()  throws IOException;
    abstract public float readFloat() throws IOException;
    abstract public double readDouble() throws IOException;
    abstract public void readFully(byte[] data) throws IOException;
    abstract public void readFully(byte[] data, int offset, int size) throws IOException;
    abstract public String readUTF() throws IOException;
    abstract public int available() throws IOException;
    abstract public int skipBytes(int len) throws IOException;
    abstract public String readLine() throws IOException;
};
