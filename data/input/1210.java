public class InsertBatchQuery extends BatchQuery {
    protected List objectIds;
    protected List objectSnapshots;
    protected List dbAttributes;
    public InsertBatchQuery(DbEntity entity, int batchCapacity) {
        super(entity);
        this.objectSnapshots = new ArrayList(batchCapacity);
        this.objectIds = new ArrayList(batchCapacity);
        this.dbAttributes = new ArrayList(getDbEntity().getAttributes());
    }
    public Object getValue(int dbAttributeIndex) {
        DbAttribute attribute = (DbAttribute) dbAttributes.get(dbAttributeIndex);
        Map currentSnapshot = (Map) objectSnapshots.get(batchIndex);
        return getValue(currentSnapshot, attribute);
    }
    public void add(Map snapshot) {
        add(snapshot, null);
    }
    public void add(Map snapshot, ObjectId id) {
        objectSnapshots.add(snapshot);
        objectIds.add(id);
    }
    public int size() {
        return objectSnapshots.size();
    }
    public List getDbAttributes() {
        return dbAttributes;
    }
    public ObjectId getObjectId() {
        return (ObjectId) objectIds.get(batchIndex);
    }
}
