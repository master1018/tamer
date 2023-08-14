public class RequestImpl
    extends Request
{
    protected org.omg.CORBA.Object _target;
    protected String             _opName;
    protected NVList             _arguments;
    protected ExceptionList      _exceptions;
    private NamedValue           _result;
    protected Environment        _env;
    private Context              _ctx;
    private ContextList          _ctxList;
    protected ORB                _orb;
    private ORBUtilSystemException _wrapper;
    protected boolean            _isOneWay      = false;
    private int[]                _paramCodes;
    private long[]               _paramLongs;
    private java.lang.Object[]   _paramObjects;
    protected boolean            gotResponse    = false;
    public RequestImpl (ORB orb,
                        org.omg.CORBA.Object targetObject,
                        Context ctx,
                        String operationName,
                        NVList argumentList,
                        NamedValue resultContainer,
                        ExceptionList exceptionList,
                        ContextList ctxList)
    {
        _orb    = orb;
        _wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.OA_INVOCATION ) ;
        _target     = targetObject;
        _ctx    = ctx;
        _opName = operationName;
        if (argumentList == null)
            _arguments = new NVListImpl(_orb);
        else
            _arguments = argumentList;
        _result = resultContainer;
        if (exceptionList == null)
            _exceptions = new ExceptionListImpl();
        else
            _exceptions = exceptionList;
        if (ctxList == null)
            _ctxList = new ContextListImpl(_orb);
        else
            _ctxList = ctxList;
        _env    = new EnvironmentImpl();
    }
    public org.omg.CORBA.Object target()
    {
        return _target;
    }
    public String operation()
    {
        return _opName;
    }
    public NVList arguments()
    {
        return _arguments;
    }
    public NamedValue result()
    {
        return _result;
    }
    public Environment env()
    {
        return _env;
    }
    public ExceptionList exceptions()
    {
        return _exceptions;
    }
    public ContextList contexts()
    {
        return _ctxList;
    }
    public synchronized Context ctx()
    {
        if (_ctx == null)
            _ctx = new ContextImpl(_orb);
        return _ctx;
    }
    public synchronized void ctx(Context newCtx)
    {
        _ctx = newCtx;
    }
    public synchronized Any add_in_arg()
    {
        return _arguments.add(org.omg.CORBA.ARG_IN.value).value();
    }
    public synchronized Any add_named_in_arg(String name)
    {
        return _arguments.add_item(name, org.omg.CORBA.ARG_IN.value).value();
    }
    public synchronized Any add_inout_arg()
    {
        return _arguments.add(org.omg.CORBA.ARG_INOUT.value).value();
    }
    public synchronized Any add_named_inout_arg(String name)
    {
        return _arguments.add_item(name, org.omg.CORBA.ARG_INOUT.value).value();
    }
    public synchronized Any add_out_arg()
    {
        return _arguments.add(org.omg.CORBA.ARG_OUT.value).value();
    }
    public synchronized Any add_named_out_arg(String name)
    {
        return _arguments.add_item(name, org.omg.CORBA.ARG_OUT.value).value();
    }
    public synchronized void set_return_type(TypeCode tc)
    {
        if (_result == null)
            _result = new NamedValueImpl(_orb);
        _result.value().type(tc);
    }
    public synchronized Any return_value()
    {
        if (_result == null)
            _result = new NamedValueImpl(_orb);
        return _result.value();
    }
    public synchronized void add_exception(TypeCode exceptionType)
    {
        _exceptions.add(exceptionType);
    }
    public synchronized void invoke()
    {
        doInvocation();
    }
    public synchronized void send_oneway()
    {
        _isOneWay = true;
        doInvocation();
    }
    public synchronized void send_deferred()
    {
        AsynchInvoke invokeObject = new AsynchInvoke(_orb, this, false);
        new Thread(invokeObject).start();
    }
    public synchronized boolean poll_response()
    {
        return gotResponse;
    }
    public synchronized void get_response()
        throws org.omg.CORBA.WrongTransaction
    {
        while (gotResponse == false) {
            try {
                wait();
            }
            catch (InterruptedException e) {}
        }
    }
    protected void doInvocation()
    {
        org.omg.CORBA.portable.Delegate delegate = StubAdapter.getDelegate(
            _target ) ;
        _orb.getPIHandler().initiateClientPIRequest( true );
        _orb.getPIHandler().setClientPIInfo( this );
        InputStream $in = null;
        try {
            OutputStream $out = delegate.request(null, _opName, !_isOneWay);
            try {
                for (int i=0; i<_arguments.count() ; i++) {
                    NamedValue nv = _arguments.item(i);
                    switch (nv.flags()) {
                    case ARG_IN.value:
                        nv.value().write_value($out);
                        break;
                    case ARG_OUT.value:
                        break;
                    case ARG_INOUT.value:
                        nv.value().write_value($out);
                        break;
                    }
                }
            } catch ( org.omg.CORBA.Bounds ex ) {
                throw _wrapper.boundsErrorInDiiRequest( ex ) ;
            }
            $in = delegate.invoke(null, $out);
        } catch (ApplicationException e) {
        } catch (RemarshalException e) {
            doInvocation();
        } catch( SystemException ex ) {
            _env.exception(ex);
            throw ex;
        } finally {
            delegate.releaseReply(null, $in);
        }
    }
    public void unmarshalReply(InputStream is)
    {
        if ( _result != null ) {
            Any returnAny = _result.value();
            TypeCode returnType = returnAny.type();
            if ( returnType.kind().value() != TCKind._tk_void )
                returnAny.read_value(is, returnType);
        }
        try {
            for ( int i=0; i<_arguments.count() ; i++) {
                NamedValue nv = _arguments.item(i);
                switch( nv.flags() ) {
                case ARG_IN.value:
                    break;
                case ARG_OUT.value:
                case ARG_INOUT.value:
                    Any any = nv.value();
                    any.read_value(is, any.type());
                    break;
                }
            }
        }
        catch ( org.omg.CORBA.Bounds ex ) {
        }
    }
}
