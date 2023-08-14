public abstract class AtomicReferenceFieldUpdater<T, V> {
    public static <U, W> AtomicReferenceFieldUpdater<U,W> newUpdater(Class<U> tclass, Class<W> vclass, String fieldName) {
        return new AtomicReferenceFieldUpdaterImpl<U,W>(tclass,
                                                        vclass,
                                                        fieldName);
    }
    protected AtomicReferenceFieldUpdater() {
    }
    public abstract boolean compareAndSet(T obj, V expect, V update);
    public abstract boolean weakCompareAndSet(T obj, V expect, V update);
    public abstract void set(T obj, V newValue);
    public abstract void lazySet(T obj, V newValue);
    public abstract V get(T obj);
    public V getAndSet(T obj, V newValue) {
        for (;;) {
            V current = get(obj);
            if (compareAndSet(obj, current, newValue))
                return current;
        }
    }
    private static final class AtomicReferenceFieldUpdaterImpl<T,V>
        extends AtomicReferenceFieldUpdater<T,V> {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final long offset;
        private final Class<T> tclass;
        private final Class<V> vclass;
        private final Class cclass;
        AtomicReferenceFieldUpdaterImpl(Class<T> tclass,
                                        Class<V> vclass,
                                        String fieldName) {
            Field field = null;
            Class fieldClass = null;
            Class caller = null;
            int modifiers = 0;
            try {
                field = tclass.getDeclaredField(fieldName);
                caller = sun.reflect.Reflection.getCallerClass(3);
                modifiers = field.getModifiers();
                sun.reflect.misc.ReflectUtil.ensureMemberAccess(
                    caller, tclass, null, modifiers);
                sun.reflect.misc.ReflectUtil.checkPackageAccess(tclass);
                fieldClass = field.getType();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            if (vclass != fieldClass)
                throw new ClassCastException();
            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("Must be volatile type");
            this.cclass = (Modifier.isProtected(modifiers) &&
                           caller != tclass) ? caller : null;
            this.tclass = tclass;
            if (vclass == Object.class)
                this.vclass = null;
            else
                this.vclass = vclass;
            offset = unsafe.objectFieldOffset(field);
        }
        void targetCheck(T obj) {
            if (!tclass.isInstance(obj))
                throw new ClassCastException();
            if (cclass != null)
                ensureProtectedAccess(obj);
        }
        void updateCheck(T obj, V update) {
            if (!tclass.isInstance(obj) ||
                (update != null && vclass != null && !vclass.isInstance(update)))
                throw new ClassCastException();
            if (cclass != null)
                ensureProtectedAccess(obj);
        }
        public boolean compareAndSet(T obj, V expect, V update) {
            if (obj == null || obj.getClass() != tclass || cclass != null ||
                (update != null && vclass != null &&
                 vclass != update.getClass()))
                updateCheck(obj, update);
            return unsafe.compareAndSwapObject(obj, offset, expect, update);
        }
        public boolean weakCompareAndSet(T obj, V expect, V update) {
            if (obj == null || obj.getClass() != tclass || cclass != null ||
                (update != null && vclass != null &&
                 vclass != update.getClass()))
                updateCheck(obj, update);
            return unsafe.compareAndSwapObject(obj, offset, expect, update);
        }
        public void set(T obj, V newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null ||
                (newValue != null && vclass != null &&
                 vclass != newValue.getClass()))
                updateCheck(obj, newValue);
            unsafe.putObjectVolatile(obj, offset, newValue);
        }
        public void lazySet(T obj, V newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null ||
                (newValue != null && vclass != null &&
                 vclass != newValue.getClass()))
                updateCheck(obj, newValue);
            unsafe.putOrderedObject(obj, offset, newValue);
        }
        public V get(T obj) {
            if (obj == null || obj.getClass() != tclass || cclass != null)
                targetCheck(obj);
            return (V)unsafe.getObjectVolatile(obj, offset);
        }
        private void ensureProtectedAccess(T obj) {
            if (cclass.isInstance(obj)) {
                return;
            }
            throw new RuntimeException(
                new IllegalAccessException("Class " +
                    cclass.getName() +
                    " can not access a protected member of class " +
                    tclass.getName() +
                    " using an instance of " +
                    obj.getClass().getName()
                )
            );
        }
    }
}
