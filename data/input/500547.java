public class AtomicMarkableReference<V>  {
    private static class ReferenceBooleanPair<T> {
        private final T reference;
        private final boolean bit;
        ReferenceBooleanPair(T r, boolean i) {
            reference = r; bit = i;
        }
    }
    private final AtomicReference<ReferenceBooleanPair<V>>  atomicRef;
    public AtomicMarkableReference(V initialRef, boolean initialMark) {
        atomicRef = new AtomicReference<ReferenceBooleanPair<V>> (new ReferenceBooleanPair<V>(initialRef, initialMark));
    }
    public V getReference() {
        return atomicRef.get().reference;
    }
    public boolean isMarked() {
        return atomicRef.get().bit;
    }
    public V get(boolean[] markHolder) {
        ReferenceBooleanPair<V> p = atomicRef.get();
        markHolder[0] = p.bit;
        return p.reference;
    }
    public boolean weakCompareAndSet(V       expectedReference,
                                     V       newReference,
                                     boolean expectedMark,
                                     boolean newMark) {
        ReferenceBooleanPair<V> current = atomicRef.get();
        return  expectedReference == current.reference &&
            expectedMark == current.bit &&
            ((newReference == current.reference && newMark == current.bit) ||
             atomicRef.weakCompareAndSet(current,
                                     new ReferenceBooleanPair<V>(newReference,
                                                              newMark)));
    }
    public boolean compareAndSet(V       expectedReference,
                                 V       newReference,
                                 boolean expectedMark,
                                 boolean newMark) {
        ReferenceBooleanPair<V> current = atomicRef.get();
        return  expectedReference == current.reference &&
            expectedMark == current.bit &&
            ((newReference == current.reference && newMark == current.bit) ||
             atomicRef.compareAndSet(current,
                                     new ReferenceBooleanPair<V>(newReference,
                                                              newMark)));
    }
    public void set(V newReference, boolean newMark) {
        ReferenceBooleanPair<V> current = atomicRef.get();
        if (newReference != current.reference || newMark != current.bit)
            atomicRef.set(new ReferenceBooleanPair<V>(newReference, newMark));
    }
    public boolean attemptMark(V expectedReference, boolean newMark) {
        ReferenceBooleanPair<V> current = atomicRef.get();
        return  expectedReference == current.reference &&
            (newMark == current.bit ||
             atomicRef.compareAndSet
             (current, new ReferenceBooleanPair<V>(expectedReference,
                                                newMark)));
    }
}
