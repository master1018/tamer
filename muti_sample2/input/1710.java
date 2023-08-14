public class test {
    public boolean isInChannel(Channel channel) {
        return channelsList.containsKey(channel.getChannelType());
    }
}
