public class PollSelectorProvider
    extends SelectorProviderImpl
{
    public AbstractSelector openSelector() throws IOException {
        return new PollSelectorImpl(this);
    }
    public Channel inheritedChannel() throws IOException {
        return InheritedChannel.getChannel();
    }
}
