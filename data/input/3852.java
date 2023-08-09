public class EncapsInputStream extends CDRInputStream
{
    private ORBUtilSystemException wrapper ;
    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] buf,
                             int size, boolean littleEndian,
                             GIOPVersion version) {
        super(orb, ByteBuffer.wrap(buf), size, littleEndian,
              version, Message.CDR_ENC_VERSION,
              BufferManagerFactory.newBufferManagerRead(
                                      BufferManagerFactory.GROW,
                                      Message.CDR_ENC_VERSION,
                                      (ORB)orb));
        wrapper = ORBUtilSystemException.get( (ORB)orb,
            CORBALogDomains.RPC_ENCODING ) ;
        performORBVersionSpecificInit();
    }
    public EncapsInputStream(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer,
                             int size, boolean littleEndian,
                             GIOPVersion version) {
        super(orb, byteBuffer, size, littleEndian,
              version, Message.CDR_ENC_VERSION,
              BufferManagerFactory.newBufferManagerRead(
                                      BufferManagerFactory.GROW,
                                      Message.CDR_ENC_VERSION,
                                      (com.sun.corba.se.spi.orb.ORB)orb));
        performORBVersionSpecificInit();
    }
    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] data, int size)
    {
        this(orb, data, size, GIOPVersion.V1_2);
    }
    public EncapsInputStream(EncapsInputStream eis)
    {
        super(eis);
        wrapper = ORBUtilSystemException.get( (ORB)(eis.orb()),
            CORBALogDomains.RPC_ENCODING ) ;
        performORBVersionSpecificInit();
    }
    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] data, int size, GIOPVersion version)
    {
        this(orb, data, size, false, version);
    }
    public EncapsInputStream(org.omg.CORBA.ORB orb,
                             byte[] data,
                             int size,
                             GIOPVersion version,
                             CodeBase codeBase) {
        super(orb,
              ByteBuffer.wrap(data),
              size,
              false,
              version, Message.CDR_ENC_VERSION,
              BufferManagerFactory.newBufferManagerRead(
                                      BufferManagerFactory.GROW,
                                      Message.CDR_ENC_VERSION,
                                      (ORB)orb));
        this.codeBase = codeBase;
        performORBVersionSpecificInit();
    }
    public CDRInputStream dup() {
        return new EncapsInputStream(this);
    }
    protected CodeSetConversion.BTCConverter createCharBTCConverter() {
        return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.ISO_8859_1);
    }
    protected CodeSetConversion.BTCConverter createWCharBTCConverter() {
        if (getGIOPVersion().equals(GIOPVersion.V1_0))
            throw wrapper.wcharDataInGiop10( CompletionStatus.COMPLETED_MAYBE);
        if (getGIOPVersion().equals(GIOPVersion.V1_1))
            return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.UTF_16,
                                                            isLittleEndian());
        return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.UTF_16,
                                                        false);
    }
    public CodeBase getCodeBase() {
        return codeBase;
    }
    private CodeBase codeBase;
}
