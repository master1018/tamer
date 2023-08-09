final class ObjectIntMap
{
    public ObjectIntMap ()
    {
        this (11, 0.75F);
    }
    public ObjectIntMap (final int initialCapacity)
    {
        this (initialCapacity, 0.75F);
    }
    public ObjectIntMap (int initialCapacity, final float loadFactor)
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
    public boolean contains (final Object key)
    {
        if ($assert.ENABLED) $assert.ASSERT (key != null, "null input: key");
        final Entry [] buckets = m_buckets;
        final int keyHash = key.hashCode ();
        final int bucketIndex = (keyHash & 0x7FFFFFFF) % buckets.length;
        for (Entry entry = buckets [bucketIndex]; entry != null; entry = entry.m_next)
        {
            if ((keyHash == entry.m_key.hashCode ()) || entry.m_key.equals (key))
                return true;
        }
        return false;
    }
    public boolean get (final Object key, final int [] out)
    {
        if ($assert.ENABLED) $assert.ASSERT (key != null, "null input: key");
        final Entry [] buckets = m_buckets;
        final int keyHash = key.hashCode ();
        final int bucketIndex = (keyHash & 0x7FFFFFFF) % buckets.length;
        for (Entry entry = buckets [bucketIndex]; entry != null; entry = entry.m_next)
        {
            if ((keyHash == entry.m_key.hashCode ()) || entry.m_key.equals (key))
            {
                out [0] = entry.m_value;
                return true;
            }
        }
        return false;
    }
    public Object [] keys ()
    {
        final Object [] result = new Object [m_size];
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
    public void put (final Object key, final int value)
    {
        if ($assert.ENABLED) $assert.ASSERT (key != null, "null input: key");
        Entry currentKeyEntry = null;
        final int keyHash = key.hashCode ();
        int bucketIndex = (keyHash & 0x7FFFFFFF) % m_buckets.length;
        Entry [] buckets = m_buckets;
        for (Entry entry = buckets [bucketIndex]; entry != null; entry = entry.m_next)
        {
            if ((keyHash == entry.m_key.hashCode ()) || entry.m_key.equals (key))
            {
                currentKeyEntry = entry;
                break;
            }
        }
        if (currentKeyEntry != null)
        {
            currentKeyEntry.m_value = value;
        }
        else
        {
            if (m_size >= m_sizeThreshold) rehash ();
            buckets = m_buckets;
            bucketIndex = (keyHash & 0x7FFFFFFF) % buckets.length;
            final Entry bucketListHead = buckets [bucketIndex];
            final Entry newEntry = new Entry (key, value, bucketListHead);
            buckets [bucketIndex] = newEntry;
            ++ m_size;
        }
    }
    public void remove (final Object key)
    {
        if ($assert.ENABLED) $assert.ASSERT (key != null, "null input: key");
        final int keyHash = key.hashCode ();
        final int bucketIndex = (keyHash  & 0x7FFFFFFF) % m_buckets.length;
        Entry [] buckets = m_buckets;
        for (Entry entry = buckets [bucketIndex], prev = entry; entry != null; )
        {
            final Entry next = entry.m_next;
            if ((keyHash == entry.m_key.hashCode ()) || entry.m_key.equals (key))
            {
                if (prev == entry)
                    buckets [bucketIndex] = next;
                else
                    prev.m_next = next;
                -- m_size;     
                break;
            }
            prev = entry;
            entry = next;
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
    private static final class Entry
    {
        Entry (final Object key, final int value, final Entry next)
        {
            m_key = key; 
            m_value = value;
            m_next = next;
        }
        Object m_key;     
        int m_value;
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
                final int entryKeyHash = entry.m_key.hashCode () & 0x7FFFFFFF;
                final int newBucketIndex = entryKeyHash % newBucketCount;
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
