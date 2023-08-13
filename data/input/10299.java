public abstract class SnmpMibTable extends SnmpMibNode
    implements NotificationBroadcaster, Serializable {
    public SnmpMibTable(SnmpMib mib) {
        this.theMib= mib;
        setCreationEnabled(false);
    }
    public abstract void createNewEntry(SnmpMibSubRequest req, SnmpOid rowOid,
                                        int depth)
        throws SnmpStatusException;
    public abstract boolean isRegistrationRequired();
    public boolean isCreationEnabled() {
        return creationEnabled;
    }
    public void setCreationEnabled(boolean remoteCreationFlag) {
        creationEnabled = remoteCreationFlag;
    }
    public boolean hasRowStatus() {
        return false;
    }
    public void get(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        final boolean         isnew  = req.isNewEntry();
        final SnmpMibSubRequest  r      = req;
        if (isnew) {
            SnmpVarBind     var = null;
            for (Enumeration e= r.getElements(); e.hasMoreElements();) {
                var      = (SnmpVarBind) e.nextElement();
                r.registerGetException(var,noSuchInstanceException);
            }
        }
        final SnmpOid     oid    = r.getEntryOid();
        get(req,oid,depth+1);
    }
    public void check(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        final SnmpOid     oid    = req.getEntryOid();
        final int         action = getRowAction(req,oid,depth+1);
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMibTable.class.getName(),
                    "check", "Calling beginRowAction");
        }
        beginRowAction(req,oid,depth+1,action);
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMibTable.class.getName(),
                    "check",
                    "Calling check for " + req.getSize() + " varbinds");
        }
        check(req,oid,depth+1);
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMibTable.class.getName(),
                    "check", "check finished");
        }
    }
    public void set(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMibTable.class.getName(),
                    "set", "Entering set");
        }
        final SnmpOid     oid    = req.getEntryOid();
        final int         action = getRowAction(req,oid,depth+1);
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMibTable.class.getName(),
                    "set", "Calling set for " + req.getSize() + " varbinds");
        }
        set(req,oid,depth+1);
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMibTable.class.getName(),
                    "set", "Calling endRowAction");
        }
        endRowAction(req,oid,depth+1,action);
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMibTable.class.getName(),
                    "set", "RowAction finished");
        }
    }
     public void addEntry(SnmpOid rowOid, Object entry)
        throws SnmpStatusException {
         addEntry(rowOid, null, entry);
    }
    public synchronized void addEntry(SnmpOid oid, ObjectName name,
                                      Object entry)
        throws SnmpStatusException {
        if (isRegistrationRequired() == true && name == null)
            throw new SnmpStatusException(SnmpStatusException.badValue);
        if (size == 0) {
            insertOid(0,oid);
            if (entries != null)
                entries.addElement(entry);
            if (entrynames != null)
                entrynames.addElement(name);
            size++;
            if (factory != null) {
                try {
                    factory.addEntryCb(0,oid,name,entry,this);
                } catch (SnmpStatusException x) {
                    removeOid(0);
                    if (entries != null)
                        entries.removeElementAt(0);
                    if (entrynames != null)
                        entrynames.removeElementAt(0);
                    throw x;
                }
            }
            sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_ADDED,
                             (new Date()).getTime(), entry, name);
            return;
        }
        int pos= 0;
        pos= getInsertionPoint(oid,true);
        if (pos == size) {
            insertOid(tablecount,oid);
            if (entries != null)
                entries.addElement(entry);
            if (entrynames != null)
                entrynames.addElement(name);
            size++;
        } else {
            try {
                insertOid(pos,oid);
                if (entries != null)
                    entries.insertElementAt(entry, pos);
                if (entrynames != null)
                    entrynames.insertElementAt(name,pos);
                size++;
            } catch(ArrayIndexOutOfBoundsException e) {
            }
        }
        if (factory != null) {
            try {
                factory.addEntryCb(pos,oid,name,entry,this);
            } catch (SnmpStatusException x) {
                removeOid(pos);
                if (entries != null)
                    entries.removeElementAt(pos);
                if (entrynames != null)
                    entrynames.removeElementAt(pos);
                throw x;
            }
        }
        sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_ADDED,
                         (new Date()).getTime(), entry, name);
    }
    public synchronized void removeEntry(SnmpOid rowOid, Object entry)
        throws SnmpStatusException {
        int pos = findObject(rowOid);
        if (pos == -1)
            return;
        removeEntry(pos,entry);
    }
    public void removeEntry(SnmpOid rowOid)
        throws SnmpStatusException {
        int pos = findObject(rowOid);
        if (pos == -1)
            return;
        removeEntry(pos,null);
    }
    public synchronized void removeEntry(int pos, Object entry)
        throws SnmpStatusException {
        if (pos == -1)
            return;
        if (pos >= size) return;
        Object obj = entry;
        if (entries != null && entries.size() > pos) {
            obj = entries.elementAt(pos);
            entries.removeElementAt(pos);
        }
        ObjectName name = null;
        if (entrynames != null && entrynames.size() > pos) {
            name = entrynames.elementAt(pos);
            entrynames.removeElementAt(pos);
        }
        final SnmpOid rowOid = tableoids[pos];
        removeOid(pos);
        size --;
        if (obj == null) obj = entry;
        if (factory != null)
            factory.removeEntryCb(pos,rowOid,name,obj,this);
        sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_REMOVED,
                         (new Date()).getTime(), obj, name);
    }
    public synchronized Object getEntry(SnmpOid rowOid)
        throws SnmpStatusException {
        int pos= findObject(rowOid);
        if (pos == -1)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return entries.elementAt(pos);
    }
    public synchronized ObjectName getEntryName(SnmpOid rowOid)
        throws SnmpStatusException {
        int pos = findObject(rowOid);
        if (entrynames == null) return null;
        if (pos == -1 || pos >= entrynames.size())
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return entrynames.elementAt(pos);
    }
    public Object[] getBasicEntries() {
        Object[] array= new Object[size];
        entries.copyInto(array);
        return array;
    }
    public int getSize() {
        return size;
    }
    public synchronized void
        addNotificationListener(NotificationListener listener,
                                NotificationFilter filter, Object handback)  {
        if (listener == null) {
            throw new java.lang.IllegalArgumentException
                ("Listener can't be null") ;
        }
        Vector<Object> handbackList =
            handbackTable.get(listener) ;
        Vector<NotificationFilter> filterList =
            filterTable.get(listener) ;
        if ( handbackList == null ) {
            handbackList = new Vector<Object>() ;
            filterList = new Vector<NotificationFilter>() ;
            handbackTable.put(listener, handbackList) ;
            filterTable.put(listener, filterList) ;
        }
        handbackList.addElement(handback) ;
        filterList.addElement(filter) ;
    }
    public synchronized void
        removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {
        java.util.Vector handbackList =
            (java.util.Vector) handbackTable.get(listener) ;
        java.util.Vector filterList =
            (java.util.Vector) filterTable.get(listener) ;
        if ( handbackList == null ) {
            throw new ListenerNotFoundException("listener");
        }
        handbackTable.remove(listener) ;
        filterTable.remove(listener) ;
    }
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = {SnmpTableEntryNotification.SNMP_ENTRY_ADDED,
                          SnmpTableEntryNotification.SNMP_ENTRY_REMOVED};
        MBeanNotificationInfo[] notifsInfo = {
            new MBeanNotificationInfo
            (types, "com.sun.jmx.snmp.agent.SnmpTableEntryNotification",
             "Notifications sent by the SnmpMibTable")
        };
        return notifsInfo;
    }
    public void registerEntryFactory(SnmpTableEntryFactory factory) {
        this.factory = factory;
    }
    protected boolean isRowStatus(SnmpOid rowOid, long var,
                                    Object  userData) {
        return false;
    }
    protected int getRowAction(SnmpMibSubRequest req, SnmpOid rowOid,
                               int depth)
        throws SnmpStatusException {
        final boolean     isnew  = req.isNewEntry();
        final SnmpVarBind vb = req.getRowStatusVarBind();
        if (vb == null) {
            if (isnew && ! hasRowStatus())
                return EnumRowStatus.createAndGo;
            else return EnumRowStatus.unspecified;
        }
        try {
            return mapRowStatus(rowOid, vb, req.getUserData());
        } catch( SnmpStatusException x) {
            checkRowStatusFail(req, x.getStatus());
        }
        return EnumRowStatus.unspecified;
    }
    protected int mapRowStatus(SnmpOid rowOid, SnmpVarBind vbstatus,
                               Object userData)
        throws SnmpStatusException {
        final SnmpValue rsvalue = vbstatus.value;
        if (rsvalue instanceof SnmpInt)
            return ((SnmpInt)rsvalue).intValue();
        else
            throw new SnmpStatusException(
                       SnmpStatusException.snmpRspInconsistentValue);
    }
    protected SnmpValue setRowStatus(SnmpOid rowOid, int newStatus,
                                     Object userData)
        throws SnmpStatusException {
        return null;
    }
    protected boolean isRowReady(SnmpOid rowOid, Object userData)
        throws SnmpStatusException {
        return true;
    }
    protected void checkRowStatusChange(SnmpMibSubRequest req,
                                        SnmpOid rowOid, int depth,
                                        int newStatus)
        throws SnmpStatusException {
    }
    protected void checkRemoveTableRow(SnmpMibSubRequest req, SnmpOid rowOid,
                                       int depth)
        throws SnmpStatusException {
    }
    protected void removeTableRow(SnmpMibSubRequest req, SnmpOid rowOid,
                                  int depth)
        throws SnmpStatusException {
        removeEntry(rowOid);
    }
    protected synchronized void beginRowAction(SnmpMibSubRequest req,
                              SnmpOid rowOid, int depth, int rowAction)
        throws SnmpStatusException {
        final boolean     isnew  = req.isNewEntry();
        final SnmpOid     oid    = rowOid;
        final int         action = rowAction;
        switch (action) {
        case EnumRowStatus.unspecified:
            if (isnew) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "beginRowAction", "Failed to create row[" +
                            rowOid + "] : RowStatus = unspecified");
                }
                checkRowStatusFail(req,SnmpStatusException.snmpRspNoAccess);
            }
            break;
        case EnumRowStatus.createAndGo:
        case EnumRowStatus.createAndWait:
            if (isnew) {
                if (isCreationEnabled()) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "beginRowAction", "Creating row[" + rowOid +
                                "] : RowStatus = createAndGo | createAndWait");
                    }
                    createNewEntry(req,oid,depth);
                } else {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMibTable.class.getName(),
                                "beginRowAction", "Can't create row[" + rowOid +
                                "] : RowStatus = createAndGo | createAndWait " +
                                "but creation is disabled");
                    }
                    checkRowStatusFail(req,
                       SnmpStatusException.snmpRspNoAccess);
                }
            } else {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "beginRowAction", "Can't create row[" + rowOid +
                            "] : RowStatus = createAndGo | createAndWait " +
                            "but row already exists");
                }
                checkRowStatusFail(req,
                       SnmpStatusException.snmpRspInconsistentValue);
            }
            break;
        case EnumRowStatus.destroy:
            if (isnew) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "beginRowAction",
                            "Warning: can't destroy row[" + rowOid +
                            "] : RowStatus = destroy but row does not exist");
                }
            } else if (!isCreationEnabled()) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "beginRowAction",
                            "Can't destroy row[" + rowOid + "] : " +
                            "RowStatus = destroy but creation is disabled");
                }
                checkRowStatusFail(req,SnmpStatusException.snmpRspNoAccess);
            }
            checkRemoveTableRow(req,rowOid,depth);
            break;
        case EnumRowStatus.active:
        case EnumRowStatus.notInService:
            if (isnew) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "beginRowAction", "Can't switch state of row[" +
                            rowOid + "] : specified RowStatus = active | " +
                            "notInService but row does not exist");
                }
                checkRowStatusFail(req,
                        SnmpStatusException.snmpRspInconsistentValue);
            }
            checkRowStatusChange(req,rowOid,depth,action);
            break;
        case EnumRowStatus.notReady:
        default:
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                        SnmpMibTable.class.getName(),
                        "beginRowAction", "Invalid RowStatus value for row[" +
                        rowOid + "] : specified RowStatus = " + action);
            }
            checkRowStatusFail(req,
                    SnmpStatusException.snmpRspInconsistentValue);
        }
    }
    protected void endRowAction(SnmpMibSubRequest req, SnmpOid rowOid,
                               int depth, int rowAction)
        throws SnmpStatusException {
        final boolean     isnew  = req.isNewEntry();
        final SnmpOid     oid    = rowOid;
        final int         action = rowAction;
        final Object      data   = req.getUserData();
        SnmpValue         value  = null;
        switch (action) {
        case EnumRowStatus.unspecified:
            break;
        case EnumRowStatus.createAndGo:
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                        SnmpMibTable.class.getName(),
                        "endRowAction", "Setting RowStatus to 'active' " +
                        "for row[" + rowOid + "] : requested RowStatus = " +
                        "createAndGo");
            }
            value = setRowStatus(oid,EnumRowStatus.active,data);
            break;
        case EnumRowStatus.createAndWait:
            if (isRowReady(oid,data)) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "endRowAction",
                            "Setting RowStatus to 'notInService' for row[" +
                            rowOid + "] : requested RowStatus = createAndWait");
                }
                value = setRowStatus(oid,EnumRowStatus.notInService,data);
            } else {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "endRowAction", "Setting RowStatus to 'notReady' " +
                            "for row[" + rowOid + "] : requested RowStatus = " +
                            "createAndWait");
                }
                value = setRowStatus(oid,EnumRowStatus.notReady,data);
            }
            break;
        case EnumRowStatus.destroy:
            if (isnew) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "endRowAction",
                            "Warning: requested RowStatus = destroy, " +
                            "but row[" + rowOid + "] does not exist");
                }
            } else {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMibTable.class.getName(),
                            "endRowAction", "Destroying row[" + rowOid +
                            "] : requested RowStatus = destroy");
                }
            }
            removeTableRow(req,oid,depth);
            break;
        case EnumRowStatus.active:
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                        SnmpMibTable.class.getName(),
                        "endRowAction",
                        "Setting RowStatus to 'active' for row[" +
                        rowOid + "] : requested RowStatus = active");
            }
            value = setRowStatus(oid,EnumRowStatus.active,data);
            break;
        case EnumRowStatus.notInService:
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                        SnmpMibTable.class.getName(),
                        "endRowAction",
                        "Setting RowStatus to 'notInService' for row[" +
                        rowOid + "] : requested RowStatus = notInService");
            }
            value = setRowStatus(oid,EnumRowStatus.notInService,data);
            break;
        case EnumRowStatus.notReady:
        default:
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                        SnmpMibTable.class.getName(),
                        "endRowAction", "Invalid RowStatus value for row[" +
                        rowOid + "] : specified RowStatus = " + action);
            }
            setRowStatusFail(req,
                          SnmpStatusException.snmpRspInconsistentValue);
        }
        if (value != null) {
            final SnmpVarBind vb = req.getRowStatusVarBind();
            if (vb != null) vb.value = value;
        }
    }
    protected long getNextVarEntryId(SnmpOid rowOid,
                                     long var,
                                     Object userData,
                                     int pduVersion)
        throws SnmpStatusException {
        long varid=var;
        do {
            varid = getNextVarEntryId(rowOid,varid,userData);
        } while (skipEntryVariable(rowOid,varid,userData,pduVersion));
        return varid;
    }
    protected boolean skipEntryVariable(SnmpOid rowOid,
                                        long var,
                                        Object userData,
                                        int pduVersion) {
        return false;
    }
    protected SnmpOid getNextOid(SnmpOid oid, Object userData)
        throws SnmpStatusException {
        if (size == 0)
            throw noSuchInstanceException;
        final SnmpOid resOid = oid;
        SnmpOid last= tableoids[tablecount-1];
        if (last.equals(resOid)) {
            throw noSuchInstanceException;
        }
        final int newPos = getInsertionPoint(resOid,false);
        if (newPos > -1 && newPos < size) {
            try {
                last = tableoids[newPos];
            } catch(ArrayIndexOutOfBoundsException e) {
                throw noSuchInstanceException;
            }
        } else {
            throw noSuchInstanceException;
        }
        return last;
    }
    protected SnmpOid getNextOid(Object userData)
        throws SnmpStatusException {
        if (size == 0)
            throw noSuchInstanceException;
        return tableoids[0];
    }
    abstract protected long getNextVarEntryId(SnmpOid rowOid, long var,
                                              Object userData)
        throws SnmpStatusException;
    abstract protected void validateVarEntryId(SnmpOid rowOid, long var,
                                               Object userData)
        throws SnmpStatusException;
    abstract protected boolean isReadableEntryId(SnmpOid rowOid, long var,
                                                 Object userData)
        throws SnmpStatusException;
    abstract protected void get(SnmpMibSubRequest req,
                                SnmpOid rowOid, int depth)
        throws SnmpStatusException;
    abstract protected void check(SnmpMibSubRequest req,
                                  SnmpOid rowOid, int depth)
        throws SnmpStatusException;
    abstract protected void set(SnmpMibSubRequest req,
                                SnmpOid rowOid, int depth)
        throws SnmpStatusException;
    SnmpOid getNextOid(long[] oid, int pos, Object userData)
        throws SnmpStatusException {
        final SnmpOid resOid = new SnmpEntryOid(oid,pos);
        return getNextOid(resOid,userData);
    }
    final static void checkRowStatusFail(SnmpMibSubRequest req,
                                         int errorStatus)
        throws SnmpStatusException {
        final SnmpVarBind statusvb  = req.getRowStatusVarBind();
        final SnmpStatusException x = new SnmpStatusException(errorStatus);
        req.registerCheckException(statusvb,x);
    }
    final static void setRowStatusFail(SnmpMibSubRequest req,
                                       int errorStatus)
        throws SnmpStatusException {
        final SnmpVarBind statusvb  = req.getRowStatusVarBind();
        final SnmpStatusException x = new SnmpStatusException(errorStatus);
        req.registerSetException(statusvb,x);
    }
    final synchronized void findHandlingNode(SnmpVarBind varbind,
                                             long[] oid, int depth,
                                             SnmpRequestTree handlers)
        throws SnmpStatusException {
        final int  length = oid.length;
        if (handlers == null)
            throw new SnmpStatusException(SnmpStatusException.snmpRspGenErr);
        if (depth >= length)
            throw new SnmpStatusException(SnmpStatusException.noAccess);
        if (oid[depth] != nodeId)
            throw new SnmpStatusException(SnmpStatusException.noAccess);
        if (depth+2 >= length)
            throw new SnmpStatusException(SnmpStatusException.noAccess);
        final SnmpOid entryoid = new SnmpEntryOid(oid, depth+2);
        final Object data = handlers.getUserData();
        final boolean hasEntry = contains(entryoid, data);
        if (!hasEntry) {
            if (!handlers.isCreationAllowed())
                throw noSuchInstanceException;
            else if (!isCreationEnabled())
                throw new
                    SnmpStatusException(SnmpStatusException.snmpRspNoAccess);
        }
        final long   var  = oid[depth+1];
        if (hasEntry) {
            validateVarEntryId(entryoid,var,data);
        }
        if (handlers.isSetRequest() && isRowStatus(entryoid,var,data))
            handlers.add(this,depth,entryoid,varbind,(!hasEntry),varbind);
        else
            handlers.add(this,depth,entryoid,varbind,(!hasEntry));
    }
    final synchronized long[] findNextHandlingNode(SnmpVarBind varbind,
                                      long[] oid, int pos, int depth,
                                      SnmpRequestTree handlers,
                                      AcmChecker checker)
        throws SnmpStatusException {
            int length = oid.length;
            if (handlers == null)
            throw noSuchObjectException;
            final Object data = handlers.getUserData();
            final int pduVersion = handlers.getRequestPduVersion();
            long var= -1;
            if (pos >= length) {
                oid = new long[1];
                oid[0] = nodeId;
                pos = 0;
                length = 1;
            } else if (oid[pos] > nodeId) {
                throw noSuchObjectException;
            } else if (oid[pos] < nodeId) {
                oid = new long[1];
                oid[0] = nodeId;
                pos = 0;
                length = 0;
            } else if ((pos + 1) < length) {
                var = oid[pos+1];
            }
            SnmpOid entryoid = null ;
            if (pos == (length - 1)) {
                entryoid = getNextOid(data);
                var = getNextVarEntryId(entryoid,var,data,pduVersion);
            } else if ( pos == (length-2)) {
                entryoid = getNextOid(data);
                if (skipEntryVariable(entryoid,var,data,pduVersion)) {
                    var = getNextVarEntryId(entryoid,var,data,pduVersion);
                }
            } else {
                try {
                    entryoid = getNextOid(oid, pos + 2, data);
                    if (skipEntryVariable(entryoid,var,data,pduVersion))
                        throw noSuchObjectException;
                } catch(SnmpStatusException se) {
                    entryoid = getNextOid(data);
                    var = getNextVarEntryId(entryoid,var,data,pduVersion);
                }
            }
            return findNextAccessibleOid(entryoid,
                                         varbind,
                                         oid,
                                         depth,
                                         handlers,
                                         checker,
                                         data,
                                         var);
        }
    private long[] findNextAccessibleOid(SnmpOid entryoid,
                                         SnmpVarBind varbind,long[] oid,
                                         int depth, SnmpRequestTree handlers,
                                         AcmChecker checker, Object data,
                                         long var)
        throws SnmpStatusException {
        final int pduVersion = handlers.getRequestPduVersion();
        while(true) {
            if (entryoid == null || var == -1 ) throw noSuchObjectException;
            try {
                if (!isReadableEntryId(entryoid,var,data))
                    throw noSuchObjectException;
                final long[] etable  = entryoid.longValue(false);
                final int    elength = etable.length;
                final long[] result  = new long[depth + 2 + elength];
                result[0] = -1 ; 
                java.lang.System.arraycopy(etable, 0, result,
                                           depth+2, elength);
                result[depth] = nodeId;
                result[depth+1] = var;
                checker.add(depth,result,depth,elength+2);
                try {
                    checker.checkCurrentOid();
                    handlers.add(this,depth,entryoid,varbind,false);
                    return result;
                } catch(SnmpStatusException e) {
                    entryoid = getNextOid(entryoid, data);
                } finally {
                    checker.remove(depth,elength+2);
                }
            } catch(SnmpStatusException e) {
                entryoid = getNextOid(data);
                var = getNextVarEntryId(entryoid,var,data,pduVersion);
            }
            if (entryoid == null || var == -1 )
                throw noSuchObjectException;
        }
    }
    final void validateOid(long[] oid, int pos) throws SnmpStatusException {
        final int length= oid.length;
        if (pos +2 >= length)
            throw noSuchInstanceException;
        if (oid[pos] != nodeId)
            throw noSuchObjectException;
    }
    private synchronized void sendNotification(Notification notification) {
        for(java.util.Enumeration k = handbackTable.keys();
            k.hasMoreElements(); ) {
            NotificationListener listener =
                (NotificationListener) k.nextElement();
            java.util.Vector handbackList =
                (java.util.Vector) handbackTable.get(listener) ;
            java.util.Vector filterList =
                (java.util.Vector) filterTable.get(listener) ;
            java.util.Enumeration f = filterList.elements();
            for(java.util.Enumeration h = handbackList.elements();
                h.hasMoreElements(); ) {
                Object handback = h.nextElement();
                NotificationFilter filter =
                    (NotificationFilter)f.nextElement();
                if ((filter == null) ||
                     (filter.isNotificationEnabled(notification))) {
                    listener.handleNotification(notification,handback) ;
                }
            }
        }
    }
    private void sendNotification(String type, long timeStamp,
                                  Object entry, ObjectName name) {
        synchronized(this) {
            sequenceNumber = sequenceNumber + 1;
        }
        SnmpTableEntryNotification notif =
            new SnmpTableEntryNotification(type, this, sequenceNumber,
                                           timeStamp, entry, name);
        this.sendNotification(notif) ;
    }
    protected boolean contains(SnmpOid oid, Object userData) {
        return (findObject(oid) > -1);
    }
    private final int findObject(SnmpOid oid) {
        int low= 0;
        int max= size - 1;
        SnmpOid pos;
        int comp;
        int curr= low + (max-low)/2;
        while (low <= max) {
            pos = tableoids[curr];
            comp = oid.compareTo(pos);
            if (comp == 0)
                return curr;
            if (oid.equals(pos) == true) {
                return curr;
            }
            if (comp > 0) {
                low = curr + 1;
            } else {
                max = curr - 1;
            }
            curr = low + (max-low)/2;
        }
        return -1;
    }
    private final int getInsertionPoint(SnmpOid oid)
        throws SnmpStatusException {
        return getInsertionPoint(oid, true);
    }
    private final int getInsertionPoint(SnmpOid oid, boolean fail)
        throws SnmpStatusException {
        final int failStatus = SnmpStatusException.snmpRspNotWritable;
        int low= 0;
        int max= size - 1;
        SnmpOid pos;
        int comp;
        int curr= low + (max-low)/2;
        while (low <= max) {
            pos= tableoids[curr];
            comp= oid.compareTo(pos);
            if (comp == 0) {
                if (fail)
                    throw new SnmpStatusException(failStatus,curr);
                else
                    return curr+1;
            }
            if (comp>0) {
                low= curr +1;
            } else {
                max= curr -1;
            }
            curr= low + (max-low)/2;
        }
        return curr;
    }
    private final void removeOid(int pos) {
        if (pos >= tablecount) return;
        if (pos < 0) return;
        final int l1 = --tablecount-pos;
        tableoids[pos] = null;
        if (l1 > 0)
            java.lang.System.arraycopy(tableoids,pos+1,tableoids,pos,l1);
        tableoids[tablecount] = null;
    }
    private final void insertOid(int pos, SnmpOid oid) {
        if (pos >= tablesize || tablecount == tablesize) {
                final SnmpOid[] olde = tableoids;
                tablesize += Delta;
                tableoids = new SnmpOid[tablesize];
                if (pos > tablecount) pos = tablecount;
                if (pos < 0) pos = 0;
                final int l1 = pos;
                final int l2 = tablecount - pos;
                if (l1 > 0)
                    java.lang.System.arraycopy(olde,0,tableoids,0,l1);
                if (l2 > 0)
                    java.lang.System.arraycopy(olde,l1,tableoids,
                                               l1+1,l2);
            } else if (pos < tablecount) {
                java.lang.System.arraycopy(tableoids,pos,tableoids,
                                           pos+1,tablecount-pos);
            }
            tableoids[pos]  = oid;
            tablecount++;
    }
    protected int nodeId=1;
    protected SnmpMib theMib;
    protected boolean creationEnabled = false;
    protected SnmpTableEntryFactory factory = null;
    private int size=0;
    private final static int Delta = 16;
    private int     tablecount     = 0;
    private int     tablesize      = Delta;
    private SnmpOid tableoids[]    = new SnmpOid[tablesize];
    private final Vector<Object> entries= new Vector<Object>();
    private final Vector<ObjectName> entrynames= new Vector<ObjectName>();
    private Hashtable<NotificationListener, Vector<Object>> handbackTable =
            new Hashtable<NotificationListener, Vector<Object>>();
    private Hashtable<NotificationListener, Vector<NotificationFilter>>
            filterTable =
            new Hashtable<NotificationListener, Vector<NotificationFilter>>();
    transient long sequenceNumber = 0;
}
