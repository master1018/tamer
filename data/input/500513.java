public class SwappedDataInputStream extends ProxyInputStream
    implements DataInput
{
    public SwappedDataInputStream( InputStream input )
    {
        super( input );
    }
    public boolean readBoolean()
        throws IOException, EOFException
    {
        return ( 0 == readByte() );
    }
    public byte readByte()
        throws IOException, EOFException
    {
        return (byte)in.read();
    }
    public char readChar()
        throws IOException, EOFException
    {
        return (char)readShort();
    }
    public double readDouble()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedDouble( in );
    }
    public float readFloat()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedFloat( in );
    }
    public void readFully( byte[] data )
        throws IOException, EOFException
    {
        readFully( data, 0, data.length );
    }
    public void readFully( byte[] data, int offset, int length )
        throws IOException, EOFException
    {
        int remaining = length;
        while( remaining > 0 )
        {
            int location = offset + ( length - remaining );
            int count = read( data, location, remaining );
            if( -1 == count )
            {
                throw new EOFException();
            }
            remaining -= count;
        }
    }
    public int readInt()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedInteger( in );
    }
    public String readLine()
        throws IOException, EOFException
    {
        throw new UnsupportedOperationException( 
                "Operation not supported: readLine()" );
    }
    public long readLong()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedLong( in );
    }
    public short readShort()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedShort( in );
    }
    public int readUnsignedByte()
        throws IOException, EOFException
    {
        return in.read();
    }
    public int readUnsignedShort()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedUnsignedShort( in );
    }
    public String readUTF()
        throws IOException, EOFException
    {
        throw new UnsupportedOperationException( 
                "Operation not supported: readUTF()" );
    }
    public int skipBytes( int count )
        throws IOException, EOFException
    {
        return (int)in.skip( count );
    }
}
