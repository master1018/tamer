public abstract class  AtomicIntegerFieldUpdater<T> {
    public static <U> AtomicIntegerFieldUpdater<U> newUpdater(Class<U> tclass, String fieldName) {
        return new AtomicIntegerFieldUpdaterImpl<U>(tclass, fieldName);
    }
    protected AtomicIntegerFieldUpdater() {
    }
    public abstract boolean compareAndSet(T obj, int expect, int update);
    public abstract boolean weakCompareAndSet(T obj, int expect, int update);
    public abstract void set(T obj, int newValue);
    public abstract void lazySet(T obj, int newValue);
    public abstract int get(T obj);
    public int getAndSet(T obj, int newValue) {
        for (;;) {
            int current = get(obj);
            if (compareAndSet(obj, current, newValue))
                return current;
        }
    }
    public int getAndIncrement(T obj) {
        for (;;) {
            int current = get(obj);
            int next = current + 1;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }
    public int getAndDecrement(T obj) {
        for (;;) {
            int current = get(obj);
            int next = current - 1;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }
    public int getAndAdd(T obj, int delta) {
        for (;;) {
            int current = get(obj);
            int next = current + delta;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }
    public int incrementAndGet(T obj) {
        for (;;) {
            int current = get(obj);
            int next = current + 1;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }
    public int decrementAndGet(T obj) {
        for (;;) {
            int current = get(obj);
            int next = current - 1;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }
    public int addAndGet(T obj, int delta) {
        for (;;) {
            int current = get(obj);
            int next = current + delta;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }
    private static class AtomicIntegerFieldUpdaterImpl<T> extends AtomicIntegerFieldUpdater<T> {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final long offset;
        private final Class<T> tclass;
        private final Class cclass;
        AtomicIntegerFieldUpdaterImpl(Class<T> tclass, String fieldName) {
            Field field = null;
            Class caller = null;
            int modifiers = 0;
            try {
                field = tclass.getDeclaredField(fieldName);
                caller = sun.reflect.Reflection.getCallerClass(3);
                modifiers = field.getModifiers();
                sun.reflect.misc.ReflectUtil.ensureMemberAccess(
                    caller, tclass, null, modifiers);
                sun.reflect.misc.ReflectUtil.checkPackageAccess(tclass);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            Class fieldt = field.getType();
            if (fieldt != int.class)
                throw new IllegalArgumentException("Must be integer type");
            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("Must be volatile type");
            this.cclass = (Modifier.isProtected(modifiers) &&
                           caller != tclass) ? caller : null;
            this.tclass = tclass;
            offset = unsafe.objectFieldOffset(field);
        }
        private void fullCheck(T obj) {
            if (!tclass.isInstance(obj))
                throw new ClassCastException();
            if (cclass != null)
                ensureProtectedAccess(obj);
        }
        public boolean compareAndSet(T obj, int expect, int update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.compareAndSwapInt(obj, offset, expect, update);
        }
        public boolean weakCompareAndSet(T obj, int expect, int update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.compareAndSwapInt(obj, offset, expect, update);
        }
        public void set(T obj, int newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            unsafe.putIntVolatile(obj, offset, newValue);
        }
        public void lazySet(T obj, int newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            unsafe.putOrderedInt(obj, offset, newValue);
        }
        public final int get(T obj) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.getIntVolatile(obj, offset);
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
