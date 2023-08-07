public class GroupByValuesQueryResultAction implements IMatchingObjectAction {
    private IValuesQuery query;
    private long nbObjects;
    private Map<OdbComparable, ValuesQueryResultAction> groupByResult;
    private Values result;
    private boolean queryHasOrderBy;
    protected InstanceBuilder instanceBuilder;
    protected ClassInfo classInfo;
    private int returnArraySize;
    private String[] groupByFieldList;
    public GroupByValuesQueryResultAction(IValuesQuery query, SessionEngine storageEngine, InstanceBuilder instanceBuilder) {
        super();
        this.query = query;
        this.queryHasOrderBy = query.hasOrderBy();
        this.instanceBuilder = instanceBuilder;
        this.returnArraySize = query.getObjectActions().size();
        this.groupByFieldList = query.getGroupByFieldList();
        this.groupByResult = new OdbHashMap<OdbComparable, ValuesQueryResultAction>();
    }
    public void add(MatchResult matchResult, OdbComparable orderByKey) {
        ValuesMatchResult valuesMatchResult = (ValuesMatchResult) matchResult;
        AttributeValuesMap attributeValuesMap = valuesMatchResult.valuesMap;
        OdbComparable groupByKey = IndexTool.buildIndexKey("GroupBy", attributeValuesMap, groupByFieldList);
        ValuesQueryResultAction result = groupByResult.get(groupByKey);
        if (result == null) {
            result = new ValuesQueryResultAction(query, null, instanceBuilder);
            result.start();
            groupByResult.put(groupByKey, result);
        }
        result.add(matchResult, orderByKey);
    }
    public void start() {
    }
    public void end() {
        if (query != null && query.hasOrderBy()) {
            result = new InMemoryBTreeCollectionForValues((int) nbObjects, query.getOrderByType());
        } else {
            result = new SimpleListForValues((int) nbObjects);
        }
        Iterator iterator = groupByResult.keySet().iterator();
        ValuesQueryResultAction vqra = null;
        OdbComparable key = null;
        while (iterator.hasNext()) {
            key = (OdbComparable) iterator.next();
            vqra = (ValuesQueryResultAction) groupByResult.get(key);
            vqra.end();
            merge(key, vqra.getValues());
        }
    }
    private void merge(OdbComparable key, Values values) {
        while (values.hasNext()) {
            if (queryHasOrderBy) {
                result.addWithKey(key, values.nextValues());
            } else {
                result.add(values.nextValues());
            }
        }
    }
    public <T> Objects<T> getObjects() {
        return (Objects<T>) result;
    }
    public void addNnoi(OID oid, NonNativeObjectInfo object, OdbComparable orderByKey) {
        throw new ODBRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);
    }
    public void addObject(OID oid, Object object, OdbComparable orderByKey) {
        throw new ODBRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);
    }
}
