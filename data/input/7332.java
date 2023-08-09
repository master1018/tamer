public final class java_sql_Timestamp extends AbstractTest<Timestamp> {
    public static void main(String[] args) {
        new java_sql_Timestamp().test(true);
    }
    protected Timestamp getObject() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        timestamp.setNanos(1 + timestamp.getNanos());
        return timestamp;
    }
    protected Timestamp getAnotherObject() {
        return new Timestamp(0L);
    }
}
