public class PersistentBindingIterator extends BindingIteratorImpl
{
    private POA biPOA;
    public PersistentBindingIterator(org.omg.CORBA.ORB orb, Hashtable aTable,
        POA thePOA ) throws java.lang.Exception
    {
        super(orb);
        this.orb = orb;
        theHashtable = aTable;
        theEnumeration = this.theHashtable.keys();
        currentSize = this.theHashtable.size();
        biPOA = thePOA;
    }
    final public boolean NextOne(org.omg.CosNaming.BindingHolder b)
    {
        boolean hasMore = theEnumeration.hasMoreElements();
        if (hasMore) {
            InternalBindingKey theBindingKey =
                 ((InternalBindingKey)theEnumeration.nextElement());
            InternalBindingValue theElement =
                (InternalBindingValue)theHashtable.get( theBindingKey );
            NameComponent n = new NameComponent( theBindingKey.id, theBindingKey.kind );
            NameComponent[] nlist = new NameComponent[1];
            nlist[0] = n;
            BindingType theType = theElement.theBindingType;
            b.value =
                new Binding( nlist, theType );
        } else {
            b.value = new Binding(new NameComponent[0],BindingType.nobject);
        }
        return hasMore;
    }
    final public void Destroy()
    {
        try {
            byte[] objectId = biPOA.servant_to_id( this );
            if( objectId != null ) {
                biPOA.deactivate_object( objectId );
            }
        }
        catch( Exception e ) {
            throw new INTERNAL( "Exception in BindingIterator.Destroy " + e );
        }
    }
    public final int RemainingElements() {
        return currentSize;
    }
    private int currentSize;
    private Hashtable theHashtable;
    private Enumeration theEnumeration;
    private org.omg.CORBA.ORB orb;
}
