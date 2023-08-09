public abstract class Entry {
    public static final String[] ID_PROJECTION = { "_id" };
    @Column("_id")
    public long id = 0;
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Table {
        String value();
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Column {
        String value();
        boolean indexed() default false;
        boolean fullText() default false;
    }
    public void clear() {
        id = 0;
    }
    public void setPropertyFromXml(String uri, String localName, Attributes attrs, String content) {
        throw new UnsupportedOperationException("Entry class does not support XML parsing");
    }
}
