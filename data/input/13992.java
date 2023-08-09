public final class java_util_Collections_SingletonSet extends AbstractTest<Set<String>> {
    public static void main(String[] args) {
        new java_util_Collections_SingletonSet().test(true);
    }
    protected Set<String> getObject() {
        return Collections.singleton("string");
    }
    protected Set<String> getAnotherObject() {
        return Collections.singleton("object");
    }
}
