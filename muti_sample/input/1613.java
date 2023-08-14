public final class java_sql_Time extends AbstractTest<Time> {
    public static void main(String[] args) {
        new java_sql_Time().test(true);
    }
    protected Time getObject() {
        return new Time(System.currentTimeMillis());
    }
    protected Time getAnotherObject() {
        return new Time(0L);
    }
}
