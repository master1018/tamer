class RepositoryIdPool_1_3_1 extends Stack {
    private static int MAX_CACHE_SIZE = 4;
    private RepositoryIdCache_1_3_1 cache;
    public final synchronized RepositoryId_1_3_1 popId() {
        try {
            return (RepositoryId_1_3_1)super.pop();
        }
        catch(EmptyStackException e) {
            increasePool(5);
            return (RepositoryId_1_3_1)super.pop();
        }
    }
    final void increasePool(int size) {
        for (int i = size; i > 0; i--)
            push(new RepositoryId_1_3_1());
    }
    final void setCaches(RepositoryIdCache_1_3_1 cache) {
        this.cache = cache;
    }
}
public class RepositoryIdCache_1_3_1 extends Hashtable {
    private RepositoryIdPool_1_3_1 pool = new RepositoryIdPool_1_3_1();
    public RepositoryIdCache_1_3_1() {
        pool.setCaches(this);
    }
    public final synchronized RepositoryId_1_3_1 getId(String key) {
        RepositoryId_1_3_1 repId = (RepositoryId_1_3_1)super.get(key);
        if (repId != null)
            return repId;
        else {
            repId = new RepositoryId_1_3_1(key);
            put(key, repId);
            return repId;
        }
    }
}
