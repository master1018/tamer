public abstract class EncapsulationFactoryBase implements IdentifiableFactory {
    private int id ;
    public int getId()
    {
        return id ;
    }
    public EncapsulationFactoryBase( int id )
    {
        this.id = id ;
    }
    public final Identifiable create( InputStream in )
    {
        InputStream is = EncapsulationUtility.getEncapsulationStream( in ) ;
        return readContents( is ) ;
    }
    protected abstract Identifiable readContents( InputStream is ) ;
}
