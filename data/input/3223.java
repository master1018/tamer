public class test {
    public ChannelTree getChannelTree() throws SAPIException {
        return ChannelTree.createFromChannelMap(getChannelsMap());
    }
}
