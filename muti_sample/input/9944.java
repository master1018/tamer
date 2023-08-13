public class IIOPProfileTemplateImpl extends TaggedProfileTemplateBase
    implements IIOPProfileTemplate
{
    private ORB orb ;
    private GIOPVersion giopVersion ;
    private IIOPAddress primary ;
    public boolean equals( Object obj )
    {
        if (!(obj instanceof IIOPProfileTemplateImpl))
            return false ;
        IIOPProfileTemplateImpl other = (IIOPProfileTemplateImpl)obj ;
        return super.equals( obj ) && giopVersion.equals( other.giopVersion ) &&
            primary.equals( other.primary ) ;
    }
    public int hashCode()
    {
        return super.hashCode() ^ giopVersion.hashCode() ^ primary.hashCode() ;
    }
    public TaggedProfile create( ObjectKeyTemplate oktemp, ObjectId id )
    {
        return IIOPFactories.makeIIOPProfile( orb, oktemp, id, this ) ;
    }
    public GIOPVersion getGIOPVersion()
    {
        return giopVersion ;
    }
    public IIOPAddress getPrimaryAddress()
    {
        return primary ;
    }
    public IIOPProfileTemplateImpl( ORB orb, GIOPVersion version, IIOPAddress primary )
    {
        this.orb = orb ;
        this.giopVersion = version ;
        this.primary = primary ;
        if (giopVersion.getMinor() == 0)
            makeImmutable() ;
    }
    public IIOPProfileTemplateImpl( InputStream istr )
    {
        byte major = istr.read_octet() ;
        byte minor = istr.read_octet() ;
        giopVersion = GIOPVersion.getInstance( major, minor ) ;
        primary = new IIOPAddressImpl( istr ) ;
        orb = (ORB)(istr.orb()) ;
        if (minor > 0)
            EncapsulationUtility.readIdentifiableSequence(
                this, orb.getTaggedComponentFactoryFinder(), istr ) ;
        makeImmutable() ;
    }
    public void write( ObjectKeyTemplate okeyTemplate, ObjectId id, OutputStream os)
    {
        giopVersion.write( os ) ;
        primary.write( os ) ;
        OutputStream encapsulatedOS = new EncapsOutputStream( (ORB)os.orb(),
            ((CDROutputStream)os).isLittleEndian() ) ;
        okeyTemplate.write( id, encapsulatedOS ) ;
        EncapsulationUtility.writeOutputStream( encapsulatedOS, os ) ;
        if (giopVersion.getMinor() > 0)
            EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }
    public void writeContents( OutputStream os)
    {
        giopVersion.write( os ) ;
        primary.write( os ) ;
        if (giopVersion.getMinor() > 0)
            EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }
    public int getId()
    {
        return TAG_INTERNET_IOP.value ;
    }
    public boolean isEquivalent( TaggedProfileTemplate temp )
    {
        if (!(temp instanceof IIOPProfileTemplateImpl))
            return false ;
        IIOPProfileTemplateImpl tempimp = (IIOPProfileTemplateImpl)temp ;
        return primary.equals( tempimp.primary )  ;
    }
}
