public class test {
    public static ChannelService getChannelService(RemoteChannelServiceAsync api) {
        return new ChannelServiceImpl(api);
    }
}
