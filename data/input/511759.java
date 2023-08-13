public final class MapMaker {
  private Strength keyStrength = Strength.STRONG;
  private Strength valueStrength = Strength.STRONG;
  private long expirationNanos = 0;
  private boolean useCustomMap;
  private final CustomConcurrentHashMap.Builder builder
      = new CustomConcurrentHashMap.Builder();
  public MapMaker() {}
  public MapMaker initialCapacity(int initialCapacity) {
    builder.initialCapacity(initialCapacity);
    return this;
  }
  @GwtIncompatible("java.util.concurrent.ConcurrentHashMap concurrencyLevel")
  public MapMaker concurrencyLevel(int concurrencyLevel) {
    builder.concurrencyLevel(concurrencyLevel);
    return this;
  }
  @GwtIncompatible("java.lang.ref.WeakReference")
  public MapMaker weakKeys() {
    return setKeyStrength(Strength.WEAK);
  }
  @GwtIncompatible("java.lang.ref.SoftReference")
  public MapMaker softKeys() {
    return setKeyStrength(Strength.SOFT);
  }
  private MapMaker setKeyStrength(Strength strength) {
    if (keyStrength != Strength.STRONG) {
      throw new IllegalStateException("Key strength was already set to "
          + keyStrength + ".");
    }
    keyStrength = strength;
    useCustomMap = true;
    return this;
  }
  @GwtIncompatible("java.lang.ref.WeakReference")
  public MapMaker weakValues() {
    return setValueStrength(Strength.WEAK);
  }
  @GwtIncompatible("java.lang.ref.SoftReference")
  public MapMaker softValues() {
    return setValueStrength(Strength.SOFT);
  }
  private MapMaker setValueStrength(Strength strength) {
    if (valueStrength != Strength.STRONG) {
      throw new IllegalStateException("Value strength was already set to "
          + valueStrength + ".");
    }
    valueStrength = strength;
    useCustomMap = true;
    return this;
  }
  public MapMaker expiration(long duration, TimeUnit unit) {
    if (expirationNanos != 0) {
      throw new IllegalStateException("expiration time of "
          + expirationNanos + " ns was already set");
    }
    if (duration <= 0) {
      throw new IllegalArgumentException("invalid duration: " + duration);
    }
    this.expirationNanos = unit.toNanos(duration);
    useCustomMap = true;
    return this;
  }
  public <K, V> ConcurrentMap<K, V> makeMap() {
    return useCustomMap
        ? new StrategyImpl<K, V>(this).map
        : new ConcurrentHashMap<K, V>(builder.getInitialCapacity(),
            0.75f, builder.getConcurrencyLevel());
  }
  public <K, V> ConcurrentMap<K, V> makeComputingMap(
      Function<? super K, ? extends V> computingFunction) {
    return new StrategyImpl<K, V>(this, computingFunction).map;
  }
  private enum Strength {
    WEAK {
      @Override boolean equal(Object a, Object b) {
        return a == b;
      }
      @Override int hash(Object o) {
        return System.identityHashCode(o);
      }
      @Override <K, V> ValueReference<K, V> referenceValue(
          ReferenceEntry<K, V> entry, V value) {
        return new WeakValueReference<K, V>(value, entry);
      }
      @Override <K, V> ReferenceEntry<K, V> newEntry(
          Internals<K, V, ReferenceEntry<K, V>> internals, K key,
          int hash, ReferenceEntry<K, V> next) {
        return (next == null)
            ? new WeakEntry<K, V>(internals, key, hash)
            : new LinkedWeakEntry<K, V>(internals, key, hash, next);
      }
      @Override <K, V> ReferenceEntry<K, V> copyEntry(
          K key, ReferenceEntry<K, V> original,
          ReferenceEntry<K, V> newNext) {
        WeakEntry<K, V> from = (WeakEntry<K, V>) original;
        return (newNext == null)
            ? new WeakEntry<K, V>(from.internals, key, from.hash)
            : new LinkedWeakEntry<K, V>(
                from.internals, key, from.hash, newNext);
      }
    },
    SOFT {
      @Override boolean equal(Object a, Object b) {
        return a == b;
      }
      @Override int hash(Object o) {
        return System.identityHashCode(o);
      }
      @Override <K, V> ValueReference<K, V> referenceValue(
          ReferenceEntry<K, V> entry, V value) {
        return new SoftValueReference<K, V>(value, entry);
      }
      @Override <K, V> ReferenceEntry<K, V> newEntry(
          Internals<K, V, ReferenceEntry<K, V>> internals, K key,
          int hash, ReferenceEntry<K, V> next) {
        return (next == null)
            ? new SoftEntry<K, V>(internals, key, hash)
            : new LinkedSoftEntry<K, V>(internals, key, hash, next);
      }
      @Override <K, V> ReferenceEntry<K, V> copyEntry(
          K key, ReferenceEntry<K, V> original,
          ReferenceEntry<K, V> newNext) {
        SoftEntry<K, V> from = (SoftEntry<K, V>) original;
        return (newNext == null)
            ? new SoftEntry<K, V>(from.internals, key, from.hash)
            : new LinkedSoftEntry<K, V>(
                from.internals, key, from.hash, newNext);
      }
    },
    STRONG {
      @Override boolean equal(Object a, Object b) {
        return a.equals(b);
      }
      @Override int hash(Object o) {
        return o.hashCode();
      }
      @Override <K, V> ValueReference<K, V> referenceValue(
          ReferenceEntry<K, V> entry, V value) {
        return new StrongValueReference<K, V>(value);
      }
      @Override <K, V> ReferenceEntry<K, V> newEntry(
          Internals<K, V, ReferenceEntry<K, V>> internals, K key,
          int hash, ReferenceEntry<K, V> next) {
        return (next == null)
            ? new StrongEntry<K, V>(internals, key, hash)
            : new LinkedStrongEntry<K, V>(
                internals, key, hash, next);
      }
      @Override <K, V> ReferenceEntry<K, V> copyEntry(
          K key, ReferenceEntry<K, V> original,
          ReferenceEntry<K, V> newNext) {
        StrongEntry<K, V> from = (StrongEntry<K, V>) original;
        return (newNext == null)
            ? new StrongEntry<K, V>(from.internals, key, from.hash)
            : new LinkedStrongEntry<K, V>(
                from.internals, key, from.hash, newNext);
      }
    };
    abstract boolean equal(Object a, Object b);
    abstract int hash(Object o);
    abstract <K, V> ValueReference<K, V> referenceValue(
        ReferenceEntry<K, V> entry, V value);
    abstract <K, V> ReferenceEntry<K, V> newEntry(
        Internals<K, V, ReferenceEntry<K, V>> internals, K key,
        int hash, ReferenceEntry<K, V> next);
    abstract <K, V> ReferenceEntry<K, V> copyEntry(K key,
        ReferenceEntry<K, V> original, ReferenceEntry<K, V> newNext);
  }
  private static class StrategyImpl<K, V> implements Serializable,
      ComputingStrategy<K, V, ReferenceEntry<K, V>> {
    final Strength keyStrength;
    final Strength valueStrength;
    final ConcurrentMap<K, V> map;
    final long expirationNanos;
    Internals<K, V, ReferenceEntry<K, V>> internals;
    StrategyImpl(MapMaker maker) {
      this.keyStrength = maker.keyStrength;
      this.valueStrength = maker.valueStrength;
      this.expirationNanos = maker.expirationNanos;
      map = maker.builder.buildMap(this);
    }
    StrategyImpl(
        MapMaker maker, Function<? super K, ? extends V> computer) {
      this.keyStrength = maker.keyStrength;
      this.valueStrength = maker.valueStrength;
      this.expirationNanos = maker.expirationNanos;
      map = maker.builder.buildComputingMap(this, computer);
    }
    public void setValue(ReferenceEntry<K, V> entry, V value) {
      setValueReference(
          entry, valueStrength.referenceValue(entry, value));
      if (expirationNanos > 0) {
        scheduleRemoval(entry.getKey(), value);
      }
    }
    void scheduleRemoval(K key, V value) {
      final WeakReference<K> keyReference = new WeakReference<K>(key);
      final WeakReference<V> valueReference = new WeakReference<V>(value);
      ExpirationTimer.instance.schedule(
          new TimerTask() {
            @Override public void run() {
              K key = keyReference.get();
              if (key != null) {
                map.remove(key, valueReference.get());
              }
            }
          }, TimeUnit.NANOSECONDS.toMillis(expirationNanos));
    }
    public boolean equalKeys(K a, Object b) {
      return keyStrength.equal(a, b);
    }
    public boolean equalValues(V a, Object b) {
      return valueStrength.equal(a, b);
    }
    public int hashKey(Object key) {
      return keyStrength.hash(key);
    }
    public K getKey(ReferenceEntry<K, V> entry) {
      return entry.getKey();
    }
    public int getHash(ReferenceEntry<K, V> entry) {
      return entry.getHash();
    }
    public ReferenceEntry<K, V> newEntry(
        K key, int hash, ReferenceEntry<K, V> next) {
      return keyStrength.newEntry(internals, key, hash, next);
    }
    public ReferenceEntry<K, V> copyEntry(K key,
        ReferenceEntry<K, V> original, ReferenceEntry<K, V> newNext) {
      ValueReference<K, V> valueReference = original.getValueReference();
      if (valueReference == COMPUTING) {
        ReferenceEntry<K, V> newEntry
            = newEntry(key, original.getHash(), newNext);
        newEntry.setValueReference(
            new FutureValueReference(original, newEntry));
        return newEntry;
      } else {
        ReferenceEntry<K, V> newEntry
            = newEntry(key, original.getHash(), newNext);
        newEntry.setValueReference(valueReference.copyFor(newEntry));
        return newEntry;
      }
    }
    public V waitForValue(ReferenceEntry<K, V> entry)
        throws InterruptedException {
      ValueReference<K, V> valueReference = entry.getValueReference();
      if (valueReference == COMPUTING) {
        synchronized (entry) {
          while ((valueReference = entry.getValueReference())
              == COMPUTING) {
            entry.wait();
          }
        }
      }
      return valueReference.waitForValue();
    }
    public V getValue(ReferenceEntry<K, V> entry) {
      ValueReference<K, V> valueReference = entry.getValueReference();
      return valueReference.get();
    }
    public V compute(K key, final ReferenceEntry<K, V> entry,
        Function<? super K, ? extends V> computer) {
      V value;
      try {
        value = computer.apply(key);
      } catch (ComputationException e) {
        setValueReference(entry,
            new ComputationExceptionReference<K, V>(e.getCause()));
        throw e;
      } catch (Throwable t) {
        setValueReference(
          entry, new ComputationExceptionReference<K, V>(t));
        throw new ComputationException(t);
      }
      if (value == null) {
        String message
            = computer + " returned null for key " + key + ".";
        setValueReference(
            entry, new NullOutputExceptionReference<K, V>(message));
        throw new NullOutputException(message);
      } else {
        setValue(entry, value);
      }
      return value;
    }
    void setValueReference(ReferenceEntry<K, V> entry,
        ValueReference<K, V> valueReference) {
      boolean notifyOthers = (entry.getValueReference() == COMPUTING);
      entry.setValueReference(valueReference);
      if (notifyOthers) {
        synchronized (entry) {
          entry.notifyAll();
        }
      }
    }
    private class FutureValueReference implements ValueReference<K, V> {
      final ReferenceEntry<K, V> original;
      final ReferenceEntry<K, V> newEntry;
      FutureValueReference(
          ReferenceEntry<K, V> original, ReferenceEntry<K, V> newEntry) {
        this.original = original;
        this.newEntry = newEntry;
      }
      public V get() {
        boolean success = false;
        try {
          V value = original.getValueReference().get();
          success = true;
          return value;
        } finally {
          if (!success) {
            removeEntry();
          }
        }
      }
      public ValueReference<K, V> copyFor(ReferenceEntry<K, V> entry) {
        return new FutureValueReference(original, entry);
      }
      public V waitForValue() throws InterruptedException {
        boolean success = false;
        try {
          V value = StrategyImpl.this.waitForValue(original);
          success = true;
          return value;
        } finally {
          if (!success) {
            removeEntry();
          }
        }
      }
      void removeEntry() {
        internals.removeEntry(newEntry);
      }
    }
    public ReferenceEntry<K, V> getNext(
        ReferenceEntry<K, V> entry) {
      return entry.getNext();
    }
    public void setInternals(
        Internals<K, V, ReferenceEntry<K, V>> internals) {
      this.internals = internals;
    }
    private static final long serialVersionUID = 0;
    private void writeObject(ObjectOutputStream out)
        throws IOException {
      out.writeObject(keyStrength);
      out.writeObject(valueStrength);
      out.writeLong(expirationNanos);
      out.writeObject(internals);
      out.writeObject(map);
    }
    private static class Fields {
      static final Field keyStrength = findField("keyStrength");
      static final Field valueStrength = findField("valueStrength");
      static final Field expirationNanos = findField("expirationNanos");
      static final Field internals = findField("internals");
      static final Field map = findField("map");
      static Field findField(String name) {
        try {
          Field f = StrategyImpl.class.getDeclaredField(name);
          f.setAccessible(true);
          return f;
        } catch (NoSuchFieldException e) {
          throw new AssertionError(e);
        }
      }
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
      try {
        Fields.keyStrength.set(this, in.readObject());
        Fields.valueStrength.set(this, in.readObject());
        Fields.expirationNanos.set(this, in.readLong());
        Fields.internals.set(this, in.readObject());
        Fields.map.set(this, in.readObject());
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }
  private interface ValueReference<K, V> {
    V get();
    ValueReference<K, V> copyFor(ReferenceEntry<K, V> entry);
    V waitForValue() throws InterruptedException;
  }
  private static final ValueReference<Object, Object> COMPUTING
      = new ValueReference<Object, Object>() {
    public Object get() {
      return null;
    }
    public ValueReference<Object, Object> copyFor(
        ReferenceEntry<Object, Object> entry) {
      throw new AssertionError();
    }
    public Object waitForValue() {
      throw new AssertionError();
    }
  };
  @SuppressWarnings("unchecked")
  private static <K, V> ValueReference<K, V> computing() {
    return (ValueReference<K, V>) COMPUTING;
  }
  private static class NullOutputExceptionReference<K, V>
      implements ValueReference<K, V> {
    final String message;
    NullOutputExceptionReference(String message) {
      this.message = message;
    }
    public V get() {
      return null;
    }
    public ValueReference<K, V> copyFor(
        ReferenceEntry<K, V> entry) {
      return this;
    }
    public V waitForValue() {
      throw new NullOutputException(message);
    }
  }
  private static class ComputationExceptionReference<K, V>
      implements ValueReference<K, V> {
    final Throwable t;
    ComputationExceptionReference(Throwable t) {
      this.t = t;
    }
    public V get() {
      return null;
    }
    public ValueReference<K, V> copyFor(
        ReferenceEntry<K, V> entry) {
      return this;
    }
    public V waitForValue() {
      throw new AsynchronousComputationException(t);
    }
  }
  private static class QueueHolder {
    static final FinalizableReferenceQueue queue
        = new FinalizableReferenceQueue();
  }
  private interface ReferenceEntry<K, V> {
    ValueReference<K, V> getValueReference();
    void setValueReference(ValueReference<K, V> valueReference);
    void valueReclaimed();
    ReferenceEntry<K, V> getNext();
    int getHash();
    public K getKey();
  }
  private static class StrongEntry<K, V> implements ReferenceEntry<K, V> {
    final K key;
    StrongEntry(Internals<K, V, ReferenceEntry<K, V>> internals, K key,
        int hash) {
      this.internals = internals;
      this.key = key;
      this.hash = hash;
    }
    public K getKey() {
      return this.key;
    }
    final Internals<K, V, ReferenceEntry<K, V>> internals;
    final int hash;
    volatile ValueReference<K, V> valueReference = computing();
    public ValueReference<K, V> getValueReference() {
      return valueReference;
    }
    public void setValueReference(
        ValueReference<K, V> valueReference) {
      this.valueReference = valueReference;
    }
    public void valueReclaimed() {
      internals.removeEntry(this, null);
    }
    public ReferenceEntry<K, V> getNext() {
      return null;
    }
    public int getHash() {
      return hash;
    }
  }
  private static class LinkedStrongEntry<K, V> extends StrongEntry<K, V> {
    LinkedStrongEntry(Internals<K, V, ReferenceEntry<K, V>> internals,
        K key, int hash, ReferenceEntry<K, V> next) {
      super(internals, key, hash);
      this.next = next;
    }
    final ReferenceEntry<K, V> next;
    @Override public ReferenceEntry<K, V> getNext() {
      return next;
    }
  }
  private static class SoftEntry<K, V> extends FinalizableSoftReference<K>
      implements ReferenceEntry<K, V> {
    SoftEntry(Internals<K, V, ReferenceEntry<K, V>> internals, K key,
        int hash) {
      super(key, QueueHolder.queue);
      this.internals = internals;
      this.hash = hash;
    }
    public K getKey() {
      return get();
    }
    public void finalizeReferent() {
      internals.removeEntry(this);
    }
    final Internals<K, V, ReferenceEntry<K, V>> internals;
    final int hash;
    volatile ValueReference<K, V> valueReference = computing();
    public ValueReference<K, V> getValueReference() {
      return valueReference;
    }
    public void setValueReference(
        ValueReference<K, V> valueReference) {
      this.valueReference = valueReference;
    }
    public void valueReclaimed() {
      internals.removeEntry(this, null);
    }
    public ReferenceEntry<K, V> getNext() {
      return null;
    }
    public int getHash() {
      return hash;
    }
  }
  private static class LinkedSoftEntry<K, V> extends SoftEntry<K, V> {
    LinkedSoftEntry(Internals<K, V, ReferenceEntry<K, V>> internals,
        K key, int hash, ReferenceEntry<K, V> next) {
      super(internals, key, hash);
      this.next = next;
    }
    final ReferenceEntry<K, V> next;
    @Override public ReferenceEntry<K, V> getNext() {
      return next;
    }
  }
  private static class WeakEntry<K, V> extends FinalizableWeakReference<K>
      implements ReferenceEntry<K, V> {
    WeakEntry(Internals<K, V, ReferenceEntry<K, V>> internals, K key,
        int hash) {
      super(key, QueueHolder.queue);
      this.internals = internals;
      this.hash = hash;
    }
    public K getKey() {
      return get();
    }
    public void finalizeReferent() {
      internals.removeEntry(this);
    }
    final Internals<K, V, ReferenceEntry<K, V>> internals;
    final int hash;
    volatile ValueReference<K, V> valueReference = computing();
    public ValueReference<K, V> getValueReference() {
      return valueReference;
    }
    public void setValueReference(
        ValueReference<K, V> valueReference) {
      this.valueReference = valueReference;
    }
    public void valueReclaimed() {
      internals.removeEntry(this, null);
    }
    public ReferenceEntry<K, V> getNext() {
      return null;
    }
    public int getHash() {
      return hash;
    }
  }
  private static class LinkedWeakEntry<K, V> extends WeakEntry<K, V> {
    LinkedWeakEntry(Internals<K, V, ReferenceEntry<K, V>> internals,
        K key, int hash, ReferenceEntry<K, V> next) {
      super(internals, key, hash);
      this.next = next;
    }
    final ReferenceEntry<K, V> next;
    @Override public ReferenceEntry<K, V> getNext() {
      return next;
    }
  }
  private static class WeakValueReference<K, V>
      extends FinalizableWeakReference<V>
      implements ValueReference<K, V> {
    final ReferenceEntry<K, V> entry;
    WeakValueReference(V referent, ReferenceEntry<K, V> entry) {
      super(referent, QueueHolder.queue);
      this.entry = entry;
    }
    public void finalizeReferent() {
      entry.valueReclaimed();
    }
    public ValueReference<K, V> copyFor(
        ReferenceEntry<K, V> entry) {
      return new WeakValueReference<K, V>(get(), entry);
    }
    public V waitForValue() {
      return get();
    }
  }
  private static class SoftValueReference<K, V>
      extends FinalizableSoftReference<V>
      implements ValueReference<K, V> {
    final ReferenceEntry<K, V> entry;
    SoftValueReference(V referent, ReferenceEntry<K, V> entry) {
      super(referent, QueueHolder.queue);
      this.entry = entry;
    }
    public void finalizeReferent() {
      entry.valueReclaimed();
    }
    public ValueReference<K, V> copyFor(
        ReferenceEntry<K, V> entry) {
      return new SoftValueReference<K, V>(get(), entry);
    }
    public V waitForValue() {
      return get();
    }
  }
  private static class StrongValueReference<K, V>
      implements ValueReference<K, V> {
    final V referent;
    StrongValueReference(V referent) {
      this.referent = referent;
    }
    public V get() {
      return referent;
    }
    public ValueReference<K, V> copyFor(
        ReferenceEntry<K, V> entry) {
      return this;
    }
    public V waitForValue() {
      return get();
    }
  }
}
