public class EPollSelectorProvider
    extends SelectorProviderImpl
{
    public AbstractSelector openSelector() throws IOException {
        return new EPollSelectorImpl(this);
    }
    public Channel inheritedChannel() throws IOException {
        return InheritedChannel.getChannel();
    }
}
