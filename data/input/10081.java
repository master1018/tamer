class RepositoryIdPool extends Stack {
    private static int MAX_CACHE_SIZE = 4;
    private RepositoryIdCache cache;
    public final synchronized RepositoryId popId() {
        try {
            return (RepositoryId)super.pop();
        }
        catch(EmptyStackException e) {
            increasePool(5);
            return (RepositoryId)super.pop();
        }
    }
    final void increasePool(int size) {
        for (int i = size; i > 0; i--)
            push(new RepositoryId());
    }
    final void setCaches(RepositoryIdCache cache) {
        this.cache = cache;
    }
}
public class RepositoryIdCache extends Hashtable {
    private RepositoryIdPool pool = new RepositoryIdPool();
    public RepositoryIdCache() {
        pool.setCaches(this);
    }
    public final synchronized RepositoryId getId(String key) {
        RepositoryId repId = (RepositoryId)super.get(key);
        if (repId != null)
            return repId;
        else {
            repId = new RepositoryId(key);
            put(key, repId);
            return repId;
        }
    }
}
