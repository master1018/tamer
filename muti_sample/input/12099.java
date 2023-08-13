public abstract class BindingIteratorImpl extends BindingIteratorPOA
{
    protected ORB orb ;
    public BindingIteratorImpl(ORB orb)
        throws java.lang.Exception
    {
        super();
        this.orb = orb ;
    }
    public synchronized boolean next_one(org.omg.CosNaming.BindingHolder b)
    {
        return NextOne(b);
    }
    public synchronized boolean next_n(int how_many,
        org.omg.CosNaming.BindingListHolder blh)
    {
        if( how_many == 0 ) {
            throw new BAD_PARAM( " 'how_many' parameter is set to 0 which is" +
            " invalid" );
        }
        return list( how_many, blh );
    }
    public boolean list( int how_many, org.omg.CosNaming.BindingListHolder blh)
    {
        int numberToGet = Math.min(RemainingElements(),how_many);
        Binding[] bl = new Binding[numberToGet];
        BindingHolder bh = new BindingHolder();
        int i = 0;
        while (i < numberToGet && this.NextOne(bh) == true) {
            bl[i] = bh.value;
            i++;
        }
        if (i == 0) {
            blh.value = new Binding[0];
            return false;
        }
        blh.value = bl;
        return true;
    }
    public synchronized void destroy()
    {
        this.Destroy();
    }
    protected abstract boolean NextOne(org.omg.CosNaming.BindingHolder b);
    protected abstract void Destroy();
    protected abstract int RemainingElements();
}
