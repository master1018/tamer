public final class Test6187118 extends AbstractTest {
    public static void main(String[] args) {
        new Test6187118().test(true);
    }
    protected ImmutableList<String> getObject() {
        return new ImmutableList<String>();
    }
    protected ImmutableList<String> getAnotherObject() {
        return new ImmutableList<String>().add("1").add("2").add("3").add("4");
    }
    protected void initialize(XMLEncoder encoder) {
        encoder.setPersistenceDelegate(
                ImmutableList.class,
                new PersistenceDelegate() {
                    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
                        return oldInstance.equals(newInstance);
                    }
                    protected Expression instantiate(Object oldInstance, Encoder out) {
                        ImmutableList list = (ImmutableList) oldInstance;
                        if (!list.hasEntries()) {
                            return getExpression(oldInstance, ImmutableList.class, "new");
                        }
                        Object object = list.getLast();
                        ImmutableList shortenedList = list.removeLast();
                        return getExpression(oldInstance, shortenedList, "add", object);
                    }
                    private Expression getExpression(Object value, Object target, String method, Object... args) {
                        return new Expression(value, target, method, args);
                    }
                }
        );
    }
    public static final class ImmutableList<T> implements Iterable {
        private final List<T> list = new ArrayList<T>();
        public ImmutableList() {
        }
        private ImmutableList(Iterable<T> iterable) {
            for (T object : iterable) {
                this.list.add(object);
            }
        }
        public Iterator<T> iterator() {
            return Collections.unmodifiableList(this.list).iterator();
        }
        public ImmutableList<T> add(T object) {
            ImmutableList<T> list = new ImmutableList<T>(this.list);
            list.list.add(object);
            return list;
        }
        public ImmutableList<T> removeLast() {
            ImmutableList<T> list = new ImmutableList<T>(this.list);
            int size = list.list.size();
            if (0 < size) {
                list.list.remove(size - 1);
            }
            return list;
        }
        public T getLast() {
            int size = this.list.size();
            return (0 < size)
                    ? this.list.get(size - 1)
                    : null;
        }
        public boolean hasEntries() {
            return 0 < this.list.size();
        }
        public boolean equals(Object object) {
            if (object instanceof ImmutableList) {
                ImmutableList list = (ImmutableList) object;
                return this.list.equals(list.list);
            }
            return false;
        }
        public int hashCode() {
            return this.list.hashCode();
        }
        public String toString() {
            return this.list.toString();
        }
    }
}
