public class test {
    @Override
    public Channel getChannel() {
        return readable() ? readChannel : writeChannel;
    }
}
