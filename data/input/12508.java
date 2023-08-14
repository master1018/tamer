public class IIOPProfileImpl extends IdentifiableBase implements IIOPProfile
{
    private ORB orb ;
    private IORSystemException wrapper ;
    private ObjectId oid;
    private IIOPProfileTemplate proftemp;
    private ObjectKeyTemplate oktemp ;
    protected String codebase = null ;
    protected boolean cachedCodebase = false;
    private boolean checkedIsLocal = false ;
    private boolean cachedIsLocal = false ;
    private static class LocalCodeBaseSingletonHolder {
        public static JavaCodebaseComponent comp ;
        static {
            String localCodebase = JDKBridge.getLocalCodebase() ;
            if (localCodebase == null)
                comp = null ;
            else
                comp = IIOPFactories.makeJavaCodebaseComponent(
                    localCodebase ) ;
        }
    }
    private GIOPVersion giopVersion = null;
    public boolean equals( Object obj )
    {
        if (!(obj instanceof IIOPProfileImpl))
            return false ;
        IIOPProfileImpl other = (IIOPProfileImpl)obj ;
        return oid.equals( other.oid ) && proftemp.equals( other.proftemp ) &&
            oktemp.equals( other.oktemp ) ;
    }
    public int hashCode()
    {
        return oid.hashCode() ^ proftemp.hashCode() ^ oktemp.hashCode() ;
    }
    public ObjectId getObjectId()
    {
        return oid ;
    }
    public TaggedProfileTemplate getTaggedProfileTemplate()
    {
        return proftemp ;
    }
    public ObjectKeyTemplate getObjectKeyTemplate()
    {
        return oktemp ;
    }
    private IIOPProfileImpl( ORB orb )
    {
        this.orb = orb ;
        wrapper = IORSystemException.get( orb,
            CORBALogDomains.OA_IOR ) ;
    }
    public IIOPProfileImpl( ORB orb, ObjectKeyTemplate oktemp, ObjectId oid,
        IIOPProfileTemplate proftemp )
    {
        this( orb ) ;
        this.oktemp = oktemp ;
        this.oid = oid ;
        this.proftemp = proftemp ;
    }
    public IIOPProfileImpl( InputStream is )
    {
        this( (ORB)(is.orb()) ) ;
        init( is ) ;
    }
    public IIOPProfileImpl( ORB orb, org.omg.IOP.TaggedProfile profile)
    {
        this( orb ) ;
        if (profile == null || profile.tag != TAG_INTERNET_IOP.value ||
            profile.profile_data == null) {
            throw wrapper.invalidTaggedProfile() ;
        }
        EncapsInputStream istr = new EncapsInputStream((ORB)orb, profile.profile_data,
            profile.profile_data.length);
        istr.consumeEndian();
        init( istr ) ;
    }
    private void init( InputStream istr )
    {
        GIOPVersion version = new GIOPVersion() ;
        version.read( istr ) ;
        IIOPAddress primary = new IIOPAddressImpl( istr ) ;
        byte[] key = EncapsulationUtility.readOctets( istr ) ;
        ObjectKey okey = orb.getObjectKeyFactory().create( key ) ;
        oktemp = okey.getTemplate() ;
        oid = okey.getId() ;
        proftemp = IIOPFactories.makeIIOPProfileTemplate( orb,
            version, primary ) ;
        if (version.getMinor() > 0)
            EncapsulationUtility.readIdentifiableSequence( proftemp,
                orb.getTaggedComponentFactoryFinder(), istr ) ;
        if (uncachedGetCodeBase() == null) {
            JavaCodebaseComponent jcc = LocalCodeBaseSingletonHolder.comp ;
            if (jcc != null) {
                if (version.getMinor() > 0)
                    proftemp.add( jcc ) ;
                codebase = jcc.getURLs() ;
            }
            cachedCodebase = true;
        }
    }
    public void writeContents(OutputStream os)
    {
        proftemp.write( oktemp, oid, os ) ;
    }
    public int getId()
    {
        return proftemp.getId() ;
    }
    public boolean isEquivalent( TaggedProfile prof )
    {
        if (!(prof instanceof IIOPProfile))
            return false ;
        IIOPProfile other = (IIOPProfile)prof ;
        return oid.equals( other.getObjectId() ) &&
               proftemp.isEquivalent( other.getTaggedProfileTemplate() ) &&
               oktemp.equals( other.getObjectKeyTemplate() ) ;
    }
    public ObjectKey getObjectKey()
    {
        ObjectKey result = IORFactories.makeObjectKey( oktemp, oid ) ;
        return result ;
    }
    public org.omg.IOP.TaggedProfile getIOPProfile()
    {
        EncapsOutputStream os = new EncapsOutputStream( orb ) ;
        os.write_long( getId() ) ;
        write( os ) ;
        InputStream is = (InputStream)(os.create_input_stream()) ;
        return org.omg.IOP.TaggedProfileHelper.read( is ) ;
    }
    private String uncachedGetCodeBase() {
        Iterator iter = proftemp.iteratorById( TAG_JAVA_CODEBASE.value ) ;
        if (iter.hasNext()) {
            JavaCodebaseComponent jcbc = (JavaCodebaseComponent)(iter.next()) ;
            return jcbc.getURLs() ;
        }
        return null ;
    }
    public synchronized String getCodebase() {
        if (!cachedCodebase) {
            cachedCodebase = true ;
            codebase = uncachedGetCodeBase() ;
        }
        return codebase ;
    }
    public ORBVersion getORBVersion() {
        return oktemp.getORBVersion();
    }
    public synchronized boolean isLocal()
    {
        if (!checkedIsLocal) {
            checkedIsLocal = true ;
            String host = proftemp.getPrimaryAddress().getHost() ;
            cachedIsLocal = orb.isLocalHost(host) &&
                orb.isLocalServerId(oktemp.getSubcontractId(),
                                           oktemp.getServerId()) &&
                orb.getLegacyServerSocketManager()
                    .legacyIsLocalServerPort(
                        proftemp.getPrimaryAddress().getPort());
        }
        return cachedIsLocal ;
    }
    public java.lang.Object getServant()
    {
        if (!isLocal())
            return null ;
        RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;
        ObjectAdapterFactory oaf = scr.getObjectAdapterFactory(
            oktemp.getSubcontractId() ) ;
        ObjectAdapterId oaid = oktemp.getObjectAdapterId() ;
        ObjectAdapter oa = null ;
        try {
            oa = oaf.find( oaid ) ;
        } catch (SystemException exc) {
            wrapper.getLocalServantFailure( exc, oaid.toString() ) ;
            return null ;
        }
        byte[] boid = oid.getId() ;
        java.lang.Object servant = oa.getLocalServant( boid ) ;
        return servant ;
    }
    public synchronized GIOPVersion getGIOPVersion()
    {
        return proftemp.getGIOPVersion() ;
    }
    public void makeImmutable()
    {
        proftemp.makeImmutable() ;
    }
}
