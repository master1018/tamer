public class EventLogTags {
    public static class Description {
        public final int mTag;
        public final String mName;
        Description(int tag, String name) {
            mTag = tag;
            mName = name;
        }
    }
    public EventLogTags() throws IOException {}
    public EventLogTags(BufferedReader input) throws IOException {}
    public Description get(String name) { return null; }
    public Description get(int tag) { return null; }
}
