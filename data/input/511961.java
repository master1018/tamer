final class IntObjectMap implements Serializable
{
    public IntObjectMap ()
    {
        this (11, 0.75F);
    }
    public IntObjectMap (final int initialCapacity)
    {
        this (initialCapacity, 0.75F);
    }
    public IntObjectMap (int initialCapacity, final float loadFactor)
    {
        if (initialCapacity < 0) throw new IllegalArgumentException ("negative input: initialCapacity [" + initialCapacity + "]");
        if ((loadFactor <= 0.0) || (loadFactor >= 1.0 + 1.0E-6))
            throw new IllegalArgumentException ("loadFactor not in (0.0, 1.0] range: " + loadFactor);
        if (initialCapacity == 0) initialCapacity = 1;
        m_loadFactor = loadFactor > 1.0 ? 1.0F : loadFactor;        
        m_sizeThreshold = (int) (initialCapacity * loadFactor);
        m_buckets = new Entry [initialCapacity];
    }
    public String toString ()
    {
        final StringBuffer s = new StringBuffer ();
        debugDump (s);
        return s.toString ();
    }
    public int size ()
    {
        return m_size;
    }
    public boolean contains (final int key)
    {
        final Entry [] buckets = m_buckets;
        final int bucketIndex = (key & 0x7FFFFFFF) % buckets.length;
        for (Entry entry = buckets [bucketIndex]; entry != null; entry = entry.m_next)
        {
            if (key == entry.m_key)
                return true;
        }
        return false;
    }
    public Object get (final int key)
    {
        final Entry [] buckets = m_buckets;
        final int bucketIndex = (key & 0x7FFFFFFF) % buckets.length;
        for (Entry entry = buckets [bucketIndex]; entry != null; entry = entry.m_next)
        {
            if (key == entry.m_key)
                return entry.m_value;
        }
        return null;
    }
    public int [] keys ()
    {
        if (m_size == 0)
            return IConstants.EMPTY_INT_ARRAY;
        else
        {
            final int [] result = new int [m_size];
            int scan = 0;
            for (int b = 0; b < m_buckets.length; ++ b)
            {
                for (Entry entry = m_buckets [b]; entry != null; entry = entry.m_next)
                {
                    result [scan ++] = entry.m_key;
                }
            }
            return result;
        }
    }
    public Object put (final int key, final Object value)
    {
        Entry currentKeyEntry = null;
        int bucketIndex = (key & 0x7FFFFFFF) % m_buckets.length;
        Entry [] buckets = m_buckets;
        for (Entry entry = buckets [bucketIndex]; entry != null; entry = entry.m_next)
        {
            if (key == entry.m_key)
            {
                currentKeyEntry = entry;
                break;
            }
        }
        if (currentKeyEntry != null)
        {
            final Object currentKeyValue = currentKeyEntry.m_value;
            currentKeyEntry.m_value = value;
            return currentKeyValue;
        }
        else
        {
            if (m_size >= m_sizeThreshold) rehash ();
            buckets = m_buckets;
            bucketIndex = (key & 0x7FFFFFFF) % buckets.length;
            final Entry bucketListHead = buckets [bucketIndex];
            final Entry newEntry = new Entry (key, value, bucketListHead);
            buckets [bucketIndex] = newEntry;
            ++ m_size;
            return null;
        }
    }
    void debugDump (final StringBuffer out)
    {
        if (out != null)
        {
            out.append (super.toString ()); out.append (EOL);
            out.append ("size = " + m_size + ", bucket table size = " + m_buckets.length + ", load factor = " + m_loadFactor + EOL);
            out.append ("size threshold = " + m_sizeThreshold + EOL);
        }
    }
    private static final class Entry implements Serializable
    {
        Entry (final int key, final Object value, final Entry next)
        {
            m_key = key; 
            m_value = value;
            m_next = next;
        }
        Object m_value;           
        final int m_key;
        Entry m_next; 
    } 
    private void rehash ()
    {
        final Entry [] buckets = m_buckets;
        final int newBucketCount = (m_buckets.length << 1) + 1;
        final Entry [] newBuckets = new Entry [newBucketCount];
        for (int b = 0; b < buckets.length; ++ b)
        {
            for (Entry entry = buckets [b]; entry != null; )
            {
                final Entry next = entry.m_next; 
                final int entryKey = entry.m_key;
                final int newBucketIndex = (entryKey & 0x7FFFFFFF) % newBucketCount;
                final Entry bucketListHead = newBuckets [newBucketIndex];
                entry.m_next = bucketListHead;
                newBuckets [newBucketIndex] = entry;                                
                entry = next;
            }
        }
        m_sizeThreshold = (int) (newBucketCount * m_loadFactor);
        m_buckets = newBuckets;
    }
    private final float m_loadFactor; 
    private Entry [] m_buckets; 
    private int m_size; 
    private int m_sizeThreshold; 
    private static final String EOL = System.getProperty ("line.separator", "\n");
} 
