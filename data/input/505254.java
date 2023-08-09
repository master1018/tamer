public class CommonNicknameCache  {
    private static final int NICKNAME_BLOOM_FILTER_SIZE = 0x1FFF;   
    private BitSet mNicknameBloomFilter;
    private HashMap<String, SoftReference<String[]>> mNicknameClusterCache = Maps.newHashMap();
    private final SQLiteDatabase mDb;
    public CommonNicknameCache(SQLiteDatabase db) {
        mDb = db;
    }
    private final static class NicknameLookupPreloadQuery {
        public final static String TABLE = Tables.NICKNAME_LOOKUP;
        public final static String[] COLUMNS = new String[] {
            NicknameLookupColumns.NAME
        };
        public final static int NAME = 0;
    }
    private void preloadNicknameBloomFilter() {
        mNicknameBloomFilter = new BitSet(NICKNAME_BLOOM_FILTER_SIZE + 1);
        Cursor cursor = mDb.query(NicknameLookupPreloadQuery.TABLE,
                NicknameLookupPreloadQuery.COLUMNS,
                null, null, null, null, null);
        try {
            int count = cursor.getCount();
            for (int i = 0; i < count; i++) {
                cursor.moveToNext();
                String normalizedName = cursor.getString(NicknameLookupPreloadQuery.NAME);
                int hashCode = normalizedName.hashCode();
                mNicknameBloomFilter.set(hashCode & NICKNAME_BLOOM_FILTER_SIZE);
            }
        } finally {
            cursor.close();
        }
    }
    protected String[] getCommonNicknameClusters(String normalizedName) {
        if (mNicknameBloomFilter == null) {
            preloadNicknameBloomFilter();
        }
        int hashCode = normalizedName.hashCode();
        if (!mNicknameBloomFilter.get(hashCode & NICKNAME_BLOOM_FILTER_SIZE)) {
            return null;
        }
        SoftReference<String[]> ref;
        String[] clusters = null;
        synchronized (mNicknameClusterCache) {
            if (mNicknameClusterCache.containsKey(normalizedName)) {
                ref = mNicknameClusterCache.get(normalizedName);
                if (ref == null) {
                    return null;
                }
                clusters = ref.get();
            }
        }
        if (clusters == null) {
            clusters = loadNicknameClusters(normalizedName);
            ref = clusters == null ? null : new SoftReference<String[]>(clusters);
            synchronized (mNicknameClusterCache) {
                mNicknameClusterCache.put(normalizedName, ref);
            }
        }
        return clusters;
    }
    private interface NicknameLookupQuery {
        String TABLE = Tables.NICKNAME_LOOKUP;
        String[] COLUMNS = new String[] {
            NicknameLookupColumns.CLUSTER
        };
        int CLUSTER = 0;
    }
    protected String[] loadNicknameClusters(String normalizedName) {
        String[] clusters = null;
        Cursor cursor = mDb.query(NicknameLookupQuery.TABLE, NicknameLookupQuery.COLUMNS,
                NicknameLookupColumns.NAME + "=?", new String[] { normalizedName },
                null, null, null);
        try {
            int count = cursor.getCount();
            if (count > 0) {
                clusters = new String[count];
                for (int i = 0; i < count; i++) {
                    cursor.moveToNext();
                    clusters[i] = cursor.getString(NicknameLookupQuery.CLUSTER);
                }
            }
        } finally {
            cursor.close();
        }
        return clusters;
    }
}
