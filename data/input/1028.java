public final class java_util_Date extends AbstractTest<Date> {
    public static void main(String[] args) {
        new java_util_Date().test(true);
    }
    protected Date getObject() {
        return new Date(System.currentTimeMillis());
    }
    protected Date getAnotherObject() {
        return new Date(0L);
    }
}
