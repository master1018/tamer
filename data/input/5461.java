public abstract class SnmpTableSupport implements SnmpTableEntryFactory,
    SnmpTableCallbackHandler, Serializable {
    protected List<Object> entries;
    protected SnmpMibTable meta;
    protected SnmpMib      theMib;
    private boolean registrationRequired = false;
    protected SnmpTableSupport(SnmpMib mib) {
        theMib  = mib;
        meta    = getRegisteredTableMeta(mib);
        bindWithTableMeta();
        entries = allocateTable();
    }
    public abstract void createNewEntry(SnmpMibSubRequest request,
                                        SnmpOid rowOid, int depth,
                                        SnmpMibTable meta)
        throws SnmpStatusException;
    public Object getEntry(int pos) {
        if (entries == null) return null;
        return entries.get(pos);
    }
    public int getSize() {
        return meta.getSize();
    }
    public void setCreationEnabled(boolean remoteCreationFlag) {
        meta.setCreationEnabled(remoteCreationFlag);
    }
    public boolean isCreationEnabled() {
        return meta.isCreationEnabled();
    }
    public boolean isRegistrationRequired() {
        return registrationRequired;
    }
    public SnmpIndex buildSnmpIndex(SnmpOid rowOid)
        throws SnmpStatusException {
        return buildSnmpIndex(rowOid.longValue(false), 0);
    }
    public abstract SnmpOid buildOidFromIndex(SnmpIndex index)
        throws SnmpStatusException;
    public abstract ObjectName buildNameFromIndex(SnmpIndex index)
        throws SnmpStatusException;
    public void addEntryCb(int pos, SnmpOid row, ObjectName name,
                           Object entry, SnmpMibTable meta)
        throws SnmpStatusException {
        try {
            if (entries != null) entries.add(pos,entry);
        } catch (Exception e) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        }
    }
    public void removeEntryCb(int pos, SnmpOid row, ObjectName name,
                              Object entry, SnmpMibTable meta)
        throws SnmpStatusException {
        try {
            if (entries != null) entries.remove(pos);
        } catch (Exception e) {
        }
    }
    public void
        addNotificationListener(NotificationListener listener,
                                NotificationFilter filter, Object handback) {
        meta.addNotificationListener(listener,filter,handback);
    }
    public synchronized void
        removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {
        meta.removeNotificationListener(listener);
    }
    public MBeanNotificationInfo[] getNotificationInfo() {
        return meta.getNotificationInfo();
    }
    protected abstract SnmpIndex buildSnmpIndex(long oid[], int start )
        throws SnmpStatusException;
    protected abstract SnmpMibTable getRegisteredTableMeta(SnmpMib mib);
    protected List<Object> allocateTable() {
        return new ArrayList<Object>();
    }
    protected void addEntry(SnmpIndex index, Object entry)
        throws SnmpStatusException {
        SnmpOid oid = buildOidFromIndex(index);
        ObjectName name = null;
        if (isRegistrationRequired()) {
            name = buildNameFromIndex(index);
        }
        meta.addEntry(oid,name,entry);
    }
    protected void addEntry(SnmpIndex index, ObjectName name, Object entry)
        throws SnmpStatusException {
        SnmpOid oid = buildOidFromIndex(index);
        meta.addEntry(oid,name,entry);
    }
    protected void removeEntry(SnmpIndex index, Object entry)
        throws SnmpStatusException {
        SnmpOid oid = buildOidFromIndex(index);
        meta.removeEntry(oid,entry);
    }
    protected Object[] getBasicEntries() {
        if (entries == null) return null;
        Object[] array= new Object[entries.size()];
        entries.toArray(array);
        return array;
    }
    protected void bindWithTableMeta() {
        if (meta == null) return;
        registrationRequired = meta.isRegistrationRequired();
        meta.registerEntryFactory(this);
    }
}
