public abstract class CorbaOutputObject
    extends CDROutputStream
    implements OutputObject
{
    public CorbaOutputObject(
        ORB orb, GIOPVersion version, byte encodingVersion,
        boolean littleEndian, BufferManagerWrite bufferManager,
        byte streamFormatVersion, boolean usePooledByteBuffers)
    {
        super(orb, version, encodingVersion, littleEndian, bufferManager,
              streamFormatVersion, usePooledByteBuffers);
    }
    public abstract void writeTo(CorbaConnection connection)
        throws java.io.IOException;
}
