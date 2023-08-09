public class FVDCodeBaseImpl extends _CodeBaseImplBase
{
    private static Hashtable fvds = new Hashtable();
    private transient ORB orb = null;
    private transient OMGSystemException wrapper = OMGSystemException.get(
        CORBALogDomains.RPC_ENCODING ) ;
    private transient ValueHandlerImpl vhandler = null;
    void setValueHandler(ValueHandler vh)
    {
        vhandler = (com.sun.corba.se.impl.io.ValueHandlerImpl) vh;
    }
    public com.sun.org.omg.CORBA.Repository get_ir (){
        return null;
    }
    public String implementation (String x){
        try{
            if (vhandler == null) {
                vhandler = new ValueHandlerImpl(false);
            }
            String result = Util.getCodebase(vhandler.getClassFromType(x));
            if (result == null)
                return "";
            else
                return result;
        } catch(ClassNotFoundException cnfe){
            throw wrapper.missingLocalValueImpl( CompletionStatus.COMPLETED_MAYBE,
                cnfe ) ;
        }
    }
    public String[] implementations (String[] x){
        String result[] = new String[x.length];
        for (int i = 0; i < x.length; i++)
            result[i] = implementation(x[i]);
        return result;
    }
    public FullValueDescription meta (String x){
        try{
            FullValueDescription result = (FullValueDescription)fvds.get(x);
            if (result == null) {
                if (vhandler == null) {
                    vhandler = new ValueHandlerImpl(false);
                }
                try{
                    result = ValueUtility.translate(_orb(),
                        ObjectStreamClass.lookup(vhandler.getAnyClassFromType(x)), vhandler);
                } catch(Throwable t){
                    if (orb == null)
                        orb = ORB.init(); 
                    result = ValueUtility.translate(orb,
                        ObjectStreamClass.lookup(vhandler.getAnyClassFromType(x)), vhandler);
                }
                if (result != null){
                    fvds.put(x, result);
                } else {
                    throw wrapper.missingLocalValueImpl( CompletionStatus.COMPLETED_MAYBE);
                }
            }
            return result;
        } catch(Throwable t){
            throw wrapper.incompatibleValueImpl(CompletionStatus.COMPLETED_MAYBE,t);
        }
    }
    public FullValueDescription[] metas (String[] x){
        FullValueDescription descriptions[] = new FullValueDescription[x.length];
        for (int i = 0; i < x.length; i++)
            descriptions[i] = meta(x[i]);
        return descriptions;
    }
    public String[] bases (String x){
        try {
            if (vhandler == null) {
                vhandler = new ValueHandlerImpl(false);
            }
            Stack repIds = new Stack();
            Class parent = ObjectStreamClass.lookup(vhandler.getClassFromType(x)).forClass().getSuperclass();
            while (!parent.equals(java.lang.Object.class)) {
                repIds.push(vhandler.createForAnyType(parent));
                parent = parent.getSuperclass();
            }
            String result[] = new String[repIds.size()];
            for (int i = result.length - 1; i >= 0; i++)
                result[i] = (String)repIds.pop();
            return result;
        } catch (Throwable t) {
            throw wrapper.missingLocalValueImpl( CompletionStatus.COMPLETED_MAYBE, t );
        }
    }
}
