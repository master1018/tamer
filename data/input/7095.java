public final class java_util_Collections_CheckedList extends AbstractTest<List<String>> {
    public static void main(String[] args) {
        new java_util_Collections_CheckedList().test(true);
    }
    protected List<String> getObject() {
        List<String> list = Collections.singletonList("string");
        return Collections.checkedList(list, String.class);
    }
    protected List<String> getAnotherObject() {
        List<String> list = Collections.emptyList();
        return Collections.checkedList(list, String.class);
    }
}
