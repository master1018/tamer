public class DevPollSelectorProvider
    extends SelectorProviderImpl
{
    public AbstractSelector openSelector() throws IOException {
        return new DevPollSelectorImpl(this);
    }
    public Channel inheritedChannel() throws IOException {
        return InheritedChannel.getChannel();
    }
}
