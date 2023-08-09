public class EncapsOutputStream extends CDROutputStream
{
    final static boolean usePooledByteBuffers = false;
    public EncapsOutputStream(ORB orb) {
        this(orb, GIOPVersion.V1_2);
    }
    public EncapsOutputStream(ORB orb, GIOPVersion version) {
        this(orb, version, false);
    }
    public EncapsOutputStream(ORB orb, boolean isLittleEndian) {
        this(orb, GIOPVersion.V1_2, isLittleEndian);
    }
    public EncapsOutputStream(ORB orb,
                              GIOPVersion version,
                              boolean isLittleEndian)
    {
        super(orb, version, Message.CDR_ENC_VERSION, isLittleEndian,
              BufferManagerFactory.newBufferManagerWrite(
                                        BufferManagerFactory.GROW,
                                        Message.CDR_ENC_VERSION,
                                        orb),
              ORBConstants.STREAM_FORMAT_VERSION_1,
              usePooledByteBuffers);
    }
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        freeInternalCaches();
        return new EncapsInputStream(orb(),
                                     getByteBuffer(),
                                     getSize(),
                                     isLittleEndian(),
                                     getGIOPVersion());
    }
    protected CodeSetConversion.CTBConverter createCharCTBConverter() {
        return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.ISO_8859_1);
    }
    protected CodeSetConversion.CTBConverter createWCharCTBConverter() {
        if (getGIOPVersion().equals(GIOPVersion.V1_0))
            throw wrapper.wcharDataInGiop10(CompletionStatus.COMPLETED_MAYBE);
        if (getGIOPVersion().equals(GIOPVersion.V1_1))
            return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.UTF_16,
                                                            isLittleEndian(),
                                                            false);
        boolean useBOM = ((ORB)orb()).getORBData().useByteOrderMarkersInEncapsulations();
        return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.UTF_16,
                                                        false,
                                                        useBOM);
    }
}
