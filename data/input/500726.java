final class UDataInputStream extends DataInputStream
{
    public UDataInputStream (final InputStream _in)
    {
        super (_in);
    }
    public final int readU2 () throws IOException
    {
        final short value = readShort ();
        return ((int) value) & 0xFFFF; 
    }
    public final long readU4 () throws IOException
    {
        final int value = readInt ();
        return ((long) value) & 0xFFFFFFFFL; 
    }
} 
