public abstract class NamingContextImpl
    extends NamingContextExtPOA
    implements NamingContextDataStore
{
    protected POA nsPOA;
    private Logger readLogger, updateLogger, lifecycleLogger;
    private NamingSystemException wrapper ;
    private static NamingSystemException staticWrapper =
        NamingSystemException.get( CORBALogDomains.NAMING_UPDATE ) ;
    private InterOperableNamingImpl insImpl;
    public NamingContextImpl(ORB orb, POA poa) throws java.lang.Exception {
        super();
        this.orb = orb;
        wrapper = NamingSystemException.get( orb,
            CORBALogDomains.NAMING_UPDATE ) ;
        insImpl = new InterOperableNamingImpl( );
        this.nsPOA = poa;
        readLogger = orb.getLogger( CORBALogDomains.NAMING_READ);
        updateLogger = orb.getLogger( CORBALogDomains.NAMING_UPDATE);
        lifecycleLogger = orb.getLogger(
            CORBALogDomains.NAMING_LIFECYCLE);
    }
    public POA getNSPOA( ) {
        return nsPOA;
    }
    public void bind(NameComponent[] n, org.omg.CORBA.Object obj)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName,
               org.omg.CosNaming.NamingContextPackage.AlreadyBound
    {
        if( obj == null )
        {
            updateLogger.warning( LogKeywords.NAMING_BIND +
                " unsuccessful because NULL Object cannot be Bound " );
            throw wrapper.objectIsNull() ;
        }
        NamingContextDataStore impl = (NamingContextDataStore)this;
        doBind(impl,n,obj,false,BindingType.nobject);
        if( updateLogger.isLoggable( Level.FINE  ) ) {
            updateLogger.fine( LogKeywords.NAMING_BIND_SUCCESS + " Name = " +
                NamingUtils.getDirectoryStructuredName( n ) );
        }
    }
    public void bind_context(NameComponent[] n, NamingContext nc)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName,
               org.omg.CosNaming.NamingContextPackage.AlreadyBound
    {
        if( nc == null ) {
            updateLogger.warning( LogKeywords.NAMING_BIND_FAILURE +
                " NULL Context cannot be Bound " );
            throw new BAD_PARAM( "Naming Context should not be null " );
        }
        NamingContextDataStore impl = (NamingContextDataStore)this;
        doBind(impl,n,nc,false,BindingType.ncontext);
        if( updateLogger.isLoggable( Level.FINE ) ) {
            updateLogger.fine( LogKeywords.NAMING_BIND_SUCCESS + " Name = " +
                NamingUtils.getDirectoryStructuredName( n ) );
        }
    }
    public  void rebind(NameComponent[] n, org.omg.CORBA.Object obj)
        throws       org.omg.CosNaming.NamingContextPackage.NotFound,
                     org.omg.CosNaming.NamingContextPackage.CannotProceed,
                     org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if( obj == null )
        {
            updateLogger.warning( LogKeywords.NAMING_REBIND_FAILURE +
                " NULL Object cannot be Bound " );
            throw wrapper.objectIsNull() ;
        }
        try {
            NamingContextDataStore impl = (NamingContextDataStore)this;
            doBind(impl,n,obj,true,BindingType.nobject);
        } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound ex) {
            updateLogger.warning( LogKeywords.NAMING_REBIND_FAILURE +
                NamingUtils.getDirectoryStructuredName( n ) +
                " is already bound to a Naming Context" );
            throw wrapper.namingCtxRebindAlreadyBound( ex ) ;
        }
        if( updateLogger.isLoggable( Level.FINE  ) ) {
            updateLogger.fine( LogKeywords.NAMING_REBIND_SUCCESS + " Name = " +
                NamingUtils.getDirectoryStructuredName( n ) );
        }
    }
    public  void rebind_context(NameComponent[] n, NamingContext nc)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if( nc == null )
        {
            updateLogger.warning( LogKeywords.NAMING_REBIND_FAILURE +
                " NULL Context cannot be Bound " );
            throw wrapper.objectIsNull() ;
        }
        try {
            NamingContextDataStore impl = (NamingContextDataStore)this;
            doBind(impl,n,nc,true,BindingType.ncontext);
        } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound ex) {
            updateLogger.warning( LogKeywords.NAMING_REBIND_FAILURE +
                NamingUtils.getDirectoryStructuredName( n ) +
                " is already bound to a CORBA Object" );
            throw wrapper.namingCtxRebindctxAlreadyBound( ex ) ;
        }
        if( updateLogger.isLoggable( Level.FINE ) ) {
            updateLogger.fine( LogKeywords.NAMING_REBIND_SUCCESS + " Name = " +
                NamingUtils.getDirectoryStructuredName( n ) );
        }
    }
    public  org.omg.CORBA.Object resolve(NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        NamingContextDataStore impl = (NamingContextDataStore)this;
        org.omg.CORBA.Object obj = doResolve(impl,n);
        if( obj != null ) {
            if( readLogger.isLoggable( Level.FINE ) ) {
                 readLogger.fine( LogKeywords.NAMING_RESOLVE_SUCCESS +
                 " Name: " + NamingUtils.getDirectoryStructuredName( n ) );
            }
        } else {
             readLogger.warning( LogKeywords.NAMING_RESOLVE_FAILURE +
                 " Name: " + NamingUtils.getDirectoryStructuredName( n ) );
        }
        return obj;
    }
    public  void unbind(NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        NamingContextDataStore impl = (NamingContextDataStore)this;
        doUnbind(impl,n);
        if( updateLogger.isLoggable( Level.FINE ) ) {
            updateLogger.fine( LogKeywords.NAMING_UNBIND_SUCCESS +
                " Name: " + NamingUtils.getDirectoryStructuredName( n ) );
        }
    }
    public  void list(int how_many, BindingListHolder bl,
        BindingIteratorHolder bi)
    {
        NamingContextDataStore impl = (NamingContextDataStore)this;
        synchronized (impl) {
            impl.List(how_many,bl,bi);
        }
        if( readLogger.isLoggable( Level.FINE ) && (bl.value != null )) {
            readLogger.fine ( LogKeywords.NAMING_LIST_SUCCESS +
                "list(" + how_many + ") -> bindings[" + bl.value.length +
                "] + iterator: " + bi.value);
        }
    }
    public synchronized NamingContext new_context()
    {
        lifecycleLogger.fine( "Creating New Naming Context " );
        NamingContextDataStore impl = (NamingContextDataStore)this;
        synchronized (impl) {
            NamingContext nctx = impl.NewContext();
            if( nctx != null ) {
                lifecycleLogger.fine( LogKeywords.LIFECYCLE_CREATE_SUCCESS );
            } else {
                lifecycleLogger.severe ( LogKeywords.LIFECYCLE_CREATE_FAILURE );
            }
            return nctx;
        }
    }
    public  NamingContext bind_new_context(NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.AlreadyBound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        NamingContext nc = null;
        NamingContext rnc = null;
        try {
            if (debug)
                dprint("bind_new_context " + nameToString(n));
            nc = this.new_context();
            this.bind_context(n,nc);
            rnc = nc;
            nc = null;
        } finally {
            try {
                if(nc != null)
                    nc.destroy();
            } catch (org.omg.CosNaming.NamingContextPackage.NotEmpty e) {
            }
        }
        if( updateLogger.isLoggable( Level.FINE ) ) {
            updateLogger.fine ( LogKeywords.NAMING_BIND +
                "New Context Bound To " +
                NamingUtils.getDirectoryStructuredName( n ) );
        }
        return rnc;
    }
    public  void destroy()
        throws org.omg.CosNaming.NamingContextPackage.NotEmpty
    {
        lifecycleLogger.fine( "Destroying Naming Context " );
        NamingContextDataStore impl = (NamingContextDataStore)this;
        synchronized (impl) {
            if (impl.IsEmpty() == true) {
                impl.Destroy();
                lifecycleLogger.fine ( LogKeywords.LIFECYCLE_DESTROY_SUCCESS );
            }
            else {
                lifecycleLogger.warning( LogKeywords.LIFECYCLE_DESTROY_FAILURE +
                    " NamingContext children are not destroyed still.." );
                throw new NotEmpty();
            }
        }
    }
    public static void doBind(NamingContextDataStore impl,
                              NameComponent[] n,
                              org.omg.CORBA.Object obj,
                              boolean rebind,
                              org.omg.CosNaming.BindingType bt)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName,
               org.omg.CosNaming.NamingContextPackage.AlreadyBound
    {
        if (n.length < 1)
            throw new InvalidName();
        if (n.length == 1) {
            if ( (n[0].id.length() == 0) && (n[0].kind.length() == 0 ) ) {
                throw new InvalidName();
            }
            synchronized (impl) {
                BindingTypeHolder bth = new BindingTypeHolder();
                if (rebind) {
                    org.omg.CORBA.Object objRef = impl.Resolve( n[0], bth );
                    if( objRef != null ) {
                        if ( bth.value.value() == BindingType.nobject.value() ){
                            if ( bt.value() == BindingType.ncontext.value() ) {
                                throw new NotFound(
                                    NotFoundReason.not_context, n);
                            }
                        } else {
                            if ( bt.value() == BindingType.nobject.value() ) {
                                throw new NotFound(
                                    NotFoundReason.not_object, n);
                            }
                        }
                        impl.Unbind(n[0]);
                    }
                } else {
                    if (impl.Resolve(n[0],bth) != null)
                        throw new AlreadyBound();
                }
                impl.Bind(n[0],obj,bt);
            }
        } else {
            NamingContext context = resolveFirstAsContext(impl,n);
            NameComponent[] tail = new NameComponent[n.length - 1];
            System.arraycopy(n,1,tail,0,n.length-1);
            switch (bt.value()) {
            case BindingType._nobject:
                {
                    if (rebind)
                        context.rebind(tail,obj);
                    else
                        context.bind(tail,obj);
                }
                break;
            case BindingType._ncontext:
                {
                    NamingContext objContext = (NamingContext)obj;
                    if (rebind)
                        context.rebind_context(tail,objContext);
                    else
                        context.bind_context(tail,objContext);
                }
                break;
            default:
                throw staticWrapper.namingCtxBadBindingtype() ;
            }
        }
    }
    public static org.omg.CORBA.Object doResolve(NamingContextDataStore impl,
                                                 NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        org.omg.CORBA.Object obj = null;
        BindingTypeHolder bth = new BindingTypeHolder();
        if (n.length < 1)
            throw new InvalidName();
        if (n.length == 1) {
            synchronized (impl) {
                obj = impl.Resolve(n[0],bth);
            }
            if (obj == null) {
                throw new NotFound(NotFoundReason.missing_node,n);
            }
            return obj;
        } else {
            if ( (n[1].id.length() == 0) && (n[1].kind.length() == 0) ) {
                throw new InvalidName();
            }
            NamingContext context = resolveFirstAsContext(impl,n);
            NameComponent[] tail = new NameComponent[n.length -1];
            System.arraycopy(n,1,tail,0,n.length-1);
            try {
                Servant servant = impl.getNSPOA().reference_to_servant(
                    context );
                return doResolve(((NamingContextDataStore)servant), tail) ;
            } catch( Exception e ) {
                return context.resolve(tail);
            }
        }
    }
    public static void doUnbind(NamingContextDataStore impl,
                                NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if (n.length < 1)
            throw new InvalidName();
        if (n.length == 1) {
            if ( (n[0].id.length() == 0) && (n[0].kind.length() == 0 ) ) {
                throw new InvalidName();
            }
            org.omg.CORBA.Object objRef = null;
            synchronized (impl) {
                objRef = impl.Unbind(n[0]);
            }
            if (objRef == null)
                throw new NotFound(NotFoundReason.missing_node,n);
            return;
        } else {
            NamingContext context = resolveFirstAsContext(impl,n);
            NameComponent[] tail = new NameComponent[n.length - 1];
            System.arraycopy(n,1,tail,0,n.length-1);
            context.unbind(tail);
        }
    }
    protected static NamingContext resolveFirstAsContext(NamingContextDataStore impl,
                                                         NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound {
        org.omg.CORBA.Object topRef = null;
        BindingTypeHolder bth = new BindingTypeHolder();
        NamingContext context = null;
        synchronized (impl) {
            topRef = impl.Resolve(n[0],bth);
            if (topRef == null) {
                throw new NotFound(NotFoundReason.missing_node,n);
            }
        }
        if (bth.value != BindingType.ncontext) {
            throw new NotFound(NotFoundReason.not_context,n);
        }
        try {
            context = NamingContextHelper.narrow(topRef);
        } catch (org.omg.CORBA.BAD_PARAM ex) {
            throw new NotFound(NotFoundReason.not_context,n);
        }
        return context;
    }
    public String to_string(org.omg.CosNaming.NameComponent[] n)
         throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if ( (n == null ) || (n.length == 0) )
        {
                throw new InvalidName();
        }
        NamingContextDataStore impl = (NamingContextDataStore)this;
        String theStringifiedName = insImpl.convertToString( n );
        if( theStringifiedName == null )
        {
                throw new InvalidName();
        }
        return theStringifiedName;
    }
    public org.omg.CosNaming.NameComponent[] to_name(String sn)
         throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if  ( (sn == null ) || (sn.length() == 0) )
        {
                throw new InvalidName();
        }
        NamingContextDataStore impl = (NamingContextDataStore)this;
        org.omg.CosNaming.NameComponent[] theNameComponents =
                insImpl.convertToNameComponent( sn );
        if( ( theNameComponents == null ) || (theNameComponents.length == 0 ) )
        {
                throw new InvalidName();
        }
        for( int i = 0; i < theNameComponents.length; i++ ) {
            if ( ( ( theNameComponents[i].id  == null )
                 ||( theNameComponents[i].id.length() == 0 ) )
               &&( ( theNameComponents[i].kind == null )
                 ||( theNameComponents[i].kind.length() == 0 ) ) ) {
                throw new InvalidName();
            }
        }
        return theNameComponents;
    }
    public String to_url(String addr, String sn)
        throws org.omg.CosNaming.NamingContextExtPackage.InvalidAddress,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if  ( (sn == null ) || (sn.length() == 0) )
        {
            throw new InvalidName();
        }
        if( addr == null )
        {
            throw new
                org.omg.CosNaming.NamingContextExtPackage.InvalidAddress();
        }
        NamingContextDataStore impl = (NamingContextDataStore)this;
        String urlBasedAddress = null;
        urlBasedAddress = insImpl.createURLBasedAddress( addr, sn );
        try {
            INSURLHandler.getINSURLHandler( ).parseURL( urlBasedAddress );
        } catch( BAD_PARAM e ) {
            throw new
                org.omg.CosNaming.NamingContextExtPackage.InvalidAddress();
        }
        return urlBasedAddress;
    }
    public org.omg.CORBA.Object resolve_str(String sn)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        org.omg.CORBA.Object theObject = null;
        if  ( (sn == null ) || (sn.length() == 0) )
        {
                throw new InvalidName();
        }
        NamingContextDataStore impl = (NamingContextDataStore)this;
        org.omg.CosNaming.NameComponent[] theNameComponents =
                insImpl.convertToNameComponent( sn );
        if( ( theNameComponents == null ) || (theNameComponents.length == 0 ) )
        {
                throw new InvalidName();
        }
        theObject = resolve( theNameComponents );
        return theObject;
    }
    transient protected ORB orb;
    public static String nameToString(NameComponent[] name)
    {
        StringBuffer s = new StringBuffer("{");
        if (name != null || name.length > 0) {
            for (int i=0;i<name.length;i++) {
                if (i>0)
                    s.append(",");
                s.append("[").
                    append(name[i].id).
                    append(",").
                    append(name[i].kind).
                    append("]");
            }
        }
        s.append("}");
        return s.toString();
    }
    public static final boolean debug = false;
    private static void dprint(String msg) {
        NamingUtils.dprint("NamingContextImpl("  +
                           Thread.currentThread().getName() + " at " +
                           System.currentTimeMillis() +
                           " ems): " + msg);
    }
}
