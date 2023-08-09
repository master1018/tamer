public final class java_util_ArrayList extends AbstractTest<List<String>> {
    public static void main(String[] args) {
        new java_util_ArrayList().test(true);
    }
    protected List<String> getObject() {
        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add("string");
        return list;
    }
    protected List<String> getAnotherObject() {
        return new ArrayList<String>();
    }
}
