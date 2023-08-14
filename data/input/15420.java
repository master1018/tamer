public class ServiceContexts {
    private static boolean isDebugging( OutputStream os )
    {
        ORB orb = (ORB)(os.orb()) ;
        if (orb==null)
            return false ;
        return orb.serviceContextDebugFlag ;
    }
    private static boolean isDebugging( InputStream is )
    {
        ORB orb = (ORB)(is.orb()) ;
        if (orb==null)
            return false ;
        return orb.serviceContextDebugFlag ;
    }
    private void dprint( String msg )
    {
        ORBUtility.dprint( this, msg ) ;
    }
    public static void writeNullServiceContext( OutputStream os )
    {
        if (isDebugging(os))
            ORBUtility.dprint( "ServiceContexts", "Writing null service context" ) ;
        os.write_long( 0 ) ;
    }
    private void createMapFromInputStream(InputStream is)
    {
        orb = (ORB)(is.orb()) ;
        if (orb.serviceContextDebugFlag)
            dprint( "Constructing ServiceContexts from input stream" ) ;
        int numValid = is.read_long() ;
        if (orb.serviceContextDebugFlag)
            dprint("Number of service contexts = " + numValid);
        for (int ctr = 0; ctr < numValid; ctr++) {
            int scId = is.read_long();
            if (orb.serviceContextDebugFlag)
                dprint("Reading service context id " + scId);
            byte[] data = OctetSeqHelper.read(is);
            if (orb.serviceContextDebugFlag)
                dprint("Service context" + scId + " length: " + data.length);
            scMap.put(new Integer(scId), data);
        }
    }
    public ServiceContexts( ORB orb )
    {
        this.orb = orb ;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        addAlignmentOnWrite = false ;
        scMap = new HashMap();
        giopVersion = orb.getORBData().getGIOPVersion();
        codeBase = null ;
    }
    public ServiceContexts(InputStream s)
    {
        this( (ORB)(s.orb()) ) ;
        codeBase = ((CDRInputStream)s).getCodeBase();
        createMapFromInputStream(s);
        giopVersion = ((CDRInputStream)s).getGIOPVersion();
    }
    private ServiceContext unmarshal(Integer scId, byte[] data) {
        ServiceContextRegistry scr = orb.getServiceContextRegistry();
        ServiceContextData scd = scr.findServiceContextData(scId.intValue());
        ServiceContext sc = null;
        if (scd == null) {
            if (orb.serviceContextDebugFlag) {
                dprint("Could not find ServiceContextData for "
                       + scId
                       + " using UnknownServiceContext");
            }
            sc = new UnknownServiceContext(scId.intValue(), data);
        } else {
            if (orb.serviceContextDebugFlag) {
                dprint("Found " + scd);
            }
            EncapsInputStream eis
                = new EncapsInputStream(orb,
                                        data,
                                        data.length,
                                        giopVersion,
                                        codeBase);
            eis.consumeEndian();
            sc = scd.makeServiceContext(eis, giopVersion);
            if (sc == null)
                throw wrapper.svcctxUnmarshalError(
                    CompletionStatus.COMPLETED_MAYBE);
        }
        return sc;
    }
    public void addAlignmentPadding()
    {
        addAlignmentOnWrite = true ;
    }
    private static final int JAVAIDL_ALIGN_SERVICE_ID = 0xbe1345cd ;
    public void write(OutputStream os, GIOPVersion gv)
    {
        if (isDebugging(os)) {
            dprint( "Writing service contexts to output stream" ) ;
            Utility.printStackTrace() ;
        }
        int numsc = scMap.size();
        if (addAlignmentOnWrite) {
            if (isDebugging(os))
                dprint( "Adding alignment padding" ) ;
            numsc++ ;
        }
        if (isDebugging(os))
            dprint( "Service context has " + numsc + " components"  ) ;
        os.write_long( numsc ) ;
        writeServiceContextsInOrder(os, gv);
        if (addAlignmentOnWrite) {
            if (isDebugging(os))
                dprint( "Writing alignment padding" ) ;
            os.write_long( JAVAIDL_ALIGN_SERVICE_ID ) ;
            os.write_long( 4 ) ;
            os.write_octet( (byte)0 ) ;
            os.write_octet( (byte)0 ) ;
            os.write_octet( (byte)0 ) ;
            os.write_octet( (byte)0 ) ;
        }
        if (isDebugging(os))
            dprint( "Service context writing complete" ) ;
    }
    private void writeServiceContextsInOrder(OutputStream os, GIOPVersion gv) {
        Integer ueInfoId
            = new Integer(UEInfoServiceContext.SERVICE_CONTEXT_ID);
        Object unknownExceptionInfo = scMap.remove(ueInfoId);
        Iterator iter = scMap.keySet().iterator();
        while (iter.hasNext()) {
            Integer id = (Integer)iter.next();
            writeMapEntry(os, id, scMap.get(id), gv);
        }
        if (unknownExceptionInfo != null) {
            writeMapEntry(os, ueInfoId, unknownExceptionInfo, gv);
            scMap.put(ueInfoId, unknownExceptionInfo);
        }
    }
    private void writeMapEntry(OutputStream os, Integer id, Object scObj, GIOPVersion gv) {
        if (scObj instanceof byte[]) {
            if (isDebugging(os))
                dprint( "Writing service context bytes for id " + id);
            OctetSeqHelper.write(os, (byte[])scObj);
        } else {
            ServiceContext sc = (ServiceContext)scObj;
            if (isDebugging(os))
                dprint( "Writing service context " + sc ) ;
            sc.write(os, gv);
        }
    }
    public void put( ServiceContext sc )
    {
        Integer id = new Integer(sc.getId());
        scMap.put(id, sc);
    }
    public void delete( int scId ) {
        this.delete(new Integer(scId));
    }
    public void delete(Integer id)
    {
        scMap.remove(id)  ;
    }
    public ServiceContext get(int scId) {
        return this.get(new Integer(scId));
    }
    public ServiceContext get(Integer id)
    {
        Object result = scMap.get(id);
        if (result == null)
            return null ;
        if (result instanceof byte[]) {
            ServiceContext sc = unmarshal(id, (byte[])result);
            scMap.put(id, sc);
            return sc;
        } else {
            return (ServiceContext)result;
        }
    }
    private ORB orb ;
    private Map scMap;
    private boolean addAlignmentOnWrite ;
    private CodeBase codeBase;
    private GIOPVersion giopVersion;
    private ORBUtilSystemException wrapper ;
}
