public class test {
    public DirectChannelSource(final String pv) {
        this(ChannelFactory.defaultFactory().getChannel(pv));
    }
}
