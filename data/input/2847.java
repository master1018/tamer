public abstract class SpecialMethod {
    public abstract boolean isNonExistentMethod() ;
    public abstract String getName();
    public abstract CorbaMessageMediator invoke(java.lang.Object servant,
                                                CorbaMessageMediator request,
                                                byte[] objectId,
                                                ObjectAdapter objectAdapter);
    public static final SpecialMethod getSpecialMethod(String operation) {
        for(int i = 0; i < methods.length; i++)
            if (methods[i].getName().equals(operation))
                return methods[i];
        return null;
    }
    static SpecialMethod[] methods = {
        new IsA(),
        new GetInterface(),
        new NonExistent(),
        new NotExistent()
    };
}
class NonExistent extends SpecialMethod {
    public boolean isNonExistentMethod()
    {
        return true ;
    }
    public String getName() {           
        return "_non_existent";
    }
    public CorbaMessageMediator invoke(java.lang.Object servant,
                                       CorbaMessageMediator request,
                                       byte[] objectId,
                                       ObjectAdapter objectAdapter)
    {
        boolean result = (servant == null) || (servant instanceof NullServant) ;
        CorbaMessageMediator response =
            request.getProtocolHandler().createResponse(request, null);
        ((OutputStream)response.getOutputObject()).write_boolean(result);
        return response;
    }
}
class NotExistent extends NonExistent {
    public String getName() {           
        return "_not_existent";
    }
}
class IsA extends SpecialMethod  {      
    public boolean isNonExistentMethod()
    {
        return false ;
    }
    public String getName() {
        return "_is_a";
    }
    public CorbaMessageMediator invoke(java.lang.Object servant,
                                       CorbaMessageMediator request,
                                       byte[] objectId,
                                       ObjectAdapter objectAdapter)
    {
        if ((servant == null) || (servant instanceof NullServant)) {
            ORB orb = (ORB)request.getBroker() ;
            ORBUtilSystemException wrapper = ORBUtilSystemException.get( orb,
                CORBALogDomains.OA_INVOCATION ) ;
            return request.getProtocolHandler().createSystemExceptionResponse(
                request, wrapper.badSkeleton(), null);
        }
        String[] ids = objectAdapter.getInterfaces( servant, objectId );
        String clientId =
            ((InputStream)request.getInputObject()).read_string();
        boolean answer = false;
        for(int i = 0; i < ids.length; i++)
            if (ids[i].equals(clientId)) {
                answer = true;
                break;
            }
        CorbaMessageMediator response =
            request.getProtocolHandler().createResponse(request, null);
        ((OutputStream)response.getOutputObject()).write_boolean(answer);
        return response;
    }
}
class GetInterface extends SpecialMethod  {     
    public boolean isNonExistentMethod()
    {
        return false ;
    }
    public String getName() {
        return "_interface";
    }
    public CorbaMessageMediator invoke(java.lang.Object servant,
                                       CorbaMessageMediator request,
                                       byte[] objectId,
                                       ObjectAdapter objectAdapter)
    {
        ORB orb = (ORB)request.getBroker() ;
        ORBUtilSystemException wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.OA_INVOCATION ) ;
        if ((servant == null) || (servant instanceof NullServant)) {
            return request.getProtocolHandler().createSystemExceptionResponse(
                request, wrapper.badSkeleton(), null);
        } else {
            return request.getProtocolHandler().createSystemExceptionResponse(
                request, wrapper.getinterfaceNotImplemented(), null);
        }
    }
}
