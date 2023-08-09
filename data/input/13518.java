public class NamedBuffer
{
    private final String    fName;
    private final byte[]    fBuffer;
    public
    NamedBuffer(    String  name,
                    byte[]  buffer)
        {
        fName =     name;
        fBuffer =   buffer;
        }
    public
    NamedBuffer(    String      name,
                    InputStream stream)
        throws IOException
        {
        this(   name,
                loadBufferFromStream(stream));
        }
    public String
    getName()
        {
        return fName;
        }
    public byte[]
    getBuffer()
        {
        return fBuffer;
        }
    public static byte[]
    loadBufferFromStream(InputStream stream)
        throws IOException
        {
        int bufferLimit = 200 * 1024;
        byte[]  readBuffer = new byte[bufferLimit];
        int actualSize = stream.read(readBuffer);
        if ( actualSize >= bufferLimit )
            {
            throw new IOException("too big for buffer");
            }
        byte[] resultBuffer = new byte[actualSize];
        System.arraycopy(   readBuffer,
                            0,
                            resultBuffer,
                            0,
                            actualSize);
        return resultBuffer;
        }
}
