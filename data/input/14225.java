public final class java_util_Collections_CheckedRandomAccessList extends AbstractTest<List<String>> {
    public static void main(String[] args) {
        new java_util_Collections_CheckedRandomAccessList().test(true);
    }
    protected List<String> getObject() {
        List<String> list = new ArrayList<String>();
        list.add("string");
        return Collections.checkedList(list, String.class);
    }
    protected List<String> getAnotherObject() {
        List<String> list = new ArrayList<String>();
        return Collections.checkedList(list, String.class);
    }
}
