public class AtomicStampedReference<V>  {
    private static class ReferenceIntegerPair<T> {
        private final T reference;
        private final int integer;
        ReferenceIntegerPair(T r, int i) {
            reference = r; integer = i;
        }
    }
    private final AtomicReference<ReferenceIntegerPair<V>>  atomicRef;
    public AtomicStampedReference(V initialRef, int initialStamp) {
        atomicRef = new AtomicReference<ReferenceIntegerPair<V>>
            (new ReferenceIntegerPair<V>(initialRef, initialStamp));
    }
    public V getReference() {
        return atomicRef.get().reference;
    }
    public int getStamp() {
        return atomicRef.get().integer;
    }
    public V get(int[] stampHolder) {
        ReferenceIntegerPair<V> p = atomicRef.get();
        stampHolder[0] = p.integer;
        return p.reference;
    }
    public boolean weakCompareAndSet(V      expectedReference,
                                     V      newReference,
                                     int    expectedStamp,
                                     int    newStamp) {
        ReferenceIntegerPair<V> current = atomicRef.get();
        return  expectedReference == current.reference &&
            expectedStamp == current.integer &&
            ((newReference == current.reference &&
              newStamp == current.integer) ||
             atomicRef.weakCompareAndSet(current,
                                     new ReferenceIntegerPair<V>(newReference,
                                                              newStamp)));
    }
    public boolean compareAndSet(V      expectedReference,
                                 V      newReference,
                                 int    expectedStamp,
                                 int    newStamp) {
        ReferenceIntegerPair<V> current = atomicRef.get();
        return  expectedReference == current.reference &&
            expectedStamp == current.integer &&
            ((newReference == current.reference &&
              newStamp == current.integer) ||
             atomicRef.compareAndSet(current,
                                     new ReferenceIntegerPair<V>(newReference,
                                                              newStamp)));
    }
    public void set(V newReference, int newStamp) {
        ReferenceIntegerPair<V> current = atomicRef.get();
        if (newReference != current.reference || newStamp != current.integer)
            atomicRef.set(new ReferenceIntegerPair<V>(newReference, newStamp));
    }
    public boolean attemptStamp(V expectedReference, int newStamp) {
        ReferenceIntegerPair<V> current = atomicRef.get();
        return  expectedReference == current.reference &&
            (newStamp == current.integer ||
             atomicRef.compareAndSet(current,
                                     new ReferenceIntegerPair<V>(expectedReference,
                                                              newStamp)));
    }
}
