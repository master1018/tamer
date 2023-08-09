public final class java_util_Collections_SynchronizedRandomAccessList extends AbstractTest<List<String>> {
    public static void main(String[] args) {
        new java_util_Collections_SynchronizedRandomAccessList().test(true);
    }
    protected List<String> getObject() {
        List<String> list = new ArrayList<String>();
        list.add("string");
        return Collections.synchronizedList(list);
    }
    protected List<String> getAnotherObject() {
        List<String> list = new ArrayList<String>();
        return Collections.synchronizedList(list);
    }
}
