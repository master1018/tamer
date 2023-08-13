public class CDRInputObject extends CDRInputStream
    implements
        InputObject
{
    private CorbaConnection corbaConnection;
    private Message header;
    private boolean unmarshaledHeader;
    private ORB orb ;
    private ORBUtilSystemException wrapper ;
    private OMGSystemException omgWrapper ;
    public CDRInputObject(ORB orb,
                          CorbaConnection corbaConnection,
                          ByteBuffer byteBuffer,
                          Message header)
    {
        super(orb, byteBuffer, header.getSize(), header.isLittleEndian(),
              header.getGIOPVersion(), header.getEncodingVersion(),
              BufferManagerFactory.newBufferManagerRead(
                                          header.getGIOPVersion(),
                                          header.getEncodingVersion(),
                                          orb));
        this.corbaConnection = corbaConnection;
        this.orb = orb ;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_ENCODING ) ;
        this.omgWrapper = OMGSystemException.get( orb,
            CORBALogDomains.RPC_ENCODING ) ;
        if (orb.transportDebugFlag) {
            dprint(".CDRInputObject constructor:");
        }
        getBufferManager().init(header);
        this.header = header;
        unmarshaledHeader = false;
        setIndex(Message.GIOPMessageHeaderLength);
        setBufferLength(header.getSize());
    }
    public final CorbaConnection getConnection()
    {
        return corbaConnection;
    }
    public Message getMessageHeader()
    {
        return header;
    }
    public void unmarshalHeader()
    {
        if (!unmarshaledHeader) {
            try {
                if (((ORB)orb()).transportDebugFlag) {
                    dprint(".unmarshalHeader->: " + getMessageHeader());
                }
                getMessageHeader().read(this);
                unmarshaledHeader= true;
            } catch (RuntimeException e) {
                if (((ORB)orb()).transportDebugFlag) {
                    dprint(".unmarshalHeader: !!ERROR!!: "
                           + getMessageHeader()
                           + ": " + e);
                }
                throw e;
            } finally {
                if (((ORB)orb()).transportDebugFlag) {
                    dprint(".unmarshalHeader<-: " + getMessageHeader());
                }
            }
        }
    }
    public final boolean unmarshaledHeader()
    {
        return unmarshaledHeader;
    }
    protected CodeSetConversion.BTCConverter createCharBTCConverter() {
        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();
        if (codesets == null)
            return super.createCharBTCConverter();
        OSFCodeSetRegistry.Entry charSet
            = OSFCodeSetRegistry.lookupEntry(codesets.getCharCodeSet());
        if (charSet == null)
            throw wrapper.unknownCodeset( charSet ) ;
        return CodeSetConversion.impl().getBTCConverter(charSet, isLittleEndian());
    }
    protected CodeSetConversion.BTCConverter createWCharBTCConverter() {
        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();
        if (codesets == null) {
            if (getConnection().isServer())
                throw omgWrapper.noClientWcharCodesetCtx() ;
            else
                throw omgWrapper.noServerWcharCodesetCmp() ;
        }
        OSFCodeSetRegistry.Entry wcharSet
            = OSFCodeSetRegistry.lookupEntry(codesets.getWCharCodeSet());
        if (wcharSet == null)
            throw wrapper.unknownCodeset( wcharSet ) ;
        if (wcharSet == OSFCodeSetRegistry.UTF_16) {
            if (getGIOPVersion().equals(GIOPVersion.V1_2))
                return CodeSetConversion.impl().getBTCConverter(wcharSet, false);
        }
        return CodeSetConversion.impl().getBTCConverter(wcharSet, isLittleEndian());
    }
    private CodeSetComponentInfo.CodeSetContext getCodeSets() {
        if (getConnection() == null)
            return CodeSetComponentInfo.LOCAL_CODE_SETS;
        else
            return getConnection().getCodeSetContext();
    }
    public final CodeBase getCodeBase() {
        if (getConnection() == null)
            return null;
        else
            return getConnection().getCodeBase();
    }
    public CDRInputStream dup() {
        return null;
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("CDRInputObject", msg);
    }
}
