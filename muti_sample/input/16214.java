public class CachedCodeBase extends _CodeBaseImplBase
{
    private Hashtable implementations, fvds, bases;
    private CodeBase delegate;
    private CorbaConnection conn;
    private static Hashtable iorToCodeBaseObjMap = new Hashtable();
    public CachedCodeBase(CorbaConnection connection) {
        conn = connection;
    }
    public com.sun.org.omg.CORBA.Repository get_ir () {
        return null;
    }
    public String implementation (String repId) {
        String urlResult = null;
        if (implementations == null)
            implementations = new Hashtable();
        else
            urlResult = (String)implementations.get(repId);
        if (urlResult == null && connectedCodeBase()) {
            urlResult = delegate.implementation(repId);
            if (urlResult != null)
                implementations.put(repId, urlResult);
        }
        return urlResult;
    }
    public String[] implementations (String[] repIds) {
        String[] urlResults = new String[repIds.length];
        for (int i = 0; i < urlResults.length; i++)
            urlResults[i] = implementation(repIds[i]);
        return urlResults;
    }
    public FullValueDescription meta (String repId) {
        FullValueDescription result = null;
        if (fvds == null)
            fvds = new Hashtable();
        else
            result = (FullValueDescription)fvds.get(repId);
        if (result == null && connectedCodeBase()) {
            result = delegate.meta(repId);
            if (result != null)
                fvds.put(repId, result);
        }
        return result;
    }
    public FullValueDescription[] metas (String[] repIds) {
        FullValueDescription[] results
            = new FullValueDescription[repIds.length];
        for (int i = 0; i < results.length; i++)
            results[i] = meta(repIds[i]);
        return results;
    }
    public String[] bases (String repId) {
        String[] results = null;
        if (bases == null)
            bases = new Hashtable();
        else
            results = (String[])bases.get(repId);
        if (results == null && connectedCodeBase()) {
            results = delegate.bases(repId);
            if (results != null)
                bases.put(repId, results);
        }
        return results;
    }
    private boolean connectedCodeBase() {
        if (delegate != null)
            return true;
        if (conn.getCodeBaseIOR() == null) {
            if (conn.getBroker().transportDebugFlag)
                conn.dprint("CodeBase unavailable on connection: " + conn);
            return false;
        }
        synchronized(this) {
            if (delegate != null)
                return true;
            delegate = (CodeBase)CachedCodeBase.iorToCodeBaseObjMap.get(conn.getCodeBaseIOR());
            if (delegate != null)
                return true;
            delegate = CodeBaseHelper.narrow(getObjectFromIOR());
            CachedCodeBase.iorToCodeBaseObjMap.put(conn.getCodeBaseIOR(),
                                                   delegate);
        }
        return true;
    }
    private final org.omg.CORBA.Object getObjectFromIOR() {
        return CDRInputStream_1_0.internalIORToObject(
            conn.getCodeBaseIOR(), null , conn.getBroker());
    }
}
