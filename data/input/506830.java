public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E>
        implements Cloneable, Serializable {
    static  LangAccess LANG_BOOTSTRAP = null;
    private static final long serialVersionUID = 1009687484059888093L;
    final Class<E> elementClass;
    EnumSet(Class<E> cls) {
        elementClass = cls;
    }
    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
        if (!elementType.isEnum()) {
            throw new ClassCastException();
        }
        E[] enums = SpecialAccess.LANG.getEnumValuesInOrder(elementType);
        if (enums.length <= 64) {
            return new MiniEnumSet<E>(elementType, enums);
        }
        return new HugeEnumSet<E>(elementType, enums);
    }
    public static <E extends Enum<E>> EnumSet<E> allOf(Class<E> elementType) {
        EnumSet<E> set = noneOf(elementType);
        set.complement();
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> copyOf(EnumSet<E> s) {
        EnumSet<E> set = EnumSet.noneOf(s.elementClass);
        set.addAll(s);
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> copyOf(Collection<E> c) {
        if (c instanceof EnumSet) {
            return copyOf((EnumSet<E>) c);
        }
        if (c.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Iterator<E> iterator = c.iterator();
        E element = iterator.next();
        EnumSet<E> set = EnumSet.noneOf(element.getDeclaringClass());
        set.add(element);
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> complementOf(EnumSet<E> s) {
        EnumSet<E> set = EnumSet.noneOf(s.elementClass);
        set.addAll(s);
        set.complement();
        return set;
    }
    abstract void complement();
    public static <E extends Enum<E>> EnumSet<E> of(E e) {
        EnumSet<E> set = EnumSet.noneOf(e.getDeclaringClass());
        set.add(e);
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
        EnumSet<E> set = of(e1);
        set.add(e2);
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
        EnumSet<E> set = of(e1, e2);
        set.add(e3);
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
        EnumSet<E> set = of(e1, e2, e3);
        set.add(e4);
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4, E e5) {
        EnumSet<E> set = of(e1, e2, e3, e4);
        set.add(e5);
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> of(E start, E... others) {
        EnumSet<E> set = of(start);
        for (E e : others) {
            set.add(e);
        }
        return set;
    }
    public static <E extends Enum<E>> EnumSet<E> range(E start, E end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException();
        }
        EnumSet<E> set = EnumSet.noneOf(start.getDeclaringClass());
        set.setRange(start, end);
        return set;
    }
    abstract void setRange(E start, E end);
    @SuppressWarnings("unchecked")
    @Override
    public EnumSet<E> clone() {
        try {
            return (EnumSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
    boolean isValidType(Class<?> cls) {
        return cls == elementClass || cls.getSuperclass() == elementClass;
    }
    private static class SerializationProxy<E extends Enum<E>> implements
            Serializable {
        private static final long serialVersionUID = 362491234563181265L;
        private Class<E> elementType;
        private E[] elements;
        private Object readResolve() {
            EnumSet<E> set = EnumSet.noneOf(elementType);
            for (E e : elements) {
                set.add(e);
            }
            return set;
        }
    }
    @SuppressWarnings("unchecked")
    Object writeReplace() {
        SerializationProxy proxy = new SerializationProxy();
        proxy.elements = toArray(new Enum[0]);
        proxy.elementType = elementClass;
        return proxy;
    }
}
