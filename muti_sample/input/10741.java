public final class java_util_Collections_SingletonList extends AbstractTest<List<String>> {
    public static void main(String[] args) {
        new java_util_Collections_SingletonList().test(true);
    }
    protected List<String> getObject() {
        return Collections.singletonList("string");
    }
    protected List<String> getAnotherObject() {
        return Collections.singletonList("object");
    }
}
