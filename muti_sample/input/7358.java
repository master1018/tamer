public final class java_util_Collections_SynchronizedSet extends AbstractTest<Set<String>> {
    public static void main(String[] args) {
        new java_util_Collections_SynchronizedSet().test(true);
    }
    protected Set<String> getObject() {
        Set<String> set = Collections.singleton("string");
        return Collections.synchronizedSet(set);
    }
    protected Set<String> getAnotherObject() {
        Set<String> set = Collections.emptySet();
        return Collections.synchronizedSet(set);
    }
}
