public abstract class  AtomicLongFieldUpdater<T> {
    public static <U> AtomicLongFieldUpdater<U> newUpdater(Class<U> tclass, String fieldName) {
        if (AtomicLong.VM_SUPPORTS_LONG_CAS)
            return new CASUpdater<U>(tclass, fieldName);
        else
            return new LockedUpdater<U>(tclass, fieldName);
    }
    protected AtomicLongFieldUpdater() {
    }
    public abstract boolean compareAndSet(T obj, long expect, long update);
    public abstract boolean weakCompareAndSet(T obj, long expect, long update);
    public abstract void set(T obj, long newValue);
    public abstract void lazySet(T obj, long newValue);
    public abstract long get(T obj);
    public long getAndSet(T obj, long newValue) {
        for (;;) {
            long current = get(obj);
            if (compareAndSet(obj, current, newValue))
                return current;
        }
    }
    public long getAndIncrement(T obj) {
        for (;;) {
            long current = get(obj);
            long next = current + 1;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }
    public long getAndDecrement(T obj) {
        for (;;) {
            long current = get(obj);
            long next = current - 1;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }
    public long getAndAdd(T obj, long delta) {
        for (;;) {
            long current = get(obj);
            long next = current + delta;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }
    public long incrementAndGet(T obj) {
        for (;;) {
            long current = get(obj);
            long next = current + 1;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }
    public long decrementAndGet(T obj) {
        for (;;) {
            long current = get(obj);
            long next = current - 1;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }
    public long addAndGet(T obj, long delta) {
        for (;;) {
            long current = get(obj);
            long next = current + delta;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }
    private static class CASUpdater<T> extends AtomicLongFieldUpdater<T> {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final long offset;
        private final Class<T> tclass;
        private final Class cclass;
        CASUpdater(Class<T> tclass, String fieldName) {
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
            if (fieldt != long.class)
                throw new IllegalArgumentException("Must be long type");
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
        public boolean compareAndSet(T obj, long expect, long update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.compareAndSwapLong(obj, offset, expect, update);
        }
        public boolean weakCompareAndSet(T obj, long expect, long update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.compareAndSwapLong(obj, offset, expect, update);
        }
        public void set(T obj, long newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            unsafe.putLongVolatile(obj, offset, newValue);
        }
        public void lazySet(T obj, long newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            unsafe.putOrderedLong(obj, offset, newValue);
        }
        public long get(T obj) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.getLongVolatile(obj, offset);
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
    private static class LockedUpdater<T> extends AtomicLongFieldUpdater<T> {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final long offset;
        private final Class<T> tclass;
        private final Class cclass;
        LockedUpdater(Class<T> tclass, String fieldName) {
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
            if (fieldt != long.class)
                throw new IllegalArgumentException("Must be long type");
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
        public boolean compareAndSet(T obj, long expect, long update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            synchronized (this) {
                long v = unsafe.getLong(obj, offset);
                if (v != expect)
                    return false;
                unsafe.putLong(obj, offset, update);
                return true;
            }
        }
        public boolean weakCompareAndSet(T obj, long expect, long update) {
            return compareAndSet(obj, expect, update);
        }
        public void set(T obj, long newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            synchronized (this) {
                unsafe.putLong(obj, offset, newValue);
            }
        }
        public void lazySet(T obj, long newValue) {
            set(obj, newValue);
        }
        public long get(T obj) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            synchronized (this) {
                return unsafe.getLong(obj, offset);
            }
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
