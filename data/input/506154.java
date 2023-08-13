 final class LangAccessImpl extends LangAccess {
     static final LangAccessImpl THE_ONE = new LangAccessImpl();
    private LangAccessImpl() {
    }
    public <T> T[] getEnumValuesInOrder(Class<T> clazz) {
        ClassCache<T> cache = clazz.getClassCache();
        return cache.getEnumValuesInOrder();
    }
    public void unpark(Thread thread) {
        thread.unpark();
    }
    public void parkFor(long nanos) {
        Thread.currentThread().parkFor(nanos);
    }
    public void parkUntil(long time) {
        Thread.currentThread().parkUntil(time);
    }
}
