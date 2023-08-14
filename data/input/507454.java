public final class WeakHashtable extends Hashtable {
    private static final int MAX_CHANGES_BEFORE_PURGE = 100;
    private static final int PARTIAL_PURGE_COUNT     = 10;
    private ReferenceQueue queue = new ReferenceQueue();
    private int changeCount = 0;
    public WeakHashtable() {}
    public boolean containsKey(Object key) {
        Referenced referenced = new Referenced(key);
        return super.containsKey(referenced);
    }
    public Enumeration elements() {
        purge();
        return super.elements();
    }
    public Set entrySet() {
        purge();
        Set referencedEntries = super.entrySet();
        Set unreferencedEntries = new HashSet();
        for (Iterator it=referencedEntries.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Referenced referencedKey = (Referenced) entry.getKey();
            Object key = referencedKey.getValue();
            Object value = entry.getValue();
            if (key != null) {
                Entry dereferencedEntry = new Entry(key, value);
                unreferencedEntries.add(dereferencedEntry);
            }
        }
        return unreferencedEntries;
    }
    public Object get(Object key) {
        Referenced referenceKey = new Referenced(key);
        return super.get(referenceKey);
    }
    public Enumeration keys() {
        purge();
        final Enumeration enumer = super.keys();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return enumer.hasMoreElements();
            }
            public Object nextElement() {
                 Referenced nextReference = (Referenced) enumer.nextElement();
                 return nextReference.getValue();
            }
        };
    }
    public Set keySet() {
        purge();
        Set referencedKeys = super.keySet();
        Set unreferencedKeys = new HashSet();
        for (Iterator it=referencedKeys.iterator(); it.hasNext();) {
            Referenced referenceKey = (Referenced) it.next();
            Object keyValue = referenceKey.getValue();
            if (keyValue != null) {
                unreferencedKeys.add(keyValue);
            }
        }
        return unreferencedKeys;
    }
    public Object put(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        }
        if (value == null) {
            throw new NullPointerException("Null values are not allowed");
        }
        if (changeCount++ > MAX_CHANGES_BEFORE_PURGE) {
            purge();
            changeCount = 0;
        }
        else if ((changeCount % PARTIAL_PURGE_COUNT) == 0) {
            purgeOne();
        }
        Object result = null;
        Referenced keyRef = new Referenced(key, queue);
        return super.put(keyRef, value);
    }
    public void putAll(Map t) {
        if (t != null) {
            Set entrySet = t.entrySet();
            for (Iterator it=entrySet.iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                put(entry.getKey(), entry.getValue());
            }
        }
    }
    public Collection values() {
        purge();
        return super.values();
    }
    public Object remove(Object key) {
        if (changeCount++ > MAX_CHANGES_BEFORE_PURGE) {
            purge();
            changeCount = 0;
        }
        else if ((changeCount % PARTIAL_PURGE_COUNT) == 0) {
            purgeOne();
        }
        return super.remove(new Referenced(key));
    }
    public boolean isEmpty() {
        purge();
        return super.isEmpty();
    }
    public int size() {
        purge();
        return super.size();
    }
    public String toString() {
        purge();
        return super.toString();
    }
    protected void rehash() {
        purge();
        super.rehash();
    }
    private void purge() {
        synchronized (queue) {
            WeakKey key;
            while ((key = (WeakKey) queue.poll()) != null) {
                super.remove(key.getReferenced());
            }
        }
    }
    private void purgeOne() {
        synchronized (queue) {
            WeakKey key = (WeakKey) queue.poll();
            if (key != null) {
                super.remove(key.getReferenced());
            }
        }
    }
    private final static class Entry implements Map.Entry {
        private final Object key;
        private final Object value;
        private Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }
        public boolean equals(Object o) {
            boolean result = false;
            if (o != null && o instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) o;
                result =    (getKey()==null ?
                                            entry.getKey() == null : 
                                            getKey().equals(entry.getKey()))
                            &&
                            (getValue()==null ?
                                            entry.getValue() == null : 
                                            getValue().equals(entry.getValue()));
            }
            return result;
        } 
        public int hashCode() {
            return (getKey()==null ? 0 : getKey().hashCode()) ^
                (getValue()==null ? 0 : getValue().hashCode());
        }
        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Entry.setValue is not supported.");
        }
        public Object getValue() {
            return value;
        }
        public Object getKey() {
            return key;
        }
    }
    private final static class Referenced {
        private final WeakReference reference;
        private final int           hashCode;
        private Referenced(Object referant) {
            reference = new WeakReference(referant);
            hashCode  = referant.hashCode();
        }
        private Referenced(Object key, ReferenceQueue queue) {
            reference = new WeakKey(key, queue, this);
            hashCode  = key.hashCode();
        }
        public int hashCode() {
            return hashCode;
        }
        private Object getValue() {
            return reference.get();
        }
        public boolean equals(Object o) {
            boolean result = false;
            if (o instanceof Referenced) {
                Referenced otherKey = (Referenced) o;
                Object thisKeyValue = getValue();
                Object otherKeyValue = otherKey.getValue();
                if (thisKeyValue == null) {                     
                    result = (otherKeyValue == null);
                    if (result == true) {
                        result = (this.hashCode() == otherKey.hashCode());
                    }
                }
                else
                {
                    result = thisKeyValue.equals(otherKeyValue);
                }
            }
            return result;
        }
    }
    private final static class WeakKey extends WeakReference {
        private final Referenced referenced;
        private WeakKey(Object key, 
                        ReferenceQueue queue,
                        Referenced referenced) {
            super(key, queue);
            this.referenced = referenced;
        }
        private Referenced getReferenced() {
            return referenced;
        }
     }
}
