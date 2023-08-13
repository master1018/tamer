public class test {
    public void channelUserJoined(ChannelUserEvent e) {
        if (e.getChannel().equals(this)) {
            addChannelUser(e.getUser());
        }
    }
}
