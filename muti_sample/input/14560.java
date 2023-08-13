public class TransientBindingIterator extends BindingIteratorImpl
{
    private POA nsPOA;
    public TransientBindingIterator(ORB orb, Hashtable aTable,
        POA thePOA )
        throws java.lang.Exception
    {
        super(orb);
        theHashtable = aTable;
        theEnumeration = this.theHashtable.elements();
        currentSize = this.theHashtable.size();
        this.nsPOA = thePOA;
    }
    final public boolean NextOne(org.omg.CosNaming.BindingHolder b)
    {
        boolean hasMore = theEnumeration.hasMoreElements();
        if (hasMore) {
            b.value =
                ((InternalBindingValue)theEnumeration.nextElement()).theBinding;
            currentSize--;
        } else {
            b.value = new Binding(new NameComponent[0],BindingType.nobject);
        }
        return hasMore;
    }
    final public void Destroy()
    {
        try {
            byte[] objectId = nsPOA.servant_to_id( this );
            if( objectId != null ) {
                nsPOA.deactivate_object( objectId );
            }
        }
        catch( Exception e ) {
            NamingUtils.errprint("BindingIterator.Destroy():caught exception:");
            NamingUtils.printException(e);
        }
    }
    public final int RemainingElements() {
        return currentSize;
    }
    private int currentSize;
    private Hashtable theHashtable;
    private Enumeration theEnumeration;
}
