public class test {
    public void shutdown() throws IOException {
        getChannel().send(SHUTDOWN, Value.create());
    }
}
