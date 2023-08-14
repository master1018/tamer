final class SoftValueMap implements Map
{
    public SoftValueMap ()
    {
        this (1, 1);
    }
    public SoftValueMap (final int readClearCheckFrequency, final int writeClearCheckFrequency)
    {
        this (11, 0.75F, readClearCheckFrequency, writeClearCheckFrequency);
    }
    public SoftValueMap (int initialCapacity, final float loadFactor, final int readClearCheckFrequency, final int writeClearCheckFrequency)
    {
        if (initialCapacity < 0)
            throw new IllegalArgumentException ("negative input: initialCapacity [" + initialCapacity + "]");
        if ((loadFactor <= 0.0) || (loadFactor >= 1.0 + 1.0E-6))
            throw new IllegalArgumentException ("loadFactor not in (0.0, 1.0] range: " + loadFactor);
        if (readClearCheckFrequency < 1)
            throw new IllegalArgumentException ("readClearCheckFrequency not in [1, +inf) range: " + readClearCheckFrequency);
        if (writeClearCheckFrequency < 1)
            throw new IllegalArgumentException ("writeClearCheckFrequency not in [1, +inf) range: " + writeClearCheckFrequency);
        if (initialCapacity == 0) initialCapacity = 1;
        m_valueReferenceQueue = new ReferenceQueue ();
        m_loadFactor = loadFactor;
        m_sizeThreshold = (int) (initialCapacity * loadFactor);
        m_readClearCheckFrequency = readClearCheckFrequency;
        m_writeClearCheckFrequency = writeClearCheckFrequency;
        m_buckets = new SoftEntry [initialCapacity];
    }
    public boolean equals (final Object rhs)
    {
        throw new UnsupportedOperationException ("not implemented: equals");
    }
    public int hashCode ()
    {
        throw new UnsupportedOperationException ("not implemented: hashCode");
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
    public boolean isEmpty ()
    {
        return m_size == 0;
    }
    public Object get (final Object key)
    {
        if (key == null) throw new IllegalArgumentException ("null input: key");
        if ((++ m_readAccessCount % m_readClearCheckFrequency) == 0) removeClearedValues ();
        final int keyHashCode = key.hashCode ();
        final SoftEntry [] buckets = m_buckets;
        final int bucketIndex = (keyHashCode & 0x7FFFFFFF) % buckets.length;
        Object result = null; 
        for (SoftEntry entry = buckets [bucketIndex]; entry != null; entry = entry.m_next)
        {
            final Object entryKey = entry.m_key;
            if (IDENTITY_OPTIMIZATION)
            {
                if ((key == entryKey) || ((keyHashCode == entryKey.hashCode ()) && key.equals (entryKey)))
                {
                    final Reference ref = entry.m_softValue;
                    result = ref.get (); 
                    if (ENQUEUE_FOUND_CLEARED_ENTRIES && (result == null))
                    {
                        ref.enqueue ();
                    }
                    return result;
                }
            }
            else
            {
                if ((keyHashCode == entryKey.hashCode ()) && key.equals (entryKey))
                {
                    final Reference ref = entry.m_softValue;
                    result = ref.get (); 
                    if (ENQUEUE_FOUND_CLEARED_ENTRIES && (result == null))
                    {
                        ref.enqueue ();
                    }
                    return result;
                }
            }
        }
        return null;
    }
    public Object put (final Object key, final Object value)
    {
        if (key == null) throw new IllegalArgumentException ("null input: key");
        if (value == null) throw new IllegalArgumentException ("null input: value");
        if ((++ m_writeAccessCount % m_writeClearCheckFrequency) == 0) removeClearedValues ();
        SoftEntry currentKeyEntry = null;
        final int keyHashCode = key.hashCode ();
        SoftEntry [] buckets = m_buckets;
        int bucketIndex = (keyHashCode & 0x7FFFFFFF) % buckets.length;
        for (SoftEntry entry = buckets [bucketIndex]; entry != null; entry = entry.m_next)
        {
            final Object entryKey = entry.m_key;
            if (IDENTITY_OPTIMIZATION)
            {
                if ((key == entryKey) || ((keyHashCode == entryKey.hashCode ()) && key.equals (entryKey)))
                {
                    currentKeyEntry = entry;
                    break;
                }
            }
            else
            {
                if ((keyHashCode == entryKey.hashCode ()) && key.equals (entryKey))
                {
                    currentKeyEntry = entry;
                    break;
                }
            }
        }
        if (currentKeyEntry != null)
        {
            final IndexedSoftReference ref = currentKeyEntry.m_softValue;
            final Object currentKeyValue = ref.get (); 
            if (currentKeyValue == null) ref.m_bucketIndex = -1; 
            currentKeyEntry.m_softValue = new IndexedSoftReference (value, m_valueReferenceQueue, bucketIndex);
            return currentKeyValue; 
        }
        else
        {
            if (m_size >= m_sizeThreshold) rehash ();
            buckets = m_buckets;
            bucketIndex = (keyHashCode & 0x7FFFFFFF) % buckets.length;
            final SoftEntry bucketListHead = buckets [bucketIndex];
            final SoftEntry newEntry = new SoftEntry (m_valueReferenceQueue, key, value, bucketListHead, bucketIndex);
            buckets [bucketIndex] = newEntry;
            ++ m_size;
            return null;
        }
    }
    public Object remove (final Object key)
    {
        if (key == null) throw new IllegalArgumentException ("null input: key");
        if ((++ m_writeAccessCount % m_writeClearCheckFrequency) == 0) removeClearedValues ();
        final int keyHashCode = key.hashCode ();
        final SoftEntry [] buckets = m_buckets;
        final int bucketIndex = (keyHashCode & 0x7FFFFFFF) % buckets.length;
        Object result = null;
        for (SoftEntry entry = buckets [bucketIndex], prev = null; entry != null; prev = entry, entry = entry.m_next)
        {
            final Object entryKey = entry.m_key;
            if ((IDENTITY_OPTIMIZATION && (entryKey == key)) || ((keyHashCode == entryKey.hashCode ()) && key.equals (entryKey)))
            {
                if (prev == null) 
                {
                    buckets [bucketIndex] = entry.m_next;
                }
                else
                {
                    prev.m_next = entry.m_next;
                }
                final IndexedSoftReference ref = entry.m_softValue; 
                result = ref.get (); 
                ref.m_bucketIndex = -1;
                entry.m_softValue = null;
                entry.m_key = null;
                entry.m_next = null;
                entry = null;
                -- m_size;
                break;
            }
        }
        return result;
    }
    public void clear ()
    {
        final SoftEntry [] buckets = m_buckets;
        for (int b = 0, bLimit = buckets.length; b < bLimit; ++ b)
        {
            for (SoftEntry entry = buckets [b]; entry != null; )
            {
                final SoftEntry next = entry.m_next; 
                entry.m_softValue.m_bucketIndex = -1;
                entry.m_softValue = null;
                entry.m_next = null;
                entry.m_key = null;
                entry = next;
            }
            buckets [b] = null;
        }
        m_size = 0;
        m_readAccessCount = 0;
        m_writeAccessCount = 0;
    }
    public boolean containsKey (final Object key)
    {
        throw new UnsupportedOperationException ("not implemented: containsKey");
    }
    public boolean containsValue (final Object value)
    {
        throw new UnsupportedOperationException ("not implemented: containsValue");
    }
    public void putAll (final Map map)
    {
        throw new UnsupportedOperationException ("not implemented: putAll");
    }
    public Set keySet ()
    {
        throw new UnsupportedOperationException ("not implemented: keySet");
    }
    public Set entrySet ()
    {
        throw new UnsupportedOperationException ("not implemented: entrySet");
    }
    public Collection values ()
    {
        throw new UnsupportedOperationException ("not implemented: values");
    }
    void debugDump (final StringBuffer out)
    {
        if (out != null)
        {
            out.append (getClass ().getName ().concat ("@").concat (Integer.toHexString (System.identityHashCode (this)))); out.append (EOL);
            out.append ("size = " + m_size + ", bucket table size = " + m_buckets.length + ", load factor = " + m_loadFactor + EOL);
            out.append ("size threshold = " + m_sizeThreshold + ", get clear frequency = " + m_readClearCheckFrequency + ", put clear frequency = " + m_writeClearCheckFrequency + EOL);
            out.append ("get count: " + m_readAccessCount + ", put count: " + m_writeAccessCount + EOL);
        }
    }
    static class IndexedSoftReference extends SoftReference
    {
        IndexedSoftReference (final Object referent, ReferenceQueue queue, final int bucketIndex)
        {
            super (referent, queue);
            m_bucketIndex = bucketIndex;
        }
        int m_bucketIndex;
    } 
    static class SoftEntry
    {
        SoftEntry (final ReferenceQueue valueReferenceQueue, final Object key, Object value, final SoftEntry next, final int bucketIndex)
        {
            m_key = key;
            m_softValue = new IndexedSoftReference (value, valueReferenceQueue, bucketIndex); 
            value = null;
            m_next = next;
        }
        IndexedSoftReference m_softValue; 
        Object m_key;  
        SoftEntry m_next; 
    } 
    private void rehash ()
    {
        final SoftEntry [] buckets = m_buckets;
        final int newBucketCount = (m_buckets.length << 1) + 1;
        final SoftEntry [] newBuckets = new SoftEntry [newBucketCount];
        int newSize = 0;
        for (int b = 0, bLimit = buckets.length; b < bLimit; ++ b)
        {
            for (SoftEntry entry = buckets [b]; entry != null; )
            {
                final SoftEntry next = entry.m_next; 
                IndexedSoftReference ref = entry.m_softValue; 
                Object entryValue = ref.get (); 
                if (entryValue != null)
                {
                    final int entryKeyHashCode = entry.m_key.hashCode ();
                    final int newBucketIndex = (entryKeyHashCode & 0x7FFFFFFF) % newBucketCount;
                    final SoftEntry bucketListHead = newBuckets [newBucketIndex];
                    entry.m_next = bucketListHead;
                    newBuckets [newBucketIndex] = entry;
                    ref.m_bucketIndex = newBucketIndex;
                    ++ newSize;
                    entryValue = null;
                }
                else
                {
                    ref.m_bucketIndex = -1;
                }
                entry = next;
            }
        }
        if (DEBUG)
        {
            if (m_size > newSize) System.out.println ("DEBUG: rehash() cleared " + (m_size - newSize) + " values, new size = " + newSize);
        }
        m_size = newSize;
        m_sizeThreshold = (int) (newBucketCount * m_loadFactor);
        m_buckets = newBuckets;
    }
    private void removeClearedValues ()
    {
        int count = 0;
next:   for (Reference _ref; (_ref = m_valueReferenceQueue.poll ()) != null; )
        {
            final int bucketIndex = ((IndexedSoftReference) _ref).m_bucketIndex;
            if (bucketIndex >= 0) 
            {
                for (SoftEntry entry = m_buckets [bucketIndex], prev = null; entry != null; prev = entry, entry = entry.m_next)
                {
                    if (entry.m_softValue == _ref)
                    {
                        if (prev == null) 
                        {
                            m_buckets [bucketIndex] = entry.m_next;
                        }
                        else
                        {
                            prev.m_next = entry.m_next;
                        }
                        entry.m_softValue = null;
                        entry.m_key = null;
                        entry.m_next = null;
                        entry = null;
                        -- m_size;
                        if (DEBUG) ++ count;
                        continue next;
                    }
                }
                final StringBuffer msg = new StringBuffer ("removeClearedValues(): soft reference [" + _ref + "] did not match within bucket #" + bucketIndex + EOL);
                debugDump (msg);
                throw new Error (msg.toString ());
            }
        }
        if (DEBUG)
        {
            if (count > 0) System.out.println ("DEBUG: removeClearedValues() cleared " + count + " keys, new size = " + m_size);
        }
    }
    private final ReferenceQueue m_valueReferenceQueue; 
    private final float m_loadFactor; 
    private final int m_readClearCheckFrequency, m_writeClearCheckFrequency; 
    private SoftEntry [] m_buckets; 
    private int m_size; 
    private int m_sizeThreshold; 
    private int m_readAccessCount, m_writeAccessCount;
    private static final String EOL = System.getProperty ("line.separator", "\n");
    private static final boolean IDENTITY_OPTIMIZATION          = true;
    private static final boolean ENQUEUE_FOUND_CLEARED_ENTRIES  = true; 
    private static final boolean DEBUG = false;
} 
