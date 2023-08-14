public class test {
    public Channel join(final ClientSession session) {
        getChannel().join(session);
        return this;
    }
}
