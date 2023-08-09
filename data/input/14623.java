public class TransientNamingContext extends NamingContextImpl implements NamingContextDataStore
{
    private Logger readLogger, updateLogger, lifecycleLogger;
    private NamingSystemException wrapper ;
    public TransientNamingContext(com.sun.corba.se.spi.orb.ORB orb,
        org.omg.CORBA.Object initial,
        POA nsPOA )
        throws java.lang.Exception
    {
        super(orb, nsPOA );
        wrapper = NamingSystemException.get( orb, CORBALogDomains.NAMING ) ;
        this.localRoot = initial;
        readLogger = orb.getLogger( CORBALogDomains.NAMING_READ);
        updateLogger = orb.getLogger( CORBALogDomains.NAMING_UPDATE);
        lifecycleLogger = orb.getLogger(
            CORBALogDomains.NAMING_LIFECYCLE);
        lifecycleLogger.fine( "Root TransientNamingContext LIFECYCLE.CREATED" );
    }
    public final void Bind(NameComponent n, org.omg.CORBA.Object obj,
                           BindingType bt)
        throws org.omg.CORBA.SystemException
    {
        InternalBindingKey key = new InternalBindingKey(n);
        NameComponent[] name = new NameComponent[1];
        name[0] = n;
        Binding b = new Binding(name,bt);
        InternalBindingValue value = new InternalBindingValue(b,null);
        value.theObjectRef = obj;
        InternalBindingValue oldValue =
            (InternalBindingValue)this.theHashtable.put(key,value);
        if (oldValue != null) {
            updateLogger.warning( LogKeywords.NAMING_BIND + "Name " +
                getName( n ) + " Was Already Bound" );
            throw wrapper.transNcBindAlreadyBound() ;
        }
        if( updateLogger.isLoggable( Level.FINE ) ) {
            updateLogger.fine( LogKeywords.NAMING_BIND_SUCCESS +
                "Name Component: " + n.id + "." + n.kind );
        }
    }
    public final org.omg.CORBA.Object Resolve(NameComponent n,
                                              BindingTypeHolder bth)
        throws org.omg.CORBA.SystemException
    {
        if ( (n.id.length() == 0)
           &&(n.kind.length() == 0 ) )
        {
            bth.value = BindingType.ncontext;
            return localRoot;
        }
        InternalBindingKey key = new InternalBindingKey(n);
        InternalBindingValue value =
            (InternalBindingValue) this.theHashtable.get(key);
        if (value == null) return null;
        if( readLogger.isLoggable( Level.FINE ) ) {
            readLogger.fine( LogKeywords.NAMING_RESOLVE_SUCCESS
                + "Namecomponent :" + getName( n ) );
        }
        bth.value = value.theBinding.binding_type;
        return value.theObjectRef;
    }
    public final org.omg.CORBA.Object Unbind(NameComponent n)
        throws org.omg.CORBA.SystemException
    {
        InternalBindingKey key = new InternalBindingKey(n);
        InternalBindingValue value =
            (InternalBindingValue)this.theHashtable.remove(key);
        if (value == null) {
            if( updateLogger.isLoggable( Level.FINE ) ) {
                updateLogger.fine( LogKeywords.NAMING_UNBIND_FAILURE +
                    " There was no binding with the name " + getName( n ) +
                    " to Unbind " );
            }
            return null;
        } else {
            if( updateLogger.isLoggable( Level.FINE ) ) {
                updateLogger.fine( LogKeywords.NAMING_UNBIND_SUCCESS +
                    " NameComponent:  " + getName( n ) );
            }
            return value.theObjectRef;
       }
    }
    public final void List(int how_many, BindingListHolder bl,
                           BindingIteratorHolder bi)
        throws org.omg.CORBA.SystemException
    {
        try {
            TransientBindingIterator bindingIterator =
                new TransientBindingIterator(this.orb,
                (Hashtable)this.theHashtable.clone(), nsPOA);
            bindingIterator.list(how_many,bl);
            byte[] objectId = nsPOA.activate_object( bindingIterator );
            org.omg.CORBA.Object obj = nsPOA.id_to_reference( objectId );
            org.omg.CosNaming.BindingIterator bindingRef =
                org.omg.CosNaming.BindingIteratorHelper.narrow( obj );
            bi.value = bindingRef;
        } catch (org.omg.CORBA.SystemException e) {
            readLogger.warning( LogKeywords.NAMING_LIST_FAILURE + e );
            throw e;
        } catch (Exception e) {
            readLogger.severe( LogKeywords.NAMING_LIST_FAILURE + e );
            throw wrapper.transNcListGotExc( e ) ;
        }
    }
    public final org.omg.CosNaming.NamingContext NewContext()
        throws org.omg.CORBA.SystemException
    {
        try {
            TransientNamingContext transContext =
                new TransientNamingContext(
                (com.sun.corba.se.spi.orb.ORB) orb,localRoot, nsPOA);
            byte[] objectId = nsPOA.activate_object( transContext );
            org.omg.CORBA.Object obj = nsPOA.id_to_reference( objectId );
            lifecycleLogger.fine( "TransientNamingContext " +
                "LIFECYCLE.CREATE SUCCESSFUL" );
            return org.omg.CosNaming.NamingContextHelper.narrow( obj );
        } catch (org.omg.CORBA.SystemException e) {
            lifecycleLogger.log(
                Level.WARNING, LogKeywords.LIFECYCLE_CREATE_FAILURE, e );
            throw e;
        } catch (Exception e) {
            lifecycleLogger.log(
                Level.WARNING, LogKeywords.LIFECYCLE_CREATE_FAILURE, e );
            throw wrapper.transNcNewctxGotExc( e ) ;
        }
    }
    public final void Destroy()
        throws org.omg.CORBA.SystemException
    {
        try {
            byte[] objectId = nsPOA.servant_to_id( this );
            if( objectId != null ) {
                nsPOA.deactivate_object( objectId );
            }
            if( lifecycleLogger.isLoggable( Level.FINE ) ) {
                lifecycleLogger.fine(
                    LogKeywords.LIFECYCLE_DESTROY_SUCCESS );
            }
        } catch (org.omg.CORBA.SystemException e) {
            lifecycleLogger.log( Level.WARNING,
                LogKeywords.LIFECYCLE_DESTROY_FAILURE, e );
            throw e;
        } catch (Exception e) {
            lifecycleLogger.log( Level.WARNING,
                LogKeywords.LIFECYCLE_DESTROY_FAILURE, e );
            throw wrapper.transNcDestroyGotExc( e ) ;
        }
    }
    private String getName( NameComponent n ) {
        return n.id + "." + n.kind;
    }
    public final boolean IsEmpty()
    {
        return this.theHashtable.isEmpty();
    }
    private final Hashtable  theHashtable = new Hashtable();
    public org.omg.CORBA.Object localRoot;
}
