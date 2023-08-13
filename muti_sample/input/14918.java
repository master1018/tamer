public class SnmpStandardObjectServer implements Serializable {
    private static final long serialVersionUID = -4641068116505308488L;
    public void get(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
                    int depth)
        throws SnmpStatusException {
        final Object data = req.getUserData();
        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            final SnmpVarBind var= (SnmpVarBind) e.nextElement();
            try {
                final long id = var.oid.getOidArc(depth);
                var.value = meta.get(id, data);
            } catch(SnmpStatusException x) {
                req.registerGetException(var,x);
            }
        }
    }
    public void set(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
                    int depth)
        throws SnmpStatusException {
        final Object data = req.getUserData();
        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            SnmpVarBind var = null;
            var = (SnmpVarBind) e.nextElement();
            try {
                final long id = var.oid.getOidArc(depth);
                var.value = meta.set(var.value, id, data);
            } catch(SnmpStatusException x) {
                req.registerSetException(var,x);
            }
        }
    }
    public void check(SnmpStandardMetaServer meta, SnmpMibSubRequest req,
                      int depth)
        throws SnmpStatusException {
        final Object data = req.getUserData();
        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            final SnmpVarBind var = (SnmpVarBind) e.nextElement();
            try {
                final long id = var.oid.getOidArc(depth);
                meta.check(var.value,id,data);
            } catch(SnmpStatusException x) {
                req.registerCheckException(var,x);
            }
        }
    }
}
