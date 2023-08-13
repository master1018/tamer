public class SnmpMibOid extends SnmpMibNode implements Serializable {
    private static final long serialVersionUID = 5012254771107446812L;
    public SnmpMibOid() {
    }
    public void get(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            SnmpVarBind var= (SnmpVarBind) e.nextElement();
            SnmpStatusException x =
                new SnmpStatusException(SnmpStatusException.noSuchObject);
            req.registerGetException(var,x);
        }
    }
    public void set(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            SnmpVarBind var= (SnmpVarBind) e.nextElement();
            SnmpStatusException x =
                new SnmpStatusException(SnmpStatusException.noAccess);
            req.registerSetException(var,x);
        }
    }
    public void check(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
            SnmpVarBind var= (SnmpVarBind) e.nextElement();
            SnmpStatusException x =
                new SnmpStatusException(SnmpStatusException.noAccess);
            req.registerCheckException(var,x);
        }
    }
    void findHandlingNode(SnmpVarBind varbind,
                          long[] oid, int depth,
                          SnmpRequestTree handlers)
        throws SnmpStatusException {
        final int length = oid.length;
        SnmpMibNode node = null;
        if (handlers == null)
            throw new SnmpStatusException(SnmpStatusException.snmpRspGenErr);
        if (depth > length) {
            throw noSuchObjectException;
        } else if (depth == length) {
            throw noSuchInstanceException;
        } else {
            final SnmpMibNode child= getChild(oid[depth]);
            if (child == null)
                handlers.add(this,depth,varbind);
            else
                child.findHandlingNode(varbind,oid,depth+1,handlers);
        }
    }
    long[] findNextHandlingNode(SnmpVarBind varbind,
                                long[] oid, int pos, int depth,
                                SnmpRequestTree handlers,
                                AcmChecker checker)
        throws SnmpStatusException {
        final int length = oid.length;
        SnmpMibNode node = null;
        long[] result = null;
        if (handlers == null)
            throw noSuchObjectException;
        final Object data = handlers.getUserData();
        final int pduVersion = handlers.getRequestPduVersion();
        if (pos >= length) {
            long[] newOid= new long[1];
            newOid[0]=  getNextVarId(-1,data,pduVersion);
            result = findNextHandlingNode(varbind,newOid,0,depth,handlers,
                                          checker);
            return result;
        }
        long[] newOid= new long[1];
        long index= oid[pos];
        while (true) {
            try {
                final SnmpMibNode child = getChild(index);
                if (child == null) {
                    throw noSuchObjectException;
                } else {
                    checker.add(depth, index);
                    try {
                        result = child.findNextHandlingNode(varbind,oid,pos+1,
                                                            depth+1,handlers,
                                                            checker);
                    } finally {
                        checker.remove(depth);
                    }
                }
                result[depth] = index;
                return result;
            } catch(SnmpStatusException e) {
                index= getNextVarId(index,data,pduVersion);
                newOid[0]=index;
                pos= 1;
                oid=newOid;
            }
        }
    }
    public void getRootOid(Vector<Integer> result) {
        if (nbChildren != 1)
            return;
        result.addElement(varList[0]);
        children.firstElement().getRootOid(result);
    }
    public void registerNode(String oidString ,SnmpMibNode node)
        throws IllegalAccessException {
        SnmpOid oid= new SnmpOid(oidString);
        registerNode(oid.longValue(), 0, node);
    }
    void registerNode(long[] oid, int cursor ,SnmpMibNode node)
        throws IllegalAccessException {
        if (cursor >= oid.length)
            throw new IllegalAccessException();
        long var= oid[cursor];
        int pos = retrieveIndex(var);
        if (pos  == nbChildren) {
            nbChildren++;
            varList= new int[nbChildren];
            varList[0]= (int) var;
            pos =0;
            if ( (cursor + 1) == oid.length) {
                children.insertElementAt(node,pos);
                return;
            }
            SnmpMibOid child= new SnmpMibOid();
            children.insertElementAt(child, pos);
            child.registerNode(oid, cursor + 1, node);
            return;
        }
        if (pos == -1) {
            int[] tmp= new int[nbChildren + 1];
            tmp[nbChildren]= (int) var;
            System.arraycopy(varList, 0, tmp, 0, nbChildren);
            varList= tmp;
            nbChildren++;
            SnmpMibNode.sort(varList);
            int newPos = retrieveIndex(var);
            varList[newPos]= (int) var;
            if ( (cursor + 1) == oid.length) {
                children.insertElementAt(node, newPos);
                return;
            }
            SnmpMibOid child= new SnmpMibOid();
            children.insertElementAt(child, newPos);
            child.registerNode(oid, cursor + 1, node);
            return;
        }
        else {
            SnmpMibNode child= children.elementAt(pos);
            if ( (cursor + 1) == oid.length ) {
                if (child == node) return;
                if (child != null && node != null) {
                    if (node instanceof SnmpMibGroup) {
                        ((SnmpMibOid)child).exportChildren((SnmpMibOid)node);
                        children.setElementAt(node,pos);
                        return;
                    } else if ((node instanceof SnmpMibOid) &&
                             (child instanceof SnmpMibGroup)) {
                        ((SnmpMibOid)node).exportChildren((SnmpMibOid)child);
                        return;
                    } else if (node instanceof SnmpMibOid) {
                        ((SnmpMibOid)child).exportChildren((SnmpMibOid)node);
                        children.setElementAt(node,pos);
                        return;
                    }
                }
                children.setElementAt(node,pos);
                return;
            } else {
                if (child == null)
                    throw new IllegalAccessException();
                ((SnmpMibOid)child).registerNode(oid, cursor + 1, node);
            }
        }
    }
    void exportChildren(SnmpMibOid brother)
        throws IllegalAccessException {
        if (brother == null) return;
        final long[] oid = new long[1];
        for (int i=0; i<nbChildren; i++) {
            final SnmpMibNode child = children.elementAt(i);
            if (child == null) continue;
            oid[0] = varList[i];
            brother.registerNode(oid,0,child);
        }
    }
    SnmpMibNode getChild(long id) throws SnmpStatusException {
        final int pos= getInsertAt(id);
        if (pos >= nbChildren)
            throw noSuchObjectException;
        if (varList[pos] != (int) id)
            throw noSuchObjectException;
        SnmpMibNode child = null;
        try {
            child = children.elementAtNonSync(pos);
        } catch(ArrayIndexOutOfBoundsException e) {
            throw noSuchObjectException;
        }
        if (child == null)
            throw noSuchInstanceException;
        return child;
    }
    private int retrieveIndex(long val) {
        int low= 0;
        int cursor= (int) val;
        if (varList == null || varList.length < 1)
            return nbChildren;
        int max= varList.length -1 ;
        int curr= low + (max-low)/2;
        int elmt= 0;
        while (low <= max) {
            elmt= varList[curr];
            if (cursor == elmt) {
                return curr;
            }
            if (elmt < cursor) {
                low= curr +1;
            } else {
                max= curr -1;
            }
            curr= low + (max-low)/2;
        }
        return -1;
    }
    private int getInsertAt(long val) {
        int low= 0;
        final int index= (int) val;
        if (varList == null)
            return -1;
        int max= varList.length -1 ;
        int elmt=0;
        int curr= low + (max-low)/2;
        while (low <= max) {
            elmt= varList[curr];
            if (index == elmt)
                return curr;
            if (elmt < index) {
                low= curr +1;
            } else {
                max= curr -1;
            }
            curr= low + (max-low)/2;
        }
        return curr;
    }
    private NonSyncVector<SnmpMibNode> children = new NonSyncVector<SnmpMibNode>(1);
    private int nbChildren= 0;
    @SuppressWarnings("serial")  
    class NonSyncVector<E> extends Vector<E> {
        public NonSyncVector(int size) {
            super(size);
        }
        final void addNonSyncElement(E obj) {
            ensureCapacity(elementCount + 1);
            elementData[elementCount++] = obj;
        }
        @SuppressWarnings("unchecked")  
        final E elementAtNonSync(int index) {
            return (E) elementData[index];
        }
    }
}
