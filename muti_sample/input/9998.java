public class ObjectAdapterIdNumber extends ObjectAdapterIdArray {
    private int poaid ;
    public ObjectAdapterIdNumber( int poaid )
    {
        super( "OldRootPOA", Integer.toString( poaid ) ) ;
        this.poaid = poaid ;
    }
    public int getOldPOAId()
    {
        return poaid ;
    }
}
