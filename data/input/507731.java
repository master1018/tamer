final class UDataOutputStream extends DataOutputStream
{
    public UDataOutputStream (final OutputStream _out)
    {
        super (_out);
    }
    public final void writeU2 (final int uint) throws IOException
    {
        writeShort ((short) uint); 
    }
    public final void writeU4 (final long ulong) throws IOException
    {
        writeInt ((int) ulong); 
    }
} 
