final class CustomConcurrentHashMap {
  private CustomConcurrentHashMap() {}
  static final class Builder {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    private static final int UNSET_INITIAL_CAPACITY = -1;
    private static final int UNSET_CONCURRENCY_LEVEL = -1;
    int initialCapacity = UNSET_INITIAL_CAPACITY;
    int concurrencyLevel = UNSET_CONCURRENCY_LEVEL;
    public Builder initialCapacity(int initialCapacity) {
      if (this.initialCapacity != UNSET_INITIAL_CAPACITY) {
        throw new IllegalStateException(
            "initial capacity was already set to " + this.initialCapacity);
      }
      if (initialCapacity < 0) {
        throw new IllegalArgumentException();
      }
      this.initialCapacity = initialCapacity;
      return this;
    }
    public Builder concurrencyLevel(int concurrencyLevel) {
      if (this.concurrencyLevel != UNSET_CONCURRENCY_LEVEL) {
        throw new IllegalStateException(
            "concurrency level was already set to " + this.concurrencyLevel);
      }
      if (concurrencyLevel <= 0) {
        throw new IllegalArgumentException();
      }
      this.concurrencyLevel = concurrencyLevel;
      return this;
    }
    public <K, V, E> ConcurrentMap<K, V> buildMap(Strategy<K, V, E> strategy) {
      if (strategy == null) {
        throw new NullPointerException("strategy");
      }
      return new Impl<K, V, E>(strategy, this);
    }
    public <K, V, E> ConcurrentMap<K, V> buildComputingMap(
        ComputingStrategy<K, V, E> strategy,
        Function<? super K, ? extends V> computer) {
      if (strategy == null) {
        throw new NullPointerException("strategy");
      }
      if (computer == null) {
        throw new NullPointerException("computer");
      }
      return new ComputingImpl<K, V, E>(strategy, this, computer);
    }
    int getInitialCapacity() {
      return (initialCapacity == UNSET_INITIAL_CAPACITY)
          ? DEFAULT_INITIAL_CAPACITY : initialCapacity;
    }
    int getConcurrencyLevel() {
      return (concurrencyLevel == UNSET_CONCURRENCY_LEVEL)
          ? DEFAULT_CONCURRENCY_LEVEL : concurrencyLevel;
    }
  }
  public interface Strategy<K, V, E> {
    abstract E newEntry(K key, int hash, E next);
    E copyEntry(K key, E original, E newNext);
    void setValue(E entry, V value);
    V getValue(E entry);
    boolean equalKeys(K a, Object b);
    boolean equalValues(V a, Object b);
    int hashKey(Object key);
    K getKey(E entry);
    E getNext(E entry);
    int getHash(E entry);
    void setInternals(Internals<K, V, E> internals);
  }
  public interface Internals<K, V, E> {
    E getEntry(K key);
    boolean removeEntry(E entry, @Nullable V value);
    boolean removeEntry(E entry);
  }
  public interface ComputingStrategy<K, V, E> extends Strategy<K, V, E> {
    V compute(K key, E entry, Function<? super K, ? extends V> computer);
    V waitForValue(E entry) throws InterruptedException;
  }
  private static int rehash(int h) {
    h += (h << 15) ^ 0xffffcd7d;
    h ^= (h >>> 10);
    h += (h << 3);
    h ^= (h >>> 6);
    h += (h << 2) + (h << 14);
    return h ^ (h >>> 16);
  }
  static class Impl<K, V, E> extends AbstractMap<K, V>
      implements ConcurrentMap<K, V>, Serializable {
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int MAX_SEGMENTS = 1 << 16; 
    static final int RETRIES_BEFORE_LOCK = 2;
    final Strategy<K, V, E> strategy;
    final int segmentMask;
    final int segmentShift;
    final Segment[] segments;
    Impl(Strategy<K, V, E> strategy, Builder builder) {
      int concurrencyLevel = builder.getConcurrencyLevel();
      int initialCapacity = builder.getInitialCapacity();
      if (concurrencyLevel > MAX_SEGMENTS) {
        concurrencyLevel = MAX_SEGMENTS;
      }
      int segmentShift = 0;
      int segmentCount = 1;
      while (segmentCount < concurrencyLevel) {
        ++segmentShift;
        segmentCount <<= 1;
      }
      this.segmentShift = 32 - segmentShift;
      segmentMask = segmentCount - 1;
      this.segments = newSegmentArray(segmentCount);
      if (initialCapacity > MAXIMUM_CAPACITY) {
        initialCapacity = MAXIMUM_CAPACITY;
      }
      int segmentCapacity = initialCapacity / segmentCount;
      if (segmentCapacity * segmentCount < initialCapacity) {
        ++segmentCapacity;
      }
      int segmentSize = 1;
      while (segmentSize < segmentCapacity) {
          segmentSize <<= 1;
      }
      for (int i = 0; i < this.segments.length; ++i) {
        this.segments[i] = new Segment(segmentSize);
      }
      this.strategy = strategy;
      strategy.setInternals(new InternalsImpl());
    }
    int hash(Object key) {
      int h = strategy.hashKey(key);
      return rehash(h);
    }
    class InternalsImpl implements Internals<K, V, E>, Serializable {
      static final long serialVersionUID = 0;
      public E getEntry(K key) {
        if (key == null) {
          throw new NullPointerException("key");
        }
        int hash = hash(key);
        return segmentFor(hash).getEntry(key, hash);
      }
      public boolean removeEntry(E entry, V value) {
        if (entry == null) {
          throw new NullPointerException("entry");
        }
        int hash = strategy.getHash(entry);
        return segmentFor(hash).removeEntry(entry, hash, value);
      }
      public boolean removeEntry(E entry) {
        if (entry == null) {
          throw new NullPointerException("entry");
        }
        int hash = strategy.getHash(entry);
        return segmentFor(hash).removeEntry(entry, hash);
      }
    }
    @SuppressWarnings("unchecked")
    Segment[] newSegmentArray(int ssize) {
      return (Segment[]) Array.newInstance(Segment.class, ssize);
    }
    Segment segmentFor(int hash) {
      return segments[(hash >>> segmentShift) & segmentMask];
    }
    @SuppressWarnings("serial") 
    final class Segment extends ReentrantLock {
      volatile int count;
      int modCount;
      int threshold;
      volatile AtomicReferenceArray<E> table;
      Segment(int initialCapacity) {
        setTable(newEntryArray(initialCapacity));
      }
      AtomicReferenceArray<E> newEntryArray(int size) {
        return new AtomicReferenceArray<E>(size);
      }
      void setTable(AtomicReferenceArray<E> newTable) {
        this.threshold = newTable.length() * 3 / 4;
        this.table = newTable;
      }
      E getFirst(int hash) {
        AtomicReferenceArray<E> table = this.table;
        return table.get(hash & (table.length() - 1));
      }
      public E getEntry(Object key, int hash) {
        Strategy<K, V, E> s = Impl.this.strategy;
        if (count != 0) { 
          for (E e = getFirst(hash); e != null; e = s.getNext(e)) {
            if (s.getHash(e) != hash) {
              continue;
            }
            K entryKey = s.getKey(e);
            if (entryKey == null) {
              continue;
            }
            if (s.equalKeys(entryKey, key)) {
              return e;
            }
          }
        }
        return null;
      }
      V get(Object key, int hash) {
        E entry = getEntry(key, hash);
        if (entry == null) {
          return null;
        }
        return strategy.getValue(entry);
      }
      boolean containsKey(Object key, int hash) {
        Strategy<K, V, E> s = Impl.this.strategy;
        if (count != 0) { 
          for (E e = getFirst(hash); e != null; e = s.getNext(e)) {
            if (s.getHash(e) != hash) {
              continue;
            }
            K entryKey = s.getKey(e);
            if (entryKey == null) {
              continue;
            }
            if (s.equalKeys(entryKey, key)) {
              return s.getValue(e) != null;
            }
          }
        }
        return false;
      }
      boolean containsValue(Object value) {
        Strategy<K, V, E> s = Impl.this.strategy;
        if (count != 0) { 
          AtomicReferenceArray<E> table = this.table;
          int length = table.length();
          for (int i = 0; i < length; i++) {
            for (E e = table.get(i); e != null; e = s.getNext(e)) {
              V entryValue = s.getValue(e);
              if (entryValue == null) {
                continue;
              }
              if (s.equalValues(entryValue, value)) {
                return true;
              }
            }
          }
        }
        return false;
      }
      boolean replace(K key, int hash, V oldValue, V newValue) {
        Strategy<K, V, E> s = Impl.this.strategy;
        lock();
        try {
          for (E e = getFirst(hash); e != null; e = s.getNext(e)) {
            K entryKey = s.getKey(e);
            if (s.getHash(e) == hash && entryKey != null
                && s.equalKeys(key, entryKey)) {
              V entryValue = s.getValue(e);
              if (entryValue == null) {
                return false;
              }
              if (s.equalValues(entryValue, oldValue)) {
                s.setValue(e, newValue);
                return true;
              }
            }
          }
          return false;
        } finally {
          unlock();
        }
      }
      V replace(K key, int hash, V newValue) {
        Strategy<K, V, E> s = Impl.this.strategy;
        lock();
        try {
          for (E e = getFirst(hash); e != null; e = s.getNext(e)) {
            K entryKey = s.getKey(e);
            if (s.getHash(e) == hash && entryKey != null
                && s.equalKeys(key, entryKey)) {
              V entryValue = s.getValue(e);
              if (entryValue == null) {
                return null;
              }
              s.setValue(e, newValue);
              return entryValue;
            }
          }
          return null;
        } finally {
          unlock();
        }
      }
      V put(K key, int hash, V value, boolean onlyIfAbsent) {
        Strategy<K, V, E> s = Impl.this.strategy;
        lock();
        try {
          int count = this.count;
          if (count++ > this.threshold) { 
            expand();
          }
          AtomicReferenceArray<E> table = this.table;
          int index = hash & (table.length() - 1);
          E first = table.get(index);
          for (E e = first; e != null; e = s.getNext(e)) {
            K entryKey = s.getKey(e);
            if (s.getHash(e) == hash && entryKey != null
                && s.equalKeys(key, entryKey)) {
              V entryValue = s.getValue(e);
              if (onlyIfAbsent && entryValue != null) {
                return entryValue;
              }
              s.setValue(e, value);
              return entryValue;
            }
          }
          ++modCount;
          E newEntry = s.newEntry(key, hash, first);
          s.setValue(newEntry, value);
          table.set(index, newEntry);
          this.count = count; 
          return null;
        } finally {
          unlock();
        }
      }
      void expand() {
        AtomicReferenceArray<E> oldTable = table;
        int oldCapacity = oldTable.length();
        if (oldCapacity >= MAXIMUM_CAPACITY) {
          return;
        }
        Strategy<K, V, E> s = Impl.this.strategy;
        AtomicReferenceArray<E> newTable = newEntryArray(oldCapacity << 1);
        threshold = newTable.length() * 3 / 4;
        int newMask = newTable.length() - 1;
        for (int oldIndex = 0; oldIndex < oldCapacity; oldIndex++) {
          E head = oldTable.get(oldIndex);
          if (head != null) {
            E next = s.getNext(head);
            int headIndex = s.getHash(head) & newMask;
            if (next == null) {
              newTable.set(headIndex, head);
            } else {
              E tail = head;
              int tailIndex = headIndex;
              for (E last = next; last != null; last = s.getNext(last)) {
                int newIndex = s.getHash(last) & newMask;
                if (newIndex != tailIndex) {
                  tailIndex = newIndex;
                  tail = last;
                }
              }
              newTable.set(tailIndex, tail);
              for (E e = head; e != tail; e = s.getNext(e)) {
                K key = s.getKey(e);
                if (key != null) {
                  int newIndex = s.getHash(e) & newMask;
                  E newNext = newTable.get(newIndex);
                  newTable.set(newIndex, s.copyEntry(key, e, newNext));
                } else {
                }
              }
            }
          }
        }
        table = newTable;
      }
      V remove(Object key, int hash) {
        Strategy<K, V, E> s = Impl.this.strategy;
        lock();
        try {
          int count = this.count - 1;
          AtomicReferenceArray<E> table = this.table;
          int index = hash & (table.length() - 1);
          E first = table.get(index);
          for (E e = first; e != null; e = s.getNext(e)) {
            K entryKey = s.getKey(e);
            if (s.getHash(e) == hash && entryKey != null
                && s.equalKeys(entryKey, key)) {
              V entryValue = strategy.getValue(e);
              ++modCount;
              E newFirst = s.getNext(e);
              for (E p = first; p != e; p = s.getNext(p)) {
                K pKey = s.getKey(p);
                if (pKey != null) {
                  newFirst = s.copyEntry(pKey, p, newFirst);
                } else {
                }
              }
              table.set(index, newFirst);
              this.count = count; 
              return entryValue;
            }
          }
          return null;
        } finally {
          unlock();
        }
      }
      boolean remove(Object key, int hash, Object value) {
        Strategy<K, V, E> s = Impl.this.strategy;
        lock();
        try {
          int count = this.count - 1;
          AtomicReferenceArray<E> table = this.table;
          int index = hash & (table.length() - 1);
          E first = table.get(index);
          for (E e = first; e != null; e = s.getNext(e)) {
            K entryKey = s.getKey(e);
            if (s.getHash(e) == hash && entryKey != null
                && s.equalKeys(entryKey, key)) {
              V entryValue = strategy.getValue(e);
              if (value == entryValue || (value != null && entryValue != null
                  && s.equalValues(entryValue, value))) {
                ++modCount;
                E newFirst = s.getNext(e);
                for (E p = first; p != e; p = s.getNext(p)) {
                  K pKey = s.getKey(p);
                  if (pKey != null) {
                    newFirst = s.copyEntry(pKey, p, newFirst);
                  } else {
                  }
                }
                table.set(index, newFirst);
                this.count = count; 
                return true;
              } else {
                return false;
              }
            }
          }
          return false;
        } finally {
          unlock();
        }
      }
      public boolean removeEntry(E entry, int hash, V value) {
        Strategy<K, V, E> s = Impl.this.strategy;
        lock();
        try {
          int count = this.count - 1;
          AtomicReferenceArray<E> table = this.table;
          int index = hash & (table.length() - 1);
          E first = table.get(index);
          for (E e = first; e != null; e = s.getNext(e)) {
            if (s.getHash(e) == hash && entry.equals(e)) {
              V entryValue = s.getValue(e);
              if (entryValue == value || (value != null
                  && s.equalValues(entryValue, value))) {
                ++modCount;
                E newFirst = s.getNext(e);
                for (E p = first; p != e; p = s.getNext(p)) {
                  K pKey = s.getKey(p);
                  if (pKey != null) {
                    newFirst = s.copyEntry(pKey, p, newFirst);
                  } else {
                  }
                }
                table.set(index, newFirst);
                this.count = count; 
                return true;
              } else {
                return false;
              }
            }
          }
          return false;
        } finally {
          unlock();
        }
      }
      public boolean removeEntry(E entry, int hash) {
        Strategy<K, V, E> s = Impl.this.strategy;
        lock();
        try {
          int count = this.count - 1;
          AtomicReferenceArray<E> table = this.table;
          int index = hash & (table.length() - 1);
          E first = table.get(index);
          for (E e = first; e != null; e = s.getNext(e)) {
            if (s.getHash(e) == hash && entry.equals(e)) {
              ++modCount;
              E newFirst = s.getNext(e);
              for (E p = first; p != e; p = s.getNext(p)) {
                K pKey = s.getKey(p);
                if (pKey != null) {
                  newFirst = s.copyEntry(pKey, p, newFirst);
                } else {
                }
              }
              table.set(index, newFirst);
              this.count = count; 
              return true;
            }
          }
          return false;
        } finally {
          unlock();
        }
      }
      void clear() {
        if (count != 0) {
          lock();
          try {
            AtomicReferenceArray<E> table = this.table;
            for (int i = 0; i < table.length(); i++) {
              table.set(i, null);
            }
            ++modCount;
            count = 0; 
          } finally {
            unlock();
          }
        }
      }
    }
    @Override public boolean isEmpty() {
      final Segment[] segments = this.segments;
      int[] mc = new int[segments.length];
      int mcsum = 0;
      for (int i = 0; i < segments.length; ++i) {
        if (segments[i].count != 0) {
          return false;
        } else {
          mcsum += mc[i] = segments[i].modCount;
        }
      }
      if (mcsum != 0) {
        for (int i = 0; i < segments.length; ++i) {
          if (segments[i].count != 0 ||
              mc[i] != segments[i].modCount) {
            return false;
          }
        }
      }
      return true;
    }
    @Override public int size() {
      final Segment[] segments = this.segments;
      long sum = 0;
      long check = 0;
      int[] mc = new int[segments.length];
      for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
        check = 0;
        sum = 0;
        int mcsum = 0;
        for (int i = 0; i < segments.length; ++i) {
          sum += segments[i].count;
          mcsum += mc[i] = segments[i].modCount;
        }
        if (mcsum != 0) {
          for (int i = 0; i < segments.length; ++i) {
            check += segments[i].count;
            if (mc[i] != segments[i].modCount) {
              check = -1; 
              break;
            }
          }
        }
        if (check == sum) {
          break;
        }
      }
      if (check != sum) { 
        sum = 0;
        for (Segment segment : segments) {
          segment.lock();
        }
        for (Segment segment : segments) {
          sum += segment.count;
        }
        for (Segment segment : segments) {
          segment.unlock();
        }
      }
      if (sum > Integer.MAX_VALUE) {
        return Integer.MAX_VALUE;
      } else {
        return (int) sum;
      }
    }
    @Override public V get(Object key) {
      if (key == null) {
        throw new NullPointerException("key");
      }
      int hash = hash(key);
      return segmentFor(hash).get(key, hash);
    }
    @Override public boolean containsKey(Object key) {
      if (key == null) {
        throw new NullPointerException("key");
      }
      int hash = hash(key);
      return segmentFor(hash).containsKey(key, hash);
    }
    @Override public boolean containsValue(Object value) {
      if (value == null) {
        throw new NullPointerException("value");
      }
      final Segment[] segments = this.segments;
      int[] mc = new int[segments.length];
      for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
        int mcsum = 0;
        for (int i = 0; i < segments.length; ++i) {
          @SuppressWarnings("UnusedDeclaration")
          int c = segments[i].count;
          mcsum += mc[i] = segments[i].modCount;
          if (segments[i].containsValue(value)) {
            return true;
          }
        }
        boolean cleanSweep = true;
        if (mcsum != 0) {
          for (int i = 0; i < segments.length; ++i) {
            @SuppressWarnings("UnusedDeclaration")
            int c = segments[i].count;
            if (mc[i] != segments[i].modCount) {
              cleanSweep = false;
              break;
            }
          }
        }
        if (cleanSweep) {
          return false;
        }
      }
      for (Segment segment : segments) {
        segment.lock();
      }
      boolean found = false;
      try {
        for (Segment segment : segments) {
          if (segment.containsValue(value)) {
            found = true;
            break;
          }
        }
      } finally {
        for (Segment segment : segments) {
          segment.unlock();
        }
      }
      return found;
    }
    @Override public V put(K key, V value) {
      if (key == null) {
        throw new NullPointerException("key");
      }
      if (value == null) {
        throw new NullPointerException("value");
      }
      int hash = hash(key);
      return segmentFor(hash).put(key, hash, value, false);
    }
    public V putIfAbsent(K key, V value) {
      if (key == null) {
        throw new NullPointerException("key");
      }
      if (value == null) {
        throw new NullPointerException("value");
      }
      int hash = hash(key);
      return segmentFor(hash).put(key, hash, value, true);
    }
    @Override public void putAll(Map<? extends K, ? extends V> m) {
      for (Entry<? extends K, ? extends V> e : m.entrySet()) {
        put(e.getKey(), e.getValue());
      }
    }
    @Override public V remove(Object key) {
      if (key == null) {
        throw new NullPointerException("key");
      }
      int hash = hash(key);
      return segmentFor(hash).remove(key, hash);
    }
    public boolean remove(Object key, Object value) {
      if (key == null) {
        throw new NullPointerException("key");
      }
      int hash = hash(key);
      return segmentFor(hash).remove(key, hash, value);
    }
    public boolean replace(K key, V oldValue, V newValue) {
      if (key == null) {
        throw new NullPointerException("key");
      }
      if (oldValue == null) {
        throw new NullPointerException("oldValue");
      }
      if (newValue == null) {
        throw new NullPointerException("newValue");
      }
      int hash = hash(key);
      return segmentFor(hash).replace(key, hash, oldValue, newValue);
    }
    public V replace(K key, V value) {
      if (key == null) {
        throw new NullPointerException("key");
      }
      if (value == null) {
        throw new NullPointerException("value");
      }
      int hash = hash(key);
      return segmentFor(hash).replace(key, hash, value);
    }
    @Override public void clear() {
      for (Segment segment : segments) {
        segment.clear();
      }
    }
    Set<K> keySet;
    @Override public Set<K> keySet() {
      Set<K> ks = keySet;
      return (ks != null) ? ks : (keySet = new KeySet());
    }
    Collection<V> values;
    @Override public Collection<V> values() {
      Collection<V> vs = values;
      return (vs != null) ? vs : (values = new Values());
    }
    Set<Entry<K, V>> entrySet;
    @Override public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> es = entrySet;
      return (es != null) ? es : (entrySet = new EntrySet());
    }
    abstract class HashIterator {
      int nextSegmentIndex;
      int nextTableIndex;
      AtomicReferenceArray<E> currentTable;
      E nextEntry;
      WriteThroughEntry nextExternal;
      WriteThroughEntry lastReturned;
      HashIterator() {
        nextSegmentIndex = segments.length - 1;
        nextTableIndex = -1;
        advance();
      }
      public boolean hasMoreElements() {
        return hasNext();
      }
      final void advance() {
        nextExternal = null;
        if (nextInChain()) {
          return;
        }
        if (nextInTable()) {
          return;
        }
        while (nextSegmentIndex >= 0) {
          Segment seg = segments[nextSegmentIndex--];
          if (seg.count != 0) {
            currentTable = seg.table;
            nextTableIndex = currentTable.length() - 1;
            if (nextInTable()) {
              return;
            }
          }
        }
      }
      boolean nextInChain() {
        Strategy<K, V, E> s = Impl.this.strategy;
        if (nextEntry != null) {
          for (nextEntry = s.getNext(nextEntry); nextEntry != null;
              nextEntry = s.getNext(nextEntry)) {
            if (advanceTo(nextEntry)) {
              return true;
            }
          }
        }
        return false;
      }
      boolean nextInTable() {
        while (nextTableIndex >= 0) {
          if ((nextEntry = currentTable.get(nextTableIndex--)) != null) {
            if (advanceTo(nextEntry) || nextInChain()) {
              return true;
            }
          }
        }
        return false;
      }
      boolean advanceTo(E entry) {
        Strategy<K, V, E> s = Impl.this.strategy;
        K key = s.getKey(entry);
        V value = s.getValue(entry);
        if (key != null && value != null) {
          nextExternal = new WriteThroughEntry(key, value);
          return true;
        } else {
          return false;
        }
      }
      public boolean hasNext() {
        return nextExternal != null;
      }
      WriteThroughEntry nextEntry() {
        if (nextExternal == null) {
          throw new NoSuchElementException();
        }
        lastReturned = nextExternal;
        advance();
        return lastReturned;
      }
      public void remove() {
        if (lastReturned == null) {
          throw new IllegalStateException();
        }
        Impl.this.remove(lastReturned.getKey());
        lastReturned = null;
      }
    }
    final class KeyIterator extends HashIterator implements Iterator<K> {
      public K next() {
        return super.nextEntry().getKey();
      }
    }
    final class ValueIterator extends HashIterator implements Iterator<V> {
      public V next() {
        return super.nextEntry().getValue();
      }
    }
    final class WriteThroughEntry extends AbstractMapEntry<K, V> {
      final K key;
      V value;
      WriteThroughEntry(K key, V value) {
        this.key = key;
        this.value = value;
      }
      @Override public K getKey() {
        return key;
      }
      @Override public V getValue() {
        return value;
      }
      @Override public V setValue(V value) {
        if (value == null) {
          throw new NullPointerException();
        }
        V oldValue = Impl.this.put(getKey(), value);
        this.value = value;
        return oldValue;
      }
    }
    final class EntryIterator extends HashIterator
        implements Iterator<Entry<K, V>> {
      public Entry<K, V> next() {
        return nextEntry();
      }
    }
    final class KeySet extends AbstractSet<K> {
      @Override public Iterator<K> iterator() {
        return new KeyIterator();
      }
      @Override public int size() {
        return Impl.this.size();
      }
      @Override public boolean isEmpty() {
        return Impl.this.isEmpty();
      }
      @Override public boolean contains(Object o) {
        return Impl.this.containsKey(o);
      }
      @Override public boolean remove(Object o) {
        return Impl.this.remove(o) != null;
      }
      @Override public void clear() {
        Impl.this.clear();
      }
    }
    final class Values extends AbstractCollection<V> {
      @Override public Iterator<V> iterator() {
        return new ValueIterator();
      }
      @Override public int size() {
        return Impl.this.size();
      }
      @Override public boolean isEmpty() {
        return Impl.this.isEmpty();
      }
      @Override public boolean contains(Object o) {
        return Impl.this.containsValue(o);
      }
      @Override public void clear() {
        Impl.this.clear();
      }
    }
    final class EntrySet extends AbstractSet<Entry<K, V>> {
      @Override public Iterator<Entry<K, V>> iterator() {
        return new EntryIterator();
      }
      @Override public boolean contains(Object o) {
        if (!(o instanceof Entry)) {
          return false;
        }
        Entry<?, ?> e = (Entry<?, ?>) o;
        Object key = e.getKey();
        if (key == null) {
          return false;
        }
        V v = Impl.this.get(key);
        return v != null && strategy.equalValues(v, e.getValue());
      }
      @Override public boolean remove(Object o) {
        if (!(o instanceof Entry)) {
          return false;
        }
        Entry<?, ?> e = (Entry<?, ?>) o;
        Object key = e.getKey();
        return key != null && Impl.this.remove(key, e.getValue());
      }
      @Override public int size() {
        return Impl.this.size();
      }
      @Override public boolean isEmpty() {
        return Impl.this.isEmpty();
      }
      @Override public void clear() {
        Impl.this.clear();
      }
    }
    private static final long serialVersionUID = 1;
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
      out.writeInt(size());
      out.writeInt(segments.length); 
      out.writeObject(strategy);
      for (Entry<K, V> entry : entrySet()) {
        out.writeObject(entry.getKey());
        out.writeObject(entry.getValue());
      }
      out.writeObject(null); 
    }
    static class Fields {
      static final Field segmentShift = findField("segmentShift");
      static final Field segmentMask = findField("segmentMask");
      static final Field segments = findField("segments");
      static final Field strategy = findField("strategy");
      static Field findField(String name) {
        try {
          Field f = Impl.class.getDeclaredField(name);
          f.setAccessible(true);
          return f;
        } catch (NoSuchFieldException e) {
          throw new AssertionError(e);
        }
      }
    }
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
      try {
        int initialCapacity = in.readInt();
        int concurrencyLevel = in.readInt();
        Strategy<K, V, E> strategy = (Strategy<K, V, E>) in.readObject();
        if (concurrencyLevel > MAX_SEGMENTS) {
          concurrencyLevel = MAX_SEGMENTS;
        }
        int segmentShift = 0;
        int segmentCount = 1;
        while (segmentCount < concurrencyLevel) {
          ++segmentShift;
          segmentCount <<= 1;
        }
        Fields.segmentShift.set(this, 32 - segmentShift);
        Fields.segmentMask.set(this, segmentCount - 1);
        Fields.segments.set(this, newSegmentArray(segmentCount));
        if (initialCapacity > MAXIMUM_CAPACITY) {
          initialCapacity = MAXIMUM_CAPACITY;
        }
        int segmentCapacity = initialCapacity / segmentCount;
        if (segmentCapacity * segmentCount < initialCapacity) {
          ++segmentCapacity;
        }
        int segmentSize = 1;
        while (segmentSize < segmentCapacity) {
            segmentSize <<= 1;
        }
        for (int i = 0; i < this.segments.length; ++i) {
          this.segments[i] = new Segment(segmentSize);
        }
        Fields.strategy.set(this, strategy);
        while (true) {
          K key = (K) in.readObject();
          if (key == null) {
            break; 
          }
          V value = (V) in.readObject();
          put(key, value);
        }
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }
  static class ComputingImpl<K, V, E> extends Impl<K, V, E> {
    static final long serialVersionUID = 0;
    final ComputingStrategy<K, V, E> computingStrategy;
    final Function<? super K, ? extends V> computer;
    ComputingImpl(ComputingStrategy<K, V, E> strategy, Builder builder,
        Function<? super K, ? extends V> computer) {
      super(strategy, builder);
      this.computingStrategy = strategy;
      this.computer = computer;
    }
    @Override public V get(Object k) {
      @SuppressWarnings("unchecked")
      K key = (K) k;
      if (key == null) {
        throw new NullPointerException("key");
      }
      int hash = hash(key);
      Segment segment = segmentFor(hash);
      outer: while (true) {
        E entry = segment.getEntry(key, hash);
        if (entry == null) {
          boolean created = false;
          segment.lock();
          try {
            entry = segment.getEntry(key, hash);
            if (entry == null) {
              created = true;
              int count = segment.count;
              if (count++ > segment.threshold) { 
                segment.expand();
              }
              AtomicReferenceArray<E> table = segment.table;
              int index = hash & (table.length() - 1);
              E first = table.get(index);
              ++segment.modCount;
              entry = computingStrategy.newEntry(key, hash, first);
              table.set(index, entry);
              segment.count = count; 
            }
          } finally {
            segment.unlock();
          }
          if (created) {
            boolean success = false;
            try {
              V value = computingStrategy.compute(key, entry, computer);
              if (value == null) {
                throw new NullPointerException(
                    "compute() returned null unexpectedly");
              }
              success = true;
              return value;
            } finally {
              if (!success) {
                segment.removeEntry(entry, hash);
              }
            }
          }
        }
        boolean interrupted = false;
        try {
          while (true) {
            try {
              V value = computingStrategy.waitForValue(entry);
              if (value == null) {
                segment.removeEntry(entry, hash);
                continue outer;
              }
              return value;
            } catch (InterruptedException e) {
              interrupted = true;
            }
          }
        } finally {
          if (interrupted) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
  }
  static class SimpleStrategy<K, V>
      implements Strategy<K, V, SimpleInternalEntry<K, V>> {
    public SimpleInternalEntry<K, V> newEntry(
        K key, int hash, SimpleInternalEntry<K, V> next) {
      return new SimpleInternalEntry<K, V>(key, hash, null, next);
    }
    public SimpleInternalEntry<K, V> copyEntry(K key,
        SimpleInternalEntry<K, V> original, SimpleInternalEntry<K, V> next) {
      return new SimpleInternalEntry<K, V>(
          key, original.hash, original.value, next);
    }
    public void setValue(SimpleInternalEntry<K, V> entry, V value) {
      entry.value = value;
    }
    public V getValue(SimpleInternalEntry<K, V> entry) {
      return entry.value;
    }
    public boolean equalKeys(K a, Object b) {
      return a.equals(b);
    }
    public boolean equalValues(V a, Object b) {
      return a.equals(b);
    }
    public int hashKey(Object key) {
      return key.hashCode();
    }
    public K getKey(SimpleInternalEntry<K, V> entry) {
      return entry.key;
    }
    public SimpleInternalEntry<K, V> getNext(SimpleInternalEntry<K, V> entry) {
      return entry.next;
    }
    public int getHash(SimpleInternalEntry<K, V> entry) {
      return entry.hash;
    }
    public void setInternals(
        Internals<K, V, SimpleInternalEntry<K, V>> internals) {
    }
  }
  static class SimpleInternalEntry<K, V> {
    final K key;
    final int hash;
    final SimpleInternalEntry<K, V> next;
    volatile V value;
    SimpleInternalEntry(
        K key, int hash, @Nullable V value, SimpleInternalEntry<K, V> next) {
      this.key = key;
      this.hash = hash;
      this.value = value;
      this.next = next;
    }
  }
}
