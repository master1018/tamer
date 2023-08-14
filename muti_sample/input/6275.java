public class ServerRequestImpl extends ServerRequest {
    private ORB                  _orb           = null;
    private ORBUtilSystemException _wrapper     = null;
    private String               _opName        = null;
    private NVList               _arguments     = null;
    private Context              _ctx           = null;
    private InputStream          _ins           = null;
    private boolean             _paramsCalled   = false;
    private boolean             _resultSet      = false;
    private boolean             _exceptionSet   = false;
    private Any                 _resultAny      = null;
    private Any                 _exception      = null;
    public ServerRequestImpl (CorbaMessageMediator req, ORB orb) {
        _opName = req.getOperationName();
        _ins    = (InputStream)req.getInputObject();
        _ctx    = null;         
        _orb = orb;
        _wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.OA_INVOCATION ) ;
    }
    public String operation() {
        return _opName;
    }
    public void arguments(NVList args)
    {
        if (_paramsCalled)
            throw _wrapper.argumentsCalledMultiple() ;
        if (_exceptionSet)
            throw _wrapper.argumentsCalledAfterException() ;
        if (args == null )
            throw _wrapper.argumentsCalledNullArgs() ;
        _paramsCalled = true;
        NamedValue arg = null;
        for (int i=0; i < args.count() ; i++) {
            try {
                arg = args.item(i);
            } catch (Bounds e) {
                throw _wrapper.boundsCannotOccur(e) ;
            }
            try {
                if ((arg.flags() == org.omg.CORBA.ARG_IN.value) ||
                    (arg.flags() == org.omg.CORBA.ARG_INOUT.value)) {
                    arg.value().read_value(_ins, arg.value().type());
                }
            } catch ( Exception ex ) {
                throw _wrapper.badArgumentsNvlist( ex ) ;
            }
        }
        _arguments = args;
        _orb.getPIHandler().setServerPIInfo( _arguments );
        _orb.getPIHandler().invokeServerPIIntermediatePoint();
    }
    public void set_result(Any res) {
        if (!_paramsCalled)
            throw _wrapper.argumentsNotCalled() ;
        if (_resultSet)
            throw _wrapper.setResultCalledMultiple() ;
        if (_exceptionSet)
            throw _wrapper.setResultAfterException() ;
        if ( res == null )
            throw _wrapper.setResultCalledNullArgs() ;
        _resultAny = res;
        _resultSet = true;
        _orb.getPIHandler().setServerPIInfo( _resultAny );
    }
    public void set_exception(Any exc)
    {
        if ( exc == null )
            throw _wrapper.setExceptionCalledNullArgs() ;
        TCKind kind = exc.type().kind();
        if ( kind != TCKind.tk_except )
            throw _wrapper.setExceptionCalledBadType() ;
        _exception = exc;
        _orb.getPIHandler().setServerPIExceptionInfo( _exception );
        if( !_exceptionSet && !_paramsCalled ) {
            _orb.getPIHandler().invokeServerPIIntermediatePoint();
        }
        _exceptionSet = true;
    }
    public Any checkResultCalled()
    {
        if ( _paramsCalled && _resultSet ) 
            return null;
        else if ( _paramsCalled && !_resultSet && !_exceptionSet ) {
            try {
                TypeCode result_tc = _orb.get_primitive_tc(
                    org.omg.CORBA.TCKind.tk_void);
                _resultAny = _orb.create_any();
                _resultAny.type(result_tc);
                _resultSet = true;
                return null;
            } catch ( Exception ex ) {
                throw _wrapper.dsiResultException(
                    CompletionStatus.COMPLETED_MAYBE, ex ) ;
            }
        } else if ( _exceptionSet )
            return _exception;
        else {
            throw _wrapper.dsimethodNotcalled(
                CompletionStatus.COMPLETED_MAYBE ) ;
        }
    }
    public void marshalReplyParams(OutputStream os)
    {
        _resultAny.write_value(os);
        NamedValue arg = null;
        for (int i=0; i < _arguments.count() ; i++) {
            try {
                arg = _arguments.item(i);
            } catch (Bounds e) {}
            if ((arg.flags() == org.omg.CORBA.ARG_OUT.value) ||
                (arg.flags() == org.omg.CORBA.ARG_INOUT.value)) {
                arg.value().write_value(os);
            }
        }
    }
    public Context ctx()
    {
        if ( !_paramsCalled || _resultSet || _exceptionSet )
            throw _wrapper.contextCalledOutOfOrder() ;
        throw _wrapper.contextNotImplemented() ;
    }
}
